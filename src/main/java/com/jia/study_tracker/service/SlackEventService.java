package com.jia.study_tracker.service;



import com.jia.study_tracker.domain.StudyLog;
import com.jia.study_tracker.domain.User;
import com.jia.study_tracker.repository.StudyLogRepository;
import com.jia.study_tracker.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;


@Service
public class SlackEventService {

    private final UserRepository userRepository;
    private final StudyLogRepository studyLogRepository;

    public SlackEventService(UserRepository userRepository, StudyLogRepository studyLogRepository) {
        this.userRepository = userRepository;
        this.studyLogRepository = studyLogRepository;
    }

    public ResponseEntity<String> handleEvent(Map<String, Object> payload) {
        // 1. URL 인증용 challenge 응답
        if ("url_verification".equals(payload.get("type"))) {
            String challenge = (String) payload.get("challenge");
            return ResponseEntity.ok(challenge);
        }

        // 2. message 이벤트 처리
        if ("event_callback".equals(payload.get("type"))) {
            Map<String, Object> event = (Map<String, Object>) payload.get("event");
            String eventType = (String) event.get("type");

            if ("message".equals(eventType)) {
                String slackUserId = (String) event.get("user");
                String text = (String) event.get("text");

                // 없는 유저는 새롭게 만들기
                User user = userRepository.findById(slackUserId)
                        .orElseGet(() -> userRepository.save(new User(slackUserId, "unknown")));

                StudyLog log = new StudyLog(text, LocalDateTime.now(), user);
                studyLogRepository.save(log);

                System.out.println("💾 저장된 메시지: " + text);
                // 메시지 저장 로직 추가 가능
            }
        }
        return ResponseEntity.ok("ok");
    }
}

