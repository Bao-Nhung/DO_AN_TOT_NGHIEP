package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.HoaDon;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
// 2 DÒNG IMPORT NÀY LÀ ĐỂ SỬA LỖI BẠN VỪA GẶP:
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

public interface HoaDonRepository extends JpaRepository<HoaDon, Integer> {

    @EntityGraph(attributePaths = {"khachHang", "nhanVien", "giamGia"})
    @Query("SELECT h FROM HoaDon h")
    List<HoaDon> findAllForSummary(Sort sort);

    @EntityGraph(attributePaths = {"khachHang", "nhanVien", "giamGia"})
    @Query(value = """
            SELECT h FROM HoaDon h
            LEFT JOIN h.khachHang kh
            WHERE (:keyword IS NULL
                   OR LOWER(h.maHoaDon) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(h.tenKhachHang) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(kh.hoVaTen) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:status IS NULL OR h.trangThai = :status)
              AND (:orderType IS NULL OR h.hinhThucNhanHang = :orderType)
            """,
            countQuery = """
            SELECT COUNT(h) FROM HoaDon h
            LEFT JOIN h.khachHang kh
            WHERE (:keyword IS NULL
                   OR LOWER(h.maHoaDon) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(h.tenKhachHang) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(kh.hoVaTen) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:status IS NULL OR h.trangThai = :status)
              AND (:orderType IS NULL OR h.hinhThucNhanHang = :orderType)
            """)
    Page<HoaDon> findAdminPage(@Param("keyword") String keyword,
                               @Param("status") Byte status,
                               @Param("orderType") Byte orderType,
                               Pageable pageable);

    @Query("""
            SELECT h.trangThai, COUNT(h) FROM HoaDon h
            LEFT JOIN h.khachHang kh
            WHERE (:keyword IS NULL
                   OR LOWER(h.maHoaDon) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(h.tenKhachHang) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(kh.hoVaTen) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:orderType IS NULL OR h.hinhThucNhanHang = :orderType)
            GROUP BY h.trangThai
            """)
    List<Object[]> countAdminOrdersByStatus(@Param("keyword") String keyword,
                                            @Param("orderType") Byte orderType);
    
    // Các method cũ của hệ thống
    List<HoaDon> findByKhachHangId(Integer khachHangId);
    List<HoaDon> findByNhanVienId(Integer nhanVienId);

    @Modifying
    @Query("""
            UPDATE HoaDon h SET h.khachHang = :customer
            WHERE h.khachHang IS NULL AND h.soDienThoai = :phone
            """)
    int linkUnassignedOrdersByPhone(@Param("customer") com.zestia.datn.zestia.entity.KhachHang customer,
                                    @Param("phone") String phone);

    @Modifying
    @Query("""
            UPDATE HoaDon h SET h.khachHang = :customer
            WHERE h.khachHang IS NULL AND LOWER(h.emailKhachHang) = LOWER(:email)
            """)
    int linkUnassignedOrdersByEmail(@Param("customer") com.zestia.datn.zestia.entity.KhachHang customer,
                                    @Param("email") String email);

    @Query("""
            SELECT h FROM HoaDon h
            JOIN FETCH h.nhanVien n
            WHERE h.hinhThucNhanHang = 0
              AND h.ngayTao >= :startAt
              AND h.ngayTao < :endAt
              AND (:employeeId IS NULL OR n.id = :employeeId)
            ORDER BY h.ngayTao ASC
            """)
    List<HoaDon> findPosOrdersForShiftReport(@Param("startAt") LocalDateTime startAt,
                                             @Param("endAt") LocalDateTime endAt,
                                             @Param("employeeId") Integer employeeId);
    
    List<HoaDon> findByHinhThucNhanHang(Byte hinhThucNhanHang);
    
    boolean existsByMaHoaDon(String maHoaDon);

    long countByTrangThai(Byte trangThai);

    @Query("""
            SELECT COUNT(h) AS orderCount, COALESCE(SUM(
                CASE WHEN h.trangThai = 4
                    OR (h.daThanhToan = true AND h.trangThai NOT IN (5, 6, 7, 9))
                THEN h.tongTien ELSE 0 END
            ), 0) AS revenue
            FROM HoaDon h
            """)
    DashboardSummary summarizeDashboard();

    interface DashboardSummary {
        Long getOrderCount();
        java.math.BigDecimal getRevenue();
    }
    
    Optional<HoaDon> findByMaHoaDon(String maHoaDon);

    Optional<HoaDon> findByMaYeuCau(String maYeuCau);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT h FROM HoaDon h WHERE h.id = :id")
    Optional<HoaDon> findByIdForUpdate(@Param("id") Integer id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT h FROM HoaDon h WHERE h.maHoaDon = :maHoaDon")
    Optional<HoaDon> findByMaHoaDonForUpdate(@Param("maHoaDon") String maHoaDon);

    // ==========================================
    // ===== TRACKING QUERIES (MỚI) =====
    // ==========================================

    @Query("SELECT h FROM HoaDon h WHERE h.maHoaDon = :maHoaDon")
    Optional<HoaDon> findOrderByCode(@Param("maHoaDon") String maHoaDon);

    @Query("SELECT h FROM HoaDon h LEFT JOIN h.khachHang kh WHERE kh.soDienThoai = :soDienThoai OR h.soDienThoai = :soDienThoai ORDER BY h.ngayTao DESC")
    List<HoaDon> findOrdersByPhoneNumber(@Param("soDienThoai") String soDienThoai);

    @Query("SELECT h FROM HoaDon h WHERE h.khachHang.id = :khachHangId ORDER BY h.ngayTao DESC")
    List<HoaDon> findByCustomerIdOrderByLatest(@Param("khachHangId") Integer khachHangId);

    @Query("SELECT h FROM HoaDon h LEFT JOIN h.khachHang kh WHERE h.maHoaDon = :maHoaDon AND (kh.soDienThoai = :soDienThoai OR h.soDienThoai = :soDienThoai)")
    Optional<HoaDon> findOrderByCodeAndPhone(@Param("maHoaDon") String maHoaDon, @Param("soDienThoai") String soDienThoai);

    @Query("SELECT h FROM HoaDon h WHERE h.trangThaiTracking = :trangThaiTracking ORDER BY h.ngayTao DESC")
    List<HoaDon> findByTrackingStatus(@Param("trangThaiTracking") String trangThaiTracking);

    @Query("""
            SELECT h FROM HoaDon h
            WHERE h.trangThai = :status
              AND (h.daThanhToan IS NULL OR h.daThanhToan = false)
              AND h.ngayTao < :cutoff
              AND UPPER(h.hinhThucThanhToan) IN :onlineMethods
              AND (h.phuongThucThanhToanOnline IS NULL OR UPPER(h.phuongThucThanhToanOnline) <> 'FAILED')
            """)
    List<HoaDon> findExpiredPendingOnlinePayments(@Param("status") Byte status,
                                                  @Param("cutoff") LocalDateTime cutoff,
                                                  @Param("onlineMethods") List<String> onlineMethods);
}
