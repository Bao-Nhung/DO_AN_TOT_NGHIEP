package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.PhanThuongVongQuay;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PhanThuongVongQuayRepository extends JpaRepository<PhanThuongVongQuay, Integer> {
    List<PhanThuongVongQuay> findByChienDichIdOrderByThuTuAscIdAsc(Integer campaignId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT prize FROM PhanThuongVongQuay prize WHERE prize.id = :id")
    Optional<PhanThuongVongQuay> findByIdForUpdate(@Param("id") Integer id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT prize FROM PhanThuongVongQuay prize
            WHERE prize.chienDich.id = :campaignId
              AND prize.trangThai = 1
            ORDER BY prize.thuTu ASC, prize.id ASC
            """)
    List<PhanThuongVongQuay> findActiveForUpdate(@Param("campaignId") Integer campaignId);
}
