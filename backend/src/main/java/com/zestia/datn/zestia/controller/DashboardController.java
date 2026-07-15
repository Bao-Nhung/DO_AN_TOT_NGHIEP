package com.zestia.datn.zestia.controller;

import com.zestia.datn.zestia.repository.HoaDonChiTietRepository;
import com.zestia.datn.zestia.repository.HoaDonRepository;
import com.zestia.datn.zestia.repository.KhachHangRepository;
import com.zestia.datn.zestia.repository.VayChiTietRepository;
import com.zestia.datn.zestia.repository.VayRepository;
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
    private final VayRepository vayRepo;
    private final VayChiTietRepository vayCtRepo;

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        HoaDonRepository.DashboardSummary summary = hoaDonRepo.summarizeDashboard();
        long orderCount = summary != null && summary.getOrderCount() != null ? summary.getOrderCount() : 0;
        BigDecimal revenue = summary != null && summary.getRevenue() != null ? summary.getRevenue() : BigDecimal.ZERO;

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("doanhThu", revenue);
        map.put("tongDonHang", orderCount);
        map.put("tongKhachHang", khachHangRepo.count());
        map.put("tongSanPham", vayRepo.count());
        map.put("tongBienThe", vayCtRepo.count());
        map.put("tongLoaiVay", vayRepo.count());
        map.put("topSellingProducts", topSellingProducts());
        map.put("lowStockVariants", lowStockVariants());
        return map;
    }

    @GetMapping("/inventory")
    public Map<String, Object> inventory() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("tongSanPham", vayRepo.count());
        map.put("tongBienThe", vayCtRepo.count());
        map.put("lowStockVariants", lowStockVariants());
        return map;
    }

    private List<Map<String, Object>> topSellingProducts() {
        return hoaDonCtRepo.findTopSellingProductStats(PageRequest.of(0, 5)).stream()
                .map(row -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("productId", row[0]);
                    item.put("maVay", row[1]);
                    item.put("tenVay", row[2]);
                    item.put("soLuongBan", row[3]);
                    item.put("doanhThu", row[4]);
                    return item;
                })
                .toList();
    }

    private List<Map<String, Object>> lowStockVariants() {
        return vayCtRepo.findLowStockSummary(PageRequest.of(0, 8)).stream()
                .map(row -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("variantId", row[0]);
                    item.put("productId", row[1]);
                    item.put("tenVay", row[2]);
                    item.put("maVay", row[3]);
                    item.put("mauSac", row[4]);
                    item.put("kichThuoc", row[5]);
                    item.put("soLuong", row[6]);
                    return item;
                })
                .toList();
    }
}
