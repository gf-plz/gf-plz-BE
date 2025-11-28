package com.aigf.gf_plz.domain.history.service;

import com.aigf.gf_plz.domain.character.dto.CharacterResponseDto;
import com.aigf.gf_plz.domain.character.dto.StatusResponseDto;
import com.aigf.gf_plz.domain.history.entity.RelationshipHistory;
import com.aigf.gf_plz.domain.history.repository.RelationshipHistoryRepository;
import com.aigf.gf_plz.domain.character.entity.Relation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * 히스토리 API용 서비스 구현
 */
@Service
public class HistoryServiceImpl implements HistoryService {

    private final RelationshipHistoryRepository historyRepository;

    public HistoryServiceImpl(RelationshipHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CharacterResponseDto> getHistory(Long historyId) {
        return historyRepository.findByHistoryId(historyId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private CharacterResponseDto mapToDto(RelationshipHistory entry) {
        var character = entry.getCharacter();
        var statusDto = new StatusResponseDto(
                entry.getHistoryId(),
                Relation.ex,
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
        );
    }
}

