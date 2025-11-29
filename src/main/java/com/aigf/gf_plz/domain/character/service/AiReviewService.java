package com.aigf.gf_plz.domain.character.service;

import com.aigf.gf_plz.domain.character.entity.Character;
import com.aigf.gf_plz.domain.message.entity.Message;
import com.aigf.gf_plz.domain.message.entity.SenderRole;
import com.aigf.gf_plz.domain.message.repository.MessageRepository;
import com.aigf.gf_plz.domain.session.entity.SessionType;
import com.aigf.gf_plz.domain.session.repository.SessionRepository;
import com.aigf.gf_plz.global.groq.GroqClient;
import com.aigf.gf_plz.global.groq.GroqMessage;
import com.aigf.gf_plz.global.groq.exception.GroqApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * AI 리뷰 요약 생성 서비스
 */
@Service
public class AiReviewService {

    private static final Logger log = LoggerFactory.getLogger(AiReviewService.class);
    private static final int MAX_HISTORY = 30;

    private final GroqClient groqClient;
    private final SessionRepository sessionRepository;
    private final MessageRepository messageRepository;

    public AiReviewService(
            GroqClient groqClient,
            SessionRepository sessionRepository,
            MessageRepository messageRepository
    ) {
        this.groqClient = groqClient;
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
    }

    public String generateReview(Character character) {
        List<GroqMessage> history = collectConversation(character);

        String systemPrompt = "사용자와 캐릭터의 대화를 바탕으로 전여친에게 남길 한줄평을 정리하는 AI입니다.";
        String userPrompt = String.format(
                "%s와의 최근 대화를 보고, 사랑과 아쉬움을 담은 한 문장으로 전여친에게 건넬 한줄평을 한국어로 진심 있게 작성해줘. 예: \"우리가 함께한 시간 고마웠고, 이제는 너에게 더 나은 사람이 나타나길 바랄게.\"",
                character.getName()
        );

        try {
            return groqClient.generateReply("chat", userPrompt, history, systemPrompt);
        } catch (GroqApiException e) {
            log.error("Groq API 호출 실패: {}", e.getMessage());
            return null;
        }
    }

    private List<GroqMessage> collectConversation(Character character) {
        return sessionRepository.findByCharacterIdAndSessionTypeOrderByLastMessageAtDesc(character.getCharacterId(), SessionType.CHAT).stream()
                .findFirst()
                .map(session -> messageRepository.findBySessionIdOrderByCreatedAtAsc(session.getSessionId()))
                .orElse(Collections.emptyList())
                .stream()
                .limit(MAX_HISTORY)
                .map(this::toGroqMessage)
                .toList();
    }

    private GroqMessage toGroqMessage(Message message) {
        String role = switch (message.getSenderRole()) {
            case USER -> "user";
            case ASSISTANT -> "assistant";
            default -> "system";
        };
        return new GroqMessage(role, message.getTextContent());
    }
}

