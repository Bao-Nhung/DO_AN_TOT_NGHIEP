package com.zestia.datn.zestia.controller;

import com.zestia.datn.zestia.entity.Anh;
import com.zestia.datn.zestia.entity.HoaDon;
import com.zestia.datn.zestia.entity.HoaDonChiTiet;
import com.zestia.datn.zestia.entity.LichSuTracking;
import com.zestia.datn.zestia.repository.AnhRepository;
import com.zestia.datn.zestia.repository.HoaDonChiTietRepository;
import com.zestia.datn.zestia.repository.HoaDonRepository;
import com.zestia.datn.zestia.repository.LichSuTrackingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/hoa-don")
@RequiredArgsConstructor
public class HoaDonController {

    private final HoaDonRepository hoaDonRepo;
    private final HoaDonChiTietRepository hoaDonCtRepo;
    private final AnhRepository anhRepo;
    private final LichSuTrackingRepository lichSuTrackingRepo;

    @GetMapping
    public List<Map<String, Object>> getAll() {
        return hoaDonRepo.findAll().stream().map(this::toMap).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        return hoaDonRepo.findById(id)
                .map(hd -> ResponseEntity.ok(toDetailMap(hd)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/trang-thai")
    public ResponseEntity<?> updateStatus(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        return hoaDonRepo.findById(id).map(hd -> {
            hd.setTrangThai(((Number) body.get("trangThai")).byteValue());
            if (body.get("ghiChu") != null) {
                hd.setGhiChu((String) body.get("ghiChu"));
            }
            hoaDonRepo.save(hd);
            return ResponseEntity.ok(toMap(hd));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ===== TRACKING ENDPOINTS (MỚI) =====

    /**
     * Tra cứu đơn hàng KHÔNG CẦN đăng nhập
     * Tìm bằng mã đơn hoặc mã đơn + số điện thoại
     * 
     * @param maHoaDon Mã hóa đơn (bắt buộc)
     * @param soDienThoai Số điện thoại (tùy chọn - để xác minh)
     * @return Thông tin đơn hàng chi tiết
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchOrder(
            @RequestParam String maHoaDon,
            @RequestParam(required = false) String soDienThoai) {

        try {
            Optional<HoaDon> hoaDon;

            if (soDienThoai != null && !soDienThoai.isEmpty()) {
                // Tìm với xác minh số điện thoại
                hoaDon = hoaDonRepo.findOrderByCodeAndPhone(maHoaDon, soDienThoai);
            } else {
                // Chỉ tìm theo mã đơn
                hoaDon = hoaDonRepo.findOrderByCode(maHoaDon);
            }

            if (hoaDon.isPresent()) {
                Map<String, Object> result = toDetailMap(hoaDon.get());
                // Thêm lịch sử tracking
                List<LichSuTracking> trackingHistory = lichSuTrackingRepo
                        .findByHoaDonIdOrderByLatest(hoaDon.get().getId());
                result.put("trackingHistory", trackingHistory.stream()
                        .map(this::trackingToMap)
                        .toList());
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.status(404).body(Map.of(
                        "success", false,
                        "message", "Không tìm thấy đơn hàng"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Lỗi khi tìm kiếm: " + e.getMessage()
            ));
        }
    }

    /**
     * Lấy danh sách đơn hàng của khách hàng hiện tại (CẦN đăng nhập)
     * 
     * @param authentication Thông tin xác thực người dùng
     * @return Danh sách đơn hàng
     */
    @GetMapping("/my-orders")
    public ResponseEntity<?> getMyOrders(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "message", "Vui lòng đăng nhập"
                ));
            }

            // Lấy ID khách hàng từ authentication (có thể cần điều chỉnh theo cách lưu user)
            // Giả sử user principal chứa khachHangId
            String username = authentication.getName();
            // TODO: Lấy khachHangId từ username hoặc từ custom user details

            // Placeholder - cần điều chỉnh theo security config
            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Chưa cấu hình xác thực khách hàng"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Lỗi: " + e.getMessage()
            ));
        }
    }

    /**
     * Lấy danh sách đơn hàng của khách hàng theo ID
     * 
     * @param khachHangId ID khách hàng
     * @return Danh sách đơn hàng
     */
    @GetMapping("/customer/{khachHangId}")
    public ResponseEntity<?> getOrdersByCustomerId(@PathVariable Integer khachHangId) {
        try {
            List<HoaDon> orders = hoaDonRepo.findByCustomerIdOrderByLatest(khachHangId);
            List<Map<String, Object>> result = orders.stream()
                    .map(this::toMap)
                    .toList();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Lỗi: " + e.getMessage()
            ));
        }
    }

    /**
     * Lấy chi tiết tracking của một đơn hàng
     * 
     * @param hoaDonId ID hóa đơn
     * @return Thông tin tracking chi tiết
     */
    @GetMapping("/{hoaDonId}/tracking")
    public ResponseEntity<?> getOrderTracking(@PathVariable Integer hoaDonId) {
        try {
            Optional<HoaDon> hoaDon = hoaDonRepo.findById(hoaDonId);

            if (hoaDon.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of(
                        "success", false,
                        "message", "Không tìm thấy đơn hàng"
                ));
            }

            HoaDon order = hoaDon.get();
            List<LichSuTracking> trackingHistory = lichSuTrackingRepo
                    .findByHoaDonIdOrderByLatest(hoaDonId);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("id", order.getId());
            result.put("maHoaDon", order.getMaHoaDon());
            result.put("trangThaiTracking", order.getTrangThaiTracking());
            result.put("ngayGiaoHangDuKien", order.getNgayGiaoHangDuKien());
            result.put("ngayGiaoHangThucTe", order.getNgayGiaoHangThucTe());
            result.put("diaChiGiaoHang", order.getDiaChiGiaoHang());
            result.put("khachHang", order.getKhachHang() != null ? Map.of(
                    "hoVaTen", order.getKhachHang().getHoVaTen(),
                    "soDienThoai", order.getKhachHang().getSoDienThoai(),
                    "email", order.getKhachHang().getEmail()
            ) : null);
            result.put("trackingHistory", trackingHistory.stream()
                    .map(this::trackingToMap)
                    .toList());
            result.put("ngayTao", order.getNgayTao());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Lỗi: " + e.getMessage()
            ));
        }
    }

    /**
     * Cập nhật trạng thái tracking của đơn hàng (Admin only)
     * 
     * @param hoaDonId ID hóa đơn
     * @param body Chứa: trangThaiTracking, moTa
     * @return Đơn hàng đã cập nhật
     */
    @PutMapping("/{hoaDonId}/tracking/update")
    public ResponseEntity<?> updateOrderTracking(
            @PathVariable Integer hoaDonId,
            @RequestBody Map<String, Object> body) {

        try {
            Optional<HoaDon> hoaDon = hoaDonRepo.findById(hoaDonId);

            if (hoaDon.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of(
                        "success", false,
                        "message", "Không tìm thấy đơn hàng"
                ));
            }

            HoaDon order = hoaDon.get();
            String newStatus = (String) body.get("trangThaiTracking");
            String moTa = (String) body.getOrDefault("moTa", "");

            // Cập nhật trạng thái
            order.setTrangThaiTracking(newStatus);

            // Cập nhật ngày giao dự kiến nếu có
            if (body.containsKey("ngayGiaoHangDuKien")) {
                // Xử lý LocalDateTime từ request
                // order.setNgayGiaoHangDuKien(...);
            }

            // Cập nhật ngày giao thực tế nếu là "delivered"
            if ("delivered".equalsIgnoreCase(newStatus)) {
                order.setNgayGiaoHangThucTe(LocalDateTime.now());
            }

            hoaDonRepo.save(order);

            // Lưu lịch sử tracking
            LichSuTracking tracking = LichSuTracking.builder()
                    .hoaDon(order)
                    .trangThai(newStatus)
                    .moTa(moTa)
                    .ngayCapNhat(LocalDateTime.now())
                    .build();
            lichSuTrackingRepo.save(tracking);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Cập nhật trạng thái thành công",
                    "data", toDetailMap(order)
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Lỗi: " + e.getMessage()
            ));
        }
    }

    /**
     * Tìm đơn hàng theo số điện thoại (không cần đăng nhập)
     * 
     * @param soDienThoai Số điện thoại khách hàng
     * @return Danh sách đơn hàng
     */
    @GetMapping("/search-by-phone")
    public ResponseEntity<?> searchOrderByPhone(@RequestParam String soDienThoai) {
        try {
            List<HoaDon> orders = hoaDonRepo.findOrdersByPhoneNumber(soDienThoai);

            if (orders.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of(
                        "success", false,
                        "message", "Không tìm thấy đơn hàng nào"
                ));
            }

            List<Map<String, Object>> result = orders.stream()
                    .map(this::toMap)
                    .toList();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", result
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Lỗi: " + e.getMessage()
            ));
        }
    }

    // ===== END TRACKING ENDPOINTS =====

    private Map<String, Object> toMap(HoaDon hd) {
        long soSanPham = hoaDonCtRepo.countByHoaDonId(hd.getId());
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", hd.getId());
        map.put("maHoaDon", hd.getMaHoaDon());
        map.put("khachHang", hd.getKhachHang() != null ? hd.getKhachHang().getHoVaTen() : null);
        map.put("soDienThoai", hd.getKhachHang() != null ? hd.getKhachHang().getSoDienThoai() : null);
        map.put("soSanPham", soSanPham);
        map.put("tongTien", hd.getTongTien());
        map.put("hinhThucThanhToan", hd.getHinhThucThanhToan());
        map.put("trangThai", hd.getTrangThai());
        map.put("trangThaiTracking", hd.getTrangThaiTracking()); // MỚI
        map.put("diaChiGiaoHang", hd.getDiaChiGiaoHang());
        map.put("ghiChu", hd.getGhiChu());
        map.put("ngayTao", hd.getNgayTao());
        map.put("ngayGiaoHangDuKien", hd.getNgayGiaoHangDuKien()); // MỚI
        return map;
    }

    private Map<String, Object> toDetailMap(HoaDon hd) {
        Map<String, Object> map = toMap(hd);
        map.put("emailKhachHang", hd.getKhachHang() != null ? hd.getKhachHang().getEmail() : null);
        List<HoaDonChiTiet> chiTiets = hoaDonCtRepo.findByHoaDonId(hd.getId());
        List<Map<String, Object>> items = new ArrayList<>();
        for (HoaDonChiTiet ct : chiTiets) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", ct.getId());
            if (ct.getVayChiTiet() != null) {
                item.put("tenVay", ct.getVayChiTiet().getVay() != null
                        ? ct.getVayChiTiet().getVay().getTenVay() : null);
                item.put("mauSac", ct.getVayChiTiet().getMauSac() != null
                        ? ct.getVayChiTiet().getMauSac().getTenMauSac() : null);
                item.put("maHex", ct.getVayChiTiet().getMauSac() != null
                        ? ct.getVayChiTiet().getMauSac().getMaHex() : null);
                item.put("kichThuoc", ct.getVayChiTiet().getKichThuoc() != null
                        ? ct.getVayChiTiet().getKichThuoc().getTenKichThuoc() : null);
                if (ct.getVayChiTiet().getVay() != null) {
                    List<Anh> anhs = anhRepo.findByVayIdAndTrangThai(
                            ct.getVayChiTiet().getVay().getId(), (byte) 1);
                    item.put("anhUrl", !anhs.isEmpty() ? anhs.get(0).getAnhUrl() : null);
                }
            }
            item.put("soLuong", ct.getSoLuong());
            item.put("donGia", ct.getDonGia());
            items.add(item);
        }
        map.put("chiTiets", items);
        return map;
    }

    private Map<String, Object> trackingToMap(LichSuTracking tracking) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", tracking.getId());
        map.put("trangThai", tracking.getTrangThai());
        map.put("moTa", tracking.getMoTa());
        map.put("ngayCapNhat", tracking.getNgayCapNhat());
        return map;
    }
}