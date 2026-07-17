package com.zestia.datn.zestia.controller;

import com.zestia.datn.zestia.entity.NewsletterSubscriber;
import com.zestia.datn.zestia.repository.NewsletterSubscriberRepository;
import com.zestia.datn.zestia.service.EmailService;
import com.zestia.datn.zestia.service.RequestRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/newsletter")
@RequiredArgsConstructor
public class NewsletterController {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);

    private final NewsletterSubscriberRepository newsletterRepo;
    private final EmailService emailService;
    private final RequestRateLimiter rateLimiter;

    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribe(@RequestBody SubscribeRequest payload, HttpServletRequest request) {
        String email = payload != null && payload.email() != null ? payload.email().trim().toLowerCase() : "";
        if (email.isBlank() || !EMAIL_PATTERN.matcher(email).matches()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email không hợp lệ"));
        }
        boolean ipAllowed = rateLimiter.tryAcquire("newsletter-ip", clientIp(request), 5, 60 * 60);
        boolean emailAllowed = rateLimiter.tryAcquire("newsletter-email", email, 2, 24 * 60 * 60);
        if (!ipAllowed || !emailAllowed) {
            return ResponseEntity.status(429).body(Map.of(
                    "error", "Bạn đã đăng ký quá nhiều lần. Vui lòng thử lại sau"
            ));
        }

        LocalDateTime now = LocalDateTime.now();
        boolean alreadyActive = false;
        NewsletterSubscriber subscriber = newsletterRepo.findByEmailIgnoreCase(email)
                .map(existing -> {
                    if (existing.getTrangThai() != null && existing.getTrangThai() == 1) {
                        return existing;
                    }
                    existing.setTrangThai((byte) 1);
                    existing.setNgayCapNhat(now);
                    return existing;
                })
                .orElseGet(() -> NewsletterSubscriber.builder()
                        .email(email)
                        .trangThai((byte) 1)
                        .ngayDangKy(now)
                        .ngayCapNhat(now)
                        .build());

        if (subscriber.getId() != null && subscriber.getTrangThai() != null && subscriber.getTrangThai() == 1) {
            alreadyActive = subscriber.getNgayCapNhat() == null || subscriber.getNgayCapNhat().isBefore(now.minusSeconds(1));
        }

        NewsletterSubscriber saved = newsletterRepo.save(subscriber);
        if (!alreadyActive) {
            emailService.sendNewsletterWelcomeEmail(saved.getEmail());
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("email", saved.getEmail());
        result.put("message", alreadyActive ? "Email này đã đăng ký nhận tin" : "Đăng ký nhận tin thành công");
        return ResponseEntity.ok(result);
    }

    private static String clientIp(HttpServletRequest request) {
        return request != null && request.getRemoteAddr() != null ? request.getRemoteAddr() : "unknown";
    }

    public record SubscribeRequest(String email) {}
}
