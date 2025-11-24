package com.aigf.gf_plz.domain.chat.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 채팅 요청 DTO
 * 사용자가 입력한 텍스트 메시지를 담습니다.
 */
public record ChatRequestDto(
        @NotBlank(message = "메시지 내용은 필수입니다.")
        String content
) {}
