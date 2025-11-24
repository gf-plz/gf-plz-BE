package com.aigf.gf_plz.domain.chat.service;

import com.aigf.gf_plz.domain.chat.dto.ChatRequestDto;
import com.aigf.gf_plz.domain.chat.dto.ChatResponseDto;

/**
 * 채팅 서비스 인터페이스
 */
public interface ChatService {
    /**
     * 사용자의 채팅 메시지에 대해 AI 여자친구의 답변을 생성합니다.
     * 
     * @param request 사용자의 채팅 메시지
     * @return AI 여자친구의 답변
     */
    ChatResponseDto chat(ChatRequestDto request);
}
