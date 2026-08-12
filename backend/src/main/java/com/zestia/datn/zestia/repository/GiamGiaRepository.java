package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.GiamGia;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.List;
import java.time.LocalDate;

public interface GiamGiaRepository extends JpaRepository<GiamGia, Integer> {
    String LOCK_TIMEOUT_MS = "5000";

    Optional<GiamGia> findByMaGiamGiaIgnoreCase(String maGiamGia);
    boolean existsByMaGiamGiaIgnoreCase(String maGiamGia);

    @Query("""
            SELECT voucher FROM GiamGia voucher
            WHERE voucher.trangThai = 1
              AND (voucher.soLuong IS NULL OR voucher.soLuong > 0)
              AND (voucher.ngayBatDau IS NULL OR voucher.ngayBatDau <= :today)
              AND (voucher.ngayKetThuc IS NULL OR voucher.ngayKetThuc >= :today)
              AND ((voucher.phanTramGiam IS NOT NULL AND voucher.phanTramGiam > 0)
                   OR (voucher.gioTriGiam IS NOT NULL AND voucher.gioTriGiam > 0))
            ORDER BY voucher.ngayKetThuc ASC, voucher.id ASC
            """)
    List<GiamGia> findPubliclyUsable(@Param("today") LocalDate today);

    @Query("""
            SELECT voucher FROM GiamGia voucher
            WHERE voucher.trangThai = 1
              AND (voucher.soLuong IS NULL OR voucher.soLuong > 0)
              AND (voucher.ngayBatDau IS NULL OR voucher.ngayBatDau <= :today)
              AND (voucher.ngayKetThuc IS NULL OR voucher.ngayKetThuc >= :today)
              AND ((voucher.phanTramGiam IS NOT NULL AND voucher.phanTramGiam > 0)
                   OR (voucher.gioTriGiam IS NOT NULL AND voucher.gioTriGiam > 0))
            ORDER BY voucher.ngayKetThuc ASC, voucher.id ASC
            """)
    List<GiamGia> findPubliclyUsableForAi(@Param("today") LocalDate today, Pageable pageable);

    @Query("""
            SELECT voucher FROM GiamGia voucher
            WHERE voucher.trangThai = 1
              AND (voucher.soLuong IS NULL OR voucher.soLuong > 0
                   OR (:reservedVoucherId IS NOT NULL AND voucher.id = :reservedVoucherId))
              AND (voucher.ngayBatDau IS NULL OR voucher.ngayBatDau <= :today)
              AND (voucher.ngayKetThuc IS NULL OR voucher.ngayKetThuc >= :today)
              AND ((voucher.phanTramGiam IS NOT NULL AND voucher.phanTramGiam > 0)
                   OR (voucher.gioTriGiam IS NOT NULL AND voucher.gioTriGiam > 0))
            """)
    List<GiamGia> findPotentiallyUsable(@Param("today") LocalDate today,
                                        @Param("reservedVoucherId") Integer reservedVoucherId);

    List<GiamGia> findAllByOrderByNgayTaoDescIdDesc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = LOCK_TIMEOUT_MS))
    @Query("SELECT gg FROM GiamGia gg WHERE LOWER(gg.maGiamGia) = LOWER(:maGiamGia)")
    Optional<GiamGia> findByMaGiamGiaIgnoreCaseForUpdate(@Param("maGiamGia") String maGiamGia);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = LOCK_TIMEOUT_MS))
    @Query("SELECT gg FROM GiamGia gg WHERE gg.id = :id")
    Optional<GiamGia> findByIdForUpdate(@Param("id") Integer id);
}
