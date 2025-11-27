package com.aigf.gf_plz.domain.character.service;

import com.aigf.gf_plz.domain.character.dto.CharacterCreateRequestDto;
import com.aigf.gf_plz.domain.character.dto.CharacterResponseDto;
import com.aigf.gf_plz.domain.character.dto.CharacterSelectResponseDto;
import com.aigf.gf_plz.domain.character.entity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class CharacterServiceTest {

    @Autowired
    private CharacterService characterService;

    @Test
    @DisplayName("캐릭터 생성 시 상태 정보가 함께 초기화되어야 한다")
    void createCharacterTest() {
        // given
        CharacterCreateRequestDto request = new CharacterCreateRequestDto(
                Mbti.ENFJ, AttachmentType.안정형, 80, Gender.FEMALE, "지은",
                "설명", "url", VoiceType.TYPE1
        );

        // when
        CharacterResponseDto response = characterService.createCharacter(request);

        // then
        assertThat(response.name()).isEqualTo("지은");
        assertThat(response.status().relation()).isEqualTo(Relation.yet); // 초기값 확인
        assertThat(response.status().like()).isEqualTo(0);
    }

    @Test
    @DisplayName("캐릭터 선택 시 관계가 NOW로 변경되고 날짜가 설정되어야 한다")
    void selectCharacterTest() {
        // given
        CharacterCreateRequestDto request = new CharacterCreateRequestDto(
                Mbti.ENFJ, AttachmentType.안정형, 80, Gender.FEMALE, "지은",
                "설명", "url", VoiceType.TYPE1
        );
        CharacterResponseDto created = characterService.createCharacter(request);

        // when
        CharacterSelectResponseDto selection = characterService.selectCharacter(created.characterId());

        // then
        assertThat(selection.character().status().relation()).isEqualTo(Relation.now);
        assertThat(selection.character().status().startDay()).isNotNull();
        assertThat(selection.character().status().endDay()).isNotNull();
    }
}

