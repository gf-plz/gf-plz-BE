package com.aigf.gf_plz.domain.message.service;

import com.aigf.gf_plz.domain.message.dto.MessageResponseDto;
import com.aigf.gf_plz.domain.message.entity.Message;
import com.aigf.gf_plz.domain.message.repository.MessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 메시지 서비스 구현체
 */
@Service
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;

    public MessageServiceImpl(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageResponseDto> getMessagesBySessionId(Long sessionId) {
        List<Message> messages = messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
        return messages.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Message 엔티티를 MessageResponseDto로 변환합니다.
     */
    private MessageResponseDto toResponseDto(Message message) {
        return new MessageResponseDto(
                message.getMessageId(),
                message.getSession().getSessionId(),
                message.getSenderRole(),
                message.getMessageType(),
                message.getTextContent(),
                message.getCreatedAt()
        );
    }
}





