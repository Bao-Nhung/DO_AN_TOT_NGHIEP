package com.zestia.datn.zestia.controller;

import com.zestia.datn.zestia.entity.LichLamViec;
import com.zestia.datn.zestia.entity.NhanVien;
import com.zestia.datn.zestia.repository.LichLamViecRepository;
import com.zestia.datn.zestia.repository.NhanVienRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lich-lam-viec")
@RequiredArgsConstructor
public class LichLamViecController {

    private final LichLamViecRepository lichLamViecRepo;
    private final NhanVienRepository nhanVienRepo;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAll(@RequestParam(required = false) String from,
                                            @RequestParam(required = false) String to) {
        try {
            List<LichLamViec> items;
            if (from != null && to != null) {
                items = lichLamViecRepo.findByNgayLamViecBetweenAndTrangThaiXoaFalseOrderByNgayLamViecAscGioBatDauAsc(
                        LocalDate.parse(from), LocalDate.parse(to));
            } else {
                items = lichLamViecRepo.findByTrangThaiXoaFalseOrderByNgayLamViecAscGioBatDauAsc();
            }
            return ResponseEntity.ok(items.stream().map(this::toMap).toList());
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Định dạng ngày không hợp lệ (YYYY-MM-DD)"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMySchedule(@RequestParam(required = false) String from,
                                           @RequestParam(required = false) String to) {
        try {
            List<LichLamViec> items;
            if (from != null && to != null) {
                items = lichLamViecRepo.findByNgayLamViecBetweenAndTrangThaiXoaFalseOrderByNgayLamViecAscGioBatDauAsc(
                        LocalDate.parse(from), LocalDate.parse(to));
            } else {
                items = lichLamViecRepo.findByTrangThaiXoaFalseOrderByNgayLamViecAscGioBatDauAsc();
            }
            return ResponseEntity.ok(items.stream().map(this::toMap).toList());
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Định dạng ngày không hợp lệ (YYYY-MM-DD)"));
        }
    }

    @GetMapping("/nhan-vien")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Map<String, Object>> getNhanVien() {
        return nhanVienRepo.findAll().stream().map(nv -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", nv.getId());
            map.put("maNhanVien", nv.getMaNhanVien());
            map.put("hoVaTen", nv.getHoVaTen());
            map.put("email", nv.getEmail());
            map.put("soDienThoai", nv.getSoDienThoai());
            map.put("vaiTro", nv.getVaiTro() != null ? nv.getVaiTro().getTenVaiTro() : null);
            map.put("tinhTrangLamViec", nv.getTinhTrangLamViec());
            return map;
        }).toList();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        ResponseEntity<?> invalid = validate(body);
        if (invalid != null) return invalid;

        try {
            LichLamViec lich = new LichLamViec();
            applyBody(lich, body);
            lich.setNgayTao(LocalDateTime.now());
            lich.setNgayCapNhat(LocalDateTime.now());
            if (lich.getTrangThai() == null) lich.setTrangThai((byte) 1);
            lich.setTrangThaiXoa(false);
            return ResponseEntity.ok(toMap(lichLamViecRepo.save(lich)));
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Định dạng dữ liệu không hợp lệ"));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        ResponseEntity<?> invalid = validate(body);
        if (invalid != null) return invalid;

        return lichLamViecRepo.findById(id).map(existing -> {
            try {
                if (existing.getTrangThaiXoa()) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Lịch này đã bị xóa"));
                }
                applyBody(existing, body);
                existing.setNgayCapNhat(LocalDateTime.now());
                return ResponseEntity.ok(toMap(lichLamViecRepo.save(existing)));
            } catch (DateTimeParseException e) {
                return ResponseEntity.badRequest().body(Map.of("error", "Định dạng dữ liệu không hợp lệ"));
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        return lichLamViecRepo.findById(id).map(lich -> {
            lich.setTrangThaiXoa(true);
            lich.setNgayCapNhat(LocalDateTime.now());
            lichLamViecRepo.save(lich);
            return ResponseEntity.ok(Map.of("message", "Xóa lịch làm việc thành công"));
        }).orElse(ResponseEntity.notFound().build());
    }

    private ResponseEntity<?> validate(Map<String, Object> body) {
        Integer nhanVienId = toInteger(body.get("nhanVienId"));
        if (nhanVienId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Vui lòng chọn nhân viên"));
        }
        if (body.get("ngayLamViec") == null || String.valueOf(body.get("ngayLamViec")).isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Vui lòng chọn ngày làm việc"));
        }
        if (!nhanVienRepo.existsById(nhanVienId)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Nhân viên không tồn tại"));
        }
        
        try {
            LocalDate ngay = LocalDate.parse(String.valueOf(body.get("ngayLamViec")));
            if (ngay.isBefore(LocalDate.now())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Không được phép tạo lịch quá khứ"));
            }
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Định dạng ngày không hợp lệ (YYYY-MM-DD)"));
        }
        
        return null;
    }

    private void applyBody(LichLamViec lich, Map<String, Object> body) {
        Integer nhanVienId = toInteger(body.get("nhanVienId"));
        NhanVien nv = nhanVienRepo.findById(nhanVienId)
            .orElseThrow(() -> new RuntimeException("Nhân viên không tồn tại"));
        lich.setNhanVien(nv);
        lich.setNgayLamViec(LocalDate.parse(String.valueOf(body.get("ngayLamViec"))));
        lich.setCaLamViec(toString(body.get("caLamViec")));
        lich.setGioBatDau(toTime(body.get("gioBatDau")));
        lich.setGioKetThuc(toTime(body.get("gioKetThuc")));
        lich.setViTri(toString(body.get("viTri")));
        lich.setGhiChu(toString(body.get("ghiChu")));
        Byte trangThai = toByte(body.get("trangThai"));
        lich.setTrangThai(trangThai != null ? trangThai : (byte) 1);
    }

    private Map<String, Object> toMap(LichLamViec lich) {
        NhanVien nv = lich.getNhanVien();
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", lich.getId());
        map.put("nhanVienId", nv != null ? nv.getId() : null);
        map.put("maNhanVien", nv != null ? nv.getMaNhanVien() : null);
        map.put("tenNhanVien", nv != null ? nv.getHoVaTen() : null);
        map.put("vaiTro", nv != null && nv.getVaiTro() != null ? nv.getVaiTro().getTenVaiTro() : null);
        map.put("ngayLamViec", lich.getNgayLamViec());
        map.put("caLamViec", lich.getCaLamViec());
        map.put("gioBatDau", lich.getGioBatDau());
        map.put("gioKetThuc", lich.getGioKetThuc());
        map.put("viTri", lich.getViTri());
        map.put("ghiChu", lich.getGhiChu());
        map.put("trangThai", lich.getTrangThai());
        map.put("ngayTao", lich.getNgayTao());
        return map;
    }

    private String toString(Object value) {
        if (value == null) return null;
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }

    private LocalTime toTime(Object value) {
        String text = toString(value);
        return text == null ? null : LocalTime.parse(text);
    }

    private Integer toInteger(Object value) {
        if (value == null) return null;
        if (value instanceof Number number) return number.intValue();
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : Integer.parseInt(text);
    }

    private Byte toByte(Object value) {
        if (value == null) return null;
        if (value instanceof Number number) return number.byteValue();
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : Byte.parseByte(text);
    }
}
