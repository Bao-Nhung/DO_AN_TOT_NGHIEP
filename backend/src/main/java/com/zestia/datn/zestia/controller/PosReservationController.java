package com.zestia.datn.zestia.controller;

import com.zestia.datn.zestia.config.JwtUtil;
import com.zestia.datn.zestia.service.PosReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/pos-reservations")
@RequiredArgsConstructor
public class PosReservationController {
    private final PosReservationService reservationService;
    private final JwtUtil jwtUtil;

    @GetMapping("/{token}")
    public ResponseEntity<?> getState(
            @PathVariable String token,
            @RequestHeader("Authorization") String authHeader
    ) {
        return execute(() -> reservationService.getState(token, employeeId(authHeader)));
    }

    @PostMapping("/items")
    public ResponseEntity<?> setItem(
            @RequestBody Map<String, Object> body,
            @RequestHeader("Authorization") String authHeader
    ) {
        String token = stringValue(body.get("token"));
        Integer variantId = integerValue(body.get("variantId"));
        Integer quantity = integerValue(body.get("quantity"));
        if (quantity == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Thiếu số lượng cần giữ"));
        }
        return execute(() -> reservationService.setItem(
                token,
                variantId,
                quantity,
                employeeId(authHeader)
        ));
    }

    @PutMapping("/{token}/voucher")
    public ResponseEntity<?> setVoucher(
            @PathVariable String token,
            @RequestBody(required = false) Map<String, Object> body,
            @RequestHeader("Authorization") String authHeader
    ) {
        String code = body != null ? stringValue(body.get("maGiamGia")) : null;
        return execute(() -> reservationService.setVoucher(token, code, employeeId(authHeader)));
    }

    @PostMapping("/{token}/voucher/best")
    public ResponseEntity<?> reserveBestVoucher(
            @PathVariable String token,
            @RequestHeader("Authorization") String authHeader
    ) {
        return execute(() -> reservationService.reserveBestVoucher(token, employeeId(authHeader)));
    }

    @DeleteMapping("/{token}/voucher")
    public ResponseEntity<?> clearVoucher(
            @PathVariable String token,
            @RequestHeader("Authorization") String authHeader
    ) {
        return execute(() -> reservationService.setVoucher(token, null, employeeId(authHeader)));
    }

    @DeleteMapping("/{token}")
    public ResponseEntity<?> release(
            @PathVariable String token,
            @RequestHeader("Authorization") String authHeader
    ) {
        return execute(() -> reservationService.release(token, employeeId(authHeader)));
    }

    private ResponseEntity<?> execute(Action action) {
        try {
            return ResponseEntity.ok(action.run());
        } catch (SecurityException exception) {
            return ResponseEntity.status(403).body(Map.of("error", exception.getMessage()));
        } catch (IllegalStateException exception) {
            return ResponseEntity.status(409).body(Map.of("error", exception.getMessage()));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("error", exception.getMessage()));
        }
    }

    private Integer employeeId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new SecurityException("Phiên đăng nhập không hợp lệ");
        }
        String token = authHeader.substring(7);
        if (!jwtUtil.isValid(token)) {
            throw new SecurityException("Phiên đăng nhập không hợp lệ");
        }
        Object rawId = jwtUtil.extractClaims(token).get("userId");
        Integer id = integerValue(rawId);
        if (id == null) {
            throw new SecurityException("Không xác định được nhân viên đang đăng nhập");
        }
        return id;
    }

    private static Integer integerValue(Object value) {
        if (value == null) return null;
        if (value instanceof Number number) return number.intValue();
        try {
            return Integer.valueOf(String.valueOf(value));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static String stringValue(Object value) {
        if (value == null) return null;
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }

    @FunctionalInterface
    private interface Action {
        Map<String, Object> run();
    }
}
