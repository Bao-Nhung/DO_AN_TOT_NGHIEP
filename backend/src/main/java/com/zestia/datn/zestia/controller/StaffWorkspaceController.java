package com.zestia.datn.zestia.controller;

import com.zestia.datn.zestia.config.JwtUtil;
import com.zestia.datn.zestia.service.StaffDashboardService;
import com.zestia.datn.zestia.service.StaffTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
public class StaffWorkspaceController {
    private final StaffTaskService staffTaskService;
    private final StaffDashboardService staffDashboardService;
    private final JwtUtil jwtUtil;

    @GetMapping("/tasks")
    public ResponseEntity<?> getTasks(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            Authentication authentication
    ) {
        StaffIdentity staff = extractStaff(authHeader, authentication);
        if (staff == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Phiên đăng nhập không hợp lệ"));
        }
        return ResponseEntity.ok(staffTaskService.getTasks(staff.id(), staff.admin()));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            Authentication authentication
    ) {
        StaffIdentity staff = extractStaff(authHeader, authentication);
        if (staff == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Phiên đăng nhập không hợp lệ"));
        }
        return ResponseEntity.ok(staffDashboardService.getDashboard(staff.id()));
    }

    private StaffIdentity extractStaff(String authHeader, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        try {
            String token = authHeader.substring(7);
            if (!jwtUtil.isValid(token)) return null;
            var claims = jwtUtil.extractClaims(token);
            Object rawId = claims.get("userId");
            Integer id = rawId instanceof Number number
                    ? number.intValue()
                    : Integer.valueOf(String.valueOf(rawId));
            boolean admin = authentication.getAuthorities().stream()
                    .anyMatch(authority -> "ROLE_Admin".equals(authority.getAuthority()));
            return new StaffIdentity(id, admin);
        } catch (Exception ignored) {
            return null;
        }
    }

    private record StaffIdentity(Integer id, boolean admin) {
    }
}
