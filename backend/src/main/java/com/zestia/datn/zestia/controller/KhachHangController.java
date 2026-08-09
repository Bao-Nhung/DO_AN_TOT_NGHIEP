package com.zestia.datn.zestia.controller;

import com.zestia.datn.zestia.entity.Anh;
import com.zestia.datn.zestia.entity.HoaDon;
import com.zestia.datn.zestia.entity.HoaDonChiTiet;
import com.zestia.datn.zestia.entity.KhachHang;
import com.zestia.datn.zestia.repository.AnhRepository;
import com.zestia.datn.zestia.repository.HoaDonChiTietRepository;
import com.zestia.datn.zestia.repository.HoaDonRepository;
import com.zestia.datn.zestia.repository.KhachHangRepository;
import com.zestia.datn.zestia.repository.DiaChiRepository;
import com.zestia.datn.zestia.service.CustomerIdentityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/khach-hang")
@RequiredArgsConstructor
public class KhachHangController {
    private static final Pattern PHONE_PATTERN = Pattern.compile("0[35789]\\d{8}");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);

    private final KhachHangRepository khachHangRepo;
    private final DiaChiRepository diaChiRepo;
    private final HoaDonRepository hoaDonRepo;
    private final HoaDonChiTietRepository hoaDonChiTietRepo;
    private final AnhRepository anhRepo;
    private final CustomerIdentityService customerIdentityService;

    @GetMapping
    public List<Map<String, Object>> getAll() {
        return khachHangRepo.findCustomerSummaries().stream().map(this::toMap).toList();
    }

    @GetMapping("/paged")
    public Map<String, Object> getPage(@RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "10") int size,
                                       @RequestParam(required = false) String q) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(100, Math.max(1, size));
        String keyword = q == null || q.isBlank() ? null : q.trim();
        var result = khachHangRepo.findCustomerSummaries(
                keyword,
                org.springframework.data.domain.PageRequest.of(safePage, safeSize)
        );
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("content", result.getContent().stream().map(this::toMap).toList());
        response.put("page", result.getNumber());
        response.put("size", result.getSize());
        response.put("totalElements", result.getTotalElements());
        response.put("totalPages", result.getTotalPages());
        return response;
    }

    @GetMapping("/{id}/addresses")
    public List<Map<String, Object>> getAddresses(@PathVariable Integer id) {
        if (!khachHangRepo.existsById(id)) return List.of();
        return diaChiRepo.findByKhachHangId(id).stream()
                .sorted(Comparator.comparing(address -> address.getMacDinh() == null ? 0 : address.getMacDinh(), Comparator.reverseOrder()))
                .map(address -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", address.getId());
                    map.put("tinhThanhPho", address.getTinhThanhPho());
                    map.put("quanHuyen", address.getQuanHuyen());
                    map.put("xaPhuong", address.getXaPhuong());
                    map.put("duong", address.getDuong());
                    map.put("macDinh", address.getMacDinh());
                    return map;
                })
                .toList();
    }

    @GetMapping("/search")
    public List<Map<String, Object>> searchForPos(@RequestParam String q) {
        String keyword = q == null ? null : q.trim();
        if (keyword == null || keyword.length() < 2) return List.of();
        return khachHangRepo.searchForPos(
                        keyword,
                        org.springframework.data.domain.PageRequest.of(0, 10)
                ).stream()
                .map(this::toLookupMap)
                .toList();
    }

    @PostMapping("/quick")
    public ResponseEntity<?> quickCreate(@RequestBody Map<String, Object> body) {
        String name = clean(body.get("hoVaTen"));
        String phone = customerIdentityService.normalizePhone(clean(body.get("soDienThoai")));
        String email = customerIdentityService.normalizeEmail(clean(body.get("email")));
        if (name == null || name.length() < 2 || name.length() > 150) {
            return ResponseEntity.badRequest().body(Map.of("error", "Vui lòng nhập họ tên khách hàng hợp lệ"));
        }
        if (phone == null || !PHONE_PATTERN.matcher(phone).matches()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Số điện thoại phải có 10 chữ số và đúng đầu số Việt Nam"));
        }
        if (email != null && !EMAIL_PATTERN.matcher(email).matches()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email khách hàng không hợp lệ"));
        }
        try {
            return ResponseEntity.ok(toLookupMap(
                    customerIdentityService.resolveForOrder(null, name, phone, email)
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<?> purchaseHistory(@PathVariable Integer id) {
        if (!khachHangRepo.existsById(id)) return ResponseEntity.notFound().build();
        List<HoaDon> orders = hoaDonRepo.findByCustomerIdOrderByLatest(id);
        if (orders.isEmpty()) return ResponseEntity.ok(List.of());

        List<Integer> orderIds = orders.stream().map(HoaDon::getId).toList();
        List<HoaDonChiTiet> details = hoaDonChiTietRepo.findByHoaDonIdIn(orderIds);
        Map<Integer, List<HoaDonChiTiet>> detailsByOrder = details.stream()
                .collect(Collectors.groupingBy(detail -> detail.getHoaDon().getId()));
        List<Integer> productIds = details.stream()
                .map(HoaDonChiTiet::getSanPhamChiTiet)
                .filter(Objects::nonNull)
                .map(detail -> detail.getSanPham())
                .filter(Objects::nonNull)
                .map(product -> product.getId())
                .distinct()
                .toList();
        Map<Integer, String> firstImages = new HashMap<>();
        if (!productIds.isEmpty()) {
            for (Anh image : anhRepo.findBySanPhamIdInAndTrangThaiOrderByIdAsc(productIds, (byte) 1)) {
                firstImages.putIfAbsent(image.getSanPham().getId(), image.getAnhUrl());
            }
        }

        List<Map<String, Object>> result = orders.stream()
                .map(order -> historyMap(order, detailsByOrder.getOrDefault(order.getId(), List.of()), firstImages))
                .toList();
        return ResponseEntity.ok(result);
    }

    private Map<String, Object> historyMap(HoaDon order, List<HoaDonChiTiet> details,
                                           Map<Integer, String> firstImages) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", order.getId());
        map.put("maHoaDon", order.getMaHoaDon());
        map.put("kenhBan", order.getHinhThucNhanHang() != null && order.getHinhThucNhanHang() == 0 ? "OFFLINE" : "ONLINE");
        map.put("hinhThucNhanHang", order.getHinhThucNhanHang());
        map.put("hinhThucThanhToan", order.getHinhThucThanhToan());
        map.put("trangThai", order.getTrangThai());
        map.put("daThanhToan", Boolean.TRUE.equals(order.getDaThanhToan()));
        map.put("tongTien", Optional.ofNullable(order.getTongTien()).orElse(BigDecimal.ZERO));
        map.put("ngayTao", order.getNgayTao());
        map.put("nhanVien", order.getNhanVien() != null ? order.getNhanVien().getHoVaTen() : null);
        map.put("items", details.stream().map(detail -> historyItemMap(detail, firstImages)).toList());
        return map;
    }

    private Map<String, Object> historyItemMap(HoaDonChiTiet detail, Map<Integer, String> firstImages) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", detail.getId());
        map.put("soLuong", detail.getSoLuong());
        map.put("donGia", detail.getDonGia());
        if (detail.getSanPhamChiTiet() != null) {
            var variant = detail.getSanPhamChiTiet();
            var product = variant.getSanPham();
            map.put("variantId", variant.getId());
            map.put("maBienThe", variant.getMaSanPhamChiTiet());
            map.put("productId", product != null ? product.getId() : null);
            map.put("maSanPham", product != null ? product.getMaSanPham() : null);
            map.put("maVay", product != null ? product.getMaSanPham() : null);
            map.put("tenSanPham", product != null ? product.getTenSanPham() : null);
            map.put("tenVay", product != null ? product.getTenSanPham() : null);
            map.put("mauSac", variant.getMauSac() != null ? variant.getMauSac().getTenMauSac() : null);
            map.put("kichThuoc", variant.getKichThuoc() != null ? variant.getKichThuoc().getTenKichThuoc() : null);
            map.put("anhUrl", variant.getAnhUrl() != null ? variant.getAnhUrl()
                    : product != null ? firstImages.get(product.getId()) : null);
        }
        return map;
    }

    private Map<String, Object> toLookupMap(KhachHang customer) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", customer.getId());
        map.put("maKhachHang", customer.getMaKhachHang());
        map.put("hoVaTen", customer.getHoVaTen());
        map.put("soDienThoai", customer.getSoDienThoai());
        map.put("email", customer.getEmail());
        map.put("diemTichLuy", customer.getDiemTichLuy() != null ? customer.getDiemTichLuy() : 0);
        map.put("hangThanhVien", customer.getHangThanhVien() != null ? customer.getHangThanhVien() : "Đồng");
        return map;
    }

    private String clean(Object value) {
        if (value == null) return null;
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }

    private Map<String, Object> toMap(KhachHangRepository.KhachHangSummary kh) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", kh.getId());
        map.put("maKhachHang", kh.getMaKhachHang());
        map.put("hoVaTen", kh.getHoVaTen());
        map.put("soDienThoai", kh.getSoDienThoai());
        map.put("email", kh.getEmail());
        map.put("gioiTinh", kh.getGioiTinh());
        map.put("diemTichLuy", kh.getDiemTichLuy() != null ? kh.getDiemTichLuy() : 0);
        map.put("hangThanhVien", kh.getHangThanhVien() != null ? kh.getHangThanhVien() : "Đồng");
        map.put("tongDon", kh.getTongDon() != null ? kh.getTongDon() : 0);
        map.put("tongChiTieu", kh.getTongChiTieu());
        map.put("ngayTao", kh.getNgayTao());
        return map;
    }
}
