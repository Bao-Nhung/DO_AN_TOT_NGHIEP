# Database Zestia - fashion_shop

Thu muc nay chua file SQL chuan cho du an local.

## File chinh

| File | Mo ta |
|------|-------|
| `fashion_shop.sql` | Tao schema va du lieu demo cho san pham, bien the, don hang, khach hang, nhan vien, voucher, dot khuyen mai, danh gia, doi/tra, thong bao va newsletter. |

## Cach chay

SSMS: mo `fashion_shop.sql` va bam Execute. File co dau hieu UTF-8 BOM de SSMS
va `sqlcmd` nhan dung tieng Viet.

Dong lenh:

```powershell
sqlcmd -S localhost,1433 -U sa -P 123456 -C -f 65001 -i fashion_shop.sql
```

File duoc viet theo huong idempotent: co the chay lai, du lieu chuan duoc them/cap
nhat theo khoa nghiep vu va cac bat bien du lieu duoc kiem tra truoc khi in thong
bao thanh cong. Du lieu thang 8/2026 phu du 31 ngay va co don hang, giao dich,
tracking, audit, danh gia, ca lam, chat ho tro va doi/tra.

Luon giu `-f 65001` khi chay bang `sqlcmd`. File cung co khoi chuan hoa de tu sua
nhung dong demo tung bi sai ma hoa boi mot lan chay cu.

Luu y: voucher (`Giam_gia`, API `/api/voucher`) la ma giam gia nhap khi thanh toan; `Dot_khuyen_mai` la chien dich dieu chinh gia ban theo thoi gian va pham vi san pham.
