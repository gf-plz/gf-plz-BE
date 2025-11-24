package com.aigf.gf_plz.global.groq.controller;

import com.aigf.gf_plz.global.groq.GroqClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Groq API 키 테스트용 컨트롤러
 * 개발/테스트 환경에서만 사용하세요.
 * 
 * 실제 운영 환경에서는 이 컨트롤러를 비활성화하거나 삭제하는 것을 권장합니다.
 */
@RestController
@RequestMapping("/api/test/groq")
public class GroqTestController {

    private final GroqClient groqClient;

    public GroqTestController(GroqClient groqClient) {
        this.groqClient = groqClient;
    }

    /**
     * Groq API 키가 정상적으로 작동하는지 간단히 테스트
     * 
     * @return 테스트 결과 및 응답 메시지
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testGroqApiKey() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String testMessage = "안녕하세요!";
            String reply = groqClient.generateReply("chat", testMessage);
            
            response.put("success", true);
            response.put("message", "Groq API 키가 정상적으로 작동합니다!");
            response.put("testInput", testMessage);
            response.put("groqReply", reply);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Groq API 호출 실패");
            response.put("error", e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 채팅 모드 테스트
     * 
     * @param message 테스트할 메시지 (기본값: "안녕하세요!")
     * @return Groq 응답
     */
    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> testChatMode(
            @RequestParam(defaultValue = "안녕하세요!") String message
    ) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String reply = groqClient.generateReply("chat", message);
            
            response.put("success", true);
            response.put("mode", "chat");
            response.put("input", message);
            response.put("reply", reply);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 통화 모드 테스트
     * 
     * @param message 테스트할 메시지 (기본값: "안녕")
     * @return Groq 응답
     */
    @PostMapping("/call")
    public ResponseEntity<Map<String, Object>> testCallMode(
            @RequestParam(defaultValue = "안녕") String message
    ) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String reply = groqClient.generateReply("call", message);
            
            response.put("success", true);
            response.put("mode", "call");
            response.put("input", message);
            response.put("reply", reply);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }
}

