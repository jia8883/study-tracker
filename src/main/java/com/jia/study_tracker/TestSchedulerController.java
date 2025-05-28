package com.jia.study_tracker;

import com.jia.study_tracker.domain.SummaryType;
import com.jia.study_tracker.service.SummaryGenerationService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * JMeter로 스케줄러를 트리거할 수 없기 때문에 엔드포인트를 노출시켜 수동 호출하는 컨트롤러
 */
@Slf4j
@Profile({"dev", "docker", "mock-openai"})
@RestController
@RequiredArgsConstructor
public class TestSchedulerController {

    private final SummaryGenerationService summaryGenerationService;

    @PostMapping("/test/run-daily-scheduler")
    public ResponseEntity<String> runScheduler() {
        System.out.println("🛠 runScheduler() 시작됨");
        try {
            System.out.println("🧪 generateSummaries 호출 시도");
            summaryGenerationService.generateSummaries(LocalDate.now(), SummaryType.DAILY);
            return ResponseEntity.ok("스케줄러 실행 완료");
        } catch (Exception e) {
            System.out.println("❌ 예외 발생: " + e.getMessage());
            e.printStackTrace();  // 👈 이거 추가
            log.error("에러", e);
            return ResponseEntity.status(500).body("에러 발생: " + (e.getMessage() == null ? "null 메시지" : e.getMessage()));
        }
    }


    static {
        System.out.println("🔥 TestSchedulerController 클래스가 로드되었습니다");
    }

    @PostConstruct
    public void init() {
        System.out.println("✅ TestSchedulerController is ACTIVE");
    }
}
