package com.aigf.gf_plz.domain.character.service;

import com.aigf.gf_plz.domain.character.dto.ThreeDaysRelationRequestDto;
import com.aigf.gf_plz.domain.character.dto.ThreeDaysRelationResponseDto;

/**
 * 3일 결과에 따라 캐릭터 관계 상태를 업데이트하는 서비스 인터페이스
 */
public interface ThreeDaysRelationService {
    ThreeDaysRelationResponseDto updateRelation(ThreeDaysRelationRequestDto request);
}

