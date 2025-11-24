package com.aigf.gf_plz.domain.call.service;

import com.aigf.gf_plz.domain.character.entity.Character;
import com.aigf.gf_plz.domain.character.exception.CharacterNotFoundException;
import com.aigf.gf_plz.domain.character.repository.CharacterRepository;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 통화 서비스 구현체
 */
@Service
public class CallServiceImpl implements CallService {

    private final GroqClient groqClient;
    private final SessionRepository sessionRepository;
    private final MessageRepository messageRepository;
    private final CharacterRepository characterRepository;

    public CallServiceImpl(
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
    public CallTextResponseDto replyToTranscript(CallTextRequestDto request) {
        // 1. 세션 조회/생성
        Session session = findOrCreateSession(
                request.sessionId(),
                request.characterId(),
                SessionType.CALL
        );

        // 2. 히스토리 조회 및 변환
        List<Message> messages = messageRepository
                .findTop30BySessionIdOrderByCreatedAtDesc(session.getSessionId());
        List<GroqMessage> history = convertToGroqMessages(messages);

        // 3. 사용자 메시지 저장 (TRANSCRIPT 타입)
        Message userMessage = Message.builder()
                .session(session)
                .senderRole(SenderRole.USER)
                .messageType(MessageType.TRANSCRIPT)
                .textContent(request.transcript())
                .build();
        messageRepository.save(userMessage);
        session.updateLastMessageAt(LocalDateTime.now());
        sessionRepository.save(session);

        // 4. Groq API 호출
        String reply = groqClient.generateReply("call", request.transcript(), history);

        // 5. AI 응답 저장 (TRANSCRIPT 타입)
        Message assistantMessage = Message.builder()
                .session(session)
                .senderRole(SenderRole.ASSISTANT)
                .messageType(MessageType.TRANSCRIPT)
                .textContent(reply)
                .build();
        messageRepository.save(assistantMessage);
        session.updateLastMessageAt(LocalDateTime.now());
        sessionRepository.save(session);

        // 6. 응답 반환
        return new CallTextResponseDto(reply, session.getSessionId());
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

