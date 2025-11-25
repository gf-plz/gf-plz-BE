package com.aigf.gf_plz.domain.character.service;

import com.aigf.gf_plz.domain.character.dto.CharacterCreateRequestDto;
import com.aigf.gf_plz.domain.character.dto.CharacterResponseDto;
import com.aigf.gf_plz.domain.character.entity.Character;
import com.aigf.gf_plz.domain.character.entity.Status;
import com.aigf.gf_plz.domain.character.exception.CharacterNotFoundException;
import com.aigf.gf_plz.domain.character.repository.CharacterRepository;
import com.aigf.gf_plz.domain.character.repository.StatusRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 캐릭터 서비스 구현체
 */
@Service
public class CharacterServiceImpl implements CharacterService {

    private final CharacterRepository characterRepository;
    private final StatusRepository statusRepository;

    public CharacterServiceImpl(
            CharacterRepository characterRepository,
            StatusRepository statusRepository
    ) {
        this.characterRepository = characterRepository;
        this.statusRepository = statusRepository;
    }

    @Override
    @Transactional
    public CharacterResponseDto createCharacter(CharacterCreateRequestDto request) {
        // 1. Status 생성
        Status status = Status.builder()
                .mbti(request.mbti())
                .attachment(request.attachment())
                .teto(request.teto())
                .gender(request.gender())
                .name(request.name())
                .build();
        Status savedStatus = statusRepository.save(status);

        // 2. Character 생성
        Character character = Character.builder()
                .status(savedStatus)
                .description(request.description())
                .imageUrl(request.imageUrl())
                .voiceType(request.voiceType())
                .build();
        Character savedCharacter = characterRepository.save(character);

        // 3. 응답 DTO 생성
        return toResponseDto(savedCharacter);
    }

    @Override
    @Transactional(readOnly = true)
    public CharacterResponseDto getCharacter(Long characterId) {
        Character character = characterRepository.findById(characterId)
                .orElseThrow(() -> new CharacterNotFoundException(characterId));
        
        return toResponseDto(character);
    }

    /**
     * Character 엔티티를 CharacterResponseDto로 변환합니다.
     */
    private CharacterResponseDto toResponseDto(Character character) {
        Status status = character.getStatus();
        return new CharacterResponseDto(
                character.getCharacterId(),
                status.getMbti(),
                status.getAttachment(),
                status.getTeto(),
                status.getGender(),
                status.getName(),
                character.getDescription(),
                character.getImageUrl(),
                character.getVoiceType(),
                character.getCreatedAt(),
                character.getUpdatedAt()
        );
    }
}



