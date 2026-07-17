package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.VayChiTiet;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VayChiTietRepository extends JpaRepository<VayChiTiet, Integer> {
    String LOCK_TIMEOUT_MS = "5000";

    List<VayChiTiet> findByVayId(Integer vayId);

    List<VayChiTiet> findByVayIdIn(List<Integer> vayIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = LOCK_TIMEOUT_MS))
    @Query("SELECT vct FROM VayChiTiet vct WHERE vct.id = :id")
    Optional<VayChiTiet> findByIdForUpdate(@Param("id") Integer id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = LOCK_TIMEOUT_MS))
    @Query("SELECT vct FROM VayChiTiet vct WHERE vct.vay.id = :vayId")
    List<VayChiTiet> findByVayIdForUpdate(@Param("vayId") Integer vayId);

    List<VayChiTiet> findByMauSacId(Integer mauSacId);

    List<VayChiTiet> findByKichThuocId(Integer kichThuocId);

    List<VayChiTiet> findByTrangThai(Byte trangThai);

    @Query("""
            SELECT v.id, v.vay.id, v.vay.tenVay, v.vay.maVay,
                   v.mauSac.tenMauSac, v.kichThuoc.tenKichThuoc, COALESCE(v.soLuong, 0)
            FROM VayChiTiet v
            WHERE v.trangThai = 1 AND v.vay.trangThai = 1 AND COALESCE(v.soLuong, 0) <= 5
            ORDER BY COALESCE(v.soLuong, 0), v.id
            """)
    List<Object[]> findLowStockSummary(Pageable pageable);

    Optional<VayChiTiet> findByMaVayChiTiet(String maVayChiTiet);

    Optional<VayChiTiet> findByVayIdAndMauSacIdAndKichThuocId(
            Integer vayId,
            Integer mauSacId,
            Integer kichThuocId
    );

    @Query("""
            SELECT COUNT(v) FROM VayChiTiet v
            WHERE v.trangThai = 1 AND v.vay.trangThai = 1
            """)
    long countActiveVariants();

    @Query("""
            SELECT COALESCE(SUM(v.soLuong), 0) FROM VayChiTiet v
            WHERE v.trangThai = 1 AND v.vay.trangThai = 1
            """)
    Long sumActiveStock();

    @Query("""
            SELECT COUNT(DISTINCT v.mauSac.id) FROM VayChiTiet v
            WHERE v.trangThai = 1 AND v.vay.trangThai = 1 AND v.mauSac IS NOT NULL
            """)
    long countActiveColors();

    @Query("""
            SELECT COUNT(DISTINCT v.kichThuoc.id) FROM VayChiTiet v
            WHERE v.trangThai = 1 AND v.vay.trangThai = 1 AND v.kichThuoc IS NOT NULL
            """)
    long countActiveSizes();
}
