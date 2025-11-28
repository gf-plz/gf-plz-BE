package com.aigf.gf_plz.domain.history.entity;

import com.aigf.gf_plz.domain.character.entity.Character;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 관계 결과 히스토리를 저장하기 위한 엔티티
 */
@Entity
@Table(name = "relationship_history")
public class RelationshipHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_entry_id")
    private Long historyEntryId;

    @Column(name = "history_id", nullable = false)
    private Long historyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id", nullable = false)
    private Character character;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    protected RelationshipHistory() {
    }

    public RelationshipHistory(Long historyId, Character character, LocalDateTime recordedAt) {
        this.historyId = historyId;
        this.character = character;
        this.recordedAt = recordedAt;
    }

    public Long getHistoryId() {
        return historyId;
    }

    public Character getCharacter() {
        return character;
    }
}
