
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
│     │  │              │  ├─ SecurityConfig.java
│     │  │              │  └─ VNPayConfig.java
│     │  │              ├─ controller
│     │  │              │  ├─ AuthController.java
│     │  │              │  ├─ DashboardController.java
│     │  │              │  ├─ GatewayPaymentController.java
│     │  │              │  ├─ HoaDonController.java
│     │  │              │  ├─ KhachHangController.java
│     │  │              │  ├─ KhuyenMaiController.java
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
│     │  │              │  ├─ KhuyenMai.java
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
│     │  │                 ├─ KhuyenMaiRepository.java
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
│  ├─ add_products.sql
│  ├─ export-db.ps1
│  ├─ fashion_shop.sql
│  ├─ fashion_shop_full.sql
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