package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.SanPhamChiTiet;
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

public interface SanPhamChiTietRepository extends JpaRepository<SanPhamChiTiet, Integer> {
    String LOCK_TIMEOUT_MS = "5000";

    List<SanPhamChiTiet> findBySanPhamId(Integer sanPhamId);

    List<SanPhamChiTiet> findBySanPhamIdIn(List<Integer> sanPhamIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = LOCK_TIMEOUT_MS))
    @Query("SELECT vct FROM SanPhamChiTiet vct WHERE vct.id = :id")
    Optional<SanPhamChiTiet> findByIdForUpdate(@Param("id") Integer id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = LOCK_TIMEOUT_MS))
    @Query("SELECT vct FROM SanPhamChiTiet vct WHERE vct.sanPham.id = :sanPhamId")
    List<SanPhamChiTiet> findBySanPhamIdForUpdate(@Param("sanPhamId") Integer sanPhamId);

    List<SanPhamChiTiet> findByMauSacId(Integer mauSacId);

    List<SanPhamChiTiet> findByKichThuocId(Integer kichThuocId);

    List<SanPhamChiTiet> findByTrangThai(Byte trangThai);

    @Query("""
            SELECT v.id, v.sanPham.id, v.sanPham.tenSanPham, v.sanPham.maSanPham,
                   v.mauSac.tenMauSac, v.kichThuoc.tenKichThuoc, COALESCE(v.soLuong, 0),
                   v.anhUrl
            FROM SanPhamChiTiet v
            WHERE v.trangThai = 1 AND v.sanPham.trangThai = 1 AND COALESCE(v.soLuong, 0) <= 5
            ORDER BY COALESCE(v.soLuong, 0), v.id
            """)
    List<Object[]> findLowStockSummary(Pageable pageable);

    @Query("""
            SELECT COUNT(v) FROM SanPhamChiTiet v
            WHERE v.trangThai = 1
              AND v.sanPham.trangThai = 1
              AND COALESCE(v.soLuong, 0) <= 5
            """)
    long countLowStockVariants();

    Optional<SanPhamChiTiet> findByMaSanPhamChiTiet(String maSanPhamChiTiet);

    Optional<SanPhamChiTiet> findBySanPhamIdAndMauSacIdAndKichThuocId(
            Integer sanPhamId,
            Integer mauSacId,
            Integer kichThuocId
    );

    @Query("""
            SELECT COUNT(v) FROM SanPhamChiTiet v
            WHERE v.trangThai = 1 AND v.sanPham.trangThai = 1
            """)
    long countActiveVariants();

    @Query("""
            SELECT COALESCE(SUM(v.soLuong), 0) FROM SanPhamChiTiet v
            WHERE v.trangThai = 1 AND v.sanPham.trangThai = 1
            """)
    Long sumActiveStock();

    @Query("""
            SELECT COUNT(DISTINCT v.mauSac.id) FROM SanPhamChiTiet v
            WHERE v.trangThai = 1 AND v.sanPham.trangThai = 1 AND v.mauSac IS NOT NULL
            """)
    long countActiveColors();

    @Query("""
            SELECT COUNT(DISTINCT v.kichThuoc.id) FROM SanPhamChiTiet v
            WHERE v.trangThai = 1 AND v.sanPham.trangThai = 1 AND v.kichThuoc IS NOT NULL
            """)
    long countActiveSizes();
}
