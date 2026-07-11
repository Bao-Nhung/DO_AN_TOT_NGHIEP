package com.zestia.datn.zestia.controller;

import com.zestia.datn.zestia.entity.GiamGia;
import com.zestia.datn.zestia.repository.GiamGiaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/voucher")
@RequiredArgsConstructor
public class VoucherController {

    private final GiamGiaRepository giamGiaRepo;

    @GetMapping
    public Object getVouchers() {
        return giamGiaRepo.findAll();
    }

    @PostMapping
    public ResponseEntity<?> addVoucher(@RequestBody GiamGia voucher) {
        voucher.setNgayTao(LocalDateTime.now());
        if (voucher.getTrangThai() == null) voucher.setTrangThai((byte) 1);
        return ResponseEntity.ok(giamGiaRepo.save(voucher));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateVoucher(@PathVariable Integer id, @RequestBody GiamGia voucher) {
        return giamGiaRepo.findById(id).map(existing -> {
            if (voucher.getMaGiamGia() != null) existing.setMaGiamGia(voucher.getMaGiamGia());
            if (voucher.getTenGiamGia() != null) existing.setTenGiamGia(voucher.getTenGiamGia());
            if (voucher.getPhanTramGiam() != null) existing.setPhanTramGiam(voucher.getPhanTramGiam());
            if (voucher.getGioTriGiam() != null) existing.setGioTriGiam(voucher.getGioTriGiam());
            if (voucher.getGiamToiDa() != null) existing.setGiamToiDa(voucher.getGiamToiDa());
            if (voucher.getGiaTriDonToiThieu() != null) existing.setGiaTriDonToiThieu(voucher.getGiaTriDonToiThieu());
            if (voucher.getSoLuong() != null) existing.setSoLuong(voucher.getSoLuong());
            if (voucher.getNgayBatDau() != null) existing.setNgayBatDau(voucher.getNgayBatDau());
            if (voucher.getNgayKetThuc() != null) existing.setNgayKetThuc(voucher.getNgayKetThuc());
            if (voucher.getTrangThai() != null) existing.setTrangThai(voucher.getTrangThai());
            return ResponseEntity.ok(giamGiaRepo.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVoucher(@PathVariable Integer id) {
        giamGiaRepo.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
