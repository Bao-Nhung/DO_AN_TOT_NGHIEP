package com.zestia.datn.zestia;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zestia.datn.zestia.config.JwtUtil;
import com.zestia.datn.zestia.entity.HoaDon;
import com.zestia.datn.zestia.entity.HoaDonChiTiet;
import com.zestia.datn.zestia.entity.GiamGia;
import com.zestia.datn.zestia.entity.KhachHang;
import com.zestia.datn.zestia.entity.KichThuoc;
import com.zestia.datn.zestia.entity.LichLamViec;
import com.zestia.datn.zestia.entity.MauSac;
import com.zestia.datn.zestia.entity.NhanVien;
import com.zestia.datn.zestia.entity.SupportConversation;
import com.zestia.datn.zestia.entity.VaiTro;
import com.zestia.datn.zestia.entity.SanPham;
import com.zestia.datn.zestia.entity.SanPhamChiTiet;
import com.zestia.datn.zestia.repository.HoaDonRepository;
import com.zestia.datn.zestia.repository.HoaDonChiTietRepository;
import com.zestia.datn.zestia.repository.GiamGiaRepository;
import com.zestia.datn.zestia.repository.KichThuocRepository;
import com.zestia.datn.zestia.repository.KhachHangRepository;
import com.zestia.datn.zestia.repository.LichSuThanhToanRepository;
import com.zestia.datn.zestia.repository.LichLamViecRepository;
import com.zestia.datn.zestia.repository.MauSacRepository;
import com.zestia.datn.zestia.repository.NhanVienRepository;
import com.zestia.datn.zestia.repository.PosPhienGiuHangRepository;
import com.zestia.datn.zestia.repository.SupportConversationRepository;
import com.zestia.datn.zestia.repository.VaiTroRepository;
import com.zestia.datn.zestia.repository.SanPhamChiTietRepository;
import com.zestia.datn.zestia.repository.SanPhamRepository;
import com.zestia.datn.zestia.repository.YeuCauDoiTraRepository;
import com.zestia.datn.zestia.service.EmailService;
import com.zestia.datn.zestia.service.GatewayPaymentResultService;
import com.zestia.datn.zestia.service.OrderStatusService;
import com.zestia.datn.zestia.service.ShiftReportService;
import com.zestia.datn.zestia.service.SupportChatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = BackendApplication.class)
@AutoConfigureMockMvc
class PaymentAndOrderSecurityTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HoaDonRepository hoaDonRepository;

    @Autowired
    private HoaDonChiTietRepository orderDetailRepository;

    @Autowired
    private GiamGiaRepository voucherRepository;

    @Autowired
    private KhachHangRepository customerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private GatewayPaymentResultService paymentResultService;

    @Autowired
    private OrderStatusService orderStatusService;

    @Autowired
    private LichSuThanhToanRepository paymentHistoryRepository;

    @Autowired
    private SanPhamRepository vayRepository;

    @Autowired
    private SanPhamChiTietRepository variantRepository;

    @Autowired
    private MauSacRepository colorRepository;

    @Autowired
    private KichThuocRepository sizeRepository;

    @Autowired
    private NhanVienRepository employeeRepository;

    @Autowired
    private PosPhienGiuHangRepository posSessionRepository;

    @Autowired
    private LichLamViecRepository scheduleRepository;

    @Autowired
    private VaiTroRepository roleRepository;

    @Autowired
    private YeuCauDoiTraRepository returnRequestRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ShiftReportService shiftReportService;

    @Autowired
    private SupportChatService supportChatService;

    @Autowired
    private SupportConversationRepository supportConversationRepository;

    @MockitoBean
    private EmailService emailService;

    @Test
    @WithMockUser(authorities = "ROLE_Admin")
    void manualPaymentConfirmationEndpointIsRemoved() throws Exception {
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
                .andExpect(status().isNotFound());

        HoaDon saved = hoaDonRepository.findById(order.getId()).orElseThrow();
        assertThat(saved.getDaThanhToan()).isFalse();
        assertThat(saved.getTrangThai()).isZero();
    }

    @Test
    @WithMockUser(authorities = "ROLE_Admin")
    void unpaidOnlineOrderCannotAdvanceToConfirmed() throws Exception {
        HoaDon order = hoaDonRepository.save(HoaDon.builder()
                .maHoaDon("HDTESTSTATE")
                .tongTien(BigDecimal.valueOf(350000))
                .hinhThucThanhToan("ZALOPAY")
                .trangThai((byte) 0)
                .daThanhToan(false)
                .ngayTao(LocalDateTime.now())
                .build());

        mockMvc.perform(put("/api/hoa-don/" + order.getId() + "/trang-thai")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"trangThai\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Đơn MoMo/ZaloPay chỉ được xác nhận sau khi cổng thanh toán báo thành công"));

        HoaDon unchanged = hoaDonRepository.findById(order.getId()).orElseThrow();
        assertThat(unchanged.getTrangThai()).isZero();
        assertThat(unchanged.getDaThanhToan()).isFalse();
    }

    @Test
    void verifiedGatewaySuccessIsIdempotent() {
        HoaDon order = hoaDonRepository.save(HoaDon.builder()
                .maHoaDon("HDTESTIDEMPOTENT")
                .tongTien(BigDecimal.valueOf(420000))
                .hinhThucThanhToan("MOMO")
                .trangThai((byte) 0)
                .daThanhToan(false)
                .ngayTao(LocalDateTime.now())
                .build());

        var first = paymentResultService.applyById(order.getId(), "MOMO", BigDecimal.valueOf(420000), "TXN-IDEMPOTENT", true);
        var second = paymentResultService.applyById(order.getId(), "MOMO", BigDecimal.valueOf(420000), "TXN-IDEMPOTENT", true);

        assertThat(first.success()).isTrue();
        assertThat(first.idempotent()).isFalse();
        assertThat(second.success()).isTrue();
        assertThat(second.idempotent()).isTrue();
        assertThat(paymentHistoryRepository.findByHoaDonId(order.getId())).hasSize(1);
    }

    @Test
    void gatewayAmountMismatchFailsOrderAndRestoresOnlyOnce() {
        HoaDon order = hoaDonRepository.save(HoaDon.builder()
                .maHoaDon("HDTESTAMOUNT")
                .tongTien(BigDecimal.valueOf(500000))
                .hinhThucThanhToan("ZALOPAY")
                .trangThai((byte) 0)
                .daThanhToan(false)
                .ngayTao(LocalDateTime.now())
                .build());

        var first = paymentResultService.applyById(order.getId(), "ZALOPAY", BigDecimal.valueOf(1000), "TXN-WRONG-AMOUNT", true);
        var second = paymentResultService.applyById(order.getId(), "ZALOPAY", BigDecimal.valueOf(1000), "TXN-WRONG-AMOUNT", true);

        HoaDon failed = hoaDonRepository.findById(order.getId()).orElseThrow();
        assertThat(first.success()).isFalse();
        assertThat(second.idempotent()).isTrue();
        assertThat(failed.getTrangThai()).isEqualTo((byte) 7);
        assertThat(failed.getDaHoanTonKho()).isTrue();
        assertThat(paymentHistoryRepository.findByHoaDonId(order.getId())).hasSize(1);
    }

    @Test
    @WithMockUser(authorities = "ROLE_NhanVien")
    void employeeCannotAccessAdminInventoryOrRevenueDashboard() throws Exception {
        mockMvc.perform(get("/api/dashboard/inventory"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/dashboard/stats"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ROLE_Admin")
    void dashboardAggregateQueriesExecuteForAdmin() throws Exception {
        mockMvc.perform(get("/api/dashboard/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tongDonHang").isNumber())
                .andExpect(jsonPath("$.topSellingProducts").isArray())
                .andExpect(jsonPath("$.lowStockVariants").isArray());
    }

    @Test
    void productPaginationReturnsOnlyTheRequestedServerPage() throws Exception {
        String marker = "Paged dress " + System.nanoTime();
        for (int index = 0; index < 12; index++) {
            vayRepository.save(SanPham.builder()
                    .maSanPham("V-PAGED-" + System.nanoTime() + "-" + index)
                    .tenSanPham(marker + " " + index)
                    .trangThai((byte) 1)
                    .ngayTao(LocalDateTime.now().plusNanos(index))
                    .build());
        }

        mockMvc.perform(get("/api/san-pham/paged")
                        .param("q", marker)
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalElements").value(12))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.content.length()").value(5));
    }

    @Test
    void staleInactiveEmployeeTokenCannotRevealLockedProducts() throws Exception {
        String marker = String.valueOf(System.nanoTime());
        VaiTro role = roleRepository.findByTenVaiTro("NhanVien")
                .orElseGet(() -> roleRepository.save(VaiTro.builder().tenVaiTro("NhanVien").build()));
        NhanVien employee = employeeRepository.save(NhanVien.builder()
                .vaiTro(role)
                .maNhanVien("NV-LOCKED-" + marker)
                .hoVaTen("Nhan vien da khoa")
                .email("locked-" + marker + "@example.com")
                .tenNguoiDung("locked-" + marker)
                .matKhau(passwordEncoder.encode("Matkhau123"))
                .tinhTrangLamViec((byte) 0)
                .ngayTao(LocalDateTime.now())
                .build());
        SanPham lockedProduct = vayRepository.save(SanPham.builder()
                .maSanPham("V-LOCKED-" + marker)
                .tenSanPham("Locked product " + marker)
                .trangThai((byte) 0)
                .ngayTao(LocalDateTime.now())
                .build());
        String staleToken = jwtUtil.generateToken(employee.getTenNguoiDung(), "NhanVien", employee.getId());

        String response = mockMvc.perform(get("/api/vay")
                        .header("Authorization", "Bearer " + staleToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertThat(response).doesNotContain(lockedProduct.getTenVay());
    }

    @Test
    @WithMockUser(authorities = "ROLE_KhachHang")
    void customerCannotSearchOtherCustomersOrdersByPhone() throws Exception {
        mockMvc.perform(get("/api/hoa-don/search-by-phone")
                        .param("soDienThoai", "0900000001"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/staff/tasks"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ROLE_Admin")
    void orderStatusMachineRejectsSkippedStages() throws Exception {
        HoaDon order = hoaDonRepository.save(HoaDon.builder()
                .maHoaDon("HDTESTSKIPSTATE")
                .tongTien(BigDecimal.valueOf(300000))
                .hinhThucThanhToan("COD")
                .trangThai((byte) 0)
                .daThanhToan(false)
                .ngayTao(LocalDateTime.now())
                .build());

        mockMvc.perform(put("/api/hoa-don/" + order.getId() + "/trang-thai")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"trangThai\":3}"))
                .andExpect(status().isBadRequest());

        assertThat(hoaDonRepository.findById(order.getId()).orElseThrow().getTrangThai()).isZero();
    }

    @Test
    @WithMockUser(authorities = "ROLE_Admin")
    void adminCanCancelConfirmedCodAndRestoreStockAndVoucherOnlyOnce() throws Exception {
        CancelableOrderFixture fixture = createCancelableCodOrder((byte) 1, 5, 8, 2);

        mockMvc.perform(put("/api/hoa-don/" + fixture.order().getId() + "/trang-thai")
                        .contentType(MediaType.APPLICATION_JSON)
                .content("{\"trangThai\":5,\"ghiChu\":\"Khách đề nghị hủy trước khi đóng gói\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trangThai").value(5));

        assertThat(hoaDonRepository.findById(fixture.order().getId()).orElseThrow().getDaHoanTonKho()).isTrue();
        assertThat(variantRepository.findById(fixture.variant().getId()).orElseThrow().getSoLuong()).isEqualTo(7);
        assertThat(voucherRepository.findById(fixture.voucher().getId()).orElseThrow().getSoLuong()).isEqualTo(9);

        mockMvc.perform(put("/api/hoa-don/" + fixture.order().getId() + "/trang-thai")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"trangThai\":5,\"ghiChu\":\"Yêu cầu lặp lại\"}"))
                .andExpect(status().isOk());

        assertThat(variantRepository.findById(fixture.variant().getId()).orElseThrow().getSoLuong()).isEqualTo(7);
        assertThat(voucherRepository.findById(fixture.voucher().getId()).orElseThrow().getSoLuong()).isEqualTo(9);
    }

    @Test
    @WithMockUser(authorities = "ROLE_NhanVien")
    void employeeCanCancelPreparingCodAndRestoreReservation() throws Exception {
        CancelableOrderFixture fixture = createCancelableCodOrder((byte) 2, 4, 6, 1);

        mockMvc.perform(put("/api/hoa-don/" + fixture.order().getId() + "/trang-thai")
                        .contentType(MediaType.APPLICATION_JSON)
                .content("{\"trangThai\":5,\"ghiChu\":\"Khách đổi ý trước khi giao\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trangThai").value(5));

        assertThat(hoaDonRepository.findById(fixture.order().getId()).orElseThrow().getDaHoanTonKho()).isTrue();
        assertThat(variantRepository.findById(fixture.variant().getId()).orElseThrow().getSoLuong()).isEqualTo(5);
        assertThat(voucherRepository.findById(fixture.voucher().getId()).orElseThrow().getSoLuong()).isEqualTo(7);
    }

    @Test
    @WithMockUser(authorities = "ROLE_Admin")
    void confirmedNonCodOrderCannotUseCodCancellationFlow() throws Exception {
        CancelableOrderFixture fixture = createCancelableCodOrder((byte) 1, 5, 8, 2);
        fixture.order().setHinhThucThanhToan("MOMO");
        hoaDonRepository.save(fixture.order());

        mockMvc.perform(put("/api/hoa-don/" + fixture.order().getId() + "/trang-thai")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"trangThai\":5,\"ghiChu\":\"Không được đi vòng luồng COD\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Chỉ đơn COD mới được hủy ở bước đã xác nhận hoặc đang chuẩn bị"));

        assertThat(hoaDonRepository.findById(fixture.order().getId()).orElseThrow().getTrangThai()).isEqualTo((byte) 1);
        assertThat(variantRepository.findById(fixture.variant().getId()).orElseThrow().getSoLuong()).isEqualTo(5);
        assertThat(voucherRepository.findById(fixture.voucher().getId()).orElseThrow().getSoLuong()).isEqualTo(8);
    }

    @Test
    void checkoutRequiresCustomerIdentityAtBackend() throws Exception {
        mockMvc.perform(post("/api/payment/create-order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"soDienThoai":"0911111111","email":"guest@example.com",
                                 "diaChi":"1 Pho Hue, Ha Noi","hinhThucThanhToan":"COD",
                                 "items":[{"productId":1,"variantId":1,"qty":1}]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Vui lòng nhập họ tên hợp lệ"));
    }

    @Test
    void checkoutRejectsMalformedOrderLinesWithoutServerError() throws Exception {
        mockMvc.perform(post("/api/payment/create-order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"hoTen":"Khach Test","soDienThoai":"0911111111","email":"guest@example.com",
                                 "diaChi":"1 Pho Hue, Ha Noi","hinhThucThanhToan":"COD","items":[1]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Dòng sản phẩm không hợp lệ"));
    }

    @Test
    void checkoutRejectsInvalidVietnamPhoneBeforeCreatingOrder() throws Exception {
        mockMvc.perform(post("/api/payment/create-order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"hoTen":"Khach Test","soDienThoai":"0212345678","email":"guest@example.com",
                                 "diaChi":"1 Pho Hue, Ha Noi","hinhThucThanhToan":"COD",
                                 "items":[{"productId":1,"variantId":1,"qty":1}]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Vui lòng nhập số điện thoại Việt Nam hợp lệ"));
    }

    @Test
    void posCheckoutRequiresAndLinksCustomerPhoneThenDeductsStock() throws Exception {
        String marker = String.valueOf(System.nanoTime());
        VaiTro role = roleRepository.findByTenVaiTro("NhanVien")
                .orElseGet(() -> roleRepository.save(VaiTro.builder().tenVaiTro("NhanVien").build()));
        NhanVien employee = employeeRepository.save(NhanVien.builder()
                .vaiTro(role)
                .maNhanVien("NV-POS-" + marker)
                .hoVaTen("Nhan vien POS test")
                .email("pos-" + marker + "@example.com")
                .tenNguoiDung("pos-" + marker)
                .matKhau(passwordEncoder.encode("123456"))
                .tinhTrangLamViec((byte) 1)
                .ngayTao(LocalDateTime.now())
                .build());
        scheduleRepository.save(LichLamViec.builder()
                .nhanVien(employee)
                .ngayLam(LocalDateTime.now().toLocalDate())
                .caLam("Ca kiểm thử POS")
                .gioBatDau(LocalTime.of(0, 0))
                .gioKetThuc(LocalTime.of(23, 59, 59))
                .trangThai((byte) 1)
                .thoiGianXacNhan(LocalDateTime.now().minusMinutes(10))
                .gioCheckIn(LocalDateTime.now().minusMinutes(5))
                .ngayTao(LocalDateTime.now())
                .build());
        MauSac color = colorRepository.save(MauSac.builder().tenMauSac("POS Black " + marker).trangThai((byte) 1).build());
        KichThuoc size = sizeRepository.save(KichThuoc.builder().tenKichThuoc("POS-S-" + marker).trangThai((byte) 1).build());
        SanPham product = vayRepository.save(SanPham.builder()
                .maSanPham("V-POS-" + marker)
                .tenSanPham("POS checkout dress")
                .trangThai((byte) 1)
                .ngayTao(LocalDateTime.now())
                .build());
        SanPhamChiTiet variant = variantRepository.save(SanPhamChiTiet.builder()
                .sanPham(product)
                .mauSac(color)
                .kichThuoc(size)
                .maSanPhamChiTiet("VC-POS-" + marker)
                .giaBanGoc(BigDecimal.valueOf(600000))
                .giaBan(BigDecimal.valueOf(500000))
                .giaNhap(BigDecimal.valueOf(300000))
                .soLuong(2)
                .trangThai((byte) 1)
                .ngayTao(LocalDateTime.now())
                .build());
        String token = jwtUtil.generateToken(employee.getTenNguoiDung(), "NhanVien", employee.getId());
        String customerPhone = "09" + marker.substring(Math.max(0, marker.length() - 8));

        mockMvc.perform(post("/api/payment/create-order")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tenKhachHang":"Khách POS test","soDienThoai":"%s","hinhThucThanhToan":"Tiền mặt",
                                 "hinhThucNhanHang":0,"trangThai":4,"daThanhToan":true,"tienKhachDua":499999,
                                 "nhanVienId":%d,"items":[{"productId":%d,"variantId":%d,"qty":1}]}
                                """.formatted(customerPhone, employee.getId(), product.getId(), variant.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Số tiền khách đưa chưa đủ. Còn thiếu 1.00đ"));

        assertThat(variantRepository.findById(variant.getId()).orElseThrow().getSoLuong()).isEqualTo(2);

        mockMvc.perform(post("/api/payment/create-order")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tenKhachHang":"Khách POS test","soDienThoai":"%s","hinhThucThanhToan":"Tiền mặt",
                                 "hinhThucNhanHang":0,"trangThai":4,"daThanhToan":true,"tienKhachDua":500000,
                                 "nhanVienId":%d,"items":[{"productId":%d,"variantId":%d,"qty":1}]}
                                """.formatted(customerPhone, employee.getId(), product.getId(), variant.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trangThai").value(4))
                .andExpect(jsonPath("$.hinhThucNhanHang").value(0))
                .andExpect(jsonPath("$.hinhThucThanhToan").value("Tiền mặt"));

        assertThat(variantRepository.findById(variant.getId()).orElseThrow().getSoLuong()).isEqualTo(1);
        HoaDon saved = hoaDonRepository.findByNhanVienId(employee.getId()).stream().findFirst().orElseThrow();
        assertThat(saved.getSoDienThoai()).isEqualTo(customerPhone);
        assertThat(saved.getKhachHang()).isNotNull();
        assertThat(saved.getKhachHang().getSoDienThoai()).isEqualTo(customerPhone);
        assertThat(saved.getDaThanhToan()).isTrue();
        assertThat(saved.getDiaChiGiaoHang()).isEqualTo("Mua trực tiếp tại cửa hàng");

        mockMvc.perform(get("/api/staff/tasks")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").isNumber())
                .andExpect(jsonPath("$.items").isArray());

        mockMvc.perform(get("/api/staff/dashboard")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.todayOrderCount").value(1))
                .andExpect(jsonPath("$.todayPosRevenue").value(500000))
                .andExpect(jsonPath("$.recentOrders[0].maHoaDon").value(saved.getMaHoaDon()));
    }

    @Test
    void posReservationRestoresOnClearAndExpiryThenCheckoutDoesNotDeductTwice() throws Exception {
        String marker = String.valueOf(System.nanoTime());
        VaiTro role = roleRepository.findByTenVaiTro("NhanVien")
                .orElseGet(() -> roleRepository.save(VaiTro.builder().tenVaiTro("NhanVien").build()));
        NhanVien employee = employeeRepository.save(NhanVien.builder()
                .vaiTro(role)
                .maNhanVien("NV-HOLD-" + marker)
                .hoVaTen("Nhan vien giu ton POS")
                .email("pos-hold-" + marker + "@example.com")
                .tenNguoiDung("pos-hold-" + marker)
                .matKhau(passwordEncoder.encode("123456"))
                .tinhTrangLamViec((byte) 1)
                .ngayTao(LocalDateTime.now())
                .build());
        scheduleRepository.save(LichLamViec.builder()
                .nhanVien(employee)
                .ngayLam(LocalDate.now())
                .caLam("Ca kiểm thử giữ tồn POS")
                .gioBatDau(LocalTime.MIN)
                .gioKetThuc(LocalTime.of(23, 59, 59))
                .trangThai((byte) 1)
                .thoiGianXacNhan(LocalDateTime.now().minusMinutes(10))
                .gioCheckIn(LocalDateTime.now().minusMinutes(5))
                .ngayTao(LocalDateTime.now())
                .build());
        MauSac color = colorRepository.save(MauSac.builder()
                .tenMauSac("Hold Black " + marker)
                .trangThai((byte) 1)
                .build());
        KichThuoc size = sizeRepository.save(KichThuoc.builder()
                .tenKichThuoc("HOLD-S-" + marker)
                .trangThai((byte) 1)
                .build());
        SanPham product = vayRepository.save(SanPham.builder()
                .maSanPham("V-HOLD-" + marker)
                .tenSanPham("POS reservation dress")
                .trangThai((byte) 1)
                .ngayTao(LocalDateTime.now())
                .build());
        SanPhamChiTiet variant = variantRepository.save(SanPhamChiTiet.builder()
                .sanPham(product)
                .mauSac(color)
                .kichThuoc(size)
                .maSanPhamChiTiet("VC-HOLD-" + marker)
                .giaBanGoc(BigDecimal.valueOf(500000))
                .giaBan(BigDecimal.valueOf(500000))
                .giaNhap(BigDecimal.valueOf(300000))
                .soLuong(4)
                .trangThai((byte) 1)
                .ngayTao(LocalDateTime.now())
                .build());
        GiamGia voucher = voucherRepository.save(GiamGia.builder()
                .maGiamGia("HOLD" + marker)
                .tenGiamGia("Voucher giữ tồn POS")
                .gioTriGiam(BigDecimal.valueOf(50000))
                .giaTriDonToiThieu(BigDecimal.ZERO)
                .soLuong(2)
                .ngayBatDau(LocalDate.now().minusDays(1))
                .ngayKetThuc(LocalDate.now().plusDays(1))
                .trangThai((byte) 1)
                .build());
        String token = jwtUtil.generateToken(employee.getTenNguoiDung(), "NhanVien", employee.getId());
        String authorization = "Bearer " + token;

        String firstSession = reservationToken(mockMvc.perform(post("/api/pos-reservations/items")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"variantId":%d,"quantity":2}
                                """.formatted(variant.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        mockMvc.perform(put("/api/pos-reservations/" + firstSession + "/voucher")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"maGiamGia\":\"" + voucher.getMaGiamGia() + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.discount").value(50000));
        assertThat(variantRepository.findById(variant.getId()).orElseThrow().getSoLuong()).isEqualTo(2);
        assertThat(voucherRepository.findById(voucher.getId()).orElseThrow().getSoLuong()).isEqualTo(1);

        mockMvc.perform(delete("/api/pos-reservations/" + firstSession)
                        .header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RELEASED"));
        assertThat(variantRepository.findById(variant.getId()).orElseThrow().getSoLuong()).isEqualTo(4);
        assertThat(voucherRepository.findById(voucher.getId()).orElseThrow().getSoLuong()).isEqualTo(2);

        String expiredSession = reservationToken(mockMvc.perform(post("/api/pos-reservations/items")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"variantId":%d,"quantity":1}
                                """.formatted(variant.getId())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        mockMvc.perform(put("/api/pos-reservations/" + expiredSession + "/voucher")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"maGiamGia\":\"" + voucher.getMaGiamGia() + "\"}"))
                .andExpect(status().isOk());
        var expiring = posSessionRepository.findByMaPhien(expiredSession).orElseThrow();
        expiring.setHetHanLuc(LocalDateTime.now().minusSeconds(1));
        posSessionRepository.saveAndFlush(expiring);

        mockMvc.perform(post("/api/pos-reservations/" + expiredSession + "/voucher/best")
                        .header("Authorization", authorization))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Phiên giữ hàng đã hết hạn. Vui lòng tạo lại giỏ POS"));
        assertThat(posSessionRepository.findByMaPhien(expiredSession).orElseThrow().getTrangThai()).isEqualTo("EXPIRED");
        assertThat(variantRepository.findById(variant.getId()).orElseThrow().getSoLuong()).isEqualTo(4);
        assertThat(voucherRepository.findById(voucher.getId()).orElseThrow().getSoLuong()).isEqualTo(2);

        String checkoutSession = reservationToken(mockMvc.perform(post("/api/pos-reservations/items")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"variantId":%d,"quantity":1}
                                """.formatted(variant.getId())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        mockMvc.perform(put("/api/pos-reservations/" + checkoutSession + "/voucher")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"maGiamGia\":\"" + voucher.getMaGiamGia() + "\"}"))
                .andExpect(status().isOk());

        String customerPhone = "09" + marker.substring(Math.max(0, marker.length() - 8));
        mockMvc.perform(post("/api/payment/create-order")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tenKhachHang":"Khách giữ tồn POS","soDienThoai":"%s",
                                 "hinhThucThanhToan":"Tiền mặt","hinhThucNhanHang":0,
                                 "tienKhachDua":450000,"nhanVienId":%d,
                                 "posReservationToken":"%s","maGiamGia":"%s",
                                 "items":[{"productId":%d,"variantId":%d,"qty":1}]}
                                """.formatted(
                                customerPhone, employee.getId(), checkoutSession, voucher.getMaGiamGia(),
                                product.getId(), variant.getId()
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trangThai").value(4))
                .andExpect(jsonPath("$.giamGia").value(50000));

        assertThat(variantRepository.findById(variant.getId()).orElseThrow().getSoLuong()).isEqualTo(3);
        assertThat(voucherRepository.findById(voucher.getId()).orElseThrow().getSoLuong()).isEqualTo(1);
        assertThat(posSessionRepository.findByMaPhien(checkoutSession).orElseThrow().getTrangThai())
                .isEqualTo("COMPLETED");
    }

    @Test
    void concurrentCheckoutCannotOversellTheLastVariant() throws Exception {
        MauSac color = colorRepository.save(MauSac.builder().tenMauSac("Concurrency Black").trangThai((byte) 1).build());
        KichThuoc size = sizeRepository.save(KichThuoc.builder().tenKichThuoc("CONCURRENT-S").trangThai((byte) 1).build());
        SanPham product = vayRepository.save(SanPham.builder()
                .maSanPham("V-CONCURRENT-" + System.nanoTime())
                .tenSanPham("Concurrent checkout dress")
                .trangThai((byte) 1)
                .ngayTao(LocalDateTime.now())
                .build());
        SanPhamChiTiet variant = variantRepository.save(SanPhamChiTiet.builder()
                .sanPham(product)
                .mauSac(color)
                .kichThuoc(size)
                .maSanPhamChiTiet("VC-CONCURRENT-" + System.nanoTime())
                .giaBanGoc(BigDecimal.valueOf(600000))
                .giaBan(BigDecimal.valueOf(500000))
                .giaNhap(BigDecimal.valueOf(300000))
                .soLuong(1)
                .trangThai((byte) 1)
                .ngayTao(LocalDateTime.now())
                .build());

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Integer> first = executor.submit(() -> performCheckout(variant, "A", "0911111121", ready, start));
            Future<Integer> second = executor.submit(() -> performCheckout(variant, "B", "0911111122", ready, start));
            ready.await();
            start.countDown();

            List<Integer> statuses = List.of(first.get(), second.get()).stream().sorted().toList();
            assertThat(statuses.get(0)).isEqualTo(200);
            assertThat(statuses.get(1)).isIn(400, 409);
            assertThat(variantRepository.findById(variant.getId()).orElseThrow().getSoLuong()).isZero();
        } finally {
            executor.shutdownNow();
        }
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

    @Test
    void guestCancellationRequiresEmailOtp() throws Exception {
        HoaDon order = hoaDonRepository.save(HoaDon.builder()
                .maHoaDon("HDTESTCANCELOTP")
                .tenKhachHang("Khach OTP")
                .soDienThoai("0900000088")
                .emailKhachHang("khachotp@example.com")
                .tongTien(BigDecimal.valueOf(200000))
                .hinhThucThanhToan("COD")
                .trangThai((byte) 0)
                .daThanhToan(false)
                .ngayTao(LocalDateTime.now())
                .build());

        String identityBody = "{\"maHoaDon\":\"HDTESTCANCELOTP\",\"soDienThoai\":\"0900000088\"}";
        mockMvc.perform(put("/api/hoa-don/" + order.getId() + "/cancel-guest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(identityBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Vui lòng nhập mã OTP gồm 6 chữ số"));

        HoaDon notCancelled = hoaDonRepository.findById(order.getId()).orElseThrow();
        assertThat(notCancelled.getTrangThai()).isZero();

        mockMvc.perform(post("/api/hoa-don/" + order.getId() + "/cancel-guest/request-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(identityBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emailMasked").value("kh***@example.com"));

        HoaDon withOtp = hoaDonRepository.findById(order.getId()).orElseThrow();
        assertThat(withOtp.getHuyDonOtpHash()).isNotBlank();
        assertThat(withOtp.getHuyDonOtpHetHan()).isAfter(LocalDateTime.now());
        assertThat(withOtp.getTrangThai()).isZero();

        withOtp.setHuyDonOtpHash(passwordEncoder.encode("123456"));
        hoaDonRepository.save(withOtp);
        mockMvc.perform(put("/api/hoa-don/" + order.getId() + "/cancel-guest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"maHoaDon\":\"HDTESTCANCELOTP\",\"soDienThoai\":\"0900000088\",\"otp\":\"123456\",\"ghiChu\":\"Đổi ý\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trangThai").value(5));

        HoaDon cancelled = hoaDonRepository.findById(order.getId()).orElseThrow();
        assertThat(cancelled.getTrangThai()).isEqualTo((byte) 5);
        assertThat(cancelled.getHuyDonOtpHash()).isNull();
    }

    @Test
    void employeeOutsideCheckedInShiftIsBlocked() throws Exception {
        String marker = String.valueOf(System.nanoTime());
        VaiTro role = roleRepository.findByTenVaiTro("NhanVien")
                .orElseGet(() -> roleRepository.save(VaiTro.builder().tenVaiTro("NhanVien").build()));
        NhanVien employee = employeeRepository.save(NhanVien.builder()
                .vaiTro(role)
                .maNhanVien("NV-NOSHIFT-" + marker)
                .hoVaTen("Nhan vien chua check in")
                .email("no-shift-" + marker + "@example.com")
                .tenNguoiDung("no-shift-" + marker)
                .matKhau(passwordEncoder.encode("123456"))
                .tinhTrangLamViec((byte) 1)
                .ngayTao(LocalDateTime.now())
                .build());
        String token = jwtUtil.generateToken(employee.getTenNguoiDung(), "NhanVien", employee.getId());

        mockMvc.perform(get("/api/vay").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("SHIFT_REQUIRED"));
    }

    @Test
    void paidWalletReturnRequiresRefundReceivingInformation() throws Exception {
        String marker = String.valueOf(System.nanoTime());
        KhachHang customer = customerRepository.save(KhachHang.builder()
                .maKhachHang("KH-RETURN-" + marker)
                .hoVaTen("Khach doi tra")
                .email("return-" + marker + "@example.com")
                .soDienThoai("0912345678")
                .ngayTao(LocalDateTime.now())
                .build());
        HoaDon order = hoaDonRepository.save(HoaDon.builder()
                .maHoaDon("HD-RETURN-" + marker)
                .khachHang(customer)
                .tongTien(BigDecimal.valueOf(700000))
                .hinhThucThanhToan("MOMO")
                .hinhThucNhanHang((byte) 1)
                .trangThai((byte) 4)
                .daThanhToan(true)
                .ngayTao(LocalDateTime.now())
                .build());
        MauSac color = colorRepository.save(MauSac.builder().tenMauSac("Return Black " + marker).trangThai((byte) 1).build());
        KichThuoc size = sizeRepository.save(KichThuoc.builder().tenKichThuoc("RETURN-S-" + marker).trangThai((byte) 1).build());
        SanPham product = vayRepository.save(SanPham.builder().maSanPham("V-RETURN-" + marker).tenSanPham("Return dress").trangThai((byte) 1).ngayTao(LocalDateTime.now()).build());
        SanPhamChiTiet variant = variantRepository.save(SanPhamChiTiet.builder()
                .sanPham(product).mauSac(color).kichThuoc(size).maSanPhamChiTiet("VC-RETURN-" + marker)
                .giaBanGoc(BigDecimal.valueOf(700000)).giaBan(BigDecimal.valueOf(700000))
                .soLuong(2).trangThai((byte) 1).ngayTao(LocalDateTime.now()).build());
        HoaDonChiTiet detail = orderDetailRepository.save(HoaDonChiTiet.builder()
                .hoaDon(order).sanPhamChiTiet(variant).soLuong(1).donGia(BigDecimal.valueOf(700000)).build());
        String token = jwtUtil.generateToken(customer.getEmail(), "KhachHang", customer.getId());
        MockMultipartFile image = new MockMultipartFile("images", "condition.png", "image/png",
                java.util.Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII="));

        mockMvc.perform(multipart("/api/returns/online")
                        .file(image)
                        .header("Authorization", "Bearer " + token)
                        .param("orderId", String.valueOf(order.getId()))
                        .param("orderDetailId", String.valueOf(detail.getId()))
                        .param("type", "TRA")
                        .param("quantity", "1")
                        .param("reason", "Sản phẩm không vừa size")
                        .param("condition", "Sản phẩm còn nguyên tem mác và chưa sử dụng"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Vui lòng nhập thông tin nhận tiền hoàn từ 10 đến 500 ký tự"));

        mockMvc.perform(multipart("/api/returns/online")
                        .file(image)
                        .header("Authorization", "Bearer " + token)
                        .param("orderId", String.valueOf(order.getId()))
                        .param("orderDetailId", String.valueOf(detail.getId()))
                        .param("type", "TRA")
                        .param("quantity", "1")
                        .param("reason", "Sản phẩm không vừa size")
                        .param("condition", "Sản phẩm còn nguyên tem mác và chưa sử dụng")
                        .param("refundInfo", "Ví MoMo 0912345678 - Nguyen Van A"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CHO_DUYET"))
                .andExpect(jsonPath("$.refundInfo").value("Ví MoMo 0912345678 - Nguyen Van A"));

        VaiTro adminRole = roleRepository.findByTenVaiTro("Admin")
                .orElseGet(() -> roleRepository.save(VaiTro.builder().tenVaiTro("Admin").build()));
        NhanVien admin = employeeRepository.save(NhanVien.builder()
                .vaiTro(adminRole)
                .maNhanVien("NV-RETURN-" + marker)
                .hoVaTen("Admin return test")
                .email("admin-return-" + marker + "@example.com")
                .tenNguoiDung("admin-return-" + marker)
                .matKhau(passwordEncoder.encode("123456"))
                .tinhTrangLamViec((byte) 1)
                .ngayTao(LocalDateTime.now())
                .build());
        String adminToken = jwtUtil.generateToken(admin.getTenNguoiDung(), "Admin", admin.getId());
        Integer requestId = returnRequestRepository.findByHoaDonChiTietId(detail.getId()).orElseThrow().getId();

        mockMvc.perform(put("/api/returns/" + requestId + "/review")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"approved\":true,\"reason\":\"Ảnh và tình trạng phù hợp\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CHO_NHAN_HANG"));
        mockMvc.perform(put("/api/returns/" + requestId + "/receive")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accepted\":true,\"reason\":\"Đã nhận đủ hàng và tem mác\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CHO_HOAN_TAT"));
        mockMvc.perform(put("/api/returns/" + requestId + "/complete")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"note\":\"Đã chuyển tiền hoàn\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DA_HOAN_TIEN"));
        assertThat(variantRepository.findById(variant.getId()).orElseThrow().getSoLuong()).isEqualTo(3);

        mockMvc.perform(put("/api/returns/" + requestId + "/complete")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isConflict());
        assertThat(variantRepository.findById(variant.getId()).orElseThrow().getSoLuong()).isEqualTo(3);
    }

    @Test
    void deliveryFailureRestoresStockAndVoucherExactlyOnce() {
        String marker = String.valueOf(System.nanoTime());
        MauSac color = colorRepository.save(MauSac.builder().tenMauSac("Failed Black " + marker).trangThai((byte) 1).build());
        KichThuoc size = sizeRepository.save(KichThuoc.builder().tenKichThuoc("FAILED-S-" + marker).trangThai((byte) 1).build());
        SanPham product = vayRepository.save(SanPham.builder()
                .maSanPham("V-FAILED-" + marker)
                .tenSanPham("Delivery failed dress")
                .trangThai((byte) 1)
                .ngayTao(LocalDateTime.now())
                .build());
        SanPhamChiTiet variant = variantRepository.save(SanPhamChiTiet.builder()
                .sanPham(product)
                .mauSac(color)
                .kichThuoc(size)
                .maSanPhamChiTiet("VC-FAILED-" + marker)
                .giaBanGoc(BigDecimal.valueOf(800000))
                .giaBan(BigDecimal.valueOf(700000))
                .soLuong(3)
                .trangThai((byte) 1)
                .ngayTao(LocalDateTime.now())
                .build());
        GiamGia voucher = voucherRepository.save(GiamGia.builder()
                .maGiamGia("FAILED" + marker)
                .tenGiamGia("Voucher hoàn khi giao thất bại")
                .gioTriGiam(BigDecimal.valueOf(50000))
                .soLuong(4)
                .trangThai((byte) 1)
                .build());
        HoaDon order = hoaDonRepository.save(HoaDon.builder()
                .maHoaDon("HD-FAILED-" + marker)
                .giamGia(voucher)
                .tongTien(BigDecimal.valueOf(1350000))
                .hinhThucThanhToan("COD")
                .trangThai((byte) 3)
                .daThanhToan(false)
                .daHoanTonKho(false)
                .ngayTao(LocalDateTime.now())
                .build());
        orderDetailRepository.save(HoaDonChiTiet.builder()
                .hoaDon(order)
                .sanPhamChiTiet(variant)
                .soLuong(2)
                .donGia(BigDecimal.valueOf(700000))
                .build());

        orderStatusService.transition(order.getId(), (byte) 6, "Khách không nhận hàng", "Admin", "Admin");
        orderStatusService.transition(order.getId(), (byte) 6, "Gọi lại vẫn không nhận", "Admin", "Admin");

        assertThat(variantRepository.findById(variant.getId()).orElseThrow().getSoLuong()).isEqualTo(5);
        assertThat(voucherRepository.findById(voucher.getId()).orElseThrow().getSoLuong()).isEqualTo(5);
        HoaDon failed = hoaDonRepository.findById(order.getId()).orElseThrow();
        assertThat(failed.getTrangThai()).isEqualTo((byte) 6);
        assertThat(failed.getDaHoanTonKho()).isTrue();
    }

    @Test
    void shiftReportCalculatesCashHandoverAndExportsRealXlsx() {
        String marker = String.valueOf(System.nanoTime());
        VaiTro role = roleRepository.findByTenVaiTro("Nhân viên")
                .orElseGet(() -> roleRepository.save(VaiTro.builder().tenVaiTro("Nhân viên").build()));
        NhanVien employee = employeeRepository.save(NhanVien.builder()
                .vaiTro(role)
                .maNhanVien("NV-REPORT-" + marker)
                .hoVaTen("Nhan vien doi soat ca")
                .email("shift-report-" + marker + "@example.com")
                .tenNguoiDung("shift-report-" + marker)
                .matKhau(passwordEncoder.encode("Matkhau123"))
                .tinhTrangLamViec((byte) 1)
                .ngayTao(LocalDateTime.now())
                .build());
        LichLamViec shift = scheduleRepository.save(LichLamViec.builder()
                .nhanVien(employee)
                .ngayLam(LocalDate.now())
                .caLam("Ca doi soat")
                .gioBatDau(LocalTime.MIN)
                .gioKetThuc(LocalTime.of(23, 59, 59))
                .trangThai((byte) 1)
                .thoiGianXacNhan(LocalDateTime.now().minusMinutes(10))
                .gioCheckIn(LocalDateTime.now().minusMinutes(5))
                .ngayTao(LocalDateTime.now())
                .build());
        hoaDonRepository.save(HoaDon.builder()
                .maHoaDon("HD-REPORT-" + marker)
                .nhanVien(employee)
                .tongTien(BigDecimal.valueOf(725000))
                .hinhThucNhanHang((byte) 0)
                .hinhThucThanhToan("Tiền mặt")
                .trangThai((byte) 4)
                .daThanhToan(true)
                .ngayTao(LocalDateTime.now())
                .build());

        List<ShiftReportService.ShiftReport> reports = shiftReportService.build(
                LocalDate.now(), LocalDate.now(), employee.getId());
        ShiftReportService.ShiftReport report = reports.stream()
                .filter(item -> item.id().equals(shift.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(report.orderCount()).isEqualTo(1);
        assertThat(report.revenue()).isEqualByComparingTo("725000");
        assertThat(report.cashToHandover()).isEqualByComparingTo("725000");
        assertThat(report.transferAmount()).isZero();
        assertThat(report.activities()).extracting(ShiftReportService.ShiftActivity::type)
                .contains("CHECK_IN", "POS_SALE");

        byte[] workbook = shiftReportService.exportExcel(reports);
        assertThat(workbook).hasSizeGreaterThan(100);
        assertThat(workbook[0]).isEqualTo((byte) 'P');
        assertThat(workbook[1]).isEqualTo((byte) 'K');
    }

    @Test
    void supportRequestIsAssignedToAnEmployeeCurrentlyCheckedIn() {
        String marker = String.valueOf(System.nanoTime());
        VaiTro role = roleRepository.findByTenVaiTro("Nhân viên")
                .orElseGet(() -> roleRepository.save(VaiTro.builder().tenVaiTro("Nhân viên").build()));
        NhanVien employee = employeeRepository.save(NhanVien.builder()
                .vaiTro(role)
                .maNhanVien("NV-SUPPORT-" + marker)
                .hoVaTen("Nhan vien ho tro truc tuyen")
                .email("support-" + marker + "@example.com")
                .tenNguoiDung("support-" + marker)
                .matKhau(passwordEncoder.encode("Matkhau123"))
                .tinhTrangLamViec((byte) 1)
                .ngayTao(LocalDateTime.now())
                .build());
        scheduleRepository.save(LichLamViec.builder()
                .nhanVien(employee)
                .ngayLam(LocalDate.now())
                .caLam("Ca ho tro truc tuyen")
                .gioBatDau(LocalTime.MIN)
                .gioKetThuc(LocalTime.of(23, 59, 59))
                .trangThai((byte) 1)
                .thoiGianXacNhan(LocalDateTime.now().minusMinutes(10))
                .gioCheckIn(LocalDateTime.now().minusMinutes(5))
                .ngayTao(LocalDateTime.now())
                .build());

        Map<String, Object> conversation = supportChatService.requestSupport(
                "Tôi cần nhân viên tư vấn thêm về sản phẩm", null);

        assertThat(conversation.get("status")).isEqualTo(SupportChatService.ACTIVE);
        assertThat(conversation.get("employeeName")).isNotNull();
        assertThat(conversation.get("token").toString()).hasSize(32);
        assertThat((List<?>) conversation.get("messages")).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void adminCanClaimWaitingSupportConversationWithoutAWorkShift() throws Exception {
        String marker = String.valueOf(System.nanoTime());
        VaiTro adminRole = roleRepository.findByTenVaiTro("Admin")
                .orElseGet(() -> roleRepository.save(VaiTro.builder().tenVaiTro("Admin").build()));
        NhanVien admin = employeeRepository.save(NhanVien.builder()
                .vaiTro(adminRole)
                .maNhanVien("NV-ADMIN-CHAT-" + marker)
                .hoVaTen("Admin")
                .email("admin-chat-" + marker + "@example.com")
                .tenNguoiDung("admin-chat-" + marker)
                .matKhau(passwordEncoder.encode("Matkhau123"))
                .tinhTrangLamViec((byte) 1)
                .ngayTao(LocalDateTime.now())
                .build());
        String publicToken = "support" + marker;
        SupportConversation waiting = supportConversationRepository.save(SupportConversation.builder()
                .publicToken(publicToken)
                .tieuDe("Yêu cầu hỗ trợ ngoài ca")
                .trangThai(SupportChatService.WAITING)
                .ngayTao(LocalDateTime.now())
                .ngayCapNhat(LocalDateTime.now())
                .build());
        String adminToken = jwtUtil.generateToken(admin.getTenNguoiDung(), "Admin", admin.getId());

        mockMvc.perform(post("/api/support-chat/staff/conversations/" + waiting.getId() + "/claim")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(SupportChatService.ACTIVE))
                .andExpect(jsonPath("$.employeeName").value("Admin"));

        mockMvc.perform(get("/api/support-chat/customer/" + publicToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(SupportChatService.ACTIVE))
                .andExpect(jsonPath("$.employeeName").value("Admin"));
    }

    @Test
    @WithMockUser(authorities = "ROLE_Admin")
    void confirmedScheduleCannotBeUpdatedOrDeletedByAdmin() throws Exception {
        String marker = String.valueOf(System.nanoTime());
        VaiTro employeeRole = roleRepository.findByTenVaiTro("Nhân viên")
                .orElseGet(() -> roleRepository.save(VaiTro.builder().tenVaiTro("Nhân viên").build()));
        NhanVien employee = employeeRepository.save(NhanVien.builder()
                .vaiTro(employeeRole)
                .maNhanVien("NV-SHIFT-OWNER-" + marker)
                .hoVaTen("Nhan vien tu xac nhan ca")
                .email("shift-owner-" + marker + "@example.com")
                .tenNguoiDung("shift-owner-" + marker)
                .matKhau(passwordEncoder.encode("Matkhau123"))
                .tinhTrangLamViec((byte) 1)
                .ngayTao(LocalDateTime.now())
                .build());
        LocalDate workDate = LocalDate.now().plusYears(3);

        String createResponse = mockMvc.perform(post("/api/lich-lam-viec")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nhanVien":{"id":%d},"ngayLam":"%s","caLam":"Ca kiểm thử",
                                 "gioBatDau":"08:00:00","gioKetThuc":"12:00:00","trangThai":1,
                                 "ghiChu":"Admin tạo lịch"}
                                """.formatted(employee.getId(), workDate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trangThai").value(0))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        int shiftId = objectMapper.readTree(createResponse).path("id").asInt();
        LichLamViec confirmedShift = scheduleRepository.findById(shiftId).orElseThrow();
        confirmedShift.setTrangThai((byte) 1);
        confirmedShift.setThoiGianXacNhan(LocalDateTime.now());
        scheduleRepository.save(confirmedShift);

        mockMvc.perform(put("/api/lich-lam-viec/" + shiftId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nhanVien":{"id":%d},"ngayLam":"%s","caLam":"Ca kiểm thử",
                                 "gioBatDau":"08:00:00","gioKetThuc":"12:00:00","trangThai":2,
                                 "ghiChu":"Admin chỉ sửa ghi chú"}
                """.formatted(employee.getId(), workDate)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Chỉ có thể sửa ca đang chờ xác nhận và chưa được nhân viên phản hồi"));

        mockMvc.perform(delete("/api/lich-lam-viec/" + shiftId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Chỉ có thể xóa ca đang chờ xác nhận và chưa được nhân viên phản hồi"));

        LichLamViec unchanged = scheduleRepository.findById(shiftId).orElseThrow();
        assertThat(unchanged.getGhiChu()).isEqualTo("Admin tạo lịch");
        assertThat(unchanged.getTrangThai()).isEqualTo((byte) 1);
    }

    @Test
    @WithMockUser(authorities = "ROLE_Admin")
    void morningAndAfternoonAreMergedIntoOneFullDayShift() throws Exception {
        String marker = String.valueOf(System.nanoTime());
        NhanVien employee = createScheduleStaff("Nhân viên", "NV-MERGE-", marker);
        LocalDate workDate = LocalDate.now().plusYears(4)
                .plusDays(Math.floorMod(marker.hashCode(), 300));

        mockMvc.perform(post("/api/lich-lam-viec")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nhanVien":{"id":%d},"ngayLam":"%s","caLam":"Ca sáng",
                                 "gioBatDau":"08:00:00","gioKetThuc":"12:00:00","ghiChu":"Trực sáng"}
                                """.formatted(employee.getId(), workDate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trangThai").value(0));

        mockMvc.perform(post("/api/lich-lam-viec")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nhanVien":{"id":%d},"ngayLam":"%s","caLam":"Ca chiều",
                                 "gioBatDau":"13:00:00","gioKetThuc":"17:00:00","ghiChu":"Trực chiều"}
                                """.formatted(employee.getId(), workDate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.autoMerged").value(true))
                .andExpect(jsonPath("$.caLam").value("Cả ngày"))
                .andExpect(jsonPath("$.gioBatDau").value("08:00:00"))
                .andExpect(jsonPath("$.gioKetThuc").value("17:00:00"));

        List<LichLamViec> merged = scheduleRepository
                .findByNhanVienIdAndNgayLamOrderByGioBatDauAsc(employee.getId(), workDate);
        assertThat(merged).hasSize(1);
        assertThat(merged.getFirst().getCaLam()).isEqualTo("Cả ngày");
        assertThat(merged.getFirst().getGhiChu()).contains("Trực sáng", "Trực chiều");

        mockMvc.perform(post("/api/lich-lam-viec")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nhanVien":{"id":%d},"ngayLam":"%s","caLam":"Ca sáng",
                                 "gioBatDau":"08:00:00","gioKetThuc":"12:00:00"}
                                """.formatted(employee.getId(), workDate)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Nhân viên đã có ca làm trùng thời gian"));
    }

    @Test
    @WithMockUser(authorities = "ROLE_Admin")
    void untouchedPendingScheduleIsDeletedFromDatabase() throws Exception {
        String marker = String.valueOf(System.nanoTime());
        NhanVien employee = createScheduleStaff("Nhân viên", "NV-DELETE-", marker);
        LichLamViec shift = scheduleRepository.save(LichLamViec.builder()
                .nhanVien(employee)
                .ngayLam(LocalDate.now().plusYears(5))
                .caLam("Ca sáng")
                .gioBatDau(LocalTime.of(8, 0))
                .gioKetThuc(LocalTime.of(12, 0))
                .trangThai((byte) 0)
                .ngayTao(LocalDateTime.now())
                .build());

        mockMvc.perform(delete("/api/lich-lam-viec/" + shift.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deleted").value(true))
                .andExpect(jsonPath("$.id").value(shift.getId()));

        assertThat(scheduleRepository.existsById(shift.getId())).isFalse();
    }

    @Test
    @WithMockUser(authorities = "ROLE_Admin")
    void everyBusyRequestRequiresAdminReviewAndLocksTheSchedule() throws Exception {
        String marker = String.valueOf(System.nanoTime());
        NhanVien employee = createScheduleStaff("Nhân viên", "NV-BUSY-", marker);
        NhanVien admin = createScheduleStaff("Admin", "NV-REVIEW-", marker);
        LichLamViec shift = scheduleRepository.save(LichLamViec.builder()
                .nhanVien(employee)
                .ngayLam(LocalDate.now().plusYears(5).plusDays(1))
                .caLam("Ca sáng")
                .gioBatDau(LocalTime.of(8, 0))
                .gioKetThuc(LocalTime.of(12, 0))
                .trangThai((byte) 0)
                .ngayTao(LocalDateTime.now())
                .build());
        String employeeToken = jwtUtil.generateToken(employee.getTenNguoiDung(), "Nhân viên", employee.getId());
        String adminToken = jwtUtil.generateToken(admin.getTenNguoiDung(), "Admin", admin.getId());
        String busyReason = "Có lịch khám bệnh đã đặt trước nên không thể tham gia ca";

        mockMvc.perform(post("/api/lich-lam-viec/" + shift.getId() + "/unavailable")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("lyDo", busyReason))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trangThai").value(3))
                .andExpect(jsonPath("$.lyDoBaoBan").value(busyReason))
                .andExpect(jsonPath("$.canAdminModify").value(false));

        mockMvc.perform(put("/api/lich-lam-viec/" + shift.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"ghiChu":"Không được phép sửa khi đang chờ duyệt"}
                                """))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/lich-lam-viec/" + shift.getId() + "/review-unavailable")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"approved":true,"phanHoi":"Đã đọc lý do và đồng ý cho nhân viên nghỉ"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trangThai").value(2))
                .andExpect(jsonPath("$.nguoiDuyet").value(admin.getTenNguoiDung()))
                .andExpect(jsonPath("$.canAdminModify").value(false));

        mockMvc.perform(delete("/api/lich-lam-viec/" + shift.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(authorities = "ROLE_Admin")
    void employeeCreationRejectsInvalidContactInformation() throws Exception {
        mockMvc.perform(post("/api/nhan-vien")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"hoVaTen":"Nguyen Van Test","tenNguoiDung":"employee_test",
                                 "email":"email-khong-hop-le","matKhau":"Matkhau123",
                                 "soDienThoai":"0912345678","ngaySinh":"2000-01-01"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email không đúng định dạng hoặc vượt quá 150 ký tự"));
    }

    private NhanVien createScheduleStaff(String roleName, String codePrefix, String marker) {
        VaiTro role = roleRepository.findByTenVaiTro(roleName)
                .orElseGet(() -> roleRepository.save(VaiTro.builder().tenVaiTro(roleName).build()));
        String normalizedPrefix = codePrefix.toLowerCase().replaceAll("[^a-z0-9]", "");
        return employeeRepository.save(NhanVien.builder()
                .vaiTro(role)
                .maNhanVien(codePrefix + marker)
                .hoVaTen(roleName + " kiểm thử lịch làm")
                .email(normalizedPrefix + marker + "@example.com")
                .tenNguoiDung(normalizedPrefix + marker)
                .matKhau(passwordEncoder.encode("Matkhau123"))
                .tinhTrangLamViec((byte) 1)
                .ngayTao(LocalDateTime.now())
                .build());
    }

    private CancelableOrderFixture createCancelableCodOrder(byte status, int stock, int voucherQuantity, int quantity) {
        String marker = String.valueOf(System.nanoTime());
        MauSac color = colorRepository.save(MauSac.builder()
                .tenMauSac("Cancel COD color " + marker)
                .trangThai((byte) 1)
                .build());
        KichThuoc size = sizeRepository.save(KichThuoc.builder()
                .tenKichThuoc("CANCEL-COD-" + marker)
                .trangThai((byte) 1)
                .build());
        SanPham product = vayRepository.save(SanPham.builder()
                .maSanPham("V-CANCEL-COD-" + marker)
                .tenSanPham("Cancelable COD dress")
                .trangThai((byte) 1)
                .ngayTao(LocalDateTime.now())
                .build());
        SanPhamChiTiet variant = variantRepository.save(SanPhamChiTiet.builder()
                .sanPham(product)
                .mauSac(color)
                .kichThuoc(size)
                .maSanPhamChiTiet("VC-CANCEL-COD-" + marker)
                .giaBanGoc(BigDecimal.valueOf(650000))
                .giaBan(BigDecimal.valueOf(650000))
                .giaNhap(BigDecimal.valueOf(400000))
                .soLuong(stock)
                .trangThai((byte) 1)
                .ngayTao(LocalDateTime.now())
                .build());
        GiamGia voucher = voucherRepository.save(GiamGia.builder()
                .maGiamGia("CANCELCOD" + marker)
                .tenGiamGia("Voucher hủy COD test")
                .gioTriGiam(BigDecimal.valueOf(30000))
                .soLuong(voucherQuantity)
                .trangThai((byte) 1)
                .build());
        HoaDon order = hoaDonRepository.save(HoaDon.builder()
                .maHoaDon("HD-CANCEL-COD-" + marker)
                .giamGia(voucher)
                .tongTien(BigDecimal.valueOf(650000L * quantity - 30000))
                .hinhThucNhanHang((byte) 1)
                .hinhThucThanhToan("COD")
                .trangThai(status)
                .daThanhToan(false)
                .daHoanTonKho(false)
                .ngayTao(LocalDateTime.now())
                .build());
        orderDetailRepository.save(HoaDonChiTiet.builder()
                .hoaDon(order)
                .sanPhamChiTiet(variant)
                .soLuong(quantity)
                .donGia(BigDecimal.valueOf(650000))
                .build());
        return new CancelableOrderFixture(order, variant, voucher);
    }

    private record CancelableOrderFixture(HoaDon order, SanPhamChiTiet variant, GiamGia voucher) {
    }

    private int performCheckout(SanPhamChiTiet variant, String suffix, String phone,
                                CountDownLatch ready, CountDownLatch start) throws Exception {
        ready.countDown();
        start.await();
        String body = """
                {"hoTen":"Guest %s","soDienThoai":"%s","email":"guest%s@example.com",
                 "diaChi":"1 Pho Hue, Ha Noi","tinhThanhCode":1,"quanHuyenCode":1,
                 "hinhThucThanhToan":"COD","checkoutRequestId":"CHECKOUT-CONCURRENT-%s",
                 "items":[{"productId":%d,"variantId":%d,"qty":1}]}
                """.formatted(suffix, phone, suffix.toLowerCase(), suffix,
                variant.getSanPham().getId(), variant.getId());
        return mockMvc.perform(post("/api/payment/create-order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andReturn()
                .getResponse()
                .getStatus();
    }

    private String reservationToken(String responseBody) throws Exception {
        JsonNode response = objectMapper.readTree(responseBody);
        return response.path("token").asText();
    }
}
