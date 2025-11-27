package com.aigf.gf_plz.domain.character.dto;

import com.aigf.gf_plz.domain.character.entity.Relation;

import java.time.LocalDateTime;

/**
 * 상태 응답 DTO
 */
public record StatusResponseDto(
        Long statusId,
        Relation relation,
        LocalDateTime startDay,
        LocalDateTime endDay,
        Integer like
) {}




