package com.zestia.datn.zestia.controller;

import com.zestia.datn.zestia.config.JwtUtil;
import com.zestia.datn.zestia.dto.LoginRequest;
import com.zestia.datn.zestia.dto.LoginResponse;
import com.zestia.datn.zestia.entity.DiaChi;
import com.zestia.datn.zestia.entity.KhachHang;
import com.zestia.datn.zestia.entity.NhanVien;
import com.zestia.datn.zestia.entity.PasswordResetToken;
import com.zestia.datn.zestia.repository.DiaChiRepository;
import com.zestia.datn.zestia.repository.KhachHangRepository;
import com.zestia.datn.zestia.repository.NhanVienRepository;
import com.zestia.datn.zestia.repository.PasswordResetTokenRepository;
import com.zestia.datn.zestia.service.EmailService;
import com.zestia.datn.zestia.service.GoogleAuthService;
import com.zestia.datn.zestia.service.CustomerIdentityService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Objects;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String ACCOUNT_NHAN_VIEN = "NHAN_VIEN";
    private static final String ACCOUNT_KHACH_HANG = "KHACH_HANG";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);
    private static final Pattern PHONE_PATTERN = Pattern.compile("0[35789]\\d{8}");

    private final NhanVienRepository nhanVienRepo;
    private final KhachHangRepository khachHangRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final DiaChiRepository diaChiRepo;
    private final EmailService emailService;
    private final PasswordResetTokenRepository passwordResetTokenRepo;
    private final GoogleAuthService googleAuthService;
    private final CustomerIdentityService customerIdentityService;

    @Value("${app.password-reset-token-minutes:30}")
    private long passwordResetTokenMinutes;

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
        String hoVaTen = clean(req.get("hoVaTen"));
        String email = customerIdentityService.normalizeEmail(req.get("email"));
        String soDienThoai = customerIdentityService.normalizePhone(req.get("soDienThoai"));
        String matKhau = req.get("matKhau");

        if (hoVaTen == null || email == null || soDienThoai == null || matKhau == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Vui lòng nhập đầy đủ thông tin"));
        }
        if (hoVaTen.length() < 2 || hoVaTen.length() > 150) {
            return ResponseEntity.badRequest().body(Map.of("error", "Họ tên không hợp lệ"));
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email không hợp lệ"));
        }
        if (!PHONE_PATTERN.matcher(soDienThoai).matches()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Số điện thoại không hợp lệ"));
        }
        if (matKhau.length() < 6 || matKhau.length() > 100) {
            return ResponseEntity.badRequest().body(Map.of("error", "Mật khẩu phải có từ 6 đến 100 ký tự"));
        }

        Optional<KhachHang> byEmail = khachHangRepo.findByEmailIgnoreCase(email);
        Optional<KhachHang> byPhone = khachHangRepo.findBySoDienThoai(soDienThoai);
        if (byEmail.isPresent() && byPhone.isPresent()
                && !Objects.equals(byEmail.get().getId(), byPhone.get().getId())) {
            return ResponseEntity.status(409).body(Map.of("error", "Email và số điện thoại đang thuộc hai hồ sơ khác nhau"));
        }
        KhachHang existing = byEmail.or(() -> byPhone).orElse(null);
        if (existing != null && existing.getMatKhau() != null && !existing.getMatKhau().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email hoặc số điện thoại đã được sử dụng"));
        }
        if (existing != null && existing.getEmail() != null
                && !existing.getEmail().equalsIgnoreCase(email)) {
            return ResponseEntity.status(409).body(Map.of("error", "Số điện thoại đã gắn với một email khác"));
        }

        KhachHang kh;
        try {
            kh = customerIdentityService.resolveForOrder(null, hoVaTen, soDienThoai, email);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(Map.of("error", e.getMessage()));
        }
        kh.setHoVaTen(hoVaTen);
        kh.setEmail(email);
        kh.setSoDienThoai(soDienThoai);
        kh.setMatKhau(passwordEncoder.encode(matKhau));
        kh = khachHangRepo.save(kh);

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
    public ResponseEntity<?> requestPasswordReset(@RequestBody Map<String, String> req) {
        String identifier = req.get("identifier");
        if (identifier == null || identifier.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Vui lòng nhập email hoặc tên đăng nhập"));
        }
        identifier = identifier.trim();

        Optional<NhanVien> nvOpt = nhanVienRepo.findByTenNguoiDung(identifier);
        if (nvOpt.isEmpty()) nvOpt = nhanVienRepo.findByEmail(identifier);
        if (nvOpt.isPresent()) {
            issuePasswordResetForEmployee(nvOpt.get());
            return passwordResetAccepted();
        }

        Optional<KhachHang> khOpt = khachHangRepo.findByEmail(identifier);
        if (khOpt.isEmpty()) khOpt = khachHangRepo.findBySoDienThoai(identifier);
        khOpt.ifPresent(this::issuePasswordResetForCustomer);

        return passwordResetAccepted();
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> req) {
        try {
            KhachHang kh = googleAuthService.authenticate(req.get("credential"));
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
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> req) {
        String token = req.get("token");
        String newPassword = firstNonBlank(req.get("newPassword"), req.get("password"), req.get("matKhau"));
        if (token == null || token.isBlank() || newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Vui lòng nhập đầy đủ token và mật khẩu mới"));
        }
        if (newPassword.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("error", "Mật khẩu mới phải có tối thiểu 6 ký tự"));
        }

        Optional<PasswordResetToken> resetTokenOpt = passwordResetTokenRepo.findByTokenAndUsedAtIsNull(token.trim());
        if (resetTokenOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Liên kết đặt lại mật khẩu không hợp lệ hoặc đã được sử dụng"));
        }

        PasswordResetToken resetToken = resetTokenOpt.get();
        LocalDateTime now = LocalDateTime.now();
        if (resetToken.getExpiresAt() == null || resetToken.getExpiresAt().isBefore(now)) {
            resetToken.setUsedAt(now);
            passwordResetTokenRepo.save(resetToken);
            return ResponseEntity.badRequest().body(Map.of("error", "Liên kết đặt lại mật khẩu đã hết hạn"));
        }

        String encodedPassword = passwordEncoder.encode(newPassword);
        if (ACCOUNT_NHAN_VIEN.equals(resetToken.getAccountType())) {
            Optional<NhanVien> nvOpt = nhanVienRepo.findById(resetToken.getAccountId());
            if (nvOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Tài khoản không còn tồn tại"));
            }
            NhanVien nv = nvOpt.get();
            nv.setMatKhau(encodedPassword);
            nhanVienRepo.save(nv);
        } else if (ACCOUNT_KHACH_HANG.equals(resetToken.getAccountType())) {
            Optional<KhachHang> khOpt = khachHangRepo.findById(resetToken.getAccountId());
            if (khOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Tài khoản không còn tồn tại"));
            }
            KhachHang kh = khOpt.get();
            kh.setMatKhau(encodedPassword);
            khachHangRepo.save(kh);
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "Loại tài khoản không hợp lệ"));
        }

        resetToken.setUsedAt(now);
        passwordResetTokenRepo.save(resetToken);
        return ResponseEntity.ok(Map.of("message", "Đặt lại mật khẩu thành công. Bạn có thể đăng nhập bằng mật khẩu mới."));
    }

    private ResponseEntity<Map<String, String>> passwordResetAccepted() {
        return ResponseEntity.ok(Map.of(
                "message",
                "Nếu tài khoản tồn tại và có email, hệ thống đã gửi hướng dẫn đặt lại mật khẩu."
        ));
    }

    private void issuePasswordResetForEmployee(NhanVien nv) {
        if (nv == null || isBlank(nv.getEmail())) return;
        PasswordResetToken token = createPasswordResetToken(ACCOUNT_NHAN_VIEN, nv.getId());
        emailService.sendPasswordResetEmail(nv.getEmail(), displayName(nv.getHoVaTen(), nv.getTenNguoiDung()), token.getToken(), token.getExpiresAt());
    }

    private void issuePasswordResetForCustomer(KhachHang kh) {
        if (kh == null || isBlank(kh.getEmail())) return;
        PasswordResetToken token = createPasswordResetToken(ACCOUNT_KHACH_HANG, kh.getId());
        emailService.sendPasswordResetEmail(kh.getEmail(), displayName(kh.getHoVaTen(), kh.getEmail()), token.getToken(), token.getExpiresAt());
    }

    private PasswordResetToken createPasswordResetToken(String accountType, Integer accountId) {
        LocalDateTime now = LocalDateTime.now();
        List<PasswordResetToken> activeTokens = passwordResetTokenRepo.findByAccountTypeAndAccountIdAndUsedAtIsNull(accountType, accountId);
        activeTokens.forEach(activeToken -> activeToken.setUsedAt(now));
        passwordResetTokenRepo.saveAll(activeTokens);

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(generateSecureToken())
                .accountType(accountType)
                .accountId(accountId)
                .expiresAt(now.plusMinutes(passwordResetTokenMinutes))
                .createdAt(now)
                .build();
        return passwordResetTokenRepo.save(resetToken);
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String firstNonBlank(String... values) {
        if (values == null) return null;
        for (String value : values) {
            if (value != null && !value.isBlank()) return value.trim();
        }
        return null;
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String displayName(String primary, String fallback) {
        if (!isBlank(primary)) return primary.trim();
        if (!isBlank(fallback)) return fallback.trim();
        return "Zestia member";
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
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

    @GetMapping("/profile/address")
    public ResponseEntity<?> getProfileAddress(@RequestHeader("Authorization") String header) {
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
            Optional<DiaChi> dcOpt = diaChiRepo.findByKhachHangIdAndMacDinh(userId, (byte) 1);
            if (dcOpt.isPresent()) {
                DiaChi dc = dcOpt.get();
                return ResponseEntity.ok(Map.of(
                    "tinhThanhPho", dc.getTinhThanhPho() != null ? dc.getTinhThanhPho() : "",
                    "quanHuyen", dc.getQuanHuyen() != null ? dc.getQuanHuyen() : "",
                    "xaPhuong", dc.getXaPhuong() != null ? dc.getXaPhuong() : "",
                    "duong", dc.getDuong() != null ? dc.getDuong() : ""
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                    "tinhThanhPho", "",
                    "quanHuyen", "",
                    "xaPhuong", "",
                    "duong", ""
                ));
            }
        }
        return ResponseEntity.badRequest().body(Map.of("error", "Chức năng này chỉ dành cho khách hàng"));
    }

    @PutMapping("/profile/address")
    public ResponseEntity<?> updateProfileAddress(@RequestHeader("Authorization") String header,
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
                String tinhThanhPho = body.get("tinhThanhPho");
                String quanHuyen = body.get("quanHuyen");
                String xaPhuong = body.get("xaPhuong");
                String duong = body.get("duong");

                // Validate
                if (tinhThanhPho == null || tinhThanhPho.isBlank() ||
                    quanHuyen == null || quanHuyen.isBlank() ||
                    xaPhuong == null || xaPhuong.isBlank() ||
                    duong == null || duong.isBlank()) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Vui lòng nhập đầy đủ thông tin địa chỉ"));
                }

                DiaChi dc = diaChiRepo.findByKhachHangIdAndMacDinh(userId, (byte) 1)
                        .orElseGet(() -> DiaChi.builder()
                                .khachHang(kh)
                                .macDinh((byte) 1)
                                .build());

                dc.setTinhThanhPho(tinhThanhPho);
                dc.setQuanHuyen(quanHuyen);
                dc.setXaPhuong(xaPhuong);
                dc.setDuong(duong);

                diaChiRepo.save(dc);
                return ResponseEntity.ok(Map.of("message", "Cập nhật địa chỉ thành công"));
            }).orElse(ResponseEntity.notFound().build());
        }
        return ResponseEntity.badRequest().body(Map.of("error", "Chức năng này chỉ dành cho khách hàng"));
    }
}
