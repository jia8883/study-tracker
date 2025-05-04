package com.jia.study_tracker.service;

import com.jia.study_tracker.domain.StudyLog;
import com.jia.study_tracker.service.dto.SummaryResult;
import com.jia.study_tracker.service.dto.openai.Message;
import com.jia.study_tracker.service.dto.openai.OpenAIRequest;
import com.jia.study_tracker.service.dto.openai.OpenAIResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 사용자의 학습 로그를 OpenAI API에 전달하여 요약 및 피드백을 생성하는 컴포넌트
 *
 * 역할:
 * - StudyLog 리스트를 문자열로 변환 후 프롬프트 형태로 구성
 * - OpenAI의 Chat Completion API에 요청을 보내고 응답을 파싱
 * - 요약 및 피드백을 추출하여 SummaryResult 객체로 반환
 *
 * 특징:
 * - Spring WebClient를 사용한 비동기 HTTP 요청
 * - 실패 시 기본 메시지 반환 및 예외 로깅 처리
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OpenAIClient {

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.model:gpt-3.5-turbo}")
    private String model;

    // WebClient는 외부 HTTP 요청을 위한 스프링 비동기 클라이언트
    private final WebClient.Builder webClientBuilder;

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    /**
     * 메소드 : 학습 로그 리스트를 받아 OpenAI에 요청하고 요약 및 피드백을 생성
     *
     * @param logs 사용자의 하루 학습 로그 목록
     * @return 요약 및 피드백이 담긴 SummaryResult
     */
    public SummaryResult generateSummaryAndFeedback(List<StudyLog> logs) {
        // StudyLog의 content만 추출하여 한 개의 문자열로 결합
        String joinedContent = logs.stream()
                .map(StudyLog::getContent)
                .collect(Collectors.joining("\n"));

        // OpenAI에게 전달할 프롬프트 구성
        String prompt = String.format("""
                다음은 사용자의 학습 로그입니다:
                ---
                %s
                ---

                위 내용을 3~4문장 이내로 요약해줘. 그리고 학습을 응원하는 동기부여 성격의 짧은 피드백을 함께 작성해줘.
                출력 형식은 다음과 같이 해줘:

                요약: ~~~
                피드백: ~~~
                """, joinedContent);

        // DTO 기반 요청 생성
        OpenAIRequest request = new OpenAIRequest(
                model,
                List.of(
                        new Message("system", "너는 친절한 학습 요약 봇이야."),
                        new Message("user", prompt)
                )
        );

        try {
            // DTO 기반 응답 처리
            OpenAIResponse response = webClientBuilder.build()
                    .post()
                    .uri(API_URL)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(OpenAIResponse.class)
                    .block(); // 블로킹 방식으로 대기

            if (response == null || response.choices().isEmpty()) {
                throw new RuntimeException("OpenAI 응답이 비어있습니다.");
            }

            // 응답에서 message → content 추출
            String content = response.choices().get(0).message().content();
            // content 문자열에서 요약과 피드백 분리
            String[] parts = content.split("피드백:");

            String summary = parts[0].replace("요약:", "").trim();
            String feedback = parts.length > 1 ? parts[1].trim() : "피드백 없음";

            return new SummaryResult(summary, feedback);

        } catch (Exception e) {
            log.error("OpenAI 호출 실패", e);
            return new SummaryResult("요약 실패", "피드백 생성 실패");
        }
    }
}
