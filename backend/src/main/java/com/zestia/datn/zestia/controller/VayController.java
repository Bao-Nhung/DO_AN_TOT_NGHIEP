package com.zestia.datn.zestia.controller;

import com.zestia.datn.zestia.config.JwtUtil;
import com.zestia.datn.zestia.entity.*;
import com.zestia.datn.zestia.entity.Anh;
import com.zestia.datn.zestia.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
public class VayController {

    private final VayRepository vayRepo;
    private final VayChiTietRepository vayCtRepo;
    private final LoaiVayRepository loaiVayRepo;
    private final ChatLieuRepository chatLieuRepo;
    private final NhaCungCapRepository nhaCungCapRepo;
    private final MauSacRepository mauSacRepo;
    private final KichThuocRepository kichThuocRepo;
    private final AnhRepository anhRepo;
    private final JwtUtil jwtUtil;

    @GetMapping
    public List<Map<String, Object>> getAll(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        boolean staff = hasStaffAccess(authHeader);
        List<Vay> list = staff
                ? vayRepo.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "id"))
                : vayRepo.findByTrangThai((byte) 1).stream()
                        .sorted(Comparator.comparing(Vay::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                        .toList();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Vay v : list) {
            result.add(toMap(v));
        }
        return result;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id,
                                     @RequestHeader(value = "Authorization", required = false) String authHeader) {
        boolean staff = hasStaffAccess(authHeader);
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
                                            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        boolean staff = hasStaffAccess(authHeader);
        return vayRepo.findByTenVayContainingIgnoreCase(q).stream()
                .filter(v -> staff || (v.getTrangThai() != null && v.getTrangThai() == 1))
                .map(this::toMap).toList();
    }

    private boolean hasStaffAccess(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return false;
        try {
            String token = authHeader.substring(7);
            if (!jwtUtil.isValid(token)) return false;
            String role = jwtUtil.extractClaims(token).get("role", String.class);
            return isStaffRole(role);
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isStaffRole(String role) {
        return "Admin".equalsIgnoreCase(role)
                || "NhanVien".equalsIgnoreCase(role)
                || "Nh\u00E2n vi\u00EAn".equalsIgnoreCase(role);
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

    private String generateProductCode() {
        List<Vay> vays = vayRepo.findAll();
        int maxNum = 0;
        for (Vay v : vays) {
            String code = v.getMaVay();
            if (code != null && code.startsWith("SP")) {
                try {
                    int num = Integer.parseInt(code.substring(2));
                    if (num > maxNum) {
                        maxNum = num;
                    }
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
        }
        return String.format("SP%04d", maxNum + 1);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        if (body.get("variants") == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "San pham phai co it nhat mot bien the mau sac va kich thuoc"));
        }
        ResponseEntity<?> validationError = validateVariants(body.get("variants"));
        if (validationError != null) return validationError;

        Vay v = new Vay();
        v.setTenVay((String) body.get("tenVay"));
        v.setMaVay(generateProductCode());
        v.setMoTa((String) body.get("moTa"));
        v.setTrangThai(body.get("trangThai") != null ? ((Number) body.get("trangThai")).byteValue() : (byte) 1);
        v.setNgayTao(LocalDateTime.now());

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
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> variants = (List<Map<String, Object>>) body.get("variants");
            int varIdx = 1;
            for (Map<String, Object> bt : variants) {
                VayChiTiet ct = new VayChiTiet();
                ct.setVay(saved);
                ct.setMaVayChiTiet(saved.getMaVay() + "-" + String.format("%03d", varIdx++));
                ct.setGiaBan(new BigDecimal(bt.get("giaBan").toString()));
                if (bt.get("giaBanGoc") != null) {
                    ct.setGiaBanGoc(new BigDecimal(bt.get("giaBanGoc").toString()));
                }
                ct.setSoLuong(bt.get("soLuong") != null ? ((Number) bt.get("soLuong")).intValue() : 0);
                
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
                vayCtRepo.save(ct);
            }
        } else if (body.get("giaBan") != null) {
            VayChiTiet ct = new VayChiTiet();
            ct.setVay(saved);
            ct.setMaVayChiTiet(saved.getMaVay() + "-001");
            ct.setGiaBan(new BigDecimal(body.get("giaBan").toString()));
            if (body.get("giaBanGoc") != null) {
                ct.setGiaBanGoc(new BigDecimal(body.get("giaBanGoc").toString()));
            }
            ct.setSoLuong(body.get("soLuong") != null ? ((Number) body.get("soLuong")).intValue() : 0);
            ct.setTrangThai((byte) 1);
            ct.setNgayTao(LocalDateTime.now());
            vayCtRepo.save(ct);
        }

        return ResponseEntity.ok(toMap(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        ResponseEntity<?> validationError = validateVariants(body.get("variants"));
        if (validationError != null) return validationError;

        return vayRepo.findById(id).map(v -> {
            if (body.get("tenVay") != null) v.setTenVay((String) body.get("tenVay"));
            if (body.get("moTa") != null) v.setMoTa((String) body.get("moTa"));
            if (body.get("trangThai") != null) v.setTrangThai(((Number) body.get("trangThai")).byteValue());
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
                        match.setGiaBan(new BigDecimal(bt.get("giaBan").toString()));
                        if (bt.get("giaBanGoc") != null) {
                            match.setGiaBanGoc(new BigDecimal(bt.get("giaBanGoc").toString()));
                        }
                        match.setSoLuong(bt.get("soLuong") != null ? ((Number) bt.get("soLuong")).intValue() : 0);
                        match.setTrangThai((byte) 1);
                        vayCtRepo.save(match);
                        keptIds.add(match.getId());
                    } else {
                        VayChiTiet ct = new VayChiTiet();
                        ct.setVay(saved);
                        ct.setMaVayChiTiet(saved.getMaVay() + "-" + String.format("%03d", varIdx++));
                        ct.setGiaBan(new BigDecimal(bt.get("giaBan").toString()));
                        if (bt.get("giaBanGoc") != null) {
                            ct.setGiaBanGoc(new BigDecimal(bt.get("giaBanGoc").toString()));
                        }
                        ct.setSoLuong(bt.get("soLuong") != null ? ((Number) bt.get("soLuong")).intValue() : 0);
                        
                        if (idMau != null) {
                            mauSacRepo.findById(idMau).ifPresent(ct::setMauSac);
                        }
                        if (idKich != null) {
                            kichThuocRepo.findById(idKich).ifPresent(ct::setKichThuoc);
                        }
                        ct.setTrangThai((byte) 1);
                        ct.setNgayTao(LocalDateTime.now());
                        VayChiTiet savedCt = vayCtRepo.save(ct);
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

            BigDecimal giaBanGoc = new BigDecimal(p[5]);
            BigDecimal giaBanReal = p[6] != null ? new BigDecimal(p[6]) : giaBanGoc;

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
                    ct.setGiaBanGoc(giaBanGoc);
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
        List<VayChiTiet> variants = vayCtRepo.findByVayId(id);
        vayCtRepo.deleteAll(variants);
        vayRepo.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // ===== Quản lý ảnh sản phẩm =====

    /** Upload 1 ảnh cho sản phẩm. Lưu vào frontend/public/images/products và tạo bản ghi Anh. */
    @PostMapping("/{id}/anh")
    public ResponseEntity<?> uploadAnh(@PathVariable Integer id, @RequestParam("file") MultipartFile file) {
        var vayOpt = vayRepo.findById(id);
        if (vayOpt.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "Sản phẩm không tồn tại"));
        if (file == null || file.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "Chưa chọn ảnh"));
        try {
            Path dir = resolveUploadDir();
            Files.createDirectories(dir);
            String original = file.getOriginalFilename() == null ? "img.jpg" : file.getOriginalFilename();
            String ext = "";
            int dot = original.lastIndexOf('.');
            if (dot >= 0) ext = original.substring(dot).toLowerCase();
            if (!ext.matches("\\.(jpg|jpeg|png|webp|gif|avif)")) ext = ".jpg";
            String filename = "vay" + id + "_" + System.currentTimeMillis() + ext;
            Files.copy(file.getInputStream(), dir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);

            Anh anh = new Anh();
            anh.setVay(vayOpt.get());
            anh.setAnhUrl("/images/products/" + filename);
            anh.setTrangThai((byte) 1);
            anh.setNgayTao(LocalDateTime.now());
            Anh saved = anhRepo.save(anh);
            return ResponseEntity.ok(Map.of("id", saved.getId(), "url", saved.getAnhUrl()));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Lỗi lưu ảnh: " + e.getMessage()));
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
            BigDecimal giaBanGoc = toDecimal(bt.get("giaBanGoc"));
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
            if (giaBan == null || giaBan.compareTo(BigDecimal.ZERO) < 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "Bien the " + idx + " chua co gia ban hop le"));
            }
            if (giaBanGoc == null || giaBanGoc.compareTo(BigDecimal.ZERO) < 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "Bien the " + idx + " chua co gia goc hop le"));
            }
            if (giaBanGoc.compareTo(giaBan) < 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "Bien the " + idx + ": gia goc khong duoc nho hon gia ban"));
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

    private Map<String, Object> toMap(Vay v) {
        List<VayChiTiet> bienThe = vayCtRepo.findByVayId(v.getId()).stream()
                .filter(VayController::activeVariant)
                .toList();
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
        map.put("idLoaiVay", v.getLoaiVay() != null ? v.getLoaiVay().getId() : null);
        map.put("loaiVay", v.getLoaiVay() != null ? v.getLoaiVay().getTenLoaiVay() : null);
        map.put("idChatLieu", v.getChatLieu() != null ? v.getChatLieu().getId() : null);
        map.put("chatLieu", v.getChatLieu() != null ? v.getChatLieu().getTenChatLieu() : null);
        map.put("idNhaCungCap", v.getNhaCungCap() != null ? v.getNhaCungCap().getId() : null);
        map.put("giaBan", minPrice);
        map.put("giaBanGoc", originalPrice);
        map.put("tonKho", stock);
        map.put("trangThai", v.getTrangThai());
        map.put("moTa", v.getMoTa());
        map.put("ngayTao", v.getNgayTao());

        List<Anh> anhs = anhRepo.findByVayIdAndTrangThai(v.getId(), (byte) 1);
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
        Map<String, Object> map = toMap(v);
        List<VayChiTiet> bienThe = vayCtRepo.findByVayId(v.getId()).stream()
                .filter(VayController::activeVariant)
                .toList();
        List<Map<String, Object>> variants = new ArrayList<>();
        for (VayChiTiet bt : bienThe) {
            Map<String, Object> btMap = new LinkedHashMap<>();
            btMap.put("id", bt.getId());
            btMap.put("maVayChiTiet", bt.getMaVayChiTiet());
            btMap.put("idMauSac", bt.getMauSac() != null ? bt.getMauSac().getId() : null);
            btMap.put("mauSac", bt.getMauSac() != null ? bt.getMauSac().getTenMauSac() : null);
            btMap.put("maHex", bt.getMauSac() != null ? bt.getMauSac().getMaHex() : null);
            btMap.put("idKichThuoc", bt.getKichThuoc() != null ? bt.getKichThuoc().getId() : null);
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
