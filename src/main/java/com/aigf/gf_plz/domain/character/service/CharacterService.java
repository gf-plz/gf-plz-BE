package com.aigf.gf_plz.domain.character.service;

import com.aigf.gf_plz.domain.character.dto.CharacterCreateRequestDto;
import com.aigf.gf_plz.domain.character.dto.CharacterResponseDto;
import com.aigf.gf_plz.domain.character.dto.CharacterSelectResponseDto;
import com.aigf.gf_plz.domain.character.entity.Gender;
import com.aigf.gf_plz.domain.character.entity.Relation;

import java.util.List;

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
     * 가장 최근에 선택한 캐릭터를 조회합니다.
     * 
     * @param gender 성별 필터 (선택사항, null이면 전체 조회)
     * @return 최근 캐릭터 정보, 없으면 null
     */
    CharacterResponseDto getRecentCharacter(Gender gender);
    
    /**
     * 캐릭터를 선택하고 세션을 생성하거나 활성화합니다.
     * 
     * @param characterId 캐릭터 ID
     * @return 캐릭터 선택 응답 (캐릭터 정보 + 세션 ID)
     */
    CharacterSelectResponseDto selectCharacter(Long characterId);
    
    /**
     * 여자친구 관계를 3일 연장합니다.
     * 
     * @param characterId 캐릭터 ID
     * @return 업데이트된 캐릭터 정보
     */
    CharacterResponseDto extendRelationship(Long characterId);
    
    /**
     * 캐릭터 목록을 조회합니다.
     * 
     * @param relation 관계 상태 필터 (선택사항, null이면 전체 조회)
     * @param gender 성별 필터 (선택사항, null이면 전체 조회)
     * @return 캐릭터 목록
     */
    List<CharacterResponseDto> getCharacters(Relation relation, Gender gender);
}
