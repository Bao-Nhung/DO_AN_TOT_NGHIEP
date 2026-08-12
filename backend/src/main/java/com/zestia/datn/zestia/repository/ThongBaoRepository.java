package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.ThongBao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

public interface ThongBaoRepository extends JpaRepository<ThongBao, Integer> {
    java.util.Optional<ThongBao> findByIdAndKhachHangIsNull(Integer id);

    @Query(value = """
            SELECT notification FROM ThongBao notification
            WHERE notification.khachHang IS NULL
              AND (:status IS NULL OR notification.trangThai = :status)
              AND (:type IS NULL OR notification.loai = :type)
              AND (:keyword IS NULL
                   OR LOWER(notification.tieuDe) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(notification.noiDung) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """,
            countQuery = """
            SELECT COUNT(notification) FROM ThongBao notification
            WHERE notification.khachHang IS NULL
              AND (:status IS NULL OR notification.trangThai = :status)
              AND (:type IS NULL OR notification.loai = :type)
              AND (:keyword IS NULL
                   OR LOWER(notification.tieuDe) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(notification.noiDung) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<ThongBao> findAdminPage(@Param("keyword") String keyword,
                                 @Param("type") String type,
                                 @Param("status") Byte status,
                                 Pageable pageable);

    @Query(value = """
            SELECT notification FROM ThongBao notification
            WHERE notification.khachHang IS NULL
              AND notification.trangThai = :status
              AND (:type IS NULL OR notification.loai = :type)
              AND (:keyword IS NULL
                   OR LOWER(notification.tieuDe) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(notification.noiDung) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """,
            countQuery = """
            SELECT COUNT(notification) FROM ThongBao notification
            WHERE notification.khachHang IS NULL
              AND notification.trangThai = :status
              AND (:type IS NULL OR notification.loai = :type)
              AND (:keyword IS NULL
                   OR LOWER(notification.tieuDe) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(notification.noiDung) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<ThongBao> findPublicPage(@Param("keyword") String keyword,
                                  @Param("type") String type,
                                  @Param("status") Byte status,
                                  Pageable pageable);

    @Query("""
            SELECT notification FROM ThongBao notification
            WHERE notification.trangThai = :status
              AND (notification.khachHang IS NULL OR notification.khachHang.id = :customerId)
            ORDER BY notification.ngayTao DESC, notification.id DESC
            """)
    List<ThongBao> findVisibleForCustomer(@Param("customerId") Integer customerId,
                                          @Param("status") Byte status);

    @Query(value = """
            SELECT notification FROM ThongBao notification
            WHERE notification.trangThai = :status
              AND (notification.khachHang IS NULL OR notification.khachHang.id = :customerId)
              AND (:type IS NULL OR notification.loai = :type)
              AND (:keyword IS NULL
                   OR LOWER(notification.tieuDe) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(notification.noiDung) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """,
            countQuery = """
            SELECT COUNT(notification) FROM ThongBao notification
            WHERE notification.trangThai = :status
              AND (notification.khachHang IS NULL OR notification.khachHang.id = :customerId)
              AND (:type IS NULL OR notification.loai = :type)
              AND (:keyword IS NULL
                   OR LOWER(notification.tieuDe) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(notification.noiDung) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<ThongBao> findVisibleForCustomerPage(@Param("customerId") Integer customerId,
                                              @Param("status") Byte status,
                                              @Param("keyword") String keyword,
                                              @Param("type") String type,
                                              Pageable pageable);

    @Query("""
            SELECT notification.loai, COUNT(notification)
            FROM ThongBao notification
            WHERE notification.trangThai = :status
              AND (notification.khachHang IS NULL OR notification.khachHang.id = :customerId)
            GROUP BY notification.loai
            """)
    List<Object[]> countVisibleByType(@Param("customerId") Integer customerId,
                                      @Param("status") Byte status);

    @Query("""
            SELECT COUNT(notification)
            FROM ThongBao notification
            WHERE notification.trangThai = :status
              AND (notification.khachHang IS NULL OR notification.khachHang.id = :customerId)
              AND NOT EXISTS (
                  SELECT readState.id FROM ThongBaoDaDoc readState
                  WHERE readState.khachHang.id = :customerId
                    AND readState.thongBao.id = notification.id
              )
            """)
    long countUnreadForCustomer(@Param("customerId") Integer customerId,
                                @Param("status") Byte status);

    @Query("""
            SELECT notification.loai, COUNT(notification)
            FROM ThongBao notification
            WHERE notification.khachHang IS NULL AND notification.trangThai = :status
            GROUP BY notification.loai
            """)
    List<Object[]> countPublicByType(@Param("status") Byte status);

    List<ThongBao> findByTrangThaiAndKhachHangIsNullAndNgayGuiBefore(
            Byte trangThai, LocalDateTime time, Pageable pageable);

    @Query("""
            SELECT notification FROM ThongBao notification
            WHERE notification.khachHang IS NULL
              AND notification.trangThai = 1
              AND notification.guiEmail = 1
              AND (notification.daGui IS NULL OR notification.daGui = 0)
            ORDER BY notification.id
            """)
    List<ThongBao> findPendingEmailAnnouncements(Pageable pageable);

    @Modifying
    @Transactional
    @Query("""
            UPDATE ThongBao notification SET notification.daGui = 2
            WHERE notification.id = :id
              AND notification.khachHang IS NULL
              AND notification.guiEmail = 1
              AND (notification.daGui IS NULL OR notification.daGui = 0)
            """)
    int claimEmailDispatch(@Param("id") Integer id);

    @Modifying
    @Transactional
    @Query("UPDATE ThongBao notification SET notification.daGui = :status WHERE notification.id = :id")
    int updateEmailDispatchStatus(@Param("id") Integer id, @Param("status") Byte status);
}
