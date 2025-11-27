package com.aigf.gf_plz.domain.character.repository;

import com.aigf.gf_plz.domain.character.entity.Character;
import com.aigf.gf_plz.domain.character.entity.Relation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}