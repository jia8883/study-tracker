package com.jia.study_tracker.service;

import com.jia.study_tracker.domain.StudyLog;
import com.jia.study_tracker.domain.Summary;
import com.jia.study_tracker.domain.SummaryType;
import com.jia.study_tracker.domain.User;
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
 * - 각 사용자의 당일 학습 로그 가져오기
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

            SummaryResult result = openAIClient.generateSummaryAndFeedback(logs);

            Summary summary = new Summary(
                    date,
                    result.getSummary(),
                    result.getFeedback(),
                    true,
                    null,
                    user,
                    type
            );

            summaryRepository.save(summary);
            log.info("✅ [{}] {} 요약/피드백 저장 완료", user.getSlackUsername(), type);
        }
    }
}

