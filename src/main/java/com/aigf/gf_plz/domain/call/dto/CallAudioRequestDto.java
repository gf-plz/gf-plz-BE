package com.aigf.gf_plz.domain.call.dto;

import jakarta.validation.constraints.NotNull;

import java.util.Optional;

/**
 * 통화 음성 요청 DTO
 * 음성 파일과 캐릭터 정보를 담습니다.
 */
public record CallAudioRequestDto(
        @NotNull(message = "캐릭터 ID는 필수입니다.")
        Long characterId,
        Optional<Long> sessionId
) {}






