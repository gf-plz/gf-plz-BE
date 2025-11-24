package com.aigf.gf_plz.domain.call.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 통화 텍스트 요청 DTO
 * Whisper로 변환된 사용자의 발화 텍스트를 담습니다.
 */
public record CallTextRequestDto(
        @NotBlank(message = "발화 텍스트는 필수입니다.")
        String transcript
) {}

