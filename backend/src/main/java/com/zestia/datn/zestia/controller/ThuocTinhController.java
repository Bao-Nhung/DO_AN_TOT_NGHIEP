package com.zestia.datn.zestia.controller;

import com.zestia.datn.zestia.entity.*;
import com.zestia.datn.zestia.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/thuoc-tinh")
@RequiredArgsConstructor
public class ThuocTinhController {

    private final MauSacRepository mauSacRepo;
    private final KichThuocRepository kichThuocRepo;
    private final ChatLieuRepository chatLieuRepo;
    private final LoaiVayRepository loaiVayRepo;
    private final NhaCungCapRepository nhaCungCapRepo;

    @GetMapping("/mau-sac")
    public Object getMauSac() { return mauSacRepo.findAll(); }

    @GetMapping("/kich-thuoc")
    public Object getKichThuoc() { return kichThuocRepo.findAll(); }

    @GetMapping("/chat-lieu")
    public Object getChatLieu() { return chatLieuRepo.findAll(); }

    @GetMapping("/loai-vay")
    public Object getLoaiVay() { return loaiVayRepo.findAll(); }

    @GetMapping("/nha-cung-cap")
    public Object getNhaCungCap() { return nhaCungCapRepo.findAll(); }

    @GetMapping
    public Map<String, Object> getAll() {
        return Map.of(
            "mauSac", mauSacRepo.findAll(),
            "kichThuoc", kichThuocRepo.findAll(),
            "chatLieu", chatLieuRepo.findAll(),
            "loaiVay", loaiVayRepo.findAll(),
            "nhaCungCap", nhaCungCapRepo.findAll()
        );
    }

    // --- Màu sắc CRUD ---
    @PostMapping("/mau-sac")
    public ResponseEntity<?> addMauSac(@RequestBody MauSac ms) {
        ms.setNgayTao(LocalDateTime.now());
        if (ms.getTrangThai() == null) ms.setTrangThai((byte) 1);
        return ResponseEntity.ok(mauSacRepo.save(ms));
    }

    @PutMapping("/mau-sac/{id}")
    public ResponseEntity<?> updateMauSac(@PathVariable Integer id, @RequestBody MauSac ms) {
        return mauSacRepo.findById(id).map(existing -> {
            existing.setTenMauSac(ms.getTenMauSac());
            existing.setMaHex(ms.getMaHex());
            if (ms.getTrangThai() != null) existing.setTrangThai(ms.getTrangThai());
            return ResponseEntity.ok(mauSacRepo.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/mau-sac/{id}")
    public ResponseEntity<?> deleteMauSac(@PathVariable Integer id) {
        mauSacRepo.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // --- Kích thước CRUD ---
    @PostMapping("/kich-thuoc")
    public ResponseEntity<?> addKichThuoc(@RequestBody KichThuoc kt) {
        kt.setNgayTao(LocalDateTime.now());
        if (kt.getTrangThai() == null) kt.setTrangThai((byte) 1);
        return ResponseEntity.ok(kichThuocRepo.save(kt));
    }

    @PutMapping("/kich-thuoc/{id}")
    public ResponseEntity<?> updateKichThuoc(@PathVariable Integer id, @RequestBody KichThuoc kt) {
        return kichThuocRepo.findById(id).map(existing -> {
            existing.setTenKichThuoc(kt.getTenKichThuoc());
            if (kt.getMoTa() != null) existing.setMoTa(kt.getMoTa());
            if (kt.getTrangThai() != null) existing.setTrangThai(kt.getTrangThai());
            return ResponseEntity.ok(kichThuocRepo.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/kich-thuoc/{id}")
    public ResponseEntity<?> deleteKichThuoc(@PathVariable Integer id) {
        kichThuocRepo.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // --- Chất liệu CRUD ---
    @PostMapping("/chat-lieu")
    public ResponseEntity<?> addChatLieu(@RequestBody ChatLieu cl) {
        cl.setNgayTao(LocalDateTime.now());
        if (cl.getTrangThai() == null) cl.setTrangThai((byte) 1);
        return ResponseEntity.ok(chatLieuRepo.save(cl));
    }

    @PutMapping("/chat-lieu/{id}")
    public ResponseEntity<?> updateChatLieu(@PathVariable Integer id, @RequestBody ChatLieu cl) {
        return chatLieuRepo.findById(id).map(existing -> {
            existing.setTenChatLieu(cl.getTenChatLieu());
            if (cl.getMoTa() != null) existing.setMoTa(cl.getMoTa());
            if (cl.getTrangThai() != null) existing.setTrangThai(cl.getTrangThai());
            return ResponseEntity.ok(chatLieuRepo.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/chat-lieu/{id}")
    public ResponseEntity<?> deleteChatLieu(@PathVariable Integer id) {
        chatLieuRepo.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // --- Loại váy CRUD ---
    @PostMapping("/loai-vay")
    public ResponseEntity<?> addLoaiVay(@RequestBody LoaiVay lv) {
        lv.setNgayTao(LocalDateTime.now());
        if (lv.getTrangThai() == null) lv.setTrangThai((byte) 1);
        return ResponseEntity.ok(loaiVayRepo.save(lv));
    }

    @PutMapping("/loai-vay/{id}")
    public ResponseEntity<?> updateLoaiVay(@PathVariable Integer id, @RequestBody LoaiVay lv) {
        return loaiVayRepo.findById(id).map(existing -> {
            existing.setTenLoaiVay(lv.getTenLoaiVay());
            if (lv.getMoTa() != null) existing.setMoTa(lv.getMoTa());
            if (lv.getTrangThai() != null) existing.setTrangThai(lv.getTrangThai());
            return ResponseEntity.ok(loaiVayRepo.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/loai-vay/{id}")
    public ResponseEntity<?> deleteLoaiVay(@PathVariable Integer id) {
        loaiVayRepo.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // --- Nhà cung cấp CRUD ---
    @PostMapping("/nha-cung-cap")
    public ResponseEntity<?> addNhaCungCap(@RequestBody NhaCungCap ncc) {
        ncc.setNgayTao(LocalDateTime.now());
        if (ncc.getTrangThai() == null) ncc.setTrangThai((byte) 1);
        return ResponseEntity.ok(nhaCungCapRepo.save(ncc));
    }

    @PutMapping("/nha-cung-cap/{id}")
    public ResponseEntity<?> updateNhaCungCap(@PathVariable Integer id, @RequestBody NhaCungCap ncc) {
        return nhaCungCapRepo.findById(id).map(existing -> {
            existing.setTenNhaCungCap(ncc.getTenNhaCungCap());
            if (ncc.getDiaChi() != null) existing.setDiaChi(ncc.getDiaChi());
            if (ncc.getSoDienThoai() != null) existing.setSoDienThoai(ncc.getSoDienThoai());
            if (ncc.getEmail() != null) existing.setEmail(ncc.getEmail());
            if (ncc.getMoTa() != null) existing.setMoTa(ncc.getMoTa());
            if (ncc.getTrangThai() != null) existing.setTrangThai(ncc.getTrangThai());
            return ResponseEntity.ok(nhaCungCapRepo.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/nha-cung-cap/{id}")
    public ResponseEntity<?> deleteNhaCungCap(@PathVariable Integer id) {
        nhaCungCapRepo.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
