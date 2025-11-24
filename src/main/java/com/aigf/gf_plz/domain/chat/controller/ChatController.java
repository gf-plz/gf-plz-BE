package com.aigf.gf_plz.domain.chat.controller;

import com.aigf.gf_plz.domain.chat.dto.ChatRequestDto;
import com.aigf.gf_plz.domain.chat.dto.ChatResponseDto;
import com.aigf.gf_plz.domain.chat.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 채팅 컨트롤러
 * 텍스트 기반 채팅 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * 단순 텍스트 채팅용 엔드포인트
     * 프론트에서 사용자가 입력한 텍스트를 보내면,
     * 캐릭터 프롬프트를 반영한 답변을 JSON으로 돌려준다.
     * 
     * @param request 사용자의 채팅 메시지
     * @return AI 여자친구의 답변
     */
    @PostMapping
    public ChatResponseDto chat(@Valid @RequestBody ChatRequestDto request) {
        return chatService.chat(request);
    }

    // TODO: 나중에 SSE 스트리밍 버전 /api/chat/stream 을 별도로 추가할 수 있도록 설계
}
