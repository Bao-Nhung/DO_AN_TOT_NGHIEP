package com.zestia.datn.zestia.controller;

import com.zestia.datn.zestia.entity.Vay;
import com.zestia.datn.zestia.entity.VayChiTiet;
import com.zestia.datn.zestia.repository.VayChiTietRepository;
import com.zestia.datn.zestia.repository.VayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/vay")
@RequiredArgsConstructor
public class VayController {

    private final VayRepository vayRepo;
    private final VayChiTietRepository vayCtRepo;

    @GetMapping
    public List<Map<String, Object>> getAll() {
        List<Vay> list = vayRepo.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Vay v : list) {
            result.add(toMap(v));
        }
        return result;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        return vayRepo.findById(id)
                .map(v -> ResponseEntity.ok(toDetailMap(v)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public List<Map<String, Object>> search(@RequestParam String q) {
        return vayRepo.findByTenVayContainingIgnoreCase(q).stream()
                .map(this::toMap).toList();
    }

    private Map<String, Object> toMap(Vay v) {
        List<VayChiTiet> bienThe = vayCtRepo.findByVayId(v.getId());
        BigDecimal minPrice = bienThe.stream()
                .map(VayChiTiet::getGiaBan)
                .filter(Objects::nonNull)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        BigDecimal originalPrice = bienThe.stream()
                .map(VayChiTiet::getGiaBanGoc)
                .filter(Objects::nonNull)
                .min(BigDecimal::compareTo)
                .orElse(null);
        int stock = bienThe.stream()
                .filter(bt -> bt.getSoLuong() != null)
                .mapToInt(VayChiTiet::getSoLuong)
                .sum();

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", v.getId());
        map.put("maVay", v.getMaVay());
        map.put("tenVay", v.getTenVay());
        map.put("loaiVay", v.getLoaiVay() != null ? v.getLoaiVay().getTenLoaiVay() : null);
        map.put("chatLieu", v.getChatLieu() != null ? v.getChatLieu().getTenChatLieu() : null);
        map.put("giaBan", minPrice);
        map.put("giaBanGoc", originalPrice);
        map.put("tonKho", stock);
        map.put("trangThai", v.getTrangThai());
        map.put("moTa", v.getMoTa());
        map.put("ngayTao", v.getNgayTao());
        return map;
    }

    private Map<String, Object> toDetailMap(Vay v) {
        Map<String, Object> map = toMap(v);
        List<VayChiTiet> bienThe = vayCtRepo.findByVayId(v.getId());
        List<Map<String, Object>> variants = new ArrayList<>();
        for (VayChiTiet bt : bienThe) {
            Map<String, Object> btMap = new LinkedHashMap<>();
            btMap.put("id", bt.getId());
            btMap.put("maVayChiTiet", bt.getMaVayChiTiet());
            btMap.put("mauSac", bt.getMauSac() != null ? bt.getMauSac().getTenMauSac() : null);
            btMap.put("maHex", bt.getMauSac() != null ? bt.getMauSac().getMaHex() : null);
            btMap.put("kichThuoc", bt.getKichThuoc() != null ? bt.getKichThuoc().getTenKichThuoc() : null);
            btMap.put("giaBan", bt.getGiaBan());
            btMap.put("giaBanGoc", bt.getGiaBanGoc());
            btMap.put("soLuong", bt.getSoLuong());
            btMap.put("trangThai", bt.getTrangThai());
            variants.add(btMap);
        }
        map.put("bienThe", variants);
        return map;
    }
}
