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

    @Test
    @DisplayName("성별로 캐릭터를 필터링할 수 있어야 한다")
    void getCharactersByGenderTest() {
        // given
        CharacterCreateRequestDto femaleRequest = new CharacterCreateRequestDto(
                Mbti.ENFJ, AttachmentType.안정형, 80, Gender.FEMALE, "지은",
                "설명", "url", VoiceType.TYPE1
        );
        CharacterCreateRequestDto maleRequest = new CharacterCreateRequestDto(
                Mbti.ENTP, AttachmentType.회피형, 60, Gender.MALE, "민수",
                "설명2", "url2", VoiceType.TYPE2
        );
        
        characterService.createCharacter(femaleRequest);
        characterService.createCharacter(maleRequest);

        // when
        var femaleCharacters = characterService.getCharacters(null, Gender.FEMALE);
        var maleCharacters = characterService.getCharacters(null, Gender.MALE);

        // then
        assertThat(femaleCharacters).hasSize(1);
        assertThat(femaleCharacters.get(0).gender()).isEqualTo(Gender.FEMALE);
        assertThat(femaleCharacters.get(0).name()).isEqualTo("지은");
        
        assertThat(maleCharacters).hasSize(1);
        assertThat(maleCharacters.get(0).gender()).isEqualTo(Gender.MALE);
        assertThat(maleCharacters.get(0).name()).isEqualTo("민수");
    }

    @Test
    @DisplayName("관계 상태와 성별을 함께 필터링할 수 있어야 한다")
    void getCharactersByRelationAndGenderTest() {
        // given
        CharacterCreateRequestDto femaleRequest = new CharacterCreateRequestDto(
                Mbti.ENFJ, AttachmentType.안정형, 80, Gender.FEMALE, "지은",
                "설명", "url", VoiceType.TYPE1
        );
        CharacterCreateRequestDto maleRequest = new CharacterCreateRequestDto(
                Mbti.ENTP, AttachmentType.회피형, 60, Gender.MALE, "민수",
                "설명2", "url2", VoiceType.TYPE2
        );
        
        CharacterResponseDto femaleCharacter = characterService.createCharacter(femaleRequest);
        CharacterResponseDto maleCharacter = characterService.createCharacter(maleRequest);
        
        // 여자 캐릭터만 선택 (관계가 now로 변경됨)
        characterService.selectCharacter(femaleCharacter.characterId());

        // when
        var nowFemaleCharacters = characterService.getCharacters(Relation.now, Gender.FEMALE);
        var nowMaleCharacters = characterService.getCharacters(Relation.now, Gender.MALE);
        var yetMaleCharacters = characterService.getCharacters(Relation.yet, Gender.MALE);

        // then
        assertThat(nowFemaleCharacters).hasSize(1);
        assertThat(nowFemaleCharacters.get(0).gender()).isEqualTo(Gender.FEMALE);
        assertThat(nowFemaleCharacters.get(0).status().relation()).isEqualTo(Relation.now);
        
        assertThat(nowMaleCharacters).isEmpty();
        
        assertThat(yetMaleCharacters).hasSize(1);
        assertThat(yetMaleCharacters.get(0).gender()).isEqualTo(Gender.MALE);
        assertThat(yetMaleCharacters.get(0).status().relation()).isEqualTo(Relation.yet);
    }

    @Test
    @DisplayName("필터 없이 전체 캐릭터를 조회할 수 있어야 한다")
    void getCharactersWithoutFilterTest() {
        // given
        CharacterCreateRequestDto femaleRequest = new CharacterCreateRequestDto(
                Mbti.ENFJ, AttachmentType.안정형, 80, Gender.FEMALE, "지은",
                "설명", "url", VoiceType.TYPE1
        );
        CharacterCreateRequestDto maleRequest = new CharacterCreateRequestDto(
                Mbti.ENTP, AttachmentType.회피형, 60, Gender.MALE, "민수",
                "설명2", "url2", VoiceType.TYPE2
        );
        
        characterService.createCharacter(femaleRequest);
        characterService.createCharacter(maleRequest);

        // when
        var allCharacters = characterService.getCharacters(null, null);

        // then
        assertThat(allCharacters).hasSizeGreaterThanOrEqualTo(2);
        assertThat(allCharacters).extracting(CharacterResponseDto::gender)
                .contains(Gender.FEMALE, Gender.MALE);
    }
}

