package com.zestia.datn.zestia.controller;

import com.zestia.datn.zestia.repository.KhachHangRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/khach-hang")
@RequiredArgsConstructor
public class KhachHangController {

    private final KhachHangRepository khachHangRepo;

    @GetMapping
    public List<Map<String, Object>> getAll() {
        return khachHangRepo.findCustomerSummaries().stream().map(this::toMap).toList();
    }

    private Map<String, Object> toMap(KhachHangRepository.KhachHangSummary kh) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", kh.getId());
        map.put("maKhachHang", kh.getMaKhachHang());
        map.put("hoVaTen", kh.getHoVaTen());
        map.put("soDienThoai", kh.getSoDienThoai());
        map.put("email", kh.getEmail());
        map.put("gioiTinh", kh.getGioiTinh());
        map.put("tongDon", kh.getTongDon() != null ? kh.getTongDon() : 0);
        map.put("tongChiTieu", kh.getTongChiTieu());
        map.put("ngayTao", kh.getNgayTao());
        return map;
    }
}
