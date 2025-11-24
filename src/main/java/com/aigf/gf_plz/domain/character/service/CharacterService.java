package com.aigf.gf_plz.domain.character.service;

import com.aigf.gf_plz.domain.character.dto.CharacterCreateRequestDto;
import com.aigf.gf_plz.domain.character.dto.CharacterResponseDto;

/**
 * 캐릭터 서비스 인터페이스
 */
public interface CharacterService {
    /**
     * 새로운 캐릭터를 생성합니다.
     * 
     * @param request 캐릭터 생성 요청
     * @return 생성된 캐릭터 정보
     */
    CharacterResponseDto createCharacter(CharacterCreateRequestDto request);
    
    /**
     * 캐릭터 ID로 캐릭터를 조회합니다.
     * 
     * @param characterId 캐릭터 ID
     * @return 캐릭터 정보
     */
    CharacterResponseDto getCharacter(Long characterId);
}
