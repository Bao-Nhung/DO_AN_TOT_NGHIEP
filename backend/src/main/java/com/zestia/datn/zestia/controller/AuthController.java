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

import java.time.LocalDateTime;
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
        Optional<NhanVien> nvOpt = nhanVienRepo.findByTenNguoiDung(req.getUsername());
        if (nvOpt.isEmpty()) {
            nvOpt = nhanVienRepo.findByEmail(req.getUsername());
        }

        if (nvOpt.isPresent()) {
            NhanVien nv = nvOpt.get();
            if (nv.getTinhTrangLamViec() != null && nv.getTinhTrangLamViec() == 0) {
                return ResponseEntity.status(403).body(Map.of("error", "Tài khoản nhân viên đang bị tạm khoá"));
            }
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
                        .soDienThoai(kh.getSoDienThoai())
                        .role("KhachHang")
                        .userId(kh.getId())
                        .gioiTinh(kh.getGioiTinh())
                        .build());
            }
        }

        return ResponseEntity.status(401).body(Map.of("error", "Sai tài khoản hoặc mật khẩu"));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> req) {
        String hoVaTen = req.get("hoVaTen");
        String email = req.get("email");
        String soDienThoai = req.get("soDienThoai");
        String matKhau = req.get("matKhau");

        if (hoVaTen == null || email == null || matKhau == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Vui lòng nhập đầy đủ thông tin"));
        }

        if (khachHangRepo.existsByEmail(email)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email đã được sử dụng"));
        }

        if (soDienThoai != null && khachHangRepo.existsBySoDienThoai(soDienThoai)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Số điện thoại đã được sử dụng"));
        }

        long count = khachHangRepo.count();
        KhachHang kh = KhachHang.builder()
                .maKhachHang("KH" + String.format("%05d", count + 1))
                .hoVaTen(hoVaTen)
                .email(email)
                .soDienThoai(soDienThoai)
                .matKhau(passwordEncoder.encode(matKhau))
                .ngayTao(LocalDateTime.now())
                .build();

        khachHangRepo.save(kh);

        String token = jwtUtil.generateToken(kh.getEmail(), "KhachHang", kh.getId());
        return ResponseEntity.ok(LoginResponse.builder()
                .token(token)
                .username(kh.getEmail())
                .hoVaTen(kh.getHoVaTen())
                .email(kh.getEmail())
                .soDienThoai(kh.getSoDienThoai())
                .role("KhachHang")
                .userId(kh.getId())
                .build());
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> req) {
        String identifier = req.get("identifier");
        if (identifier == null || identifier.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Vui lòng nhập email hoặc tên đăng nhập"));
        }

        // Check NhanVien
        Optional<NhanVien> nvOpt = nhanVienRepo.findByTenNguoiDung(identifier);
        if (nvOpt.isEmpty()) nvOpt = nhanVienRepo.findByEmail(identifier);
        if (nvOpt.isPresent()) {
            NhanVien nv = nvOpt.get();
            return ResponseEntity.ok(Map.of(
                "found", true,
                "maskedEmail", maskEmail(nv.getEmail()),
                "hint", "Mật khẩu mặc định: 123456. Hãy liên hệ admin để đặt lại."
            ));
        }

        // Check KhachHang
        Optional<KhachHang> khOpt = khachHangRepo.findByEmail(identifier);
        if (khOpt.isEmpty()) khOpt = khachHangRepo.findBySoDienThoai(identifier);
        if (khOpt.isPresent()) {
            KhachHang kh = khOpt.get();
            return ResponseEntity.ok(Map.of(
                "found", true,
                "maskedEmail", maskEmail(kh.getEmail()),
                "hint", "Mật khẩu mặc định: 123456. Hãy liên hệ admin để đặt lại."
            ));
        }

        return ResponseEntity.status(404).body(Map.of("error", "Không tìm thấy tài khoản"));
    }

    private String maskEmail(String email) {
        if (email == null) return "***";
        int at = email.indexOf('@');
        if (at <= 2) return "***" + email.substring(at);
        return email.substring(0, 2) + "***" + email.substring(at);
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestHeader("Authorization") String header,
                                            @RequestBody Map<String, String> body) {
        if (header == null || !header.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        String token = header.substring(7);
        if (!jwtUtil.isValid(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Token hết hạn"));
        }
        var claims = jwtUtil.extractClaims(token);
        String role = (String) claims.get("role");
        Integer userId = ((Number) claims.get("userId")).intValue();

        if ("KhachHang".equals(role)) {
            return khachHangRepo.findById(userId).map(kh -> {
                if (body.containsKey("hoVaTen")) kh.setHoVaTen(body.get("hoVaTen"));
                if (body.containsKey("soDienThoai")) kh.setSoDienThoai(body.get("soDienThoai"));
                if (body.containsKey("gioiTinh")) {
                    String gt = body.get("gioiTinh");
                    kh.setGioiTinh(gt != null && !gt.isEmpty() ? Byte.parseByte(gt) : null);
                }
                khachHangRepo.save(kh);
                return ResponseEntity.ok(Map.of(
                    "hoVaTen", kh.getHoVaTen() != null ? kh.getHoVaTen() : "",
                    "email", kh.getEmail() != null ? kh.getEmail() : "",
                    "soDienThoai", kh.getSoDienThoai() != null ? kh.getSoDienThoai() : "",
                    "gioiTinh", kh.getGioiTinh() != null ? kh.getGioiTinh().toString() : ""
                ));
            }).orElse(ResponseEntity.notFound().build());
        }

        return ResponseEntity.badRequest().body(Map.of("error", "Chức năng này chỉ dành cho khách hàng"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader("Authorization") String header) {
        if (header == null || !header.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        String token = header.substring(7);
        if (!jwtUtil.isValid(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Token hết hạn"));
        }
        var claims = jwtUtil.extractClaims(token);
        return ResponseEntity.ok(Map.of(
            "username", claims.getSubject(),
            "role", claims.get("role"),
            "userId", claims.get("userId")
        ));
    }
}
