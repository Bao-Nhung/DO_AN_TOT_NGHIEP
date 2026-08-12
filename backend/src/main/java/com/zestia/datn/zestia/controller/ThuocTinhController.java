package com.zestia.datn.zestia.controller;

import com.zestia.datn.zestia.entity.*;
import com.zestia.datn.zestia.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/thuoc-tinh")
@RequiredArgsConstructor
public class ThuocTinhController {
    private static final Pattern HEX_COLOR = Pattern.compile("^#[0-9A-Fa-f]{6}$");
    private static final Pattern EMAIL = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);
    private static final Pattern PHONE = Pattern.compile("^0\\d{9,10}$");

    private final MauSacRepository mauSacRepo;
    private final KichThuocRepository kichThuocRepo;
    private final ChatLieuRepository chatLieuRepo;
    private final LoaiSanPhamRepository loaiSanPhamRepo;
    private final NhaCungCapRepository nhaCungCapRepo;
    private final SanPhamRepository sanPhamRepo;
    private final SanPhamChiTietRepository sanPhamChiTietRepo;

    @GetMapping("/mau-sac")
    public List<Map<String, Object>> getMauSac(Authentication authentication) {
        return mauSacRepo.findAll().stream()
                .filter(item -> isAdmin(authentication) || isActive(item.getTrangThai()))
                .map(this::toMap).toList();
    }

    @GetMapping("/kich-thuoc")
    public List<Map<String, Object>> getKichThuoc(Authentication authentication) {
        return kichThuocRepo.findAll().stream()
                .filter(item -> isAdmin(authentication) || isActive(item.getTrangThai()))
                .map(this::toMap).toList();
    }

    @GetMapping("/chat-lieu")
    public List<Map<String, Object>> getChatLieu(Authentication authentication) {
        return chatLieuRepo.findAll().stream()
                .filter(item -> isAdmin(authentication) || isActive(item.getTrangThai()))
                .map(this::toMap).toList();
    }

    @GetMapping("/loai-san-pham")
    public List<Map<String, Object>> getLoaiSanPham(Authentication authentication) {
        return loaiSanPhamRepo.findAll().stream()
                .filter(item -> isAdmin(authentication) || isActive(item.getTrangThai()))
                .map(this::toMap).toList();
    }

    @GetMapping("/nha-cung-cap")
    public Object getNhaCungCap(Authentication authentication) {
        return nhaCungCapRepo.findAll().stream()
                .filter(item -> isAdmin(authentication) || isActive(item.getTrangThai()))
                .toList();
    }

    @GetMapping
    public Map<String, Object> getAll(Authentication authentication) {
        boolean includeInactive = isAdmin(authentication);
        List<Map<String, Object>> categories = loaiSanPhamRepo.findAll().stream()
                .filter(item -> includeInactive || isActive(item.getTrangThai()))
                .map(this::toMap).toList();
        return Map.of(
            "mauSac", mauSacRepo.findAll().stream().filter(item -> includeInactive || isActive(item.getTrangThai())).map(this::toMap).toList(),
            "kichThuoc", kichThuocRepo.findAll().stream().filter(item -> includeInactive || isActive(item.getTrangThai())).map(this::toMap).toList(),
            "chatLieu", chatLieuRepo.findAll().stream().filter(item -> includeInactive || isActive(item.getTrangThai())).map(this::toMap).toList(),
            "loaiSanPham", categories
        );
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_Admin".equalsIgnoreCase(authority.getAuthority()));
    }

    private boolean isActive(Byte status) {
        return Byte.valueOf((byte) 1).equals(status);
    }

    private Map<String, Object> toMap(MauSac item) {
        Map<String, Object> map = commonMap(item.getId(), item.getTrangThai(), item.getNgayTao());
        map.put("tenMauSac", item.getTenMauSac());
        map.put("maHex", item.getMaHex());
        return map;
    }

    private Map<String, Object> toMap(KichThuoc item) {
        Map<String, Object> map = commonMap(item.getId(), item.getTrangThai(), item.getNgayTao());
        map.put("tenKichThuoc", item.getTenKichThuoc());
        map.put("moTa", item.getMoTa());
        return map;
    }

    private Map<String, Object> toMap(ChatLieu item) {
        Map<String, Object> map = commonMap(item.getId(), item.getTrangThai(), item.getNgayTao());
        map.put("tenChatLieu", item.getTenChatLieu());
        map.put("moTa", item.getMoTa());
        return map;
    }

    private Map<String, Object> toMap(LoaiSanPham item) {
        Map<String, Object> map = commonMap(item.getId(), item.getTrangThai(), item.getNgayTao());
        map.put("tenLoaiSanPham", item.getTenLoaiSanPham());
        map.put("moTa", item.getMoTa());
        return map;
    }

    private Map<String, Object> commonMap(Integer id, Byte status, LocalDateTime createdAt) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", id);
        map.put("trangThai", status);
        map.put("ngayTao", createdAt);
        return map;
    }

    // --- Màu sắc CRUD ---
    @PostMapping("/mau-sac")
    public ResponseEntity<?> addMauSac(@RequestBody MauSac ms) {
        if (ms == null) throw badRequest("Dữ liệu màu sắc không hợp lệ");
        ms.setId(null);
        ms.setTenMauSac(required(ms.getTenMauSac(), "Tên màu sắc", 2, 100));
        ms.setMaHex(colorHex(ms.getMaHex()));
        rejectDuplicateColor(ms, null);
        ms.setNgayTao(LocalDateTime.now());
        ms.setTrangThai(status(ms.getTrangThai(), (byte) 1));
        return ResponseEntity.ok(mauSacRepo.save(ms));
    }

    @PutMapping("/mau-sac/{id}")
    public ResponseEntity<?> updateMauSac(@PathVariable Integer id, @RequestBody MauSac ms) {
        return mauSacRepo.findById(id).map(existing -> {
            if (ms == null) throw badRequest("Dữ liệu màu sắc không hợp lệ");
            existing.setTenMauSac(required(ms.getTenMauSac(), "Tên màu sắc", 2, 100));
            existing.setMaHex(colorHex(ms.getMaHex()));
            rejectDuplicateColor(existing, id);
            if (ms.getTrangThai() != null) {
                Byte nextStatus = status(ms.getTrangThai(), existing.getTrangThai());
                if (!isActive(nextStatus) && sanPhamChiTietRepo.existsByMauSacIdAndTrangThai(id, (byte) 1)) {
                    throw conflict("Không thể ngừng màu sắc đang được biến thể hoạt động sử dụng");
                }
                existing.setTrangThai(nextStatus);
            }
            return ResponseEntity.ok(mauSacRepo.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    // --- Kích thước CRUD ---
    @PostMapping("/kich-thuoc")
    public ResponseEntity<?> addKichThuoc(@RequestBody KichThuoc kt) {
        if (kt == null) throw badRequest("Dữ liệu kích thước không hợp lệ");
        kt.setId(null);
        kt.setTenKichThuoc(required(kt.getTenKichThuoc(), "Tên kích thước", 1, 50).toUpperCase(Locale.ROOT));
        kt.setMoTa(optional(kt.getMoTa(), "Mô tả", 2000));
        if (kichThuocRepo.existsByTenKichThuocIgnoreCase(kt.getTenKichThuoc())) throw conflict("Kích thước đã tồn tại");
        kt.setNgayTao(LocalDateTime.now());
        kt.setTrangThai(status(kt.getTrangThai(), (byte) 1));
        return ResponseEntity.ok(kichThuocRepo.save(kt));
    }

    @PutMapping("/kich-thuoc/{id}")
    public ResponseEntity<?> updateKichThuoc(@PathVariable Integer id, @RequestBody KichThuoc kt) {
        return kichThuocRepo.findById(id).map(existing -> {
            if (kt == null) throw badRequest("Dữ liệu kích thước không hợp lệ");
            String name = required(kt.getTenKichThuoc(), "Tên kích thước", 1, 50).toUpperCase(Locale.ROOT);
            if (kichThuocRepo.existsByTenKichThuocIgnoreCaseAndIdNot(name, id)) throw conflict("Kích thước đã tồn tại");
            existing.setTenKichThuoc(name);
            existing.setMoTa(optional(kt.getMoTa(), "Mô tả", 2000));
            if (kt.getTrangThai() != null) {
                Byte nextStatus = status(kt.getTrangThai(), existing.getTrangThai());
                if (!isActive(nextStatus) && sanPhamChiTietRepo.existsByKichThuocIdAndTrangThai(id, (byte) 1)) {
                    throw conflict("Không thể ngừng kích thước đang được biến thể hoạt động sử dụng");
                }
                existing.setTrangThai(nextStatus);
            }
            return ResponseEntity.ok(kichThuocRepo.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    // --- Chất liệu CRUD ---
    @PostMapping("/chat-lieu")
    public ResponseEntity<?> addChatLieu(@RequestBody ChatLieu cl) {
        if (cl == null) throw badRequest("Dữ liệu chất liệu không hợp lệ");
        cl.setId(null);
        cl.setTenChatLieu(required(cl.getTenChatLieu(), "Tên chất liệu", 2, 100));
        cl.setMoTa(optional(cl.getMoTa(), "Mô tả", 2000));
        if (chatLieuRepo.existsByTenChatLieuIgnoreCase(cl.getTenChatLieu())) throw conflict("Chất liệu đã tồn tại");
        cl.setNgayTao(LocalDateTime.now());
        cl.setTrangThai(status(cl.getTrangThai(), (byte) 1));
        return ResponseEntity.ok(chatLieuRepo.save(cl));
    }

    @PutMapping("/chat-lieu/{id}")
    public ResponseEntity<?> updateChatLieu(@PathVariable Integer id, @RequestBody ChatLieu cl) {
        return chatLieuRepo.findById(id).map(existing -> {
            if (cl == null) throw badRequest("Dữ liệu chất liệu không hợp lệ");
            String name = required(cl.getTenChatLieu(), "Tên chất liệu", 2, 100);
            if (chatLieuRepo.existsByTenChatLieuIgnoreCaseAndIdNot(name, id)) throw conflict("Chất liệu đã tồn tại");
            existing.setTenChatLieu(name);
            existing.setMoTa(optional(cl.getMoTa(), "Mô tả", 2000));
            if (cl.getTrangThai() != null) {
                Byte nextStatus = status(cl.getTrangThai(), existing.getTrangThai());
                if (!isActive(nextStatus) && sanPhamRepo.existsByChatLieuIdAndTrangThai(id, (byte) 1)) {
                    throw conflict("Không thể ngừng chất liệu đang được sản phẩm hoạt động sử dụng");
                }
                existing.setTrangThai(nextStatus);
            }
            return ResponseEntity.ok(chatLieuRepo.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    // --- Loại sản phẩm CRUD ---
    @PostMapping("/loai-san-pham")
    public ResponseEntity<?> addLoaiSanPham(@RequestBody LoaiSanPham lsp) {
        if (lsp == null) throw badRequest("Dữ liệu loại sản phẩm không hợp lệ");
        lsp.setId(null);
        lsp.setTenLoaiSanPham(required(lsp.getTenLoaiSanPham(), "Tên loại sản phẩm", 2, 150));
        lsp.setMoTa(optional(lsp.getMoTa(), "Mô tả", 2000));
        if (loaiSanPhamRepo.existsByTenLoaiSanPhamIgnoreCase(lsp.getTenLoaiSanPham())) throw conflict("Loại sản phẩm đã tồn tại");
        lsp.setNgayTao(LocalDateTime.now());
        lsp.setTrangThai(status(lsp.getTrangThai(), (byte) 1));
        return ResponseEntity.ok(loaiSanPhamRepo.save(lsp));
    }

    @PutMapping("/loai-san-pham/{id}")
    public ResponseEntity<?> updateLoaiSanPham(@PathVariable Integer id, @RequestBody LoaiSanPham lsp) {
        return loaiSanPhamRepo.findById(id).map(existing -> {
            if (lsp == null) throw badRequest("Dữ liệu loại sản phẩm không hợp lệ");
            String name = required(lsp.getTenLoaiSanPham(), "Tên loại sản phẩm", 2, 150);
            if (loaiSanPhamRepo.existsByTenLoaiSanPhamIgnoreCaseAndIdNot(name, id)) throw conflict("Loại sản phẩm đã tồn tại");
            existing.setTenLoaiSanPham(name);
            existing.setMoTa(optional(lsp.getMoTa(), "Mô tả", 2000));
            if (lsp.getTrangThai() != null) {
                Byte nextStatus = status(lsp.getTrangThai(), existing.getTrangThai());
                if (!isActive(nextStatus) && sanPhamRepo.existsByLoaiSanPhamIdAndTrangThai(id, (byte) 1)) {
                    throw conflict("Không thể ngừng danh mục đang được sản phẩm hoạt động sử dụng");
                }
                existing.setTrangThai(nextStatus);
            }
            return ResponseEntity.ok(loaiSanPhamRepo.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    // --- Nhà cung cấp CRUD ---
    @PostMapping("/nha-cung-cap")
    public ResponseEntity<?> addNhaCungCap(@RequestBody NhaCungCap ncc) {
        if (ncc == null) throw badRequest("Dữ liệu nhà cung cấp không hợp lệ");
        ncc.setId(null);
        normalizeSupplier(ncc, null);
        ncc.setNgayTao(LocalDateTime.now());
        ncc.setTrangThai(status(ncc.getTrangThai(), (byte) 1));
        return ResponseEntity.ok(nhaCungCapRepo.save(ncc));
    }

    @PutMapping("/nha-cung-cap/{id}")
    public ResponseEntity<?> updateNhaCungCap(@PathVariable Integer id, @RequestBody NhaCungCap ncc) {
        return nhaCungCapRepo.findById(id).map(existing -> {
            if (ncc == null) throw badRequest("Dữ liệu nhà cung cấp không hợp lệ");
            existing.setTenNhaCungCap(ncc.getTenNhaCungCap());
            existing.setDiaChi(ncc.getDiaChi());
            existing.setSoDienThoai(ncc.getSoDienThoai());
            existing.setEmail(ncc.getEmail());
            existing.setMoTa(ncc.getMoTa());
            normalizeSupplier(existing, id);
            if (ncc.getTrangThai() != null) {
                Byte nextStatus = status(ncc.getTrangThai(), existing.getTrangThai());
                if (!isActive(nextStatus) && sanPhamRepo.existsByNhaCungCapIdAndTrangThai(id, (byte) 1)) {
                    throw conflict("Không thể ngừng nhà cung cấp đang được sản phẩm hoạt động sử dụng");
                }
                existing.setTrangThai(nextStatus);
            }
            return ResponseEntity.ok(nhaCungCapRepo.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    private void rejectDuplicateColor(MauSac color, Integer currentId) {
        boolean duplicateName = currentId == null
                ? mauSacRepo.existsByTenMauSacIgnoreCase(color.getTenMauSac())
                : mauSacRepo.existsByTenMauSacIgnoreCaseAndIdNot(color.getTenMauSac(), currentId);
        if (duplicateName) throw conflict("Tên màu sắc đã tồn tại");

        boolean duplicateHex = currentId == null
                ? mauSacRepo.existsByMaHexIgnoreCase(color.getMaHex())
                : mauSacRepo.existsByMaHexIgnoreCaseAndIdNot(color.getMaHex(), currentId);
        if (duplicateHex) throw conflict("Mã màu đã tồn tại");
    }

    private void normalizeSupplier(NhaCungCap supplier, Integer currentId) {
        String name = required(supplier.getTenNhaCungCap(), "Tên nhà cung cấp", 2, 150);
        boolean duplicate = currentId == null
                ? nhaCungCapRepo.existsByTenNhaCungCapIgnoreCase(name)
                : nhaCungCapRepo.existsByTenNhaCungCapIgnoreCaseAndIdNot(name, currentId);
        if (duplicate) throw conflict("Nhà cung cấp đã tồn tại");

        String phone = optional(supplier.getSoDienThoai(), "Số điện thoại", 20);
        if (phone != null && !PHONE.matcher(phone).matches()) {
            throw badRequest("Số điện thoại nhà cung cấp phải gồm 10 hoặc 11 chữ số và bắt đầu bằng 0");
        }
        String email = optional(supplier.getEmail(), "Email", 100);
        if (email != null && !EMAIL.matcher(email).matches()) {
            throw badRequest("Email nhà cung cấp không đúng định dạng");
        }

        supplier.setTenNhaCungCap(name);
        supplier.setDiaChi(optional(supplier.getDiaChi(), "Địa chỉ", 255));
        supplier.setSoDienThoai(phone);
        supplier.setEmail(email == null ? null : email.toLowerCase(Locale.ROOT));
        supplier.setMoTa(optional(supplier.getMoTa(), "Mô tả", 4000));
    }

    private String colorHex(String value) {
        String hex = required(value, "Mã màu", 7, 7).toUpperCase(Locale.ROOT);
        if (!HEX_COLOR.matcher(hex).matches()) throw badRequest("Mã màu phải có định dạng #RRGGBB");
        return hex;
    }

    private String required(String value, String label, int minLength, int maxLength) {
        String normalized = optional(value, label, maxLength);
        if (normalized == null || normalized.length() < minLength) {
            throw badRequest(label + " phải có ít nhất " + minLength + " ký tự");
        }
        return normalized;
    }

    private String optional(String value, String label, int maxLength) {
        if (value == null) return null;
        String normalized = value.trim().replaceAll("\\s+", " ");
        if (normalized.isEmpty()) return null;
        if (normalized.length() > maxLength) {
            throw badRequest(label + " không được vượt quá " + maxLength + " ký tự");
        }
        return normalized;
    }

    private Byte status(Byte value, Byte fallback) {
        Byte resolved = value == null ? fallback : value;
        if (resolved == null) resolved = 1;
        if (resolved != 0 && resolved != 1) throw badRequest("Trạng thái không hợp lệ");
        return resolved;
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }
}
