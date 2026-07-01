package com.zestia.datn.zestia.controller;

import com.zestia.datn.zestia.entity.ThongBao;
import com.zestia.datn.zestia.repository.ThongBaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/thong-bao")
@RequiredArgsConstructor
public class ThongBaoController {

    private final ThongBaoRepository thongBaoRepo;

    @GetMapping
    public List<ThongBao> getAll() {
        return thongBaoRepo.findAllByOrderByNgayTaoDesc();
    }

    @GetMapping("/active")
    public List<ThongBao> getActive() {
        return thongBaoRepo.findByTrangThaiOrderByNgayTaoDesc((byte) 1);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody ThongBao body) {
        if (body.getTieuDe() == null || body.getTieuDe().trim().isEmpty() ||
            body.getNoiDung() == null || body.getNoiDung().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Vui lòng nhập đầy đủ tiêu đề và nội dung"));
        }
        body.setNgayTao(LocalDateTime.now());
        if (body.getTrangThai() == null) body.setTrangThai((byte) 1);
        return ResponseEntity.ok(thongBaoRepo.save(body));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody ThongBao body) {
        return thongBaoRepo.findById(id).map(existing -> {
            if (body.getTieuDe() != null) existing.setTieuDe(body.getTieuDe());
            if (body.getNoiDung() != null) existing.setNoiDung(body.getNoiDung());
            if (body.getLoai() != null) existing.setLoai(body.getLoai());
            if (body.getTrangThai() != null) existing.setTrangThai(body.getTrangThai());
            return ResponseEntity.ok(thongBaoRepo.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        thongBaoRepo.deleteById(id);
        return ResponseEntity.ok().build();
    }
}