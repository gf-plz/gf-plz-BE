package com.aigf.gf_plz.domain.history.service;

import com.aigf.gf_plz.domain.character.dto.CharacterResponseDto;

import java.util.List;

/**
 * 히스토리 관련 API를 제공하는 서비스 인터페이스
 */
public interface HistoryService {
    List<CharacterResponseDto> getHistory(Long historyId);
}
