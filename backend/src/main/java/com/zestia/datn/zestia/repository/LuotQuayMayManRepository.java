package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.LuotQuayMayMan;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface LuotQuayMayManRepository extends JpaRepository<LuotQuayMayMan, Integer> {
    @EntityGraph(attributePaths = {"chienDich", "phanThuong"})
    Optional<LuotQuayMayMan> findByChienDichIdAndMaHoaDonIgnoreCase(Integer campaignId, String orderCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"chienDich", "phanThuong"})
    @Query("SELECT spin FROM LuotQuayMayMan spin WHERE spin.id = :id")
    Optional<LuotQuayMayMan> findByIdForUpdate(@Param("id") Integer id);

    boolean existsByMaNhanThuong(String claimCode);

    @EntityGraph(attributePaths = {"chienDich", "phanThuong"})
    @Query("""
            SELECT spin FROM LuotQuayMayMan spin
            WHERE (:campaignId IS NULL OR spin.chienDich.id = :campaignId)
              AND (:status IS NULL OR spin.trangThaiNhan = :status)
            """)
    Page<LuotQuayMayMan> findForAdmin(@Param("campaignId") Integer campaignId,
                                      @Param("status") String status,
                                      Pageable pageable);

    long countByChienDichId(Integer campaignId);
    long countByChienDichIdAndTrungThuongTrue(Integer campaignId);
    long countByChienDichIdAndTrangThaiNhan(Integer campaignId, String status);
    long countByPhanThuongId(Integer prizeId);
}
