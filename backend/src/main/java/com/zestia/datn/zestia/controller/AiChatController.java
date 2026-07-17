package com.zestia.datn.zestia.controller;

import com.zestia.datn.zestia.config.JwtUtil;
import com.zestia.datn.zestia.service.AiChatService;
import com.zestia.datn.zestia.service.RequestRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai-chat")
@RequiredArgsConstructor
public class AiChatController {

    private static final int MAX_REQUESTS = 20;
    private static final long WINDOW_SECONDS = 10 * 60;

    private final AiChatService aiChatService;
    private final JwtUtil jwtUtil;
    private final RequestRateLimiter rateLimiter;

    @PostMapping
    public ResponseEntity<?> chat(@RequestBody Map<String, Object> body,
                                  @RequestHeader(value = "Authorization", required = false) String authHeader,
                                  HttpServletRequest request) {
        if (!rateLimiter.tryAcquire("ai-chat", clientKey(request), MAX_REQUESTS, WINDOW_SECONDS)) {
            return ResponseEntity.status(429).body(Map.of(
                    "reply", "Bạn đang gửi quá nhiều tin nhắn. Vui lòng thử lại sau ít phút."
            ));
        }

        String message = body != null ? String.valueOf(body.getOrDefault("message", "")) : "";
        List<?> history = body != null && body.get("history") instanceof List<?> list ? list : List.of();
        return ResponseEntity.ok(aiChatService.reply(message, history, extractUser(authHeader)));
    }

    private AiChatService.ChatUser extractUser(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return new AiChatService.ChatUser(null, null);
        }
        try {
            String token = authHeader.substring(7);
            if (!jwtUtil.isValid(token)) {
                return new AiChatService.ChatUser(null, null);
            }
            var claims = jwtUtil.extractClaims(token);
            String role = claims.get("role", String.class);
            Object rawUserId = claims.get("userId");
            Integer userId = rawUserId instanceof Number n ? n.intValue() : Integer.parseInt(String.valueOf(rawUserId));
            return new AiChatService.ChatUser(role, userId);
        } catch (Exception e) {
            return new AiChatService.ChatUser(null, null);
        }
    }

    private String clientKey(HttpServletRequest request) {
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "unknown";
    }
}
