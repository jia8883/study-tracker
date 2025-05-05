package com.jia.study_tracker.service;

import com.jia.study_tracker.domain.StudyLog;
import com.jia.study_tracker.domain.User;
import com.jia.study_tracker.repository.StudyLogRepository;
import com.jia.study_tracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

/**
 * StudyLogQueryService는 사용자 ID(slackUserId)를 기반으로
 * 일/주/월 단위의 학습 기록을 조회하는 기능을 제공합니다.
 *
 * 이 서비스는 읽기 전용 쿼리 기능만 담당하며,
 * 사용자나 학습 기록을 수정하지 않습니다.
 */

@Service
@RequiredArgsConstructor
public class StudyLogQueryService {

    private final StudyLogRepository studyLogRepository;
    private final UserRepository userRepository;

    public List<StudyLog> getDailyLogs(String slackUserId, LocalDate date) {
        User user = userRepository.findById(slackUserId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저"));

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        return studyLogRepository.findByUserAndTimestampBetween(user, start, end);
    }

    public List<StudyLog> getWeeklyLogs(String slackUserId, LocalDate weekStartDate) {
        User user = userRepository.findById(slackUserId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저"));

        LocalDateTime start = weekStartDate.atStartOfDay();
        LocalDateTime end = weekStartDate.plusDays(7).atStartOfDay();

        return studyLogRepository.findByUserAndTimestampBetween(user, start, end);
    }

    public List<StudyLog> getMonthlyLogs(String slackUserId, YearMonth month) {
        User user = userRepository.findById(slackUserId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저"));

        LocalDateTime start = month.atDay(1).atStartOfDay();
        LocalDateTime end = month.plusMonths(1).atDay(1).atStartOfDay();

        return studyLogRepository.findByUserAndTimestampBetween(user, start, end);
    }
}
