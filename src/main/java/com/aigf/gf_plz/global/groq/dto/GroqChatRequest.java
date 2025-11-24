package com.aigf.gf_plz.global.groq.dto;

import com.aigf.gf_plz.global.groq.GroqMessage;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Groq Chat Completions API 요청 DTO
 */
public record GroqChatRequest(
        String model,
        List<GroqMessage> messages,
        @JsonProperty("max_tokens") Integer maxTokens,
        Double temperature
) {
    public GroqChatRequest {
        if (maxTokens == null) {
            maxTokens = 1024;
        }
        if (temperature == null) {
            temperature = 0.7;
        }
    }
}

