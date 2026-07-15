package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.SupportMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupportMessageRepository extends JpaRepository<SupportMessage, Integer> {
    List<SupportMessage> findByConversationIdOrderByIdAsc(Integer conversationId);
}
