package com.zestia.datn.zestia.controller;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.zestia.datn.zestia.dto.ThongKeResponse;
import com.zestia.datn.zestia.repository.ThongKeRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/thong-ke")
@RequiredArgsConstructor
public class ThongKeController {

    private final ThongKeRepository thongKeRepository;

    @GetMapping("/tong-hop")
    public ThongKeResponse getThongKeTongHop(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime endDate,
            @RequestParam(defaultValue = "ngay") String timeType
    ) {
        if (!endDate.isAfter(startDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thời gian kết thúc phải sau thời gian bắt đầu");
        }
        if (!"ngay".equalsIgnoreCase(timeType) && !"thang".equalsIgnoreCase(timeType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Kiểu thống kê chỉ nhận 'ngay' hoặc 'thang'");
        }

        ThongKeResponse response = new ThongKeResponse();

        // 1. Dữ liệu tổng quan chung (Thẻ số lượng)
        response.setTongQuan(thongKeRepository.getTongQuanTheoThoiGian(startDate, endDate));
        response.setTrangThaiDonHang(thongKeRepository.getTyLeTrangThaiDonHang(startDate, endDate));
        response.setTopSanPham(thongKeRepository.getTopSanPhamBanChay(startDate, endDate));
        response.setTheoSize(thongKeRepository.getThongKeTheoSize(startDate, endDate));
        response.setTheoMau(thongKeRepository.getThongKeTheoMau(startDate, endDate));
        response.setDoanhThuDanhMuc(thongKeRepository.getThongKeDoanhThuTheoDanhMuc(startDate, endDate));

        // 2. Dữ liệu mốc thời gian (Line/Bar Chart) - Render động dựa vào timeType
        if ("thang".equalsIgnoreCase(timeType)) {
            response.setDoanhThuLoiNhuan(thongKeRepository.getDoanhThuLoiNhuanTheoThang(startDate, endDate));
            response.setTangTruongKhachHang(thongKeRepository.getKhachHangTheoThang(startDate, endDate));
        } else {
            response.setDoanhThuLoiNhuan(thongKeRepository.getDoanhThuLoiNhuanTheoNgay(startDate, endDate));
            response.setTangTruongKhachHang(thongKeRepository.getKhachHangTheoNgay(startDate, endDate));
        }

        return response;
    }
}
