package com.zestia.datn.zestia;

import com.zestia.datn.zestia.entity.HoaDon;
import com.zestia.datn.zestia.repository.HoaDonRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = BackendApplication.class)
@AutoConfigureMockMvc
class PaymentAndOrderSecurityTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private HoaDonRepository hoaDonRepository;

    @Test
    @WithMockUser(authorities = "ROLE_Admin")
    void staffCanManuallyConfirmSandboxPayment() throws Exception {
        HoaDon order = hoaDonRepository.save(HoaDon.builder()
                .maHoaDon("HDTESTCONFIRM")
                .tongTien(BigDecimal.valueOf(150000))
                .hinhThucThanhToan("MOMO")
                .trangThai((byte) 0)
                .daThanhToan(false)
                .ngayTao(LocalDateTime.now())
                .build());

        mockMvc.perform(post("/api/payment/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderId\":" + order.getId() + ",\"method\":\"MOMO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trangThai").value(1));

        HoaDon saved = hoaDonRepository.findById(order.getId()).orElseThrow();
        assertThat(saved.getDaThanhToan()).isTrue();
        assertThat(saved.getPhuongThucThanhToanOnline()).isEqualTo("MOMO");
    }

    @Test
    void publicOrderSearchRequiresPhoneNumber() throws Exception {
        hoaDonRepository.save(HoaDon.builder()
                .maHoaDon("HDTESTLOOKUP")
                .tenKhachHang("Khach Test")
                .soDienThoai("0900000099")
                .tongTien(BigDecimal.valueOf(100000))
                .hinhThucThanhToan("COD")
                .trangThai((byte) 0)
                .daThanhToan(false)
                .ngayTao(LocalDateTime.now())
                .build());

        mockMvc.perform(get("/api/hoa-don/search")
                        .param("maHoaDon", "HDTESTLOOKUP"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/hoa-don/search")
                        .param("maHoaDon", "HDTESTLOOKUP")
                        .param("soDienThoai", "0900000099"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maHoaDon").value("HDTESTLOOKUP"));
    }
}
