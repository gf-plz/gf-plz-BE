package com.aigf.gf_plz.domain.call.controller;

import com.aigf.gf_plz.domain.call.dto.CallAudioRequestDto;
import com.aigf.gf_plz.domain.call.dto.CallAudioResponseDto;
import com.aigf.gf_plz.domain.call.service.CallService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
     * 음성 파일 기반 통화 엔드포인트
     * 프론트에서 음성 파일을 보내면,
     * Whisper(STT) → AI 답변 생성 → TTS 변환을 수행하여
     * 음성 응답을 반환합니다.
     *
     * @param audioFile 사용자의 음성 파일 (MP3, WAV, M4A 등)
     * @param characterId 캐릭터 ID
     * @param sessionId 세션 ID (선택사항)
     * @return AI 여자친구의 음성 응답 (MP3 형식)
     */
    @PostMapping("/audio")
    public ResponseEntity<byte[]> callByAudio(
            @RequestParam("audio") MultipartFile audioFile,
            @RequestParam("characterId") Long characterId,
            @RequestParam(value = "sessionId", required = false) Long sessionId
    ) {
        CallAudioRequestDto request = new CallAudioRequestDto(
                characterId,
                sessionId != null ? java.util.Optional.of(sessionId) : java.util.Optional.empty()
        );

        CallAudioResponseDto response = callService.replyToAudio(audioFile, request);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("audio/mpeg"));
        headers.setContentLength(response.audioData().length);
        headers.setContentDispositionFormData("attachment", "response.mp3");

        return ResponseEntity.ok()
                .headers(headers)
                .body(response.audioData());
    }
}
