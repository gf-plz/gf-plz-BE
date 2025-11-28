package com.aigf.gf_plz.domain.session.repository;

import com.aigf.gf_plz.domain.session.entity.Session;
import com.aigf.gf_plz.domain.session.entity.SessionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 세션 리포지토리
 */
public interface SessionRepository extends JpaRepository<Session, Long> {

    /**
     * 세션 ID와 활성화 상태로 세션을 조회합니다.
     */
    Optional<Session> findBySessionIdAndIsActiveTrue(Long sessionId);

    /**
     * 캐릭터 ID, 세션 타입, 활성화 상태로 세션을 조회합니다.
     */
    @Query("SELECT s FROM Session s WHERE s.character.characterId = :characterId AND s.sessionType = :sessionType AND s.isActive = true")
    Optional<Session> findByCharacterIdAndSessionTypeAndIsActiveTrue(
            @Param("characterId") Long characterId,
            @Param("sessionType") SessionType sessionType
    );

    /**
     * 캐릭터 ID와 세션 타입으로 최근 세션을 조회합니다 (비활성 포함).
     */
    @Query("SELECT s FROM Session s WHERE s.character.characterId = :characterId AND s.sessionType = :sessionType ORDER BY s.lastMessageAt DESC")
    List<Session> findByCharacterIdAndSessionTypeOrderByLastMessageAtDesc(
            @Param("characterId") Long characterId,
            @Param("sessionType") SessionType sessionType
    );

    /**
     * 최근 활성 세션을 조회합니다 (중복 캐릭터 제거, 최근 대화 시간 기준).
     * 각 캐릭터별로 가장 최근에 대화한 세션만 반환합니다.
     */
    @Query("SELECT s FROM Session s WHERE s.isActive = true AND s.lastMessageAt IS NOT NULL " +
           "ORDER BY s.lastMessageAt DESC")
    List<Session> findRecentActiveSessions();
    
    /**
     * 성별로 필터링된 최근 활성 세션을 조회합니다.
     * 각 캐릭터별로 가장 최근에 대화한 세션만 반환합니다.
     */
    @Query("SELECT s FROM Session s WHERE s.isActive = true AND s.lastMessageAt IS NOT NULL " +
           "AND s.character.gender = :gender ORDER BY s.lastMessageAt DESC")
    List<Session> findRecentActiveSessionsByGender(@Param("gender") com.aigf.gf_plz.domain.character.entity.Gender gender);
}
