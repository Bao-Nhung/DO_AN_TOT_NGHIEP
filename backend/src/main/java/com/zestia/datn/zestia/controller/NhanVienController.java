package com.zestia.datn.zestia.controller;

import com.zestia.datn.zestia.entity.NhanVien;
import com.zestia.datn.zestia.entity.VaiTro;
import com.zestia.datn.zestia.repository.NhanVienRepository;
import com.zestia.datn.zestia.repository.VaiTroRepository;
import com.zestia.datn.zestia.repository.LichLamViecRepository;
import com.zestia.datn.zestia.repository.HoaDonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/nhan-vien")
@RequiredArgsConstructor
public class NhanVienController {

    private final NhanVienRepository nhanVienRepo;
    private final VaiTroRepository vaiTroRepo;
    private final PasswordEncoder passwordEncoder;
    private final LichLamViecRepository lichLamViecRepo;
    private final HoaDonRepository hoaDonRepo;

    @GetMapping
    public List<Map<String, Object>> getAll() {
        return nhanVienRepo.findAll().stream().map(this::toMap).toList();
    }

    @GetMapping("/{id}/hieu-suat")
    public ResponseEntity<?> getPerformance(@PathVariable Integer id) {
        return nhanVienRepo.findById(id).map(nv -> {
            java.math.BigDecimal totalSales = java.math.BigDecimal.ZERO;
            List<com.zestia.datn.zestia.entity.HoaDon> orders = hoaDonRepo.findByNhanVienId(id);
            long orderCount = orders.size();
            long completedOrders = 0;
            for (com.zestia.datn.zestia.entity.HoaDon hd : orders) {
                if (hd.getTrangThai() != null && hd.getTrangThai() == 4) {
                    completedOrders++;
                    if (hd.getTongTien() != null) {
                        totalSales = totalSales.add(hd.getTongTien());
                    }
                }
            }
            
            double totalHours = 0;
            List<com.zestia.datn.zestia.entity.LichLamViec> schedules = lichLamViecRepo.findByNhanVienIdOrderByNgayLamAscGioBatDauAsc(id);
            for (com.zestia.datn.zestia.entity.LichLamViec sch : schedules) {
                if (sch.getGioBatDau() != null && sch.getGioKetThuc() != null) {
                    double diff = java.time.Duration.between(sch.getGioBatDau(), sch.getGioKetThuc()).toMinutes() / 60.0;
                    totalHours += Math.max(0.0, diff);
                }
            }

            Map<String, Object> stats = new LinkedHashMap<>();
            stats.put("totalSales", totalSales);
            stats.put("totalOrders", orderCount);
            stats.put("completedOrders", completedOrders);
            stats.put("totalHours", totalHours);
            stats.put("shiftCount", schedules.size());
            return ResponseEntity.ok(stats);
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/vai-tro")
    public List<Map<String, Object>> getVaiTro() {
        return vaiTroRepo.findAll().stream().map(vt -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", vt.getId());
            map.put("tenVaiTro", vt.getTenVaiTro());
            return map;
        }).toList();
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        String hoVaTen = trim(toStringVal(body.get("hoVaTen")));
        String tenNguoiDung = trim(toStringVal(body.get("tenNguoiDung")));
        String email = trim(toStringVal(body.get("email")));
        String matKhau = toStringVal(body.get("matKhau"));

        if (isBlank(hoVaTen) || isBlank(tenNguoiDung) || isBlank(email) || isBlank(matKhau)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Vui lòng nhập họ tên, tên đăng nhập, email và mật khẩu"));
        }
        if (matKhau.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("message", "Mật khẩu phải có tối thiểu 6 ký tự"));
        }
        if (nhanVienRepo.existsByTenNguoiDung(tenNguoiDung)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Tên đăng nhập đã tồn tại"));
        }
        if (nhanVienRepo.existsByEmail(email)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email đã được sử dụng"));
        }

        NhanVien nv = new NhanVien();
        nv.setMaNhanVien(nextMaNhanVien());
        applyFields(nv, body, true);
        nv.setMatKhau(passwordEncoder.encode(matKhau));
        nv.setNgayTao(LocalDateTime.now());
        if (nv.getTinhTrangLamViec() == null) nv.setTinhTrangLamViec((byte) 1);

        return ResponseEntity.ok(toMap(nhanVienRepo.save(nv)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        return nhanVienRepo.findById(id).map(existing -> {
            String tenNguoiDung = trim(toStringVal(body.get("tenNguoiDung")));
            String email = trim(toStringVal(body.get("email")));
            String matKhau = toStringVal(body.get("matKhau"));

            if (!isBlank(tenNguoiDung) && nhanVienRepo.findByTenNguoiDung(tenNguoiDung)
                    .filter(nv -> !nv.getId().equals(id)).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Tên đăng nhập đã tồn tại"));
            }
            if (!isBlank(email) && nhanVienRepo.findByEmail(email)
                    .filter(nv -> !nv.getId().equals(id)).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Email đã được sử dụng"));
            }
            if (!isBlank(matKhau)) {
                if (matKhau.length() < 6) {
                    return ResponseEntity.badRequest().body(Map.of("message", "Mật khẩu phải có tối thiểu 6 ký tự"));
                }
                existing.setMatKhau(passwordEncoder.encode(matKhau));
            }

            applyFields(existing, body, false);
            return ResponseEntity.ok(toMap(nhanVienRepo.save(existing)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/trang-thai")
    public ResponseEntity<?> updateStatus(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        return nhanVienRepo.findById(id).map(existing -> {
            existing.setTinhTrangLamViec(parseByte(toStringVal(body.get("tinhTrangLamViec")), existing.getTinhTrangLamViec()));
            return ResponseEntity.ok(toMap(nhanVienRepo.save(existing)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        return nhanVienRepo.findById(id).map(existing -> {
            existing.setTinhTrangLamViec((byte) 0);
            return ResponseEntity.ok(toMap(nhanVienRepo.save(existing)));
        }).orElse(ResponseEntity.notFound().build());
    }

    private void applyFields(NhanVien nv, Map<String, Object> body, boolean create) {
        if (create || body.containsKey("hoVaTen")) nv.setHoVaTen(trim(toStringVal(body.get("hoVaTen"))));
        if (create || body.containsKey("tenNguoiDung")) nv.setTenNguoiDung(trim(toStringVal(body.get("tenNguoiDung"))));
        if (create || body.containsKey("email")) nv.setEmail(trim(toStringVal(body.get("email"))));
        if (body.containsKey("soDienThoai")) nv.setSoDienThoai(trim(toStringVal(body.get("soDienThoai"))));
        if (body.containsKey("diaChi")) nv.setDiaChi(trim(toStringVal(body.get("diaChi"))));
        if (body.containsKey("ngaySinh")) nv.setNgaySinh(isBlank(toStringVal(body.get("ngaySinh"))) ? null : java.time.LocalDate.parse(toStringVal(body.get("ngaySinh"))));
        if (body.containsKey("gioiTinh")) nv.setGioiTinh(parseByte(toStringVal(body.get("gioiTinh")), null));
        if (body.containsKey("tinhTrangLamViec")) nv.setTinhTrangLamViec(parseByte(toStringVal(body.get("tinhTrangLamViec")), (byte) 1));

        Integer vaiTroId = parseInteger(toStringVal(body.get("vaiTroId")));
        if (vaiTroId != null) {
            vaiTroRepo.findById(vaiTroId).ifPresent(nv::setVaiTro);
        } else if (create) {
            VaiTro defaultRole = vaiTroRepo.findByTenVaiTro("Nhân viên").orElse(null);
            nv.setVaiTro(defaultRole);
        }
    }

    private String toStringVal(Object value) {
        return value == null ? null : value.toString();
    }

    private Map<String, Object> toMap(NhanVien nv) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", nv.getId());
        map.put("maNhanVien", nv.getMaNhanVien());
        map.put("hoVaTen", nv.getHoVaTen());
        map.put("gioiTinh", nv.getGioiTinh());
        map.put("ngaySinh", nv.getNgaySinh());
        map.put("soDienThoai", nv.getSoDienThoai());
        map.put("diaChi", nv.getDiaChi());
        map.put("email", nv.getEmail());
        map.put("tenNguoiDung", nv.getTenNguoiDung());
        map.put("tinhTrangLamViec", nv.getTinhTrangLamViec());
        map.put("ngayTao", nv.getNgayTao());
        if (nv.getVaiTro() != null) {
            map.put("vaiTroId", nv.getVaiTro().getId());
            map.put("tenVaiTro", nv.getVaiTro().getTenVaiTro());
        }
        return map;
    }

    private String nextMaNhanVien() {
        long next = nhanVienRepo.count() + 1;
        String code;
        do {
            code = "NV" + String.format("%03d", next++);
        } while (nhanVienRepo.existsByMaNhanVien(code));
        return code;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private Integer parseInteger(String value) {
        if (isBlank(value)) return null;
        return Integer.parseInt(value);
    }

    private Byte parseByte(String value, Byte fallback) {
        if (isBlank(value)) return fallback;
        return Byte.parseByte(value);
    }
}
