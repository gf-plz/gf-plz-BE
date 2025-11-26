package com.aigf.gf_plz.domain.message.service;

import com.aigf.gf_plz.domain.message.dto.MessageResponseDto;

import java.util.List;

/**
 * 메시지 서비스 인터페이스
 */
public interface MessageService {

    /**
     * 세션 ID로 모든 메시지를 조회합니다.
     * @param sessionId 세션 ID
     * @return 메시지 리스트 (시간순)
     */
    List<MessageResponseDto> getMessagesBySessionId(Long sessionId);
}






