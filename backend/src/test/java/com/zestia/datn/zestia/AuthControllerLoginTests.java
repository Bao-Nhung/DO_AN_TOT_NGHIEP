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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.lenient;
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
        lenient().when(httpRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        lenient().when(rateLimiter.tryAcquire(anyString(), anyString(), anyInt(), anyLong())).thenReturn(true);
    }

    @Test
    void employeeLoginTrimsIdentifierAndIgnoresCase() {
        LoginRequest request = loginRequest("  ADMIN  ", "staff");
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
        verify(rateLimiter).reset("login", "127.0.0.1|staff|admin");
    }

    @Test
    void customerLoginTrimsEmailAndIgnoresCase() {
        LoginRequest request = loginRequest("  CUSTOMER@ZESTIA.VN  ", "customer");
        KhachHang customer = KhachHang.builder()
                .id(12)
                .hoVaTen("Customer")
                .email("customer@zestia.vn")
                .soDienThoai("0911111111")
                .matKhau("bcrypt-hash")
                .build();

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
        verify(rateLimiter).reset("login", "127.0.0.1|customer|customer@zestia.vn");
        verify(nhanVienRepository, never()).findByTenNguoiDungIgnoreCase(anyString());
    }

    @Test
    void customerTabCannotAuthenticateEmployeeAccount() {
        LoginRequest request = loginRequest("admin", "customer");
        when(khachHangRepository.findByEmailIgnoreCase("admin")).thenReturn(Optional.empty());
        when(customerIdentityService.normalizePhone("admin")).thenReturn(null);

        ResponseEntity<?> response = controller.login(request, httpRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(nhanVienRepository, never()).findByTenNguoiDungIgnoreCase(anyString());
        verify(nhanVienRepository, never()).findByEmailIgnoreCase(anyString());
    }

    @Test
    void staffTabCannotAuthenticateCustomerAccount() {
        LoginRequest request = loginRequest("customer@zestia.vn", "staff");
        when(nhanVienRepository.findByTenNguoiDungIgnoreCase("customer@zestia.vn"))
                .thenReturn(Optional.empty());
        when(nhanVienRepository.findByEmailIgnoreCase("customer@zestia.vn"))
                .thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.login(request, httpRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(khachHangRepository, never()).findByEmailIgnoreCase(anyString());
        verify(khachHangRepository, never()).findBySoDienThoai(anyString());
    }

    @Test
    void customerProfileLookupCannotBeConfusedWithEmployeeIdentifier() {
        KhachHang customer = KhachHang.builder()
                .id(12)
                .hoVaTen("Customer")
                .email("shared@zestia.vn")
                .soDienThoai("0911111111")
                .build();
        var authentication = new UsernamePasswordAuthenticationToken(
                "shared@zestia.vn",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_KhachHang"))
        );
        when(khachHangRepository.findByEmailIgnoreCase("shared@zestia.vn")).thenReturn(Optional.of(customer));

        ResponseEntity<?> response = controller.me(authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(((Map<?, ?>) response.getBody()).get("hoVaTen")).isEqualTo("Customer");
        verify(nhanVienRepository, never()).findByTenNguoiDungIgnoreCase(anyString());
    }

    @Test
    void employeeCannotUpdateCustomerProfileThroughSharedIdentifier() {
        var authentication = new UsernamePasswordAuthenticationToken(
                "shared@zestia.vn",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_NhanVien"))
        );

        ResponseEntity<?> response = controller.updateProfile(Map.of("hoVaTen", "Changed"), authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(khachHangRepository, never()).findByEmailIgnoreCase(anyString());
    }

    private LoginRequest loginRequest(String username, String accountType) {
        LoginRequest request = new LoginRequest();
        request.setUsername(username);
        request.setPassword("123456");
        request.setAccountType(accountType);
        return request;
    }
}
