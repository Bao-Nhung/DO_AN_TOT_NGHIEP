package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.SupportConversation;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SupportConversationRepository extends JpaRepository<SupportConversation, Integer> {

    Optional<SupportConversation> findByPublicToken(String publicToken);

    List<SupportConversation> findByTrangThaiInOrderByNgayCapNhatDesc(Collection<String> statuses);

    long countByTrangThaiIn(Collection<String> statuses);

    long countByNhanVienIdAndTrangThaiIn(Integer employeeId, Collection<String> statuses);

    @Query("""
            SELECT COUNT(c) FROM SupportConversation c
            WHERE c.trangThai IN :statuses
              AND (c.nhanVien IS NULL OR c.nhanVien.id = :employeeId)
            """)
    long countActionableForEmployee(@Param("employeeId") Integer employeeId,
                                    @Param("statuses") Collection<String> statuses);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM SupportConversation c WHERE c.id = :id")
    Optional<SupportConversation> findByIdForUpdate(@Param("id") Integer id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM SupportConversation c WHERE c.publicToken = :token")
    Optional<SupportConversation> findByPublicTokenForUpdate(@Param("token") String token);
}
