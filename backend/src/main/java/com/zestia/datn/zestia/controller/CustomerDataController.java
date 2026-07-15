package com.zestia.datn.zestia.controller;

import com.zestia.datn.zestia.service.CustomerDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = body.get("items") instanceof List<?> list
                ? (List<Map<String, Object>>) list
                : List.of();
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
