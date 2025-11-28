package com.aigf.gf_plz.domain.character.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 3일 결과 API에서 전달하는 요청 정보 (캐릭터 ID)
 */
public record ThreeDaysRelationRequestDto(
        @NotNull(message = "characterId는 필수입니다.")
        @Min(value = 1, message = "characterId는 1 이상의 값이어야 합니다.")
        Long characterId
) {}

