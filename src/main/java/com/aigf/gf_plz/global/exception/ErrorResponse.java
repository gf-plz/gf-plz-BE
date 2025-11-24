package com.aigf.gf_plz.global.exception;

import java.time.LocalDateTime;

/**
 * 공통 에러 응답 DTO
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {
    public ErrorResponse(int status, String error, String message, String path) {
        this(LocalDateTime.now(), status, error, message, path);
    }
}

