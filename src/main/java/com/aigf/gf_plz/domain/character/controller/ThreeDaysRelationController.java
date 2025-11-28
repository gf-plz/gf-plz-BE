package com.aigf.gf_plz.domain.character.controller;

import com.aigf.gf_plz.domain.character.dto.ThreeDaysRelationRequestDto;
import com.aigf.gf_plz.domain.character.dto.ThreeDaysRelationResponseDto;
import com.aigf.gf_plz.domain.character.service.ThreeDaysRelationService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 3일 결과와 관련된 관계 업데이트 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/relations")
public class ThreeDaysRelationController {

    private final ThreeDaysRelationService threeDaysRelationService;

    public ThreeDaysRelationController(ThreeDaysRelationService threeDaysRelationService) {
        this.threeDaysRelationService = threeDaysRelationService;
    }

    @PostMapping(value = "/three-days", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public ThreeDaysRelationResponseDto createThreeDayRelation(@Valid @RequestBody ThreeDaysRelationRequestDto request) {
        return threeDaysRelationService.updateRelation(request);
    }
}

