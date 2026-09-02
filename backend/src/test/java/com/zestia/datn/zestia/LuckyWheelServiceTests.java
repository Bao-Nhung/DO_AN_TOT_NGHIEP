package com.zestia.datn.zestia;

import com.zestia.datn.zestia.entity.HoaDon;
import com.zestia.datn.zestia.entity.LuotQuayMayMan;
import com.zestia.datn.zestia.entity.PhanThuongVongQuay;
import com.zestia.datn.zestia.entity.VongQuayMayMan;
import com.zestia.datn.zestia.repository.HoaDonRepository;
import com.zestia.datn.zestia.repository.LuotQuayMayManRepository;
import com.zestia.datn.zestia.repository.PhanThuongVongQuayRepository;
import com.zestia.datn.zestia.repository.VongQuayMayManRepository;
import com.zestia.datn.zestia.service.LuckyWheelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LuckyWheelServiceTests {
    @Mock private VongQuayMayManRepository campaignRepository;
    @Mock private PhanThuongVongQuayRepository prizeRepository;
    @Mock private LuotQuayMayManRepository spinRepository;
    @Mock private HoaDonRepository orderRepository;

    private LuckyWheelService service;
    private VongQuayMayMan campaign;
    private HoaDon order;

    @BeforeEach
    void setUp() {
        service = new LuckyWheelService(campaignRepository, prizeRepository, spinRepository, orderRepository);
        campaign = VongQuayMayMan.builder()
                .id(1)
                .maChienDich("VQ-TEST")
                .tenChienDich("Vòng quay test")
                .giaTriDonToiThieu(new BigDecimal("1000000"))
                .ngayBatDau(LocalDateTime.now().minusDays(1))
                .ngayKetThuc(LocalDateTime.now().plusDays(1))
                .trangThai((byte) 1)
                .ngayTao(LocalDateTime.now())
                .build();
        order = HoaDon.builder()
                .id(10)
                .maHoaDon("HD-TEST")
                .tongTien(new BigDecimal("1250000"))
                .trangThai((byte) 4)
                .hinhThucThanhToan("COD")
                .daThanhToan(true)
                .tenKhachHang("Khách test")
                .soDienThoai("0912345678")
                .emailKhachHang("customer@example.com")
                .build();
        lenient().when(campaignRepository.findActiveAt(any())).thenReturn(List.of(campaign));
    }

    @Test
    void completedOrderAboveThresholdIsEligible() {
        when(orderRepository.findOrderByCodeAndPhone("HD-TEST", "0912345678")).thenReturn(Optional.of(order));
        when(spinRepository.findByChienDichIdAndMaHoaDonIgnoreCase(1, "HD-TEST")).thenReturn(Optional.empty());

        Map<String, Object> result = service.checkEligibility("hd-test", "0912 345 678");

        assertEquals(Boolean.TRUE, result.get("eligible"));
        assertEquals("HD-TEST", result.get("maHoaDon"));
    }

    @Test
    void orderBelowThresholdIsRejectedWithoutCreatingSpin() {
        order.setTongTien(new BigDecimal("999999"));
        when(orderRepository.findOrderByCodeAndPhone("HD-TEST", "0912345678")).thenReturn(Optional.of(order));

        Map<String, Object> result = service.checkEligibility("HD-TEST", "0912345678");

        assertEquals(Boolean.FALSE, result.get("eligible"));
        assertTrue(String.valueOf(result.get("message")).contains("tối thiểu"));
        verifyNoInteractions(prizeRepository);
    }

    @Test
    void successfulSpinDecrementsOnlyPhysicalPrizeInventory() {
        PhanThuongVongQuay prize = PhanThuongVongQuay.builder()
                .id(7)
                .chienDich(campaign)
                .tenPhanThuong("Balo Zestia")
                .loaiPhanThuong(PhanThuongVongQuay.PHYSICAL)
                .soLuongBanDau(2)
                .soLuongCon(2)
                .trongSo(100)
                .mauHienThi("#D4564E")
                .bieuTuong("bi-backpack")
                .thuTu(1)
                .trangThai((byte) 1)
                .ngayTao(LocalDateTime.now())
                .build();
        when(orderRepository.findByMaHoaDonForUpdate("HD-TEST")).thenReturn(Optional.of(order));
        when(spinRepository.findByChienDichIdAndMaHoaDonIgnoreCase(1, "HD-TEST")).thenReturn(Optional.empty());
        when(prizeRepository.findActiveForUpdate(1)).thenReturn(List.of(prize));
        when(spinRepository.existsByMaNhanThuong(any())).thenReturn(false);
        when(spinRepository.save(any())).thenAnswer(invocation -> {
            LuotQuayMayMan spin = invocation.getArgument(0);
            spin.setId(99);
            return spin;
        });

        Map<String, Object> result = service.spin("HD-TEST", "0912345678");

        assertEquals(Boolean.TRUE, result.get("trungThuong"));
        assertEquals(1, prize.getSoLuongCon());
        verify(prizeRepository).save(prize);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void adminCanSaveManagedCustomPrizeIcon() {
        when(campaignRepository.findById(1)).thenReturn(Optional.of(campaign));
        when(prizeRepository.save(any())).thenAnswer(invocation -> {
            PhanThuongVongQuay prize = invocation.getArgument(0);
            prize.setId(21);
            return prize;
        });
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("tenPhanThuong", "Túi tote Zestia");
        body.put("loaiPhanThuong", "VAT_PHAM");
        body.put("soLuongBanDau", 10);
        body.put("trongSo", 8);
        body.put("mauHienThi", "#2F6F73");
        body.put("bieuTuong", "bi-bag-heart");
        body.put("anhBieuTuong", "/images/lucky-wheel/prize_123abc.webp");
        body.put("thuTu", 4);
        body.put("trangThai", 1);

        Map<String, Object> saved = service.savePrize(1, null, body);

        assertEquals("/images/lucky-wheel/prize_123abc.webp", saved.get("anhBieuTuong"));
        assertEquals("bi-bag-heart", saved.get("bieuTuong"));
    }

    @Test
    void adminCannotAttachExternalPrizeImageUrl() {
        when(campaignRepository.findById(1)).thenReturn(Optional.of(campaign));
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("tenPhanThuong", "Quà thử nghiệm");
        body.put("loaiPhanThuong", "VAT_PHAM");
        body.put("soLuongBanDau", 1);
        body.put("trongSo", 1);
        body.put("mauHienThi", "#17171A");
        body.put("bieuTuong", "bi-gift");
        body.put("anhBieuTuong", "https://example.com/untrusted.svg");

        assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> service.savePrize(1, null, body));
        verify(prizeRepository, never()).save(any());
    }

    @Test
    void adminMarkDeliveredPersistsAndReturnsUpdatedStatus() {
        PhanThuongVongQuay prize = PhanThuongVongQuay.builder()
                .id(7)
                .chienDich(campaign)
                .tenPhanThuong("Balo Zestia")
                .loaiPhanThuong(PhanThuongVongQuay.PHYSICAL)
                .trangThai((byte) 1)
                .build();
        LuotQuayMayMan spin = LuotQuayMayMan.builder()
                .id(99)
                .chienDich(campaign)
                .phanThuong(prize)
                .maHoaDon("HD-TEST")
                .tenKhachHang("Khách test")
                .soDienThoai("0912345678")
                .giaTriDon(new BigDecimal("1250000"))
                .tenKetQua("Balo Zestia")
                .trungThuong(true)
                .maNhanThuong("ZST-TEST")
                .trangThaiNhan(LuotQuayMayMan.WAITING)
                .ngayQuay(LocalDateTime.now())
                .build();
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("admin");
        when(spinRepository.findByIdForUpdate(99)).thenReturn(Optional.of(spin));
        when(spinRepository.saveAndFlush(spin)).thenReturn(spin);

        Map<String, Object> result = service.markDelivered(99, authentication);

        assertEquals(LuotQuayMayMan.DELIVERED, result.get("trangThaiNhan"));
        assertEquals("admin", result.get("nguoiTrao"));
        assertNotNull(result.get("ngayTrao"));
        verify(spinRepository).saveAndFlush(spin);
    }
}
