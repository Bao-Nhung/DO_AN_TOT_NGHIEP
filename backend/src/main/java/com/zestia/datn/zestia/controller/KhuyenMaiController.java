package com.zestia.datn.zestia.controller;

import com.zestia.datn.zestia.repository.GiamGiaRepository;
import com.zestia.datn.zestia.repository.KhuyenMaiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/khuyen-mai")
@RequiredArgsConstructor
public class KhuyenMaiController {

    private final KhuyenMaiRepository khuyenMaiRepo;
    private final GiamGiaRepository giamGiaRepo;

    @GetMapping
    public Object getKhuyenMai() { return khuyenMaiRepo.findAll(); }

    @GetMapping("/giam-gia")
    public Object getGiamGia() { return giamGiaRepo.findAll(); }

    @GetMapping("/all")
    public Map<String, Object> getAll() {
        return Map.of(
            "khuyenMai", khuyenMaiRepo.findAll(),
            "giamGia", giamGiaRepo.findAll()
        );
    }
}
