package com.aigf.gf_plz.domain.message.dto;

import com.aigf.gf_plz.domain.message.entity.MessageType;
import com.aigf.gf_plz.domain.message.entity.SenderRole;

import java.time.LocalDateTime;

/**
 * 메시지 응답 DTO
 */
public record MessageResponseDto(
        Long messageId,
        Long sessionId,
        SenderRole senderRole,
        MessageType messageType,
        String textContent,
        LocalDateTime createdAt
) {}





