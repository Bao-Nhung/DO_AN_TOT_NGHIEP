package com.zestia.datn.zestia.controller;

import com.zestia.datn.zestia.entity.KhachHang;
import com.zestia.datn.zestia.repository.HoaDonRepository;
import com.zestia.datn.zestia.repository.KhachHangRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/khach-hang")
@RequiredArgsConstructor
public class KhachHangController {

    private final KhachHangRepository khachHangRepo;
    private final HoaDonRepository hoaDonRepo;

    @GetMapping
    public List<Map<String, Object>> getAll() {
        return khachHangRepo.findAll().stream().map(this::toMap).toList();
    }

    private Map<String, Object> toMap(KhachHang kh) {
        var orders = hoaDonRepo.findByKhachHangId(kh.getId());
        BigDecimal totalSpent = orders.stream()
                .map(o -> o.getTongTien() != null ? o.getTongTien() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", kh.getId());
        map.put("maKhachHang", kh.getMaKhachHang());
        map.put("hoVaTen", kh.getHoVaTen());
        map.put("soDienThoai", kh.getSoDienThoai());
        map.put("email", kh.getEmail());
        map.put("gioiTinh", kh.getGioiTinh());
        map.put("tongDon", orders.size());
        map.put("tongChiTieu", totalSpent);
        map.put("ngayTao", kh.getNgayTao());
        return map;
    }
}
