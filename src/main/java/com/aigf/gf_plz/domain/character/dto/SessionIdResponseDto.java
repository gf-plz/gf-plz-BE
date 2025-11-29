package com.aigf.gf_plz.domain.character.dto;

/**
 * 세션 ID 응답 DTO
 * 캐릭터의 가장 최근 세션 ID를 반환합니다.
 */
public record SessionIdResponseDto(
        Long characterId,
        Long sessionId
) {}

