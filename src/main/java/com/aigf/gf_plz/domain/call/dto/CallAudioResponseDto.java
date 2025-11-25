package com.aigf.gf_plz.domain.call.dto;

/**
 * 통화 음성 응답 DTO
 * AI 여자친구의 음성 응답을 담습니다.
 */
public record CallAudioResponseDto(
        Long sessionId,
        byte[] audioData,  // MP3 형식의 음성 파일 바이트
        String transcript  // 사용자 발화 텍스트 (디버깅용)
) {}



