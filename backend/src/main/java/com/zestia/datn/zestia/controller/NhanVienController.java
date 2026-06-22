package com.zestia.datn.zestia.controller;

import com.zestia.datn.zestia.entity.NhanVien;
import com.zestia.datn.zestia.entity.VaiTro;
import com.zestia.datn.zestia.repository.NhanVienRepository;
import com.zestia.datn.zestia.repository.VaiTroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/nhan-vien")
@RequiredArgsConstructor
public class NhanVienController {

    private final NhanVienRepository nhanVienRepo;
    private final VaiTroRepository vaiTroRepo;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public List<Map<String, Object>> getAll() {
        return nhanVienRepo.findAll().stream().map(this::toMap).toList();
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        String hoVaTen = asString(body.get("hoVaTen"));
        String email = asString(body.get("email"));
        String tenNguoiDung = asString(body.get("username"));
        String matKhau = asString(body.get("password"));
        String soDienThoai = asString(body.get("phone"));
        String maNhanVien = asString(body.get("maNhanVien"));
        String roleName = asString(body.get("role"));

        if (hoVaTen == null || email == null || tenNguoiDung == null || matKhau == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Vui lòng nhập đầy đủ họ tên, email, tên đăng nhập và mật khẩu"));
        }

        if (nhanVienRepo.existsByEmail(email)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email đã tồn tại"));
        }
        if (nhanVienRepo.existsByTenNguoiDung(tenNguoiDung)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Tên đăng nhập đã tồn tại"));
        }
        if (maNhanVien != null && nhanVienRepo.existsByMaNhanVien(maNhanVien)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Mã nhân viên đã tồn tại"));
        }

        if (maNhanVien == null || maNhanVien.isBlank()) {
            long count = nhanVienRepo.count();
            maNhanVien = "NV" + String.format("%05d", count + 1);
        }

        NhanVien nv = NhanVien.builder()
                .maNhanVien(maNhanVien)
                .hoVaTen(hoVaTen)
                .email(email)
                .tenNguoiDung(tenNguoiDung)
                .soDienThoai(soDienThoai)
                .matKhau(passwordEncoder.encode(matKhau))
                .tinhTrangLamViec((byte) 1)
                .ngayTao(LocalDateTime.now())
                .build();

        if (roleName != null && !roleName.isBlank()) {
            Optional<VaiTro> roleOpt = vaiTroRepo.findByTenVaiTro(roleName);
            if (roleOpt.isPresent()) {
                nv.setVaiTro(roleOpt.get());
            }
        }

        NhanVien saved = nhanVienRepo.save(nv);
        return ResponseEntity.ok(toMap(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        return nhanVienRepo.findById(id).map(existing -> {
            String hoVaTen = asString(body.get("hoVaTen"));
            String email = asString(body.get("email"));
            String tenNguoiDung = asString(body.get("username"));
            String matKhau = asString(body.get("password"));
            String soDienThoai = asString(body.get("phone"));
            String roleName = asString(body.get("role"));
            Number statusNumber = (Number) body.get("status");

            if (email != null && !email.equals(existing.getEmail()) && nhanVienRepo.existsByEmail(email)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email đã tồn tại"));
            }
            if (tenNguoiDung != null && !tenNguoiDung.equals(existing.getTenNguoiDung()) && nhanVienRepo.existsByTenNguoiDung(tenNguoiDung)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Tên đăng nhập đã tồn tại"));
            }

            if (hoVaTen != null) existing.setHoVaTen(hoVaTen);
            if (email != null) existing.setEmail(email);
            if (tenNguoiDung != null) existing.setTenNguoiDung(tenNguoiDung);
            if (soDienThoai != null) existing.setSoDienThoai(soDienThoai);
            if (matKhau != null && !matKhau.isBlank()) {
                existing.setMatKhau(passwordEncoder.encode(matKhau));
            }
            if (roleName != null && !roleName.isBlank()) {
                vaiTroRepo.findByTenVaiTro(roleName).ifPresent(existing::setVaiTro);
            }
            if (statusNumber != null) {
                existing.setTinhTrangLamViec(statusNumber.byteValue());
            }

            NhanVien updated = nhanVienRepo.save(existing);
            return ResponseEntity.ok(toMap(updated));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        Number statusNumber = (Number) body.get("status");
        if (statusNumber == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Trạng thái không hợp lệ"));
        }
        return nhanVienRepo.findById(id).map(existing -> {
            existing.setTinhTrangLamViec(statusNumber.byteValue());
            return ResponseEntity.ok(toMap(nhanVienRepo.save(existing)));
        }).orElse(ResponseEntity.notFound().build());
    }

    private Map<String, Object> toMap(NhanVien nv) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", nv.getId());
        map.put("maNhanVien", nv.getMaNhanVien());
        map.put("hoVaTen", nv.getHoVaTen());
        map.put("username", nv.getTenNguoiDung());
        map.put("email", nv.getEmail());
        map.put("phone", nv.getSoDienThoai());
        map.put("status", nv.getTinhTrangLamViec());
        map.put("role", nv.getVaiTro() != null ? nv.getVaiTro().getTenVaiTro() : "NhanVien");
        map.put("ngayTao", nv.getNgayTao());
        return map;
    }

    private String asString(Object value) {
        return value != null ? value.toString().trim() : null;
    }
}
