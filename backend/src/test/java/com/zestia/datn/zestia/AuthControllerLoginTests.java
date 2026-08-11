package com.zestia.datn.zestia;

import com.zestia.datn.zestia.config.JwtUtil;
import com.zestia.datn.zestia.controller.AuthController;
import com.zestia.datn.zestia.dto.LoginRequest;
import com.zestia.datn.zestia.dto.LoginResponse;
import com.zestia.datn.zestia.entity.KhachHang;
import com.zestia.datn.zestia.entity.NhanVien;
import com.zestia.datn.zestia.entity.VaiTro;
import com.zestia.datn.zestia.repository.DiaChiRepository;
import com.zestia.datn.zestia.repository.KhachHangRepository;
import com.zestia.datn.zestia.repository.NhanVienRepository;
import com.zestia.datn.zestia.repository.PasswordResetTokenRepository;
import com.zestia.datn.zestia.service.CustomerIdentityService;
import com.zestia.datn.zestia.service.EmailService;
import com.zestia.datn.zestia.service.GoogleAuthService;
import com.zestia.datn.zestia.service.RequestRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerLoginTests {

    @Mock private NhanVienRepository nhanVienRepository;
    @Mock private KhachHangRepository khachHangRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private DiaChiRepository diaChiRepository;
    @Mock private EmailService emailService;
    @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock private GoogleAuthService googleAuthService;
    @Mock private CustomerIdentityService customerIdentityService;
    @Mock private RequestRateLimiter rateLimiter;
    @Mock private HttpServletRequest httpRequest;

    @InjectMocks
    private AuthController controller;

    @BeforeEach
    void allowLoginAttempt() {
        when(httpRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(rateLimiter.tryAcquire(anyString(), anyString(), anyInt(), anyLong())).thenReturn(true);
    }

    @Test
    void employeeLoginTrimsIdentifierAndIgnoresCase() {
        LoginRequest request = loginRequest("  ADMIN  ");
        NhanVien employee = NhanVien.builder()
                .id(1)
                .tenNguoiDung("admin")
                .hoVaTen("Admin")
                .email("admin@zestia.vn")
                .matKhau("bcrypt-hash")
                .tinhTrangLamViec((byte) 1)
                .vaiTro(VaiTro.builder().id(1).tenVaiTro("Admin").build())
                .build();

        when(nhanVienRepository.findByTenNguoiDungIgnoreCase("ADMIN")).thenReturn(Optional.of(employee));
        when(passwordEncoder.matches("123456", "bcrypt-hash")).thenReturn(true);
        when(jwtUtil.generateToken("admin", "Admin", 1)).thenReturn("jwt-token");

        ResponseEntity<?> response = controller.login(request, httpRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        LoginResponse body = (LoginResponse) response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getToken()).isEqualTo("jwt-token");
        assertThat(body.getRole()).isEqualTo("Admin");
        verify(rateLimiter).reset("login", "127.0.0.1|admin");
    }

    @Test
    void customerLoginTrimsEmailAndIgnoresCase() {
        LoginRequest request = loginRequest("  CUSTOMER@ZESTIA.VN  ");
        KhachHang customer = KhachHang.builder()
                .id(12)
                .hoVaTen("Customer")
                .email("customer@zestia.vn")
                .soDienThoai("0911111111")
                .matKhau("bcrypt-hash")
                .build();

        when(nhanVienRepository.findByTenNguoiDungIgnoreCase("CUSTOMER@ZESTIA.VN"))
                .thenReturn(Optional.empty());
        when(nhanVienRepository.findByEmailIgnoreCase("CUSTOMER@ZESTIA.VN"))
                .thenReturn(Optional.empty());
        when(khachHangRepository.findByEmailIgnoreCase("CUSTOMER@ZESTIA.VN"))
                .thenReturn(Optional.of(customer));
        when(passwordEncoder.matches("123456", "bcrypt-hash")).thenReturn(true);
        when(jwtUtil.generateToken("customer@zestia.vn", "KhachHang", 12)).thenReturn("jwt-token");

        ResponseEntity<?> response = controller.login(request, httpRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        LoginResponse body = (LoginResponse) response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getUsername()).isEqualTo("customer@zestia.vn");
        assertThat(body.getRole()).isEqualTo("KhachHang");
        verify(rateLimiter).reset("login", "127.0.0.1|customer@zestia.vn");
    }

    private LoginRequest loginRequest(String username) {
        LoginRequest request = new LoginRequest();
        request.setUsername(username);
        request.setPassword("123456");
        return request;
    }
}
