# Database Zestia - fashion_shop

Thu muc nay chua file SQL chuan cho du an local.

## File chinh

| File | Mo ta |
|------|-------|
| `fashion_shop.sql` | Tao database, tao bang va nap du lieu demo cho san pham, bien the, anh, don hang, khach hang, nhan vien, voucher, thong bao va newsletter. |

## Cach chay

SSMS: mo `fashion_shop.sql` va bam Execute.

Dong lenh:

```powershell
sqlcmd -S localhost,1433 -U sa -P 123456 -C -i fashion_shop.sql
```

File duoc viet theo huong an toan khi chay lai: bang chi tao khi chua ton tai, du lieu mau chi nap khi bang trong, cac cot bo sung deu co kiem tra ton tai truoc khi `ALTER TABLE`.

Luu y: du an hien chi dung domain voucher (`Giam_gia`/API `/api/voucher`) cho ma giam gia.
