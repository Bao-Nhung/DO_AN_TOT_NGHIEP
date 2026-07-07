-- USE fashion_shop;
-- GO

-- SET NOCOUNT ON;
-- GO

-- /* Zestia demo data refresh - 2026
--    Run after backing up the database. Password values are intentionally plain
--    "123456"; the Spring DataInitializer will BCrypt-hash them on app startup.
-- */

-- DECLARE @adminRoleId INT;
-- DECLARE @staffRoleId INT;

-- IF NOT EXISTS (SELECT 1 FROM dbo.Vai_tro WHERE ten_vai_tro = N'Admin')
--     INSERT INTO dbo.Vai_tro (ten_vai_tro) VALUES (N'Admin');

-- IF NOT EXISTS (SELECT 1 FROM dbo.Vai_tro WHERE ten_vai_tro = N'Nhân viên')
--     INSERT INTO dbo.Vai_tro (ten_vai_tro) VALUES (N'Nhân viên');

-- UPDATE dbo.Vai_tro
-- SET ten_vai_tro = N'Nhân viên'
-- WHERE ten_vai_tro IN (N'NhanVien', N'Nhan Vien', N'NhÃ¢n viÃªn');

-- SELECT @adminRoleId = id FROM dbo.Vai_tro WHERE ten_vai_tro = N'Admin';
-- SELECT @staffRoleId = id FROM dbo.Vai_tro WHERE ten_vai_tro = N'Nhân viên';

-- IF NOT EXISTS (SELECT 1 FROM dbo.Nhan_vien WHERE ten_nguoi_dung = N'admin')
-- BEGIN
--     INSERT INTO dbo.Nhan_vien
--         (id_vai_tro, ma_nhan_vien, ho_va_ten, gioi_tinh, ngay_sinh, so_dien_thoai, dia_chi, email, ten_nguoi_dung, mat_khau, tinh_trang_lam_viec, ngay_tao)
--     VALUES
--         (@adminRoleId, N'NV001', N'Quản trị Zestia', 1, '1998-01-10', N'0900000001', N'Hà Nội', N'admin@zestia.vn', N'admin', N'123456', 1, GETDATE());
-- END
-- ELSE
-- BEGIN
--     UPDATE dbo.Nhan_vien
--     SET id_vai_tro = @adminRoleId,
--         ho_va_ten = N'Quản trị Zestia',
--         email = N'admin@zestia.vn',
--         tinh_trang_lam_viec = 1
--     WHERE ten_nguoi_dung = N'admin';
-- END

-- IF NOT EXISTS (SELECT 1 FROM dbo.Nhan_vien WHERE ten_nguoi_dung = N'nv_pos')
-- BEGIN
--     INSERT INTO dbo.Nhan_vien
--         (id_vai_tro, ma_nhan_vien, ho_va_ten, gioi_tinh, ngay_sinh, so_dien_thoai, dia_chi, email, ten_nguoi_dung, mat_khau, tinh_trang_lam_viec, ngay_tao)
--     VALUES
--         (@staffRoleId, N'NV002', N'Linh Mai - Nhân viên bán hàng', 0, '2001-05-18', N'0900000002', N'Zestia Flagship Store', N'nv.pos@zestia.vn', N'nv_pos', N'123456', 1, GETDATE());
-- END
-- ELSE
-- BEGIN
--     UPDATE dbo.Nhan_vien
--     SET id_vai_tro = @staffRoleId,
--         ho_va_ten = N'Linh Mai - Nhân viên bán hàng',
--         tinh_trang_lam_viec = 1
--     WHERE ten_nguoi_dung = N'nv_pos';
-- END

-- IF NOT EXISTS (SELECT 1 FROM dbo.Khach_hang WHERE email = N'khach.demo@zestia.vn')
-- BEGIN
--     INSERT INTO dbo.Khach_hang
--         (ma_khach_hang, ho_va_ten, gioi_tinh, ngay_sinh, so_dien_thoai, email, mat_khau, ngay_tao)
--     VALUES
--         (N'KHDEMO', N'Minh Anh', 0, '2002-09-20', N'0911111111', N'khach.demo@zestia.vn', N'123456', GETDATE());
-- END

-- DECLARE @demoCustomerId INT = (SELECT TOP 1 id FROM dbo.Khach_hang WHERE email = N'khach.demo@zestia.vn');
-- IF @demoCustomerId IS NOT NULL
-- AND NOT EXISTS (SELECT 1 FROM dbo.Dia_chi WHERE id_khach_hang = @demoCustomerId AND mac_dinh = 1)
-- BEGIN
--     INSERT INTO dbo.Dia_chi
--         (id_khach_hang, tinh_thanh_pho, quan_huyen, xa_phuong, duong, mac_dinh)
--     VALUES
--         (@demoCustomerId, N'Thành phố Hà Nội', N'Quận Cầu Giấy', N'Phường Dịch Vọng', N'24 Xuân Thủy', 1);
-- END

-- UPDATE dbo.Khuyen_mai
-- SET trang_thai = 0
-- WHERE trang_thai = 1;

-- UPDATE dbo.Giam_gia
-- SET trang_thai = 0
-- WHERE ngay_ket_thuc < CAST(GETDATE() AS DATE);

-- MERGE dbo.Giam_gia AS target
-- USING (VALUES
--     (N'ZESTIA10', N'Giảm 10% cho đơn từ 500K', 500000, NULL, 10.00, 100000, 200),
--     (N'ZESTIA50', N'Giảm 50K cho đơn từ 800K', 800000, 50000, NULL, NULL, 150),
--     (N'FREESHIP', N'Hỗ trợ phí vận chuyển', 300000, 30000, NULL, NULL, 300)
-- ) AS src(ma_giam_gia, ten_giam_gia, gia_tri_don_toi_thieu, gio_tri_giam, phan_tram_giam, giam_toi_da, so_luong)
-- ON target.ma_giam_gia = src.ma_giam_gia
-- WHEN MATCHED THEN
--     UPDATE SET
--         ten_giam_gia = src.ten_giam_gia,
--         gia_tri_don_toi_thieu = src.gia_tri_don_toi_thieu,
--         gio_tri_giam = src.gio_tri_giam,
--         phan_tram_giam = src.phan_tram_giam,
--         giam_toi_da = src.giam_toi_da,
--         so_luong = src.so_luong,
--         ngay_bat_dau = DATEADD(DAY, -7, GETDATE()),
--         ngay_ket_thuc = DATEADD(DAY, 120, GETDATE()),
--         trang_thai = 1
-- WHEN NOT MATCHED THEN
--     INSERT (ma_giam_gia, ten_giam_gia, gia_tri_don_toi_thieu, gio_tri_giam, phan_tram_giam, giam_toi_da, so_luong, ngay_bat_dau, ngay_ket_thuc, trang_thai, ngay_tao)
--     VALUES (src.ma_giam_gia, src.ten_giam_gia, src.gia_tri_don_toi_thieu, src.gio_tri_giam, src.phan_tram_giam, src.giam_toi_da, src.so_luong, DATEADD(DAY, -7, GETDATE()), DATEADD(DAY, 120, GETDATE()), 1, GETDATE());

-- UPDATE dbo.Vay_chi_tiet
-- SET gia_ban_goc = ROUND(gia_ban * 0.85, 0)
-- WHERE gia_ban IS NOT NULL
--   AND (gia_ban_goc IS NULL OR gia_ban_goc >= gia_ban);

-- UPDATE dbo.Vay_chi_tiet
-- SET gia_nhap = ROUND(gia_ban * 0.65, 0)
-- WHERE gia_ban IS NOT NULL
--   AND gia_nhap IS NULL;

-- UPDATE dbo.Vay_chi_tiet
-- SET so_luong = 12
-- WHERE so_luong IS NULL OR so_luong < 0;

-- UPDATE dbo.Vay_chi_tiet
-- SET trang_thai = 1
-- WHERE trang_thai IS NULL;

-- DECLARE @defaultStaffId INT = (SELECT TOP 1 id FROM dbo.Nhan_vien WHERE ten_nguoi_dung = N'nv_pos');

-- UPDATE dbo.Hoa_don
-- SET hinh_thuc_nhan_hang = 0,
--     dia_chi_giao_hang = N'Mua trực tiếp tại cửa hàng',
--     id_nhan_vien = COALESCE(id_nhan_vien, @defaultStaffId),
--     da_thanh_toan = 1,
--     ghi_chu = COALESCE(NULLIF(ghi_chu, N''), N'[Tại quầy] Dữ liệu demo đã chuẩn hóa')
-- WHERE hinh_thuc_nhan_hang = 0
--    OR ghi_chu LIKE N'%Tại quầy%'
--    OR ghi_chu LIKE N'%tai quay%';

-- UPDATE dbo.Hoa_don
-- SET trang_thai = 7,
--     da_thanh_toan = 0,
--     phuong_thuc_thanh_toan_online = N'FAILED'
-- WHERE phuong_thuc_thanh_toan_online = N'FAILED'
--    OR (trang_thai = 5 AND hinh_thuc_thanh_toan IN (N'MOMO', N'ZALOPAY', N'VNPAY') AND ISNULL(da_thanh_toan, 0) = 0);

-- IF NOT EXISTS (SELECT 1 FROM dbo.Thong_bao WHERE tieu_de = N'Bộ sưu tập mới đã lên kệ')
-- BEGIN
--     INSERT INTO dbo.Thong_bao (tieu_de, noi_dung, loai, trang_thai, ngay_tao)
--     VALUES
--         (N'Bộ sưu tập mới đã lên kệ', N'Zestia cập nhật các mẫu váy mới cho mùa lễ hội và sự kiện.', N'HeThong', 1, GETDATE()),
--         (N'Voucher ZESTIA10 đang hoạt động', N'Khách hàng có thể nhập ZESTIA10 để giảm 10% cho đơn đủ điều kiện.', N'KhuyenMai', 1, GETDATE());
-- END

-- PRINT N'Zestia demo data refresh completed.';
-- GO
