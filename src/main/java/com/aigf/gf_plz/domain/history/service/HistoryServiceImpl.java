package com.aigf.gf_plz.domain.history.service;

import com.aigf.gf_plz.domain.character.dto.CharacterResponseDto;
import com.aigf.gf_plz.domain.character.dto.StatusResponseDto;
import com.aigf.gf_plz.domain.character.entity.Character;
import com.aigf.gf_plz.domain.character.entity.Gender;
import com.aigf.gf_plz.domain.character.entity.Relation;
import com.aigf.gf_plz.domain.character.repository.CharacterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 히스토리 API용 서비스 구현
 */
@Service
public class HistoryServiceImpl implements HistoryService {

    private final CharacterRepository characterRepository;

    public HistoryServiceImpl(CharacterRepository characterRepository) {
        this.characterRepository = characterRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CharacterResponseDto> getHistory(Gender gender) {
        var characters = gender == null
                ? characterRepository.findByRelation(Relation.ex)
                : characterRepository.findByRelationAndGender(Relation.ex, gender);
        return characters.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private CharacterResponseDto mapToDto(Character character) {
        var statusDto = new StatusResponseDto(
                character.getCharacterId(),
                character.getRelation(),
                character.getStartDay(),
                character.getEndDay(),
                character.getLike()
        );

        return new CharacterResponseDto(
                character.getCharacterId(),
                character.getMbti(),
                character.getAttachment(),
                character.getTeto(),
                character.getGender(),
                character.getName(),
                character.getDescription(),
                character.getImageUrl(),
                character.getVoiceType(),
                statusDto
                ,
                character.getAiSummary()
        );
    }
}

