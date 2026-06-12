package com.zestia.datn.zestia.controller;

import com.zestia.datn.zestia.entity.GiamGia;
import com.zestia.datn.zestia.entity.KhuyenMai;
import com.zestia.datn.zestia.repository.GiamGiaRepository;
import com.zestia.datn.zestia.repository.KhuyenMaiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/khuyen-mai")
@RequiredArgsConstructor
public class KhuyenMaiController {

    private final KhuyenMaiRepository khuyenMaiRepo;
    private final GiamGiaRepository giamGiaRepo;

    @GetMapping
    public Object getKhuyenMai() { return khuyenMaiRepo.findAll(); }

    @GetMapping("/giam-gia")
    public Object getGiamGia() { return giamGiaRepo.findAll(); }

    @GetMapping("/all")
    public Map<String, Object> getAll() {
        return Map.of(
            "khuyenMai", khuyenMaiRepo.findAll(),
            "giamGia", giamGiaRepo.findAll()
        );
    }

    // --- Mã giảm giá (Voucher) CRUD ---
    @PostMapping("/giam-gia")
    public ResponseEntity<?> addGiamGia(@RequestBody GiamGia gg) {
        gg.setNgayTao(LocalDateTime.now());
        if (gg.getTrangThai() == null) gg.setTrangThai((byte) 1);
        return ResponseEntity.ok(giamGiaRepo.save(gg));
    }

    @PutMapping("/giam-gia/{id}")
    public ResponseEntity<?> updateGiamGia(@PathVariable Integer id, @RequestBody GiamGia gg) {
        return giamGiaRepo.findById(id).map(existing -> {
            if (gg.getMaGiamGia() != null) existing.setMaGiamGia(gg.getMaGiamGia());
            if (gg.getTenGiamGia() != null) existing.setTenGiamGia(gg.getTenGiamGia());
            if (gg.getPhanTramGiam() != null) existing.setPhanTramGiam(gg.getPhanTramGiam());
            if (gg.getGiamToiDa() != null) existing.setGiamToiDa(gg.getGiamToiDa());
            if (gg.getGiaTriDonToiThieu() != null) existing.setGiaTriDonToiThieu(gg.getGiaTriDonToiThieu());
            if (gg.getSoLuong() != null) existing.setSoLuong(gg.getSoLuong());
            if (gg.getNgayBatDau() != null) existing.setNgayBatDau(gg.getNgayBatDau());
            if (gg.getNgayKetThuc() != null) existing.setNgayKetThuc(gg.getNgayKetThuc());
            if (gg.getTrangThai() != null) existing.setTrangThai(gg.getTrangThai());
            return ResponseEntity.ok(giamGiaRepo.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/giam-gia/{id}")
    public ResponseEntity<?> deleteGiamGia(@PathVariable Integer id) {
        giamGiaRepo.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // --- Khuyến mãi CRUD ---
    @PostMapping
    public ResponseEntity<?> addKhuyenMai(@RequestBody KhuyenMai km) {
        km.setNgayTao(LocalDateTime.now());
        if (km.getTrangThai() == null) km.setTrangThai((byte) 1);
        return ResponseEntity.ok(khuyenMaiRepo.save(km));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateKhuyenMai(@PathVariable Integer id, @RequestBody KhuyenMai km) {
        return khuyenMaiRepo.findById(id).map(existing -> {
            if (km.getMaKhuyenMai() != null) existing.setMaKhuyenMai(km.getMaKhuyenMai());
            if (km.getTenKhuyenMai() != null) existing.setTenKhuyenMai(km.getTenKhuyenMai());
            if (km.getPhanTramGiam() != null) existing.setPhanTramGiam(km.getPhanTramGiam());
            if (km.getSoTienGiam() != null) existing.setSoTienGiam(km.getSoTienGiam());
            if (km.getNgayBatDau() != null) existing.setNgayBatDau(km.getNgayBatDau());
            if (km.getNgayKetThuc() != null) existing.setNgayKetThuc(km.getNgayKetThuc());
            if (km.getMoTa() != null) existing.setMoTa(km.getMoTa());
            if (km.getTrangThai() != null) existing.setTrangThai(km.getTrangThai());
            return ResponseEntity.ok(khuyenMaiRepo.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteKhuyenMai(@PathVariable Integer id) {
        khuyenMaiRepo.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
