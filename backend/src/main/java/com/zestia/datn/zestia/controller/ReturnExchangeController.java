package com.zestia.datn.zestia.controller;

import com.zestia.datn.zestia.service.ReturnExchangeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/returns")
@RequiredArgsConstructor
public class ReturnExchangeController {
    private final ReturnExchangeService returnService;

    @GetMapping("/mine")
    public Map<String, Object> mine(@RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "6") int size,
                                    @RequestParam(required = false) String q,
                                    Authentication authentication) {
        return returnService.mine(authentication, page, size, q);
    }

    @PostMapping(value = "/online", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> createOnline(@RequestParam Integer orderId,
                                             @RequestParam Integer orderDetailId,
                                             @RequestParam String type,
                                             @RequestParam Integer quantity,
                                             @RequestParam String reason,
                                             @RequestParam String condition,
                                             @RequestParam(required = false) String refundInfo,
                                             @RequestParam(required = false) Integer replacementVariantId,
                                             @RequestPart("images") List<MultipartFile> images,
                                             Authentication authentication) {
        return returnService.createOnline(orderId, orderDetailId, type, quantity, reason, condition,
                refundInfo, replacementVariantId, images, authentication);
    }

    @GetMapping
    public Map<String, Object> all(@RequestParam(required = false) String type,
                                   @RequestParam(required = false) String status,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "10") int size) {
        return returnService.all(type, status, page, size);
    }

    @PostMapping("/offline")
    public Map<String, Object> createOffline(@RequestBody Map<String, Object> body,
                                              Authentication authentication) {
        requireBody(body);
        return returnService.createOffline(
                integer(body.get("orderId")),
                integer(body.get("orderDetailId")),
                string(body.get("type")),
                integer(body.get("quantity")),
                string(body.get("reason")),
                string(body.get("refundInfo")),
                integer(body.get("replacementVariantId")),
                authentication
        );
    }

    @PutMapping("/{id}/review")
    public Map<String, Object> review(@PathVariable Integer id,
                                       @RequestBody Map<String, Object> body,
                                       Authentication authentication) {
        requireBody(body);
        return returnService.review(id, bool(body.get("approved")), string(body.get("reason")), authentication);
    }

    @PutMapping("/{id}/receive")
    public Map<String, Object> receive(@PathVariable Integer id,
                                        @RequestBody Map<String, Object> body,
                                        Authentication authentication) {
        requireBody(body);
        return returnService.receive(id, bool(body.get("accepted")), string(body.get("reason")), authentication);
    }

    @PutMapping("/{id}/complete")
    public Map<String, Object> complete(@PathVariable Integer id,
                                         @RequestBody(required = false) Map<String, Object> body,
                                         Authentication authentication) {
        return returnService.complete(id, body != null ? string(body.get("note")) : null, authentication);
    }

    private Integer integer(Object value) {
        if (value == null) return null;
        if (value instanceof Number number) return number.intValue();
        try {
            return Integer.valueOf(value.toString());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private void requireBody(Map<String, Object> body) {
        if (body == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dữ liệu đổi trả không hợp lệ");
    }

    private String string(Object value) {
        return value != null ? value.toString() : null;
    }

    private boolean bool(Object value) {
        return Boolean.TRUE.equals(value) || "true".equalsIgnoreCase(String.valueOf(value));
    }
}
