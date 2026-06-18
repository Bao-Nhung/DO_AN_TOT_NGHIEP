# Database Zestia — fashion_shop

Thư mục này chứa **dữ liệu chuẩn** của dự án. Nhờ nó, dự án chạy **giống hệt nhau trên mọi máy**.

## Có gì trong đây?

| File | Mô tả |
|------|-------|
| **`fashion_shop.sql`** | File SQL **duy nhất, hoàn chỉnh**: tạo database, tạo tất cả bảng và nạp đầy đủ dữ liệu (60 sản phẩm, 548 biến thể kèm **giá**, 240 **hình ảnh**, đơn hàng, khách hàng, tài khoản...). |
| `export-db.ps1` | Script PowerShell để **xuất lại** `fashion_shop.sql` từ database đang chạy (chỉ dùng khi cần cập nhật dữ liệu chuẩn). |

## Cách dùng (cài trên máy mới)

> Đây là bước bắt buộc để sản phẩm hiển thị **đúng giá và hình ảnh**.

**Cách 1 — SSMS:** mở `fashion_shop.sql` → nhấn **Execute (F5)**.

**Cách 2 — dòng lệnh:**
```
sqlcmd -S localhost,1433 -U sa -P 123456 -C -i fashion_shop.sql
```

File **an toàn khi chạy lại nhiều lần**: mỗi bảng chỉ nạp khi đang trống (idempotent), không bị trùng dữ liệu.

## Vì sao trước đây sang máy khác bị lỗi giá 0đ / mất ảnh?

- **Giá tiền** không nằm trong bảng `Vay` (sản phẩm) mà nằm ở bảng **`Vay_chi_tiet`** (biến thể).
- **Hình ảnh** nằm ở bảng **`Anh`**.

Khi chỉ sao chép một phần dữ liệu (hoặc để Spring Boot tự tạo bảng trống), hai bảng này thiếu dữ liệu → sản phẩm hiện `0đ` và không có ảnh. File `fashion_shop.sql` nạp **đầy đủ cả ba bảng** nên khắc phục triệt để.

## Cập nhật lại file dữ liệu chuẩn (tùy chọn)

Khi bạn đã thêm/sửa dữ liệu trên máy mình và muốn cập nhật file chuẩn cho cả nhóm:

```
powershell -ExecutionPolicy Bypass -File export-db.ps1
```

Script sẽ đọc database `fashion_shop` đang chạy và ghi đè `fashion_shop.sql`.
