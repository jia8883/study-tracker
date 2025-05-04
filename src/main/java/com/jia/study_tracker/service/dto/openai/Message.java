package com.jia.study_tracker.service.dto.openai;

// OpenAI API에 전달할 메시지를 나타내는 DTO (역할: user, system / 내용: content)
public record Message(String role, String content) {}
