package com.jia.study_tracker.service;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

@Component
public class StudyMessageFilter {

    private static final List<String> STUDY_KEYWORDS = List.of("공부", "강의", "복습", "학습", "인강", "문제풀이");

    public boolean isStudyRelated(String text) {
        if (!StringUtils.hasText(text)) return false;
        return STUDY_KEYWORDS.stream().anyMatch(text::contains);
    }

}
