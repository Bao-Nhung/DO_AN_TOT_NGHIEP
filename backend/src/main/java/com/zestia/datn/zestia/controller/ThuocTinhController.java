package com.zestia.datn.zestia.controller;

import com.zestia.datn.zestia.entity.*;
import com.zestia.datn.zestia.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/thuoc-tinh")
@RequiredArgsConstructor
public class ThuocTinhController {

    private final MauSacRepository mauSacRepo;
    private final KichThuocRepository kichThuocRepo;
    private final ChatLieuRepository chatLieuRepo;
    private final LoaiSanPhamRepository loaiSanPhamRepo;
    private final NhaCungCapRepository nhaCungCapRepo;

    @GetMapping("/mau-sac")
    public List<Map<String, Object>> getMauSac() {
        return mauSacRepo.findAll().stream().map(this::toMap).toList();
    }

    @GetMapping("/kich-thuoc")
    public List<Map<String, Object>> getKichThuoc() {
        return kichThuocRepo.findAll().stream().map(this::toMap).toList();
    }

    @GetMapping("/chat-lieu")
    public List<Map<String, Object>> getChatLieu() {
        return chatLieuRepo.findAll().stream().map(this::toMap).toList();
    }

    @GetMapping({"/loai-san-pham", "/loai-vay"})
    public List<Map<String, Object>> getLoaiSanPham() {
        return loaiSanPhamRepo.findAll().stream().map(this::toMap).toList();
    }

    @GetMapping("/nha-cung-cap")
    public Object getNhaCungCap() { return nhaCungCapRepo.findAll(); }

    @GetMapping
    public Map<String, Object> getAll() {
        List<Map<String, Object>> categories = loaiSanPhamRepo.findAll().stream().map(this::toMap).toList();
        return Map.of(
            "mauSac", mauSacRepo.findAll().stream().map(this::toMap).toList(),
            "kichThuoc", kichThuocRepo.findAll().stream().map(this::toMap).toList(),
            "chatLieu", chatLieuRepo.findAll().stream().map(this::toMap).toList(),
            "loaiSanPham", categories,
            "loaiVay", categories,
            "nhaCungCap", nhaCungCapRepo.findAll()
        );
    }

    private Map<String, Object> toMap(MauSac item) {
        Map<String, Object> map = commonMap(item.getId(), item.getTrangThai(), item.getNgayTao());
        map.put("tenMauSac", item.getTenMauSac());
        map.put("maHex", item.getMaHex());
        return map;
    }

    private Map<String, Object> toMap(KichThuoc item) {
        Map<String, Object> map = commonMap(item.getId(), item.getTrangThai(), item.getNgayTao());
        map.put("tenKichThuoc", item.getTenKichThuoc());
        map.put("moTa", item.getMoTa());
        return map;
    }

    private Map<String, Object> toMap(ChatLieu item) {
        Map<String, Object> map = commonMap(item.getId(), item.getTrangThai(), item.getNgayTao());
        map.put("tenChatLieu", item.getTenChatLieu());
        map.put("moTa", item.getMoTa());
        return map;
    }

    private Map<String, Object> toMap(LoaiSanPham item) {
        Map<String, Object> map = commonMap(item.getId(), item.getTrangThai(), item.getNgayTao());
        map.put("tenLoaiSanPham", item.getTenLoaiSanPham());
        map.put("tenLoaiVay", item.getTenLoaiSanPham());
        map.put("moTa", item.getMoTa());
        return map;
    }

    private Map<String, Object> commonMap(Integer id, Byte status, LocalDateTime createdAt) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", id);
        map.put("trangThai", status);
        map.put("ngayTao", createdAt);
        return map;
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
        return mauSacRepo.findById(id).map(item -> {
            item.setTrangThai((byte) 0);
            return ResponseEntity.ok(mauSacRepo.save(item));
        }).orElse(ResponseEntity.notFound().build());
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
        return kichThuocRepo.findById(id).map(item -> {
            item.setTrangThai((byte) 0);
            return ResponseEntity.ok(kichThuocRepo.save(item));
        }).orElse(ResponseEntity.notFound().build());
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
        return chatLieuRepo.findById(id).map(item -> {
            item.setTrangThai((byte) 0);
            return ResponseEntity.ok(chatLieuRepo.save(item));
        }).orElse(ResponseEntity.notFound().build());
    }

    // --- Loại sản phẩm CRUD ---
    @PostMapping({"/loai-san-pham", "/loai-vay"})
    public ResponseEntity<?> addLoaiSanPham(@RequestBody LoaiSanPham lsp) {
        lsp.setNgayTao(LocalDateTime.now());
        if (lsp.getTrangThai() == null) lsp.setTrangThai((byte) 1);
        return ResponseEntity.ok(loaiSanPhamRepo.save(lsp));
    }

    @PutMapping({"/loai-san-pham/{id}", "/loai-vay/{id}"})
    public ResponseEntity<?> updateLoaiSanPham(@PathVariable Integer id, @RequestBody LoaiSanPham lsp) {
        return loaiSanPhamRepo.findById(id).map(existing -> {
            existing.setTenLoaiSanPham(lsp.getTenLoaiSanPham());
            if (lsp.getMoTa() != null) existing.setMoTa(lsp.getMoTa());
            if (lsp.getTrangThai() != null) existing.setTrangThai(lsp.getTrangThai());
            return ResponseEntity.ok(loaiSanPhamRepo.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping({"/loai-san-pham/{id}", "/loai-vay/{id}"})
    public ResponseEntity<?> deleteLoaiSanPham(@PathVariable Integer id) {
        return loaiSanPhamRepo.findById(id).map(item -> {
            item.setTrangThai((byte) 0);
            return ResponseEntity.ok(loaiSanPhamRepo.save(item));
        }).orElse(ResponseEntity.notFound().build());
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
        return nhaCungCapRepo.findById(id).map(item -> {
            item.setTrangThai((byte) 0);
            return ResponseEntity.ok(nhaCungCapRepo.save(item));
        }).orElse(ResponseEntity.notFound().build());
    }
}
