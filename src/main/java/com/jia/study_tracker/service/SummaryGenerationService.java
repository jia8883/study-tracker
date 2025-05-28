package com.jia.study_tracker.service;

import com.jia.study_tracker.domain.StudyLog;
import com.jia.study_tracker.domain.Summary;
import com.jia.study_tracker.domain.SummaryType;
import com.jia.study_tracker.domain.User;
import com.jia.study_tracker.exception.InvalidOpenAIResponseException;
import com.jia.study_tracker.exception.OpenAIClientException;
import com.jia.study_tracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.jia.study_tracker.dto.SummaryRetryRequest;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDate;
import java.util.List;

/**
 * 사용자별 로그를 조회하고, OpenAI를 통해 요약을 생성하여 DB에 저장하는 스케줄러 컴포넌트
 *
 * 주요 책임:
 * - 전체 사용자 조회
 * - 각 사용자의 StudyLog 가져오기
 * - OpenAI API 호출을 통해 요약 및 피드백 생성
 * - 결과를 Summary 엔티티로 저장
 * - 사용자에게 슬랙으로 AI 메시지 전송
 *
 * 예외 상황 처리:
 * - OpenAI 응답 이상 또는 호출 실패 시, 슬랙으로 사용자에게 오류 메시지를 알림
 * - 실패한 요청은 Redis 큐에 등록되어 재시도 프로세서에서 후속 처리됨
 *
 * 신뢰성 보장:
 * - 최소 1회 이상 요약을 시도(at-least-once)하는 구조를 통해
 *   사용자 로그가 누락되지 않도록 설계됨
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SummaryGenerationService {

    private final UserRepository userRepository;
    private final StudyLogQueryService studyLogQueryService;
    private final OpenAIClient openAIClient;
    private final SlackNotificationService slackNotificationService;
    private final SummarySaver summarySaver;
    private final RedisTemplate<String, SummaryRetryRequest> redisTemplate;

    /**
     * 스케줄러에서 호출됨
     */
    public void generateSummaries(LocalDate date, SummaryType type) {

        log.info("📅 스케줄러 시작 - date: {}, type: {}", date, type);
        log.info("👥 전체 유저 수: {}", userRepository.count());

        userRepository.findAll().forEach(user -> {
            log.info("➡ 사용자: {} ({})", user.getSlackUsername(), user.getSlackUserId());
            try {
                processOneUser(user, date, type);
            } catch (Exception e) {
                log.error("🚨 [{}] 요약 처리 중 예외 발생: {}", user.getSlackUsername(), e.getMessage(), e);
            }
        });
    }

    /**
     * 한 유저에 대해 로그 조회 → AI 요약 생성 → 저장 → 슬랙 전송 흐름을 처리
     */
    private void processOneUser(User user, LocalDate date, SummaryType type) {
        log.debug("🕵️ [{}] {} 요약 시작", user.getSlackUsername(), type);

        List<StudyLog> logs = studyLogQueryService.getLogs(user.getSlackUserId(), date, type);
        log.debug("📝 [{}] 로그 수: {}", user.getSlackUsername(), logs.size());


        if (logs.isEmpty()) {
            log.debug("[{}] {} 로그 없음 - 요약 생략", user.getSlackUsername(), type);
            return;
        }

        Summary summary;
        try {
            var result = openAIClient.generateSummaryAndFeedback(logs);
            summary = new Summary(
                    date,
                    result.getSummary(),
                    result.getFeedback(),
                    true,
                    null,
                    user,
                    type
            );
        } catch (InvalidOpenAIResponseException e) {
            log.warn("⚠️ [{}] {} 요약 생성 실패 - OpenAI 응답 이상: {}", user.getSlackUsername(), type, e.getMessage());

            slackNotificationService.sendErrorNotice(user, date, type);
            registerRetry(user, date, type);
            return;
        } catch (OpenAIClientException e) {
            log.error("❌ [{}] {} API 호출 실패 - {}", user.getSlackUsername(), type, e.getMessage());

            registerRetry(user, date, type);
            return;
        }

        summarySaver.save(summary);
        slackNotificationService.sendSummaryToUser(user, summary);
    }

    private void registerRetry(User user, LocalDate date, SummaryType type) {
        SummaryRetryRequest retryRequest = new SummaryRetryRequest(
                user.getSlackUserId(),
                user.getSlackUsername(),
                type.name(),
                date.toString(),
                0
        );
        redisTemplate.opsForList().rightPush("summary-retry-queue", retryRequest);
    }
}

