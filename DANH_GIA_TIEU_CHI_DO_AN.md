# ĐÁNH GIÁ DỰ ÁN ZESTIA THEO TIÊU CHÍ ĐỒ ÁN BÁN HÀNG

Ngày rà soát: 01/08/2026

Tài liệu này đối chiếu trực tiếp mã nguồn, test tự động và database local hiện tại với bảng tiêu chí của nhà trường. Đây là đánh giá kỹ thuật có bằng chứng, không phải cam kết điểm số.

## 1. Kết luận ngắn

Zestia đáp ứng chắc nhóm chức năng cơ bản và phần lớn nhóm Khá (7-8 điểm). Dự án cũng đã có nhiều điểm thuộc nhóm Giỏi như AI, bản đồ, hỗ trợ trực tuyến, dữ liệu đơn hàng theo thời gian và trải nghiệm Vue 3 tương đối hoàn chỉnh.

Tiêu chí chưa nên tuyên bố là đã hoàn thành là tích hợp kênh bán hàng bên ngoài. Dự án
đã đặt liên kết chính thức tới Facebook Shop, TikTok Shop, Shopee và Lazada nhưng chưa
đồng bộ sản phẩm, tồn kho hay đơn hàng vì chưa có tài khoản người bán và API credential.

Kiến trúc i18n, so sánh sản phẩm và dữ liệu đầy đủ đến hết tháng 8/2026 đã có. Nội dung
dịch tự động vẫn cần một lượt hiệu đính ngôn ngữ thủ công trước khi trình diễn tiếng Anh.

## 2. Đối chiếu tiêu chí

| Nhóm tiêu chí | Trạng thái | Bằng chứng trong dự án | Nhận xét |
|---|---|---|---|
| Giao diện dùng framework frontend | Đạt | Vue 3, Vue Router, Bootstrap 5 trong `frontend/package.json` | Có lazy loading theo route, responsive cho trang khách và admin |
| Giỏ hàng, tìm kiếm, yêu thích | Đạt | `useCart.js`, `AppNavbar.vue`, `WishlistPage.vue` | Giỏ hàng và yêu thích còn được đồng bộ theo tài khoản qua `/api/customer-data/**` |
| Thanh toán và tạo đơn | Đạt | `CheckoutPage.vue`, `AdminPOS.vue`, `PaymentController.java` | Có COD, MoMo, ZaloPay và bán tại quầy |
| Quản lý trạng thái đơn | Đạt | `OrderStatusService.java`, `AdminOrders.vue`, `OrderTrackingCard.vue` | Có state machine; không cho bỏ qua bước hoặc xử lý đơn online chưa thanh toán |
| Hủy đơn có kiểm soát | Đạt | API hủy theo tài khoản và OTP email cho khách vãng lai | Test kiểm tra tra cứu bằng SĐT và OTP hủy đơn |
| Quản lý sản phẩm và biến thể | Đạt | `AdminProducts.vue`, `VayController.java` | Màu, size, tồn kho, ảnh biến thể, khóa/mở sản phẩm |
| Voucher | Đạt | `AdminVouchers.vue`, `VoucherController.java` | Có điều kiện đơn tối thiểu, số lượt, thời hạn và tự chọn voucher tốt nhất |
| Nhiều đợt/đối tượng khuyến mãi | Đạt | `AdminPromotions.vue`, `PromotionPricingService.java` | Phạm vi theo toàn bộ, sản phẩm, loại, màu hoặc size |
| Validate số lượng và chống bán vượt kho | Đạt | Khóa bi quan tại repository và xử lý transaction | Có test hai checkout đồng thời tranh sản phẩm cuối |
| Đánh giá sản phẩm | Đạt | `ReviewsPage.vue`, `ReviewService.java` | Điểm và nội dung lấy từ DB, ràng buộc người mua và đơn đã giao |
| So sánh nhiều sản phẩm | Đạt | `CompareProductsPage.vue`, dữ liệu sản phẩm/biến thể/đánh giá từ API | So sánh giá, chất liệu, phom, size và điểm đánh giá |
| AI trên website | Đạt | `AiChatService.java`, `CustomerChatWidget.vue` | AI lấy ngữ cảnh sản phẩm, voucher và đơn hàng từ backend; model cấu hình bằng biến môi trường |
| Hỗ trợ khách hàng trực tuyến | Đạt | `SupportChatService.java`, `AdminSupportChat.vue` | AI có thể chuyển sang nhân viên đang trong ca |
| Trung tâm công việc nhân viên | Đạt | `StaffTaskService.java`, `StaffDashboardService.java`, `AdminLayout.vue` | Tổng hợp đơn chờ, chat, đổi/trả và cảnh báo tồn kho; mỗi mục dẫn tới đúng màn hình xử lý |
| Bản đồ cửa hàng | Đạt | `AboutPage.vue` | Có bản đồ nhúng và liên kết chỉ đường |
| Đa ngôn ngữ | Đạt về kiến trúc và độ phủ | Vue I18n, từ điển frontend/backend, `Accept-Language`, email song ngữ | Nên hiệu đính bản dịch tự động trước khi bảo vệ |
| Tích hợp kênh bán hàng khác | Chưa có | Chỉ có liên kết mạng xã hội | Không có đồng bộ sản phẩm, đơn hoặc tồn kho với nền tảng ngoài |
| Xử lý dữ liệu ít nhất một tháng | Đạt ở dữ liệu demo | 376 đơn từ 01/06/2026 đến 31/08/2026 | Cần nói rõ đây là dữ liệu demo, không phải số liệu vận hành thật |
| Trải nghiệm người dùng | Khá | Modal giữa màn hình, toast, trạng thái tải/lỗi/rỗng, responsive | Nên có kiểm thử người dùng và audit accessibility độc lập trước khi tuyên bố mức “tuyệt vời” |

## 3. Các điểm kỹ thuật nổi bật có thể trình bày

1. Tồn kho được cập nhật theo transaction và khóa biến thể khi checkout; test chứng minh không thể bán vượt sản phẩm cuối.
2. Kết quả cổng thanh toán được kiểm tra số tiền và xử lý idempotent; callback lặp không trừ kho hoặc voucher lần hai.
3. Đơn MoMo/ZaloPay chưa thanh toán không được đi tiếp trong luồng xử lý; đơn thất bại hoàn kho và voucher đúng một lần.
4. POS liên kết khách online/offline bằng SĐT hoặc email, bắt buộc thông tin khách và kiểm tra tiền mặt ở cả frontend lẫn backend.
5. Nhân viên phải đúng ca và check-in mới được dùng POS, đơn hàng, thống kê hoặc hỗ trợ khách.
6. Đổi/trả tách luồng online và offline; hoàn tiền ví yêu cầu thông tin nhận tiền.
7. Báo cáo ca xuất XLSX thật và tính tiền mặt phải bàn giao.
8. Dashboard dùng truy vấn tổng hợp; danh sách sản phẩm, đơn hàng và khách hàng trong admin dùng phân trang server thay vì tải toàn bộ để đếm ở frontend.
9. Nhân viên có trung tâm công việc theo dữ liệu thật; ngoài ca chỉ thấy yêu cầu kiểm tra/xác nhận ca, trong ca mới được vào nghiệp vụ cửa hàng.
10. Vòng quay may mắn có miền dữ liệu riêng, khóa kho quà khi quay, chống quay lặp bằng ràng buộc database và không sửa dữ liệu đơn/voucher/tồn kho sản phẩm.

## 4. Kết quả rà soát commit mới

### Commit `f3a2683`

- Ý tưởng thêm VI/EN phù hợp rubric nhưng bản ban đầu mới dịch một phần rất nhỏ.
- Bản ban đầu đổi sai một số liên kết/nội dung footer và tạo nút ngôn ngữ admin dù trang admin chưa được dịch.
- Đã sửa lại liên kết footer, mở rộng từ điển cho khung khách hàng, cập nhật thuộc tính `lang` của trang và bỏ nút dịch gây hiểu nhầm ở admin.
- Trạng thái chính xác sau sửa: hỗ trợ song ngữ một phần, chưa phải đa ngôn ngữ toàn website.

### Commit `6f8bd64`

- Cảnh báo tiền khách đưa thiếu ở POS bị trùng với cảnh báo đã có và chỉ kiểm tra tại frontend.
- Đã bỏ phần cảnh báo trùng, gửi số tiền khách đưa về backend và từ chối tạo đơn nếu số tiền không hợp lệ hoặc chưa đủ.
- Watcher voucher có thể nhận phản hồi cũ sau khi tổng tiền đã đổi hoặc component đã đóng. Đã thêm mã phiên yêu cầu và hủy timer khi unmount.
- Phần bản đồ cửa hàng là thay đổi hợp lý và được giữ lại, nhưng bản commit dùng một class nút không tồn tại và thông tin liên hệ chưa đồng nhất với footer. Hai điểm này đã được sửa.

### Tài liệu đi kèm commit

Hai tài liệu cũ đã được thay bằng tài liệu này vì có các khẳng định không kiểm chứng như “top 5%”, “10/10 tuyệt đối”, gọi cố định model GPT-4 và mô tả đa ngôn ngữ là hoàn tất. Khi bảo vệ chỉ nên trình bày điều đã chứng minh được bằng code, test và dữ liệu.

## 5. Kiểm thử và dữ liệu đã đối chiếu

Backend hiện có 32 test: 24 test nghiệp vụ/bảo mật trong `PaymentAndOrderSecurityTests`, năm test nghiệp vụ vòng quay, hai test xác thực tệp biểu tượng và một test khởi động context. Các nhóm chính gồm quyền admin/nhân viên/khách, trạng thái và xác minh thanh toán, checkout đồng thời, POS, OTP hủy đơn, ca làm, đổi trả, hoàn kho/voucher, báo cáo XLSX, chat nhân viên, validate nhân viên, điều kiện/kho quà vòng quay và kiểm tra magic bytes của ảnh tải lên.

Test dùng H2 để chạy nhanh và độc lập. H2 kiểm tra tốt logic controller/service/repository phổ thông, nhưng không thay thế hoàn toàn SQL Server đối với khóa, isolation, index và cú pháp đặc thù. Vì vậy dự án còn được kiểm tra trực tiếp trên database `fashion_shop`.

Trạng thái database local sau khi dọn dữ liệu thử:

| Dữ liệu | Số lượng |
|---|---:|
| Sản phẩm | 60 |
| Biến thể | 548 |
| Tổng tồn | 7.758 |
| Khách hàng | 13 |
| Hóa đơn | 376 |
| Đánh giá | 124 |
| Voucher | 19 |
| Đợt khuyến mãi | 4 |
| Ca làm | 369 |
| Chiến dịch vòng quay | 1 |
| Lượt quay mẫu | 1 |

Các kiểm tra toàn vẹn đã đạt: không tồn kho âm, không trùng mã hóa đơn, không có chi tiết hóa đơn mồ côi và `DBCC CHECKCONSTRAINTS` không báo vi phạm.

Kết quả xác minh bản hiện tại:

- Backend: `32/32` test đạt.
- Frontend: Vite `8.1.5` build production thành công; `npm audit` không phát hiện lỗ hổng trong cây phụ thuộc hiện tại.
- SQL Server: chỉ có hai role `Admin` và `Nhân viên`; không có role quản lý kho, tồn âm, mã hóa đơn trùng hay chi tiết hóa đơn mồ côi.
- Giao diện: đã kiểm tra trực tiếp ở desktop và mobile cho trang chủ, giới thiệu, dashboard và trung tâm công việc; không có ảnh vỡ hoặc tràn ngang tại các màn hình này.
- Kiểu chữ: `Be Vietnam Pro` dùng cho nội dung, điều khiển và chữ số; `Noto Serif` chỉ dùng có chủ đích cho tiêu đề thời trang.
- Repository: `frontend/node_modules` đã được bỏ khỏi Git tracking; phiên bản cài đặt được tái tạo từ `package-lock.json`.

## 6. Việc nên hoàn thiện trước khi bảo vệ

1. Chạy lại kịch bản demo trên máy bảo vệ với SQL Server, email và sandbox MoMo/ZaloPay.
2. Chuẩn bị hai tài khoản admin/nhân viên và một ca đang hoạt động để trình diễn quyền theo ca.
3. Trình diễn chuyển VI/EN và nói rõ bản dịch tự động đã có độ phủ nhưng vẫn cần hiệu đính ngôn ngữ thủ công.
4. Không gọi dữ liệu tháng 6-7 là dữ liệu khách thật; đây là dữ liệu demo có liên kết nghiệp vụ.
5. Nếu còn thời gian, ưu tiên test end-to-end tự động cho checkout, POS, đổi trả, ca làm và vòng quay.

## 7. Hướng phát triển theo rubric

1. Hiệu đính bản dịch tiếng Anh bằng người dùng thật và bổ sung kiểm tra tự động phát hiện chuỗi chưa đưa vào i18n.
2. Tích hợp thật một kênh bán hàng ngoài: đồng bộ sản phẩm, tồn kho và đơn hàng qua API; không chỉ đặt liên kết.
3. Bổ sung E2E browser test, test tải đồng thời trên SQL Server và báo cáo độ phủ.
4. Thực hiện kiểm thử người dùng có kịch bản và audit WCAG để có bằng chứng khách quan cho tiêu chí trải nghiệm.
