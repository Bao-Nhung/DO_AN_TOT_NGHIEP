package com.zestia.datn.zestia.service;

import com.zestia.datn.zestia.entity.*;
import com.zestia.datn.zestia.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReturnExchangeService {
    public static final String EXCHANGE = "DOI";
    public static final String RETURN = "TRA";
    public static final String ONLINE = "ONLINE";
    public static final String OFFLINE = "OFFLINE";
    public static final String PENDING = "CHO_DUYET";
    public static final String WAITING_FOR_GOODS = "CHO_NHAN_HANG";
    public static final String READY_TO_COMPLETE = "CHO_HOAN_TAT";
    public static final String REJECTED = "TU_CHOI";
    public static final String SEND_BACK = "TRA_LAI_KHACH";
    public static final String EXCHANGED = "DA_DOI";
    public static final String REFUNDED = "DA_HOAN_TIEN";

    private static final int MAX_IMAGES = 5;
    private static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024;

    private final YeuCauDoiTraRepository requestRepo;
    private final AnhDoiTraRepository requestImageRepo;
    private final HoaDonRepository orderRepo;
    private final HoaDonChiTietRepository orderDetailRepo;
    private final VayChiTietRepository variantRepo;
    private final NhanVienRepository employeeRepo;
    private final HoaDonAuditLogRepository auditRepo;
    private final CurrentCustomerService currentCustomerService;

    @Value("${app.upload.return-dir:}")
    private String configuredUploadDir;

    @Transactional(readOnly = true)
    public List<Map<String, Object>> mine(Authentication authentication) {
        KhachHang customer = currentCustomerService.require(authentication);
        return mapAll(requestRepo.findByKhachHangIdOrderByNgayTaoDesc(customer.getId()));
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> all(String type, String status) {
        String normalizedType = optionalType(type);
        String normalizedStatus = clean(status);
        return mapAll(requestRepo.findAllByOrderByNgayTaoDesc().stream()
                .filter(item -> normalizedType == null || normalizedType.equals(item.getLoaiYeuCau()))
                .filter(item -> normalizedStatus == null || normalizedStatus.equalsIgnoreCase(item.getTrangThai()))
                .toList());
    }

    @Transactional
    public Map<String, Object> createOnline(Integer orderId, Integer detailId, String type, Integer quantity,
                                             String reason, String condition, String refundInfo,
                                             Integer replacementVariantId, List<MultipartFile> images,
                                             Authentication authentication) {
        KhachHang customer = currentCustomerService.require(authentication);
        HoaDon order = orderRepo.findByIdForUpdate(orderId)
                .orElseThrow(() -> notFound("Không tìm thấy đơn hàng"));
        requireOwner(order, customer);
        if (Objects.equals(order.getHinhThucNhanHang(), (byte) 0)) {
            throw badRequest("Đơn tại quầy được xử lý trực tiếp bởi nhân viên");
        }
        if (!Objects.equals(order.getTrangThai(), (byte) 4)) {
            throw badRequest("Chỉ đơn đã giao thành công mới được yêu cầu đổi hoặc trả");
        }
        if (order.getNgayGiaoHangThucTe() != null
                && order.getNgayGiaoHangThucTe().isBefore(LocalDateTime.now().minusDays(30))) {
            throw badRequest("Đơn hàng đã quá thời hạn đổi trả 30 ngày");
        }

        HoaDonChiTiet detail = requireOrderDetail(order, detailId);
        String normalizedType = requireType(type);
        int safeQuantity = requireQuantity(quantity, detail.getSoLuong());
        requireNewRequest(detail);
        String safeReason = requireText(reason, 5, 1000, "Vui lòng nhập lý do từ 5 đến 1000 ký tự");
        String safeCondition = requireText(condition, 10, 2000, "Vui lòng mô tả tình trạng hàng từ 10 đến 2000 ký tự");
        if (images == null || images.stream().filter(file -> file != null && !file.isEmpty()).count() == 0) {
            throw badRequest("Vui lòng tải ít nhất một ảnh tình trạng sản phẩm");
        }
        VayChiTiet replacement = EXCHANGE.equals(normalizedType)
                ? requireReplacement(detail, replacementVariantId, safeQuantity)
                : null;
        String safeRefundInfo = RETURN.equals(normalizedType)
                ? requireText(refundInfo, 10, 500, "Vui lòng nhập thông tin nhận tiền hoàn từ 10 đến 500 ký tự")
                : null;

        YeuCauDoiTra saved = requestRepo.save(YeuCauDoiTra.builder()
                .hoaDon(order)
                .hoaDonChiTiet(detail)
                .bienTheDoi(replacement)
                .khachHang(customer)
                .loaiYeuCau(normalizedType)
                .nguon(ONLINE)
                .trangThai(PENDING)
                .soLuong(safeQuantity)
                .lyDo(safeReason)
                .tinhTrangHang(safeCondition)
                .thongTinHoanTien(safeRefundInfo)
                .daHoanTonKho(false)
                .ngayTao(LocalDateTime.now())
                .build());
        saveImages(saved, images);
        audit(saved, "TAO_YEU_CAU_" + normalizedType, customer.getHoVaTen(), "KhachHang", safeReason);
        return toMap(saved, imageUrls(saved.getId()));
    }

    @Transactional
    public Map<String, Object> createOffline(Integer orderId, Integer detailId, String type, Integer quantity,
                                              String reason, String refundInfo, Integer replacementVariantId,
                                              Authentication authentication) {
        NhanVien employee = requireEmployee(authentication);
        HoaDon order = orderRepo.findByIdForUpdate(orderId)
                .orElseThrow(() -> notFound("Không tìm thấy đơn hàng"));
        if (!Objects.equals(order.getHinhThucNhanHang(), (byte) 0)) {
            throw badRequest("Chức năng tại quầy chỉ áp dụng cho hóa đơn offline");
        }
        if (!Boolean.TRUE.equals(order.getDaThanhToan())) {
            throw badRequest("Hóa đơn tại quầy chưa được thanh toán");
        }
        if (order.getKhachHang() == null) {
            throw badRequest("Hóa đơn chưa liên kết với hồ sơ khách hàng");
        }

        HoaDonChiTiet detail = requireOrderDetail(order, detailId);
        requireNewRequest(detail);
        String normalizedType = requireType(type);
        int safeQuantity = requireQuantity(quantity, detail.getSoLuong());
        String safeReason = requireText(reason, 5, 1000, "Vui lòng nhập lý do từ 5 đến 1000 ký tự");
        VayChiTiet replacement = EXCHANGE.equals(normalizedType)
                ? requireReplacement(detail, replacementVariantId, safeQuantity)
                : null;
        String safeRefundInfo = RETURN.equals(normalizedType)
                ? requireText(refundInfo, 3, 500, "Vui lòng ghi phương thức hoặc thông tin hoàn tiền")
                : null;

        YeuCauDoiTra request = requestRepo.save(YeuCauDoiTra.builder()
                .hoaDon(order)
                .hoaDonChiTiet(detail)
                .bienTheDoi(replacement)
                .khachHang(order.getKhachHang())
                .nhanVienXuLy(employee)
                .loaiYeuCau(normalizedType)
                .nguon(OFFLINE)
                .trangThai(READY_TO_COMPLETE)
                .soLuong(safeQuantity)
                .lyDo(safeReason)
                .thongTinHoanTien(safeRefundInfo)
                .daHoanTonKho(false)
                .ngayTao(LocalDateTime.now())
                .ngayDuyet(LocalDateTime.now())
                .ngayNhanHang(LocalDateTime.now())
                .build());
        finishInventory(request);
        request.setNgayHoanTat(LocalDateTime.now());
        requestRepo.save(request);
        audit(request, "DOI_TRA_TAI_QUAY", employee.getHoVaTen(), "NhanVien", safeReason);
        return toMap(request, List.of());
    }

    @Transactional
    public Map<String, Object> review(Integer id, boolean approved, String reason, Authentication authentication) {
        NhanVien employee = requireEmployee(authentication);
        YeuCauDoiTra request = locked(id);
        requireStatus(request, PENDING);
        request.setNhanVienXuLy(employee);
        request.setNgayDuyet(LocalDateTime.now());
        if (approved) {
            request.setTrangThai(WAITING_FOR_GOODS);
            request.setGhiChuNhanVien(clean(reason));
        } else {
            request.setTrangThai(REJECTED);
            request.setLyDoTuChoi(requireText(reason, 5, 1000, "Nhân viên phải nhập lý do từ chối"));
        }
        requestRepo.save(request);
        audit(request, approved ? "DUYET_DOI_TRA" : "TU_CHOI_DOI_TRA", employee.getHoVaTen(), "NhanVien", reason);
        return toMap(request, imageUrls(request.getId()));
    }

    @Transactional
    public Map<String, Object> receive(Integer id, boolean accepted, String reason, Authentication authentication) {
        NhanVien employee = requireEmployee(authentication);
        YeuCauDoiTra request = locked(id);
        requireStatus(request, WAITING_FOR_GOODS);
        request.setNhanVienXuLy(employee);
        request.setNgayNhanHang(LocalDateTime.now());
        if (accepted) {
            request.setTrangThai(READY_TO_COMPLETE);
            request.setGhiChuNhanVien(clean(reason));
        } else {
            request.setTrangThai(SEND_BACK);
            request.setLyDoTuChoi(requireText(reason, 5, 1000, "Vui lòng nhập lý do trả lại hàng cho khách"));
            request.setNgayHoanTat(LocalDateTime.now());
        }
        requestRepo.save(request);
        audit(request, accepted ? "NHAN_HANG_DOI_TRA" : "TRA_LAI_HANG_CHO_KHACH",
                employee.getHoVaTen(), "NhanVien", reason);
        return toMap(request, imageUrls(request.getId()));
    }

    @Transactional
    public Map<String, Object> complete(Integer id, String note, Authentication authentication) {
        NhanVien employee = requireEmployee(authentication);
        YeuCauDoiTra request = locked(id);
        requireStatus(request, READY_TO_COMPLETE);
        request.setNhanVienXuLy(employee);
        request.setGhiChuNhanVien(clean(note));
        finishInventory(request);
        request.setNgayHoanTat(LocalDateTime.now());
        requestRepo.save(request);
        audit(request, EXCHANGE.equals(request.getLoaiYeuCau()) ? "HOAN_TAT_DOI_HANG" : "HOAN_TAT_HOAN_TIEN",
                employee.getHoVaTen(), "NhanVien", note);
        return toMap(request, imageUrls(request.getId()));
    }

    private void finishInventory(YeuCauDoiTra request) {
        if (Boolean.TRUE.equals(request.getDaHoanTonKho())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Yêu cầu này đã cập nhật tồn kho trước đó");
        }
        Integer oldId = request.getHoaDonChiTiet().getVayChiTiet().getId();
        Integer newId = request.getBienTheDoi() != null ? request.getBienTheDoi().getId() : null;
        List<Integer> lockIds = new ArrayList<>(new LinkedHashSet<>(newId == null ? List.of(oldId) : List.of(oldId, newId)));
        Collections.sort(lockIds);
        Map<Integer, VayChiTiet> locked = new HashMap<>();
        for (Integer id : lockIds) {
            locked.put(id, variantRepo.findByIdForUpdate(id)
                    .orElseThrow(() -> notFound("Không tìm thấy biến thể sản phẩm")));
        }

        int quantity = request.getSoLuong();
        VayChiTiet oldVariant = locked.get(oldId);
        oldVariant.setSoLuong(value(oldVariant.getSoLuong()) + quantity);
        variantRepo.save(oldVariant);

        if (EXCHANGE.equals(request.getLoaiYeuCau())) {
            VayChiTiet newVariant = locked.get(newId);
            if (value(newVariant.getSoLuong()) < quantity) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Biến thể đổi không còn đủ tồn kho");
            }
            newVariant.setSoLuong(value(newVariant.getSoLuong()) - quantity);
            variantRepo.save(newVariant);
            request.setTrangThai(EXCHANGED);
        } else {
            request.setTrangThai(REFUNDED);
        }
        request.setDaHoanTonKho(true);
    }

    private VayChiTiet requireReplacement(HoaDonChiTiet detail, Integer replacementId, int quantity) {
        if (replacementId == null) throw badRequest("Vui lòng chọn màu và kích cỡ muốn đổi");
        VayChiTiet current = detail.getVayChiTiet();
        VayChiTiet replacement = variantRepo.findById(replacementId)
                .orElseThrow(() -> notFound("Không tìm thấy biến thể muốn đổi"));
        if (Objects.equals(current.getId(), replacement.getId())) {
            throw badRequest("Biến thể đổi phải khác sản phẩm hiện tại");
        }
        if (current.getVay() == null || replacement.getVay() == null
                || !Objects.equals(current.getVay().getId(), replacement.getVay().getId())) {
            throw badRequest("Chỉ được đổi màu hoặc kích cỡ của cùng sản phẩm");
        }
        if (!Objects.equals(replacement.getTrangThai(), (byte) 1) || value(replacement.getSoLuong()) < quantity) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Biến thể muốn đổi không còn đủ tồn kho");
        }
        return replacement;
    }

    private HoaDonChiTiet requireOrderDetail(HoaDon order, Integer detailId) {
        if (detailId == null) throw badRequest("Vui lòng chọn sản phẩm cần đổi hoặc trả");
        HoaDonChiTiet detail = orderDetailRepo.findById(detailId)
                .orElseThrow(() -> notFound("Không tìm thấy sản phẩm trong đơn"));
        if (detail.getHoaDon() == null || !Objects.equals(detail.getHoaDon().getId(), order.getId())) {
            throw badRequest("Sản phẩm không thuộc đơn hàng này");
        }
        if (detail.getVayChiTiet() == null) throw badRequest("Sản phẩm không còn biến thể hợp lệ");
        return detail;
    }

    private void requireNewRequest(HoaDonChiTiet detail) {
        if (requestRepo.existsByHoaDonChiTietId(detail.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Sản phẩm này đã có hồ sơ đổi hoặc trả hàng");
        }
    }

    private void requireOwner(HoaDon order, KhachHang customer) {
        if (order.getKhachHang() == null || !Objects.equals(order.getKhachHang().getId(), customer.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền đổi trả đơn hàng này");
        }
    }

    private NhanVien requireEmployee(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Vui lòng đăng nhập");
        }
        String identity = authentication.getName();
        return employeeRepo.findByTenNguoiDung(identity)
                .or(() -> employeeRepo.findByEmail(identity))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Không tìm thấy hồ sơ nhân viên"));
    }

    private YeuCauDoiTra locked(Integer id) {
        return requestRepo.findByIdForUpdate(id).orElseThrow(() -> notFound("Không tìm thấy yêu cầu đổi trả"));
    }

    private void requireStatus(YeuCauDoiTra request, String expected) {
        if (!expected.equals(request.getTrangThai())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Yêu cầu không còn ở bước xử lý này");
        }
    }

    private int requireQuantity(Integer quantity, Integer purchased) {
        int result = quantity != null ? quantity : 0;
        if (result < 1 || result > value(purchased)) {
            throw badRequest("Số lượng đổi trả không hợp lệ");
        }
        return result;
    }

    private String requireType(String type) {
        String normalized = optionalType(type);
        if (normalized == null) throw badRequest("Loại yêu cầu phải là đổi hoặc trả hàng");
        return normalized;
    }

    private String optionalType(String type) {
        String normalized = clean(type);
        if (normalized == null) return null;
        normalized = normalized.toUpperCase(Locale.ROOT);
        if (EXCHANGE.equals(normalized) || RETURN.equals(normalized)) return normalized;
        return null;
    }

    private String requireText(String value, int min, int max, String message) {
        String result = clean(value);
        if (result == null || result.length() < min || result.length() > max) throw badRequest(message);
        return result;
    }

    private String clean(String value) {
        if (value == null) return null;
        String result = value.trim();
        return result.isEmpty() ? null : result;
    }

    private int value(Integer number) {
        return number != null ? number : 0;
    }

    private List<Map<String, Object>> mapAll(List<YeuCauDoiTra> requests) {
        if (requests.isEmpty()) return List.of();
        Map<Integer, List<String>> images = new HashMap<>();
        for (AnhDoiTra image : requestImageRepo.findByYeuCauIdInOrderByIdAsc(requests.stream().map(YeuCauDoiTra::getId).toList())) {
            images.computeIfAbsent(image.getYeuCau().getId(), key -> new ArrayList<>()).add(image.getAnhUrl());
        }
        return requests.stream().map(item -> toMap(item, images.getOrDefault(item.getId(), List.of()))).toList();
    }

    private Map<String, Object> toMap(YeuCauDoiTra request, List<String> images) {
        HoaDonChiTiet detail = request.getHoaDonChiTiet();
        VayChiTiet variant = detail.getVayChiTiet();
        VayChiTiet replacement = request.getBienTheDoi();
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", request.getId());
        map.put("orderId", request.getHoaDon().getId());
        map.put("orderCode", request.getHoaDon().getMaHoaDon());
        map.put("orderDetailId", detail.getId());
        map.put("type", request.getLoaiYeuCau());
        map.put("source", request.getNguon());
        map.put("status", request.getTrangThai());
        map.put("quantity", request.getSoLuong());
        map.put("reason", request.getLyDo());
        map.put("condition", request.getTinhTrangHang());
        map.put("refundInfo", request.getThongTinHoanTien());
        map.put("rejectionReason", request.getLyDoTuChoi());
        map.put("staffNote", request.getGhiChuNhanVien());
        map.put("images", images);
        map.put("customerId", request.getKhachHang().getId());
        map.put("customerName", request.getKhachHang().getHoVaTen());
        map.put("customerPhone", request.getKhachHang().getSoDienThoai());
        map.put("employeeName", request.getNhanVienXuLy() != null ? request.getNhanVienXuLy().getHoVaTen() : null);
        map.put("productId", variant.getVay().getId());
        map.put("productCode", variant.getVay().getMaVay());
        map.put("productName", variant.getVay().getTenVay());
        map.put("variantId", variant.getId());
        map.put("color", variant.getMauSac() != null ? variant.getMauSac().getTenMauSac() : null);
        map.put("size", variant.getKichThuoc() != null ? variant.getKichThuoc().getTenKichThuoc() : null);
        map.put("productImage", variant.getAnhUrl());
        map.put("replacement", replacement != null ? variantMap(replacement) : null);
        map.put("createdAt", request.getNgayTao());
        map.put("reviewedAt", request.getNgayDuyet());
        map.put("receivedAt", request.getNgayNhanHang());
        map.put("completedAt", request.getNgayHoanTat());
        return map;
    }

    private Map<String, Object> variantMap(VayChiTiet variant) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", variant.getId());
        map.put("color", variant.getMauSac() != null ? variant.getMauSac().getTenMauSac() : null);
        map.put("size", variant.getKichThuoc() != null ? variant.getKichThuoc().getTenKichThuoc() : null);
        map.put("stock", value(variant.getSoLuong()));
        map.put("image", variant.getAnhUrl());
        return map;
    }

    private List<String> imageUrls(Integer requestId) {
        return requestImageRepo.findByYeuCauIdOrderByIdAsc(requestId).stream().map(AnhDoiTra::getAnhUrl).toList();
    }

    private void saveImages(YeuCauDoiTra request, List<MultipartFile> images) {
        List<MultipartFile> files = images == null ? List.of() : images.stream()
                .filter(file -> file != null && !file.isEmpty()).toList();
        if (files.size() > MAX_IMAGES) throw badRequest("Mỗi yêu cầu được tải tối đa 5 ảnh");
        Path directory = resolveUploadDir();
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Không thể tạo thư mục ảnh đổi trả");
        }

        for (MultipartFile image : files) {
            if (image.getSize() > MAX_IMAGE_BYTES) throw badRequest("Mỗi ảnh tối đa 5MB");
            String contentType = Optional.ofNullable(image.getContentType()).orElse("").toLowerCase(Locale.ROOT);
            String extension = "image/png".equals(contentType) ? ".png"
                    : "image/jpeg".equals(contentType) ? ".jpg" : null;
            if (extension == null) throw badRequest("Ảnh chỉ hỗ trợ JPG hoặc PNG");
            try {
                if (ImageIO.read(image.getInputStream()) == null) {
                    throw badRequest("Tệp tải lên không phải ảnh hợp lệ");
                }
                String filename = "return_" + request.getId() + "_" + UUID.randomUUID() + extension;
                Files.copy(image.getInputStream(), directory.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
                requestImageRepo.save(AnhDoiTra.builder()
                        .yeuCau(request)
                        .anhUrl("/images/returns/" + filename)
                        .ngayTao(LocalDateTime.now())
                        .build());
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Không thể lưu ảnh đổi trả");
            }
        }
    }

    private Path resolveUploadDir() {
        if (configuredUploadDir != null && !configuredUploadDir.isBlank()) {
            return Paths.get(configuredUploadDir).toAbsolutePath().normalize();
        }
        Path[] candidates = {
                Paths.get("..", "frontend", "public", "images", "returns"),
                Paths.get("frontend", "public", "images", "returns")
        };
        for (Path candidate : candidates) {
            Path absolute = candidate.toAbsolutePath().normalize();
            Path frontend = absolute.getParent().getParent().getParent();
            if (Files.exists(frontend)) return absolute;
        }
        return candidates[0].toAbsolutePath().normalize();
    }

    private void audit(YeuCauDoiTra request, String action, String actor, String role, String note) {
        auditRepo.save(HoaDonAuditLog.builder()
                .hoaDon(request.getHoaDon())
                .hanhDong(action)
                .trangThaiCu(request.getHoaDon().getTrangThai())
                .trangThaiMoi(request.getHoaDon().getTrangThai())
                .nguoiThucHien(actor)
                .vaiTro(role)
                .ghiChu("Yêu cầu #" + request.getId() + (clean(note) != null ? ": " + clean(note) : ""))
                .ngayTao(LocalDateTime.now())
                .build());
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }
}
