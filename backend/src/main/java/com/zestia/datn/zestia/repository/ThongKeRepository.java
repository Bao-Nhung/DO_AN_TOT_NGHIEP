package com.zestia.datn.zestia.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.zestia.datn.zestia.dto.response.ThongKeDoanhThuDTO;
import com.zestia.datn.zestia.dto.response.ThongKeDoanhThuThoiGianDTO;
import com.zestia.datn.zestia.dto.response.ThongKeKhachHangDTO;
import com.zestia.datn.zestia.dto.response.ThongKeSoLuongDTO;
import com.zestia.datn.zestia.dto.response.ThongKeSanPhamDTO;
import com.zestia.datn.zestia.dto.response.ThongKeTongQuanDTO;
import com.zestia.datn.zestia.dto.response.ThongKeTrangThaiDTO;
import com.zestia.datn.zestia.entity.HoaDon;

@Repository
public interface ThongKeRepository extends JpaRepository<HoaDon, Integer> {

    /* 1. TỔNG QUAN DASHBOARD */
    @Query(value = """
            SELECT
                COALESCE((
                    SELECT SUM(hd.tong_tien + COALESCE(refund.adjustment, 0))
                    FROM Hoa_don hd
                    LEFT JOIN (
                        SELECT id_hoa_don, SUM(so_tien) AS adjustment
                        FROM Lich_su_thanh_toan
                        WHERE UPPER(trang_thai) = N'REFUND_SUCCESS'
                        GROUP BY id_hoa_don
                    ) refund ON refund.id_hoa_don = hd.id
                    WHERE hd.trang_thai = 4 AND hd.ngay_tao >= :startDate AND hd.ngay_tao < :endDate
                ), 0) AS doanhThu,
                (SELECT COUNT(*) FROM Hoa_don hd WHERE hd.ngay_tao >= :startDate AND hd.ngay_tao < :endDate) AS tongDonHang,
                (SELECT COUNT(*) FROM Hoa_don hd WHERE hd.trang_thai = 4 AND hd.ngay_tao >= :startDate AND hd.ngay_tao < :endDate) AS donHangThanhCong,
                (SELECT COUNT(*) FROM Khach_hang kh WHERE kh.ngay_tao >= :startDate AND kh.ngay_tao < :endDate) AS khachHangMoi
            """, nativeQuery = true)
    ThongKeTongQuanDTO getTongQuanTheoThoiGian(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /* 2. TOP SẢN PHẨM BÁN CHẠY (Giới hạn TOP tự động bằng SQL) */
    @Query(value = """
            SELECT TOP 5
                v.id AS id,
                v.ma_san_pham AS ma,
                v.ten_san_pham AS ten,
                lv.ten_loai_san_pham AS danhMuc,
                MIN(vct.gia_ban) AS giaBan,
                SUM(hdct.so_luong - COALESCE(returned.so_luong, 0)) AS tongSoLuong,
                SUM((hdct.so_luong - COALESCE(returned.so_luong, 0)) * hdct.don_gia) AS doanhThu,
                MIN(vct.anh_url) AS anhUrl
            FROM Hoa_don hd
            JOIN Hoa_don_chi_tiet hdct ON hd.id = hdct.id_hoa_don
            JOIN San_pham_chi_tiet vct ON hdct.id_san_pham_chi_tiet = vct.id
            JOIN San_pham v ON vct.id_san_pham = v.id
            JOIN Loai_san_pham lv ON v.id_loai_san_pham = lv.id
            LEFT JOIN (
                SELECT id_hoa_don_chi_tiet, SUM(so_luong) AS so_luong
                FROM Yeu_cau_doi_tra
                WHERE loai_yeu_cau = N'TRA' AND trang_thai = N'DA_HOAN_TIEN'
                GROUP BY id_hoa_don_chi_tiet
            ) returned ON returned.id_hoa_don_chi_tiet = hdct.id
            WHERE hd.trang_thai = 4 AND hd.ngay_tao >= :startDate AND hd.ngay_tao < :endDate
            GROUP BY v.id, v.ma_san_pham, v.ten_san_pham, lv.ten_loai_san_pham
            HAVING SUM(hdct.so_luong - COALESCE(returned.so_luong, 0)) > 0
            ORDER BY tongSoLuong DESC
            """, nativeQuery = true)
    List<ThongKeSanPhamDTO> getTopSanPhamBanChay(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /* 3. SIZE BÁN CHẠY */
    @Query(value = """
            SELECT kt.ten_kich_thuoc AS ten,
                   SUM(hdct.so_luong - COALESCE(returned.so_luong, 0)) AS tongSoLuong,
                   NULL AS doanhThu, NULL AS maHex
            FROM Hoa_don hd
            JOIN Hoa_don_chi_tiet hdct ON hd.id = hdct.id_hoa_don
            JOIN San_pham_chi_tiet vct ON hdct.id_san_pham_chi_tiet = vct.id
            JOIN Kich_Thuoc kt ON vct.id_kich_thuoc = kt.id
            LEFT JOIN (
                SELECT id_hoa_don_chi_tiet, SUM(so_luong) AS so_luong
                FROM Yeu_cau_doi_tra
                WHERE loai_yeu_cau = N'TRA' AND trang_thai = N'DA_HOAN_TIEN'
                GROUP BY id_hoa_don_chi_tiet
            ) returned ON returned.id_hoa_don_chi_tiet = hdct.id
            WHERE hd.trang_thai = 4 AND hd.ngay_tao >= :startDate AND hd.ngay_tao < :endDate
            GROUP BY kt.id, kt.ten_kich_thuoc
            HAVING SUM(hdct.so_luong - COALESCE(returned.so_luong, 0)) > 0
            ORDER BY tongSoLuong DESC
            """, nativeQuery = true)
    List<ThongKeSoLuongDTO> getThongKeTheoSize(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /* 4. MÀU BÁN CHẠY */
    @Query(value = """
            SELECT ms.ten_mau_sac AS ten, ms.ma_hex AS maHex,
                   SUM(hdct.so_luong - COALESCE(returned.so_luong, 0)) AS tongSoLuong,
                   NULL AS doanhThu
            FROM Hoa_don hd
            JOIN Hoa_don_chi_tiet hdct ON hd.id = hdct.id_hoa_don
            JOIN San_pham_chi_tiet vct ON hdct.id_san_pham_chi_tiet = vct.id
            JOIN Mau_Sac ms ON vct.id_mau_sac = ms.id
            LEFT JOIN (
                SELECT id_hoa_don_chi_tiet, SUM(so_luong) AS so_luong
                FROM Yeu_cau_doi_tra
                WHERE loai_yeu_cau = N'TRA' AND trang_thai = N'DA_HOAN_TIEN'
                GROUP BY id_hoa_don_chi_tiet
            ) returned ON returned.id_hoa_don_chi_tiet = hdct.id
            WHERE hd.trang_thai = 4 AND hd.ngay_tao >= :startDate AND hd.ngay_tao < :endDate
            GROUP BY ms.id, ms.ten_mau_sac, ms.ma_hex
            HAVING SUM(hdct.so_luong - COALESCE(returned.so_luong, 0)) > 0
            ORDER BY tongSoLuong DESC
            """, nativeQuery = true)
    List<ThongKeSoLuongDTO> getThongKeTheoMau(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /* 5. DOANH THU THEO DANH MỤC */
    @Query(value = """
            SELECT lv.ten_loai_san_pham AS tenDanhMuc,
                   SUM((hdct.so_luong - COALESCE(returned.so_luong, 0)) * hdct.don_gia) AS tongDoanhThu
            FROM Hoa_don hd
            JOIN Hoa_don_chi_tiet hdct ON hd.id = hdct.id_hoa_don
            JOIN San_pham_chi_tiet vct ON hdct.id_san_pham_chi_tiet = vct.id
            JOIN San_pham v ON vct.id_san_pham = v.id
            JOIN Loai_san_pham lv ON v.id_loai_san_pham = lv.id
            LEFT JOIN (
                SELECT id_hoa_don_chi_tiet, SUM(so_luong) AS so_luong
                FROM Yeu_cau_doi_tra
                WHERE loai_yeu_cau = N'TRA' AND trang_thai = N'DA_HOAN_TIEN'
                GROUP BY id_hoa_don_chi_tiet
            ) returned ON returned.id_hoa_don_chi_tiet = hdct.id
            WHERE hd.trang_thai = 4 AND hd.ngay_tao >= :startDate AND hd.ngay_tao < :endDate
            GROUP BY lv.id, lv.ten_loai_san_pham
            HAVING SUM(hdct.so_luong - COALESCE(returned.so_luong, 0)) > 0
            """, nativeQuery = true)
    List<ThongKeDoanhThuDTO> getThongKeDoanhThuTheoDanhMuc(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /* 6. TRẠNG THÁI ĐƠN HÀNG */
    @Query(value = """
            SELECT 
                CASE
                    WHEN hd.trang_thai = 0 THEN N'Chờ xử lý'
                    WHEN hd.trang_thai = 1 THEN N'Đã xác nhận'
                    WHEN hd.trang_thai = 2 THEN N'Đang chuẩn bị'
                    WHEN hd.trang_thai = 3 THEN N'Đang giao hàng'
                    WHEN hd.trang_thai = 4 THEN N'Hoàn thành'
                    WHEN hd.trang_thai = 5 THEN N'Đã hủy'
                    WHEN hd.trang_thai = 6 THEN N'Giao hàng thất bại'
                    WHEN hd.trang_thai = 7 THEN N'Thanh toán thất bại'
                    ELSE N'Không xác định'
                END AS tenTrangThai,
                COUNT(hd.id) AS soLuong
            FROM Hoa_don hd
            WHERE hd.ngay_tao >= :startDate AND hd.ngay_tao < :endDate
            GROUP BY hd.trang_thai
            """, nativeQuery = true)
    List<ThongKeTrangThaiDTO> getTyLeTrangThaiDonHang(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /* 7. DOANH THU & TĂNG TRƯỞNG THEO NGÀY (Để vẽ Line Chart thời gian ngắn) */
    @Query(value = """
            SELECT FORMAT(hd.ngay_tao, 'yyyy-MM-dd') AS thoiGian,
                   SUM(hd.tong_tien + COALESCE(refund.adjustment, 0)) AS doanhThu
            FROM Hoa_don hd
            LEFT JOIN (
                SELECT id_hoa_don, SUM(so_tien) AS adjustment
                FROM Lich_su_thanh_toan
                WHERE UPPER(trang_thai) = N'REFUND_SUCCESS'
                GROUP BY id_hoa_don
            ) refund ON hd.id = refund.id_hoa_don
            WHERE hd.trang_thai = 4 AND hd.ngay_tao >= :startDate AND hd.ngay_tao < :endDate
            GROUP BY FORMAT(hd.ngay_tao, 'yyyy-MM-dd')
            ORDER BY thoiGian
            """, nativeQuery = true)
    List<ThongKeDoanhThuThoiGianDTO> getDoanhThuTheoNgay(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query(value = """
            SELECT FORMAT(kh.ngay_tao, 'yyyy-MM-dd') AS thoiGian, COUNT(kh.id) AS soLuongKHMoi
            FROM Khach_hang kh
            WHERE kh.ngay_tao >= :startDate AND kh.ngay_tao < :endDate
            GROUP BY FORMAT(kh.ngay_tao, 'yyyy-MM-dd')
            ORDER BY thoiGian
            """, nativeQuery = true)
    List<ThongKeKhachHangDTO> getKhachHangTheoNgay(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /* 8. DOANH THU & TĂNG TRƯỞNG THEO THÁNG (Để vẽ Line Chart thời gian dài) */
    @Query(value = """
            SELECT FORMAT(hd.ngay_tao, 'yyyy-MM') AS thoiGian,
                   SUM(hd.tong_tien + COALESCE(refund.adjustment, 0)) AS doanhThu
            FROM Hoa_don hd
            LEFT JOIN (
                SELECT id_hoa_don, SUM(so_tien) AS adjustment
                FROM Lich_su_thanh_toan
                WHERE UPPER(trang_thai) = N'REFUND_SUCCESS'
                GROUP BY id_hoa_don
            ) refund ON hd.id = refund.id_hoa_don
            WHERE hd.trang_thai = 4 AND hd.ngay_tao >= :startDate AND hd.ngay_tao < :endDate
            GROUP BY FORMAT(hd.ngay_tao, 'yyyy-MM')
            ORDER BY thoiGian
            """, nativeQuery = true)
    List<ThongKeDoanhThuThoiGianDTO> getDoanhThuTheoThang(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query(value = """
            SELECT FORMAT(kh.ngay_tao, 'yyyy-MM') AS thoiGian, COUNT(kh.id) AS soLuongKHMoi
            FROM Khach_hang kh
            WHERE kh.ngay_tao >= :startDate AND kh.ngay_tao < :endDate
            GROUP BY FORMAT(kh.ngay_tao, 'yyyy-MM')
            ORDER BY thoiGian
            """, nativeQuery = true)
    List<ThongKeKhachHangDTO> getKhachHangTheoThang(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
