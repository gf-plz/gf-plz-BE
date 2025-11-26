package com.aigf.gf_plz.domain.character.repository;

import com.aigf.gf_plz.domain.character.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 상태 리포지토리
 */
public interface StatusRepository extends JpaRepository<Status, Long> {
}






