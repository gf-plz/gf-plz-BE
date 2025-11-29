package com.aigf.gf_plz.domain.character.controller;

import com.aigf.gf_plz.domain.character.dto.AffectionResetResponseDto;
import com.aigf.gf_plz.domain.character.dto.CharacterCreateRequestDto;
import com.aigf.gf_plz.domain.character.dto.CharacterResponseDto;
import com.aigf.gf_plz.domain.character.dto.CharacterSelectResponseDto;
import com.aigf.gf_plz.domain.character.dto.SessionIdResponseDto;
import com.aigf.gf_plz.domain.character.entity.Gender;
import com.aigf.gf_plz.domain.character.entity.Relation;
import com.aigf.gf_plz.domain.character.service.CharacterService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 캐릭터 컨트롤러
 * 캐릭터 생성, 조회, 선택 관련 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/characters")
public class CharacterController {

    private final CharacterService characterService;

    public CharacterController(CharacterService characterService) {
        this.characterService = characterService;
    }

    /**
     * 캐릭터 목록을 조회합니다.
     *
     * @param relation 관계 상태 필터 (선택사항)
     * @param gender 성별 필터 (선택사항)
     * @return 캐릭터 목록
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public List<CharacterResponseDto> getCharacters(
            @RequestParam(value = "relation", required = false) Relation relation,
            @RequestParam(value = "gender", required = false) Gender gender) {
        return characterService.getCharacters(relation, gender);
    }

    /**
     * 새로운 캐릭터를 생성합니다.
     *
     * @param request 캐릭터 생성 요청
     * @return 생성된 캐릭터 정보
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public CharacterResponseDto createCharacter(@Valid @RequestBody CharacterCreateRequestDto request) {
        return characterService.createCharacter(request);
    }

    /**
     * 캐릭터 ID로 캐릭터를 조회합니다.
     *
     * @param characterId 캐릭터 ID
     * @return 캐릭터 정보
     */
    @GetMapping(value = "/{characterId}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public CharacterResponseDto getCharacter(@PathVariable Long characterId) {
        return characterService.getCharacter(characterId);
    }

    /**
     * 가장 최근에 선택한 캐릭터를 조회합니다.
     *
     * @param gender 성별 필터 (선택사항)
     * @return 최근 캐릭터 정보, 없으면 null
     */
    @GetMapping(value = "/recent", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public CharacterResponseDto getRecentCharacter(
            @RequestParam(value = "gender", required = false) Gender gender) {
        return characterService.getRecentCharacter(gender);
    }

    /**
     * 캐릭터를 선택하고 세션을 생성하거나 활성화합니다.
     *
     * @param characterId 캐릭터 ID
     * @return 캐릭터 선택 응답 (캐릭터 정보 + 세션 ID)
     */
    @PostMapping(value = "/{characterId}/select", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public CharacterSelectResponseDto selectCharacter(@PathVariable Long characterId) {
        return characterService.selectCharacter(characterId);
    }

    /**
     * 여자친구 관계를 3일 연장합니다.
     *
     * @param characterId 캐릭터 ID
     * @return 업데이트된 캐릭터 정보
     */
    @PostMapping(value = "/{characterId}/extend", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public CharacterResponseDto extendRelationship(@PathVariable Long characterId) {
        return characterService.extendRelationship(characterId);
    }

    /**
     * 캐릭터의 가장 최근 세션 ID를 조회합니다.
     *
     * @param characterId 캐릭터 ID
     * @return 세션 ID 응답 (세션이 없으면 null)
     */
    @GetMapping(value = "/{characterId}/session", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public SessionIdResponseDto getRecentSessionId(@PathVariable Long characterId) {
        return characterService.getRecentSessionId(characterId);
    }

    /**
     * 테스트용: 헤어지는 날짜를 현재 시간으로 당깁니다.
     *
     * @param characterId 캐릭터 ID
     * @return 업데이트된 캐릭터 정보
     */
    @PostMapping(value = "/{characterId}/fast-forward-end-day", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public CharacterResponseDto fastForwardEndDay(@PathVariable Long characterId) {
        return characterService.fastForwardEndDay(characterId);
    }

    /**
     * 모든 캐릭터의 애정도를 50으로 설정합니다.
     *
     * @return 애정도 초기화 결과
     */
    @PostMapping(value = "/reset-affection", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public AffectionResetResponseDto resetAllAffectionTo50() {
        return characterService.resetAllAffectionTo50();
    }
}
