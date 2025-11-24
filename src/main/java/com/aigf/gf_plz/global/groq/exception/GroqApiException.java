package com.aigf.gf_plz.global.groq.exception;

/**
 * Groq API 호출 중 발생하는 예외
 */
public class GroqApiException extends RuntimeException {

    public GroqApiException(String message) {
        super(message);
    }

    public GroqApiException(String message, Throwable cause) {
        super(message, cause);
    }
}

