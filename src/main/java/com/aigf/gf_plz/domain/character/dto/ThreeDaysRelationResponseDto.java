package com.aigf.gf_plz.domain.character.dto;

import com.aigf.gf_plz.domain.character.entity.Relation;

/**
 * 3일 결과 API 응답으로 넘어가는 결과 정보
 */
public record ThreeDaysRelationResponseDto(
        String status,
        Long characterId,
        Relation newRelation
) {}

