package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.SupportMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface SupportMessageRepository extends JpaRepository<SupportMessage, Integer> {
    List<SupportMessage> findByConversationIdOrderByIdAsc(Integer conversationId);

    @Query("""
            SELECT message FROM SupportMessage message
            WHERE message.id IN (
                SELECT MAX(latest.id) FROM SupportMessage latest
                WHERE latest.conversation.id IN :conversationIds
                GROUP BY latest.conversation.id
            )
            """)
    List<SupportMessage> findLatestByConversationIds(@Param("conversationIds") Collection<Integer> conversationIds);
}
