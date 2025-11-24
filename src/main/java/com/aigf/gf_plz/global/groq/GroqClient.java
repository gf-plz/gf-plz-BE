package com.aigf.gf_plz.global.groq;

/**
 * Groq API 클라이언트 인터페이스
 */
public interface GroqClient {

    /**
     * Groq API를 호출하여 답변을 생성합니다.
     * 
     * @param mode "chat" 또는 "call" 모드
     * @param userText 사용자의 최종 입력 텍스트 (채팅 입력, Whisper 결과 등)
     * @return Groq가 생성한 답변 텍스트 (캐릭터 프롬프트를 반영한 대답)
     * @throws GroqApiException Groq API 호출 실패 시
     */
    String generateReply(String mode, String userText);
}

