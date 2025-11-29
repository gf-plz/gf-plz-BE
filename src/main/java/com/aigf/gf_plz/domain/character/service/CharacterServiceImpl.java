package com.aigf.gf_plz.domain.character.service;

import com.aigf.gf_plz.domain.character.dto.CharacterCreateRequestDto;
import com.aigf.gf_plz.domain.character.dto.CharacterResponseDto;
import com.aigf.gf_plz.domain.character.dto.CharacterSelectResponseDto;
import com.aigf.gf_plz.domain.character.dto.SessionIdResponseDto;
import com.aigf.gf_plz.domain.character.dto.StatusResponseDto;
import com.aigf.gf_plz.domain.character.entity.Character;
import com.aigf.gf_plz.domain.character.entity.Gender;
import com.aigf.gf_plz.domain.character.entity.Relation;
import com.aigf.gf_plz.domain.character.exception.CharacterNotFoundException;
import com.aigf.gf_plz.domain.character.repository.CharacterRepository;
import com.aigf.gf_plz.domain.session.entity.Session;
import com.aigf.gf_plz.domain.session.entity.SessionType;
import com.aigf.gf_plz.domain.session.repository.SessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 캐릭터 서비스 구현체
 */
@Service
public class CharacterServiceImpl implements CharacterService {

    private static final Logger logger = LoggerFactory.getLogger(CharacterServiceImpl.class);

    private final CharacterRepository characterRepository;
    private final SessionRepository sessionRepository;

    public CharacterServiceImpl(
            CharacterRepository characterRepository,
            SessionRepository sessionRepository
    ) {
        this.characterRepository = characterRepository;
        this.sessionRepository = sessionRepository;
    }

    @Override
    @Transactional
    public CharacterResponseDto createCharacter(CharacterCreateRequestDto request) {
        // 요청 데이터 검증
        if (request.attachment() == null) {
            throw new IllegalArgumentException("애착타입은 필수입니다.");
        }
        
        // Character 생성 (Status 정보 포함)
        Character character = Character.builder()
                .relation(Relation.yet) // 초기 상태는 아직 만나지 않음
                .startDay(null)
                .endDay(null)
                .like(0)
                .description(request.description())
                .imageUrl(request.imageUrl())
                .voiceType(request.voiceType())
                .mbti(request.mbti())
                .gender(request.gender())
                .name(request.name())
                .attachment(request.attachment())
                .teto(request.teto())
                .aiSummary(null)
                .build();
        
        Character savedCharacter = characterRepository.save(character);

        return toResponseDto(savedCharacter);
    }

    @Override
    @Transactional(readOnly = true)
    public CharacterResponseDto getCharacter(Long characterId) {
        Character character = characterRepository.findById(characterId)
                .orElseThrow(() -> new CharacterNotFoundException(characterId));
        
        // Status 만료 체크 (프론트에서 처리)
        
        return toResponseDto(character);
    }

    @Override
    @Transactional(readOnly = true)
    public CharacterResponseDto getRecentCharacter(Gender gender) {
        // 최근 활성 세션 조회 (성별 필터링 적용)
        List<Session> recentSessions;
        if (gender == null) {
            recentSessions = sessionRepository.findRecentActiveSessions();
        } else {
            recentSessions = sessionRepository.findRecentActiveSessionsByGender(gender);
        }
        
        // 가장 최근 세션이 없으면 null 반환
        if (recentSessions.isEmpty()) {
            return null;
        }
        
        // 가장 최근 세션의 캐릭터 반환
        Session mostRecentSession = recentSessions.get(0);
        Character character = mostRecentSession.getCharacter();
        
        // Status 만료 체크 (프론트에서 처리)
        
        return toResponseDto(character);
    }

    @Override
    @Transactional
    public CharacterSelectResponseDto selectCharacter(Long characterId) {
        // 1. 캐릭터 조회
        Character character = characterRepository.findById(characterId)
                .orElseThrow(() -> new CharacterNotFoundException(characterId));
        
        // 2. Status 만료 체크 및 업데이트 (프론트에서 처리)
        
        // 3. 캐릭터 선택 시 Status를 now로 변경하고 날짜 설정
        LocalDateTime now = LocalDateTime.now();
        character.updateRelation(Relation.now);
        character.updateStartDay(now);
        character.updateEndDay(now.plusDays(3));
        characterRepository.save(character);
        
        // 4. 기존 활성 세션이 있는지 확인 (CHAT 타입 우선, 가장 최근 세션 사용)
        List<Session> existingChatSessions = sessionRepository
                .findByCharacterIdAndSessionTypeAndIsActiveTrueOrderByLastMessageAtDesc(characterId, SessionType.CHAT);
        
        Session session;
        if (!existingChatSessions.isEmpty()) {
            // 가장 최근 활성 세션 사용
            session = existingChatSessions.get(0);
            logger.debug("기존 활성 세션 사용 - SessionId: {}, CharacterId: {}, LastMessageAt: {}", 
                    session.getSessionId(), characterId, session.getLastMessageAt());
        } else {
            // 기존 세션이 없으면 새 세션 생성 (CHAT 타입)
            session = Session.builder()
                    .character(character)
                    .sessionType(SessionType.CHAT)
                    .build();
            session = sessionRepository.save(session);
            logger.debug("새 세션 생성 - SessionId: {}, CharacterId: {}", 
                    session.getSessionId(), characterId);
        }
        
        // 5. 응답 DTO 생성
        CharacterResponseDto characterDto = toResponseDto(character);
        return new CharacterSelectResponseDto(
                characterId,
                session.getSessionId(),
                characterDto
        );
    }

    @Override
    @Transactional
    public CharacterResponseDto extendRelationship(Long characterId) {
        // 1. 캐릭터 조회
        Character character = characterRepository.findById(characterId)
                .orElseThrow(() -> new CharacterNotFoundException(characterId));
        
        // 2. Status 확인 및 만료 체크 (프론트가 판단)
        
        // 3. 현재 연애 중인 경우에만 연장 가능
        if (character.getRelation() != Relation.now) {
            throw new IllegalStateException("현재 연애 중인 상태에서만 관계를 연장할 수 있습니다.");
        }
        
        // 4. endDay가 null이면 연장 불가
        if (character.getEndDay() == null) {
            throw new IllegalStateException("헤어지는 날짜가 설정되지 않아 연장할 수 없습니다.");
        }
        
        // 5. 헤어지는 날짜를 현재 시점 기준으로 3일 연장
        LocalDateTime newEndDay = LocalDateTime.now().plusDays(3);
        character.updateEndDay(newEndDay);
        characterRepository.save(character);
        
        // 6. 응답 DTO 생성
        return toResponseDto(character);
    }

    /**
     * Character 엔티티를 CharacterResponseDto로 변환합니다.
     */
    private CharacterResponseDto toResponseDto(Character character) {
        // StatusResponseDto를 생성하여 DTO 구조 유지
        StatusResponseDto statusDto = new StatusResponseDto(
                // statusId는 이제 characterId와 같거나, null로 처리. 
                // 기존 구조 유지를 위해 characterId 사용하거나 임의 값 사용. 
                // Status 엔티티가 없어졌으므로 별도의 ID는 없지만, DTO가 요구한다면 characterId를 사용하는 것이 합리적.
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

    @Override
    @Transactional
    public List<CharacterResponseDto> getCharacters(Relation relation, Gender gender) {
        List<Character> characters;
        
        if (relation == null && gender == null) {
            // 둘 다 null이면 전체 조회
            characters = characterRepository.findAll();
        } else if (relation != null && gender != null) {
            // 둘 다 있으면 둘 다로 필터링
            characters = characterRepository.findByRelationAndGender(relation, gender);
        } else if (relation != null) {
            // relation만 있으면 relation으로 필터링
            characters = characterRepository.findByRelation(relation);
        } else {
            // gender만 있으면 gender로 필터링
            characters = characterRepository.findByGender(gender);
        }
        
        return characters.stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SessionIdResponseDto getRecentSessionId(Long characterId) {
        // 캐릭터 존재 여부 확인
        if (!characterRepository.existsById(characterId)) {
            throw new CharacterNotFoundException(characterId);
        }
        
        // 캐릭터의 가장 최근 세션 조회 (세션 타입 무관, 비활성 포함)
        List<Session> sessions = sessionRepository
                .findByCharacterIdOrderByLastMessageAtDesc(characterId);
        
        if (sessions.isEmpty()) {
            // 세션이 없으면 null 반환
            return null;
        }
        
        // 가장 최근 세션 반환
        Session mostRecentSession = sessions.get(0);
        return new SessionIdResponseDto(characterId, mostRecentSession.getSessionId());
    }
}