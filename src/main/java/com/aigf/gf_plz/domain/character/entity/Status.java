package com.aigf.gf_plz.domain.character.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 캐릭터 상태 정보 엔티티
 * MBTI, 애착유형, 테토력, 성별, 이름을 포함
 */
@Entity
@Table(name = "status")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Status {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "상태ID")
    private Long statusId;

    @Enumerated(EnumType.STRING)
    @Column(name = "MBTI", nullable = false)
    private Mbti mbti;

    @Enumerated(EnumType.STRING)
    @Column(name = "애착타입", nullable = false)
    private AttachmentType attachment;

    @Column(name = "테토력", nullable = false)
    private Integer teto; // 0~100

    @Enumerated(EnumType.STRING)
    @Column(name = "성별", nullable = false)
    private Gender gender;

    @Column(name = "이름", nullable = false, length = 50)
    private String name;

    @Builder
    public Status(Mbti mbti, AttachmentType attachment, Integer teto, Gender gender, String name) {
        this.mbti = mbti;
        this.attachment = attachment;
        this.teto = teto;
        this.gender = gender;
        this.name = name;
    }
}

