# Database Zestia - fashion_shop

Thu muc nay chua file SQL chuan cho du an local.

## File chinh

| File | Mo ta |
|------|-------|
| `fashion_shop.sql` | Tao schema va du lieu demo cho san pham, bien the, don hang, khach hang, nhan vien, voucher, dot khuyen mai, danh gia, doi/tra, thong bao, newsletter va vong quay may man. |

## Cach chay

SSMS: mo `fashion_shop.sql` va bam Execute. File co dau hieu UTF-8 BOM de SSMS
va `sqlcmd` nhan dung tieng Viet.

Dong lenh:

```powershell
sqlcmd -S localhost,1433 -U sa -P 123456 -C -f 65001 -i fashion_shop.sql
```

Canh bao: file nay xoa toan bo database `fashion_shop` hien co va tao lai du lieu demo tu dau. Hay backup truoc khi chay neu co du lieu can giu.

File duoc viet theo huong idempotent: co the chay lai, du lieu chuan duoc them/cap
nhat theo khoa nghiep vu va cac bat bien du lieu duoc kiem tra truoc khi in thong
bao thanh cong. Du lieu nghiep vu thang 8/2026 phu du tu 01/08 den het 31/08/2026.
Moi ngay co 4 don mau, kem giao dich, tracking, audit va danh gia hop le. Chat ho
tro, newsletter, yeu thich, lich su xem va lich lam viec cung phu du 31 ngay;
voucher, dot khuyen mai, thong bao va vong quay co hieu luc den cuoi thang.

Luon giu `-f 65001` khi chay bang `sqlcmd`. File cung co khoi chuan hoa de tu sua
nhung dong demo tung bi sai ma hoa boi mot lan chay cu.

Luu y: voucher (`Giam_gia`, API `/api/voucher`) la ma giam gia nhap khi thanh toan; `Dot_khuyen_mai` la chien dich dieu chinh gia ban theo thoi gian va pham vi san pham.
