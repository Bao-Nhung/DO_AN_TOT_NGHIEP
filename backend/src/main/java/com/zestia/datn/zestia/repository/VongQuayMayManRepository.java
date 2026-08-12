package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.VongQuayMayMan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface VongQuayMayManRepository extends JpaRepository<VongQuayMayMan, Integer> {
    boolean existsByMaChienDichIgnoreCase(String code);
    boolean existsByMaChienDichIgnoreCaseAndIdNot(String code, Integer id);

    @Query("""
            SELECT campaign FROM VongQuayMayMan campaign
            WHERE campaign.trangThai = 1
              AND campaign.ngayBatDau <= :now
              AND campaign.ngayKetThuc >= :now
            ORDER BY campaign.ngayBatDau DESC, campaign.id DESC
            """)
    List<VongQuayMayMan> findActiveAt(@Param("now") LocalDateTime now);

    @Query("""
            SELECT COUNT(campaign) FROM VongQuayMayMan campaign
            WHERE campaign.trangThai = 1
              AND (:excludedId IS NULL OR campaign.id <> :excludedId)
              AND campaign.ngayBatDau < :endsAt
              AND campaign.ngayKetThuc > :startsAt
            """)
    long countActiveOverlaps(@Param("excludedId") Integer excludedId,
                             @Param("startsAt") LocalDateTime startsAt,
                             @Param("endsAt") LocalDateTime endsAt);

    @Query("SELECT campaign FROM VongQuayMayMan campaign ORDER BY campaign.ngayBatDau DESC, campaign.id DESC")
    List<VongQuayMayMan> findAllLatest();
}
