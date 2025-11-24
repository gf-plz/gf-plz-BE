package com.aigf.gf_plz.global.groq;

import com.aigf.gf_plz.global.groq.exception.GroqApiException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Groq API 클라이언트 통합 테스트
 * 실제 Groq API를 호출하여 API 키가 정상적으로 작동하는지 확인합니다.
 * 
 * 주의: 이 테스트는 실제 API를 호출하므로 API 키가 필요하고 비용이 발생할 수 있습니다.
 */
@SpringBootTest
@ActiveProfiles("test")
class GroqClientTest {

    @Autowired
    private GroqClient groqClient;

    @Test
    @DisplayName("Groq API 키가 정상적으로 작동하는지 테스트 (채팅 모드)")
    void testGroqApiKey_ChatMode() {
        // given
        String userText = "안녕하세요!";
        String mode = "chat";

        // when
        String reply = groqClient.generateReply(mode, userText);

        // then
        assertThat(reply).isNotNull();
        assertThat(reply).isNotBlank();
        System.out.println("채팅 모드 응답: " + reply);
    }

    @Test
    @DisplayName("Groq API 키가 정상적으로 작동하는지 테스트 (통화 모드)")
    void testGroqApiKey_CallMode() {
        // given
        String userText = "안녕";
        String mode = "call";

        // when
        String reply = groqClient.generateReply(mode, userText);

        // then
        assertThat(reply).isNotNull();
        assertThat(reply).isNotBlank();
        System.out.println("통화 모드 응답: " + reply);
    }

    @Test
    @DisplayName("빈 텍스트 입력 시 예외 발생")
    void testEmptyText_ThrowsException() {
        // given
        String userText = "";
        String mode = "chat";

        // when & then
        assertThatThrownBy(() -> groqClient.generateReply(mode, userText))
                .isInstanceOf(GroqApiException.class)
                .hasMessageContaining("사용자 입력 텍스트가 비어있습니다.");
    }

    @Test
    @DisplayName("null 텍스트 입력 시 예외 발생")
    void testNullText_ThrowsException() {
        // given
        String userText = null;
        String mode = "chat";

        // when & then
        assertThatThrownBy(() -> groqClient.generateReply(mode, userText))
                .isInstanceOf(GroqApiException.class)
                .hasMessageContaining("사용자 입력 텍스트가 비어있습니다.");
    }
}

