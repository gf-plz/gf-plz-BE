package com.aigf.gf_plz.domain.character.controller;

import com.aigf.gf_plz.domain.character.dto.CharacterCreateRequestDto;
import com.aigf.gf_plz.domain.character.dto.CharacterResponseDto;
import com.aigf.gf_plz.domain.character.dto.CharacterSelectResponseDto;
import com.aigf.gf_plz.domain.character.service.CharacterService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 캐릭터 컨트롤러
 */
@RestController
@RequestMapping("/api/characters")
public class CharacterController {

    private final CharacterService characterService;

    public CharacterController(CharacterService characterService) {
        this.characterService = characterService;
    }

    /**
     * 새로운 캐릭터를 생성합니다.
     * 
     * @param request 캐릭터 생성 요청
     * @return 생성된 캐릭터 정보
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public ResponseEntity<CharacterResponseDto> createCharacter(
            @Valid @RequestBody CharacterCreateRequestDto request
    ) {
        CharacterResponseDto response = characterService.createCharacter(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 캐릭터 ID로 캐릭터를 조회합니다.
     * 
     * @param characterId 캐릭터 ID
     * @return 캐릭터 정보
     */
    @GetMapping(value = "/{characterId}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public ResponseEntity<CharacterResponseDto> getCharacter(@PathVariable Long characterId) {
        CharacterResponseDto response = characterService.getCharacter(characterId);
        return ResponseEntity.ok(response);
    }

    /**
     * 최근 선택한 캐릭터를 조회합니다.
     * 가장 최근에 대화한 활성 세션의 캐릭터를 반환합니다.
     * 
     * @return 최근 선택한 캐릭터 (없으면 404)
     */
    @GetMapping(value = "/recent", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public ResponseEntity<CharacterResponseDto> getRecentCharacter() {
        CharacterResponseDto response = characterService.getRecentCharacter();
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    /**
     * 캐릭터를 선택하고 세션을 생성하거나 활성화합니다.
     * 
     * @param characterId 캐릭터 ID
     * @return 캐릭터 선택 결과 (캐릭터 정보 + 세션 ID)
     */
    @GetMapping(value = "/select", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public ResponseEntity<CharacterSelectResponseDto> selectCharacter(
            @RequestParam("characterId") Long characterId
    ) {
        CharacterSelectResponseDto response = characterService.selectCharacter(characterId);
        return ResponseEntity.ok(response);
    }
}
