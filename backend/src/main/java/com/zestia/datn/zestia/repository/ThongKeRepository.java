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
import com.zestia.datn.zestia.dto.response.ThongKeTongQuanDTO;
import com.zestia.datn.zestia.dto.response.ThongKeTrangThaiDTO;
import com.zestia.datn.zestia.entity.HoaDon;

@Repository
public interface ThongKeRepository extends JpaRepository<HoaDon, Integer> {

    /* 1. TỔNG QUAN DASHBOARD */
    @Query(value = """
            SELECT
                COALESCE((SELECT SUM(hd.tong_tien) FROM Hoa_don hd WHERE hd.trang_thai = 3 AND hd.ngay_tao BETWEEN :startDate AND :endDate), 0) AS doanhThu,
                COALESCE((SELECT SUM(hdct.so_luong * hdct.gia_nhap) FROM Hoa_don hd JOIN Hoa_don_chi_tiet hdct ON hd.id = hdct.id_hoa_don WHERE hd.trang_thai = 3 AND hd.ngay_tao BETWEEN :startDate AND :endDate), 0) AS giaVon,
                COALESCE((SELECT SUM(hd.tong_tien) FROM Hoa_don hd WHERE hd.trang_thai = 3 AND hd.ngay_tao BETWEEN :startDate AND :endDate), 0) -
                COALESCE((SELECT SUM(hdct.so_luong * hdct.gia_nhap) FROM Hoa_don hd JOIN Hoa_don_chi_tiet hdct ON hd.id = hdct.id_hoa_don WHERE hd.trang_thai = 3 AND hd.ngay_tao BETWEEN :startDate AND :endDate), 0) AS loiNhuanGop,
                (SELECT COUNT(*) FROM Hoa_don hd WHERE hd.ngay_tao BETWEEN :startDate AND :endDate) AS tongDonHang,
                (SELECT COUNT(*) FROM Hoa_don hd WHERE hd.trang_thai = 3 AND hd.ngay_tao BETWEEN :startDate AND :endDate) AS donHangThanhCong,
                (SELECT COUNT(*) FROM Khach_hang kh WHERE kh.ngay_tao BETWEEN :startDate AND :endDate) AS khachHangMoi
            """, nativeQuery = true)
    ThongKeTongQuanDTO getTongQuanTheoThoiGian(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /* 2. TOP SẢN PHẨM BÁN CHẠY (Giới hạn TOP tự động bằng SQL) */
    @Query(value = """
            SELECT TOP 10
                v.ten_vay AS ten,
                SUM(hdct.so_luong) AS tongSoLuong,
                SUM(hdct.so_luong * hdct.don_gia) AS doanhThu
            FROM Hoa_don hd
            JOIN Hoa_don_chi_tiet hdct ON hd.id = hdct.id_hoa_don
            JOIN Vay_chi_tiet vct ON hdct.id_vay_chi_tiet = vct.id
            JOIN Vay v ON vct.id_vay = v.id
            WHERE hd.trang_thai = 3 AND hd.ngay_tao BETWEEN :startDate AND :endDate
            GROUP BY v.id, v.ten_vay
            ORDER BY tongSoLuong DESC
            """, nativeQuery = true)
    List<ThongKeSoLuongDTO> getTopSanPhamBanChay(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /* 3. SIZE BÁN CHẠY */
    @Query(value = """
            SELECT kt.ten_kich_thuoc AS ten, SUM(hdct.so_luong) AS tongSoLuong, NULL AS doanhThu, NULL AS maHex
            FROM Hoa_don hd
            JOIN Hoa_don_chi_tiet hdct ON hd.id = hdct.id_hoa_don
            JOIN Vay_chi_tiet vct ON hdct.id_vay_chi_tiet = vct.id
            JOIN Kich_Thuoc kt ON vct.id_kich_thuoc = kt.id
            WHERE hd.trang_thai = 3 AND hd.ngay_tao BETWEEN :startDate AND :endDate
            GROUP BY kt.id, kt.ten_kich_thuoc
            ORDER BY tongSoLuong DESC
            """, nativeQuery = true)
    List<ThongKeSoLuongDTO> getThongKeTheoSize(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /* 4. MÀU BÁN CHẠY */
    @Query(value = """
            SELECT ms.ten_mau_sac AS ten, ms.ma_hex AS maHex, SUM(hdct.so_luong) AS tongSoLuong, NULL AS doanhThu
            FROM Hoa_don hd
            JOIN Hoa_don_chi_tiet hdct ON hd.id = hdct.id_hoa_don
            JOIN Vay_chi_tiet vct ON hdct.id_vay_chi_tiet = vct.id
            JOIN Mau_Sac ms ON vct.id_mau_sac = ms.id
            WHERE hd.trang_thai = 3 AND hd.ngay_tao BETWEEN :startDate AND :endDate
            GROUP BY ms.id, ms.ten_mau_sac, ms.ma_hex
            ORDER BY tongSoLuong DESC
            """, nativeQuery = true)
    List<ThongKeSoLuongDTO> getThongKeTheoMau(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /* 5. DOANH THU THEO DANH MỤC */
    @Query(value = """
            SELECT lv.ten_loai_vay AS tenDanhMuc, SUM(hdct.so_luong * hdct.don_gia) AS tongDoanhThu
            FROM Hoa_don hd
            JOIN Hoa_don_chi_tiet hdct ON hd.id = hdct.id_hoa_don
            JOIN Vay_chi_tiet vct ON hdct.id_vay_chi_tiet = vct.id
            JOIN Vay v ON vct.id_vay = v.id
            JOIN Loai_vay lv ON v.id_loai_vay = lv.id
            WHERE hd.trang_thai = 3 AND hd.ngay_tao BETWEEN :startDate AND :endDate
            GROUP BY lv.id, lv.ten_loai_vay
            """, nativeQuery = true)
    List<ThongKeDoanhThuDTO> getThongKeDoanhThuTheoDanhMuc(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /* 6. TRẠNG THÁI ĐƠN HÀNG */
    @Query(value = """
            SELECT 
                CASE
                    WHEN hd.trang_thai = 0 THEN N'Chờ xử lý'
                    WHEN hd.trang_thai = 1 THEN N'Đã xác nhận'
                    WHEN hd.trang_thai = 2 THEN N'Đang giao'
                    WHEN hd.trang_thai = 3 THEN N'Hoàn thành'
                    WHEN hd.trang_thai = 4 THEN N'Đã huỷ'
                    ELSE N'Không xác định'
                END AS tenTrangThai,
                COUNT(hd.id) AS soLuong
            FROM Hoa_don hd
            WHERE hd.ngay_tao BETWEEN :startDate AND :endDate
            GROUP BY hd.trang_thai
            """, nativeQuery = true)
    List<ThongKeTrangThaiDTO> getTyLeTrangThaiDonHang(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /* 7. DOANH THU & TĂNG TRƯỞNG THEO NGÀY (Để vẽ Line Chart thời gian ngắn) */
    @Query(value = """
            SELECT FORMAT(hd.ngay_tao, 'yyyy-MM-dd') AS thoiGian, SUM(hd.tong_tien) AS doanhThu, 
                   SUM(hd.tong_tien) - SUM(hdct.so_luong * hdct.gia_nhap) AS loiNhuan
            FROM Hoa_don hd
            JOIN Hoa_don_chi_tiet hdct ON hd.id = hdct.id_hoa_don
            WHERE hd.trang_thai = 3 AND hd.ngay_tao BETWEEN :startDate AND :endDate
            GROUP BY FORMAT(hd.ngay_tao, 'yyyy-MM-dd')
            ORDER BY thoiGian
            """, nativeQuery = true)
    List<ThongKeDoanhThuThoiGianDTO> getDoanhThuLoiNhuanTheoNgay(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query(value = """
            SELECT FORMAT(kh.ngay_tao, 'yyyy-MM-dd') AS thoiGian, COUNT(kh.id) AS soLuongKHMoi
            FROM Khach_hang kh
            WHERE kh.ngay_tao BETWEEN :startDate AND :endDate
            GROUP BY FORMAT(kh.ngay_tao, 'yyyy-MM-dd')
            ORDER BY thoiGian
            """, nativeQuery = true)
    List<ThongKeKhachHangDTO> getKhachHangTheoNgay(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /* 8. DOANH THU & TĂNG TRƯỞNG THEO THÁNG (Để vẽ Line Chart thời gian dài) */
    @Query(value = """
            SELECT FORMAT(hd.ngay_tao, 'yyyy-MM') AS thoiGian, SUM(hd.tong_tien) AS doanhThu, 
                   SUM(hd.tong_tien) - SUM(hdct.so_luong * hdct.gia_nhap) AS loiNhuan
            FROM Hoa_don hd
            JOIN Hoa_don_chi_tiet hdct ON hd.id = hdct.id_hoa_don
            WHERE hd.trang_thai = 3 AND hd.ngay_tao BETWEEN :startDate AND :endDate
            GROUP BY FORMAT(hd.ngay_tao, 'yyyy-MM')
            ORDER BY thoiGian
            """, nativeQuery = true)
    List<ThongKeDoanhThuThoiGianDTO> getDoanhThuLoiNhuanTheoThang(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query(value = """
            SELECT FORMAT(kh.ngay_tao, 'yyyy-MM') AS thoiGian, COUNT(kh.id) AS soLuongKHMoi
            FROM Khach_hang kh
            WHERE kh.ngay_tao BETWEEN :startDate AND :endDate
            GROUP BY FORMAT(kh.ngay_tao, 'yyyy-MM')
            ORDER BY thoiGian
            """, nativeQuery = true)
    List<ThongKeKhachHangDTO> getKhachHangTheoThang(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}