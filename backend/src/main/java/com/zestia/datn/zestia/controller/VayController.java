package com.zestia.datn.zestia.controller;

import com.zestia.datn.zestia.entity.*;
import com.zestia.datn.zestia.entity.Anh;
import com.zestia.datn.zestia.repository.*;
import com.zestia.datn.zestia.service.PromotionPricingService;
import com.zestia.datn.zestia.service.InventoryMovementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/vay")
@RequiredArgsConstructor
@Slf4j
public class VayController {

    private final VayRepository vayRepo;
    private final VayChiTietRepository vayCtRepo;
    private final LoaiVayRepository loaiVayRepo;
    private final ChatLieuRepository chatLieuRepo;
    private final NhaCungCapRepository nhaCungCapRepo;
    private final MauSacRepository mauSacRepo;
    private final KichThuocRepository kichThuocRepo;
    private final AnhRepository anhRepo;
    private final HuongDanKichThuocRepository sizeGuideRepo;
    private final DanhGiaRepository danhGiaRepo;
    private final PromotionPricingService promotionPricingService;
    private final InventoryMovementService inventoryMovementService;

    @GetMapping
    public List<Map<String, Object>> getAll(Authentication authentication) {
        boolean staff = hasStaffAccess(authentication);
        List<Vay> list = staff
                ? vayRepo.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "id"))
                : vayRepo.findByTrangThai((byte) 1).stream()
                        .sorted(Comparator.comparing(Vay::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                        .toList();
        return toMaps(list);
    }

    @GetMapping("/paged")
    public Map<String, Object> getPage(@RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "10") int size,
                                       @RequestParam(required = false) String q,
                                       @RequestParam(required = false) Byte status,
                                       @RequestParam(required = false) String category,
                                       Authentication authentication) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(100, Math.max(1, size));
        String keyword = normalizeFilter(q);
        String categoryFilter = normalizeFilter(category);
        Byte effectiveStatus = status;
        if (!hasStaffAccess(authentication)) effectiveStatus = Byte.valueOf((byte) 1);
        var pageable = org.springframework.data.domain.PageRequest.of(
                safePage,
                safeSize,
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "ngayTao", "id")
        );
        var result = vayRepo.findAdminPage(keyword, effectiveStatus, categoryFilter, pageable);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("content", toMaps(result.getContent()));
        response.put("page", result.getNumber());
        response.put("size", result.getSize());
        response.put("totalElements", result.getTotalElements());
        response.put("totalPages", result.getTotalPages());
        return response;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id,
                                     Authentication authentication) {
        boolean staff = hasStaffAccess(authentication);
        return vayRepo.findById(id)
                .map(v -> {
                    if (!staff && (v.getTrangThai() == null || v.getTrangThai() != 1)) {
                        return ResponseEntity.notFound().build();
                    }
                    return ResponseEntity.ok(toDetailMap(v));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public List<Map<String, Object>> search(@RequestParam String q,
                                            Authentication authentication) {
        boolean staff = hasStaffAccess(authentication);
        List<Vay> matches = vayRepo.findByTenVayContainingIgnoreCase(q).stream()
                .filter(v -> staff || (v.getTrangThai() != null && v.getTrangThai() == 1))
                .toList();
        return toMaps(matches);
    }

    private boolean hasStaffAccess(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && authentication.getAuthorities().stream()
                        .map(authority -> authority.getAuthority())
                        .anyMatch(authority -> "ROLE_Admin".equalsIgnoreCase(authority)
                                || "ROLE_NhanVien".equalsIgnoreCase(authority)
                                || "ROLE_Nh\u00E2n vi\u00EAn".equalsIgnoreCase(authority));
    }

    private static boolean activeVariant(VayChiTiet variant) {
        return variant != null && (variant.getTrangThai() == null || variant.getTrangThai() == 1);
    }

    private Integer toInt(Object val) {
        if (val == null) return null;
        if (val instanceof Number) return ((Number) val).intValue();
        try {
            return Integer.parseInt(val.toString());
        } catch (Exception e) {
            return null;
        }
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        String productName = cleanText(body.get("tenVay"));
        if (productName == null || productName.length() < 2 || productName.length() > 200) {
            return ResponseEntity.badRequest().body(Map.of("error", "Tên sản phẩm phải có từ 2 đến 200 ký tự"));
        }
        Integer status = body.get("trangThai") != null ? toInt(body.get("trangThai")) : 1;
        if (status == null || (status != 0 && status != 1)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Trạng thái sản phẩm không hợp lệ"));
        }
        Integer categoryId = toInt(body.get("idLoaiVay"));
        Integer materialId = toInt(body.get("idChatLieu"));
        LoaiVay category = categoryId != null ? loaiVayRepo.findById(categoryId).orElse(null) : null;
        ChatLieu material = materialId != null ? chatLieuRepo.findById(materialId).orElse(null) : null;
        if (category == null || material == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Vui lòng chọn loại váy và chất liệu hợp lệ"));
        }
        if (body.get("variants") == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "San pham phai co it nhat mot bien the mau sac va kich thuoc"));
        }
        ResponseEntity<?> validationError = validateVariants(body.get("variants"));
        if (validationError != null) return validationError;

        Vay v = new Vay();
        v.setTenVay(productName);
        v.setMaVay("TMP-" + UUID.randomUUID());
        v.setMoTa(cleanText(body.get("moTa")));
        v.setTrangThai(status.byteValue());
        v.setNgayTao(LocalDateTime.now());
        applyFitFields(v, body);
        v.setLoaiVay(category);
        v.setChatLieu(material);
        if (body.get("idNhaCungCap") != null) {
            Integer supplierId = toInt(body.get("idNhaCungCap"));
            NhaCungCap supplier = supplierId != null ? nhaCungCapRepo.findById(supplierId).orElse(null) : null;
            if (supplier == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Nhà cung cấp không tồn tại"));
            }
            v.setNhaCungCap(supplier);
        }

        Vay saved = vayRepo.saveAndFlush(v);
        saved.setMaVay(String.format("SP%04d", saved.getId()));
        saved = vayRepo.save(saved);

        if (body.get("variants") != null) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> variants = (List<Map<String, Object>>) body.get("variants");
            int varIdx = 1;
            for (Map<String, Object> bt : variants) {
                VayChiTiet ct = new VayChiTiet();
                ct.setVay(saved);
                ct.setMaVayChiTiet(saved.getMaVay() + "-" + String.format("%03d", varIdx++));
                BigDecimal sellingPrice = new BigDecimal(bt.get("giaBan").toString());
                ct.setGiaBan(sellingPrice);
                ct.setGiaBanGoc(sellingPrice); // Cột cũ được giữ để tương thích dữ liệu, không còn là một giá thứ hai.
                ct.setGiaNhap(toDecimal(bt.get("giaNhap")));
                ct.setAnhUrl(cleanText(bt.get("anhUrl")));
                int initialStock = bt.get("soLuong") != null ? ((Number) bt.get("soLuong")).intValue() : 0;
                ct.setSoLuong(initialStock);
                
                Integer idMau = toInt(bt.get("idMauSac"));
                if (idMau != null) {
                    mauSacRepo.findById(idMau).ifPresent(ct::setMauSac);
                }
                Integer idKich = toInt(bt.get("idKichThuoc"));
                if (idKich != null) {
                    kichThuocRepo.findById(idKich).ifPresent(ct::setKichThuoc);
                }
                
                ct.setTrangThai((byte) 1);
                ct.setNgayTao(LocalDateTime.now());
                VayChiTiet savedVariant = vayCtRepo.save(ct);
                inventoryMovementService.record(
                        savedVariant, 0, initialStock, "NHAP_KHO_BAN_DAU",
                        saved.getMaVay(), "Admin", "Tạo biến thể sản phẩm"
                );
            }
        } else if (body.get("giaBan") != null) {
            VayChiTiet ct = new VayChiTiet();
            ct.setVay(saved);
            ct.setMaVayChiTiet(saved.getMaVay() + "-001");
            BigDecimal sellingPrice = new BigDecimal(body.get("giaBan").toString());
            ct.setGiaBan(sellingPrice);
            ct.setGiaBanGoc(sellingPrice);
            ct.setSoLuong(body.get("soLuong") != null ? ((Number) body.get("soLuong")).intValue() : 0);
            ct.setTrangThai((byte) 1);
            ct.setNgayTao(LocalDateTime.now());
            vayCtRepo.save(ct);
        }

        saveSizeGuides(saved, body.get("huongDanSize"));
        return ResponseEntity.ok(toMap(saved));
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        ResponseEntity<?> validationError = validateVariants(body.get("variants"));
        if (validationError != null) return validationError;
        if (body.containsKey("tenVay")) {
            String name = cleanText(body.get("tenVay"));
            if (name == null || name.length() < 2 || name.length() > 200) {
                return ResponseEntity.badRequest().body(Map.of("error", "Tên sản phẩm phải có từ 2 đến 200 ký tự"));
            }
        }
        if (body.containsKey("trangThai")) {
            Integer status = toInt(body.get("trangThai"));
            if (status == null || (status != 0 && status != 1)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Trạng thái sản phẩm không hợp lệ"));
            }
        }

        return vayRepo.findById(id).map(v -> {
            if (body.get("tenVay") != null) v.setTenVay(cleanText(body.get("tenVay")));
            if (body.get("moTa") != null) v.setMoTa(cleanText(body.get("moTa")));
            if (body.get("trangThai") != null) v.setTrangThai(toInt(body.get("trangThai")).byteValue());
            applyFitFields(v, body);
            if (body.get("idLoaiVay") != null) {
                loaiVayRepo.findById(((Number) body.get("idLoaiVay")).intValue()).ifPresent(v::setLoaiVay);
            }
            if (body.get("idChatLieu") != null) {
                chatLieuRepo.findById(((Number) body.get("idChatLieu")).intValue()).ifPresent(v::setChatLieu);
            }
            if (body.get("idNhaCungCap") != null) {
                nhaCungCapRepo.findById(((Number) body.get("idNhaCungCap")).intValue()).ifPresent(v::setNhaCungCap);
            }
            
            Vay saved = vayRepo.save(v);
            
            if (body.get("variants") != null) {
                List<VayChiTiet> oldVariants = vayCtRepo.findByVayId(saved.getId());
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> newVariants = (List<Map<String, Object>>) body.get("variants");
                
                Set<Integer> keptIds = new HashSet<>();
                int varIdx = oldVariants.size() + 1;
                
                for (Map<String, Object> bt : newVariants) {
                    Integer idMau = toInt(bt.get("idMauSac"));
                    Integer idKich = toInt(bt.get("idKichThuoc"));
                    
                    VayChiTiet match = null;
                    for (VayChiTiet ov : oldVariants) {
                        Integer ovMau = ov.getMauSac() != null ? ov.getMauSac().getId() : null;
                        Integer ovKich = ov.getKichThuoc() != null ? ov.getKichThuoc().getId() : null;
                        if (Objects.equals(ovMau, idMau) && Objects.equals(ovKich, idKich)) {
                            match = ov;
                            break;
                        }
                    }
                    
                    if (match != null) {
                        BigDecimal sellingPrice = new BigDecimal(bt.get("giaBan").toString());
                        match.setGiaBan(sellingPrice);
                        match.setGiaBanGoc(sellingPrice);
                        if (bt.containsKey("giaNhap")) match.setGiaNhap(toDecimal(bt.get("giaNhap")));
                        match.setAnhUrl(cleanText(bt.get("anhUrl")));
                        int beforeStock = Optional.ofNullable(match.getSoLuong()).orElse(0);
                        int afterStock = bt.get("soLuong") != null ? ((Number) bt.get("soLuong")).intValue() : 0;
                        match.setSoLuong(afterStock);
                        match.setTrangThai((byte) 1);
                        vayCtRepo.save(match);
                        inventoryMovementService.record(
                                match, beforeStock, afterStock, "DIEU_CHINH_ADMIN",
                                saved.getMaVay(), "Admin", "Cập nhật tồn kho biến thể"
                        );
                        keptIds.add(match.getId());
                    } else {
                        VayChiTiet ct = new VayChiTiet();
                        ct.setVay(saved);
                        ct.setMaVayChiTiet(saved.getMaVay() + "-" + String.format("%03d", varIdx++));
                        BigDecimal sellingPrice = new BigDecimal(bt.get("giaBan").toString());
                        ct.setGiaBan(sellingPrice);
                        ct.setGiaBanGoc(sellingPrice);
                        ct.setGiaNhap(toDecimal(bt.get("giaNhap")));
                        ct.setAnhUrl(cleanText(bt.get("anhUrl")));
                        int initialStock = bt.get("soLuong") != null ? ((Number) bt.get("soLuong")).intValue() : 0;
                        ct.setSoLuong(initialStock);
                        
                        if (idMau != null) {
                            mauSacRepo.findById(idMau).ifPresent(ct::setMauSac);
                        }
                        if (idKich != null) {
                            kichThuocRepo.findById(idKich).ifPresent(ct::setKichThuoc);
                        }
                        ct.setTrangThai((byte) 1);
                        ct.setNgayTao(LocalDateTime.now());
                        VayChiTiet savedCt = vayCtRepo.save(ct);
                        inventoryMovementService.record(
                                savedCt, 0, initialStock, "NHAP_KHO_BAN_DAU",
                                saved.getMaVay(), "Admin", "Thêm biến thể sản phẩm"
                        );
                        keptIds.add(savedCt.getId());
                    }
                }
                
                for (VayChiTiet ov : oldVariants) {
                    if (!keptIds.contains(ov.getId())) {
                        ov.setTrangThai((byte) 0);
                        vayCtRepo.save(ov);
                    }
                }
            }
            
            saveSizeGuides(saved, body.get("huongDanSize"));
            return ResponseEntity.ok(toMap(saved));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/seed")
    public ResponseEntity<?> seed() {
        if (vayRepo.count() > 28) {
            return ResponseEntity.ok(Map.of("message", "Đã có đủ sản phẩm", "count", vayRepo.count()));
        }

        List<LoaiVay> loaiVays = loaiVayRepo.findAll();
        List<ChatLieu> chatLieus = chatLieuRepo.findAll();
        List<NhaCungCap> nhaCungCaps = nhaCungCapRepo.findAll();
        List<MauSac> mauSacs = mauSacRepo.findAll();
        List<KichThuoc> kichThuocs = kichThuocRepo.findAll();

        String[][] products = {
            {"Váy Lụa Tơ Tằm Hoàng Gia", "VTT001", "1", "1", "Váy lụa tơ tằm cao cấp với hoa văn truyền thống, phù hợp cho các dịp lễ hội.", "3890000", null},
            {"Váy Truyền Thống Áo Dài Cách Điệu", "VTT002", "1", "2", "Áo dài cách điệu với chất liệu voan mềm mại, tạo nên vẻ đẹp duyên dáng.", "2690000", null},
            {"Váy Truyền Thống Gấm Đỏ", "VTT003", "1", "3", "Váy gấm đỏ truyền thống với đường may tinh tế, toát lên nét đẹp phương Đông.", "4290000", "3590000"},
            {"Váy Truyền Thống Hoa Văn Cổ", "VTT004", "1", "4", "Họa tiết hoa văn cổ điển trên nền vải nhung, mang đậm nét Việt Nam.", "3190000", null},

            {"Váy Cách Tân Hiện Đại", "VCT001", "2", "1", "Sự kết hợp hoàn hảo giữa phong cách truyền thống và xu hướng hiện đại.", "2490000", null},
            {"Váy Cách Tân Phối Ren", "VCT002", "2", "2", "Điểm nhấn ren Pháp tinh tế trên nền vải lụa, tôn dáng người mặc.", "2890000", "2290000"},
            {"Váy Cách Tân Hoa Nhí", "VCT003", "2", "5", "Họa tiết hoa nhí tươi trẻ, phù hợp cho các buổi dạo phố và hẹn hò.", "1890000", null},
            {"Váy Cách Tân Minimalist", "VCT004", "2", "6", "Thiết kế tối giản với đường cắt sắc nét, dành cho phụ nữ hiện đại.", "2190000", null},

            {"Váy Dạ Hội Sequin Vàng", "VDH001", "3", "3", "Lấp lánh với sequin vàng cao cấp, nổi bật trong mọi bữa tiệc.", "6490000", null},
            {"Váy Dạ Hội Đen Huyền Bí", "VDH002", "3", "1", "Sự quyến rũ của sắc đen trên nền lụa satin, tạo nên vẻ đẹp bí ẩn.", "5890000", "4890000"},
            {"Váy Dạ Hội Xẻ Đùi Sang Trọng", "VDH003", "3", "2", "Thiết kế xẻ đùi gợi cảm nhưng vẫn giữ được sự thanh lịch.", "7290000", null},
            {"Váy Dạ Hội Ren Trắng Ngà", "VDH004", "3", "4", "Ren trắng ngà tinh khiết, lý tưởng cho các sự kiện trang trọng.", "5490000", "4590000"},

            {"Váy Công Sở Thanh Lịch", "VCS001", "4", "6", "Thiết kế chuyên nghiệp, thoải mái suốt ngày làm việc.", "1690000", null},
            {"Váy Công Sở Body Fit", "VCS002", "4", "5", "Ôm body nhẹ nhàng, tôn dáng người mặc trong mọi cuộc họp.", "1890000", "1490000"},
            {"Váy Công Sở Kẻ Sọc", "VCS003", "4", "1", "Họa tiết kẻ sọc cổ điển, phong cách Âu sang trọng.", "1790000", null},
            {"Váy Công Sở Chữ A", "VCS004", "4", "2", "Dáng chữ A thanh thoát, phù hợp cho nhiều vóc dáng.", "1990000", null},

            {"Váy Cưới Lụa Trắng Tinh Khôi", "VCU001", "5", "1", "Lụa trắng tinh khôi cho ngày trọng đại của bạn.", "8990000", null},
            {"Váy Cưới Ren Pháp Hoàng Gia", "VCU002", "5", "4", "Ren Pháp nhập khẩu, thiết kế phong cách hoàng gia.", "12990000", "9990000"},
            {"Váy Cưới Đuôi Cá Quyến Rũ", "VCU003", "5", "2", "Dáng đuôi cá tôn vóc dáng, tạo nên vẻ đẹp quyến rũ.", "7990000", null},
            {"Váy Cưới Bohemian Tự Do", "VCU004", "5", "3", "Phong cách bohemian lãng mạn cho cô dâu yêu tự do.", "6490000", "5490000"},

            {"Váy Đi Tiệc Ngắn Trẻ Trung", "VDT001", "6", "5", "Thiết kế ngắn trẻ trung, hoàn hảo cho các buổi tiệc tối.", "2290000", null},
            {"Váy Đi Tiệc Xòe Công Chúa", "VDT002", "6", "6", "Dáng xòe bồng bềnh như công chúa trong câu chuyện cổ tích.", "3290000", "2690000"},
            {"Váy Đi Tiệc Nhung Xanh", "VDT003", "6", "3", "Chất nhung xanh cổ vịt sang trọng, nổi bật trong đêm tiệc.", "2890000", null},
            {"Váy Đi Tiệc Metallic Bạc", "VDT004", "6", "1", "Ánh metallic bạc hiện đại, thu hút mọi ánh nhìn.", "3490000", "2890000"},
        };

        int created = 0;
        Random rand = new Random(42);
        for (String[] p : products) {
            if (vayRepo.existsByMaVay(p[1])) continue;
            Vay v = new Vay();
            v.setTenVay(p[0]);
            v.setMaVay(p[1]);
            int loaiIdx = Integer.parseInt(p[2]) - 1;
            int chatLieuIdx = Integer.parseInt(p[3]) - 1;
            if (loaiIdx < loaiVays.size()) v.setLoaiVay(loaiVays.get(loaiIdx));
            if (chatLieuIdx < chatLieus.size()) v.setChatLieu(chatLieus.get(chatLieuIdx));
            if (!nhaCungCaps.isEmpty()) v.setNhaCungCap(nhaCungCaps.get(rand.nextInt(nhaCungCaps.size())));
            v.setMoTa(p[4]);
            v.setTrangThai((byte) 1);
            v.setNgayTao(LocalDateTime.now().minusDays(rand.nextInt(90)));
            Vay saved = vayRepo.save(v);

            BigDecimal giaBanReal = p[6] != null ? new BigDecimal(p[6]) : new BigDecimal(p[5]);

            int numColors = Math.min(rand.nextInt(3) + 2, mauSacs.size());
            int numSizes = Math.min(rand.nextInt(3) + 3, kichThuocs.size());
            List<MauSac> selColors = new ArrayList<>(mauSacs);
            Collections.shuffle(selColors, rand);
            selColors = selColors.subList(0, numColors);

            List<KichThuoc> selSizes = new ArrayList<>(kichThuocs);
            Collections.shuffle(selSizes, rand);
            selSizes = selSizes.subList(0, numSizes);

            int varIdx = 1;
            for (MauSac ms : selColors) {
                for (KichThuoc kt : selSizes) {
                    VayChiTiet ct = new VayChiTiet();
                    ct.setVay(saved);
                    ct.setMauSac(ms);
                    ct.setKichThuoc(kt);
                    ct.setMaVayChiTiet(saved.getMaVay() + "-" + String.format("%03d", varIdx++));
                    ct.setGiaBan(giaBanReal);
                    ct.setGiaBanGoc(giaBanReal);
                    ct.setSoLuong(rand.nextInt(20) + 5);
                    ct.setTrangThai((byte) 1);
                    ct.setNgayTao(LocalDateTime.now());
                    vayCtRepo.save(ct);
                }
            }
            created++;
        }

        return ResponseEntity.ok(Map.of("message", "Đã tạo " + created + " sản phẩm mẫu", "count", created));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        return vayRepo.findById(id).map(v -> {
            v.setTrangThai((byte) 0);
            vayRepo.save(v);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }

    // ===== Quản lý ảnh sản phẩm =====

    /** Upload 1 ảnh cho sản phẩm. Lưu vào frontend/public/images/products và tạo bản ghi Anh. */
    @PostMapping("/{id}/anh")
    @Transactional
    public ResponseEntity<?> uploadAnh(@PathVariable Integer id, @RequestParam("file") MultipartFile file) {
        var vayOpt = vayRepo.findById(id);
        if (vayOpt.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "Sản phẩm không tồn tại"));
        if (file == null || file.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "Chưa chọn ảnh"));
        try {
            String imageUrl = storeImage(file, "vay" + id);
            registerFileRollback(imageUrl);

            Anh anh = new Anh();
            anh.setVay(vayOpt.get());
            anh.setAnhUrl(imageUrl);
            anh.setTrangThai((byte) 1);
            anh.setNgayTao(LocalDateTime.now());
            Anh saved = anhRepo.save(anh);
            return ResponseEntity.ok(Map.of("id", saved.getId(), "url", saved.getAnhUrl()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            log.error("Không thể lưu ảnh cho sản phẩm {}", id, e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Không thể lưu ảnh sản phẩm lúc này"));
        }
    }

    /** Upload one image for a product color and apply it to every size of that color. */
    @PostMapping("/{id}/mau/{mauSacId}/anh")
    @Transactional
    public ResponseEntity<?> uploadColorImage(@PathVariable Integer id,
                                              @PathVariable Integer mauSacId,
                                              @RequestParam("file") MultipartFile file) {
        if (vayRepo.findById(id).isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Sản phẩm không tồn tại"));
        }
        List<VayChiTiet> variants = vayCtRepo.findByVayId(id).stream()
                .filter(variant -> variant.getMauSac() != null && Objects.equals(variant.getMauSac().getId(), mauSacId))
                .toList();
        if (variants.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Màu sắc không thuộc sản phẩm này"));
        }
        try {
            String imageUrl = storeImage(file, "vay" + id + "_mau" + mauSacId);
            registerFileRollback(imageUrl);
            variants.forEach(variant -> variant.setAnhUrl(imageUrl));
            vayCtRepo.saveAll(variants);
            return ResponseEntity.ok(Map.of("url", imageUrl, "updatedVariants", variants.size()));
        } catch (IllegalArgumentException error) {
            return ResponseEntity.badRequest().body(Map.of("error", error.getMessage()));
        } catch (IOException error) {
            log.error("Không thể lưu ảnh màu {} cho sản phẩm {}", mauSacId, id, error);
            return ResponseEntity.internalServerError().body(Map.of("error", "Không thể lưu ảnh sản phẩm lúc này"));
        }
    }

    /** Xoá 1 ảnh sản phẩm theo id ảnh. */
    @DeleteMapping("/anh/{anhId}")
    public ResponseEntity<?> deleteAnh(@PathVariable Integer anhId) {
        anhRepo.deleteById(anhId);
        return ResponseEntity.ok().build();
    }

    /** Tìm thư mục lưu ảnh: ưu tiên ../frontend (chạy từ backend/) rồi ./frontend (chạy từ gốc repo). */
    private Path resolveUploadDir() {
        Path[] candidates = {
            Paths.get("..", "frontend", "public", "images", "products"),
            Paths.get("frontend", "public", "images", "products"),
        };
        for (Path c : candidates) {
            Path abs = c.toAbsolutePath().normalize();
            Path frontendDir = abs.getParent().getParent().getParent(); // .../frontend
            if (Files.exists(frontendDir)) return abs;
        }
        return candidates[0].toAbsolutePath().normalize();
    }

    private String storeImage(MultipartFile file, String prefix) throws IOException {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("Chưa chọn ảnh");
        if (file.getSize() > 10L * 1024 * 1024) throw new IllegalArgumentException("Ảnh không được vượt quá 10MB");
        byte[] bytes = file.getBytes();
        String extension = detectImageExtension(bytes);
        if (extension == null) {
            throw new IllegalArgumentException("Tệp không có định dạng ảnh JPG, PNG, WebP hoặc AVIF hợp lệ");
        }
        Path directory = resolveUploadDir();
        Files.createDirectories(directory);
        String filename = prefix + "_" + System.currentTimeMillis() + extension;
        Files.write(directory.resolve(filename), bytes);
        return "/images/products/" + filename;
    }

    private String detectImageExtension(byte[] bytes) {
        if (bytes == null || bytes.length < 12) return null;
        if ((bytes[0] & 0xff) == 0xff && (bytes[1] & 0xff) == 0xd8 && (bytes[2] & 0xff) == 0xff) {
            return ".jpg";
        }
        if ((bytes[0] & 0xff) == 0x89 && bytes[1] == 'P' && bytes[2] == 'N' && bytes[3] == 'G') {
            return ".png";
        }
        String header = new String(
                bytes, 0, Math.min(bytes.length, 12),
                java.nio.charset.StandardCharsets.ISO_8859_1
        );
        if (header.startsWith("RIFF") && "WEBP".equals(header.substring(8, 12))) return ".webp";
        String box = header.substring(4, 12);
        if (box.startsWith("ftypavif") || box.startsWith("ftypavis")) return ".avif";
        return null;
    }

    private void registerFileRollback(String imageUrl) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) return;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != TransactionSynchronization.STATUS_COMMITTED) deleteStoredImage(imageUrl);
            }
        });
    }

    private void deleteStoredImage(String imageUrl) {
        try {
            String filename = Paths.get(imageUrl).getFileName().toString();
            Files.deleteIfExists(resolveUploadDir().resolve(filename));
        } catch (Exception ignored) {
            // Database rollback remains authoritative; orphan cleanup can be retried later.
        }
    }

    @SuppressWarnings("unchecked")
    private ResponseEntity<?> validateVariants(Object variantsObj) {
        if (variantsObj == null) return null;
        if (!(variantsObj instanceof List<?> variants) || variants.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "San pham phai co it nhat mot bien the"));
        }

        Set<String> uniqueKeys = new HashSet<>();
        int idx = 1;
        for (Object obj : variants) {
            if (!(obj instanceof Map<?, ?> raw)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Bien the " + idx + " khong hop le"));
            }
            Map<String, Object> bt = (Map<String, Object>) raw;
            Integer idMau = toInt(bt.get("idMauSac"));
            Integer idKich = toInt(bt.get("idKichThuoc"));
            BigDecimal giaBan = toDecimal(bt.get("giaBan"));
            Integer soLuong = toInt(bt.get("soLuong"));

            if (idMau == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Bien the " + idx + " chua chon mau sac"));
            }
            if (idKich == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Bien the " + idx + " chua chon kich thuoc"));
            }
            if (!mauSacRepo.existsById(idMau)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Bien the " + idx + " co mau sac khong ton tai"));
            }
            if (!kichThuocRepo.existsById(idKich)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Bien the " + idx + " co kich thuoc khong ton tai"));
            }
            if (giaBan == null || giaBan.compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "Bien the " + idx + " chua co gia ban hop le"));
            }
            BigDecimal giaNhap = toDecimal(bt.get("giaNhap"));
            if (giaNhap != null && giaNhap.compareTo(BigDecimal.ZERO) < 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "Giá vốn biến thể " + idx + " không hợp lệ"));
            }
            if (soLuong == null || soLuong < 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "Bien the " + idx + " chua co so luong hop le"));
            }
            String key = idMau + "-" + idKich;
            if (!uniqueKeys.add(key)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Bien the " + idx + " bi trung mau sac va kich thuoc"));
            }
            idx++;
        }
        return null;
    }

    private BigDecimal toDecimal(Object val) {
        if (val == null) return null;
        try {
            return new BigDecimal(val.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private List<Map<String, Object>> toMaps(List<Vay> products) {
        if (products.isEmpty()) return List.of();
        List<Integer> productIds = products.stream().map(Vay::getId).toList();
        Map<Integer, List<VayChiTiet>> variantsByProduct = vayCtRepo.findByVayIdIn(productIds).stream()
                .filter(VayController::activeVariant)
                .collect(java.util.stream.Collectors.groupingBy(
                        variant -> variant.getVay().getId(),
                        LinkedHashMap::new,
                        java.util.stream.Collectors.toList()
                ));
        Map<Integer, List<Anh>> imagesByProduct = anhRepo
                .findByVayIdInAndTrangThaiOrderByIdAsc(productIds, (byte) 1).stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        image -> image.getVay().getId(),
                        LinkedHashMap::new,
                        java.util.stream.Collectors.toList()
                ));
        Map<Integer, DanhGiaRepository.ProductReviewSummary> reviewsByProduct =
                danhGiaRepo.summarizeProducts(productIds).stream()
                        .collect(java.util.stream.Collectors.toMap(
                                DanhGiaRepository.ProductReviewSummary::getProductId,
                                summary -> summary
                        ));
        return products.stream()
                .map(product -> toMap(
                        product,
                        variantsByProduct.getOrDefault(product.getId(), List.of()),
                        imagesByProduct.getOrDefault(product.getId(), List.of()),
                        reviewsByProduct.get(product.getId())
                ))
                .toList();
    }

    private static String normalizeFilter(String value) {
        if (value == null) return null;
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private Map<String, Object> toMap(Vay v) {
        List<VayChiTiet> bienThe = vayCtRepo.findByVayId(v.getId()).stream()
                .filter(VayController::activeVariant)
                .toList();
        List<Anh> anhs = anhRepo.findByVayIdAndTrangThai(v.getId(), (byte) 1);
        return toMap(v, bienThe, anhs, danhGiaRepo.summarizeProduct(v.getId()));
    }

    private Map<String, Object> toMap(
            Vay v,
            List<VayChiTiet> bienThe,
            List<Anh> anhs,
            DanhGiaRepository.ReviewSummary reviewSummary
    ) {
        return toMap(
                v,
                bienThe,
                anhs,
                reviewSummary != null ? reviewSummary.getAverage() : 0,
                reviewSummary != null ? reviewSummary.getTotal() : 0
        );
    }

    private Map<String, Object> toMap(
            Vay v,
            List<VayChiTiet> bienThe,
            List<Anh> anhs,
            DanhGiaRepository.ProductReviewSummary reviewSummary
    ) {
        return toMap(
                v,
                bienThe,
                anhs,
                reviewSummary != null ? reviewSummary.getAverage() : 0,
                reviewSummary != null ? reviewSummary.getTotal() : 0
        );
    }

    private Map<String, Object> toMap(
            Vay v,
            List<VayChiTiet> bienThe,
            List<Anh> anhs,
            Double averageRating,
            Long reviewCount
    ) {
        List<PromotionPricingService.PriceQuote> priceQuotes = bienThe.stream()
                .map(promotionPricingService::quote)
                .toList();
        PromotionPricingService.PriceQuote bestQuote = priceQuotes.stream()
                .min(Comparator.comparing(PromotionPricingService.PriceQuote::effectivePrice))
                .orElse(new PromotionPricingService.PriceQuote(
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, null, null, null
                ));
        BigDecimal minPrice = bestQuote.effectivePrice();
        BigDecimal basePrice = bestQuote.basePrice();
        int stock = bienThe.stream()
                .filter(bt -> bt.getSoLuong() != null)
                .mapToInt(VayChiTiet::getSoLuong)
                .sum();

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", v.getId());
        map.put("maVay", v.getMaVay());
        map.put("tenVay", v.getTenVay());
        map.put("idLoaiVay", v.getLoaiVay() != null ? v.getLoaiVay().getId() : null);
        map.put("loaiVay", v.getLoaiVay() != null ? v.getLoaiVay().getTenLoaiVay() : null);
        map.put("idChatLieu", v.getChatLieu() != null ? v.getChatLieu().getId() : null);
        map.put("chatLieu", v.getChatLieu() != null ? v.getChatLieu().getTenChatLieu() : null);
        map.put("idNhaCungCap", v.getNhaCungCap() != null ? v.getNhaCungCap().getId() : null);
        map.put("giaBan", minPrice);
        map.put("giaBanCoSo", basePrice);
        map.put("coKhuyenMai", bestQuote.discounted());
        map.put("dotKhuyenMai", bestQuote.campaignName());
        map.put("maDotKhuyenMai", bestQuote.campaignCode());
        map.put("mucGiam", bestQuote.discount());
        map.put("tonKho", stock);
        map.put("trangThai", v.getTrangThai());
        map.put("moTa", v.getMoTa());
        map.put("ngayTao", v.getNgayTao());
        map.put("chieuCaoNguoiMau", v.getChieuCaoNguoiMau());
        map.put("canNangNguoiMau", v.getCanNangNguoiMau());
        map.put("sizeNguoiMau", v.getSizeNguoiMau());
        map.put("moTaPhom", v.getMoTaPhom());
        map.put("diemDanhGia", averageRating != null ? averageRating : 0);
        map.put("soDanhGia", reviewCount != null ? reviewCount : 0);

        if (!anhs.isEmpty()) {
            map.put("anhUrl", anhs.get(0).getAnhUrl());
            map.put("danhSachAnh", anhs.stream().map(Anh::getAnhUrl).toList());
        }
        // Danh sách ảnh kèm id (để admin sửa/xoá ảnh)
        map.put("anhList", anhs.stream().map(a -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", a.getId());
            m.put("url", a.getAnhUrl());
            return m;
        }).toList());
        return map;
    }

    private Map<String, Object> toDetailMap(Vay v) {
        List<VayChiTiet> bienThe = vayCtRepo.findByVayId(v.getId()).stream()
                .filter(VayController::activeVariant)
                .toList();
        List<Anh> anhs = anhRepo.findByVayIdAndTrangThai(v.getId(), (byte) 1);
        Map<String, Object> map = toMap(v, bienThe, anhs, danhGiaRepo.summarizeProduct(v.getId()));
        List<Map<String, Object>> variants = new ArrayList<>();
        for (VayChiTiet bt : bienThe) {
            PromotionPricingService.PriceQuote quote = promotionPricingService.quote(bt);
            Map<String, Object> btMap = new LinkedHashMap<>();
            btMap.put("id", bt.getId());
            btMap.put("maVayChiTiet", bt.getMaVayChiTiet());
            btMap.put("idMauSac", bt.getMauSac() != null ? bt.getMauSac().getId() : null);
            btMap.put("mauSac", bt.getMauSac() != null ? bt.getMauSac().getTenMauSac() : null);
            btMap.put("maHex", bt.getMauSac() != null ? bt.getMauSac().getMaHex() : null);
            btMap.put("idKichThuoc", bt.getKichThuoc() != null ? bt.getKichThuoc().getId() : null);
            btMap.put("kichThuoc", bt.getKichThuoc() != null ? bt.getKichThuoc().getTenKichThuoc() : null);
            btMap.put("giaBan", quote.effectivePrice());
            btMap.put("giaBanCoSo", quote.basePrice());
            btMap.put("coKhuyenMai", quote.discounted());
            btMap.put("dotKhuyenMai", quote.campaignName());
            btMap.put("maDotKhuyenMai", quote.campaignCode());
            btMap.put("soLuong", bt.getSoLuong());
            btMap.put("anhUrl", bt.getAnhUrl());
            btMap.put("trangThai", bt.getTrangThai());
            variants.add(btMap);
        }
        map.put("bienThe", variants);
        map.put("huongDanSize", sizeGuideRepo.findByVayIdOrderByKichThuocId(v.getId()).stream()
                .map(this::sizeGuideMap)
                .toList());
        return map;
    }

    private void applyFitFields(Vay product, Map<String, Object> body) {
        if (body.containsKey("chieuCaoNguoiMau")) product.setChieuCaoNguoiMau(toInt(body.get("chieuCaoNguoiMau")));
        if (body.containsKey("canNangNguoiMau")) product.setCanNangNguoiMau(toInt(body.get("canNangNguoiMau")));
        if (body.containsKey("sizeNguoiMau")) product.setSizeNguoiMau(cleanText(body.get("sizeNguoiMau")));
        if (body.containsKey("moTaPhom")) product.setMoTaPhom(cleanText(body.get("moTaPhom")));
    }

    @SuppressWarnings("unchecked")
    private void saveSizeGuides(Vay product, Object guideObject) {
        if (guideObject instanceof List<?> rawGuides) {
            for (Object raw : rawGuides) {
                if (!(raw instanceof Map<?, ?> guide)) continue;
                Integer sizeId = toInt(guide.get("idKichThuoc"));
                if (sizeId == null) continue;
                KichThuoc size = kichThuocRepo.findById(sizeId).orElse(null);
                if (size == null) continue;
                HuongDanKichThuoc entity = sizeGuideRepo.findByVayIdAndKichThuocId(product.getId(), sizeId)
                        .orElseGet(() -> HuongDanKichThuoc.builder().vay(product).kichThuoc(size).build());
                entity.setChieuCaoTu(toInt(guide.get("chieuCaoTu")));
                entity.setChieuCaoDen(toInt(guide.get("chieuCaoDen")));
                entity.setCanNangTu(toInt(guide.get("canNangTu")));
                entity.setCanNangDen(toInt(guide.get("canNangDen")));
                entity.setVongNgucTu(toInt(guide.get("vongNgucTu")));
                entity.setVongNgucDen(toInt(guide.get("vongNgucDen")));
                entity.setVongEoTu(toInt(guide.get("vongEoTu")));
                entity.setVongEoDen(toInt(guide.get("vongEoDen")));
                entity.setVongMongTu(toInt(guide.get("vongMongTu")));
                entity.setVongMongDen(toInt(guide.get("vongMongDen")));
                entity.setGhiChu(cleanText(guide.get("ghiChu")));
                sizeGuideRepo.save(entity);
            }
        }
        ensureDefaultGuides(product);
    }

    private void ensureDefaultGuides(Vay product) {
        Set<Integer> sizeIds = vayCtRepo.findByVayId(product.getId()).stream()
                .filter(VayController::activeVariant)
                .map(VayChiTiet::getKichThuoc)
                .filter(Objects::nonNull)
                .map(KichThuoc::getId)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        for (Integer sizeId : sizeIds) {
            if (sizeGuideRepo.findByVayIdAndKichThuocId(product.getId(), sizeId).isPresent()) continue;
            KichThuoc size = kichThuocRepo.findById(sizeId).orElse(null);
            if (size == null) continue;
            int[] values = defaultMeasurements(size.getTenKichThuoc());
            sizeGuideRepo.save(HuongDanKichThuoc.builder()
                    .vay(product)
                    .kichThuoc(size)
                    .chieuCaoTu(values[0]).chieuCaoDen(values[1])
                    .canNangTu(values[2]).canNangDen(values[3])
                    .vongNgucTu(values[4]).vongNgucDen(values[5])
                    .vongEoTu(values[6]).vongEoDen(values[7])
                    .vongMongTu(values[8]).vongMongDen(values[9])
                    .ghiChu("Số đo tham khảo, ưu tiên đối chiếu phom sản phẩm")
                    .build());
        }
    }

    private int[] defaultMeasurements(String sizeName) {
        String size = sizeName != null ? sizeName.trim().toUpperCase(Locale.ROOT) : "M";
        return switch (size) {
            case "XS" -> new int[]{148, 160, 36, 43, 76, 80, 58, 62, 82, 86};
            case "S" -> new int[]{150, 163, 40, 48, 80, 84, 62, 66, 86, 90};
            case "L" -> new int[]{155, 170, 57, 64, 88, 92, 70, 74, 94, 98};
            case "XL" -> new int[]{158, 175, 65, 72, 92, 98, 74, 80, 98, 104};
            case "XXL" -> new int[]{158, 178, 73, 82, 98, 104, 80, 86, 104, 110};
            default -> new int[]{152, 168, 49, 56, 84, 88, 66, 70, 90, 94};
        };
    }

    private Map<String, Object> sizeGuideMap(HuongDanKichThuoc guide) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", guide.getId());
        map.put("idKichThuoc", guide.getKichThuoc().getId());
        map.put("size", guide.getKichThuoc().getTenKichThuoc());
        map.put("chieuCaoTu", guide.getChieuCaoTu());
        map.put("chieuCaoDen", guide.getChieuCaoDen());
        map.put("canNangTu", guide.getCanNangTu());
        map.put("canNangDen", guide.getCanNangDen());
        map.put("vongNgucTu", guide.getVongNgucTu());
        map.put("vongNgucDen", guide.getVongNgucDen());
        map.put("vongEoTu", guide.getVongEoTu());
        map.put("vongEoDen", guide.getVongEoDen());
        map.put("vongMongTu", guide.getVongMongTu());
        map.put("vongMongDen", guide.getVongMongDen());
        map.put("ghiChu", guide.getGhiChu());
        return map;
    }

    private String cleanText(Object value) {
        if (value == null) return null;
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }
}
