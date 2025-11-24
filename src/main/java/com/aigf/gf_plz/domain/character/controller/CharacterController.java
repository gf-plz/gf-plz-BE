package com.aigf.gf_plz.domain.character.controller;

import com.aigf.gf_plz.domain.character.dto.CharacterCreateRequestDto;
import com.aigf.gf_plz.domain.character.dto.CharacterResponseDto;
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
}
