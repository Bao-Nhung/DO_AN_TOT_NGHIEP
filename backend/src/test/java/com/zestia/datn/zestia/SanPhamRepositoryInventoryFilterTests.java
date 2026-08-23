package com.zestia.datn.zestia;

import com.zestia.datn.zestia.entity.SanPham;
import com.zestia.datn.zestia.entity.SanPhamChiTiet;
import com.zestia.datn.zestia.repository.SanPhamChiTietRepository;
import com.zestia.datn.zestia.repository.SanPhamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class SanPhamRepositoryInventoryFilterTests {

    @Autowired private SanPhamRepository sanPhamRepository;
    @Autowired private SanPhamChiTietRepository chiTietRepository;

    @BeforeEach
    void resetData() {
        chiTietRepository.deleteAll();
        sanPhamRepository.deleteAll();
    }

    @Test
    void filtersByTotalActiveStockAndProductStatus() {
        SanPham healthy = createProduct("FILTER-HEALTHY", (byte) 1);
        createVariant(healthy, "FILTER-HEALTHY-1", 4, (byte) 1);
        createVariant(healthy, "FILTER-HEALTHY-2", 3, (byte) 1);

        SanPham low = createProduct("FILTER-LOW", (byte) 1);
        createVariant(low, "FILTER-LOW-1", 5, (byte) 1);

        SanPham out = createProduct("FILTER-OUT", (byte) 1);
        createVariant(out, "FILTER-OUT-INACTIVE", 99, (byte) 0);

        SanPham inactive = createProduct("FILTER-INACTIVE", (byte) 0);
        createVariant(inactive, "FILTER-INACTIVE-1", 2, (byte) 1);

        PageRequest page = PageRequest.of(0, 20);

        assertThat(sanPhamRepository.findAdminPage(null, null, null, "HEALTHY", page).getContent())
                .extracting(SanPham::getMaSanPham)
                .containsExactly("FILTER-HEALTHY");
        assertThat(sanPhamRepository.findAdminPage(null, null, null, "LOW", page).getContent())
                .extracting(SanPham::getMaSanPham)
                .containsExactlyInAnyOrder("FILTER-LOW", "FILTER-INACTIVE");
        assertThat(sanPhamRepository.findAdminPage(null, null, null, "OUT", page).getContent())
                .extracting(SanPham::getMaSanPham)
                .containsExactly("FILTER-OUT");
        assertThat(sanPhamRepository.findAdminPage(null, (byte) 0, null, null, page).getContent())
                .extracting(SanPham::getMaSanPham)
                .containsExactly("FILTER-INACTIVE");
    }

    private SanPham createProduct(String code, byte status) {
        return sanPhamRepository.save(SanPham.builder()
                .maSanPham(code)
                .tenSanPham(code)
                .trangThai(status)
                .ngayTao(LocalDateTime.of(2026, 8, 1, 8, 0))
                .build());
    }

    private void createVariant(SanPham product, String code, int stock, byte status) {
        chiTietRepository.save(SanPhamChiTiet.builder()
                .sanPham(product)
                .maSanPhamChiTiet(code)
                .giaBan(new BigDecimal("500000"))
                .soLuong(stock)
                .trangThai(status)
                .ngayTao(LocalDateTime.of(2026, 8, 1, 8, 0))
                .build());
    }
}
