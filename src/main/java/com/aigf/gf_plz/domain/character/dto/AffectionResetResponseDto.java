package com.aigf.gf_plz.domain.character.dto;

/**
 * 애정도 초기화 응답 DTO
 * 모든 캐릭터의 애정도를 50으로 설정한 결과를 반환합니다.
 */
public record AffectionResetResponseDto(
        Integer updatedCount,
        String message
) {
    public AffectionResetResponseDto(Integer updatedCount) {
        this(updatedCount, String.format("%d명의 캐릭터 애정도가 50으로 설정되었습니다.", updatedCount));
    }
}

