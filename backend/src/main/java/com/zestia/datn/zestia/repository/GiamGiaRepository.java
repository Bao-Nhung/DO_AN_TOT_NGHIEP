package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.GiamGia;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface GiamGiaRepository extends JpaRepository<GiamGia, Integer> {
    String LOCK_TIMEOUT_MS = "5000";

    Optional<GiamGia> findByMaGiamGiaIgnoreCase(String maGiamGia);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = LOCK_TIMEOUT_MS))
    @Query("SELECT gg FROM GiamGia gg WHERE LOWER(gg.maGiamGia) = LOWER(:maGiamGia)")
    Optional<GiamGia> findByMaGiamGiaIgnoreCaseForUpdate(@Param("maGiamGia") String maGiamGia);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = LOCK_TIMEOUT_MS))
    @Query("SELECT gg FROM GiamGia gg WHERE gg.id = :id")
    Optional<GiamGia> findByIdForUpdate(@Param("id") Integer id);
}
