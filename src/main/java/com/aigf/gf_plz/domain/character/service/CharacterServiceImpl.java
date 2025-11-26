package com.aigf.gf_plz.domain.character.service;

import com.aigf.gf_plz.domain.character.dto.CharacterCreateRequestDto;
import com.aigf.gf_plz.domain.character.dto.CharacterResponseDto;
import com.aigf.gf_plz.domain.character.dto.CharacterSelectResponseDto;
import com.aigf.gf_plz.domain.character.entity.Character;
import com.aigf.gf_plz.domain.character.entity.Relation;
import com.aigf.gf_plz.domain.character.entity.Status;
import com.aigf.gf_plz.domain.character.exception.CharacterNotFoundException;
import com.aigf.gf_plz.domain.character.repository.CharacterRepository;
import com.aigf.gf_plz.domain.character.repository.StatusRepository;
import com.aigf.gf_plz.domain.session.entity.Session;
import com.aigf.gf_plz.domain.session.entity.SessionType;
import com.aigf.gf_plz.domain.session.repository.SessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 캐릭터 서비스 구현체
 */
@Service
public class CharacterServiceImpl implements CharacterService {

    private final CharacterRepository characterRepository;
    private final StatusRepository statusRepository;
    private final SessionRepository sessionRepository;

    public CharacterServiceImpl(
            CharacterRepository characterRepository,
            StatusRepository statusRepository,
            SessionRepository sessionRepository
    ) {
        this.characterRepository = characterRepository;
        this.statusRepository = statusRepository;
        this.sessionRepository = sessionRepository;
    }

    @Override
    @Transactional
    public CharacterResponseDto createCharacter(CharacterCreateRequestDto request) {
        // 1. Status 생성 (ERD에 따른 relation, start_day, end_day, like 필드)
        Status status = Status.builder()
                .relation(Relation.yet) // 초기 상태는 아직 만나지 않음
                .like(0) // 초기 애정도는 0
                .build();
        Status savedStatus = statusRepository.save(status);

        // 2. Character 생성 (ERD에 따른 MBTI, 성별, 이름, 애착타입, 테토력 포함)
        Character character = Character.builder()
                .status(savedStatus)
                .description(request.description())
                .imageUrl(request.imageUrl())
                .voiceType(request.voiceType())
                .mbti(request.mbti())
                .gender(request.gender())
                .name(request.name())
                .attachment(request.attachment())
                .teto(request.teto())
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

    @Override
    @Transactional(readOnly = true)
    public CharacterResponseDto getRecentCharacter() {
        // 최근 활성 세션 조회
        List<Session> recentSessions = sessionRepository.findRecentActiveSessions();
        
        // 가장 최근 세션이 없으면 null 반환
        if (recentSessions.isEmpty()) {
            return null;
        }
        
        // 가장 최근 세션의 캐릭터 반환
        Session mostRecentSession = recentSessions.get(0);
        return toResponseDto(mostRecentSession.getCharacter());
    }

    @Override
    @Transactional
    public CharacterSelectResponseDto selectCharacter(Long characterId) {
        // 1. 캐릭터 조회
        Character character = characterRepository.findById(characterId)
                .orElseThrow(() -> new CharacterNotFoundException(characterId));
        
        // 2. 기존 활성 세션이 있는지 확인 (CHAT 타입 우선)
        Optional<Session> existingChatSession = sessionRepository
                .findByCharacterIdAndSessionTypeAndIsActiveTrue(characterId, SessionType.CHAT);
        
        Session session;
        if (existingChatSession.isPresent()) {
            // 기존 세션이 있으면 활성화 상태 유지
            session = existingChatSession.get();
        } else {
            // 기존 세션이 없으면 새 세션 생성 (CHAT 타입)
            session = Session.builder()
                    .character(character)
                    .sessionType(SessionType.CHAT)
                    .build();
            session = sessionRepository.save(session);
        }
        
        // 3. 응답 DTO 생성
        CharacterResponseDto characterDto = toResponseDto(character);
        return new CharacterSelectResponseDto(
                characterId,
                session.getSessionId(),
                characterDto
        );
    }

    /**
     * Character 엔티티를 CharacterResponseDto로 변환합니다.
     */
    private CharacterResponseDto toResponseDto(Character character) {
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
                character.getCreatedAt(),
                character.getUpdatedAt()
        );
    }
}




