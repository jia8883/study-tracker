package com.jia.study_tracker.slack;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SlackEventPayload {
    private String type;
    private String challenge;
    private Event event;

    @Getter
    @NoArgsConstructor
    public static class Event {
        private String type;
        private String user;
        private String text;
    }
}
