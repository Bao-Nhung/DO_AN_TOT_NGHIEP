package com.zestia.datn.zestia.controller;

import com.zestia.datn.zestia.service.LuckyWheelService;
import com.zestia.datn.zestia.service.LuckyWheelIconService;
import com.zestia.datn.zestia.service.RequestRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lucky-wheel")
@RequiredArgsConstructor
public class LuckyWheelController {
    private final LuckyWheelService luckyWheelService;
    private final LuckyWheelIconService luckyWheelIconService;
    private final RequestRateLimiter rateLimiter;

    @GetMapping("/campaign")
    public Map<String, Object> campaign() {
        return luckyWheelService.activeCampaign();
    }

    @PostMapping("/check")
    public Map<String, Object> check(@RequestBody PlayRequest body, HttpServletRequest request) {
        throttle("lucky-check-ip", clientIp(request), 20, 60);
        throttle("lucky-check-order", key(body), 12, 60 * 60);
        return luckyWheelService.checkEligibility(body != null ? body.orderCode() : null,
                body != null ? body.phone() : null);
    }

    @PostMapping("/spin")
    public Map<String, Object> spin(@RequestBody PlayRequest body, HttpServletRequest request) {
        throttle("lucky-spin-ip", clientIp(request), 8, 60);
        throttle("lucky-spin-order", key(body), 4, 60 * 60);
        return luckyWheelService.spin(body != null ? body.orderCode() : null,
                body != null ? body.phone() : null);
    }

    @GetMapping("/admin/campaigns")
    public List<Map<String, Object>> campaigns() {
        return luckyWheelService.listCampaigns();
    }

    @PostMapping("/admin/campaigns")
    public Map<String, Object> createCampaign(@RequestBody Map<String, Object> body) {
        return luckyWheelService.saveCampaign(null, body);
    }

    @PutMapping("/admin/campaigns/{id}")
    public Map<String, Object> updateCampaign(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        return luckyWheelService.saveCampaign(id, body);
    }

    @PostMapping("/admin/campaigns/{campaignId}/prizes")
    public Map<String, Object> createPrize(@PathVariable Integer campaignId, @RequestBody Map<String, Object> body) {
        return luckyWheelService.savePrize(campaignId, null, body);
    }

    @PutMapping("/admin/campaigns/{campaignId}/prizes/{prizeId}")
    public Map<String, Object> updatePrize(@PathVariable Integer campaignId,
                                           @PathVariable Integer prizeId,
                                           @RequestBody Map<String, Object> body) {
        return luckyWheelService.savePrize(campaignId, prizeId, body);
    }

    @PostMapping(value = "/admin/icons", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, String> uploadPrizeIcon(@RequestParam("file") MultipartFile file) {
        return luckyWheelIconService.store(file);
    }

    @GetMapping("/admin/spins")
    public List<Map<String, Object>> spins(@RequestParam(required = false) Integer campaignId,
                                           @RequestParam(required = false) String status) {
        return luckyWheelService.listSpins(campaignId, status);
    }

    @PutMapping("/admin/spins/{id}/deliver")
    public Map<String, Object> deliver(@PathVariable Integer id, Authentication authentication) {
        return luckyWheelService.markDelivered(id, authentication);
    }

    private void throttle(String namespace, String key, int limit, long windowSeconds) {
        if (!rateLimiter.tryAcquire(namespace, key, limit, windowSeconds)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Bạn thao tác quá nhanh. Vui lòng chờ một chút rồi thử lại.");
        }
    }

    private String key(PlayRequest body) {
        if (body == null) return "missing";
        return String.valueOf(body.orderCode()).trim().toUpperCase() + ":" + String.valueOf(body.phone()).replaceAll("\\D", "");
    }

    private String clientIp(HttpServletRequest request) {
        return request != null && request.getRemoteAddr() != null ? request.getRemoteAddr() : "unknown";
    }

    public record PlayRequest(String orderCode, String phone) {}
}
