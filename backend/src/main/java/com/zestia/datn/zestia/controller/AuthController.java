package com.zestia.datn.zestia.controller;

import com.zestia.datn.zestia.config.JwtUtil;
import com.zestia.datn.zestia.dto.LoginRequest;
import com.zestia.datn.zestia.dto.LoginResponse;
import com.zestia.datn.zestia.entity.KhachHang;
import com.zestia.datn.zestia.entity.NhanVien;
import com.zestia.datn.zestia.entity.PasswordResetToken;
import com.zestia.datn.zestia.repository.KhachHangRepository;
import com.zestia.datn.zestia.repository.NhanVienRepository;
import com.zestia.datn.zestia.repository.PasswordResetTokenRepository;
import com.zestia.datn.zestia.service.EmailService;
import com.zestia.datn.zestia.service.GoogleAuthService;
import com.zestia.datn.zestia.service.CustomerIdentityService;
import com.zestia.datn.zestia.service.RequestRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
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
    private static final Pattern STRONG_PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{8,100}$");

    private final NhanVienRepository nhanVienRepo;
    private final KhachHangRepository khachHangRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final PasswordResetTokenRepository passwordResetTokenRepo;
    private final GoogleAuthService googleAuthService;
    private final CustomerIdentityService customerIdentityService;
    private final RequestRateLimiter rateLimiter;

    @Value("${app.password-reset-token-minutes:30}")
    private long passwordResetTokenMinutes;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req, HttpServletRequest request) {
        String identifier = req != null ? clean(req.getUsername()) : null;
        String accountType = req != null ? clean(req.getAccountType()) : null;
        if (accountType != null
                && !"customer".equalsIgnoreCase(accountType)
                && !"staff".equalsIgnoreCase(accountType)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Loại tài khoản đăng nhập không hợp lệ"));
        }
        String normalizedIdentifier = identifier != null ? identifier.toLowerCase(Locale.ROOT) : "empty";
        String rateKey = clientIp(request) + "|" + Objects.toString(accountType, "auto") + "|" + normalizedIdentifier;
        if (!rateLimiter.tryAcquire("login", rateKey, 8, 15 * 60)) {
            return ResponseEntity.status(429).body(Map.of(
                    "error", "Bạn đã đăng nhập sai quá nhiều lần. Vui lòng thử lại sau 15 phút"
            ));
        }
        if (identifier == null || req.getPassword() == null || req.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Vui lòng nhập tài khoản và mật khẩu"));
        }
        boolean allowStaff = accountType == null || "staff".equalsIgnoreCase(accountType);
        boolean allowCustomer = accountType == null || "customer".equalsIgnoreCase(accountType);
        Optional<NhanVien> nvOpt = Optional.empty();
        if (allowStaff) {
            nvOpt = nhanVienRepo.findByTenNguoiDungIgnoreCase(identifier);
            if (nvOpt.isEmpty()) nvOpt = nhanVienRepo.findByEmailIgnoreCase(identifier);
        }

        if (nvOpt.isPresent()) {
            NhanVien nv = nvOpt.get();
            if (!Byte.valueOf((byte) 1).equals(nv.getTinhTrangLamViec())) {
                return ResponseEntity.status(403).body(Map.of("error", "Tài khoản nhân viên đang bị tạm khoá"));
            }
            if (passwordMatches(req.getPassword(), nv.getMatKhau())) {
                String role = nv.getVaiTro() != null ? nv.getVaiTro().getTenVaiTro() : null;
                if (!isStaffRole(role)) {
                    return ResponseEntity.status(403).body(Map.of("error", "Tài khoản nhân viên chưa được cấp vai trò hợp lệ"));
                }
                rateLimiter.reset("login", rateKey);
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

        Optional<KhachHang> khOpt = Optional.empty();
        if (allowCustomer) {
            khOpt = khachHangRepo.findByEmailIgnoreCase(identifier);
            if (khOpt.isEmpty()) {
                String normalizedPhone = customerIdentityService.normalizePhone(identifier);
                if (normalizedPhone != null) khOpt = khachHangRepo.findBySoDienThoai(normalizedPhone);
            }
        }

        if (khOpt.isPresent()) {
            KhachHang kh = khOpt.get();
            if (passwordMatches(req.getPassword(), kh.getMatKhau())) {
                rateLimiter.reset("login", rateKey);
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
    @Transactional
    public ResponseEntity<?> register(@RequestBody Map<String, String> req, HttpServletRequest request) {
        if (req == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Vui lòng nhập đầy đủ thông tin"));
        }
        if (!rateLimiter.tryAcquire("register", clientIp(request), 5, 60 * 60)) {
            return ResponseEntity.status(429).body(Map.of(
                    "error", "Bạn đã tạo quá nhiều tài khoản. Vui lòng thử lại sau"
            ));
        }
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
        if (!isStrongPassword(matKhau)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Mật khẩu cần 8-100 ký tự, gồm ít nhất một chữ và một số"));
        }

        Optional<KhachHang> byEmail = khachHangRepo.findByEmailIgnoreCase(email);
        Optional<KhachHang> byPhone = khachHangRepo.findBySoDienThoai(soDienThoai);
        if (byEmail.isPresent() && byPhone.isPresent()
                && !Objects.equals(byEmail.get().getId(), byPhone.get().getId())) {
            return ResponseEntity.status(409).body(Map.of("error", "Email và số điện thoại đang thuộc hai hồ sơ khác nhau"));
        }
        KhachHang existing = byEmail.or(() -> byPhone).orElse(null);
        if (existing != null) {
            if (existing.getMatKhau() != null && !existing.getMatKhau().isBlank()) {
                return ResponseEntity.status(409).body(Map.of("error", "Email hoặc số điện thoại đã được sử dụng"));
            }
            if (existing.getEmail() != null && existing.getEmail().equalsIgnoreCase(email)) {
                return ResponseEntity.status(409).body(Map.of(
                        "error", "Thông tin này đã có trong lịch sử mua hàng. Hãy dùng Quên mật khẩu để xác minh email và tạo mật khẩu."
                ));
            }
            return ResponseEntity.status(409).body(Map.of(
                    "error", "Số điện thoại đã có trong hồ sơ mua tại cửa hàng. Vui lòng liên hệ cửa hàng để xác minh tài khoản."
            ));
        }

        KhachHang kh = customerIdentityService.resolveForRegistration(hoVaTen, soDienThoai, email);
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
    @Transactional
    public ResponseEntity<?> requestPasswordReset(@RequestBody Map<String, String> req,
                                                   HttpServletRequest request) {
        if (req == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Vui lòng nhập email hoặc tên đăng nhập"));
        }
        String identifier = req.get("identifier");
        if (identifier == null || identifier.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Vui lòng nhập email hoặc tên đăng nhập"));
        }
        identifier = identifier.trim();

        String normalizedIdentifier = identifier.toLowerCase(Locale.ROOT);
        String clientAddress = clientIp(request);
        boolean ipAllowed = rateLimiter.tryAcquire("forgot-password-ip", clientAddress, 5, 15 * 60);
        boolean accountAllowed = rateLimiter.tryAcquire("forgot-password-account", normalizedIdentifier, 3, 30 * 60);
        if (!ipAllowed || !accountAllowed) {
            return ResponseEntity.status(429).body(Map.of(
                    "error", "Bạn đã yêu cầu quá nhiều lần. Vui lòng thử lại sau"
            ));
        }

        Optional<NhanVien> nvOpt = nhanVienRepo.findByTenNguoiDungIgnoreCase(identifier);
        if (nvOpt.isEmpty()) nvOpt = nhanVienRepo.findByEmailIgnoreCase(identifier);
        if (nvOpt.isPresent()) {
            issuePasswordResetForEmployee(nvOpt.get());
            return passwordResetAccepted();
        }

        Optional<KhachHang> khOpt = khachHangRepo.findByEmailIgnoreCase(identifier);
        if (khOpt.isEmpty()) khOpt = khachHangRepo.findBySoDienThoai(identifier);
        khOpt.ifPresent(this::issuePasswordResetForCustomer);

        return passwordResetAccepted();
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> req, HttpServletRequest request) {
        if (!rateLimiter.tryAcquire("google-login", clientIp(request), 20, 5 * 60)) {
            return ResponseEntity.status(429).body(Map.of(
                    "error", "Bạn đã thử đăng nhập Google quá nhiều lần. Vui lòng chờ ít phút"
            ));
        }
        try {
            KhachHang kh = googleAuthService.authenticate(req != null ? req.get("credential") : null);
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
    @Transactional
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> req, HttpServletRequest request) {
        if (!rateLimiter.tryAcquire("reset-password", clientIp(request), 10, 15 * 60)) {
            return ResponseEntity.status(429).body(Map.of(
                    "error", "Bạn đã thử đặt lại mật khẩu quá nhiều lần. Vui lòng thử lại sau"
            ));
        }
        String token = req != null ? req.get("token") : null;
        String newPassword = req != null
                ? firstNonBlank(req.get("newPassword"), req.get("password"), req.get("matKhau"))
                : null;
        if (token == null || token.isBlank() || newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Vui lòng nhập đầy đủ token và mật khẩu mới"));
        }
        if (!isStrongPassword(newPassword)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Mật khẩu mới cần 8-100 ký tự, gồm ít nhất một chữ và một số"));
        }

        Optional<PasswordResetToken> resetTokenOpt = passwordResetTokenRepo.findByTokenAndUsedAtIsNullForUpdate(token.trim());
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
            customerIdentityService.linkUnassignedOrders(kh);
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "Loại tài khoản không hợp lệ"));
        }

        resetToken.setUsedAt(now);
        passwordResetTokenRepo.save(resetToken);
        return ResponseEntity.ok(Map.of("message", "Đặt lại mật khẩu thành công. Bạn có thể đăng nhập bằng mật khẩu mới."));
    }

    @PostMapping("/change-password")
    @Transactional
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> req, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "Vui lòng đăng nhập lại"));
        }
        String currentPassword = req != null ? req.get("currentPassword") : null;
        String newPassword = req != null ? req.get("newPassword") : null;
        if (!isStrongPassword(newPassword)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Mật khẩu mới cần 8-100 ký tự, gồm ít nhất một chữ và một số"));
        }

        boolean customerAccount = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_KhachHang".equals(authority.getAuthority()));
        if (customerAccount) {
            KhachHang customer = khachHangRepo.findByEmailIgnoreCase(authentication.getName())
                    .or(() -> khachHangRepo.findBySoDienThoai(authentication.getName()))
                    .orElse(null);
            if (customer == null) {
                return ResponseEntity.status(403).body(Map.of("error", "Tài khoản không còn tồn tại"));
            }
            if (!passwordMatches(currentPassword, customer.getMatKhau())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Mật khẩu hiện tại không đúng"));
            }
            if (customer.getMatKhau() != null && passwordEncoder.matches(newPassword, customer.getMatKhau())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Mật khẩu mới phải khác mật khẩu hiện tại"));
            }
            customer.setMatKhau(passwordEncoder.encode(newPassword));
            khachHangRepo.save(customer);
            invalidateResetTokens(ACCOUNT_KHACH_HANG, customer.getId());
        } else {
            NhanVien employee = nhanVienRepo.findByTenNguoiDungIgnoreCase(authentication.getName())
                    .or(() -> nhanVienRepo.findByEmailIgnoreCase(authentication.getName()))
                    .orElse(null);
            if (employee == null) {
                return ResponseEntity.status(403).body(Map.of("error", "Tài khoản không còn tồn tại"));
            }
            if (!passwordMatches(currentPassword, employee.getMatKhau())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Mật khẩu hiện tại không đúng"));
            }
            if (employee.getMatKhau() != null && passwordEncoder.matches(newPassword, employee.getMatKhau())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Mật khẩu mới phải khác mật khẩu hiện tại"));
            }
            employee.setMatKhau(passwordEncoder.encode(newPassword));
            nhanVienRepo.save(employee);
            invalidateResetTokens(ACCOUNT_NHAN_VIEN, employee.getId());
        }
        return ResponseEntity.ok(Map.of("message", "Đổi mật khẩu thành công"));
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

    private boolean isStrongPassword(String value) {
        return value != null && STRONG_PASSWORD_PATTERN.matcher(value).matches();
    }

    private boolean passwordMatches(String rawPassword, String encodedPassword) {
        if (encodedPassword == null || encodedPassword.isBlank()) {
            return rawPassword == null || rawPassword.isBlank();
        }
        return rawPassword != null && passwordEncoder.matches(rawPassword, encodedPassword);
    }

    private void invalidateResetTokens(String accountType, Integer accountId) {
        LocalDateTime now = LocalDateTime.now();
        List<PasswordResetToken> tokens = passwordResetTokenRepo
                .findByAccountTypeAndAccountIdAndUsedAtIsNull(accountType, accountId);
        tokens.forEach(token -> token.setUsedAt(now));
        passwordResetTokenRepo.saveAll(tokens);
    }

    private String clientIp(HttpServletRequest request) {
        return request != null && request.getRemoteAddr() != null ? request.getRemoteAddr() : "unknown";
    }

    @PutMapping("/profile")
    @Transactional
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> body, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "Vui lòng đăng nhập lại"));
        }
        if (!hasAuthority(authentication, "ROLE_KhachHang")) {
            return ResponseEntity.status(403).body(Map.of("error", "Chức năng này chỉ dành cho khách hàng"));
        }
        KhachHang customer = khachHangRepo.findByEmailIgnoreCase(authentication.getName())
                .or(() -> khachHangRepo.findBySoDienThoai(authentication.getName()))
                .orElse(null);
        if (customer == null) {
            return ResponseEntity.status(403).body(Map.of("error", "Chức năng này chỉ dành cho khách hàng"));
        }

        if (body != null && body.containsKey("hoVaTen")) {
            String fullName = clean(body.get("hoVaTen"));
            if (fullName == null || fullName.length() < 2 || fullName.length() > 150) {
                return ResponseEntity.badRequest().body(Map.of("error", "Họ tên phải có từ 2 đến 150 ký tự"));
            }
            customer.setHoVaTen(fullName);
        }
        if (body != null && body.containsKey("soDienThoai")) {
            String phone = customerIdentityService.normalizePhone(body.get("soDienThoai"));
            if (phone == null || !PHONE_PATTERN.matcher(phone).matches()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Số điện thoại Việt Nam không hợp lệ"));
            }
            Optional<KhachHang> owner = khachHangRepo.findBySoDienThoai(phone);
            if (owner.isPresent() && !Objects.equals(owner.get().getId(), customer.getId())) {
                return ResponseEntity.status(409).body(Map.of("error", "Số điện thoại đã thuộc tài khoản khác"));
            }
            customer.setSoDienThoai(phone);
        }
        if (body != null && body.containsKey("gioiTinh")) {
            String gender = clean(body.get("gioiTinh"));
            if (gender == null) {
                customer.setGioiTinh(null);
            } else if ("0".equals(gender) || "1".equals(gender)) {
                customer.setGioiTinh(Byte.valueOf(gender));
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Giới tính không hợp lệ"));
            }
        }
        khachHangRepo.save(customer);
        return ResponseEntity.ok(Map.of(
                "hoVaTen", Objects.toString(customer.getHoVaTen(), ""),
                "email", Objects.toString(customer.getEmail(), ""),
                "soDienThoai", Objects.toString(customer.getSoDienThoai(), ""),
                "gioiTinh", customer.getGioiTinh() != null ? customer.getGioiTinh().toString() : ""
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        if (hasAnyStaffAuthority(authentication)) {
            Optional<NhanVien> employee = nhanVienRepo.findByTenNguoiDungIgnoreCase(authentication.getName())
                    .or(() -> nhanVienRepo.findByEmailIgnoreCase(authentication.getName()));
            if (employee.isEmpty()) {
                return ResponseEntity.status(401).body(Map.of("error", "Tài khoản không còn tồn tại"));
            }
            NhanVien value = employee.get();
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("username", value.getTenNguoiDung());
            result.put("hoVaTen", value.getHoVaTen());
            result.put("email", value.getEmail());
            result.put("role", value.getVaiTro() != null ? value.getVaiTro().getTenVaiTro() : "NhanVien");
            result.put("userId", value.getId());
            return ResponseEntity.ok(result);
        }
        if (!hasAuthority(authentication, "ROLE_KhachHang")) {
            return ResponseEntity.status(403).body(Map.of("error", "Vai trò tài khoản không hợp lệ"));
        }
        return khachHangRepo.findByEmailIgnoreCase(authentication.getName())
                .or(() -> khachHangRepo.findBySoDienThoai(authentication.getName()))
                .<ResponseEntity<?>>map(customer -> {
                    Map<String, Object> result = new LinkedHashMap<>();
                    result.put("username", customer.getEmail());
                    result.put("hoVaTen", customer.getHoVaTen());
                    result.put("email", customer.getEmail());
                    result.put("soDienThoai", customer.getSoDienThoai());
                    result.put("gioiTinh", customer.getGioiTinh());
                    result.put("role", "KhachHang");
                    result.put("userId", customer.getId());
                    return ResponseEntity.ok(result);
                })
                .orElse(ResponseEntity.status(401).body(Map.of("error", "Tài khoản không còn tồn tại")));
    }

    private boolean hasAnyStaffAuthority(Authentication authentication) {
        return hasAuthority(authentication, "ROLE_Admin")
                || hasAuthority(authentication, "ROLE_NhanVien")
                || hasAuthority(authentication, "ROLE_Nhân viên");
    }

    private boolean hasAuthority(Authentication authentication, String expected) {
        return authentication != null
                && authentication.isAuthenticated()
                && authentication.getAuthorities().stream()
                        .anyMatch(authority -> expected.equalsIgnoreCase(authority.getAuthority()));
    }

    private boolean isStaffRole(String role) {
        return "Admin".equalsIgnoreCase(role)
                || "NhanVien".equalsIgnoreCase(role)
                || "Nhân viên".equalsIgnoreCase(role);
    }

}
