package com.zestia.datn.zestia.controller;

import com.zestia.datn.zestia.config.JwtUtil;
import com.zestia.datn.zestia.entity.GioHang;
import com.zestia.datn.zestia.entity.GioHangChiTiet;
import com.zestia.datn.zestia.entity.KhachHang;
import com.zestia.datn.zestia.entity.VayChiTiet;
import com.zestia.datn.zestia.repository.GioHangChiTietRepository;
import com.zestia.datn.zestia.repository.GioHangRepository;
import com.zestia.datn.zestia.repository.KhachHangRepository;
import com.zestia.datn.zestia.repository.VayChiTietRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/gio-hang")
@RequiredArgsConstructor
@Transactional
public class GioHangController {

    private final GioHangRepository gioHangRepo;
    private final GioHangChiTietRepository gioHangChiTietRepo;
    private final KhachHangRepository khachHangRepo;
    private final VayChiTietRepository vayChiTietRepo;
    private final JwtUtil jwtUtil;

    private Integer getUserIdFromToken(String header) {
        if (header == null || !header.startsWith("Bearer ")) return null;
        String token = header.substring(7);
        if (!jwtUtil.isValid(token)) return null;
        var claims = jwtUtil.extractClaims(token);
        if (!"KhachHang".equals(claims.get("role"))) return null;
        return ((Number) claims.get("userId")).intValue();
    }

    private GioHang getOrCreateGioHang(Integer khachHangId) {
        KhachHang kh = khachHangRepo.findById(khachHangId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));
        GioHang gioHang = gioHangRepo.findByKhachHangId(khachHangId).orElse(null);
        if (gioHang == null) {
            gioHang = new GioHang();
            gioHang.setKhachHang(kh);
            gioHang.setNgayTao(LocalDateTime.now());
            gioHang = gioHangRepo.save(gioHang);
        }
        return gioHang;
    }

    @GetMapping
    public ResponseEntity<?> getGioHang(@RequestHeader(value = "Authorization", required = false) String header) {
        Integer userId = getUserIdFromToken(header);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));

        GioHang gioHang = getOrCreateGioHang(userId);
        List<GioHangChiTiet> chiTiets = gioHangChiTietRepo.findByGioHangId(gioHang.getId());

        List<Map<String, Object>> items = chiTiets.stream()
            .filter(ct -> ct.getVayChiTiet() != null && ct.getVayChiTiet().getVay() != null)
            .map(ct -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", ct.getId());
            map.put("idVayChiTiet", ct.getVayChiTiet().getId());
            map.put("soLuong", ct.getSoLuong());
            map.put("maVayChiTiet", ct.getVayChiTiet().getMaVayChiTiet());
            map.put("giaBan", ct.getVayChiTiet().getGiaBan());
            map.put("tenVay", ct.getVayChiTiet().getVay().getTenVay());
            map.put("mauSac", ct.getVayChiTiet().getMauSac() != null ? ct.getVayChiTiet().getMauSac().getTenMauSac() : null);
            map.put("kichThuoc", ct.getVayChiTiet().getKichThuoc() != null ? ct.getVayChiTiet().getKichThuoc().getTenKichThuoc() : null);
            
            String anhUrl = null;
            if (ct.getVayChiTiet().getVay().getDanhSachAnh() != null && !ct.getVayChiTiet().getVay().getDanhSachAnh().isEmpty()) {
                anhUrl = ct.getVayChiTiet().getVay().getDanhSachAnh().get(0).getAnhUrl();
            }
            if (anhUrl == null) anhUrl = ct.getVayChiTiet().getAnhUrl();
            map.put("anhUrl", anhUrl);
            
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(items);
    }

    @PostMapping
    public ResponseEntity<?> addItem(@RequestHeader(value = "Authorization", required = false) String header,
                                     @RequestBody Map<String, Object> body) {
        Integer userId = getUserIdFromToken(header);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));

        Integer idVayChiTiet = (Integer) body.get("idVayChiTiet");
        Integer soLuong = (Integer) body.get("soLuong");

        if (idVayChiTiet == null || soLuong == null || soLuong <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "Dữ liệu không hợp lệ"));
        }

        VayChiTiet vayChiTiet = vayChiTietRepo.findById(idVayChiTiet).orElse(null);
        if (vayChiTiet == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Sản phẩm không tồn tại"));
        }

        if (vayChiTiet.getSoLuong() < soLuong) {
            return ResponseEntity.badRequest().body(Map.of("error", "Vượt quá số lượng tồn kho"));
        }

        GioHang gioHang = getOrCreateGioHang(userId);
        List<GioHangChiTiet> chiTiets = gioHangChiTietRepo.findByGioHangId(gioHang.getId());

        Optional<GioHangChiTiet> existing = chiTiets.stream()
                .filter(ct -> ct.getVayChiTiet().getId().equals(idVayChiTiet))
                .findFirst();

        if (existing.isPresent()) {
            GioHangChiTiet ct = existing.get();
            int newQty = ct.getSoLuong() + soLuong;
            if (newQty > vayChiTiet.getSoLuong()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Vượt quá số lượng tồn kho"));
            }
            ct.setSoLuong(newQty);
            gioHangChiTietRepo.save(ct);
        } else {
            GioHangChiTiet ct = new GioHangChiTiet();
            ct.setGioHang(gioHang);
            ct.setVayChiTiet(vayChiTiet);
            ct.setSoLuong(soLuong);
            ct.setNgayTao(LocalDateTime.now());
            gioHangChiTietRepo.save(ct);
        }

        return ResponseEntity.ok(Map.of("message", "Đã thêm vào giỏ hàng"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateItem(@RequestHeader(value = "Authorization", required = false) String header,
                                        @PathVariable Integer id,
                                        @RequestBody Map<String, Object> body) {
        Integer userId = getUserIdFromToken(header);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));

        Integer soLuong = (Integer) body.get("soLuong");
        if (soLuong == null || soLuong <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "Số lượng không hợp lệ"));
        }

        Optional<GioHangChiTiet> ctOpt = gioHangChiTietRepo.findById(id);
        if (ctOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        GioHangChiTiet ct = ctOpt.get();
        if (!ct.getGioHang().getKhachHang().getId().equals(userId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Không có quyền sửa giỏ hàng này"));
        }

        if (soLuong > ct.getVayChiTiet().getSoLuong()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Vượt quá số lượng tồn kho"));
        }

        ct.setSoLuong(soLuong);
        gioHangChiTietRepo.save(ct);

        return ResponseEntity.ok(Map.of("message", "Đã cập nhật số lượng"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteItem(@RequestHeader(value = "Authorization", required = false) String header,
                                        @PathVariable Integer id) {
        Integer userId = getUserIdFromToken(header);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));

        Optional<GioHangChiTiet> ctOpt = gioHangChiTietRepo.findById(id);
        if (ctOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        GioHangChiTiet ct = ctOpt.get();
        if (!ct.getGioHang().getKhachHang().getId().equals(userId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Không có quyền xóa giỏ hàng này"));
        }

        gioHangChiTietRepo.delete(ct);

        return ResponseEntity.ok(Map.of("message", "Đã xóa khỏi giỏ hàng"));
    }
}
