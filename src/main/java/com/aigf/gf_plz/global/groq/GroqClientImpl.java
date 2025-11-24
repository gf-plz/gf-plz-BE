package com.aigf.gf_plz.global.groq;

import com.aigf.gf_plz.global.groq.dto.GroqChatRequest;
import com.aigf.gf_plz.global.groq.dto.GroqChatResponse;
import com.aigf.gf_plz.global.groq.exception.GroqApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

/**
 * Groq API 클라이언트 구현체
 * WebClient를 사용하여 Groq Chat Completions API를 호출합니다.
 */
@Service
public class GroqClientImpl implements GroqClient {

    private static final String BASE_SYSTEM_PROMPT = """
    [여기에 내가 따로 붙여넣을 AI 여자친구 캐릭터 프롬프트 내용]
    """;

    private static final String MODEL = "llama-3.3-70b-versatile";
    private static final String CHAT_COMPLETIONS_ENDPOINT = "/chat/completions";

    private final WebClient webClient;

    @Value("${groq.api-key}")
    private String apiKey;

    public GroqClientImpl(WebClient.Builder builder) {
        this.webClient = builder
                .baseUrl("https://api.groq.com/openai/v1")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public String generateReply(String mode, String userText) {
        if (userText == null || userText.isBlank()) {
            throw new GroqApiException("사용자 입력 텍스트가 비어있습니다.");
        }

        // mode: "chat" → 채팅 스타일, "call" → 음성용 짧은 스타일
        String modePrompt = "mode: " + mode;

        List<GroqMessage> messages = List.of(
                new GroqMessage("system", BASE_SYSTEM_PROMPT),
                new GroqMessage("system", modePrompt),
                new GroqMessage("user", userText)
        );

        GroqChatRequest request = new GroqChatRequest(
                MODEL,
                messages,
                1024,
                0.7
        );

        try {
            GroqChatResponse response = webClient.post()
                    .uri(CHAT_COMPLETIONS_ENDPOINT)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(GroqChatResponse.class)
                    .block();

            if (response == null || response.choices() == null || response.choices().isEmpty()) {
                throw new GroqApiException("Groq API 응답이 비어있습니다.");
            }

            String reply = response.choices().get(0).message().content();
            
            if (reply == null || reply.isBlank()) {
                throw new GroqApiException("Groq API 응답의 답변 내용이 비어있습니다.");
            }

            return reply;
        } catch (WebClientResponseException e) {
            throw new GroqApiException(
                    String.format("Groq API 호출 실패: %s - %s", e.getStatusCode(), e.getResponseBodyAsString()),
                    e
            );
        } catch (Exception e) {
            if (e instanceof GroqApiException) {
                throw e;
            }
            throw new GroqApiException("Groq API 호출 중 예상치 못한 오류가 발생했습니다.", e);
        }
    }
}

