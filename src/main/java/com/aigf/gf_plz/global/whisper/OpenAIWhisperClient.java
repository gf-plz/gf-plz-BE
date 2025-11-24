package com.aigf.gf_plz.global.whisper;

import com.aigf.gf_plz.global.whisper.exception.WhisperException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * OpenAI Whisper API 클라이언트 구현체
 * Groq는 OpenAI 호환 API를 제공하므로 Whisper도 사용 가능합니다.
 */
@Service
public class OpenAIWhisperClient implements WhisperClient {

    private static final String WHISPER_ENDPOINT = "/audio/transcriptions";
    private static final String MODEL = "whisper-large-v3";

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${groq.api-key}")
    private String apiKey;

    public OpenAIWhisperClient(WebClient.Builder builder, ObjectMapper objectMapper) {
        this.webClient = builder
                .baseUrl("https://api.groq.com/openai/v1")
                .build();
        this.objectMapper = objectMapper;
    }

    @Override
    public String transcribe(MultipartFile audioFile) {
        if (audioFile == null || audioFile.isEmpty()) {
            throw new WhisperException("음성 파일이 비어있습니다.");
        }

        try {
            // Multipart form data 생성
            var bodyBuilder = org.springframework.web.reactive.function.BodyInserters
                    .fromMultipartData("file", audioFile.getResource())
                    .with("model", MODEL)
                    .with("language", "ko"); // 한국어 지정

            String response = webClient.post()
                    .uri(WHISPER_ENDPOINT)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(bodyBuilder)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (response == null || response.isBlank()) {
                throw new WhisperException("Whisper API 응답이 비어있습니다.");
            }

            // JSON 응답에서 text 필드 추출 (간단한 파싱)
            // 실제로는 JSON 파서를 사용하는 것이 좋습니다
            String transcript = extractTextFromResponse(response);

            if (transcript == null || transcript.isBlank()) {
                throw new WhisperException("Whisper API 응답에서 텍스트를 추출할 수 없습니다.");
            }

            return transcript;
        } catch (WebClientResponseException e) {
            throw new WhisperException(
                    String.format("Whisper API 호출 실패: %s - %s", e.getStatusCode(), e.getResponseBodyAsString()),
                    e
            );
        } catch (Exception e) {
            if (e instanceof WhisperException) {
                throw e;
            }
            throw new WhisperException("Whisper API 호출 중 예상치 못한 오류가 발생했습니다.", e);
        }
    }

    /**
     * JSON 응답에서 text 필드를 추출합니다.
     */
    private String extractTextFromResponse(String response) {
        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            JsonNode textNode = jsonNode.get("text");
            if (textNode != null && textNode.isTextual()) {
                return textNode.asText();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}

