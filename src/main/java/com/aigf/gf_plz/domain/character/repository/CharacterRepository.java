package com.aigf.gf_plz.domain.character.repository;

import com.aigf.gf_plz.domain.character.entity.Character;
import com.aigf.gf_plz.domain.character.entity.Gender;
import com.aigf.gf_plz.domain.character.entity.Relation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 캐릭터 리포지토리
 */
public interface CharacterRepository extends JpaRepository<Character, Long> {
    /**
     * relation으로 캐릭터 목록을 조회합니다.
     * 
     * @param relation 관계 상태
     * @return 캐릭터 목록
     */
    List<Character> findByRelation(Relation relation);
    
    /**
     * gender로 캐릭터 목록을 조회합니다.
     * 
     * @param gender 성별
     * @return 캐릭터 목록
     */
    List<Character> findByGender(Gender gender);
    
    /**
     * relation과 gender로 캐릭터 목록을 조회합니다.
     * 
     * @param relation 관계 상태
     * @param gender 성별
     * @return 캐릭터 목록
     */
    List<Character> findByRelationAndGender(Relation relation, Gender gender);
}