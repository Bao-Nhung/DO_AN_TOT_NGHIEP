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
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/san-pham")
@RequiredArgsConstructor
@Slf4j
public class SanPhamController {

    private static final int MAX_VARIANTS_PER_PRODUCT = 200;
    private static final int MAX_STOCK_PER_VARIANT = 1_000_000;
    private static final BigDecimal MAX_SELLING_PRICE = new BigDecimal("999999999");

    private final SanPhamRepository sanPhamRepo;
    private final SanPhamChiTietRepository sanPhamCtRepo;
    private final LoaiSanPhamRepository loaiSanPhamRepo;
    private final ChatLieuRepository chatLieuRepo;
    private final NhaCungCapRepository nhaCungCapRepo;
    private final MauSacRepository mauSacRepo;
    private final KichThuocRepository kichThuocRepo;
    private final AnhRepository anhRepo;
    private final HuongDanKichThuocRepository sizeGuideRepo;
    private final DanhGiaRepository danhGiaRepo;
    private final PromotionPricingService promotionPricingService;
    private final InventoryMovementService inventoryMovementService;
    private final BienDongTonKhoRepository bienDongTonKhoRepo;

    @GetMapping
    public List<Map<String, Object>> getAll(Authentication authentication) {
        boolean staff = hasStaffAccess(authentication);
        List<SanPham> list = staff
                ? sanPhamRepo.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "id"))
                : sanPhamRepo.findByTrangThai((byte) 1).stream()
                        .sorted(Comparator.comparing(SanPham::getId, Comparator.nullsLast(Comparator.reverseOrder())))
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
        var result = sanPhamRepo.findAdminPage(keyword, effectiveStatus, categoryFilter, pageable);
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
        return sanPhamRepo.findById(id)
                .map(v -> {
                    if (!staff && (v.getTrangThai() == null || v.getTrangThai() != 1)) {
                        return ResponseEntity.notFound().build();
                    }
                    return ResponseEntity.ok(toDetailMap(v, staff));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public List<Map<String, Object>> search(@RequestParam String q,
                                            Authentication authentication) {
        boolean staff = hasStaffAccess(authentication);
        List<SanPham> matches = sanPhamRepo.findByTenSanPhamContainingIgnoreCase(q).stream()
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

    private static boolean activeVariant(SanPhamChiTiet variant) {
        return variant != null && Byte.valueOf((byte) 1).equals(variant.getTrangThai());
    }

    private static boolean isActive(Byte status) {
        return Byte.valueOf((byte) 1).equals(status);
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
        if (body == null) return ResponseEntity.badRequest().body(Map.of("error", "Dữ liệu sản phẩm không hợp lệ"));
        String productName = cleanText(body.get("tenSanPham"));
        if (productName == null || productName.length() < 2 || productName.length() > 200) {
            return ResponseEntity.badRequest().body(Map.of("error", "Tên sản phẩm phải có từ 2 đến 200 ký tự"));
        }
        Integer status = body.get("trangThai") != null ? toInt(body.get("trangThai")) : 1;
        if (status == null || (status != 0 && status != 1)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Trạng thái sản phẩm không hợp lệ"));
        }
        Integer categoryId = toInt(body.get("idLoaiSanPham"));
        Integer materialId = toInt(body.get("idChatLieu"));
        LoaiSanPham category = categoryId != null ? loaiSanPhamRepo.findById(categoryId).orElse(null) : null;
        ChatLieu material = materialId != null ? chatLieuRepo.findById(materialId).orElse(null) : null;
        if (category == null || material == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Vui lòng chọn loại sản phẩm và chất liệu hợp lệ"));
        }
        if (!isActive(category.getTrangThai()) || !isActive(material.getTrangThai())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Không thể dùng danh mục hoặc chất liệu đã ngừng hoạt động"));
        }
        if (body.get("variants") == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Sản phẩm phải có ít nhất một biến thể màu sắc và kích thước"));
        }
        ResponseEntity<?> validationError = validateVariants(body.get("variants"));
        if (validationError != null) return validationError;
        if (status == 1 && !hasActiveVariantAfterUpdate(body.get("variants"), List.of())) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Sản phẩm đang bán phải có ít nhất một biến thể đang hoạt động"
            ));
        }
        validationError = validateProductDetails(body);
        if (validationError != null) return validationError;

        SanPham v = new SanPham();
        v.setTenSanPham(productName);
        v.setMaSanPham("TMP-" + UUID.randomUUID());
        v.setMoTa(cleanText(body.get("moTa")));
        v.setTrangThai(status.byteValue());
        v.setNgayTao(LocalDateTime.now());
        applyFitFields(v, body);
        v.setLoaiSanPham(category);
        v.setChatLieu(material);
        if (body.get("idNhaCungCap") != null) {
            Integer supplierId = toInt(body.get("idNhaCungCap"));
            NhaCungCap supplier = supplierId != null ? nhaCungCapRepo.findById(supplierId).orElse(null) : null;
            if (supplier == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Nhà cung cấp không tồn tại"));
            }
            if (!isActive(supplier.getTrangThai())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Nhà cung cấp đã ngừng hoạt động"));
            }
            v.setNhaCungCap(supplier);
        }

        SanPham saved = sanPhamRepo.saveAndFlush(v);
        saved.setMaSanPham(String.format("SP%04d", saved.getId()));
        saved = sanPhamRepo.save(saved);

        if (body.get("variants") != null) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> variants = (List<Map<String, Object>>) body.get("variants");
            int varIdx = 1;
            for (Map<String, Object> bt : variants) {
                SanPhamChiTiet ct = new SanPhamChiTiet();
                ct.setSanPham(saved);
                ct.setMaSanPhamChiTiet(saved.getMaSanPham() + "-" + String.format("%03d", varIdx++));
                BigDecimal sellingPrice = new BigDecimal(bt.get("giaBan").toString());
                ct.setGiaBan(sellingPrice);
                ct.setAnhUrl(cleanText(bt.get("anhUrl")));
                int initialStock = toInt(bt.get("soLuong"));
                ct.setSoLuong(initialStock);
                
                Integer idMau = toInt(bt.get("idMauSac"));
                if (idMau != null) {
                    mauSacRepo.findById(idMau).ifPresent(ct::setMauSac);
                }
                Integer idKich = toInt(bt.get("idKichThuoc"));
                if (idKich != null) {
                    kichThuocRepo.findById(idKich).ifPresent(ct::setKichThuoc);
                }
                
                Integer variantStatus = bt.containsKey("trangThai") ? toInt(bt.get("trangThai")) : 1;
                ct.setTrangThai(variantStatus.byteValue());
                ct.setNgayTao(LocalDateTime.now());
                SanPhamChiTiet savedVariant = sanPhamCtRepo.save(ct);
                inventoryMovementService.record(
                        savedVariant, 0, initialStock, "NHAP_KHO_BAN_DAU",
                        saved.getMaSanPham(), "Admin", "Tạo biến thể sản phẩm"
                );
            }
        }

        saveSizeGuides(saved, body.get("huongDanSize"));
        return ResponseEntity.ok(toMap(saved));
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> update(@PathVariable Integer id,
                                    @RequestBody Map<String, Object> body,
                                    Authentication authentication) {
        if (body == null) return ResponseEntity.badRequest().body(Map.of("error", "Dữ liệu sản phẩm không hợp lệ"));
        ResponseEntity<?> validationError = validateVariants(body.get("variants"));
        if (validationError != null) return validationError;
        validationError = validateProductDetails(body);
        if (validationError != null) return validationError;
        if (body.containsKey("tenSanPham")) {
            String name = cleanText(body.get("tenSanPham"));
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

        SanPham product = sanPhamRepo.findById(id).orElse(null);
        if (product == null) return ResponseEntity.notFound().build();

        LoaiSanPham category = product.getLoaiSanPham();
        if (body.containsKey("idLoaiSanPham")) {
            Integer referenceId = toInt(body.get("idLoaiSanPham"));
            category = referenceId != null ? loaiSanPhamRepo.findById(referenceId).orElse(null) : null;
            if (category == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Loại sản phẩm không tồn tại"));
            }
            if (!isActive(category.getTrangThai())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Không thể chọn loại sản phẩm đã ngừng hoạt động"));
            }
        }
        ChatLieu material = product.getChatLieu();
        if (body.containsKey("idChatLieu")) {
            Integer referenceId = toInt(body.get("idChatLieu"));
            material = referenceId != null ? chatLieuRepo.findById(referenceId).orElse(null) : null;
            if (material == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Chất liệu không tồn tại"));
            }
            if (!isActive(material.getTrangThai())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Không thể chọn chất liệu đã ngừng hoạt động"));
            }
        }
        NhaCungCap supplier = product.getNhaCungCap();
        if (body.containsKey("idNhaCungCap")) {
            Integer referenceId = toInt(body.get("idNhaCungCap"));
            supplier = referenceId != null ? nhaCungCapRepo.findById(referenceId).orElse(null) : null;
            if (body.get("idNhaCungCap") != null && referenceId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Nhà cung cấp không hợp lệ"));
            }
            if (referenceId != null && supplier == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Nhà cung cấp không tồn tại"));
            }
            if (supplier != null && !isActive(supplier.getTrangThai())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Không thể chọn nhà cung cấp đã ngừng hoạt động"));
            }
        }

        boolean variantsProvided = body.get("variants") != null;
        List<SanPhamChiTiet> oldVariants = variantsProvided
                ? sanPhamCtRepo.findBySanPhamIdForUpdate(id)
                : sanPhamCtRepo.findBySanPhamId(id);
        validationError = validateVariantIdentities(body.get("variants"), oldVariants);
        if (validationError != null) return validationError;

        int targetProductStatus = body.containsKey("trangThai")
                ? toInt(body.get("trangThai"))
                : Optional.ofNullable(product.getTrangThai()).orElse((byte) 0).intValue();
        if (targetProductStatus == 1) {
            if (category == null || material == null
                    || !isActive(category.getTrangThai()) || !isActive(material.getTrangThai())
                    || (supplier != null && !isActive(supplier.getTrangThai()))) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Sản phẩm đang bán chỉ được dùng danh mục, chất liệu và nhà cung cấp đang hoạt động"
                ));
            }
            if (!hasActiveVariantAfterUpdate(body.get("variants"), oldVariants)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Sản phẩm đang bán phải có ít nhất một biến thể đang hoạt động"
                ));
            }
        }

        if (body.containsKey("tenSanPham")) product.setTenSanPham(cleanText(body.get("tenSanPham")));
        if (body.containsKey("moTa")) product.setMoTa(cleanText(body.get("moTa")));
        if (body.containsKey("trangThai")) product.setTrangThai(toInt(body.get("trangThai")).byteValue());
        applyFitFields(product, body);
        product.setLoaiSanPham(category);
        product.setChatLieu(material);
        product.setNhaCungCap(supplier);
        SanPham saved = sanPhamRepo.save(product);

        if (body.get("variants") != null) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> newVariants = (List<Map<String, Object>>) body.get("variants");
            Map<Integer, SanPhamChiTiet> variantsById = new HashMap<>();
            oldVariants.forEach(variant -> variantsById.put(variant.getId(), variant));
            Set<Integer> keptIds = new HashSet<>();
            int nextVariantNumber = nextVariantNumber(oldVariants);
            String actor = authentication != null && authentication.getName() != null
                    ? authentication.getName()
                    : "Admin";

            for (Map<String, Object> variantPayload : newVariants) {
                Integer colorId = toInt(variantPayload.get("idMauSac"));
                Integer sizeId = toInt(variantPayload.get("idKichThuoc"));
                Integer variantId = toInt(variantPayload.get("variantId"));
                SanPhamChiTiet match = variantId != null
                        ? variantsById.get(variantId)
                        : findVariantByAttributes(oldVariants, colorId, sizeId);

                if (match != null) {
                    int beforeStock = Optional.ofNullable(match.getSoLuong()).orElse(0);
                    int afterStock = toInt(variantPayload.get("soLuong"));
                    match.setGiaBan(toDecimal(variantPayload.get("giaBan")));
                    match.setAnhUrl(cleanText(variantPayload.get("anhUrl")));
                    match.setSoLuong(afterStock);
                    if (variantPayload.containsKey("trangThai")) {
                        match.setTrangThai(toInt(variantPayload.get("trangThai")).byteValue());
                    }
                    sanPhamCtRepo.save(match);
                    inventoryMovementService.record(
                            match, beforeStock, afterStock, "DIEU_CHINH_ADMIN",
                            saved.getMaSanPham(), actor, "Cập nhật tồn kho biến thể"
                    );
                    keptIds.add(match.getId());
                    continue;
                }

                SanPhamChiTiet created = new SanPhamChiTiet();
                created.setSanPham(saved);
                created.setMaSanPhamChiTiet(saved.getMaSanPham() + "-" + String.format("%03d", nextVariantNumber++));
                created.setGiaBan(toDecimal(variantPayload.get("giaBan")));
                created.setAnhUrl(cleanText(variantPayload.get("anhUrl")));
                int initialStock = toInt(variantPayload.get("soLuong"));
                created.setSoLuong(initialStock);
                created.setMauSac(mauSacRepo.findById(colorId).orElseThrow());
                created.setKichThuoc(kichThuocRepo.findById(sizeId).orElseThrow());
                Integer variantStatus = variantPayload.containsKey("trangThai")
                        ? toInt(variantPayload.get("trangThai"))
                        : 1;
                created.setTrangThai(variantStatus.byteValue());
                created.setNgayTao(LocalDateTime.now());
                SanPhamChiTiet savedVariant = sanPhamCtRepo.save(created);
                inventoryMovementService.record(
                        savedVariant, 0, initialStock, "NHAP_KHO_BAN_DAU",
                        saved.getMaSanPham(), actor, "Thêm biến thể sản phẩm"
                );
                keptIds.add(savedVariant.getId());
            }

            for (SanPhamChiTiet oldVariant : oldVariants) {
                if (!keptIds.contains(oldVariant.getId())) {
                    oldVariant.setTrangThai((byte) 0);
                    sanPhamCtRepo.save(oldVariant);
                }
            }
        }

        saveSizeGuides(saved, body.get("huongDanSize"));
        return ResponseEntity.ok(toMap(saved));
    }

    // ===== Quản lý ảnh sản phẩm =====

    @PostMapping("/{id}/anh")
    @Transactional
    public ResponseEntity<?> uploadAnh(@PathVariable Integer id, @RequestParam("file") MultipartFile file) {
        var product = sanPhamRepo.findById(id);
        if (product.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "Sản phẩm không tồn tại"));
        if (file == null || file.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "Chưa chọn ảnh"));
        try {
            String imageUrl = storeImage(file, "sanpham" + id);
            registerFileRollback(imageUrl);

            Anh anh = new Anh();
            anh.setSanPham(product.get());
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

    @PostMapping("/{id}/mau/{mauSacId}/anh")
    @Transactional
    public ResponseEntity<?> uploadColorImage(@PathVariable Integer id,
                                              @PathVariable Integer mauSacId,
                                              @RequestParam("file") MultipartFile file) {
        if (sanPhamRepo.findById(id).isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Sản phẩm không tồn tại"));
        }
        List<SanPhamChiTiet> variants = sanPhamCtRepo.findBySanPhamId(id).stream()
                .filter(variant -> variant.getMauSac() != null && Objects.equals(variant.getMauSac().getId(), mauSacId))
                .toList();
        if (variants.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Màu sắc không thuộc sản phẩm này"));
        }
        try {
            String imageUrl = storeImage(file, "sanpham" + id + "_mau" + mauSacId);
            registerFileRollback(imageUrl);
            variants.forEach(variant -> variant.setAnhUrl(imageUrl));
            sanPhamCtRepo.saveAll(variants);
            return ResponseEntity.ok(Map.of("url", imageUrl, "updatedVariants", variants.size()));
        } catch (IllegalArgumentException error) {
            return ResponseEntity.badRequest().body(Map.of("error", error.getMessage()));
        } catch (IOException error) {
            log.error("Không thể lưu ảnh màu {} cho sản phẩm {}", mauSacId, id, error);
            return ResponseEntity.internalServerError().body(Map.of("error", "Không thể lưu ảnh sản phẩm lúc này"));
        }
    }

    @DeleteMapping("/anh/{anhId}")
    @Transactional
    public ResponseEntity<?> deleteAnh(@PathVariable Integer anhId) {
        Anh image = anhRepo.findById(anhId).orElse(null);
        if (image == null) return ResponseEntity.notFound().build();
        String imageUrl = image.getAnhUrl();
        boolean shared = imageUrl != null && (anhRepo.countByAnhUrl(imageUrl) > 1
                || sanPhamCtRepo.countByAnhUrl(imageUrl) > 0);
        anhRepo.delete(image);
        if (!shared) registerFileDeleteAfterCommit(imageUrl);
        return ResponseEntity.ok().build();
    }

    private Path resolveUploadDir() {
        Path[] candidates = {
            Paths.get("..", "frontend", "public", "images", "products"),
            Paths.get("frontend", "public", "images", "products"),
        };
        for (Path c : candidates) {
            Path abs = c.toAbsolutePath().normalize();
            Path frontendDir = abs.getParent().getParent().getParent();
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
        String filename = prefix + "_" + UUID.randomUUID().toString().replace("-", "") + extension;
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
        if (imageUrl == null || !imageUrl.startsWith("/images/products/")) return;
        try {
            String filename = Paths.get(imageUrl).getFileName().toString();
            Files.deleteIfExists(resolveUploadDir().resolve(filename));
        } catch (Exception ignored) {
        }
    }

    private void registerFileDeleteAfterCommit(String imageUrl) {
        if (imageUrl == null) return;
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            deleteStoredImage(imageUrl);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                deleteStoredImage(imageUrl);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private ResponseEntity<?> validateVariants(Object variantsObj) {
        if (variantsObj == null) return null;
        if (!(variantsObj instanceof List<?> variants) || variants.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Sản phẩm phải có ít nhất một biến thể"));
        }
        if (variants.size() > MAX_VARIANTS_PER_PRODUCT) {
            return ResponseEntity.badRequest().body(Map.of("error", "Một sản phẩm chỉ được có tối đa " + MAX_VARIANTS_PER_PRODUCT + " biến thể"));
        }

        Set<String> uniqueKeys = new HashSet<>();
        Set<Integer> uniqueVariantIds = new HashSet<>();
        int idx = 1;
        for (Object obj : variants) {
            if (!(obj instanceof Map<?, ?> raw)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Biến thể " + idx + " không hợp lệ"));
            }
            Map<String, Object> bt = (Map<String, Object>) raw;
            Integer idMau = toInt(bt.get("idMauSac"));
            Integer idKich = toInt(bt.get("idKichThuoc"));
            BigDecimal giaBan = toDecimal(bt.get("giaBan"));
            Integer soLuong = toInt(bt.get("soLuong"));
            Object rawVariantId = bt.get("variantId");
            Integer variantId = toInt(rawVariantId);
            Integer requestedStatus = bt.containsKey("trangThai") ? toInt(bt.get("trangThai")) : null;

            if (rawVariantId != null && (variantId == null || variantId <= 0)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Biến thể " + idx + " có mã định danh không hợp lệ"));
            }
            if (variantId != null && !uniqueVariantIds.add(variantId)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Mã định danh biến thể bị lặp trong yêu cầu"));
            }
            if (bt.containsKey("trangThai") && (requestedStatus == null || (requestedStatus != 0 && requestedStatus != 1))) {
                return ResponseEntity.badRequest().body(Map.of("error", "Biến thể " + idx + " có trạng thái không hợp lệ"));
            }

            if (idMau == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Biến thể " + idx + " chưa chọn màu sắc"));
            }
            if (idKich == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Biến thể " + idx + " chưa chọn kích thước"));
            }
            MauSac color = mauSacRepo.findById(idMau).orElse(null);
            if (color == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Biến thể " + idx + " có màu sắc không tồn tại"));
            }
            KichThuoc size = kichThuocRepo.findById(idKich).orElse(null);
            if (size == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Biến thể " + idx + " có kích thước không tồn tại"));
            }
            boolean requiresActiveAttributes = variantId == null
                    ? !Integer.valueOf(0).equals(requestedStatus)
                    : Integer.valueOf(1).equals(requestedStatus);
            if (requiresActiveAttributes && (!isActive(color.getTrangThai()) || !isActive(size.getTrangThai()))) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Biến thể " + idx + " không thể hoạt động với màu sắc hoặc kích thước đã ngừng"
                ));
            }
            if (giaBan == null || giaBan.compareTo(BigDecimal.ZERO) <= 0 || giaBan.compareTo(MAX_SELLING_PRICE) > 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "Biến thể " + idx + " chưa có giá bán hợp lệ"));
            }
            if (soLuong == null || soLuong < 0 || soLuong > MAX_STOCK_PER_VARIANT) {
                return ResponseEntity.badRequest().body(Map.of("error", "Biến thể " + idx + " chưa có số lượng hợp lệ"));
            }
            String key = idMau + "-" + idKich;
            if (!uniqueKeys.add(key)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Biến thể " + idx + " bị trùng màu sắc và kích thước"));
            }
            idx++;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private boolean hasActiveVariantAfterUpdate(Object variantsObj, List<SanPhamChiTiet> existingVariants) {
        if (!(variantsObj instanceof List<?> payloads)) {
            return existingVariants.stream().anyMatch(this::isSellableVariantReference);
        }
        Map<Integer, SanPhamChiTiet> existingById = new HashMap<>();
        existingVariants.forEach(variant -> existingById.put(variant.getId(), variant));
        for (Object raw : payloads) {
            if (!(raw instanceof Map<?, ?> payload)) continue;
            Integer variantId = toInt(payload.get("variantId"));
            SanPhamChiTiet existing = variantId != null ? existingById.get(variantId) : null;
            int status = payload.containsKey("trangThai")
                    ? Optional.ofNullable(toInt(payload.get("trangThai"))).orElse(0)
                    : existing != null && existing.getTrangThai() != null ? existing.getTrangThai() : 1;
            if (status != 1) continue;
            MauSac color = mauSacRepo.findById(toInt(payload.get("idMauSac"))).orElse(null);
            KichThuoc size = kichThuocRepo.findById(toInt(payload.get("idKichThuoc"))).orElse(null);
            if (color != null && size != null && isActive(color.getTrangThai()) && isActive(size.getTrangThai())) {
                return true;
            }
        }
        return false;
    }

    private boolean isSellableVariantReference(SanPhamChiTiet variant) {
        return activeVariant(variant)
                && variant.getMauSac() != null
                && isActive(variant.getMauSac().getTrangThai())
                && variant.getKichThuoc() != null
                && isActive(variant.getKichThuoc().getTrangThai());
    }

    @SuppressWarnings("unchecked")
    private ResponseEntity<?> validateVariantIdentities(Object variantsObj, List<SanPhamChiTiet> existingVariants) {
        if (variantsObj == null) return null;
        List<Map<String, Object>> variants = (List<Map<String, Object>>) variantsObj;
        Map<Integer, SanPhamChiTiet> existingById = new HashMap<>();
        existingVariants.forEach(variant -> existingById.put(variant.getId(), variant));

        for (int index = 0; index < variants.size(); index++) {
            Map<String, Object> payload = variants.get(index);
            Integer variantId = toInt(payload.get("variantId"));
            if (variantId == null) continue;
            SanPhamChiTiet existing = existingById.get(variantId);
            if (existing == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Biến thể " + (index + 1) + " không thuộc sản phẩm đang sửa"
                ));
            }
            Integer existingColorId = existing.getMauSac() != null ? existing.getMauSac().getId() : null;
            Integer existingSizeId = existing.getKichThuoc() != null ? existing.getKichThuoc().getId() : null;
            if (!Objects.equals(existingColorId, toInt(payload.get("idMauSac")))
                    || !Objects.equals(existingSizeId, toInt(payload.get("idKichThuoc")))) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Không thể đổi màu hoặc kích thước của biến thể đã tồn tại; hãy thêm biến thể mới"
                ));
            }
        }
        return null;
    }

    private SanPhamChiTiet findVariantByAttributes(List<SanPhamChiTiet> variants, Integer colorId, Integer sizeId) {
        return variants.stream()
                .filter(variant -> Objects.equals(
                        variant.getMauSac() != null ? variant.getMauSac().getId() : null,
                        colorId
                ))
                .filter(variant -> Objects.equals(
                        variant.getKichThuoc() != null ? variant.getKichThuoc().getId() : null,
                        sizeId
                ))
                .findFirst()
                .orElse(null);
    }

    private int nextVariantNumber(List<SanPhamChiTiet> variants) {
        int max = 0;
        for (SanPhamChiTiet variant : variants) {
            String code = variant.getMaSanPhamChiTiet();
            if (code == null) continue;
            int separator = code.lastIndexOf('-');
            if (separator < 0 || separator == code.length() - 1) continue;
            try {
                max = Math.max(max, Integer.parseInt(code.substring(separator + 1)));
            } catch (NumberFormatException ignored) {
            }
        }
        return max + 1;
    }

    private ResponseEntity<?> validateProductDetails(Map<String, Object> body) {
        String description = cleanText(body.get("moTa"));
        if (description != null && description.length() > 5000) {
            return ResponseEntity.badRequest().body(Map.of("error", "Mô tả sản phẩm không được vượt quá 5.000 ký tự"));
        }
        Integer height = toInt(body.get("chieuCaoNguoiMau"));
        if (body.get("chieuCaoNguoiMau") != null && (height == null || height < 120 || height > 220)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Chiều cao người mẫu phải từ 120 đến 220 cm"));
        }
        Integer weight = toInt(body.get("canNangNguoiMau"));
        if (body.get("canNangNguoiMau") != null && (weight == null || weight < 30 || weight > 200)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Cân nặng người mẫu phải từ 30 đến 200 kg"));
        }
        String modelSize = cleanText(body.get("sizeNguoiMau"));
        if (modelSize != null && modelSize.length() > 30) {
            return ResponseEntity.badRequest().body(Map.of("error", "Size người mẫu không được vượt quá 30 ký tự"));
        }
        String fitDescription = cleanText(body.get("moTaPhom"));
        if (fitDescription != null && fitDescription.length() > 500) {
            return ResponseEntity.badRequest().body(Map.of("error", "Mô tả phom không được vượt quá 500 ký tự"));
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

    private List<Map<String, Object>> toMaps(List<SanPham> products) {
        if (products.isEmpty()) return List.of();
        List<Integer> productIds = products.stream().map(SanPham::getId).toList();
        Map<Integer, List<SanPhamChiTiet>> variantsByProduct = sanPhamCtRepo.findBySanPhamIdIn(productIds).stream()
                .filter(SanPhamController::activeVariant)
                .collect(java.util.stream.Collectors.groupingBy(
                        variant -> variant.getSanPham().getId(),
                        LinkedHashMap::new,
                        java.util.stream.Collectors.toList()
                ));
        Map<Integer, List<Anh>> imagesByProduct = anhRepo
                .findBySanPhamIdInAndTrangThaiOrderByIdAsc(productIds, (byte) 1).stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        image -> image.getSanPham().getId(),
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

    private Map<String, Object> toMap(SanPham v) {
        List<SanPhamChiTiet> bienThe = sanPhamCtRepo.findBySanPhamId(v.getId()).stream()
                .filter(SanPhamController::activeVariant)
                .toList();
        List<Anh> anhs = anhRepo.findBySanPhamIdAndTrangThai(v.getId(), (byte) 1);
        return toMap(v, bienThe, anhs, danhGiaRepo.summarizeProduct(v.getId()));
    }

    private Map<String, Object> toMap(
            SanPham v,
            List<SanPhamChiTiet> bienThe,
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
            SanPham v,
            List<SanPhamChiTiet> bienThe,
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
            SanPham v,
            List<SanPhamChiTiet> bienThe,
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
                .mapToInt(SanPhamChiTiet::getSoLuong)
                .sum();

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", v.getId());
        map.put("maSanPham", v.getMaSanPham());
        map.put("tenSanPham", v.getTenSanPham());
        map.put("idLoaiSanPham", v.getLoaiSanPham() != null ? v.getLoaiSanPham().getId() : null);
        map.put("loaiSanPham", v.getLoaiSanPham() != null ? v.getLoaiSanPham().getTenLoaiSanPham() : null);
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

        String primaryImage = !anhs.isEmpty() ? anhs.get(0).getAnhUrl() : getFallbackImage(v);
        List<String> imageList = !anhs.isEmpty()
                ? anhs.stream().map(Anh::getAnhUrl).toList()
                : List.of(primaryImage);

        map.put("anhUrl", primaryImage);
        map.put("danhSachAnh", imageList);
        map.put("anhList", anhs.stream().map(a -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", a.getId());
            m.put("url", a.getAnhUrl());
            return m;
        }).toList());
        return map;
    }

    private Map<String, Object> toDetailMap(SanPham v, boolean includeInactiveVariants) {
        List<SanPhamChiTiet> allVariants = sanPhamCtRepo.findBySanPhamId(v.getId());
        List<SanPhamChiTiet> activeVariants = allVariants.stream()
                .filter(SanPhamController::activeVariant)
                .toList();
        List<SanPhamChiTiet> visibleVariants = includeInactiveVariants ? allVariants : activeVariants;
        List<Anh> anhs = anhRepo.findBySanPhamIdAndTrangThai(v.getId(), (byte) 1);
        Map<String, Object> map = toMap(v, activeVariants, anhs, danhGiaRepo.summarizeProduct(v.getId()));
        String defaultImage = (String) map.get("anhUrl");
        List<Map<String, Object>> variants = new ArrayList<>();
        for (SanPhamChiTiet bt : visibleVariants) {
            PromotionPricingService.PriceQuote quote = promotionPricingService.quote(bt);
            Map<String, Object> btMap = new LinkedHashMap<>();
            btMap.put("id", bt.getId());
            btMap.put("maSanPhamChiTiet", bt.getMaSanPhamChiTiet());
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
            btMap.put("anhUrl", bt.getAnhUrl() != null && !bt.getAnhUrl().isBlank() ? bt.getAnhUrl() : defaultImage);
            btMap.put("trangThai", bt.getTrangThai());
            variants.add(btMap);
        }
        map.put("bienThe", variants);
        map.put("huongDanSize", sizeGuideRepo.findBySanPhamIdOrderByKichThuocId(v.getId()).stream()
                .map(this::sizeGuideMap)
                .toList());
        return map;
    }

    private void applyFitFields(SanPham product, Map<String, Object> body) {
        if (body.containsKey("chieuCaoNguoiMau")) product.setChieuCaoNguoiMau(toInt(body.get("chieuCaoNguoiMau")));
        if (body.containsKey("canNangNguoiMau")) product.setCanNangNguoiMau(toInt(body.get("canNangNguoiMau")));
        if (body.containsKey("sizeNguoiMau")) product.setSizeNguoiMau(cleanText(body.get("sizeNguoiMau")));
        if (body.containsKey("moTaPhom")) product.setMoTaPhom(cleanText(body.get("moTaPhom")));
    }

    @SuppressWarnings("unchecked")
    private void saveSizeGuides(SanPham product, Object guideObject) {
        if (guideObject instanceof List<?> rawGuides) {
            for (Object raw : rawGuides) {
                if (!(raw instanceof Map<?, ?> guide)) continue;
                Integer sizeId = toInt(guide.get("idKichThuoc"));
                if (sizeId == null) continue;
                KichThuoc size = kichThuocRepo.findById(sizeId).orElse(null);
                if (size == null) continue;
                HuongDanKichThuoc entity = sizeGuideRepo.findBySanPhamIdAndKichThuocId(product.getId(), sizeId)
                        .orElseGet(() -> HuongDanKichThuoc.builder().sanPham(product).kichThuoc(size).build());
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

    private void ensureDefaultGuides(SanPham product) {
        Set<Integer> sizeIds = sanPhamCtRepo.findBySanPhamId(product.getId()).stream()
                .filter(SanPhamController::activeVariant)
                .map(SanPhamChiTiet::getKichThuoc)
                .filter(Objects::nonNull)
                .map(KichThuoc::getId)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        for (Integer sizeId : sizeIds) {
            if (sizeGuideRepo.findBySanPhamIdAndKichThuocId(product.getId(), sizeId).isPresent()) continue;
            KichThuoc size = kichThuocRepo.findById(sizeId).orElse(null);
            if (size == null) continue;
            int[] values = defaultMeasurements(size.getTenKichThuoc());
            sizeGuideRepo.save(HuongDanKichThuoc.builder()
                    .sanPham(product)
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

    @GetMapping("/stock-movements")
    public Map<String, Object> getStockMovements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String q,
            Authentication authentication) {
        if (!hasStaffAccess(authentication)) {
            throw new org.springframework.security.access.AccessDeniedException("Cần quyền Nhân viên/Admin");
        }
        int safePage = Math.max(0, page);
        int safeSize = Math.min(100, Math.max(1, size));
        var pageable = org.springframework.data.domain.PageRequest.of(
                safePage,
                safeSize,
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "ngayTao", "id")
        );
        var result = bienDongTonKhoRepo.findLogs(q, pageable);
        List<Map<String, Object>> content = result.getContent().stream().map(b -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", b.getId());
            if (b.getSanPhamChiTiet() != null) {
                map.put("sanPhamChiTietId", b.getSanPhamChiTiet().getId());
                if (b.getSanPhamChiTiet().getSanPham() != null) {
                    map.put("tenSanPham", b.getSanPhamChiTiet().getSanPham().getTenSanPham());
                    map.put("maSanPham", b.getSanPhamChiTiet().getSanPham().getMaSanPham());
                }
                if (b.getSanPhamChiTiet().getMauSac() != null) {
                    map.put("mauSac", b.getSanPhamChiTiet().getMauSac().getTenMauSac());
                }
                if (b.getSanPhamChiTiet().getKichThuoc() != null) {
                    map.put("kichThuoc", b.getSanPhamChiTiet().getKichThuoc().getTenKichThuoc());
                }
            }
            map.put("soLuongTruoc", b.getSoLuongTruoc());
            map.put("soLuongThayDoi", b.getSoLuongThayDoi());
            map.put("soLuongSau", b.getSoLuongSau());
            map.put("loaiBienDong", b.getLoaiBienDong());
            map.put("maThamChieu", b.getMaThamChieu());
            map.put("nguoiThucHien", b.getNguoiThucHien());
            map.put("ghiChu", b.getGhiChu());
            map.put("ngayTao", b.getNgayTao());
            return map;
        }).toList();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("content", content);
        response.put("page", result.getNumber());
        response.put("size", result.getSize());
        response.put("totalElements", result.getTotalElements());
        response.put("totalPages", result.getTotalPages());
        return response;
    }

    public static String getFallbackImage(SanPham v) {
        if (v == null) return "/images/products/shirt1.jpg";
        String code = v.getMaSanPham() != null ? v.getMaSanPham().toUpperCase(Locale.ROOT) : "";
        int id = v.getId() != null ? Math.abs(v.getId()) : 1;
        int index = (id % 20) + 1;
        if (code.startsWith("ASM") || code.startsWith("AKH")) {
            return "/images/products/shirt" + index + ".jpg";
        } else if (code.startsWith("QJN") || code.startsWith("QTY")) {
            return "/images/products/pants" + index + ".jpg";
        } else if (code.startsWith("PKT")) {
            return "/images/products/accessories" + index + ".jpg";
        } else {
            return "/images/products/dress" + index + ".jpg";
        }
    }
}
