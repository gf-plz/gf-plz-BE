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

    /**
     * 대화 내용을 분석하여 애정도를 평가합니다.
     * 대화가 적절하고 긍정적이면 높은 점수, 부적절하거나 부정적이면 낮은 점수를 반환합니다.
     * 
     * @param character 캐릭터
     * @return 애정도 점수 (0-100)
     */
    public Integer evaluateAffection(Character character) {
        List<GroqMessage> history = collectConversation(character);

        // 대화가 없으면 기본 점수 50 반환
        if (history == null || history.isEmpty()) {
            log.warn("대화 내역이 없어 기본 애정도 50을 반환합니다. CharacterId: {}", character.getCharacterId());
            return 50;
        }

        String systemPrompt = "사용자와 캐릭터의 대화를 분석하여 애정도를 평가하는 AI입니다. " +
                "대화의 적절성, 긍정성, 존중, 관심 등을 종합적으로 평가하여 0부터 100까지의 점수를 매깁니다.";
        
        String userPrompt = String.format(
                "%s와의 최근 대화를 분석하여 애정도를 평가해주세요.\n\n" +
                "평가 기준:\n" +
                "- 대화가 적절하고 정중했으면 높은 점수 (80-100점)\n" +
                "- 대화가 긍정적이고 서로를 존중했으면 높은 점수 (70-90점)\n" +
                "- 대화가 평범하고 무난했으면 중간 점수 (40-70점)\n" +
                "- 대화가 부적절하거나 부정적이면 낮은 점수 (0-40점)\n" +
                "- 모욕적이거나 비정상적인 내용이 있으면 매우 낮은 점수 (0-20점)\n\n" +
                "대화 내용을 종합적으로 분석하여 0부터 100까지의 정수 점수만 숫자로 반환해주세요. " +
                "설명이나 다른 텍스트 없이 숫자만 반환해주세요. 예: 75",
                character.getName()
        );

        try {
            String response = groqClient.generateReply("chat", userPrompt, history, systemPrompt);
            
            if (response == null || response.isBlank()) {
                log.warn("애정도 평가 응답이 비어있어 기본 점수 50을 반환합니다. CharacterId: {}", character.getCharacterId());
                return 50;
            }

            // 응답에서 숫자만 추출
            String scoreStr = response.trim().replaceAll("[^0-9]", "");
            if (scoreStr.isEmpty()) {
                log.warn("애정도 평가 응답에서 숫자를 찾을 수 없어 기본 점수 50을 반환합니다. 응답: {}, CharacterId: {}", 
                        response, character.getCharacterId());
                return 50;
            }

            int score = Integer.parseInt(scoreStr);
            
            // 0-100 범위로 제한
            if (score < 0) {
                score = 0;
            } else if (score > 100) {
                score = 100;
            }

            log.info("애정도 평가 완료 - CharacterId: {}, Score: {}", character.getCharacterId(), score);
            return score;

        } catch (GroqApiException e) {
            log.error("애정도 평가 Groq API 호출 실패: {}", e.getMessage());
            return 50; // 에러 발생 시 기본 점수 반환
        } catch (NumberFormatException e) {
            log.error("애정도 평가 응답 파싱 실패: {}", e.getMessage());
            return 50; // 파싱 실패 시 기본 점수 반환
        } catch (Exception e) {
            log.error("애정도 평가 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            return 50; // 예외 발생 시 기본 점수 반환
        }
    }

    /**
     * 캐릭터와의 대화를 수집합니다.
     * 채팅과 통화 세션의 메시지를 모두 수집하여 반환합니다.
     */
    private List<GroqMessage> collectConversation(Character character) {
        // 채팅 세션 메시지 수집
        List<Message> chatMessages = sessionRepository
                .findByCharacterIdAndSessionTypeOrderByLastMessageAtDesc(character.getCharacterId(), SessionType.CHAT)
                .stream()
                .findFirst()
                .map(session -> messageRepository.findBySessionIdOrderByCreatedAtAsc(session.getSessionId()))
                .orElse(Collections.emptyList());
        
        // 통화 세션 메시지 수집
        List<Message> callMessages = sessionRepository
                .findByCharacterIdAndSessionTypeOrderByLastMessageAtDesc(character.getCharacterId(), SessionType.CALL)
                .stream()
                .findFirst()
                .map(session -> messageRepository.findBySessionIdOrderByCreatedAtAsc(session.getSessionId()))
                .orElse(Collections.emptyList());
        
        // 두 세션의 메시지를 시간순으로 합침
        List<Message> allMessages = new java.util.ArrayList<>();
        allMessages.addAll(chatMessages);
        allMessages.addAll(callMessages);
        
        // 시간순으로 정렬
        allMessages.sort((m1, m2) -> {
            if (m1.getCreatedAt() == null && m2.getCreatedAt() == null) return 0;
            if (m1.getCreatedAt() == null) return -1;
            if (m2.getCreatedAt() == null) return 1;
            return m1.getCreatedAt().compareTo(m2.getCreatedAt());
        });
        
        // 최대 개수로 제한하고 GroqMessage로 변환
        return allMessages.stream()
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

