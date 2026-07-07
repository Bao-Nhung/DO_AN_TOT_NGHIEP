package com.zestia.datn.zestia.controller;

import com.zestia.datn.zestia.entity.LichLamViec;
import com.zestia.datn.zestia.entity.NhanVien;
import com.zestia.datn.zestia.config.JwtUtil;
import com.zestia.datn.zestia.repository.LichLamViecRepository;
import com.zestia.datn.zestia.repository.NhanVienRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lich-lam-viec")
@RequiredArgsConstructor
public class LichLamViecController {

    private final LichLamViecRepository lichLamViecRepo;
    private final NhanVienRepository nhanVienRepo;
    private final JwtUtil jwtUtil;

    @GetMapping
    public List<Map<String, Object>> getAll(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Integer nhanVienId,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        Integer tokenUserId = extractStaffUserId(authHeader);
        if (!isAdmin(authHeader) && tokenUserId != null) {
            nhanVienId = tokenUserId;
        }

        List<LichLamViec> data;
        if (nhanVienId != null) {
            data = lichLamViecRepo.findByNhanVienIdOrderByNgayLamAscGioBatDauAsc(nhanVienId);
        } else if (startDate != null && endDate != null) {
            data = lichLamViecRepo.findByNgayLamBetweenOrderByNgayLamAscGioBatDauAsc(startDate, endDate);
        } else {
            data = lichLamViecRepo.findAll();
        }

        return data.stream()
                .filter(item -> startDate == null || !item.getNgayLam().isBefore(startDate))
                .filter(item -> endDate == null || !item.getNgayLam().isAfter(endDate))
                .map(this::toMap)
                .toList();
    }

    private Integer extractStaffUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        try {
            String token = authHeader.substring(7);
            if (!jwtUtil.isValid(token)) return null;
            return toInt(jwtUtil.extractClaims(token).get("userId"));
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isAdmin(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return false;
        try {
            String token = authHeader.substring(7);
            if (!jwtUtil.isValid(token)) return false;
            String role = jwtUtil.extractClaims(token).get("role", String.class);
            return "Admin".equals(role);
        } catch (Exception e) {
            return false;
        }
    }

    private Integer toInt(Object value) {
        if (value == null) return null;
        if (value instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @GetMapping("/nhan-vien")
    public List<Map<String, Object>> getNhanVien() {
        return nhanVienRepo.findAll().stream()
                .filter(nv -> nv.getTinhTrangLamViec() == null || nv.getTinhTrangLamViec() == 1)
                .map(nv -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", nv.getId());
                    map.put("maNhanVien", nv.getMaNhanVien());
                    map.put("hoVaTen", nv.getHoVaTen());
                    map.put("email", nv.getEmail());
                    map.put("soDienThoai", nv.getSoDienThoai());
                    return map;
                })
                .toList();
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody LichLamViec lich) {
        if (lich.getNhanVien() == null || lich.getNhanVien().getId() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Vui lòng chọn nhân viên"));
        }
        NhanVien nhanVien = nhanVienRepo.findById(lich.getNhanVien().getId()).orElse(null);
        if (nhanVien == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Nhân viên không tồn tại"));
        }
        lich.setNhanVien(nhanVien);
        lich.setNgayTao(LocalDateTime.now());
        if (lich.getTrangThai() == null) lich.setTrangThai((byte) 1);
        return ResponseEntity.ok(toMap(lichLamViecRepo.save(lich)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody LichLamViec lich) {
        return lichLamViecRepo.findById(id).map(existing -> {
            if (lich.getNhanVien() != null && lich.getNhanVien().getId() != null) {
                nhanVienRepo.findById(lich.getNhanVien().getId()).ifPresent(existing::setNhanVien);
            }
            if (lich.getNgayLam() != null) existing.setNgayLam(lich.getNgayLam());
            if (lich.getCaLam() != null) existing.setCaLam(lich.getCaLam());
            if (lich.getGioBatDau() != null) existing.setGioBatDau(lich.getGioBatDau());
            if (lich.getGioKetThuc() != null) existing.setGioKetThuc(lich.getGioKetThuc());
            if (lich.getGhiChu() != null) existing.setGhiChu(lich.getGhiChu());
            if (lich.getTrangThai() != null) existing.setTrangThai(lich.getTrangThai());
            return ResponseEntity.ok(toMap(lichLamViecRepo.save(existing)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        lichLamViecRepo.deleteById(id);
        return ResponseEntity.ok().build();
    }

    private Map<String, Object> toMap(LichLamViec lich) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", lich.getId());
        map.put("ngayLam", lich.getNgayLam());
        map.put("caLam", lich.getCaLam());
        map.put("gioBatDau", lich.getGioBatDau());
        map.put("gioKetThuc", lich.getGioKetThuc());
        map.put("ghiChu", lich.getGhiChu());
        map.put("trangThai", lich.getTrangThai());
        map.put("ngayTao", lich.getNgayTao());
        if (lich.getNhanVien() != null) {
            map.put("nhanVienId", lich.getNhanVien().getId());
            map.put("maNhanVien", lich.getNhanVien().getMaNhanVien());
            map.put("tenNhanVien", lich.getNhanVien().getHoVaTen());
            map.put("emailNhanVien", lich.getNhanVien().getEmail());
        }
        return map;
    }
}
