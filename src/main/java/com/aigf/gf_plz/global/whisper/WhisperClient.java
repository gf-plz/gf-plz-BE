package com.aigf.gf_plz.global.whisper;

import com.aigf.gf_plz.global.whisper.exception.WhisperException;
import org.springframework.web.multipart.MultipartFile;

/**
 * Whisper(STT) 클라이언트 인터페이스
 * 음성 파일을 텍스트로 변환합니다.
 */
public interface WhisperClient {

    /**
     * 음성 파일을 텍스트로 변환합니다.
     *
     * @param audioFile 음성 파일 (MP3, WAV, M4A 등)
     * @return 변환된 텍스트 (transcript)
     * @throws WhisperException Whisper API 호출 실패 시
     */
    String transcribe(MultipartFile audioFile);
}










