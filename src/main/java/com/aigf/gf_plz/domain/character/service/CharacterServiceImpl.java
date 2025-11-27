package com.aigf.gf_plz.domain.character.service;

import com.aigf.gf_plz.domain.character.dto.CharacterCreateRequestDto;
import com.aigf.gf_plz.domain.character.dto.CharacterResponseDto;
import com.aigf.gf_plz.domain.character.dto.CharacterSelectResponseDto;
import com.aigf.gf_plz.domain.character.dto.StatusResponseDto;
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

import java.time.LocalDateTime;
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
        // 요청 데이터 검증
        if (request.attachment() == null) {
            throw new IllegalArgumentException("애착타입은 필수입니다.");
        }
        
        // 1. Status 생성 (ERD에 따른 relation, start_day, end_day, like 필드)
        Status status = Status.builder()
                .relation(Relation.yet) // 초기 상태는 아직 만나지 않음
                .startDay(null) // 캐릭터 선택 시 설정됨
                .endDay(null) // 캐릭터 선택 시 설정됨
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

        // 3. 응답 DTO 생성 (저장된 Status를 직접 사용)
        return toResponseDto(savedCharacter, savedStatus);
    }

    @Override
    @Transactional(readOnly = true)
    public CharacterResponseDto getCharacter(Long characterId) {
        Character character = characterRepository.findById(characterId)
                .orElseThrow(() -> new CharacterNotFoundException(characterId));
        
        // Status 만료 체크
        checkAndUpdateExpiredStatus(character.getStatus());
        
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
        Character character = mostRecentSession.getCharacter();
        
        // Status 만료 체크
        checkAndUpdateExpiredStatus(character.getStatus());
        
        return toResponseDto(character);
    }

    @Override
    @Transactional
    public CharacterSelectResponseDto selectCharacter(Long characterId) {
        // 1. 캐릭터 조회
        Character character = characterRepository.findById(characterId)
                .orElseThrow(() -> new CharacterNotFoundException(characterId));
        
        // 2. Status 만료 체크 및 업데이트
        Status status = character.getStatus();
        checkAndUpdateExpiredStatus(status); // 만료 체크 먼저
        
        // 3. 캐릭터 선택 시 Status를 now로 변경하고 날짜 설정
        LocalDateTime now = LocalDateTime.now();
        status.updateRelation(Relation.now);
        status.updateStartDay(now);
        status.updateEndDay(now.plusDays(3));
        statusRepository.save(status);
        
        // 4. 기존 활성 세션이 있는지 확인 (CHAT 타입 우선)
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
        
        // 5. 응답 DTO 생성
        CharacterResponseDto characterDto = toResponseDto(character);
        return new CharacterSelectResponseDto(
                characterId,
                session.getSessionId(),
                characterDto
        );
    }

    /**
     * Status의 만료 여부를 체크하고, 만료되었으면 ex로 변경합니다.
     */
    @Transactional
    private void checkAndUpdateExpiredStatus(Status status) {
        if (status.getEndDay() != null && 
            status.getEndDay().isBefore(LocalDateTime.now()) && 
            status.getRelation() == Relation.now) {
            status.updateRelation(Relation.ex);
            statusRepository.save(status);
        }
    }

    @Override
    @Transactional
    public CharacterResponseDto extendRelationship(Long characterId) {
        // 1. 캐릭터 조회
        Character character = characterRepository.findById(characterId)
                .orElseThrow(() -> new CharacterNotFoundException(characterId));
        
        // 2. Status 확인 및 만료 체크
        Status status = character.getStatus();
        checkAndUpdateExpiredStatus(status);
        
        // 3. 현재 연애 중인 경우에만 연장 가능
        if (status.getRelation() != Relation.now) {
            throw new IllegalStateException("현재 연애 중인 상태에서만 관계를 연장할 수 있습니다.");
        }
        
        // 4. endDay가 null이면 연장 불가
        if (status.getEndDay() == null) {
            throw new IllegalStateException("헤어지는 날짜가 설정되지 않아 연장할 수 없습니다.");
        }
        
        // 5. 헤어지는 날짜를 3일 연장
        LocalDateTime newEndDay = status.getEndDay().plusDays(3);
        status.updateEndDay(newEndDay);
        statusRepository.save(status);
        
        // 6. 응답 DTO 생성
        return toResponseDto(character);
    }

    /**
     * Character 엔티티를 CharacterResponseDto로 변환합니다.
     */
    private CharacterResponseDto toResponseDto(Character character) {
        Status status = character.getStatus();
        return toResponseDto(character, status);
    }

    /**
     * Character 엔티티와 Status를 CharacterResponseDto로 변환합니다.
     * Status를 직접 전달하여 LAZY 로딩 문제를 방지합니다.
     */
    private CharacterResponseDto toResponseDto(Character character, Status status) {
        StatusResponseDto statusDto = new StatusResponseDto(
                status.getStatusId(),
                status.getRelation(),
                status.getStartDay(),
                status.getEndDay(),
                status.getLike()
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

    @Override
    @Transactional
    public List<CharacterResponseDto> getCharacters(Relation relation) {
        List<Character> characters;
        
        if (relation == null) {
            // relation이 null이면 전체 조회
            characters = characterRepository.findAll();
        } else {
            // relation으로 필터링
            characters = characterRepository.findByStatus_Relation(relation);
        }
        
        // 각 캐릭터의 Status 만료 체크 및 DTO 변환
        return characters.stream()
                .map(character -> {
                    checkAndUpdateExpiredStatus(character.getStatus());
                    return toResponseDto(character);
                })
                .toList();
    }
}

