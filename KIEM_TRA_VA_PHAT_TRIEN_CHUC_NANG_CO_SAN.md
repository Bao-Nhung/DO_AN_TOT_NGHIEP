# RÀ SOÁT VÀ HOÀN THIỆN CÁC CHỨC NĂNG HIỆN CÓ CHỐNG LỖI VẶT KHI BẢO VỆ

> **Mục tiêu**: Kiểm tra toàn bộ các chức năng đã làm, xử lý hết các lỗi vặt (Edge cases) và trang bị các phương án dự phòng (Fallback) để buổi Demo bảo vệ trước Hội đồng diễn ra **100% mượt mà, không gặp bất kỳ sự cố kỹ thuật nào**.

---

## 🛡️ 1. TOP 5 RỦI RO "LỖI VẶT" CẦN PHÒNG NGỪA KHI DEMO

### 🛑 Rủi ro 1: Cổng thanh toán MoMo / ZaloPay bị chậm hoặc mất mạng tại phòng bảo vệ
- **Vấn đề**: Wi-Fi phòng bảo vệ chập chờn hoặc MoMo API Sandbox bị timeout khiến việc demo thanh toán online bị nghẽn.
- **Giải pháp xử lý (Đã có sẵn trong backend)**:
  - Backend có API `/api/payment/mock-success/{maHoaDon}` cho phép thanh toán thành công tức thì phục vụ Demo offline nếu mạng chập chờn.
  - *Mẹo demo*: Luôn chuẩn bị sẵn tab Postman hoặc cờ Sandbox trên trang Checkout để nếu MoMo bị chậm thì nổ đơn tức thì.

### 🛑 Rủi ro 2: Nhập số tiền khách đưa nhỏ hơn tổng tiền đơn hàng tại quầy POS
- **Vấn đề**: Nhân viên POS nhập `tienKhachDua < tongTien` nhưng hệ thống vẫn cho tạo đơn.
- **Giải pháp xử lý**:
  - Trong `AdminPOS.vue`, bắt buộc validate: Nếu `tienKhachDua < tongTien` thì disable nút **"Thanh toán"** và hiển thị cảnh báo đỏ *"Số tiền khách đưa chưa đủ!"*. Tự động tính `tienThua = tienKhachDua - tongTien`.

### 🛑 Rủi ro 3: Bấm chọn biến thể đã hết hàng (`soLuong == 0`)
- **Vấn đề**: Khách chọn Size/Màu đã hết tồn kho rồi bấm Đặt hàng mới nhận được báo lỗi 400.
- **Giải pháp xử lý**:
  - Tại trang Chi tiết sản phẩm (`ProductDetail.vue`) và Màn hình POS (`AdminPOS.vue`): Biến thể nào có `soLuong == 0` sẽ bị mờ đi (disabled) kèm gạch ngang hoặc chữ *"Hết hàng"*, ngăn khách chọn mua.

### 🛑 Rủi ro 4: Upload ảnh đổi trả vượt dung lượng / sai định dạng
- **Vấn đề**: Khách chọn file PDF/ZIP hoặc ảnh 20MB khi tạo yêu cầu Đổi/Trả hàng làm nghẽn server.
- **Giải pháp xử lý**:
  - Trong `ReturnExchangeService.java` và `ProfilePage.vue`: Đã có validate tối đa 5 ảnh, dung lượng <= 5MB/ảnh và chỉ nhận định dạng hình ảnh (PNG, JPG, JPEG, WEBP).

### 🛑 Rủi ro 5: Mã giảm giá (Voucher) hết lượt dùng hoặc chưa đạt đơn tối thiểu
- **Vấn đề**: Nhập voucher nhưng không thấy giảm giá hoặc thông báo lỗi không rõ ràng.
- **Giải pháp xử lý**:
  - `PaymentController.java` đã có hàm `validateVoucher` trả về rõ lý do: *"Đơn hàng chưa đạt giá trị tối thiểu 500.000đ"* hoặc *"Mã giảm giá đã hết lượt sử dụng"*.

---

## 🛠️ 2. CÁC ĐIỂM CẦN HOÀN THIỆN NHỎ ĐỂ CHẮC CHẮN ĐẠT ĐIỂM 10

Để các chức năng hiện có trông **chuyên nghiệp như một phần mềm thương mại thực thụ**, bạn chỉ cần hoàn thiện thêm 3 chi tiết nhỏ sau:

### 1. In Hóa Đơn Bán Hàng Tại Quầy (POS Receipt Print)
- **Tình trạng hiện tại**: Đơn POS thanh toán xong thành công nhưng chưa có nút In bill.
- **Hoàn thiện**: Thêm Modal xem trước Hóa đơn K80 có Logo Zestia, Mã đơn, Danh sách món, Số tiền, Mã QR chuyển khoản và nút **"🖨️ In Hóa Đơn"** (`window.print()`).

### 2. Trạng Thái Đơn Hàng Visual Timeline
- **Tình trạng hiện tại**: Xem đơn hàng hiển thị dạng text trạng thái.
- **Hoàn thiện**: Tại trang Tra cứu đơn (`OrderTrackingPage.vue`) & Đơn hàng của tôi (`MyOrdersPage.vue`), hiển thị một **Thanh tiến trình 5 bước (Stepper Timeline)**:
  `Chờ xử lý ➔ Đã xác nhận ➔ Đang chuẩn bị ➔ Đang giao ➔ Thành công` giúp giao diện cực kỳ trực quan và đẹp mắt.

### 3. Thông Báo Toast Feedback Khi Thao Tác
- **Tình trạng hiện tại**: Đã có `ToastNotification.vue` và `useToast.js`.
- **Hoàn thiện**: Đảm bảo mọi thao tác quan trọng (Thêm vào giỏ, Đổi ngôn ngữ, Lưu địa chỉ, Hủy đơn, Áp voucher) đều bật Toast thông báo màu xanh (Success) góc phải màn hình.

---

## 🎯 3. CHECKLIST KIỂM TRA TRƯỚC KHI LÊN SÀN BẢO VỆ

| Hạng mục kiểm tra | Trạng thái | Ghi chú demo |
| :--- | :---: | :--- |
| **1. Đăng nhập / Đăng xuất** | ✅ Đạt | Test tài khoản `admin`, `nv_pos`, `khach.demo@zestia.vn` (Pass: `123456`). |
| **2. Đa ngôn ngữ (VI / EN)** | ✅ Đạt | Đã tích hợp nút switch `VI 🇻🇳 / EN 🇬🇧` trên Navbar và Admin Sidebar. |
| **3. Trợ lý AI Chatbot** | ✅ Đạt | Mở cửa sổ chat AI ở góc dưới phải, hỏi: *"Tư vấn cho mình mẫu váy đi tiệc"* hoặc *"Tra cứu đơn HD-2025-0001"*. |
| **4. Đặt hàng Online COD & MoMo** | ✅ Đạt | Test nhập voucher `ZESTIA10`, kiểm tra tồn kho giảm sau khi nổ đơn. |
| **5. Bán hàng tại quầy POS** | ✅ Đạt | Test chọn biến thể, gõ SĐT khách, chọn thanh toán tiền mặt. |
| **6. Đổi / Trả hàng** | ✅ Đạt | Khách vào đơn đã giao thành công -> Tạo yêu cầu Đổi size -> Admin duyệt và kiểm tra kho tự động bù trừ. |
| **7. Quản lý Ca làm việc POS** | ✅ Đạt | Mở ca -> Thu tiền -> Chốt ca -> Kiểm tra tiền mặt cần bàn giao & Tiền chênh lệch. |

---

*Tài liệu này giúp bạn tự tin 100% kiểm soát hệ thống, loại bỏ toàn bộ rủi ro kỹ thuật để có một buổi bảo vệ hoàn hảo!*
