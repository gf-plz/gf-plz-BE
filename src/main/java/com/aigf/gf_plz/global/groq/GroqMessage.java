package com.aigf.gf_plz.global.groq;

/**
 * Groq API 메시지 모델
 * role: "system", "user", "assistant" 중 하나
 * content: 메시지 내용
 */
public record GroqMessage(String role, String content) {}

