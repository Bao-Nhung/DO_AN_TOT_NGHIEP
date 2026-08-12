package com.zestia.datn.zestia.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.zestia.datn.zestia.entity.KhachHang;
import com.zestia.datn.zestia.repository.KhachHangRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GoogleAuthService {
    private final KhachHangRepository customerRepo;
    private final PasswordEncoder passwordEncoder;

    @Value("${google.oauth.client-id:}")
    private String clientId;

    private volatile GoogleIdTokenVerifier verifier;

    @Transactional
    public KhachHang authenticate(String credential) {
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalStateException("Chưa cấu hình GOOGLE_CLIENT_ID cho đăng nhập Google");
        }
        if (credential == null || credential.isBlank()) {
            throw new IllegalArgumentException("Thiếu Google ID token");
        }

        GoogleIdToken idToken;
        try {
            idToken = getVerifier().verify(credential.trim());
        } catch (Exception e) {
            throw new IllegalArgumentException("Không thể xác minh tài khoản Google");
        }
        if (idToken == null) throw new IllegalArgumentException("Google ID token không hợp lệ hoặc đã hết hạn");

        GoogleIdToken.Payload payload = idToken.getPayload();
        String subject = clean(payload.getSubject());
        String email = clean(payload.getEmail());
        String hostedDomain = clean(payload.getHostedDomain());
        boolean verified = Boolean.TRUE.equals(payload.getEmailVerified());
        boolean googleAuthoritative = email != null
                && (email.toLowerCase(Locale.ROOT).endsWith("@gmail.com") || hostedDomain != null);
        if (subject == null || email == null || !verified) {
            throw new IllegalArgumentException("Tài khoản Google chưa có email được Google xác thực");
        }

        KhachHang customer = customerRepo.findByGoogleSubject(subject).orElse(null);
        if (customer == null) {
            customer = customerRepo.findByEmailIgnoreCase(email).orElse(null);
            if (customer != null && customer.getGoogleSubject() != null
                    && !subject.equals(customer.getGoogleSubject())) {
                throw new IllegalStateException("Email này đã liên kết với một tài khoản Google khác");
            }
            if (customer != null && customer.getGoogleSubject() == null && !googleAuthoritative) {
                throw new IllegalStateException("Hãy đăng nhập bằng mật khẩu trước khi liên kết email ngoài Gmail với Google");
            }
        }

        if (customer == null) {
            String name = clean((String) payload.get("name"));
            customer = KhachHang.builder()
                    .maKhachHang("KHG" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase(Locale.ROOT))
                    .hoVaTen(name != null ? name : email.substring(0, email.indexOf('@')))
                    .email(email)
                    .googleSubject(subject)
                    .matKhau(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .ngayTao(LocalDateTime.now())
                    .build();
        } else {
            customer.setGoogleSubject(subject);
        }
        return customerRepo.save(customer);
    }

    private GoogleIdTokenVerifier getVerifier() {
        GoogleIdTokenVerifier local = verifier;
        if (local == null) {
            synchronized (this) {
                local = verifier;
                if (local == null) {
                    local = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                            .setAudience(Collections.singletonList(clientId.trim()))
                            .build();
                    verifier = local;
                }
            }
        }
        return local;
    }

    private String clean(String value) {
        if (value == null) return null;
        String cleaned = value.trim();
        return cleaned.isEmpty() ? null : cleaned;
    }
}
