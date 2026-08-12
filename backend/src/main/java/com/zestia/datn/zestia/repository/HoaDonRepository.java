package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.HoaDon;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

public interface HoaDonRepository extends JpaRepository<HoaDon, Integer> {

    @EntityGraph(attributePaths = {"khachHang", "nhanVien", "giamGia"})
    @Query(value = """
            SELECT h FROM HoaDon h
            LEFT JOIN h.khachHang kh
            WHERE (:keyword IS NULL
                   OR LOWER(h.maHoaDon) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(h.tenKhachHang) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(kh.hoVaTen) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR h.soDienThoai LIKE CONCAT('%', :keyword, '%')
                   OR kh.soDienThoai LIKE CONCAT('%', :keyword, '%'))
              AND (:status IS NULL OR h.trangThai = :status)
              AND (:orderType IS NULL OR h.hinhThucNhanHang = :orderType)
            """,
            countQuery = """
            SELECT COUNT(h) FROM HoaDon h
            LEFT JOIN h.khachHang kh
            WHERE (:keyword IS NULL
                   OR LOWER(h.maHoaDon) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(h.tenKhachHang) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(kh.hoVaTen) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR h.soDienThoai LIKE CONCAT('%', :keyword, '%')
                   OR kh.soDienThoai LIKE CONCAT('%', :keyword, '%'))
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
                   OR LOWER(kh.hoVaTen) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR h.soDienThoai LIKE CONCAT('%', :keyword, '%')
                   OR kh.soDienThoai LIKE CONCAT('%', :keyword, '%'))
              AND (:orderType IS NULL OR h.hinhThucNhanHang = :orderType)
            GROUP BY h.trangThai
            """)
    List<Object[]> countAdminOrdersByStatus(@Param("keyword") String keyword,
                                            @Param("orderType") Byte orderType);
    
    List<HoaDon> findByNhanVienId(Integer nhanVienId);

    @Modifying
    @Query("""
            UPDATE HoaDon h SET h.khachHang = :customer
            WHERE h.khachHang IS NULL
              AND (
                    (:phone IS NOT NULL AND h.soDienThoai = :phone
                     AND (h.emailKhachHang IS NULL OR :email IS NULL OR LOWER(h.emailKhachHang) = LOWER(:email)))
                 OR (:email IS NOT NULL AND LOWER(h.emailKhachHang) = LOWER(:email)
                     AND (h.soDienThoai IS NULL OR :phone IS NULL OR h.soDienThoai = :phone))
              )
            """)
    int linkUnassignedOrdersByIdentity(
            @Param("customer") com.zestia.datn.zestia.entity.KhachHang customer,
            @Param("phone") String phone,
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
    
    boolean existsByMaHoaDon(String maHoaDon);

    long countByTrangThai(Byte trangThai);

    @Query("""
            SELECT COUNT(h) FROM HoaDon h
            WHERE h.trangThai = 0
              AND (UPPER(COALESCE(h.hinhThucThanhToan, '')) = 'COD'
                   OR h.daThanhToan = true)
            """)
    long countActionablePendingOrders();

    @Query("""
            SELECT COUNT(h) AS orderCount, COALESCE(SUM(
                CASE WHEN h.hinhThucNhanHang = 0
                           AND (h.trangThai = 4 OR h.daThanhToan = true)
                     THEN h.tongTien ELSE 0 END
            ), 0) AS posRevenue
            FROM HoaDon h
            WHERE h.nhanVien.id = :employeeId
              AND h.ngayTao >= :startAt
              AND h.ngayTao < :endAt
            """)
    StaffTodaySummary summarizeStaffToday(@Param("employeeId") Integer employeeId,
                                           @Param("startAt") LocalDateTime startAt,
                                           @Param("endAt") LocalDateTime endAt);

    @EntityGraph(attributePaths = {"khachHang", "nhanVien", "giamGia"})
    @Query("SELECT h FROM HoaDon h WHERE h.nhanVien.id = :employeeId")
    Page<HoaDon> findStaffRecentOrders(
            @Param("employeeId") Integer employeeId,
            Pageable pageable
    );

    @Query("""
            SELECT COUNT(h) AS orderCount, COALESCE(SUM(
                CASE WHEN h.trangThai = 4 THEN h.tongTien ELSE 0 END
            ), 0) AS revenue
            FROM HoaDon h
            """)
    DashboardSummary summarizeDashboard();

    @Query("""
            SELECT COALESCE(SUM(h.tongTien), 0) FROM HoaDon h
            WHERE h.khachHang.id = :customerId AND h.trangThai = 4
            """)
    java.math.BigDecimal sumCompletedSpendByCustomerId(@Param("customerId") Integer customerId);

    interface DashboardSummary {
        Long getOrderCount();
        java.math.BigDecimal getRevenue();
    }

    interface StaffTodaySummary {
        Long getOrderCount();
        java.math.BigDecimal getPosRevenue();
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

    @EntityGraph(attributePaths = {"khachHang", "nhanVien", "giamGia"})
    @Query("SELECT h FROM HoaDon h WHERE h.khachHang.id = :customerId")
    Page<HoaDon> findCustomerHistoryPage(@Param("customerId") Integer customerId, Pageable pageable);

    @EntityGraph(attributePaths = {"khachHang", "nhanVien", "giamGia"})
    @Query(value = """
            SELECT h FROM HoaDon h
            WHERE h.khachHang.id = :customerId
              AND (:keyword IS NULL OR LOWER(h.maHoaDon) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (
                    :tab = 'all'
                 OR (:tab = 'unpaid' AND h.trangThai = 0
                     AND UPPER(COALESCE(h.hinhThucThanhToan, '')) IN ('MOMO', 'ZALOPAY')
                     AND (h.daThanhToan IS NULL OR h.daThanhToan = false))
                 OR (:tab = 'pending' AND h.trangThai = 0
                     AND UPPER(COALESCE(h.hinhThucThanhToan, 'COD')) = 'COD')
                 OR (:tab = 'processing' AND h.trangThai IN (1, 2, 3))
                 OR (:tab = 'completed' AND h.trangThai = 4)
                 OR (:tab = 'cancelled' AND h.trangThai IN (5, 6, 7))
              )
            """,
            countQuery = """
            SELECT COUNT(h) FROM HoaDon h
            WHERE h.khachHang.id = :customerId
              AND (:keyword IS NULL OR LOWER(h.maHoaDon) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (
                    :tab = 'all'
                 OR (:tab = 'unpaid' AND h.trangThai = 0
                     AND UPPER(COALESCE(h.hinhThucThanhToan, '')) IN ('MOMO', 'ZALOPAY')
                     AND (h.daThanhToan IS NULL OR h.daThanhToan = false))
                 OR (:tab = 'pending' AND h.trangThai = 0
                     AND UPPER(COALESCE(h.hinhThucThanhToan, 'COD')) = 'COD')
                 OR (:tab = 'processing' AND h.trangThai IN (1, 2, 3))
                 OR (:tab = 'completed' AND h.trangThai = 4)
                 OR (:tab = 'cancelled' AND h.trangThai IN (5, 6, 7))
              )
            """)
    Page<HoaDon> findCustomerPage(@Param("customerId") Integer customerId,
                                  @Param("keyword") String keyword,
                                  @Param("tab") String tab,
                                  Pageable pageable);

    @Query("""
            SELECT h.trangThai, UPPER(COALESCE(h.hinhThucThanhToan, 'COD')),
                   COALESCE(h.daThanhToan, false), COUNT(h)
            FROM HoaDon h
            WHERE h.khachHang.id = :customerId
            GROUP BY h.trangThai, UPPER(COALESCE(h.hinhThucThanhToan, 'COD')),
                     COALESCE(h.daThanhToan, false)
            """)
    List<Object[]> countCustomerOrdersByState(@Param("customerId") Integer customerId);

    @Query("SELECT h FROM HoaDon h LEFT JOIN h.khachHang kh WHERE h.maHoaDon = :maHoaDon AND (kh.soDienThoai = :soDienThoai OR h.soDienThoai = :soDienThoai)")
    Optional<HoaDon> findOrderByCodeAndPhone(@Param("maHoaDon") String maHoaDon, @Param("soDienThoai") String soDienThoai);

    @Query("""
            SELECT h FROM HoaDon h
            WHERE h.trangThai = :status
              AND (h.daThanhToan IS NULL OR h.daThanhToan = false)
              AND ((h.thanhToanHetHan IS NOT NULL AND h.thanhToanHetHan < :now)
                   OR (h.thanhToanHetHan IS NULL AND h.ngayTao < :fallbackCutoff))
              AND UPPER(h.hinhThucThanhToan) IN :onlineMethods
              AND (h.phuongThucThanhToanOnline IS NULL OR UPPER(h.phuongThucThanhToanOnline) <> 'FAILED')
            ORDER BY h.thanhToanHetHan ASC, h.ngayTao ASC, h.id ASC
            """)
    List<HoaDon> findExpiredPendingOnlinePayments(@Param("status") Byte status,
                                                   @Param("now") LocalDateTime now,
                                                   @Param("fallbackCutoff") LocalDateTime fallbackCutoff,
                                                   @Param("onlineMethods") List<String> onlineMethods,
                                                   Pageable pageable);
}
