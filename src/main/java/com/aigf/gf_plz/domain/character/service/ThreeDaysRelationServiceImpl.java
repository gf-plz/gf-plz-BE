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
    private final AiReviewService aiReviewService;

    public ThreeDaysRelationServiceImpl(CharacterRepository characterRepository, AiReviewService aiReviewService) {
        this.characterRepository = characterRepository;
        this.aiReviewService = aiReviewService;
    }

    @Override
    @Transactional
    public ThreeDaysRelationResponseDto updateRelation(ThreeDaysRelationRequestDto request) {
        Character character = characterRepository.findById(request.characterId())
                .orElseThrow(() -> new CharacterNotFoundException(request.characterId()));

        character.updateRelation(Relation.ex);
        character.updateEndDay(LocalDateTime.now());
        
        // 한줄평 생성
        String aiSummary = aiReviewService.generateReview(character);
        if (aiSummary != null) {
            character.updateAiSummary(aiSummary);
        }
        
        // 애정도 평가 및 업데이트
        Integer affectionScore = aiReviewService.evaluateAffection(character);
        if (affectionScore != null) {
            character.updateLike(affectionScore);
        }
        
        characterRepository.save(character);

        return new ThreeDaysRelationResponseDto(
                "UPDATED",
                character.getCharacterId(),
                Relation.ex
        );
    }
}

