package com.aigf.gf_plz.domain.character.dto;

/**
 * 캐릭터 선택 응답 DTO
 * 캐릭터 선택 시 세션 정보를 포함하여 반환합니다.
 */
public record CharacterSelectResponseDto(
        Long characterId,
        Long sessionId,
        CharacterResponseDto character
) {}




