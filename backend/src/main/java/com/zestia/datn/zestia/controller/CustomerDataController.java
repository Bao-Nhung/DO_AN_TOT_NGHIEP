package com.zestia.datn.zestia.controller;

import com.zestia.datn.zestia.service.CustomerDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customer-data")
@RequiredArgsConstructor
public class CustomerDataController {
    private final CustomerDataService customerDataService;

    @GetMapping
    public Map<String, Object> getAll(Authentication authentication) {
        return customerDataService.getAll(authentication);
    }

    @PutMapping("/cart")
    public List<Map<String, Object>> replaceCart(@RequestBody Map<String, Object> body, Authentication authentication) {
        Object rawItems = body != null ? body.get("items") : null;
        if (!(rawItems instanceof List<?> list)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Danh sách giỏ hàng không hợp lệ");
        }
        if (list.size() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Giỏ hàng vượt quá 100 dòng sản phẩm");
        }
        List<Map<String, Object>> items = new ArrayList<>();
        for (Object value : list) {
            if (!(value instanceof Map<?, ?> source)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dòng sản phẩm không hợp lệ");
            }
            Map<String, Object> item = new LinkedHashMap<>();
            source.forEach((key, itemValue) -> {
                if (key instanceof String stringKey) item.put(stringKey, itemValue);
            });
            items.add(item);
        }
        return customerDataService.replaceCart(authentication, items);
    }

    @PostMapping("/wishlist/{productId}")
    public ResponseEntity<Void> addWishlist(@PathVariable Integer productId, Authentication authentication) {
        customerDataService.addWishlist(authentication, productId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/wishlist/{productId}")
    public ResponseEntity<Void> removeWishlist(@PathVariable Integer productId, Authentication authentication) {
        customerDataService.removeWishlist(authentication, productId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/recent/{productId}")
    public ResponseEntity<Void> recordView(@PathVariable Integer productId, Authentication authentication) {
        customerDataService.recordView(authentication, productId);
        return ResponseEntity.noContent().build();
    }
}
