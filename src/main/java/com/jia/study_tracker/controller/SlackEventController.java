package com.jia.study_tracker.controller;


import com.jia.study_tracker.service.SlackEventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RestController
@RequestMapping("/slack")
public class SlackEventController {

    private final SlackEventService slackEventService;

    public SlackEventController(SlackEventService slackEventService) {
        this.slackEventService = slackEventService;
    }

    @PostMapping("/events")
    public ResponseEntity<String> receiveEvent(@RequestBody Map<String, Object> payload) {
        return slackEventService.handleEvent(payload);
    }

}

