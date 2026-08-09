package com.zestia.datn.zestia.service;

import com.zestia.datn.zestia.entity.*;
import com.zestia.datn.zestia.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PosReservationService {
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_RELEASED = "RELEASED";
    public static final String STATUS_EXPIRED = "EXPIRED";

    private static final int HOLD_MINUTES = 15;
    private static final int MAX_LINES = 50;
    private static final int MAX_QUANTITY_PER_LINE = 100;
    private static final int MAX_TOTAL_QUANTITY = 200;

    private final PosPhienGiuHangRepository sessionRepository;
    private final PosChiTietGiuHangRepository itemRepository;
    private final SanPhamChiTietRepository variantRepository;
    private final GiamGiaRepository voucherRepository;
    private final NhanVienRepository employeeRepository;
    private final PromotionPricingService pricingService;
    private final VoucherApplicationService voucherService;
    private final InventoryMovementService inventoryMovementService;

    @Transactional
    public Map<String, Object> getState(String token, Integer employeeId) {
        PosPhienGiuHang session = requireSession(token, employeeId);
        if (isExpired(session)) {
            releaseInternal(session, STATUS_EXPIRED, "Hết thời gian giữ giỏ POS");
        }
        return toState(session);
    }

    @Transactional
    public Map<String, Object> setItem(String token, Integer variantId, int targetQuantity, Integer employeeId) {
        if (variantId == null) {
            throw new IllegalArgumentException("Thiếu thông tin biến thể sản phẩm");
        }
        if (targetQuantity < 0 || targetQuantity > MAX_QUANTITY_PER_LINE) {
            throw new IllegalArgumentException("Mỗi biến thể chỉ được giữ tối đa 100 sản phẩm");
        }

        PosPhienGiuHang session = findOrCreateActiveSession(token, employeeId);
        List<PosChiTietGiuHang> currentItems = itemRepository.findByPhienIdOrderById(session.getId());
        PosChiTietGiuHang existing = currentItems.stream()
                .filter(item -> Objects.equals(item.getSanPhamChiTiet().getId(), variantId))
                .findFirst()
                .orElse(null);
        int currentQuantity = existing != null && existing.getSoLuong() != null ? existing.getSoLuong() : 0;
        int delta = targetQuantity - currentQuantity;

        int resultingLines = currentItems.size()
                + (currentQuantity == 0 && targetQuantity > 0 ? 1 : 0)
                - (currentQuantity > 0 && targetQuantity == 0 ? 1 : 0);
        int resultingTotal = currentItems.stream()
                .mapToInt(item -> item.getSoLuong() != null ? item.getSoLuong() : 0)
                .sum() + delta;
        if (resultingLines > MAX_LINES || resultingTotal > MAX_TOTAL_QUANTITY) {
            throw new IllegalArgumentException("Giỏ POS chỉ được tối đa 50 biến thể và 200 sản phẩm");
        }

        if (delta != 0) {
            SanPhamChiTiet variant = variantRepository.findByIdForUpdate(variantId)
                    .orElseThrow(() -> new IllegalArgumentException("Biến thể sản phẩm không tồn tại"));
            requireOrderable(variant);

            int before = Optional.ofNullable(variant.getSoLuong()).orElse(0);
            int after = before - delta;
            if (after < 0) {
                throw new IllegalArgumentException(
                        "Không đủ tồn kho. Phiên này chỉ có thể giữ thêm " + before + " sản phẩm"
                );
            }
            variant.setSoLuong(after);
            variantRepository.save(variant);
            inventoryMovementService.record(
                    variant,
                    before,
                    after,
                    delta > 0 ? "GIU_TON_POS" : "HOAN_GIU_TON_POS",
                    session.getMaPhien(),
                    session.getNhanVien().getHoVaTen(),
                    delta > 0 ? "Giữ tồn khi thêm vào giỏ POS" : "Hoàn tồn khi giảm giỏ POS"
            );

            LocalDateTime now = LocalDateTime.now();
            if (targetQuantity == 0 && existing != null) {
                itemRepository.delete(existing);
            } else if (existing == null) {
                itemRepository.save(PosChiTietGiuHang.builder()
                        .phien(session)
                        .sanPhamChiTiet(variant)
                        .soLuong(targetQuantity)
                        .donGia(pricingService.quote(variant).effectivePrice())
                        .ngayTao(now)
                        .capNhatLuc(now)
                        .build());
            } else {
                existing.setSoLuong(targetQuantity);
                existing.setCapNhatLuc(now);
                itemRepository.save(existing);
            }
        }

        reconcileVoucher(session);
        touch(session);
        sessionRepository.save(session);
        return toState(session);
    }

    @Transactional(noRollbackFor = ReservationExpiredException.class)
    public Map<String, Object> setVoucher(String token, String code, Integer employeeId) {
        PosPhienGiuHang session = requireActiveSession(token, employeeId);
        BigDecimal subtotal = subtotal(session);
        Integer currentId = session.getGiamGia() != null ? session.getGiamGia().getId() : null;

        if (code == null || code.isBlank()) {
            releaseCurrentVoucher(session, currentId);
            touch(session);
            return toState(sessionRepository.save(session));
        }

        GiamGia candidate = voucherRepository.findByMaGiamGiaIgnoreCase(code.trim())
                .orElseThrow(() -> new IllegalArgumentException("Mã giảm giá không tồn tại"));
        Integer candidateId = candidate.getId();

        Map<Integer, GiamGia> locked = new HashMap<>();
        StreamSupport.sortedDistinct(currentId, candidateId).forEach(id ->
                locked.put(id, voucherRepository.findByIdForUpdate(id)
                        .orElseThrow(() -> new IllegalArgumentException("Mã giảm giá không còn tồn tại")))
        );
        GiamGia lockedCandidate = locked.get(candidateId);
        boolean sameVoucher = Objects.equals(currentId, candidateId);
        VoucherApplicationService.VoucherEvaluation evaluation =
                voucherService.evaluate(lockedCandidate, subtotal, sameVoucher ? 1 : 0);
        if (!evaluation.valid()) {
            throw new IllegalArgumentException(evaluation.message());
        }

        if (!sameVoucher) {
            if (currentId != null) {
                GiamGia current = locked.get(currentId);
                if (current.getSoLuong() != null) {
                    current.setSoLuong(current.getSoLuong() + 1);
                    voucherRepository.save(current);
                }
            }
            if (lockedCandidate.getSoLuong() != null) {
                lockedCandidate.setSoLuong(lockedCandidate.getSoLuong() - 1);
                voucherRepository.save(lockedCandidate);
            }
            session.setGiamGia(lockedCandidate);
        }

        touch(session);
        sessionRepository.save(session);
        return toState(session);
    }

    @Transactional(noRollbackFor = ReservationExpiredException.class)
    public Map<String, Object> reserveBestVoucher(String token, Integer employeeId) {
        PosPhienGiuHang session = requireActiveSession(token, employeeId);
        VoucherApplicationService.VoucherEvaluation best = voucherService.findBest(
                subtotal(session),
                session.getGiamGia() != null ? session.getGiamGia().getId() : null
        );
        if (!best.valid()) {
            releaseCurrentVoucher(session, session.getGiamGia() != null ? session.getGiamGia().getId() : null);
            touch(session);
            sessionRepository.save(session);
            return toState(session);
        }
        return setVoucher(session.getMaPhien(), best.voucher().getMaGiamGia(), employeeId);
    }

    @Transactional
    public Map<String, Object> release(String token, Integer employeeId) {
        PosPhienGiuHang session = requireSession(token, employeeId);
        if (STATUS_ACTIVE.equals(session.getTrangThai())) {
            releaseInternal(session, STATUS_RELEASED, "Nhân viên xóa giỏ POS");
        }
        return toState(session);
    }

    @Transactional(noRollbackFor = ReservationExpiredException.class)
    public CheckoutReservation prepareCheckout(
            String token,
            Integer employeeId,
            Map<Integer, Integer> requestedQuantities,
            String requestedVoucherCode
    ) {
        PosPhienGiuHang session = requireActiveSession(token, employeeId);
        Map<Integer, Integer> heldQuantities = new LinkedHashMap<>();
        Map<Integer, BigDecimal> heldPrices = new LinkedHashMap<>();
        for (PosChiTietGiuHang item : itemRepository.findByPhienIdOrderById(session.getId())) {
            Integer variantId = item.getSanPhamChiTiet().getId();
            heldQuantities.put(variantId, item.getSoLuong());
            heldPrices.put(variantId, item.getDonGia());
        }
        if (heldQuantities.isEmpty() || !heldQuantities.equals(requestedQuantities)) {
            throw new IllegalStateException("Giỏ POS đã thay đổi. Vui lòng tải lại giỏ trước khi thanh toán");
        }

        String heldVoucherCode = session.getGiamGia() != null ? session.getGiamGia().getMaGiamGia() : null;
        if (!sameCode(heldVoucherCode, requestedVoucherCode)) {
            throw new IllegalStateException("Voucher trên hóa đơn không khớp với voucher đã giữ");
        }

        GiamGia voucher = session.getGiamGia();
        if (voucher != null) {
            VoucherApplicationService.VoucherEvaluation evaluation =
                    voucherService.evaluate(voucher, subtotal(session), 1);
            if (!evaluation.valid()) {
                throw new IllegalStateException(evaluation.message());
            }
        }
        touch(session);
        sessionRepository.save(session);
        return new CheckoutReservation(
                session.getMaPhien(),
                Map.copyOf(heldQuantities),
                Map.copyOf(heldPrices),
                voucher
        );
    }

    @Transactional(noRollbackFor = ReservationExpiredException.class)
    public void completeCheckout(String token, Integer employeeId, HoaDon order) {
        PosPhienGiuHang session = requireActiveSession(token, employeeId);
        session.setTrangThai(STATUS_COMPLETED);
        session.setHoaDon(order);
        session.setCapNhatLuc(LocalDateTime.now());
        session.setHetHanLuc(LocalDateTime.now());
        sessionRepository.save(session);
    }

    public List<Integer> findExpiredIds(int limit) {
        return sessionRepository.findExpiredIds(
                LocalDateTime.now(),
                PageRequest.of(0, Math.max(1, Math.min(limit, 500)))
        );
    }

    @Transactional
    public void expireById(Integer sessionId) {
        PosPhienGiuHang session = sessionRepository.findByIdForUpdate(sessionId).orElse(null);
        if (session != null && STATUS_ACTIVE.equals(session.getTrangThai()) && isExpired(session)) {
            releaseInternal(session, STATUS_EXPIRED, "Tự động hoàn giỏ POS hết hạn");
        }
    }

    private PosPhienGiuHang findOrCreateActiveSession(String token, Integer employeeId) {
        if (token == null || token.isBlank()) {
            return createSession(employeeId);
        }
        PosPhienGiuHang existing = requireSession(token, employeeId);
        if (STATUS_ACTIVE.equals(existing.getTrangThai()) && !isExpired(existing)) {
            return existing;
        }
        if (STATUS_ACTIVE.equals(existing.getTrangThai())) {
            releaseInternal(existing, STATUS_EXPIRED, "Tự động hoàn giỏ POS hết hạn");
        }
        return createSession(employeeId);
    }

    private PosPhienGiuHang createSession(Integer employeeId) {
        NhanVien employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Nhân viên không tồn tại"));
        if (employee.getTinhTrangLamViec() != null && employee.getTinhTrangLamViec() != 1) {
            throw new IllegalStateException("Tài khoản nhân viên đang bị tạm khóa");
        }
        LocalDateTime now = LocalDateTime.now();
        return sessionRepository.save(PosPhienGiuHang.builder()
                .maPhien(UUID.randomUUID().toString())
                .nhanVien(employee)
                .trangThai(STATUS_ACTIVE)
                .ngayTao(now)
                .capNhatLuc(now)
                .hetHanLuc(now.plusMinutes(HOLD_MINUTES))
                .build());
    }

    private PosPhienGiuHang requireSession(String token, Integer employeeId) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Thiếu mã phiên giỏ POS");
        }
        PosPhienGiuHang session = sessionRepository.findByMaPhienForUpdate(token.trim())
                .orElseThrow(() -> new IllegalArgumentException("Phiên giỏ POS không tồn tại"));
        if (session.getNhanVien() == null || !Objects.equals(session.getNhanVien().getId(), employeeId)) {
            throw new SecurityException("Bạn không có quyền truy cập giỏ POS này");
        }
        return session;
    }

    private PosPhienGiuHang requireActiveSession(String token, Integer employeeId) {
        PosPhienGiuHang session = requireSession(token, employeeId);
        if (!STATUS_ACTIVE.equals(session.getTrangThai())) {
            throw new IllegalStateException("Phiên giỏ POS đã kết thúc");
        }
        if (isExpired(session)) {
            releaseInternal(session, STATUS_EXPIRED, "Tự động hoàn giỏ POS hết hạn");
            throw new ReservationExpiredException("Phiên giữ hàng đã hết hạn. Vui lòng tạo lại giỏ POS");
        }
        return session;
    }

    private void requireOrderable(SanPhamChiTiet variant) {
        if (variant.getSanPham() == null
                || variant.getMauSac() == null
                || variant.getKichThuoc() == null
                || (variant.getTrangThai() != null && variant.getTrangThai() != 1)
                || (variant.getSanPham().getTrangThai() != null && variant.getSanPham().getTrangThai() != 1)) {
            throw new IllegalArgumentException("Biến thể sản phẩm đã ngừng bán hoặc không hợp lệ");
        }
    }

    private void reconcileVoucher(PosPhienGiuHang session) {
        if (session.getGiamGia() == null) return;
        GiamGia lockedVoucher = voucherRepository.findByIdForUpdate(session.getGiamGia().getId())
                .orElse(null);
        if (lockedVoucher == null
                || !voucherService.evaluate(lockedVoucher, subtotal(session), 1).valid()) {
            if (lockedVoucher != null) {
                if (lockedVoucher.getSoLuong() != null) {
                    lockedVoucher.setSoLuong(lockedVoucher.getSoLuong() + 1);
                    voucherRepository.save(lockedVoucher);
                }
            }
            session.setGiamGia(null);
        } else {
            session.setGiamGia(lockedVoucher);
        }
    }

    private void releaseCurrentVoucher(PosPhienGiuHang session, Integer voucherId) {
        if (voucherId == null) {
            session.setGiamGia(null);
            return;
        }
        GiamGia voucher = voucherRepository.findByIdForUpdate(voucherId).orElse(null);
        if (voucher != null && voucher.getSoLuong() != null) {
            voucher.setSoLuong(voucher.getSoLuong() + 1);
            voucherRepository.save(voucher);
        }
        session.setGiamGia(null);
    }

    private void releaseInternal(PosPhienGiuHang session, String targetStatus, String note) {
        List<PosChiTietGiuHang> items = itemRepository.findByPhienIdOrderById(session.getId());
        items.stream()
                .sorted(Comparator.comparing(item -> item.getSanPhamChiTiet().getId()))
                .forEach(item -> {
                    SanPhamChiTiet variant = variantRepository.findByIdForUpdate(item.getSanPhamChiTiet().getId())
                            .orElse(null);
                    if (variant == null) return;
                    int before = Optional.ofNullable(variant.getSoLuong()).orElse(0);
                    int quantity = Optional.ofNullable(item.getSoLuong()).orElse(0);
                    int after = before + quantity;
                    variant.setSoLuong(after);
                    variantRepository.save(variant);
                    inventoryMovementService.record(
                            variant,
                            before,
                            after,
                            "HOAN_GIU_TON_POS",
                            session.getMaPhien(),
                            session.getNhanVien().getHoVaTen(),
                            note
                    );
                });
        releaseCurrentVoucher(
                session,
                session.getGiamGia() != null ? session.getGiamGia().getId() : null
        );
        session.setTrangThai(targetStatus);
        session.setCapNhatLuc(LocalDateTime.now());
        session.setHetHanLuc(LocalDateTime.now());
        sessionRepository.save(session);
    }

    private void touch(PosPhienGiuHang session) {
        LocalDateTime now = LocalDateTime.now();
        session.setCapNhatLuc(now);
        session.setHetHanLuc(now.plusMinutes(HOLD_MINUTES));
    }

    private boolean isExpired(PosPhienGiuHang session) {
        return session.getHetHanLuc() != null && !session.getHetHanLuc().isAfter(LocalDateTime.now());
    }

    private BigDecimal subtotal(PosPhienGiuHang session) {
        return itemRepository.findByPhienIdOrderById(session.getId()).stream()
                .map(item -> item.getDonGia().multiply(BigDecimal.valueOf(item.getSoLuong())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Map<String, Object> toState(PosPhienGiuHang session) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("token", session.getMaPhien());
        response.put("status", session.getTrangThai());
        response.put("expiresAt", session.getHetHanLuc());

        if (!STATUS_ACTIVE.equals(session.getTrangThai())) {
            response.put("items", List.of());
            response.put("subtotal", BigDecimal.ZERO);
            response.put("discount", BigDecimal.ZERO);
            response.put("voucher", null);
            return response;
        }

        List<Map<String, Object>> items = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        for (PosChiTietGiuHang heldItem : itemRepository.findByPhienIdOrderById(session.getId())) {
            SanPhamChiTiet variant = heldItem.getSanPhamChiTiet();
            SanPham product = variant.getSanPham();
            int quantity = Optional.ofNullable(heldItem.getSoLuong()).orElse(0);
            BigDecimal lineTotal = heldItem.getDonGia().multiply(BigDecimal.valueOf(quantity));
            subtotal = subtotal.add(lineTotal);

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("variantId", variant.getId());
            item.put("productId", product.getId());
            item.put("productCode", product.getMaSanPham());
            item.put("maVay", product.getMaSanPham());
            item.put("productName", product.getTenSanPham());
            item.put("tenVay", product.getTenSanPham());
            item.put("color", variant.getMauSac().getTenMauSac());
            item.put("size", variant.getKichThuoc().getTenKichThuoc());
            item.put("quantity", quantity);
            item.put("unitPrice", heldItem.getDonGia());
            item.put("lineTotal", lineTotal);
            item.put("image", variant.getAnhUrl());
            item.put("maxQuantity", Optional.ofNullable(variant.getSoLuong()).orElse(0) + quantity);
            items.add(item);
        }

        Map<String, Object> voucherMap = null;
        BigDecimal discount = BigDecimal.ZERO;
        if (session.getGiamGia() != null) {
            VoucherApplicationService.VoucherEvaluation evaluation =
                    voucherService.evaluate(session.getGiamGia(), subtotal, 1);
            if (evaluation.valid()) {
                discount = evaluation.discount();
                voucherMap = new LinkedHashMap<>();
                voucherMap.put("code", session.getGiamGia().getMaGiamGia());
                voucherMap.put("name", session.getGiamGia().getTenGiamGia());
                voucherMap.put("discount", discount);
            }
        }
        response.put("items", items);
        response.put("subtotal", subtotal);
        response.put("discount", discount);
        response.put("voucher", voucherMap);
        return response;
    }

    private boolean sameCode(String first, String second) {
        String left = first == null || first.isBlank() ? null : first.trim();
        String right = second == null || second.isBlank() ? null : second.trim();
        return left == null ? right == null : right != null && left.equalsIgnoreCase(right);
    }

    public record CheckoutReservation(
            String token,
            Map<Integer, Integer> quantities,
            Map<Integer, BigDecimal> prices,
            GiamGia voucher
    ) {
    }

    public static final class ReservationExpiredException extends IllegalStateException {
        public ReservationExpiredException(String message) {
            super(message);
        }
    }

    private static final class StreamSupport {
        private StreamSupport() {
        }

        static List<Integer> sortedDistinct(Integer... values) {
            return Arrays.stream(values)
                    .filter(Objects::nonNull)
                    .distinct()
                    .sorted()
                    .toList();
        }
    }
}
