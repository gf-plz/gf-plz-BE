package com.aigf.gf_plz.domain.character.service;

import com.aigf.gf_plz.domain.character.dto.ThreeDaysRelationRequestDto;
import com.aigf.gf_plz.domain.character.dto.ThreeDaysRelationResponseDto;
import com.aigf.gf_plz.domain.character.entity.Character;
import com.aigf.gf_plz.domain.character.entity.Relation;
import com.aigf.gf_plz.domain.character.exception.CharacterNotFoundException;
import com.aigf.gf_plz.domain.character.repository.CharacterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 3일 결과 API에서 캐릭터의 관계 상태를 업데이트하는 구현체
 */
@Service
public class ThreeDaysRelationServiceImpl implements ThreeDaysRelationService {

    private final CharacterRepository characterRepository;

    public ThreeDaysRelationServiceImpl(CharacterRepository characterRepository) {
        this.characterRepository = characterRepository;
    }

    @Override
    @Transactional
    public ThreeDaysRelationResponseDto updateRelation(ThreeDaysRelationRequestDto request) {
        Character character = characterRepository.findById(request.characterId())
                .orElseThrow(() -> new CharacterNotFoundException(request.characterId()));

        character.updateRelation(Relation.ex);
        character.updateEndDay(LocalDateTime.now());
        characterRepository.save(character);

        return new ThreeDaysRelationResponseDto(
                "UPDATED",
                character.getCharacterId(),
                Relation.ex
        );
    }
}

