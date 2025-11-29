package com.aigf.gf_plz.domain.call.service;

import com.aigf.gf_plz.domain.character.entity.Character;
import com.aigf.gf_plz.domain.character.exception.CharacterNotFoundException;
import com.aigf.gf_plz.domain.character.repository.CharacterRepository;
import com.aigf.gf_plz.domain.call.dto.CallAudioRequestDto;
import com.aigf.gf_plz.domain.call.dto.CallAudioResponseDto;
import com.aigf.gf_plz.domain.call.dto.CallTextRequestDto;
import com.aigf.gf_plz.domain.call.dto.CallTextResponseDto;
import com.aigf.gf_plz.domain.message.entity.Message;
import com.aigf.gf_plz.domain.message.entity.MessageType;
import com.aigf.gf_plz.domain.message.entity.SenderRole;
import com.aigf.gf_plz.domain.message.repository.MessageRepository;
import com.aigf.gf_plz.domain.session.entity.Session;
import com.aigf.gf_plz.domain.session.entity.SessionType;
import com.aigf.gf_plz.domain.session.repository.SessionRepository;
import com.aigf.gf_plz.global.groq.GroqClient;
import com.aigf.gf_plz.global.groq.GroqMessage;
import com.aigf.gf_plz.global.tts.TtsClient;
import com.aigf.gf_plz.global.whisper.WhisperClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 통화 서비스 구현체
 */
@Service
public class CallServiceImpl implements CallService {

    private static final Logger logger = LoggerFactory.getLogger(CallServiceImpl.class);

    private final GroqClient groqClient;
    private final SessionRepository sessionRepository;
    private final MessageRepository messageRepository;
    private final CharacterRepository characterRepository;
    private final WhisperClient whisperClient;
    private final TtsClient ttsClient;

    public CallServiceImpl(
            GroqClient groqClient,
            SessionRepository sessionRepository,
            MessageRepository messageRepository,
            CharacterRepository characterRepository,
            WhisperClient whisperClient,
            TtsClient ttsClient
    ) {
        this.groqClient = groqClient;
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
        this.characterRepository = characterRepository;
        this.whisperClient = whisperClient;
        this.ttsClient = ttsClient;
    }

    @Override
    @Transactional
    public CallTextResponseDto replyToTranscript(CallTextRequestDto request) {
        Session session = null;
        Message userMessage = null;
        
        try {
            // 1. 세션 조회/생성
            session = findOrCreateSession(
                    request.sessionId(),
                    request.characterId(),
                    SessionType.CALL
            );

            // 2. 히스토리 조회 및 변환 (최근 30개로 제한)
            List<Message> allMessages = messageRepository
                    .findBySessionIdOrderByCreatedAtDesc(session.getSessionId());
            List<Message> messages = allMessages.stream()
                    .limit(30)
                    .collect(Collectors.toList());
            List<GroqMessage> history = convertToGroqMessages(messages);

            // 3. 사용자 메시지 저장 (별도 트랜잭션으로 저장하여 예외 발생 시에도 보존)
            userMessage = saveUserMessage(session, request.transcript(), MessageType.TRANSCRIPT);
            logger.debug("사용자 메시지 저장 완료 - SessionId: {}, MessageId: {}", 
                    session.getSessionId(), userMessage.getMessageId());

            // 4. Character 조회 및 프롬프트 생성
            Character character = characterRepository.findById(request.characterId())
                    .orElseThrow(() -> new CharacterNotFoundException(request.characterId()));
            String systemPrompt = character.generateFullSystemPrompt();

            // 5. Groq API 호출
            String reply;
            try {
                reply = groqClient.generateReply("call", request.transcript(), history, systemPrompt);
            } catch (Exception e) {
                logger.error("Groq API 호출 실패 - SessionId: {}, CharacterId: {}", 
                        session.getSessionId(), request.characterId(), e);
                // 사용자 메시지는 이미 저장되었으므로 기본 응답 반환
                reply = "죄송합니다. 일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
            }

            // 6. AI 응답 저장 (TRANSCRIPT 타입)
            try {
                Message assistantMessage = Message.builder()
                        .session(session)
                        .senderRole(SenderRole.ASSISTANT)
                        .messageType(MessageType.TRANSCRIPT)
                        .textContent(reply)
                        .build();
                messageRepository.save(assistantMessage);
                session.updateLastMessageAt(LocalDateTime.now());
                sessionRepository.save(session);
                logger.debug("AI 응답 메시지 저장 완료 - SessionId: {}, MessageId: {}", 
                        session.getSessionId(), assistantMessage.getMessageId());
            } catch (Exception e) {
                logger.error("AI 응답 메시지 저장 실패 - SessionId: {}", session.getSessionId(), e);
                // 사용자 메시지는 이미 저장되었으므로 응답은 반환
            }

            // 7. 응답 반환
            return new CallTextResponseDto(session.getSessionId(), reply);
            
        } catch (CharacterNotFoundException e) {
            logger.error("캐릭터를 찾을 수 없음 - CharacterId: {}", request.characterId(), e);
            throw e;
        } catch (Exception e) {
            logger.error("통화 텍스트 처리 중 예외 발생 - SessionId: {}, CharacterId: {}", 
                    session != null ? session.getSessionId() : "null", request.characterId(), e);
            // 사용자 메시지가 저장되었는지 확인
            if (userMessage != null && userMessage.getMessageId() != null) {
                logger.info("사용자 메시지는 저장되었으나 전체 프로세스 실패 - MessageId: {}", 
                        userMessage.getMessageId());
            }
            throw new RuntimeException("통화 처리 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public CallAudioResponseDto replyToAudio(MultipartFile audioFile, CallAudioRequestDto request) {
        Session session = null;
        Message userMessage = null;
        String transcript = null;
        
        try {
            // 1. Whisper로 음성 파일을 텍스트로 변환
            try {
                transcript = whisperClient.transcribe(audioFile);
            } catch (Exception e) {
                logger.error("Whisper API 호출 실패 - CharacterId: {}", request.characterId(), e);
                throw new RuntimeException("음성 인식 중 오류가 발생했습니다: " + e.getMessage(), e);
            }

            // 2. 세션 조회/생성
            session = findOrCreateSession(
                    request.sessionId(),
                    request.characterId(),
                    SessionType.CALL
            );

            // 3. 히스토리 조회 및 변환 (최근 30개로 제한)
            List<Message> allMessages = messageRepository
                    .findBySessionIdOrderByCreatedAtDesc(session.getSessionId());
            List<Message> messages = allMessages.stream()
                    .limit(30)
                    .collect(Collectors.toList());
            List<GroqMessage> history = convertToGroqMessages(messages);

            // 4. 사용자 메시지 저장 (별도 트랜잭션으로 저장하여 예외 발생 시에도 보존)
            userMessage = saveUserMessage(session, transcript, MessageType.TRANSCRIPT);
            logger.debug("사용자 메시지 저장 완료 - SessionId: {}, MessageId: {}", 
                    session.getSessionId(), userMessage.getMessageId());

            // 5. Character 조회 및 프롬프트 생성
            Character character = characterRepository.findById(request.characterId())
                    .orElseThrow(() -> new CharacterNotFoundException(request.characterId()));
            String systemPrompt = character.generateFullSystemPrompt();

            // 6. Groq API 호출하여 답변 생성
            String reply;
            try {
                reply = groqClient.generateReply("call", transcript, history, systemPrompt);
            } catch (Exception e) {
                logger.error("Groq API 호출 실패 - SessionId: {}, CharacterId: {}", 
                        session.getSessionId(), request.characterId(), e);
                // 사용자 메시지는 이미 저장되었으므로 기본 응답 반환
                reply = "죄송합니다. 일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
            }

            // 7. AI 응답 저장 (TRANSCRIPT 타입)
            try {
                Message assistantMessage = Message.builder()
                        .session(session)
                        .senderRole(SenderRole.ASSISTANT)
                        .messageType(MessageType.TRANSCRIPT)
                        .textContent(reply)
                        .build();
                messageRepository.save(assistantMessage);
                session.updateLastMessageAt(LocalDateTime.now());
                sessionRepository.save(session);
                logger.debug("AI 응답 메시지 저장 완료 - SessionId: {}, MessageId: {}", 
                        session.getSessionId(), assistantMessage.getMessageId());
            } catch (Exception e) {
                logger.error("AI 응답 메시지 저장 실패 - SessionId: {}", session.getSessionId(), e);
                // 사용자 메시지는 이미 저장되었으므로 응답은 반환
            }

            // 8. TTS로 답변을 음성 파일로 변환
            byte[] audioData;
            try {
                audioData = ttsClient.synthesize(reply, character.getVoiceType().name());
            } catch (Exception e) {
                logger.error("TTS API 호출 실패 - SessionId: {}, CharacterId: {}", 
                        session.getSessionId(), request.characterId(), e);
                // TTS 실패 시 빈 오디오 반환
                audioData = new byte[0];
            }

            // 9. 응답 반환
            return new CallAudioResponseDto(session.getSessionId(), audioData, transcript);
            
        } catch (CharacterNotFoundException e) {
            logger.error("캐릭터를 찾을 수 없음 - CharacterId: {}", request.characterId(), e);
            throw e;
        } catch (Exception e) {
            logger.error("통화 오디오 처리 중 예외 발생 - SessionId: {}, CharacterId: {}", 
                    session != null ? session.getSessionId() : "null", request.characterId(), e);
            // 사용자 메시지가 저장되었는지 확인
            if (userMessage != null && userMessage.getMessageId() != null) {
                logger.info("사용자 메시지는 저장되었으나 전체 프로세스 실패 - MessageId: {}", 
                        userMessage.getMessageId());
            }
            throw new RuntimeException("통화 처리 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 세션을 조회하거나 생성합니다.
     * 기존 세션이 있으면 재사용하고, 없을 때만 새로 생성합니다.
     */
    private Session findOrCreateSession(
            java.util.Optional<Long> sessionId,
            Long characterId,
            SessionType sessionType
    ) {
        // sessionId가 제공된 경우
        if (sessionId.isPresent()) {
            Long providedSessionId = sessionId.get();
            // 활성 여부와 관계없이 세션 조회
            Optional<Session> existingSession = sessionRepository.findBySessionId(providedSessionId);
            
            if (existingSession.isPresent()) {
                Session session = existingSession.get();
                // 세션이 해당 캐릭터와 세션 타입과 일치하는지 확인
                if (session.getCharacter().getCharacterId().equals(characterId) 
                        && session.getSessionType() == sessionType) {
                    // 비활성화되어 있으면 재활성화
                    if (!session.getIsActive()) {
                        session.activate();
                        sessionRepository.save(session);
                        logger.debug("비활성 세션 재활성화 - SessionId: {}, CharacterId: {}", 
                                session.getSessionId(), characterId);
                    }
                    return session;
                } else {
                    logger.warn("제공된 세션 ID가 캐릭터 또는 세션 타입과 일치하지 않음 - SessionId: {}, CharacterId: {}, SessionType: {}", 
                            providedSessionId, characterId, sessionType);
                    // 일치하지 않으면 기존 세션 무시하고 새로 찾거나 생성
                }
            }
        }

        // sessionId가 없거나 제공된 sessionId가 유효하지 않은 경우
        // 1. 먼저 활성 세션 조회
        List<Session> activeSessions = sessionRepository
                .findByCharacterIdAndSessionTypeAndIsActiveTrueOrderByLastMessageAtDesc(characterId, sessionType);
        
        if (!activeSessions.isEmpty()) {
            // 가장 최근 활성 세션 반환
            Session mostRecentSession = activeSessions.get(0);
            logger.debug("기존 활성 세션 사용 - SessionId: {}, CharacterId: {}, LastMessageAt: {}", 
                    mostRecentSession.getSessionId(), characterId, mostRecentSession.getLastMessageAt());
            return mostRecentSession;
        }

        // 2. 활성 세션이 없으면 비활성 세션도 조회 (비활성 포함)
        List<Session> allSessions = sessionRepository
                .findByCharacterIdAndSessionTypeOrderByLastMessageAtDesc(characterId, sessionType);
        
        if (!allSessions.isEmpty()) {
            // 가장 최근 세션을 재활성화하여 사용
            Session mostRecentSession = allSessions.get(0);
            mostRecentSession.activate();
            sessionRepository.save(mostRecentSession);
            logger.debug("비활성 세션 재활성화하여 사용 - SessionId: {}, CharacterId: {}, LastMessageAt: {}", 
                    mostRecentSession.getSessionId(), characterId, mostRecentSession.getLastMessageAt());
            return mostRecentSession;
        }

        // 3. 정말 세션이 없을 때만 새로 생성
        logger.debug("새 세션 생성 - CharacterId: {}, SessionType: {}", characterId, sessionType);
        return createNewSession(characterId, sessionType);
    }

    /**
     * 새 세션을 생성합니다.
     */
    private Session createNewSession(Long characterId, SessionType sessionType) {
        Character character = characterRepository.findById(characterId)
                .orElseThrow(() -> new CharacterNotFoundException(characterId));

        Session session = Session.builder()
                .character(character)
                .sessionType(sessionType)
                .build();

        return sessionRepository.save(session);
    }

    /**
     * 사용자 메시지를 별도 트랜잭션으로 저장합니다.
     * 예외 발생 시에도 사용자 메시지는 보존됩니다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Message saveUserMessage(Session session, String content, MessageType messageType) {
        Message userMessage = Message.builder()
                .session(session)
                .senderRole(SenderRole.USER)
                .messageType(messageType)
                .textContent(content)
                .build();
        messageRepository.save(userMessage);
        session.updateLastMessageAt(LocalDateTime.now());
        sessionRepository.save(session);
        return userMessage;
    }

    /**
     * Message 리스트를 GroqMessage 리스트로 변환합니다.
     * USER와 ASSISTANT 메시지만 변환하고, 시간순으로 정렬합니다.
     */
    private List<GroqMessage> convertToGroqMessages(List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return Collections.emptyList();
        }

        // 역순으로 정렬된 메시지를 시간순으로 변환
        List<Message> sortedMessages = new java.util.ArrayList<>(messages);
        Collections.reverse(sortedMessages);

        return sortedMessages.stream()
                .filter(msg -> msg.getSenderRole() == SenderRole.USER 
                        || msg.getSenderRole() == SenderRole.ASSISTANT)
                .map(msg -> {
                    String role = msg.getSenderRole() == SenderRole.USER ? "user" : "assistant";
                    return new GroqMessage(role, msg.getTextContent());
                })
                .collect(Collectors.toList());
    }
}

