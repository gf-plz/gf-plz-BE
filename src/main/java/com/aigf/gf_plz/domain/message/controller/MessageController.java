package com.aigf.gf_plz.domain.message.controller;

import com.aigf.gf_plz.domain.message.dto.MessageResponseDto;
import com.aigf.gf_plz.domain.message.service.MessageService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 메시지 컨트롤러
 */
@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    /**
     * 세션 ID로 모든 메시지를 조회합니다.
     *
     * @param sessionId 세션 ID
     * @return 메시지 리스트 (시간순)
     */
    @GetMapping(value = "/session/{sessionId}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public List<MessageResponseDto> getMessagesBySessionId(@PathVariable Long sessionId) {
        return messageService.getMessagesBySessionId(sessionId);
    }
}



