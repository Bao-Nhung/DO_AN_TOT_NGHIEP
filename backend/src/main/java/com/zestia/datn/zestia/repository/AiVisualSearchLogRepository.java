package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.AiVisualSearchLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiVisualSearchLogRepository extends JpaRepository<AiVisualSearchLog, Integer> {
    List<AiVisualSearchLog> findTop20ByOrderByIdDesc();
}
