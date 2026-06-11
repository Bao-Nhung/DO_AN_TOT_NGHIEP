package com.zestia.datn.zestia.controller;

import com.zestia.datn.zestia.config.JwtUtil;
import com.zestia.datn.zestia.dto.LoginRequest;
import com.zestia.datn.zestia.dto.LoginResponse;
import com.zestia.datn.zestia.entity.KhachHang;
import com.zestia.datn.zestia.entity.NhanVien;
import com.zestia.datn.zestia.repository.KhachHangRepository;
import com.zestia.datn.zestia.repository.NhanVienRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final NhanVienRepository nhanVienRepo;
    private final KhachHangRepository khachHangRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        // Try NhanVien (admin/employee) first — match by tenNguoiDung or email
        Optional<NhanVien> nvOpt = nhanVienRepo.findByTenNguoiDung(req.getUsername());
        if (nvOpt.isEmpty()) {
            nvOpt = nhanVienRepo.findByEmail(req.getUsername());
        }

        if (nvOpt.isPresent()) {
            NhanVien nv = nvOpt.get();
            if (passwordEncoder.matches(req.getPassword(), nv.getMatKhau())) {
                String role = nv.getVaiTro() != null ? nv.getVaiTro().getTenVaiTro() : "NhanVien";
                String token = jwtUtil.generateToken(nv.getTenNguoiDung(), role, nv.getId());
                return ResponseEntity.ok(LoginResponse.builder()
                        .token(token)
                        .username(nv.getTenNguoiDung())
                        .hoVaTen(nv.getHoVaTen())
                        .email(nv.getEmail())
                        .role(role)
                        .userId(nv.getId())
                        .build());
            }
        }

        // Try KhachHang — match by email or soDienThoai
        Optional<KhachHang> khOpt = khachHangRepo.findByEmail(req.getUsername());
        if (khOpt.isEmpty()) {
            khOpt = khachHangRepo.findBySoDienThoai(req.getUsername());
        }

        if (khOpt.isPresent()) {
            KhachHang kh = khOpt.get();
            if (passwordEncoder.matches(req.getPassword(), kh.getMatKhau())) {
                String token = jwtUtil.generateToken(kh.getEmail(), "KhachHang", kh.getId());
                return ResponseEntity.ok(LoginResponse.builder()
                        .token(token)
                        .username(kh.getEmail())
                        .hoVaTen(kh.getHoVaTen())
                        .email(kh.getEmail())
                        .role("KhachHang")
                        .userId(kh.getId())
                        .build());
            }
        }

        return ResponseEntity.status(401).body(Map.of("error", "Sai tai khoan hoac mat khau"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader("Authorization") String header) {
        if (header == null || !header.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        String token = header.substring(7);
        if (!jwtUtil.isValid(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Token het han"));
        }
        var claims = jwtUtil.extractClaims(token);
        return ResponseEntity.ok(Map.of(
            "username", claims.getSubject(),
            "role", claims.get("role"),
            "userId", claims.get("userId")
        ));
    }
}
