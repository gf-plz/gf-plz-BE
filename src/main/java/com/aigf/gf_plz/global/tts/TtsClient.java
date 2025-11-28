package com.aigf.gf_plz.global.tts;

import com.aigf.gf_plz.global.tts.exception.TtsException;

/**
 * TTS(Text-to-Speech) 클라이언트 인터페이스
 * 텍스트를 음성 파일로 변환합니다.
 */
public interface TtsClient {

    /**
     * 텍스트를 음성 파일로 변환합니다.
     *
     * @param text 변환할 텍스트
     * @param voiceType 목소리 타입 (캐릭터별)
     * @return 음성 파일의 바이트 배열 (MP3 형식)
     * @throws TtsException TTS API 호출 실패 시
     */
    byte[] synthesize(String text, String voiceType);
}













