package com.zestia.datn.zestia.service;

import com.zestia.datn.zestia.entity.KhachHang;
import com.zestia.datn.zestia.repository.KhachHangRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CurrentCustomerService {
    private final KhachHangRepository customerRepo;

    public KhachHang require(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Vui lòng đăng nhập");
        }
        return customerRepo.findByEmail(authentication.getName())
                .or(() -> customerRepo.findBySoDienThoai(authentication.getName()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Chức năng này chỉ dành cho khách hàng"));
    }
}
