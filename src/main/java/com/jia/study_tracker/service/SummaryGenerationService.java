package com.jia.study_tracker.service;

import com.jia.study_tracker.domain.StudyLog;
import com.jia.study_tracker.domain.Summary;
import com.jia.study_tracker.domain.SummaryType;
import com.jia.study_tracker.domain.User;
import com.jia.study_tracker.exception.InvalidOpenAIResponseException;
import com.jia.study_tracker.exception.OpenAIClientException;
import com.jia.study_tracker.repository.SummaryRepository;
import com.jia.study_tracker.repository.UserRepository;
import com.jia.study_tracker.service.dto.SummaryResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SummaryGenerationService {

    private final UserRepository userRepository;
    private final StudyLogQueryService studyLogQueryService;
    private final OpenAIClient openAIClient;
    private final SummaryRepository summaryRepository;

    public void generateSummaries(LocalDate date, SummaryType type) {
        List<User> users = userRepository.findAll();

        for (User user : users) {
            List<StudyLog> logs = studyLogQueryService.getLogs(user.getSlackUserId(), date, type);

            if (logs.isEmpty()) {
                log.info("❌ [{}] {} 로그 없음 - 요약 생략", user.getSlackUsername(), type);
                continue;
            }

            try {
                SummaryResult result = openAIClient.generateSummaryAndFeedback(logs);
                summaryRepository.save(new Summary(
                        date,
                        result.getSummary(),
                        result.getFeedback(),
                        true,
                        null,
                        user,
                        type
                ));
                log.info("✅ [{}] {} 요약/피드백 저장 완료", user.getSlackUsername(), type);

            } catch (InvalidOpenAIResponseException e) {
                log.warn("⚠️ [{}] {} 요약 생성 실패 - OpenAI 응답 이상: {}", user.getSlackUsername(), type, e.getMessage());
                summaryRepository.save(new Summary(
                        date,
                        "AI 메시지 생성에 실패했습니다. 반복해서 이 메시지를 받으신다면 관리자에게 문의해주세요.",
                        null,
                        false,
                        e.getMessage(),
                        user,
                        type
                ));
            } catch (OpenAIClientException e) {
                log.error("❌ [{}] {} API 호출 실패 - {}", user.getSlackUsername(), type, e.getMessage());
                // 관리자 채널 알림 로직 추가 고려 가능
                // 재시도 큐에 등록 (예: Redis, DB 테이블, Kafka 등) 고려 가능 - 오버엔지니어링 논란있음
            }
        }
    }
}

