package com.zestia.datn.zestia.controller;

import com.zestia.datn.zestia.entity.HoaDon;
import com.zestia.datn.zestia.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final HoaDonRepository hoaDonRepo;
    private final KhachHangRepository khachHangRepo;
    private final VayRepository vayRepo;
    private final VayChiTietRepository vayCtRepo;

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        List<HoaDon> allOrders = hoaDonRepo.findAll();

        BigDecimal doanhThu = allOrders.stream()
                .map(o -> o.getTongTien() != null ? o.getTongTien() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("doanhThu", doanhThu);
        map.put("tongDonHang", allOrders.size());
        map.put("tongKhachHang", khachHangRepo.count());
        map.put("tongSanPham", vayCtRepo.count());
        map.put("tongLoaiVay", vayRepo.count());
        return map;
    }
}
