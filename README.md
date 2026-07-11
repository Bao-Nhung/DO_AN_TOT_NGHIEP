Dữ liệu test chính nằm ở file [database/fashion_shop.sql](<C:/Users/Admin/Documents/GitHub/DO_AN_TOT_NGHIEP/database/fashion_shop.sql>). Nếu muốn test trên dữ liệu sạch, import file này vào SQL Server trước rồi chạy backend.

**Tài Khoản Test**

| Vai trò | Tài khoản | Mật khẩu | Ghi chú |
|---|---|---:|---|
| Admin | `admin` hoặc `admin@zestia.vn` | `123456` | Vào được toàn bộ `/admin` |
| Nhân viên POS | `nv_pos` hoặc `nv.pos@zestia.vn` | `123456` | Quyền ít hơn admin, nên dùng để test bán tại quầy |
| Nhân viên cũ | `tuannv` hoặc `tuan@zestia.vn` | `123456` | Cũng có role nhân viên |
| Khách hàng | `khach.demo@zestia.vn` hoặc `0911111111` | `123456` | Có địa chỉ mặc định để test checkout |
| SQL Server local | user `sa` | `123456` | Theo `application.properties` và README |

Tôi đã kiểm tra hash BCrypt trong SQL, password test admin/nhân viên khớp `123456`.

**Voucher Test**

| Mã | Điều kiện |
|---|---|
| `ZESTIA10` | Giảm 10% cho đơn từ 500K, tối đa 100K |
| `ZESTIA50` | Giảm 50K cho đơn từ 800K |
| `FREESHIP` | Hỗ trợ 30K phí vận chuyển cho đơn từ 300K |
| `SUMMER20` | Có trong dữ liệu gốc, giảm 20% cho đơn từ 1 triệu |

**Đơn Hàng Có Sẵn Để Tra Cứu**

Dùng trang tra cứu đơn:

| Mã đơn | SĐT |
|---|---|
| `HD-2025-0001` | `0912345678` |
| `HD-2025-0002` | `0912345678` |
| `HD-2025-0003` | `0923456789` |

Đơn guest mới thì sau khi đặt xong lấy `maHoaDon` + SĐT vừa nhập để tra cứu.

**Tồn Kho Trừ Lúc Nào**

Tồn kho nằm ở bảng `Vay_chi_tiet.so_luong`, xem được trong admin phần sản phẩm/biến thể. Ví dụ dữ liệu seed có `VAY001-TRANG-S` tồn `15`, `VAY001-TRANG-M` tồn `20`.

Luồng tồn kho hiện tại:

| Luồng | Khi nào trừ kho? |
|---|---|
| Thêm vào giỏ | Chưa trừ |
| Checkout COD online | Trừ ngay khi bấm đặt hàng, API `/api/payment/create-order` chạy |
| Checkout MoMo/ZaloPay | Trừ ngay khi tạo đơn trước khi chuyển sang cổng thanh toán |
| Thanh toán online thành công | Không trừ thêm lần nữa |
| Thanh toán online thất bại | Hoàn kho |
| Bỏ thanh toán quá hạn | Sau khoảng 30 phút, job tự chuyển `Thanh toán thất bại` và hoàn kho |
| POS bán tại quầy | Trừ khi nhân viên bấm thanh toán/tạo đơn POS, không trừ lúc chỉ chọn sản phẩm |
| Hủy đơn đang chờ | Hoàn kho |
| Admin đổi trạng thái sang hủy/giao thất bại/thanh toán thất bại/hoàn tiền | Hoàn kho nếu trước đó chưa hoàn |

**Checklist Test Tay Nên Chạy**

1. Login admin `admin / 123456`, vào sản phẩm xem tồn kho một biến thể.
2. Login khách `khach.demo@zestia.vn / 123456`, đặt COD một sản phẩm, kiểm tra tồn kho giảm.
3. Hủy đơn khi đang chờ xử lý, kiểm tra tồn kho tăng lại.
4. Login nhân viên `nv_pos / 123456`, bán tại quầy, chọn màu/size, thanh toán tiền mặt, kiểm tra đơn offline và tồn kho giảm.
5. Test voucher `ZESTIA10`, `ZESTIA50`, `FREESHIP`.
6. Test guest checkout không đăng nhập, sau đó tra cứu bằng mã đơn + SĐT.
7. Test quên mật khẩu bằng email có thật nếu bạn đã cấu hình `MAIL_USERNAME`/`MAIL_PASSWORD`.
```
DO_AN_TOT_NGHIEP
├─ backend
│  ├─ .mvn
│  │  └─ wrapper
│  │     └─ maven-wrapper.properties
│  ├─ mvnw
│  ├─ mvnw.cmd
│  ├─ pom.xml
│  └─ src
│     ├─ main
│     │  ├─ java
│     │  │  └─ com
│     │  │     └─ zestia
│     │  │        └─ datn
│     │  │           └─ zestia
│     │  │              ├─ BackendApplication.java
│     │  │              ├─ config
│     │  │              │  ├─ DataInitializer.java
│     │  │              │  ├─ JwtAuthFilter.java
│     │  │              │  ├─ JwtUtil.java
│     │  │              │  ├─ ApiExceptionHandler.java
│     │  │              │  └─ SecurityConfig.java
│     │  │              ├─ controller
│     │  │              │  ├─ AuthController.java
│     │  │              │  ├─ DashboardController.java
│     │  │              │  ├─ GatewayPaymentController.java
│     │  │              │  ├─ HoaDonController.java
│     │  │              │  ├─ KhachHangController.java
│     │  │              │  ├─ VoucherController.java
│     │  │              │  ├─ PaymentController.java
│     │  │              │  ├─ ThongKeController.java
│     │  │              │  ├─ ThuocTinhController.java
│     │  │              │  └─ VayController.java
│     │  │              ├─ dto
│     │  │              │  ├─ LoginRequest.java
│     │  │              │  ├─ LoginResponse.java
│     │  │              │  ├─ response
│     │  │              │  │  ├─ ThongKeDoanhThuDTO.java
│     │  │              │  │  ├─ ThongKeDoanhThuThoiGianDTO.java
│     │  │              │  │  ├─ ThongKeKhachHangDTO.java
│     │  │              │  │  ├─ ThongKeSoLuongDTO.java
│     │  │              │  │  ├─ ThongKeTongQuanDTO.java
│     │  │              │  │  └─ ThongKeTrangThaiDTO.java
│     │  │              │  └─ ThongKeResponse.java
│     │  │              ├─ entity
│     │  │              │  ├─ Anh.java
│     │  │              │  ├─ BaseEntity.java
│     │  │              │  ├─ ChatLieu.java
│     │  │              │  ├─ DanhGia.java
│     │  │              │  ├─ DiaChi.java
│     │  │              │  ├─ GiamGia.java
│     │  │              │  ├─ GioHang.java
│     │  │              │  ├─ GioHangChiTiet.java
│     │  │              │  ├─ HoaDon.java
│     │  │              │  ├─ HoaDonChiTiet.java
│     │  │              │  ├─ KhachHang.java
│     │  │              │  ├─ NewsletterSubscriber.java
│     │  │              │  ├─ KichThuoc.java
│     │  │              │  ├─ LichSuThanhToan.java
│     │  │              │  ├─ LichSuXem.java
│     │  │              │  ├─ LoaiVay.java
│     │  │              │  ├─ MauSac.java
│     │  │              │  ├─ NhaCungCap.java
│     │  │              │  ├─ NhanVien.java
│     │  │              │  ├─ NhatKy.java
│     │  │              │  ├─ SanPhamYeuThich.java
│     │  │              │  ├─ TaiTro.java
│     │  │              │  ├─ ThongBao.java
│     │  │              │  ├─ VaiTro.java
│     │  │              │  ├─ Vay.java
│     │  │              │  └─ VayChiTiet.java
│     │  │              └─ repository
│     │  │                 ├─ AnhRepository.java
│     │  │                 ├─ ChatLieuRepository.java
│     │  │                 ├─ GiamGiaRepository.java
│     │  │                 ├─ GioHangChiTietRepository.java
│     │  │                 ├─ GioHangRepository.java
│     │  │                 ├─ HoaDonChiTietRepository.java
│     │  │                 ├─ HoaDonRepository.java
│     │  │                 ├─ KhachHangRepository.java
│     │  │                 ├─ NewsletterSubscriberRepository.java
│     │  │                 ├─ KichThuocRepository.java
│     │  │                 ├─ LichSuThanhToanRepository.java
│     │  │                 ├─ LoaiVayRepository.java
│     │  │                 ├─ MauSacRepository.java
│     │  │                 ├─ NhaCungCapRepository.java
│     │  │                 ├─ NhanVienRepository.java
│     │  │                 ├─ ThongKeRepository.java
│     │  │                 ├─ VaiTroRepository.java
│     │  │                 ├─ VayChiTietRepository.java
│     │  │                 └─ VayRepository.java
│     │  └─ resources
│     │     └─ application.properties
│     └─ test
│        ├─ java
│        │  └─ com
│        │     └─ zestia
│        │        └─ datn
│        │           └─ backend
│        │              └─ BackendApplicationTests.java
│        └─ resources
│           └─ application.properties
├─ database
│  ├─ fashion_shop.sql
│  └─ README.md
├─ frontend
│  ├─ index.html
│  ├─ package-lock.json
│  ├─ package.json
│  ├─ public
│  │  └─ images
│  │     ├─ banners
│  │     │  ├─ banner1.png
│  │     │  ├─ banner2.jpg
│  │     │  ├─ banner3.jpg
│  │     │  ├─ banner4.jpg
│  │     │  ├─ banner5.png
│  │     │  ├─ banner6.png
│  │     │  ├─ banner7.png
│  │     │  ├─ banner8.png
│  │     │  └─ banner9.png
│  │     └─ products
│  │        ├─ accessories1.jpg
│  │        ├─ accessories10.jpg
│  │        ├─ accessories11.jpg
│  │        ├─ accessories12.jpg
│  │        ├─ accessories13.jpg
│  │        ├─ accessories14.jpg
│  │        ├─ accessories15.jpg
│  │        ├─ accessories16.jpg
│  │        ├─ accessories17.jpg
│  │        ├─ accessories18.jpg
│  │        ├─ accessories19.jpg
│  │        ├─ accessories2.jpg
│  │        ├─ accessories20.jpg
│  │        ├─ accessories3.jpg
│  │        ├─ accessories4.jpg
│  │        ├─ accessories5.jpg
│  │        ├─ accessories6.jpg
│  │        ├─ accessories7.jpg
│  │        ├─ accessories8.jpg
│  │        ├─ accessories9.jpg
│  │        ├─ dress1.jpg
│  │        ├─ dress10.jpg
│  │        ├─ dress11.jpg
│  │        ├─ dress12.jpg
│  │        ├─ dress13.jpg
│  │        ├─ dress14.jpg
│  │        ├─ dress15.jpg
│  │        ├─ dress16.jpg
│  │        ├─ dress17.jpg
│  │        ├─ dress18.jpg
│  │        ├─ dress19.jpg
│  │        ├─ dress2.jpg
│  │        ├─ dress20.jpg
│  │        ├─ dress3.jpg
│  │        ├─ dress4.jpg
│  │        ├─ dress5.jpg
│  │        ├─ dress6.jpg
│  │        ├─ dress7.jpg
│  │        ├─ dress8.jpg
│  │        ├─ dress9.jpg
│  │        ├─ pants1.jpg
│  │        ├─ pants10.jpg
│  │        ├─ pants11.jpg
│  │        ├─ pants12.jpg
│  │        ├─ pants13.jpg
│  │        ├─ pants14.jpg
│  │        ├─ pants15.jpg
│  │        ├─ pants16.jpg
│  │        ├─ pants17.jpg
│  │        ├─ pants18.jpg
│  │        ├─ pants19.jpg
│  │        ├─ pants2.jpg
│  │        ├─ pants20.jpg
│  │        ├─ pants3.jpg
│  │        ├─ pants4.jpg
│  │        ├─ pants5.jpg
│  │        ├─ pants6.jpg
│  │        ├─ pants7.jpg
│  │        ├─ pants8.jpg
│  │        ├─ pants9.jpg
│  │        ├─ shirt1.jpg
│  │        ├─ shirt10.jpg
│  │        ├─ shirt11.jpg
│  │        ├─ shirt12.jpg
│  │        ├─ shirt13.jpg
│  │        ├─ shirt14.jpg
│  │        ├─ shirt15.jpg
│  │        ├─ shirt17.jpg
│  │        ├─ shirt18.jpg
│  │        ├─ shirt19.jpg
│  │        ├─ shirt2.jpg
│  │        ├─ shirt20.jpg
│  │        ├─ shirt3.jpg
│  │        ├─ shirt4.jpg
│  │        ├─ shirt5.jpg
│  │        ├─ shirt6.jpg
│  │        ├─ shirt7.jpg
│  │        ├─ shirt8.jpg
│  │        └─ shirt9.jpg
│  ├─ README.md
│  ├─ src
│  │  ├─ App.vue
│  │  ├─ assets
│  │  │  └─ main.css
│  │  ├─ components
│  │  │  ├─ charts
│  │  │  │  ├─ BarChartUI.vue
│  │  │  │  ├─ LineChartUI.vue
│  │  │  │  └─ PieChartUI.vue
│  │  │  ├─ layout
│  │  │  │  ├─ AdminLayout.vue
│  │  │  │  ├─ AppFooter.vue
│  │  │  │  ├─ AppNavbar.vue
│  │  │  │  ├─ CartDrawer.vue
│  │  │  │  └─ ToastNotification.vue
│  │  │  └─ ui
│  │  │     ├─ CollectionCard.vue
│  │  │     ├─ MarqueeStrip.vue
│  │  │     └─ ProductCard.vue
│  │  ├─ composables
│  │  │  ├─ useApi.js
│  │  │  ├─ useCart.js
│  │  │  ├─ useProducts.js
│  │  │  ├─ useReveal.js
│  │  │  ├─ useToast.js
│  │  │  └─ useWishlist.js
│  │  ├─ main.js
│  │  └─ pages
│  │     ├─ AboutPage.vue
│  │     ├─ admin
│  │     │  ├─ AdminCustomers.vue
│  │     │  ├─ AdminDashboard.vue
│  │     │  ├─ AdminOrders.vue
│  │     │  ├─ AdminPOS.vue
│  │     │  ├─ AdminProducts.vue
│  │     │  ├─ AdminSettings.vue
│  │     │  ├─ AdminStatisticalDashboard.vue
│  │     │  └─ AdminVouchers.vue
│  │     ├─ CheckoutPage.vue
│  │     ├─ HomePage.vue
│  │     ├─ LoginPage.vue
│  │     ├─ PaymentResultPage.vue
│  │     ├─ ProductDetail.vue
│  │     ├─ ProductsPage.vue
│  │     ├─ ProfilePage.vue
│  │     ├─ QRPaymentPage.vue
│  │     └─ WishlistPage.vue
│  └─ vite.config.js
└─ HUONG_DAN_CHAY_DU_AN.md

```
