package com.aigf.gf_plz.domain.call.dto;

/**
 * 통화 텍스트 응답 DTO
 * Groq가 생성한 답변 텍스트를 담습니다.
 * 이 텍스트는 TTS로 변환되어 음성으로 재생됩니다.
 */
public record CallTextResponseDto(
        String reply
) {}

