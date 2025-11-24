package com.aigf.gf_plz.domain.message.repository;

import com.aigf.gf_plz.domain.message.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 메시지 리포지토리
 */
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * 세션 ID로 모든 메시지를 시간순으로 조회합니다.
     */
    @Query("SELECT m FROM Message m WHERE m.session.sessionId = :sessionId ORDER BY m.createdAt ASC")
    List<Message> findBySessionIdOrderByCreatedAtAsc(@Param("sessionId") Long sessionId);

    /**
     * 세션 ID로 최근 30개 메시지를 역순으로 조회합니다 (Groq 전달용).
     */
    @Query("SELECT m FROM Message m WHERE m.session.sessionId = :sessionId ORDER BY m.createdAt DESC LIMIT 30")
    List<Message> findTop30BySessionIdOrderByCreatedAtDesc(@Param("sessionId") Long sessionId);
}

