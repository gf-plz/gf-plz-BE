package com.aigf.gf_plz.domain.session.entity;

import com.aigf.gf_plz.domain.character.entity.Character;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 세션 엔티티
 * 채팅과 통화 세션을 통합 관리합니다.
 */
@Entity
@Table(name = "Session")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "세션ID")
    private Long sessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "캐릭터ID", nullable = false)
    private Character character;

    @Enumerated(EnumType.STRING)
    @Column(name = "대화 타입", nullable = false)
    private SessionType sessionType;

    @Column(name = "활성화", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "생성 날짜", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "마지막 대화")
    private LocalDateTime lastMessageAt;

    @Builder
    public Session(Character character, SessionType sessionType) {
        this.character = character;
        this.sessionType = sessionType;
        this.isActive = true;
    }

    /**
     * 마지막 메시지 시간을 업데이트합니다.
     */
    public void updateLastMessageAt(LocalDateTime time) {
        this.lastMessageAt = time;
    }

    /**
     * 세션을 비활성화합니다.
     */
    public void deactivate() {
        this.isActive = false;
    }
}



