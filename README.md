# Zestia Fashion

Đồ án tốt nghiệp xây dựng hệ thống bán thời trang đa kênh, gồm website khách hàng,
quản trị cửa hàng và POS cho nhân viên.

## Công nghệ

- Backend: Java 21, Spring Boot 3, Spring Security, JPA/Hibernate, SQL Server.
- Frontend: Vue 3, Vue Router, Vue I18n, Vite 8, Bootstrap 5, Chart.js.
- Tích hợp: MoMo/ZaloPay sandbox, Google Sign-In, Gmail SMTP, OpenAI API, VietQR.
- Database: một file cài đặt idempotent tại `database/fashion_shop.sql`.

## Chức năng chính

- Khách hàng: duyệt sản phẩm và biến thể, giỏ hàng, yêu thích, sản phẩm đã xem,
  checkout COD/MoMo/ZaloPay, voucher tốt nhất, nhiều địa chỉ, tra cứu đơn, đánh giá
  đã xác minh, so sánh sản phẩm, đổi/trả, thông báo, ChatAI, chuyển tiếp cho nhân viên
  và vòng quay quà hiện vật theo đơn đủ điều kiện.
- Nhân viên: xác nhận ca, báo bận, check-in/check-out, POS, xử lý đơn, đổi/trả,
  chat hỗ trợ, trung tâm việc cần xử lý, lịch sử và xuất Excel ca làm.
- Admin: dashboard, thống kê, sản phẩm, đơn hàng online/offline, khách hàng hợp nhất,
  nhân viên, lịch làm việc, voucher, đợt khuyến mãi, thông báo, quản lý chiến dịch vòng
  quay/kho quà/lịch sử trao thưởng và trung tâm việc cần xử lý.
- Ngôn ngữ: Việt/Anh trên cả website khách và khu vực quản trị; locale được gửi qua
  `Accept-Language` để dịch validation, trạng thái, danh mục và dữ liệu API. Email giao
  dịch dùng nội dung song ngữ.

## Ngôn ngữ và kênh bán ngoài

Sau khi thêm hoặc sửa nội dung tiếng Việt, cập nhật từ điển tự động bằng:

```powershell
cd frontend
npm run i18n:generate
```

Lệnh này quét frontend, thông báo/validation backend và dữ liệu SQL, sau đó cập nhật
từ điển dùng chung cho frontend và backend. Footer hiện liên kết tới Facebook Shop,
TikTok Shop, Shopee và Lazada. Chưa khai báo đồng bộ API giả: để đồng bộ sản phẩm,
tồn kho và đơn hàng thật cần tài khoản người bán, mã cửa hàng và credential/webhook
do từng sàn cấp.

## Khởi chạy nhanh

### 1. Database

Mở `database/fashion_shop.sql` bằng SSMS và chạy toàn bộ file, hoặc:

```powershell
sqlcmd -S localhost,1433 -U sa -P 123456 -C -f 65001 -i database/fashion_shop.sql
```

File SQL dùng UTF-8 và tự tạo database `fashion_shop`; có thể chạy lại nhiều lần. Nó kiểm
tra các bất biến trước khi báo thành công và tạo dữ liệu tháng 8/2026 phủ đủ 31 ngày,
bao gồm đơn hàng, thanh toán, ca làm, đánh giá, hỗ trợ và đổi/trả. Hệ thống chỉ có hai
role `Admin`, `Nhân viên`.

### 2. Backend

```powershell
cd backend
$env:MAIL_USERNAME="your_email@gmail.com"
$env:MAIL_PASSWORD="your_gmail_app_password"
$env:OPENAI_API_KEY="your_openai_api_key"
.\mvnw.cmd spring-boot:run
```

`MAIL_*` cần cho OTP, hóa đơn và email trạng thái đơn. `OPENAI_API_KEY` cần cho ChatAI.
Backend chạy tại `http://localhost:8080`.

### 3. Frontend

```powershell
cd frontend
npm install
npm run dev
```

Frontend chạy tại `http://localhost:5173`. Dự án dùng history routing nên URL không có `/#/`.

Hướng dẫn cài đặt chi tiết nằm trong `HUONG_DAN_CHAY_DU_AN.md`.

## Tài khoản demo

| Vai trò | Tài khoản | Mật khẩu | Ghi chú |
|---|---|---:|---|
| Admin | `admin` hoặc `admin@zestia.vn` | `123456` | Toàn quyền quản trị |
| Nhân viên POS | `nv_pos` hoặc `nv.pos@zestia.vn` | `123456` | Cần đúng ca và đã check-in |
| Nhân viên | `tuannv` hoặc `tuan@zestia.vn` | `123456` | Quyền nhân viên |
| Khách hàng | `khach.demo@zestia.vn` hoặc `0911111111` | `123456` | Có địa chỉ để test checkout |

## Dữ liệu test

Voucher thường dùng:

| Mã | Điều kiện |
|---|---|
| `ZESTIA10` | Giảm 10% cho đơn từ 500.000đ, tối đa 100.000đ |
| `ZESTIA50` | Giảm 50.000đ cho đơn từ 800.000đ |
| `FREESHIP` | Hỗ trợ 30.000đ cho đơn từ 300.000đ |
| `SUMMER20` | Giảm 20% cho đơn từ 1.000.000đ |

Đơn có sẵn để tra cứu:

| Mã đơn | Số điện thoại |
|---|---|
| `HD-2025-0001` | `0912345678` |
| `HD-2025-0002` | `0912345678` |
| `HD-2025-0003` | `0923456789` |

## Vòng quay may mắn

Chiến dịch mẫu tháng 8 cho phép mỗi đơn đã giao thành công từ 1.000.000đ quay đúng
một lần. Người chơi xác minh bằng mã đơn và số điện thoại; vòng quay mẫu có tám lựa
chọn gồm bảy quà hiện vật và một kết quả không trúng. Admin quản lý chiến dịch, trọng
số, số lượng quà, lịch sử lượt quay và xác nhận đã trao quà tại `/admin/lucky-wheel`.
Mỗi phần thưởng có thể dùng icon Bootstrap, nhập mã icon khác hoặc tải ảnh biểu tượng
JPG/PNG/WebP/AVIF riêng tối đa 2 MB.

Ba bảng `Vong_quay_may_man`, `Phan_thuong_vong_quay` và `Luot_quay_may_man` là miền
dữ liệu riêng. Hệ thống chỉ đọc hóa đơn để xét điều kiện, lưu bản chụp thông tin người
nhận trong lượt quay và không sửa đơn hàng, voucher hay tồn kho sản phẩm. Khóa bi quan
trên hóa đơn và kho quà, cùng ràng buộc duy nhất theo chiến dịch + mã đơn, ngăn quay
lặp và ngăn hai giao dịch cùng lấy phần quà cuối. Ảnh biểu tượng tùy chỉnh chỉ được lưu
trong `frontend/public/images/lucky-wheel`; backend kiểm tra magic bytes và không nhận
URL ngoài thư mục do ứng dụng quản lý.

## Luồng tồn kho

| Luồng | Thời điểm cập nhật |
|---|---|
| Thêm vào giỏ | Chưa trừ |
| COD online | Trừ khi API tạo đơn thành công |
| MoMo/ZaloPay | Giữ/trừ khi tạo đơn chờ thanh toán |
| Thanh toán thành công | Không trừ lần thứ hai |
| Thanh toán thất bại hoặc quá hạn | Hoàn kho và voucher đúng một lần |
| POS thêm sản phẩm | Trừ ngay và giữ trong phiên POS 15 phút |
| POS áp voucher | Trừ lượt dùng ngay và giữ trong cùng phiên |
| POS thanh toán thành công | Gắn phiên giữ vào hóa đơn, không trừ lần thứ hai |
| POS xóa giỏ hoặc hết hạn | Hoàn kho và voucher đúng một lần |
| Hủy, giao thất bại, hoàn tiền hợp lệ | Hoàn kho nếu chưa hoàn trước đó |

Các thay đổi tồn kho được ghi tại bảng `Bien_dong_ton_kho`.

## Kiểm tra dự án

```powershell
cd backend
.\mvnw.cmd test

cd ..\frontend
npm install
npm run build
npm audit
```

Đánh giá theo rubric và phần còn thiếu được ghi tại `DANH_GIA_TIEU_CHI_DO_AN.md`.
