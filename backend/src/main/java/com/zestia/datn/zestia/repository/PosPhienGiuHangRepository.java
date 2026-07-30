package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.PosPhienGiuHang;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PosPhienGiuHangRepository extends JpaRepository<PosPhienGiuHang, Integer> {
    String LOCK_TIMEOUT_MS = "5000";

    Optional<PosPhienGiuHang> findByMaPhien(String maPhien);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = LOCK_TIMEOUT_MS))
    @Query("SELECT p FROM PosPhienGiuHang p WHERE p.maPhien = :maPhien")
    Optional<PosPhienGiuHang> findByMaPhienForUpdate(@Param("maPhien") String maPhien);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = LOCK_TIMEOUT_MS))
    @Query("SELECT p FROM PosPhienGiuHang p WHERE p.id = :id")
    Optional<PosPhienGiuHang> findByIdForUpdate(@Param("id") Integer id);

    @Query("""
            SELECT p.id
            FROM PosPhienGiuHang p
            WHERE p.trangThai = 'ACTIVE' AND p.hetHanLuc <= :now
            ORDER BY p.hetHanLuc
            """)
    List<Integer> findExpiredIds(@Param("now") LocalDateTime now, Pageable pageable);
}
