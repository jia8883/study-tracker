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
