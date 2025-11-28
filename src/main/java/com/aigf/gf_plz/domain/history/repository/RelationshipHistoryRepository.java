package com.aigf.gf_plz.domain.history.repository;

import com.aigf.gf_plz.domain.history.entity.RelationshipHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RelationshipHistoryRepository extends JpaRepository<RelationshipHistory, Long> {
    List<RelationshipHistory> findByHistoryId(Long historyId);
}
