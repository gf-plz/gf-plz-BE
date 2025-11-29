package com.aigf.gf_plz.domain.character.dto;

import com.aigf.gf_plz.domain.character.entity.AttachmentType;
import com.aigf.gf_plz.domain.character.entity.Gender;
import com.aigf.gf_plz.domain.character.entity.Mbti;
import com.aigf.gf_plz.domain.character.entity.VoiceType;

/**
 * 캐릭터 응답 DTO
 */
public record CharacterResponseDto(
        Long characterId,
        Mbti mbti,
        AttachmentType attachment,
        Integer teto,
        Gender gender,
        String name,
        String description,
        String imageUrl,
        VoiceType voiceType,
        StatusResponseDto status,
        String aiSummary
) {}
