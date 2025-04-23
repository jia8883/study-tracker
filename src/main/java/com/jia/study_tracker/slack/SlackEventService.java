package com.jia.study_tracker.slack;


import com.jia.study_tracker.domain.StudyLog;
import com.jia.study_tracker.domain.User;
import com.jia.study_tracker.service.UserService;
import com.jia.study_tracker.repository.StudyLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class SlackEventService {

    private final UserService userService;
    private final StudyLogRepository studyLogRepository;


    public String handleEvent(SlackEventPayload payload) {
        // 1. URL 인증
        if ("url_verification".equals(payload.getType())) {
            return payload.getChallenge();
        }

        // 2. 이벤트 콜백 처리
        if ("event_callback".equals(payload.getType())) {
            SlackEventPayload.Event event = payload.getEvent();

            if ("message".equals(event.getType())) {
                String slackUserId = event.getUser();
                String text = event.getText();

                User user = userService.findOrCreateUser(slackUserId, "unknown");
                StudyLog studyLog = new StudyLog(text, LocalDateTime.now(), user);
                studyLogRepository.save(studyLog);

                log.info("💾 저장된 메시지: {}", text);
            }
        }

        return "ok";
    }
}

