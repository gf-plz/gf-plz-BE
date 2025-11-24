package com.aigf.gf_plz.domain.character.dto;

import com.aigf.gf_plz.domain.character.entity.AttachmentType;
import com.aigf.gf_plz.domain.character.entity.Gender;
import com.aigf.gf_plz.domain.character.entity.Mbti;
import com.aigf.gf_plz.domain.character.entity.VoiceType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 캐릭터 생성 요청 DTO
 */
public record CharacterCreateRequestDto(
        // Status 정보
        @NotNull(message = "MBTI는 필수입니다.")
        Mbti mbti,
        
        @NotNull(message = "애착 유형은 필수입니다.")
        AttachmentType attachment,
        
        @NotNull(message = "테토력은 필수입니다.")
        @Min(value = 0, message = "테토력은 0 이상이어야 합니다.")
        @Max(value = 100, message = "테토력은 100 이하여야 합니다.")
        Integer teto,
        
        @NotNull(message = "성별은 필수입니다.")
        Gender gender,
        
        @NotBlank(message = "이름은 필수입니다.")
        String name,
        
        // Character 정보
        String description,
        
        String imageUrl,
        
        @NotNull(message = "목소리 타입은 필수입니다.")
        VoiceType voiceType
) {}
