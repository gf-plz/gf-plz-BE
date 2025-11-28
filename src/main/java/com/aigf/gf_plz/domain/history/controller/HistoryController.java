package com.aigf.gf_plz.domain.history.controller;

import com.aigf.gf_plz.domain.character.dto.CharacterResponseDto;
import com.aigf.gf_plz.domain.history.service.HistoryService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 관계 히스토리를 조회하는 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/history")
public class HistoryController {

    private final HistoryService historyService;

    public HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping(value = "/{historyId}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public List<CharacterResponseDto> getHistory(@PathVariable Long historyId) {
        return historyService.getHistory(historyId);
    }
}
