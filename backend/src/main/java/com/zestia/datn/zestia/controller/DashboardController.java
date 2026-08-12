package com.zestia.datn.zestia.controller;

import com.zestia.datn.zestia.repository.HoaDonChiTietRepository;
import com.zestia.datn.zestia.repository.HoaDonRepository;
import com.zestia.datn.zestia.repository.KhachHangRepository;
import com.zestia.datn.zestia.repository.SanPhamChiTietRepository;
import com.zestia.datn.zestia.repository.SanPhamRepository;
import com.zestia.datn.zestia.repository.LoaiSanPhamRepository;
import com.zestia.datn.zestia.repository.LichSuThanhToanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final HoaDonRepository hoaDonRepo;
    private final HoaDonChiTietRepository hoaDonCtRepo;
    private final KhachHangRepository khachHangRepo;
    private final SanPhamRepository sanPhamRepo;
    private final SanPhamChiTietRepository sanPhamCtRepo;
    private final LoaiSanPhamRepository loaiSanPhamRepo;
    private final LichSuThanhToanRepository paymentHistoryRepo;

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        HoaDonRepository.DashboardSummary summary = hoaDonRepo.summarizeDashboard();
        long orderCount = summary != null && summary.getOrderCount() != null ? summary.getOrderCount() : 0;
        BigDecimal revenue = summary != null && summary.getRevenue() != null ? summary.getRevenue() : BigDecimal.ZERO;
        revenue = revenue.add(paymentHistoryRepo.sumSuccessfulRefundAdjustments()).max(BigDecimal.ZERO);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("doanhThu", revenue);
        map.put("tongDonHang", orderCount);
        map.put("tongKhachHang", khachHangRepo.count());
        map.put("tongSanPham", sanPhamRepo.count());
        map.put("tongBienThe", sanPhamCtRepo.count());
        map.put("tongLoaiSanPham", loaiSanPhamRepo.count());
        map.put("topSellingProducts", topSellingProducts());
        map.put("lowStockVariants", lowStockVariants());
        return map;
    }

    @GetMapping("/inventory")
    public Map<String, Object> inventory() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("tongSanPham", sanPhamRepo.count());
        map.put("tongBienThe", sanPhamCtRepo.count());
        map.put("lowStockVariants", lowStockVariants());
        return map;
    }

    private List<Map<String, Object>> topSellingProducts() {
        return hoaDonCtRepo.findTopSellingProductStats(PageRequest.of(0, 5)).stream()
                .map(row -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("productId", row[0]);
                    item.put("maSanPham", row[1]);
                    item.put("tenSanPham", row[2]);
                    item.put("soLuongBan", row[3]);
                    item.put("doanhThu", row[4]);
                    item.put("anhUrl", row[5]);
                    return item;
                })
                .toList();
    }

    private List<Map<String, Object>> lowStockVariants() {
        return sanPhamCtRepo.findLowStockSummary(PageRequest.of(0, 8)).stream()
                .map(row -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("variantId", row[0]);
                    item.put("productId", row[1]);
                    item.put("tenSanPham", row[2]);
                    item.put("maSanPham", row[3]);
                    item.put("mauSac", row[4]);
                    item.put("kichThuoc", row[5]);
                    item.put("soLuong", row[6]);
                    item.put("anhUrl", row[7]);
                    return item;
                })
                .toList();
    }
}
