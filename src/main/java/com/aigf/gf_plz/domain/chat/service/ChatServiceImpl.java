package com.aigf.gf_plz.domain.chat.service;

import com.aigf.gf_plz.domain.character.entity.Character;
import com.aigf.gf_plz.domain.character.exception.CharacterNotFoundException;
import com.aigf.gf_plz.domain.character.repository.CharacterRepository;
import com.aigf.gf_plz.domain.chat.dto.ChatRequestDto;
import com.aigf.gf_plz.domain.chat.dto.ChatResponseDto;
import com.aigf.gf_plz.domain.message.entity.Message;
import com.aigf.gf_plz.domain.message.entity.MessageType;
import com.aigf.gf_plz.domain.message.entity.SenderRole;
import com.aigf.gf_plz.domain.message.repository.MessageRepository;
import com.aigf.gf_plz.domain.session.entity.Session;
import com.aigf.gf_plz.domain.session.entity.SessionType;
import com.aigf.gf_plz.domain.session.repository.SessionRepository;
import com.aigf.gf_plz.global.groq.GroqClient;
import com.aigf.gf_plz.global.groq.GroqMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 채팅 서비스 구현체
 */
@Service
public class ChatServiceImpl implements ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatServiceImpl.class);

    private final GroqClient groqClient;
    private final SessionRepository sessionRepository;
    private final MessageRepository messageRepository;
    private final CharacterRepository characterRepository;

    public ChatServiceImpl(
            GroqClient groqClient,
            SessionRepository sessionRepository,
            MessageRepository messageRepository,
            CharacterRepository characterRepository
    ) {
        this.groqClient = groqClient;
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
        this.characterRepository = characterRepository;
    }

    @Override
    @Transactional
    public ChatResponseDto chat(ChatRequestDto request) {
        Session session = null;
        Message userMessage = null;
        
        try {
            // 1. 세션 조회/생성
            session = findOrCreateSession(
                    request.sessionId(),
                    request.characterId(),
                    SessionType.CHAT
            );

            // 2. 히스토리 조회 및 변환 (최근 30개로 제한)
            List<Message> allMessages = messageRepository
                    .findBySessionIdOrderByCreatedAtDesc(session.getSessionId());
            List<Message> messages = allMessages.stream()
                    .limit(30)
                    .collect(Collectors.toList());
            List<GroqMessage> history = convertToGroqMessages(messages);

            // 3. 사용자 메시지 저장 (별도 트랜잭션으로 저장하여 예외 발생 시에도 보존)
            userMessage = saveUserMessage(session, request.content());
            logger.debug("사용자 메시지 저장 완료 - SessionId: {}, MessageId: {}", 
                    session.getSessionId(), userMessage.getMessageId());

            // 4. Character 조회 및 프롬프트 생성
            Character character = characterRepository.findById(request.characterId())
                    .orElseThrow(() -> new CharacterNotFoundException(request.characterId()));
            String systemPrompt = character.generateFullSystemPrompt();

            // 5. Groq API 호출
            String reply;
            try {
                reply = groqClient.generateReply("chat", request.content(), history, systemPrompt);
            } catch (com.aigf.gf_plz.global.groq.exception.GroqApiException e) {
                logger.error("Groq API 호출 실패 - SessionId: {}, CharacterId: {}, Error: {}", 
                        session.getSessionId(), request.characterId(), e.getMessage(), e);
                // 상태 코드에 따라 다른 사용자 메시지 반환
                reply = getUserFriendlyErrorMessage(e.getMessage());
            } catch (Exception e) {
                logger.error("Groq API 호출 중 예상치 못한 오류 - SessionId: {}, CharacterId: {}", 
                        session.getSessionId(), request.characterId(), e);
                reply = "죄송합니다. 일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
            }

            // 6. AI 응답 저장
            try {
                Message assistantMessage = Message.builder()
                        .session(session)
                        .senderRole(SenderRole.ASSISTANT)
                        .messageType(MessageType.TEXT)
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
            return new ChatResponseDto(session.getSessionId(), reply);
            
        } catch (CharacterNotFoundException e) {
            logger.error("캐릭터를 찾을 수 없음 - CharacterId: {}", request.characterId(), e);
            throw e;
        } catch (Exception e) {
            logger.error("채팅 처리 중 예외 발생 - SessionId: {}, CharacterId: {}", 
                    session != null ? session.getSessionId() : "null", request.characterId(), e);
            // 사용자 메시지가 저장되었는지 확인
            if (userMessage != null && userMessage.getMessageId() != null) {
                logger.info("사용자 메시지는 저장되었으나 전체 프로세스 실패 - MessageId: {}", 
                        userMessage.getMessageId());
            }
            throw new RuntimeException("채팅 처리 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 사용자 메시지를 별도 트랜잭션으로 저장합니다.
     * 예외 발생 시에도 사용자 메시지는 보존됩니다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Message saveUserMessage(Session session, String content) {
        Message userMessage = Message.builder()
                .session(session)
                .senderRole(SenderRole.USER)
                .messageType(MessageType.TEXT)
                .textContent(content)
                .build();
        messageRepository.save(userMessage);
        session.updateLastMessageAt(LocalDateTime.now());
        sessionRepository.save(session);
        return userMessage;
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

    /**
     * Groq API 에러 메시지에서 상태 코드를 추출하여 사용자 친화적인 메시지를 반환합니다.
     */
    private String getUserFriendlyErrorMessage(String errorMessage) {
        if (errorMessage == null) {
            return "죄송합니다. 일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
        }

        if (errorMessage.contains("(401)")) {
            return "죄송합니다. 서비스 설정에 문제가 있어 대화를 이어갈 수 없습니다. 관리자에게 문의해주세요.";
        } else if (errorMessage.contains("(429)")) {
            return "죄송합니다. 현재 요청이 너무 많아 잠시 대기 중입니다. 잠시 후 다시 시도해주세요.";
        } else if (errorMessage.contains("(500)") || errorMessage.contains("(502)") 
                || errorMessage.contains("(503)") || errorMessage.contains("(504)")) {
            return "죄송합니다. AI 서버에 일시적인 문제가 발생했습니다. 잠시 후 다시 시도해주세요.";
        } else if (errorMessage.contains("(400)")) {
            return "죄송합니다. 요청 처리 중 오류가 발생했습니다. 다시 시도해주세요.";
        } else {
            return "죄송합니다. 일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
        }
    }
}

