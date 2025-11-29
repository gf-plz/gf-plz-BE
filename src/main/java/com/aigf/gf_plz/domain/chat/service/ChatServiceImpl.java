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
            } catch (Exception e) {
                logger.error("Groq API 호출 실패 - SessionId: {}, CharacterId: {}", 
                        session.getSessionId(), request.characterId(), e);
                // 사용자 메시지는 이미 저장되었으므로 기본 응답 반환
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
     */
    private Session findOrCreateSession(
            java.util.Optional<Long> sessionId,
            Long characterId,
            SessionType sessionType
    ) {
        // sessionId가 제공된 경우
        if (sessionId.isPresent()) {
            return sessionRepository
                    .findBySessionIdAndIsActiveTrue(sessionId.get())
                    .orElseGet(() -> createNewSession(characterId, sessionType));
        }

        // sessionId가 없는 경우 - characterId로 활성 세션 조회
        return sessionRepository
                .findByCharacterIdAndSessionTypeAndIsActiveTrue(characterId, sessionType)
                .orElseGet(() -> createNewSession(characterId, sessionType));
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
}

