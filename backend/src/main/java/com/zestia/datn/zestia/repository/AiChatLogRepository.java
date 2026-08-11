package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.AiChatLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiChatLogRepository extends JpaRepository<AiChatLog, Integer> {
    List<AiChatLog> findTop20ByOrderByIdDesc();
}
