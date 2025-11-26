package com.aigf.gf_plz.domain.character.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 캐릭터 상태 정보 엔티티
 * ERD에 따른 관계 상태, 만난 날짜, 헤어지는 날짜, 애정도를 포함
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
    @Column(name = "관계", nullable = false)
    private Relation relation;

    @Column(name = "만난 날짜")
    private LocalDateTime startDay;

    @Column(name = "헤어지는 날짜")
    private LocalDateTime endDay;

    @Column(name = "애정도")
    private Integer like; // 애정도 점수

    @Builder
    public Status(Relation relation, LocalDateTime startDay, LocalDateTime endDay, Integer like) {
        this.relation = relation;
        this.startDay = startDay;
        this.endDay = endDay;
        this.like = like;
    }

    /**
     * 애정도를 업데이트합니다.
     */
    public void updateLike(Integer newLike) {
        this.like = newLike;
    }

    /**
     * 관계 상태를 업데이트합니다.
     */
    public void updateRelation(Relation relation) {
        this.relation = relation;
    }

    /**
     * 만난 날짜를 업데이트합니다.
     */
    public void updateStartDay(LocalDateTime startDay) {
        this.startDay = startDay;
    }

    /**
     * 헤어지는 날짜를 업데이트합니다.
     */
    public void updateEndDay(LocalDateTime endDay) {
        this.endDay = endDay;
    }

    /**
     * 만난 날짜와 헤어지는 날짜를 함께 업데이트합니다.
     */
    public void updateDates(LocalDateTime startDay, LocalDateTime endDay) {
        this.startDay = startDay;
        this.endDay = endDay;
    }
}

