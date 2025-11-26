package com.aigf.gf_plz.domain.character.service;

import com.aigf.gf_plz.domain.character.dto.CharacterCreateRequestDto;
import com.aigf.gf_plz.domain.character.dto.CharacterResponseDto;
import com.aigf.gf_plz.domain.character.dto.CharacterSelectResponseDto;

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

    /**
     * 최근 선택한 캐릭터를 조회합니다.
     * 가장 최근에 대화한 활성 세션의 캐릭터를 반환합니다.
     * 
     * @return 최근 선택한 캐릭터 (없으면 null)
     */
    CharacterResponseDto getRecentCharacter();

    /**
     * 캐릭터를 선택하고 세션을 생성하거나 활성화합니다.
     * 
     * @param characterId 캐릭터 ID
     * @return 캐릭터 선택 결과 (캐릭터 정보 + 세션 ID)
     */
    CharacterSelectResponseDto selectCharacter(Long characterId);
}
