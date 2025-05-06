package com.jia.study_tracker;

import com.jia.study_tracker.domain.StudyLog;
import com.jia.study_tracker.domain.SummaryType;
import com.jia.study_tracker.service.StudyLogQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class LogTestRunner implements CommandLineRunner {

    private final StudyLogQueryService studyLogQueryService;

    @Override
    public void run(String... args) throws Exception {
        List<StudyLog> dailyLogs = studyLogQueryService.getLogs("U123456", LocalDate.of(2025, 5, 2), SummaryType.DAILY);
        dailyLogs.forEach(log -> System.out.println("✅ 로그: " + log.getContent()));

        List<StudyLog> weeklyLogs = studyLogQueryService.getLogs("U123456", LocalDate.of(2025, 4, 29), SummaryType.WEEKLY);
        System.out.println("📅 주간 로그 개수: " + weeklyLogs.size());

        List<StudyLog> monthlyLogs = studyLogQueryService.getLogs("U123456", LocalDate.of(2025, 4, 1), SummaryType.MONTHLY);
        System.out.println("📆 월간 로그 개수: " + monthlyLogs.size());
    }
}
