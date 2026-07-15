package com.zestia.datn.zestia.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zestia.datn.zestia.service.ShiftAccessService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class EmployeeShiftAccessFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final ShiftAccessService shiftAccessService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ") || isExempt(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        String role;
        Integer employeeId;
        try {
            if (!jwtUtil.isValid(token)) {
                filterChain.doFilter(request, response);
                return;
            }
            var claims = jwtUtil.extractClaims(token);
            role = claims.get("role", String.class);
            employeeId = toInt(claims.get("userId"));
        } catch (Exception ignored) {
            filterChain.doFilter(request, response);
            return;
        }
        if (!isEmployeeRole(role)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            ShiftAccessService.WorkStatus status = shiftAccessService.getWorkStatus(employeeId, LocalDateTime.now());
            if (status.canOperate()) {
                filterChain.doFilter(request, response);
                return;
            }

            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getWriter(), Map.of(
                    "error", status.reason(),
                    "code", "SHIFT_REQUIRED"
            ));
        } catch (Exception ignored) {
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getWriter(), Map.of(
                    "error", "Không thể kiểm tra ca làm lúc này. Vui lòng thử lại",
                    "code", "SHIFT_CHECK_UNAVAILABLE"
            ));
        }
    }

    private boolean isExempt(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;
        String path = request.getRequestURI();
        return path.startsWith("/api/auth/") || path.startsWith("/api/lich-lam-viec");
    }

    private boolean isEmployeeRole(String role) {
        return "NhanVien".equalsIgnoreCase(role) || "Nhân viên".equalsIgnoreCase(role);
    }

    private Integer toInt(Object value) {
        if (value instanceof Number number) return number.intValue();
        try {
            return value == null ? null : Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
