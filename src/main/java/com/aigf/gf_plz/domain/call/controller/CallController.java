package com.aigf.gf_plz.domain.call.controller;

import com.aigf.gf_plz.domain.call.dto.CallTextRequestDto;
import com.aigf.gf_plz.domain.call.dto.CallTextResponseDto;
import com.aigf.gf_plz.domain.call.service.CallService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 통화 컨트롤러
 * 음성 통화 관련 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/call")
public class CallController {

    private final CallService callService;

    public CallController(CallService callService) {
        this.callService = callService;
    }

    /**
     * 1단계: 텍스트 기반 통화 엔드포인트
     * 프론트에서 Whisper 결과(문자열)를 transcript로 보내면,
     * 음성용으로 짧게 말하는 스타일의 답변 텍스트를 돌려준다.
     * 이 텍스트는 프론트나 다른 서비스에서 TTS로 변환해서 사용한다.
     * 
     * @param request Whisper로 변환된 발화 텍스트
     * @return AI 여자친구의 답변 텍스트
     */
    @PostMapping("/text")
    public CallTextResponseDto callByText(@Valid @RequestBody CallTextRequestDto request) {
        return callService.replyToTranscript(request);
    }

    // TODO: 2단계 - MultipartFile로 음성 파일을 받아서
    // Groq Whisper → transcript 추출
    // generateReply("call", transcript) 호출
    // TTS 호출 후 음성 파일/스트림 반환
    // /api/call/audio 엔드포인트 추가
}
