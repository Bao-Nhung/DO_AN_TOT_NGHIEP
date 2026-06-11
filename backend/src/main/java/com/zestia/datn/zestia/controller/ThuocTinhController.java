package com.zestia.datn.zestia.controller;

import com.zestia.datn.zestia.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/thuoc-tinh")
@RequiredArgsConstructor
public class ThuocTinhController {

    private final MauSacRepository mauSacRepo;
    private final KichThuocRepository kichThuocRepo;
    private final ChatLieuRepository chatLieuRepo;
    private final LoaiVayRepository loaiVayRepo;
    private final NhaCungCapRepository nhaCungCapRepo;

    @GetMapping("/mau-sac")
    public Object getMauSac() { return mauSacRepo.findAll(); }

    @GetMapping("/kich-thuoc")
    public Object getKichThuoc() { return kichThuocRepo.findAll(); }

    @GetMapping("/chat-lieu")
    public Object getChatLieu() { return chatLieuRepo.findAll(); }

    @GetMapping("/loai-vay")
    public Object getLoaiVay() { return loaiVayRepo.findAll(); }

    @GetMapping("/nha-cung-cap")
    public Object getNhaCungCap() { return nhaCungCapRepo.findAll(); }

    @GetMapping
    public Map<String, Object> getAll() {
        return Map.of(
            "mauSac", mauSacRepo.findAll(),
            "kichThuoc", kichThuocRepo.findAll(),
            "chatLieu", chatLieuRepo.findAll(),
            "loaiVay", loaiVayRepo.findAll(),
            "nhaCungCap", nhaCungCapRepo.findAll()
        );
    }
}
