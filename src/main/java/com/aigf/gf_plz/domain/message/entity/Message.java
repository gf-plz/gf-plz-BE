package com.aigf.gf_plz.domain.message.entity;

import com.aigf.gf_plz.domain.session.entity.Session;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 메시지 엔티티
 * 채팅과 통화의 모든 메시지를 저장합니다.
 */
@Entity
@Table(name = "Message")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "메시지ID")
    private Long messageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "세션ID", nullable = false)
    private Session session;

    @Enumerated(EnumType.STRING)
    @Column(name = "보낸 이", nullable = false)
    private SenderRole senderRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "메시지 타입", nullable = false)
    private MessageType messageType;

    @Column(name = "대화 내용", columnDefinition = "TEXT CHARACTER SET UTF8", nullable = false)
    private String textContent;

    @CreationTimestamp
    @Column(name = "생성 시간", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Message(
            Session session,
            SenderRole senderRole,
            MessageType messageType,
            String textContent
    ) {
        this.session = session;
        this.senderRole = senderRole;
        this.messageType = messageType;
        this.textContent = textContent;
    }
}














