package com.zestia.datn.zestia.controller;

import com.zestia.datn.zestia.config.JwtUtil;
import com.zestia.datn.zestia.entity.KhachHang;
import com.zestia.datn.zestia.repository.KhachHangRepository;
import com.zestia.datn.zestia.service.SupportChatService;
import com.zestia.datn.zestia.service.RequestRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/support-chat")
@RequiredArgsConstructor
public class SupportChatController {
    private final SupportChatService supportChatService;
    private final KhachHangRepository customerRepository;
    private final JwtUtil jwtUtil;
    private final RequestRateLimiter rateLimiter;

    @PostMapping("/customer/request")
    public ResponseEntity<?> requestSupport(@RequestBody Map<String, Object> body,
                                            @RequestHeader(value = "Authorization", required = false) String authHeader,
                                            HttpServletRequest request) {
        if (!rateLimiter.tryAcquire("support-request", clientIp(request), 5, 10 * 60)) {
            return ResponseEntity.status(429).body(Map.of(
                    "error", "Bạn đã tạo quá nhiều yêu cầu hỗ trợ. Vui lòng thử lại sau"
            ));
        }
        return ResponseEntity.ok(supportChatService.requestSupport(text(body, "message"), customer(authHeader)));
    }

    @GetMapping("/customer/{token}")
    public ResponseEntity<?> customerConversation(@PathVariable String token) {
        return ResponseEntity.ok(supportChatService.getCustomerConversation(token));
    }

    @PostMapping("/customer/{token}/messages")
    public ResponseEntity<?> customerMessage(@PathVariable String token,
                                             @RequestBody Map<String, Object> body,
                                             HttpServletRequest request) {
        String rateKey = clientIp(request) + "|" + token;
        if (!rateLimiter.tryAcquire("support-message", rateKey, 30, 5 * 60)) {
            return ResponseEntity.status(429).body(Map.of(
                    "error", "Bạn đang gửi tin nhắn quá nhanh. Vui lòng chờ một chút"
            ));
        }
        return ResponseEntity.ok(supportChatService.sendCustomerMessage(token, text(body, "message")));
    }

    @GetMapping("/staff/conversations")
    public ResponseEntity<?> staffConversations(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Staff staff = requireStaff(authHeader);
        return ResponseEntity.ok(supportChatService.getStaffConversations(staff.id(), staff.admin()));
    }

    @GetMapping("/staff/conversations/{id}")
    public ResponseEntity<?> staffConversation(@PathVariable Integer id,
                                               @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Staff staff = requireStaff(authHeader);
        return ResponseEntity.ok(supportChatService.getStaffConversation(id, staff.id(), staff.admin()));
    }

    @PostMapping("/staff/conversations/{id}/claim")
    public ResponseEntity<?> claim(@PathVariable Integer id,
                                   @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Staff staff = requireStaff(authHeader);
        return ResponseEntity.ok(supportChatService.claim(id, staff.id(), staff.admin()));
    }

    @PostMapping("/staff/conversations/{id}/messages")
    public ResponseEntity<?> staffMessage(@PathVariable Integer id, @RequestBody Map<String, Object> body,
                                          @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Staff staff = requireStaff(authHeader);
        return ResponseEntity.ok(supportChatService.sendStaffMessage(
                id, staff.id(), staff.admin(), text(body, "message"), staff.name()
        ));
    }

    @PostMapping("/staff/conversations/{id}/close")
    public ResponseEntity<?> close(@PathVariable Integer id,
                                   @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Staff staff = requireStaff(authHeader);
        return ResponseEntity.ok(supportChatService.close(id, staff.id(), staff.admin(), staff.name()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> badRequest(IllegalArgumentException error) {
        return ResponseEntity.badRequest().body(Map.of("error", error.getMessage()));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<?> notFound(NoSuchElementException error) {
        return ResponseEntity.status(404).body(Map.of("error", error.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> conflict(IllegalStateException error) {
        return ResponseEntity.status(409).body(Map.of("error", error.getMessage()));
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<?> forbidden(SecurityException error) {
        return ResponseEntity.status(403).body(Map.of("error", error.getMessage()));
    }

    private KhachHang customer(String authHeader) {
        Staff token = token(authHeader);
        if (token == null || !"KhachHang".equals(token.role())) return null;
        return customerRepository.findById(token.id()).orElse(null);
    }

    private Staff requireStaff(String authHeader) {
        Staff staff = token(authHeader);
        if (staff == null || (!staff.admin() && !isEmployee(staff.role()))) {
            throw new SecurityException("Bạn không có quyền truy cập kênh hỗ trợ");
        }
        return staff;
    }

    private Staff token(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        try {
            String raw = authHeader.substring(7);
            if (!jwtUtil.isValid(raw)) return null;
            var claims = jwtUtil.extractClaims(raw);
            Object idValue = claims.get("userId");
            Integer id = idValue instanceof Number number
                    ? number.intValue()
                    : Integer.parseInt(String.valueOf(idValue));
            String role = claims.get("role", String.class);
            String name = jwtUtil.extractUsername(raw);
            return new Staff(id, role, name, "Admin".equals(role));
        } catch (Exception ignored) {
            return null;
        }
    }

    private boolean isEmployee(String role) {
        return "NhanVien".equalsIgnoreCase(role) || "Nhân viên".equalsIgnoreCase(role);
    }

    private String text(Map<String, Object> body, String key) {
        Object value = body != null ? body.get(key) : null;
        return value == null ? "" : String.valueOf(value);
    }

    private static String clientIp(HttpServletRequest request) {
        return request != null && request.getRemoteAddr() != null ? request.getRemoteAddr() : "unknown";
    }

    private record Staff(Integer id, String role, String name, boolean admin) {
    }
}
