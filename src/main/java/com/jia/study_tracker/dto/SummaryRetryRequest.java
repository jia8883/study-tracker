package com.jia.study_tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SummaryRetryRequest implements Serializable {
    private String slackUserId;
    private String slackUsername;
    private String summaryType;
    private String targetDate;
}
