package com.aigf.gf_plz.domain.call.service;

import com.aigf.gf_plz.domain.call.dto.CallTextRequestDto;
import com.aigf.gf_plz.domain.call.dto.CallTextResponseDto;

/**
 * 통화 서비스 인터페이스
 */
public interface CallService {
    /**
     * Whisper로 변환된 발화 텍스트에 대해 AI 여자친구의 답변을 생성합니다.
     * 
     * @param request Whisper로 변환된 발화 텍스트
     * @return AI 여자친구의 답변 텍스트 (TTS로 변환되어 재생됨)
     */
    CallTextResponseDto replyToTranscript(CallTextRequestDto request);
}
