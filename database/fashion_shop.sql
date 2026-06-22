-- ============================================================
-- ZESTIA  -  fashion_shop  (schema + du lieu day du)
-- File chuan, tu dong export tu DB dang chay.
-- Cach dung: mo SSMS hoac sqlcmd, mo file nay, Execute 1 lan.
-- ============================================================
IF DB_ID('fashion_shop') IS NULL CREATE DATABASE [fashion_shop];
GO
USE [fashion_shop];
GO

-- ===== Vai_tro =====
IF OBJECT_ID(N'dbo.Vai_tro','U') IS NULL
BEGIN
CREATE TABLE [dbo].[Vai_tro] (
  [id] int IDENTITY(1,1) NOT NULL,
  [ten_vai_tro] nvarchar(100) NOT NULL,
  CONSTRAINT [PK_Vai_tro] PRIMARY KEY ([id])
);
END
GO
IF NOT EXISTS (SELECT 1 FROM [dbo].[Vai_tro])
BEGIN
SET IDENTITY_INSERT [dbo].[Vai_tro] ON;
INSERT INTO [dbo].[Vai_tro] ([id], [ten_vai_tro]) VALUES
(1, N'Admin'),
(2, N'Nhân viên'),
(3, N'Quản lý kho');
SET IDENTITY_INSERT [dbo].[Vai_tro] OFF;
END
GO

-- ===== Nhan_vien =====
IF OBJECT_ID(N'dbo.Nhan_vien','U') IS NULL
BEGIN
CREATE TABLE [dbo].[Nhan_vien] (
  [id] int IDENTITY(1,1) NOT NULL,
  [id_vai_tro] int NULL,
  [ma_nhan_vien] nvarchar(50) NULL,
  [ho_va_ten] nvarchar(150) NOT NULL,
  [gioi_tinh] tinyint NULL,
  [ngay_sinh] date NULL,
  [so_dien_thoai] nvarchar(20) NULL,
  [dia_chi] nvarchar(255) NULL,
  [email] nvarchar(150) NULL,
  [ten_nguoi_dung] nvarchar(100) NULL,
  [mat_khau] nvarchar(255) NULL,
  [tinh_trang_lam_viec] tinyint NULL,
  [ngay_tao] datetime2(7) NULL,
  CONSTRAINT [PK_Nhan_vien] PRIMARY KEY ([id])
);
END
GO
IF NOT EXISTS (SELECT 1 FROM [dbo].[Nhan_vien])
BEGIN
SET IDENTITY_INSERT [dbo].[Nhan_vien] ON;
INSERT INTO [dbo].[Nhan_vien] ([id], [id_vai_tro], [ma_nhan_vien], [ho_va_ten], [gioi_tinh], [ngay_sinh], [so_dien_thoai], [dia_chi], [email], [ten_nguoi_dung], [mat_khau], [tinh_trang_lam_viec], [ngay_tao]) VALUES
(1, 1, N'NV001', N'Quản trị viên', NULL, NULL, NULL, NULL, N'admin@zestia.vn', N'admin', N'$2a$10$1319tfuwROs5099h0RHfbeEV.RarbCu15eZh09TwTuRsFznGC0Zze', 1, '2026-06-10T23:44:29.193'),
(2, 2, N'NV002', N'Trần Minh Tuấn', NULL, NULL, N'0901234567', NULL, N'tuan@zestia.vn', N'tuannv', N'$2a$10$csfRWdR./6P2bikv1yGV5uoJSBEiNQFgrdM9tqkWFv5CDE.BasFY6', 1, '2026-06-10T23:44:29.200');
SET IDENTITY_INSERT [dbo].[Nhan_vien] OFF;
END
GO

-- ===== Lich_lam_viec =====
IF OBJECT_ID(N'dbo.Lich_lam_viec','U') IS NULL
BEGIN
CREATE TABLE [dbo].[Lich_lam_viec] (
  [id] int IDENTITY(1,1) NOT NULL,
  [id_nhan_vien] int NULL,
  [ngay_lam_viec] date NOT NULL,
  [ca_lam_viec] nvarchar(50) NULL,
  [gio_bat_dau] time(7) NULL,
  [gio_ket_thuc] time(7) NULL,
  [vi_tri] nvarchar(100) NULL,
  [ghi_chu] nvarchar(500) NULL,
  [trang_thai] tinyint NULL,
  [ngay_tao] datetime2(7) NULL,
  [ngay_cap_nhat] datetime2(7) NULL,
  [trang_thai_xoa] bit DEFAULT 0 NOT NULL,
  CONSTRAINT [PK_Lich_lam_viec] PRIMARY KEY ([id])
);
END
GO
IF NOT EXISTS (SELECT 1 FROM [dbo].[Lich_lam_viec])
BEGIN
SET IDENTITY_INSERT [dbo].[Lich_lam_viec] ON;
INSERT INTO [dbo].[Lich_lam_viec] ([id], [id_nhan_vien], [ngay_lam_viec], [ca_lam_viec], [gio_bat_dau], [gio_ket_thuc], [vi_tri], [ghi_chu], [trang_thai], [ngay_tao], [ngay_cap_nhat], [trang_thai_xoa]) VALUES
(1, 1, '2026-06-22', N'Ca sáng', '08:00:00', '12:00:00', N'Cửa hàng', N'Kiểm tra vận hành đầu ngày', 1, '2026-06-22T08:00:00.000', '2026-06-22T08:00:00.000', 0),
(2, 2, '2026-06-22', N'Ca chiều', '13:00:00', '17:00:00', N'Quầy bán hàng', N'Tư vấn khách và xử lý đơn tại quầy', 1, '2026-06-22T08:00:00.000', '2026-06-22T08:00:00.000', 0),
(3, 2, '2026-06-24', N'Ca sáng', '08:00:00', '12:00:00', N'Kho', N'Sắp xếp tồn kho theo size/màu', 0, '2026-06-22T08:00:00.000', '2026-06-22T08:00:00.000', 0),
(4, 1, '2026-06-26', N'Ca cả ngày', '08:00:00', '17:00:00', N'Cửa hàng', N'Tổng kết doanh thu tuần', 1, '2026-06-22T08:00:00.000', '2026-06-22T08:00:00.000', 0);
SET IDENTITY_INSERT [dbo].[Lich_lam_viec] OFF;
END
GO

-- ===== Khach_hang =====
IF OBJECT_ID(N'dbo.Khach_hang','U') IS NULL
BEGIN
CREATE TABLE [dbo].[Khach_hang] (
  [id] int IDENTITY(1,1) NOT NULL,
  [ma_khach_hang] nvarchar(50) NULL,
  [ho_va_ten] nvarchar(150) NOT NULL,
  [gioi_tinh] tinyint NULL,
  [ngay_sinh] date NULL,
  [so_dien_thoai] nvarchar(20) NULL,
  [email] nvarchar(150) NULL,
  [mat_khau] nvarchar(255) NULL,
  [ngay_tao] datetime2(7) NULL,
  CONSTRAINT [PK_Khach_hang] PRIMARY KEY ([id])
);
END
GO
IF NOT EXISTS (SELECT 1 FROM [dbo].[Khach_hang])
BEGIN
SET IDENTITY_INSERT [dbo].[Khach_hang] ON;
INSERT INTO [dbo].[Khach_hang] ([id], [ma_khach_hang], [ho_va_ten], [gioi_tinh], [ngay_sinh], [so_dien_thoai], [email], [mat_khau], [ngay_tao]) VALUES
(1, N'KH001', N'Nguyễn Lan Anh', 0, '1995-03-15', N'0912345678', N'lananh@email.com', N'$2a$10$NgsNjrRb637ghlDZct04PeWt91d7ilCBmeRZYP2O6A/5QyvUIrhcu', '2026-06-10T23:44:29.206'),
(2, N'KH002', N'Phạm Thị Hương', 0, '1998-07-22', N'0923456789', N'huong@email.com', N'$2a$10$ODu0aFkPU/3g7rm.k2CNMemX5S8xX1pQyK9wre2.m3H38pdJgO//K', '2026-06-10T23:44:29.206'),
(3, N'KH003', N'Lê Văn Minh', 1, '1992-11-08', N'0934567890', N'minh@email.com', N'$2a$10$TtnHfT8nBplciIuAZBeBzeB5bCClZ3hYtKSgZXfyvNryoHVBa5SXS', '2026-06-10T23:44:29.206'),
(4, N'KH00004', N'Test User', NULL, NULL, N'0901234567', N'test@zestia.vn', N'$2a$10$v57XvllkZqAlI0bAOZOZ4u5I/uXqKdzhesaLfCRhyRVj9FyUmBzxO', '2026-06-11T14:02:40.926');
SET IDENTITY_INSERT [dbo].[Khach_hang] OFF;
END
GO

-- ===== Dia_chi =====
IF OBJECT_ID(N'dbo.Dia_chi','U') IS NULL
BEGIN
CREATE TABLE [dbo].[Dia_chi] (
  [id] int IDENTITY(1,1) NOT NULL,
  [id_khach_hang] int NOT NULL,
  [tinh_thanh_pho] nvarchar(100) NULL,
  [quan_huyen] nvarchar(100) NULL,
  [xa_phuong] nvarchar(100) NULL,
  [duong] nvarchar(255) NULL,
  [mac_dinh] tinyint NULL,
  CONSTRAINT [PK_Dia_chi] PRIMARY KEY ([id])
);
END
GO
IF NOT EXISTS (SELECT 1 FROM [dbo].[Dia_chi])
BEGIN
SET IDENTITY_INSERT [dbo].[Dia_chi] ON;
INSERT INTO [dbo].[Dia_chi] ([id], [id_khach_hang], [tinh_thanh_pho], [quan_huyen], [xa_phuong], [duong], [mac_dinh]) VALUES
(1, 1, N'Hà Nội', N'Hoàn Kiếm', N'Tràng Tiền', N'128 Trần Hưng Đạo', 1),
(2, 2, N'TP. Hồ Chí Minh', N'Quận 1', N'Bến Nghé', N'45 Nguyễn Huệ', 1),
(3, 3, N'Đà Nẵng', N'Hải Châu', N'Thạch Thang', N'22 Bạch Đằng', 1);
SET IDENTITY_INSERT [dbo].[Dia_chi] OFF;
END
GO

-- ===== Loai_vay =====
IF OBJECT_ID(N'dbo.Loai_vay','U') IS NULL
BEGIN
CREATE TABLE [dbo].[Loai_vay] (
  [id] int IDENTITY(1,1) NOT NULL,
  [ten_loai_vay] nvarchar(150) NOT NULL,
  [trang_thai] tinyint NULL,
  [mo_ta] nvarchar(max) NULL,
  [ngay_tao] datetime2(7) NULL,
  CONSTRAINT [PK_Loai_vay] PRIMARY KEY ([id])
);
END
GO
IF NOT EXISTS (SELECT 1 FROM [dbo].[Loai_vay])
BEGIN
SET IDENTITY_INSERT [dbo].[Loai_vay] ON;
INSERT INTO [dbo].[Loai_vay] ([id], [ten_loai_vay], [trang_thai], [mo_ta], [ngay_tao]) VALUES
(1, N'Váy truyền thống', 1, NULL, '2026-06-10T23:44:29.116'),
(2, N'Váy cách tân', 1, NULL, '2026-06-10T23:44:29.116'),
(3, N'Váy dạ hội', 1, NULL, '2026-06-10T23:44:29.116'),
(4, N'Váy cưới', 1, NULL, '2026-06-10T23:44:29.116'),
(5, N'Váy học sinh', 1, NULL, '2026-06-10T23:44:29.116'),
(6, N'Váy công sở', 1, NULL, '2026-06-10T23:44:29.116');
SET IDENTITY_INSERT [dbo].[Loai_vay] OFF;
END
GO

-- ===== Chat_lieu =====
IF OBJECT_ID(N'dbo.Chat_lieu','U') IS NULL
BEGIN
CREATE TABLE [dbo].[Chat_lieu] (
  [id] int IDENTITY(1,1) NOT NULL,
  [ten_chat_lieu] nvarchar(100) NOT NULL,
  [trang_thai] tinyint NULL,
  [mo_ta] nvarchar(max) NULL,
  [ngay_tao] datetime2(7) NULL,
  CONSTRAINT [PK_Chat_lieu] PRIMARY KEY ([id])
);
END
GO
IF NOT EXISTS (SELECT 1 FROM [dbo].[Chat_lieu])
BEGIN
SET IDENTITY_INSERT [dbo].[Chat_lieu] ON;
INSERT INTO [dbo].[Chat_lieu] ([id], [ten_chat_lieu], [trang_thai], [mo_ta], [ngay_tao]) VALUES
(1, N'Lụa tơ tằm', 1, NULL, '2026-06-10T23:44:29.106'),
(2, N'Gấm', 1, NULL, '2026-06-10T23:44:29.106'),
(3, N'Voan', 1, NULL, '2026-06-10T23:44:29.106'),
(4, N'Cotton', 1, NULL, '2026-06-10T23:44:29.106'),
(5, N'Đũi', 1, NULL, '2026-06-10T23:44:29.106'),
(6, N'Nhung', 1, NULL, '2026-06-10T23:44:29.106');
SET IDENTITY_INSERT [dbo].[Chat_lieu] OFF;
END
GO

-- ===== Mau_Sac =====
IF OBJECT_ID(N'dbo.Mau_Sac','U') IS NULL
BEGIN
CREATE TABLE [dbo].[Mau_Sac] (
  [id] int IDENTITY(1,1) NOT NULL,
  [ten_mau_sac] nvarchar(100) NOT NULL,
  [ma_hex] varchar(7) NULL,
  [trang_thai] tinyint NULL,
  [ngay_tao] datetime2(7) NULL,
  CONSTRAINT [PK_Mau_Sac] PRIMARY KEY ([id])
);
END
GO
IF NOT EXISTS (SELECT 1 FROM [dbo].[Mau_Sac])
BEGIN
SET IDENTITY_INSERT [dbo].[Mau_Sac] ON;
INSERT INTO [dbo].[Mau_Sac] ([id], [ten_mau_sac], [ma_hex], [trang_thai], [ngay_tao]) VALUES
(1, N'Đỏ', N'#FF0000', 1, '2026-06-10T23:44:29.096'),
(2, N'Xanh Navy', N'#001F5B', 1, '2026-06-10T23:44:29.096'),
(3, N'Vàng', N'#FFD700', 1, '2026-06-10T23:44:29.096'),
(4, N'Trắng', N'#FFFFFF', 1, '2026-06-10T23:44:29.096'),
(5, N'Đen', N'#000000', 1, '2026-06-10T23:44:29.096'),
(6, N'Hồng', N'#FFC0CB', 1, '2026-06-10T23:44:29.096'),
(7, N'Tím', N'#800080', 1, '2026-06-10T23:44:29.096'),
(8, N'Xanh Lá', N'#008000', 1, '2026-06-10T23:44:29.096');
SET IDENTITY_INSERT [dbo].[Mau_Sac] OFF;
END
GO

-- ===== Kich_Thuoc =====
IF OBJECT_ID(N'dbo.Kich_Thuoc','U') IS NULL
BEGIN
CREATE TABLE [dbo].[Kich_Thuoc] (
  [id] int IDENTITY(1,1) NOT NULL,
  [ten_kich_thuoc] nvarchar(50) NOT NULL,
  [mo_ta] nvarchar(max) NULL,
  [trang_thai] tinyint NULL,
  [ngay_tao] datetime2(7) NULL,
  CONSTRAINT [PK_Kich_Thuoc] PRIMARY KEY ([id])
);
END
GO
IF NOT EXISTS (SELECT 1 FROM [dbo].[Kich_Thuoc])
BEGIN
SET IDENTITY_INSERT [dbo].[Kich_Thuoc] ON;
INSERT INTO [dbo].[Kich_Thuoc] ([id], [ten_kich_thuoc], [mo_ta], [trang_thai], [ngay_tao]) VALUES
(1, N'S', N'Small - Nhỏ', 1, '2026-06-10T23:44:29.100'),
(2, N'M', N'Medium - Vừa', 1, '2026-06-10T23:44:29.100'),
(3, N'L', N'Large - Lớn', 1, '2026-06-10T23:44:29.100'),
(4, N'XL', N'Extra Large', 1, '2026-06-10T23:44:29.100'),
(5, N'XXL', N'Double Extra Large', 1, '2026-06-10T23:44:29.100'),
(6, N'Free size', N'Một kích thước', 1, '2026-06-10T23:44:29.100');
SET IDENTITY_INSERT [dbo].[Kich_Thuoc] OFF;
END
GO

-- ===== Nha_cung_cap =====
IF OBJECT_ID(N'dbo.Nha_cung_cap','U') IS NULL
BEGIN
CREATE TABLE [dbo].[Nha_cung_cap] (
  [id] int IDENTITY(1,1) NOT NULL,
  [ten_nha_cung_cap] nvarchar(150) NOT NULL,
  [dia_chi] nvarchar(255) NULL,
  [so_dien_thoai] nvarchar(20) NULL,
  [email] nvarchar(100) NULL,
  [trang_thai] tinyint NULL,
  [mo_ta] nvarchar(max) NULL,
  [ngay_tao] datetime2(7) NULL,
  CONSTRAINT [PK_Nha_cung_cap] PRIMARY KEY ([id])
);
END
GO
IF NOT EXISTS (SELECT 1 FROM [dbo].[Nha_cung_cap])
BEGIN
SET IDENTITY_INSERT [dbo].[Nha_cung_cap] ON;
INSERT INTO [dbo].[Nha_cung_cap] ([id], [ten_nha_cung_cap], [dia_chi], [so_dien_thoai], [email], [trang_thai], [mo_ta], [ngay_tao]) VALUES
(1, N'Công ty Lụa Bảo Lộc', N'Bảo Lộc, Lâm Đồng', N'0263123456', N'baoloc@silk.vn', 1, NULL, '2026-06-10T23:44:29.120'),
(2, N'Xưởng Gấm Hà Nội', N'12 Hàng Bông, Hoàn Kiếm, HN', N'0243456789', N'gam@hanoi.vn', 1, NULL, '2026-06-10T23:44:29.120');
SET IDENTITY_INSERT [dbo].[Nha_cung_cap] OFF;
END
GO

-- ===== Tai_tro =====
IF OBJECT_ID(N'dbo.Tai_tro','U') IS NULL
BEGIN
CREATE TABLE [dbo].[Tai_tro] (
  [id] int IDENTITY(1,1) NOT NULL,
  [ten_tai_tro] nvarchar(150) NOT NULL,
  [trang_thai] tinyint NULL,
  [mo_ta] nvarchar(max) NULL,
  [ngay_tao] datetime2(7) NULL,
  CONSTRAINT [PK_Tai_tro] PRIMARY KEY ([id])
);
END
GO
IF NOT EXISTS (SELECT 1 FROM [dbo].[Tai_tro])
BEGIN
SET IDENTITY_INSERT [dbo].[Tai_tro] ON;
INSERT INTO [dbo].[Tai_tro] ([id], [ten_tai_tro], [trang_thai], [mo_ta], [ngay_tao]) VALUES
(1, N'Zestia Original', 1, NULL, '2026-06-10T23:44:29.130'),
(2, N'Local Brand', 1, NULL, '2026-06-10T23:44:29.130'),
(3, N'Premium Line', 1, NULL, '2026-06-10T23:44:29.130');
SET IDENTITY_INSERT [dbo].[Tai_tro] OFF;
END
GO

-- ===== Khuyen_mai =====
IF OBJECT_ID(N'dbo.Khuyen_mai','U') IS NULL
BEGIN
CREATE TABLE [dbo].[Khuyen_mai] (
  [id] int IDENTITY(1,1) NOT NULL,
  [ma_khuyen_mai] nvarchar(50) NOT NULL,
  [ten_khuyen_mai] nvarchar(200) NOT NULL,
  [phan_tram_giam] decimal(5,2) NULL,
  [so_tien_giam] decimal(15,2) NULL,
  [ngay_bat_dau] date NOT NULL,
  [ngay_ket_thuc] date NOT NULL,
  [mo_ta] nvarchar(max) NULL,
  [ngay_tao] datetime2(7) NULL,
  [trang_thai] tinyint NULL,
  CONSTRAINT [PK_Khuyen_mai] PRIMARY KEY ([id])
);
END
GO
IF NOT EXISTS (SELECT 1 FROM [dbo].[Khuyen_mai])
BEGIN
SET IDENTITY_INSERT [dbo].[Khuyen_mai] ON;
INSERT INTO [dbo].[Khuyen_mai] ([id], [ma_khuyen_mai], [ten_khuyen_mai], [phan_tram_giam], [so_tien_giam], [ngay_bat_dau], [ngay_ket_thuc], [mo_ta], [ngay_tao], [trang_thai]) VALUES
(1, N'KM_TETNGUYEN', N'Khuyến mãi Tết Nguyên Đán', 20.00, 0.00, '2025-01-01', '2025-02-15', NULL, '2026-06-10T23:44:29.126', 1),
(2, N'KM_8_3', N'Sale mừng 8/3', 15.00, 0.00, '2025-03-01', '2025-03-10', NULL, '2026-06-10T23:44:29.126', 1);
SET IDENTITY_INSERT [dbo].[Khuyen_mai] OFF;
END
GO

-- ===== Giam_gia =====
IF OBJECT_ID(N'dbo.Giam_gia','U') IS NULL
BEGIN
CREATE TABLE [dbo].[Giam_gia] (
  [id] int IDENTITY(1,1) NOT NULL,
  [ma_giam_gia] nvarchar(50) NOT NULL,
  [ten_giam_gia] nvarchar(200) NULL,
  [gia_tri_don_toi_thieu] decimal(15,2) NULL,
  [gio_tri_giam] decimal(15,2) NULL,
  [phan_tram_giam] decimal(5,2) NULL,
  [giam_toi_da] decimal(15,2) NULL,
  [so_luong] int NULL,
  [ngay_bat_dau] date NULL,
  [ngay_ket_thuc] date NULL,
  [trang_thai] tinyint NULL,
  [ngay_tao] datetime2(7) NULL,
  CONSTRAINT [PK_Giam_gia] PRIMARY KEY ([id])
);
END
GO
IF NOT EXISTS (SELECT 1 FROM [dbo].[Giam_gia])
BEGIN
SET IDENTITY_INSERT [dbo].[Giam_gia] ON;
INSERT INTO [dbo].[Giam_gia] ([id], [ma_giam_gia], [ten_giam_gia], [gia_tri_don_toi_thieu], [gio_tri_giam], [phan_tram_giam], [giam_toi_da], [so_luong], [ngay_bat_dau], [ngay_ket_thuc], [trang_thai], [ngay_tao]) VALUES
(1, N'ZESTIA10', N'Giảm 10% đơn đầu tiên', 500000.00, NULL, 10.00, 200000.00, 100, '2025-01-01', '2025-12-31', 1, '2026-06-10T23:44:29.303'),
(2, N'FREESHIP', N'Miễn phí vận chuyển', 300000.00, NULL, NULL, NULL, 200, '2025-01-01', '2025-06-30', 1, '2026-06-10T23:44:29.303'),
(3, N'SUMMER20', N'Giảm 20% hè rực rỡ', 1000000.00, NULL, 20.00, 500000.00, 50, '2025-06-01', '2025-08-31', 1, '2026-06-10T23:44:29.303');
SET IDENTITY_INSERT [dbo].[Giam_gia] OFF;
END
GO

-- ===== Vay =====
IF OBJECT_ID(N'dbo.Vay','U') IS NULL
BEGIN
CREATE TABLE [dbo].[Vay] (
  [id] int IDENTITY(1,1) NOT NULL,
  [id_nha_cung_cap] int NULL,
  [id_loai_vay] int NULL,
  [id_chat_lieu] int NULL,
  [id_tai_tro] int NULL,
  [ma_vay] nvarchar(50) NOT NULL,
  [ten_vay] nvarchar(200) NOT NULL,
  [link_youtube] nvarchar(500) NULL,
  [trang_thai] tinyint NULL,
  [mo_ta] nvarchar(max) NULL,
  [ngay_tao] datetime2(7) NULL,
  CONSTRAINT [PK_Vay] PRIMARY KEY ([id])
);
END
GO
IF NOT EXISTS (SELECT 1 FROM [dbo].[Vay])
BEGIN
SET IDENTITY_INSERT [dbo].[Vay] ON;
INSERT INTO [dbo].[Vay] ([id], [id_nha_cung_cap], [id_loai_vay], [id_chat_lieu], [id_tai_tro], [ma_vay], [ten_vay], [link_youtube], [trang_thai], [mo_ta], [ngay_tao]) VALUES
(1, 1, 1, 1, 1, N'VAY001', N'Váy Lụa Tơ Tằm Cổ Điển', NULL, 1, N'Váy lụa tơ tằm mềm mại, phong cách cổ điển sang trọng', '2026-06-10T23:44:29.150'),
(2, 1, 2, 3, 1, N'VAY002', N'Váy Voan Cách Tân Hiện Đại', NULL, 1, N'Váy voan nhẹ nhàng với thiết kế cách tân trẻ trung', '2026-06-10T23:44:29.150'),
(3, 2, 3, 2, 2, N'VAY003', N'Váy Dạ Hội Gấm Hoàng Gia', NULL, 1, N'Váy dạ hội chất liệu gấm cao cấp cho những dịp đặc biệt', '2026-06-10T23:44:29.150'),
(4, 1, 6, 4, 1, N'VAY004', N'Váy Cotton Công Sở Thanh Lịch', NULL, 1, N'Váy cotton thoáng mát, phù hợp môi trường công sở', '2026-06-10T23:44:29.150'),
(5, 2, 2, 5, 3, N'VAY005', N'Váy Đũi Cách Tân Mùa Hè', NULL, 1, N'Váy đũi thoáng mát với thiết kế hiện đại cho mùa hè', '2026-06-10T23:44:29.150'),
(6, 1, 4, 1, 2, N'VAY006', N'Váy Cưới Lụa Trắng Tinh Khôi', NULL, 1, N'Váy cưới lụa tơ tằm trắng tinh tế, thanh lịch', '2026-06-10T23:44:29.150'),
(9, 2, 1, 1, NULL, N'VTT001', N'Váy Lụa Tơ Tằm Hoàng Gia', NULL, 1, N'Váy lụa tơ tằm cao cấp với hoa văn truyền thống, phù hợp cho các dịp lễ hội.', '2026-06-08T13:25:51.276'),
(10, 1, 1, 2, NULL, N'VTT002', N'Váy Truyền Thống Áo Dài Cách Điệu', NULL, 1, N'Áo dài cách điệu với chất liệu voan mềm mại, tạo nên vẻ đẹp duyên dáng.', '2026-04-14T13:25:51.366'),
(11, 1, 1, 3, NULL, N'VTT003', N'Váy Truyền Thống Gấm Đỏ', NULL, 1, N'Váy gấm đỏ truyền thống với đường may tinh tế, toát lên nét đẹp phương Đông.', '2026-06-03T13:25:51.406'),
(12, 2, 1, 4, NULL, N'VTT004', N'Váy Truyền Thống Hoa Văn Cổ', NULL, 1, N'Họa tiết hoa văn cổ điển trên nền vải nhung, mang đậm nét Việt Nam.', '2026-06-09T13:25:51.452'),
(13, 1, 2, 1, NULL, N'VCT001', N'Váy Cách Tân Hiện Đại', NULL, 1, N'Sự kết hợp hoàn hảo giữa phong cách truyền thống và xu hướng hiện đại.', '2026-03-18T13:25:51.483'),
(14, 2, 2, 2, NULL, N'VCT002', N'Váy Cách Tân Phối Ren', NULL, 1, N'Điểm nhấn ren Pháp tinh tế trên nền vải lụa, tôn dáng người mặc.', '2026-04-28T13:25:51.542'),
(15, 2, 2, 5, NULL, N'VCT003', N'Váy Cách Tân Hoa Nhí', NULL, 1, N'Họa tiết hoa nhí tươi trẻ, phù hợp cho các buổi dạo phố và hẹn hò.', '2026-03-22T13:25:51.575'),
(16, 1, 2, 6, NULL, N'VCT004', N'Váy Cách Tân Minimalist', NULL, 1, N'Thiết kế tối giản với đường cắt sắc nét, dành cho phụ nữ hiện đại.', '2026-04-13T13:25:51.595'),
(17, 2, 3, 3, NULL, N'VDH001', N'Váy Dạ Hội Sequin Vàng', NULL, 1, N'Lấp lánh với sequin vàng cao cấp, nổi bật trong mọi bữa tiệc.', '2026-04-08T13:25:51.657'),
(18, 1, 3, 1, NULL, N'VDH002', N'Váy Dạ Hội Đen Huyền Bí', NULL, 1, N'Sự quyến rũ của sắc đen trên nền lụa satin, tạo nên vẻ đẹp bí ẩn.', '2026-03-28T13:25:51.703'),
(19, 2, 3, 2, NULL, N'VDH003', N'Váy Dạ Hội Xẻ Đùi Sang Trọng', NULL, 1, N'Thiết kế xẻ đùi gợi cảm nhưng vẫn giữ được sự thanh lịch.', '2026-05-03T13:25:51.734'),
(20, 2, 3, 4, NULL, N'VDH004', N'Váy Dạ Hội Ren Trắng Ngà', NULL, 1, N'Ren trắng ngà tinh khiết, lý tưởng cho các sự kiện trang trọng.', '2026-04-29T13:25:51.773'),
(21, 1, 4, 6, NULL, N'VCS001', N'Váy Công Sở Thanh Lịch', NULL, 1, N'Thiết kế chuyên nghiệp, thoải mái suốt ngày làm việc.', '2026-05-19T13:25:51.834'),
(22, 2, 4, 5, NULL, N'VCS002', N'Váy Công Sở Body Fit', NULL, 1, N'Ôm body nhẹ nhàng, tôn dáng người mặc trong mọi cuộc họp.', '2026-03-21T13:25:51.899'),
(23, 2, 4, 1, NULL, N'VCS003', N'Váy Công Sở Kẻ Sọc', NULL, 1, N'Họa tiết kẻ sọc cổ điển, phong cách Âu sang trọng.', '2026-05-07T13:25:51.940'),
(24, 2, 4, 2, NULL, N'VCS004', N'Váy Công Sở Chữ A', NULL, 1, N'Dáng chữ A thanh thoát, phù hợp cho nhiều vóc dáng.', '2026-04-14T13:25:51.972'),
(25, 1, 5, 1, NULL, N'VCU001', N'Váy Cưới Lụa Trắng Tinh Khôi', NULL, 1, N'Lụa trắng tinh khôi cho ngày trọng đại của bạn.', '2026-05-13T13:25:51.993'),
(26, 1, 5, 4, NULL, N'VCU002', N'Váy Cưới Ren Pháp Hoàng Gia', NULL, 1, N'Ren Pháp nhập khẩu, thiết kế phong cách hoàng gia.', '2026-06-04T13:25:52.053'),
(27, 1, 5, 2, NULL, N'VCU003', N'Váy Cưới Đuôi Cá Quyến Rũ', NULL, 1, N'Dáng đuôi cá tôn vóc dáng, tạo nên vẻ đẹp quyến rũ.', '2026-04-26T13:25:52.095'),
(28, 2, 5, 3, NULL, N'VCU004', N'Váy Cưới Bohemian Tự Do', NULL, 1, N'Phong cách bohemian lãng mạn cho cô dâu yêu tự do.', '2026-05-21T13:25:52.137'),
(29, 2, 6, 5, NULL, N'VDT001', N'Váy Đi Tiệc Ngắn Trẻ Trung', NULL, 1, N'Thiết kế ngắn trẻ trung, hoàn hảo cho các buổi tiệc tối.', '2026-05-24T13:25:52.196'),
(30, 2, 6, 6, NULL, N'VDT002', N'Váy Đi Tiệc Xòe Công Chúa', NULL, 1, N'Dáng xòe bồng bềnh như công chúa trong câu chuyện cổ tích.', '2026-05-28T13:25:52.219'),
(31, 1, 6, 3, NULL, N'VDT003', N'Váy Đi Tiệc Nhung Xanh', NULL, 1, N'Chất nhung xanh cổ vịt sang trọng, nổi bật trong đêm tiệc.', '2026-05-19T13:25:52.283'),
(32, 2, 6, 1, NULL, N'VDT004', N'Váy Đi Tiệc Metallic Bạc', NULL, 1, N'Ánh metallic bạc hiện đại, thu hút mọi ánh nhìn.', '2026-05-25T13:25:52.335'),
(33, 1, 1, 1, NULL, N'VTT006', N'Váy Truyền Thống Lụa Hồng', NULL, 1, N'Lụa tơ tằm hồng nhạt, thêu hoa sen tinh tế.', '2026-06-11T20:19:36.620'),
(34, 2, 1, 2, NULL, N'VTT007', N'Váy Truyền Thống Gấm Hoàng Gia', NULL, 1, N'Gấm vàng hoàng gia, hoa văn rồng phượng cổ điển.', '2026-06-11T20:19:36.620'),
(35, 1, 1, 5, NULL, N'VTT008', N'Váy Truyền Thống Đũi Tự Nhiên', NULL, 1, N'Chất đũi mềm mại, phong cách mộc mạc thanh lịch.', '2026-06-11T20:19:36.620'),
(36, 2, 1, 3, NULL, N'VTT009', N'Váy Truyền Thống Voan Bay Bổng', NULL, 1, N'Voan mỏng nhẹ bay bổng, họa tiết hoa cúc trắng.', '2026-06-11T20:19:36.620'),
(37, 1, 1, 6, NULL, N'VTT010', N'Váy Truyền Thống Nhung Tím', NULL, 1, N'Nhung tím quý phái, phù hợp dịp lễ hội truyền thống.', '2026-06-11T20:19:36.620'),
(38, 1, 2, 1, NULL, N'VCT007', N'Váy Cách Tân Lụa Ombre', NULL, 1, N'Lụa loang màu ombre từ trắng sang hồng.', '2026-06-11T20:19:36.636'),
(39, 2, 2, 4, NULL, N'VCT008', N'Váy Cách Tân Cotton Phố Cổ', NULL, 1, N'Cotton thoáng mát, in họa tiết phố cổ Hà Nội.', '2026-06-11T20:19:36.636'),
(40, 1, 2, 3, NULL, N'VCT009', N'Váy Cách Tân Voan Tay Phồng', NULL, 1, N'Voan trắng tay phồng, cổ áo mandarin hiện đại.', '2026-06-11T20:19:36.636'),
(41, 2, 2, 2, NULL, N'VCT010', N'Váy Cách Tân Gấm Đỏ Xuân', NULL, 1, N'Gấm đỏ rực rỡ, thiết kế dáng ngắn năng động.', '2026-06-11T20:19:36.636'),
(42, 1, 2, 5, NULL, N'VCT011', N'Váy Cách Tân Đũi Vintage', NULL, 1, N'Đũi nâu vintage, phối ren trắng cổ điển.', '2026-06-11T20:19:36.636'),
(43, 1, 3, 1, NULL, N'VDH006', N'Váy Dạ Hội Sequin Vàng', NULL, 1, N'Lụa phủ sequin vàng lấp lánh, dáng đuôi cá quyến rũ.', '2026-06-11T20:19:36.653'),
(44, 2, 3, 3, NULL, N'VDH007', N'Váy Dạ Hội Voan Nhiều Tầng', NULL, 1, N'Voan xếp tầng bay bổng, tông pastel nhẹ nhàng.', '2026-06-11T20:19:36.653'),
(45, 1, 3, 6, NULL, N'VDH008', N'Váy Dạ Hội Nhung Đen Huyền Bí', NULL, 1, N'Nhung đen sang trọng, xẻ tà cao thanh lịch.', '2026-06-11T20:19:36.653'),
(46, 2, 3, 1, NULL, N'VDH009', N'Váy Dạ Hội Lụa Champagne', NULL, 1, N'Lụa tơ tằm màu champagne, đính pha lê.', '2026-06-11T20:19:36.653'),
(47, 1, 3, 2, NULL, N'VDH010', N'Váy Dạ Hội Gấm Xanh Ngọc', NULL, 1, N'Gấm xanh ngọc lục bảo, hoa văn phượng hoàng.', '2026-06-11T20:19:36.653'),
(48, 1, 4, 3, NULL, N'VC006', N'Váy Cưới Voan Công Chúa', NULL, 1, N'Voan trắng xếp tầng, đính hoa ren 3D lãng mạn.', '2026-06-11T20:19:36.673'),
(49, 2, 4, 1, NULL, N'VC007', N'Váy Cưới Lụa Hoàng Cung', NULL, 1, N'Lụa ngà hoàng cung, thêu chỉ vàng thủ công.', '2026-06-11T20:19:36.673'),
(50, 1, 4, 2, NULL, N'VC008', N'Váy Cưới Gấm Đỏ Truyền Thống', NULL, 1, N'Gấm đỏ truyền thống, cô dâu Việt hiện đại.', '2026-06-11T20:19:36.673'),
(51, 2, 4, 6, NULL, N'VC009', N'Váy Cưới Nhung Trắng Tối Giản', NULL, 1, N'Nhung trắng tối giản, dáng suông thanh lịch.', '2026-06-11T20:19:36.673'),
(52, 1, 4, 3, NULL, N'VC010', N'Váy Cưới Voan Dài Thướt Tha', NULL, 1, N'Voan mỏng nhẹ, tà dài 2m ấn tượng.', '2026-06-11T20:19:36.673'),
(53, 1, 5, 4, NULL, N'VHS005', N'Váy Học Sinh Cotton Sọc', NULL, 1, N'Cotton mát mẻ, sọc xanh navy trẻ trung.', '2026-06-11T20:19:36.690'),
(54, 2, 5, 4, NULL, N'VHS006', N'Váy Học Sinh Cotton Xếp Li', NULL, 1, N'Cotton xếp li cổ điển, kiểu Nhật Bản dễ thương.', '2026-06-11T20:19:36.690'),
(55, 1, 5, 4, NULL, N'VHS007', N'Váy Học Sinh Kẻ Caro', NULL, 1, N'Cotton kẻ caro đỏ, phong cách preppy.', '2026-06-11T20:19:36.690'),
(56, 2, 5, 4, NULL, N'VHS008', N'Váy Học Sinh Thể Thao', NULL, 1, N'Cotton co giãn, thiết kế thể thao khỏe khoắn.', '2026-06-11T20:19:36.690'),
(57, 1, 5, 3, NULL, N'VHS009', N'Váy Học Sinh Voan Nhẹ', NULL, 1, N'Voan nhẹ mát, phù hợp mùa hè.', '2026-06-11T20:19:36.690'),
(58, 1, 6, 1, NULL, N'VCS005', N'Váy Công Sở Lụa Cổ Tim', NULL, 1, N'Lụa xanh navy cổ tim, phom ôm thanh lịch.', '2026-06-11T20:19:36.723'),
(59, 2, 6, 4, NULL, N'VCS006', N'Váy Công Sở Cotton Dáng A', NULL, 1, N'Cotton trắng dáng A, phù hợp mọi vóc dáng.', '2026-06-11T20:19:36.723'),
(60, 1, 6, 3, NULL, N'VCS007', N'Váy Công Sở Voan Xếp Nếp', NULL, 1, N'Voan xếp nếp tôn dáng, tông nude nhã nhặn.', '2026-06-11T20:19:36.723'),
(61, 2, 6, 5, NULL, N'VCS008', N'Váy Công Sở Đũi Cổ Tròn', NULL, 1, N'Đũi nâu nhạt cổ tròn, đơn giản mà tinh tế.', '2026-06-11T20:19:36.723'),
(62, 1, 6, 1, NULL, N'VCS009', N'Váy Công Sở Lụa Đen Sang Trọng', NULL, 1, N'Lụa đen bóng mượt, little black dress hoàn hảo.', '2026-06-11T20:19:36.723');
SET IDENTITY_INSERT [dbo].[Vay] OFF;
END
GO

-- ===== Vay_chi_tiet =====
IF OBJECT_ID(N'dbo.Vay_chi_tiet','U') IS NULL
BEGIN
CREATE TABLE [dbo].[Vay_chi_tiet] (
  [id] int IDENTITY(1,1) NOT NULL,
  [id_vay] int NOT NULL,
  [id_mau_sac] int NULL,
  [id_kich_thuoc] int NULL,
  [id_khuyen_mai] int NULL,
  [ma_vay_chi_tiet] nvarchar(80) NULL,
  [gia_ban_goc] decimal(15,2) NOT NULL,
  [gia_ban] decimal(15,2) NOT NULL,
  [phan_tram_giam] decimal(5,2) NULL,
  [so_luong] int NULL,
  [anh_url] nvarchar(500) NULL,
  [trang_thai] tinyint NULL,
  [ngay_tao] datetime2(7) NULL,
  CONSTRAINT [PK_Vay_chi_tiet] PRIMARY KEY ([id])
);
END
GO
IF NOT EXISTS (SELECT 1 FROM [dbo].[Vay_chi_tiet])
BEGIN
SET IDENTITY_INSERT [dbo].[Vay_chi_tiet] ON;
INSERT INTO [dbo].[Vay_chi_tiet] ([id], [id_vay], [id_mau_sac], [id_kich_thuoc], [id_khuyen_mai], [ma_vay_chi_tiet], [gia_ban_goc], [gia_ban], [phan_tram_giam], [so_luong], [anh_url], [trang_thai], [ngay_tao]) VALUES
(1, 1, 4, 1, NULL, N'VAY001-TRANG-S', 2890000.00, 2890000.00, 0.00, 15, NULL, 1, '2026-06-10T23:44:29.170'),
(2, 1, 4, 2, NULL, N'VAY001-TRANG-M', 2890000.00, 2890000.00, 0.00, 20, NULL, 1, '2026-06-10T23:44:29.170'),
(3, 1, 4, 3, NULL, N'VAY001-TRANG-L', 2890000.00, 2890000.00, 0.00, 10, NULL, 1, '2026-06-10T23:44:29.170'),
(4, 1, 5, 1, NULL, N'VAY001-DEN-S', 2890000.00, 2890000.00, 0.00, 12, NULL, 1, '2026-06-10T23:44:29.170'),
(5, 1, 5, 2, NULL, N'VAY001-DEN-M', 2890000.00, 2890000.00, 0.00, 18, NULL, 1, '2026-06-10T23:44:29.170'),
(6, 2, 6, 2, 1, N'VAY002-HONG-M', 1990000.00, 1590000.00, 20.00, 25, NULL, 1, '2026-06-10T23:44:29.170'),
(7, 2, 6, 3, 1, N'VAY002-HONG-L', 1990000.00, 1590000.00, 20.00, 20, NULL, 1, '2026-06-10T23:44:29.170'),
(8, 2, 7, 2, 1, N'VAY002-TIM-M', 1990000.00, 1790000.00, 20.00, 15, NULL, 1, '2026-06-10T23:44:29.170'),
(9, 3, 1, 2, 2, N'VAY003-DO-M', 5490000.00, 4290000.00, 15.00, 8, NULL, 1, '2026-06-10T23:44:29.170'),
(10, 3, 1, 3, 2, N'VAY003-DO-L', 5490000.00, 4290000.00, 15.00, 5, NULL, 1, '2026-06-10T23:44:29.170'),
(11, 3, 3, 2, 2, N'VAY003-VANG-M', 5490000.00, 4890000.00, 15.00, 6, NULL, 1, '2026-06-10T23:44:29.170'),
(12, 4, 4, 1, NULL, N'VAY004-TRANG-S', 1290000.00, 1290000.00, 0.00, 30, NULL, 1, '2026-06-10T23:44:29.170'),
(13, 4, 4, 2, NULL, N'VAY004-TRANG-M', 1290000.00, 1290000.00, 0.00, 35, NULL, 1, '2026-06-10T23:44:29.170'),
(14, 4, 2, 2, NULL, N'VAY004-NAVY-M', 1290000.00, 1290000.00, 0.00, 25, NULL, 1, '2026-06-10T23:44:29.170'),
(15, 5, 8, 2, NULL, N'VAY005-XANHLA-M', 1690000.00, 1390000.00, 0.00, 20, NULL, 1, '2026-06-10T23:44:29.170'),
(16, 5, 8, 3, NULL, N'VAY005-XANHLA-L', 1690000.00, 1390000.00, 0.00, 15, NULL, 1, '2026-06-10T23:44:29.170'),
(17, 6, 4, 2, NULL, N'VAY006-TRANG-M', 8990000.00, 8990000.00, 0.00, 3, NULL, 1, '2026-06-10T23:44:29.170'),
(18, 6, 4, 3, NULL, N'VAY006-TRANG-L', 8990000.00, 8990000.00, 0.00, 2, NULL, 1, '2026-06-10T23:44:29.170'),
(20, 9, 1, 1, NULL, N'VTT001-001', 3890000.00, 3890000.00, NULL, 21, NULL, 1, '2026-06-11T13:25:51.307'),
(21, 9, 1, 4, NULL, N'VTT001-002', 3890000.00, 3890000.00, NULL, 15, NULL, 1, '2026-06-11T13:25:51.322'),
(22, 9, 1, 6, NULL, N'VTT001-003', 3890000.00, 3890000.00, NULL, 8, NULL, 1, '2026-06-11T13:25:51.328'),
(23, 9, 1, 5, NULL, N'VTT001-004', 3890000.00, 3890000.00, NULL, 14, NULL, 1, '2026-06-11T13:25:51.328'),
(24, 9, 1, 2, NULL, N'VTT001-005', 3890000.00, 3890000.00, NULL, 5, NULL, 1, '2026-06-11T13:25:51.339'),
(25, 9, 2, 1, NULL, N'VTT001-006', 3890000.00, 3890000.00, NULL, 8, NULL, 1, '2026-06-11T13:25:51.339'),
(26, 9, 2, 4, NULL, N'VTT001-007', 3890000.00, 3890000.00, NULL, 11, NULL, 1, '2026-06-11T13:25:51.349'),
(27, 9, 2, 6, NULL, N'VTT001-008', 3890000.00, 3890000.00, NULL, 18, NULL, 1, '2026-06-11T13:25:51.355'),
(28, 9, 2, 5, NULL, N'VTT001-009', 3890000.00, 3890000.00, NULL, 8, NULL, 1, '2026-06-11T13:25:51.360'),
(29, 9, 2, 2, NULL, N'VTT001-010', 3890000.00, 3890000.00, NULL, 6, NULL, 1, '2026-06-11T13:25:51.360'),
(30, 10, 2, 1, NULL, N'VTT002-001', 2690000.00, 2690000.00, NULL, 8, NULL, 1, '2026-06-11T13:25:51.368'),
(31, 10, 2, 5, NULL, N'VTT002-002', 2690000.00, 2690000.00, NULL, 22, NULL, 1, '2026-06-11T13:25:51.371'),
(32, 10, 2, 3, NULL, N'VTT002-003', 2690000.00, 2690000.00, NULL, 20, NULL, 1, '2026-06-11T13:25:51.371'),
(33, 10, 8, 1, NULL, N'VTT002-004', 2690000.00, 2690000.00, NULL, 19, NULL, 1, '2026-06-11T13:25:51.381'),
(34, 10, 8, 5, NULL, N'VTT002-005', 2690000.00, 2690000.00, NULL, 12, NULL, 1, '2026-06-11T13:25:51.387'),
(35, 10, 8, 3, NULL, N'VTT002-006', 2690000.00, 2690000.00, NULL, 20, NULL, 1, '2026-06-11T13:25:51.391'),
(36, 10, 6, 1, NULL, N'VTT002-007', 2690000.00, 2690000.00, NULL, 22, NULL, 1, '2026-06-11T13:25:51.394'),
(37, 10, 6, 5, NULL, N'VTT002-008', 2690000.00, 2690000.00, NULL, 8, NULL, 1, '2026-06-11T13:25:51.394'),
(38, 10, 6, 3, NULL, N'VTT002-009', 2690000.00, 2690000.00, NULL, 18, NULL, 1, '2026-06-11T13:25:51.402'),
(39, 11, 8, 1, NULL, N'VTT003-001', 4290000.00, 3590000.00, NULL, 24, NULL, 1, '2026-06-11T13:25:51.410'),
(40, 11, 8, 5, NULL, N'VTT003-002', 4290000.00, 3590000.00, NULL, 20, NULL, 1, '2026-06-11T13:25:51.412'),
(41, 11, 8, 6, NULL, N'VTT003-003', 4290000.00, 3590000.00, NULL, 24, NULL, 1, '2026-06-11T13:25:51.412'),
(42, 11, 8, 4, NULL, N'VTT003-004', 4290000.00, 3590000.00, NULL, 15, NULL, 1, '2026-06-11T13:25:51.412'),
(43, 11, 8, 2, NULL, N'VTT003-005', 4290000.00, 3590000.00, NULL, 6, NULL, 1, '2026-06-11T13:25:51.420'),
(44, 11, 4, 1, NULL, N'VTT003-006', 4290000.00, 3590000.00, NULL, 18, NULL, 1, '2026-06-11T13:25:51.422'),
(45, 11, 4, 5, NULL, N'VTT003-007', 4290000.00, 3590000.00, NULL, 14, NULL, 1, '2026-06-11T13:25:51.422'),
(46, 11, 4, 6, NULL, N'VTT003-008', 4290000.00, 3590000.00, NULL, 18, NULL, 1, '2026-06-11T13:25:51.422'),
(47, 11, 4, 4, NULL, N'VTT003-009', 4290000.00, 3590000.00, NULL, 18, NULL, 1, '2026-06-11T13:25:51.432'),
(48, 11, 4, 2, NULL, N'VTT003-010', 4290000.00, 3590000.00, NULL, 11, NULL, 1, '2026-06-11T13:25:51.434'),
(49, 11, 5, 1, NULL, N'VTT003-011', 4290000.00, 3590000.00, NULL, 5, NULL, 1, '2026-06-11T13:25:51.442'),
(50, 11, 5, 5, NULL, N'VTT003-012', 4290000.00, 3590000.00, NULL, 10, NULL, 1, '2026-06-11T13:25:51.442'),
(51, 11, 5, 6, NULL, N'VTT003-013', 4290000.00, 3590000.00, NULL, 18, NULL, 1, '2026-06-11T13:25:51.450'),
(52, 11, 5, 4, NULL, N'VTT003-014', 4290000.00, 3590000.00, NULL, 5, NULL, 1, '2026-06-11T13:25:51.452'),
(53, 11, 5, 2, NULL, N'VTT003-015', 4290000.00, 3590000.00, NULL, 19, NULL, 1, '2026-06-11T13:25:51.452'),
(54, 12, 4, 2, NULL, N'VTT004-001', 3190000.00, 3190000.00, NULL, 21, NULL, 1, '2026-06-11T13:25:51.460'),
(55, 12, 4, 3, NULL, N'VTT004-002', 3190000.00, 3190000.00, NULL, 13, NULL, 1, '2026-06-11T13:25:51.462'),
(56, 12, 4, 1, NULL, N'VTT004-003', 3190000.00, 3190000.00, NULL, 15, NULL, 1, '2026-06-11T13:25:51.466'),
(57, 12, 6, 2, NULL, N'VTT004-004', 3190000.00, 3190000.00, NULL, 5, NULL, 1, '2026-06-11T13:25:51.466'),
(58, 12, 6, 3, NULL, N'VTT004-005', 3190000.00, 3190000.00, NULL, 17, NULL, 1, '2026-06-11T13:25:51.470'),
(59, 12, 6, 1, NULL, N'VTT004-006', 3190000.00, 3190000.00, NULL, 14, NULL, 1, '2026-06-11T13:25:51.472'),
(60, 12, 1, 2, NULL, N'VTT004-007', 3190000.00, 3190000.00, NULL, 8, NULL, 1, '2026-06-11T13:25:51.472'),
(61, 12, 1, 3, NULL, N'VTT004-008', 3190000.00, 3190000.00, NULL, 16, NULL, 1, '2026-06-11T13:25:51.481'),
(62, 12, 1, 1, NULL, N'VTT004-009', 3190000.00, 3190000.00, NULL, 14, NULL, 1, '2026-06-11T13:25:51.483'),
(63, 13, 8, 4, NULL, N'VCT001-001', 2490000.00, 2490000.00, NULL, 8, NULL, 1, '2026-06-11T13:25:51.483'),
(64, 13, 8, 2, NULL, N'VCT001-002', 2490000.00, 2490000.00, NULL, 17, NULL, 1, '2026-06-11T13:25:51.493'),
(65, 13, 8, 1, NULL, N'VCT001-003', 2490000.00, 2490000.00, NULL, 17, NULL, 1, '2026-06-11T13:25:51.493'),
(66, 13, 8, 6, NULL, N'VCT001-004', 2490000.00, 2490000.00, NULL, 7, NULL, 1, '2026-06-11T13:25:51.497'),
(67, 13, 8, 5, NULL, N'VCT001-005', 2490000.00, 2490000.00, NULL, 8, NULL, 1, '2026-06-11T13:25:51.501'),
(68, 13, 1, 4, NULL, N'VCT001-006', 2490000.00, 2490000.00, NULL, 18, NULL, 1, '2026-06-11T13:25:51.503'),
(69, 13, 1, 2, NULL, N'VCT001-007', 2490000.00, 2490000.00, NULL, 7, NULL, 1, '2026-06-11T13:25:51.503'),
(70, 13, 1, 1, NULL, N'VCT001-008', 2490000.00, 2490000.00, NULL, 17, NULL, 1, '2026-06-11T13:25:51.503'),
(71, 13, 1, 6, NULL, N'VCT001-009', 2490000.00, 2490000.00, NULL, 17, NULL, 1, '2026-06-11T13:25:51.511'),
(72, 13, 1, 5, NULL, N'VCT001-010', 2490000.00, 2490000.00, NULL, 23, NULL, 1, '2026-06-11T13:25:51.511'),
(73, 13, 4, 4, NULL, N'VCT001-011', 2490000.00, 2490000.00, NULL, 17, NULL, 1, '2026-06-11T13:25:51.513'),
(74, 13, 4, 2, NULL, N'VCT001-012', 2490000.00, 2490000.00, NULL, 17, NULL, 1, '2026-06-11T13:25:51.513'),
(75, 13, 4, 1, NULL, N'VCT001-013', 2490000.00, 2490000.00, NULL, 13, NULL, 1, '2026-06-11T13:25:51.522'),
(76, 13, 4, 6, NULL, N'VCT001-014', 2490000.00, 2490000.00, NULL, 10, NULL, 1, '2026-06-11T13:25:51.524'),
(77, 13, 4, 5, NULL, N'VCT001-015', 2490000.00, 2490000.00, NULL, 7, NULL, 1, '2026-06-11T13:25:51.524'),
(78, 13, 5, 4, NULL, N'VCT001-016', 2490000.00, 2490000.00, NULL, 12, NULL, 1, '2026-06-11T13:25:51.529'),
(79, 13, 5, 2, NULL, N'VCT001-017', 2490000.00, 2490000.00, NULL, 18, NULL, 1, '2026-06-11T13:25:51.532'),
(80, 13, 5, 1, NULL, N'VCT001-018', 2490000.00, 2490000.00, NULL, 18, NULL, 1, '2026-06-11T13:25:51.534'),
(81, 13, 5, 6, NULL, N'VCT001-019', 2490000.00, 2490000.00, NULL, 12, NULL, 1, '2026-06-11T13:25:51.534'),
(82, 13, 5, 5, NULL, N'VCT001-020', 2490000.00, 2490000.00, NULL, 6, NULL, 1, '2026-06-11T13:25:51.534'),
(83, 14, 7, 1, NULL, N'VCT002-001', 2890000.00, 2290000.00, NULL, 6, NULL, 1, '2026-06-11T13:25:51.544'),
(84, 14, 7, 6, NULL, N'VCT002-002', 2890000.00, 2290000.00, NULL, 9, NULL, 1, '2026-06-11T13:25:51.545'),
(85, 14, 7, 4, NULL, N'VCT002-003', 2890000.00, 2290000.00, NULL, 21, NULL, 1, '2026-06-11T13:25:51.549'),
(86, 14, 5, 1, NULL, N'VCT002-004', 2890000.00, 2290000.00, NULL, 5, NULL, 1, '2026-06-11T13:25:51.552'),
(87, 14, 5, 6, NULL, N'VCT002-005', 2890000.00, 2290000.00, NULL, 15, NULL, 1, '2026-06-11T13:25:51.554'),
(88, 14, 5, 4, NULL, N'VCT002-006', 2890000.00, 2290000.00, NULL, 10, NULL, 1, '2026-06-11T13:25:51.554'),
(89, 14, 3, 1, NULL, N'VCT002-007', 2890000.00, 2290000.00, NULL, 15, NULL, 1, '2026-06-11T13:25:51.554'),
(90, 14, 3, 6, NULL, N'VCT002-008', 2890000.00, 2290000.00, NULL, 15, NULL, 1, '2026-06-11T13:25:51.561'),
(91, 14, 3, 4, NULL, N'VCT002-009', 2890000.00, 2290000.00, NULL, 14, NULL, 1, '2026-06-11T13:25:51.562'),
(92, 14, 2, 1, NULL, N'VCT002-010', 2890000.00, 2290000.00, NULL, 14, NULL, 1, '2026-06-11T13:25:51.564'),
(93, 14, 2, 6, NULL, N'VCT002-011', 2890000.00, 2290000.00, NULL, 16, NULL, 1, '2026-06-11T13:25:51.564'),
(94, 14, 2, 4, NULL, N'VCT002-012', 2890000.00, 2290000.00, NULL, 9, NULL, 1, '2026-06-11T13:25:51.569'),
(95, 15, 7, 4, NULL, N'VCT003-001', 1890000.00, 1890000.00, NULL, 10, NULL, 1, '2026-06-11T13:25:51.577'),
(96, 15, 7, 2, NULL, N'VCT003-002', 1890000.00, 1890000.00, NULL, 17, NULL, 1, '2026-06-11T13:25:51.580'),
(97, 15, 7, 6, NULL, N'VCT003-003', 1890000.00, 1890000.00, NULL, 16, NULL, 1, '2026-06-11T13:25:51.583'),
(98, 15, 7, 1, NULL, N'VCT003-004', 1890000.00, 1890000.00, NULL, 23, NULL, 1, '2026-06-11T13:25:51.585'),
(99, 15, 2, 4, NULL, N'VCT003-005', 1890000.00, 1890000.00, NULL, 18, NULL, 1, '2026-06-11T13:25:51.585'),
(100, 15, 2, 2, NULL, N'VCT003-006', 1890000.00, 1890000.00, NULL, 11, NULL, 1, '2026-06-11T13:25:51.585'),
(101, 15, 2, 6, NULL, N'VCT003-007', 1890000.00, 1890000.00, NULL, 23, NULL, 1, '2026-06-11T13:25:51.585'),
(102, 15, 2, 1, NULL, N'VCT003-008', 1890000.00, 1890000.00, NULL, 11, NULL, 1, '2026-06-11T13:25:51.593'),
(103, 16, 1, 4, NULL, N'VCT004-001', 2190000.00, 2190000.00, NULL, 18, NULL, 1, '2026-06-11T13:25:51.595'),
(104, 16, 1, 5, NULL, N'VCT004-002', 2190000.00, 2190000.00, NULL, 13, NULL, 1, '2026-06-11T13:25:51.604'),
(105, 16, 1, 3, NULL, N'VCT004-003', 2190000.00, 2190000.00, NULL, 13, NULL, 1, '2026-06-11T13:25:51.604'),
(106, 16, 1, 6, NULL, N'VCT004-004', 2190000.00, 2190000.00, NULL, 19, NULL, 1, '2026-06-11T13:25:51.606'),
(107, 16, 7, 4, NULL, N'VCT004-005', 2190000.00, 2190000.00, NULL, 10, NULL, 1, '2026-06-11T13:25:51.608'),
(108, 16, 7, 5, NULL, N'VCT004-006', 2190000.00, 2190000.00, NULL, 24, NULL, 1, '2026-06-11T13:25:51.614'),
(109, 16, 7, 3, NULL, N'VCT004-007', 2190000.00, 2190000.00, NULL, 16, NULL, 1, '2026-06-11T13:25:51.616'),
(110, 16, 7, 6, NULL, N'VCT004-008', 2190000.00, 2190000.00, NULL, 8, NULL, 1, '2026-06-11T13:25:51.616'),
(111, 16, 6, 4, NULL, N'VCT004-009', 2190000.00, 2190000.00, NULL, 8, NULL, 1, '2026-06-11T13:25:51.616'),
(112, 16, 6, 5, NULL, N'VCT004-010', 2190000.00, 2190000.00, NULL, 20, NULL, 1, '2026-06-11T13:25:51.624'),
(113, 16, 6, 3, NULL, N'VCT004-011', 2190000.00, 2190000.00, NULL, 6, NULL, 1, '2026-06-11T13:25:51.626'),
(114, 16, 6, 6, NULL, N'VCT004-012', 2190000.00, 2190000.00, NULL, 16, NULL, 1, '2026-06-11T13:25:51.634'),
(115, 16, 5, 4, NULL, N'VCT004-013', 2190000.00, 2190000.00, NULL, 8, NULL, 1, '2026-06-11T13:25:51.640'),
(116, 16, 5, 5, NULL, N'VCT004-014', 2190000.00, 2190000.00, NULL, 18, NULL, 1, '2026-06-11T13:25:51.645'),
(117, 16, 5, 3, NULL, N'VCT004-015', 2190000.00, 2190000.00, NULL, 18, NULL, 1, '2026-06-11T13:25:51.647'),
(118, 16, 5, 6, NULL, N'VCT004-016', 2190000.00, 2190000.00, NULL, 21, NULL, 1, '2026-06-11T13:25:51.647'),
(119, 17, 3, 3, NULL, N'VDH001-001', 6490000.00, 6490000.00, NULL, 15, NULL, 1, '2026-06-11T13:25:51.666'),
(120, 17, 3, 4, NULL, N'VDH001-002', 6490000.00, 6490000.00, NULL, 13, NULL, 1, '2026-06-11T13:25:51.668'),
(121, 17, 3, 5, NULL, N'VDH001-003', 6490000.00, 6490000.00, NULL, 6, NULL, 1, '2026-06-11T13:25:51.671'),
(122, 17, 7, 3, NULL, N'VDH001-004', 6490000.00, 6490000.00, NULL, 6, NULL, 1, '2026-06-11T13:25:51.677'),
(123, 17, 7, 4, NULL, N'VDH001-005', 6490000.00, 6490000.00, NULL, 16, NULL, 1, '2026-06-11T13:25:51.679'),
(124, 17, 7, 5, NULL, N'VDH001-006', 6490000.00, 6490000.00, NULL, 15, NULL, 1, '2026-06-11T13:25:51.679'),
(125, 17, 5, 3, NULL, N'VDH001-007', 6490000.00, 6490000.00, NULL, 20, NULL, 1, '2026-06-11T13:25:51.687'),
(126, 17, 5, 4, NULL, N'VDH001-008', 6490000.00, 6490000.00, NULL, 18, NULL, 1, '2026-06-11T13:25:51.693'),
(127, 17, 5, 5, NULL, N'VDH001-009', 6490000.00, 6490000.00, NULL, 10, NULL, 1, '2026-06-11T13:25:51.693'),
(128, 18, 7, 1, NULL, N'VDH002-001', 5890000.00, 4890000.00, NULL, 24, NULL, 1, '2026-06-11T13:25:51.709'),
(129, 18, 7, 2, NULL, N'VDH002-002', 5890000.00, 4890000.00, NULL, 11, NULL, 1, '2026-06-11T13:25:51.713'),
(130, 18, 7, 4, NULL, N'VDH002-003', 5890000.00, 4890000.00, NULL, 19, NULL, 1, '2026-06-11T13:25:51.714'),
(131, 18, 8, 1, NULL, N'VDH002-004', 5890000.00, 4890000.00, NULL, 5, NULL, 1, '2026-06-11T13:25:51.721'),
(132, 18, 8, 2, NULL, N'VDH002-005', 5890000.00, 4890000.00, NULL, 13, NULL, 1, '2026-06-11T13:25:51.721'),
(133, 18, 8, 4, NULL, N'VDH002-006', 5890000.00, 4890000.00, NULL, 16, NULL, 1, '2026-06-11T13:25:51.731'),
(134, 19, 3, 3, NULL, N'VDH003-001', 7290000.00, 7290000.00, NULL, 5, NULL, 1, '2026-06-11T13:25:51.741'),
(135, 19, 3, 4, NULL, N'VDH003-002', 7290000.00, 7290000.00, NULL, 23, NULL, 1, '2026-06-11T13:25:51.743'),
(136, 19, 3, 6, NULL, N'VDH003-003', 7290000.00, 7290000.00, NULL, 24, NULL, 1, '2026-06-11T13:25:51.743'),
(137, 19, 3, 2, NULL, N'VDH003-004', 7290000.00, 7290000.00, NULL, 7, NULL, 1, '2026-06-11T13:25:51.752'),
(138, 19, 6, 3, NULL, N'VDH003-005', 7290000.00, 7290000.00, NULL, 12, NULL, 1, '2026-06-11T13:25:51.753'),
(139, 19, 6, 4, NULL, N'VDH003-006', 7290000.00, 7290000.00, NULL, 22, NULL, 1, '2026-06-11T13:25:51.760'),
(140, 19, 6, 6, NULL, N'VDH003-007', 7290000.00, 7290000.00, NULL, 8, NULL, 1, '2026-06-11T13:25:51.763'),
(141, 19, 6, 2, NULL, N'VDH003-008', 7290000.00, 7290000.00, NULL, 20, NULL, 1, '2026-06-11T13:25:51.765'),
(142, 20, 8, 4, NULL, N'VDH004-001', 5490000.00, 4590000.00, NULL, 8, NULL, 1, '2026-06-11T13:25:51.773'),
(143, 20, 8, 5, NULL, N'VDH004-002', 5490000.00, 4590000.00, NULL, 19, NULL, 1, '2026-06-11T13:25:51.781'),
(144, 20, 8, 2, NULL, N'VDH004-003', 5490000.00, 4590000.00, NULL, 20, NULL, 1, '2026-06-11T13:25:51.784'),
(145, 20, 8, 6, NULL, N'VDH004-004', 5490000.00, 4590000.00, NULL, 18, NULL, 1, '2026-06-11T13:25:51.784'),
(146, 20, 6, 4, NULL, N'VDH004-005', 5490000.00, 4590000.00, NULL, 10, NULL, 1, '2026-06-11T13:25:51.794'),
(147, 20, 6, 5, NULL, N'VDH004-006', 5490000.00, 4590000.00, NULL, 5, NULL, 1, '2026-06-11T13:25:51.797'),
(148, 20, 6, 2, NULL, N'VDH004-007', 5490000.00, 4590000.00, NULL, 12, NULL, 1, '2026-06-11T13:25:51.802'),
(149, 20, 6, 6, NULL, N'VDH004-008', 5490000.00, 4590000.00, NULL, 10, NULL, 1, '2026-06-11T13:25:51.804'),
(150, 20, 7, 4, NULL, N'VDH004-009', 5490000.00, 4590000.00, NULL, 19, NULL, 1, '2026-06-11T13:25:51.804'),
(151, 20, 7, 5, NULL, N'VDH004-010', 5490000.00, 4590000.00, NULL, 14, NULL, 1, '2026-06-11T13:25:51.804'),
(152, 20, 7, 2, NULL, N'VDH004-011', 5490000.00, 4590000.00, NULL, 13, NULL, 1, '2026-06-11T13:25:51.813'),
(153, 20, 7, 6, NULL, N'VDH004-012', 5490000.00, 4590000.00, NULL, 23, NULL, 1, '2026-06-11T13:25:51.816'),
(154, 20, 2, 4, NULL, N'VDH004-013', 5490000.00, 4590000.00, NULL, 14, NULL, 1, '2026-06-11T13:25:51.816'),
(155, 20, 2, 5, NULL, N'VDH004-014', 5490000.00, 4590000.00, NULL, 6, NULL, 1, '2026-06-11T13:25:51.821'),
(156, 20, 2, 2, NULL, N'VDH004-015', 5490000.00, 4590000.00, NULL, 11, NULL, 1, '2026-06-11T13:25:51.825'),
(157, 20, 2, 6, NULL, N'VDH004-016', 5490000.00, 4590000.00, NULL, 7, NULL, 1, '2026-06-11T13:25:51.826'),
(158, 21, 7, 5, NULL, N'VCS001-001', 1690000.00, 1690000.00, NULL, 7, NULL, 1, '2026-06-11T13:25:51.836'),
(159, 21, 7, 6, NULL, N'VCS001-002', 1690000.00, 1690000.00, NULL, 18, NULL, 1, '2026-06-11T13:25:51.845'),
(160, 21, 7, 4, NULL, N'VCS001-003', 1690000.00, 1690000.00, NULL, 24, NULL, 1, '2026-06-11T13:25:51.846'),
(161, 21, 7, 2, NULL, N'VCS001-004', 1690000.00, 1690000.00, NULL, 23, NULL, 1, '2026-06-11T13:25:51.854'),
(162, 21, 6, 5, NULL, N'VCS001-005', 1690000.00, 1690000.00, NULL, 9, NULL, 1, '2026-06-11T13:25:51.857'),
(163, 21, 6, 6, NULL, N'VCS001-006', 1690000.00, 1690000.00, NULL, 22, NULL, 1, '2026-06-11T13:25:51.867'),
(164, 21, 6, 4, NULL, N'VCS001-007', 1690000.00, 1690000.00, NULL, 6, NULL, 1, '2026-06-11T13:25:51.869'),
(165, 21, 6, 2, NULL, N'VCS001-008', 1690000.00, 1690000.00, NULL, 21, NULL, 1, '2026-06-11T13:25:51.869'),
(166, 21, 8, 5, NULL, N'VCS001-009', 1690000.00, 1690000.00, NULL, 5, NULL, 1, '2026-06-11T13:25:51.877'),
(167, 21, 8, 6, NULL, N'VCS001-010', 1690000.00, 1690000.00, NULL, 14, NULL, 1, '2026-06-11T13:25:51.880'),
(168, 21, 8, 4, NULL, N'VCS001-011', 1690000.00, 1690000.00, NULL, 11, NULL, 1, '2026-06-11T13:25:51.880'),
(169, 21, 8, 2, NULL, N'VCS001-012', 1690000.00, 1690000.00, NULL, 17, NULL, 1, '2026-06-11T13:25:51.886'),
(170, 21, 3, 5, NULL, N'VCS001-013', 1690000.00, 1690000.00, NULL, 12, NULL, 1, '2026-06-11T13:25:51.888'),
(171, 21, 3, 6, NULL, N'VCS001-014', 1690000.00, 1690000.00, NULL, 17, NULL, 1, '2026-06-11T13:25:51.888'),
(172, 21, 3, 4, NULL, N'VCS001-015', 1690000.00, 1690000.00, NULL, 8, NULL, 1, '2026-06-11T13:25:51.893'),
(173, 21, 3, 2, NULL, N'VCS001-016', 1690000.00, 1690000.00, NULL, 22, NULL, 1, '2026-06-11T13:25:51.897'),
(174, 22, 8, 1, NULL, N'VCS002-001', 1890000.00, 1490000.00, NULL, 10, NULL, 1, '2026-06-11T13:25:51.907'),
(175, 22, 8, 5, NULL, N'VCS002-002', 1890000.00, 1490000.00, NULL, 13, NULL, 1, '2026-06-11T13:25:51.909'),
(176, 22, 8, 2, NULL, N'VCS002-003', 1890000.00, 1490000.00, NULL, 7, NULL, 1, '2026-06-11T13:25:51.909'),
(177, 22, 2, 1, NULL, N'VCS002-004', 1890000.00, 1490000.00, NULL, 17, NULL, 1, '2026-06-11T13:25:51.918'),
(178, 22, 2, 5, NULL, N'VCS002-005', 1890000.00, 1490000.00, NULL, 17, NULL, 1, '2026-06-11T13:25:51.920'),
(179, 22, 2, 2, NULL, N'VCS002-006', 1890000.00, 1490000.00, NULL, 10, NULL, 1, '2026-06-11T13:25:51.925'),
(180, 22, 5, 1, NULL, N'VCS002-007', 1890000.00, 1490000.00, NULL, 7, NULL, 1, '2026-06-11T13:25:51.930'),
(181, 22, 5, 5, NULL, N'VCS002-008', 1890000.00, 1490000.00, NULL, 12, NULL, 1, '2026-06-11T13:25:51.930'),
(182, 22, 5, 2, NULL, N'VCS002-009', 1890000.00, 1490000.00, NULL, 17, NULL, 1, '2026-06-11T13:25:51.930'),
(183, 23, 7, 3, NULL, N'VCS003-001', 1790000.00, 1790000.00, NULL, 8, NULL, 1, '2026-06-11T13:25:51.940'),
(184, 23, 7, 5, NULL, N'VCS003-002', 1790000.00, 1790000.00, NULL, 23, NULL, 1, '2026-06-11T13:25:51.951'),
(185, 23, 7, 4, NULL, N'VCS003-003', 1790000.00, 1790000.00, NULL, 18, NULL, 1, '2026-06-11T13:25:51.951'),
(186, 23, 7, 2, NULL, N'VCS003-004', 1790000.00, 1790000.00, NULL, 13, NULL, 1, '2026-06-11T13:25:51.956'),
(187, 23, 7, 6, NULL, N'VCS003-005', 1790000.00, 1790000.00, NULL, 6, NULL, 1, '2026-06-11T13:25:51.959'),
(188, 23, 4, 3, NULL, N'VCS003-006', 1790000.00, 1790000.00, NULL, 15, NULL, 1, '2026-06-11T13:25:51.961'),
(189, 23, 4, 5, NULL, N'VCS003-007', 1790000.00, 1790000.00, NULL, 7, NULL, 1, '2026-06-11T13:25:51.964'),
(190, 23, 4, 4, NULL, N'VCS003-008', 1790000.00, 1790000.00, NULL, 5, NULL, 1, '2026-06-11T13:25:51.966'),
(191, 23, 4, 2, NULL, N'VCS003-009', 1790000.00, 1790000.00, NULL, 16, NULL, 1, '2026-06-11T13:25:51.969'),
(192, 23, 4, 6, NULL, N'VCS003-010', 1790000.00, 1790000.00, NULL, 22, NULL, 1, '2026-06-11T13:25:51.972'),
(193, 24, 1, 4, NULL, N'VCS004-001', 1990000.00, 1990000.00, NULL, 18, NULL, 1, '2026-06-11T13:25:51.980'),
(194, 24, 1, 6, NULL, N'VCS004-002', 1990000.00, 1990000.00, NULL, 19, NULL, 1, '2026-06-11T13:25:51.983'),
(195, 24, 1, 5, NULL, N'VCS004-003', 1990000.00, 1990000.00, NULL, 14, NULL, 1, '2026-06-11T13:25:51.984'),
(196, 24, 4, 4, NULL, N'VCS004-004', 1990000.00, 1990000.00, NULL, 16, NULL, 1, '2026-06-11T13:25:51.987'),
(197, 24, 4, 6, NULL, N'VCS004-005', 1990000.00, 1990000.00, NULL, 13, NULL, 1, '2026-06-11T13:25:51.991'),
(198, 24, 4, 5, NULL, N'VCS004-006', 1990000.00, 1990000.00, NULL, 11, NULL, 1, '2026-06-11T13:25:51.993'),
(199, 25, 4, 5, NULL, N'VCU001-001', 8990000.00, 8990000.00, NULL, 23, NULL, 1, '2026-06-11T13:25:52.001'),
(200, 25, 4, 4, NULL, N'VCU001-002', 8990000.00, 8990000.00, NULL, 5, NULL, 1, '2026-06-11T13:25:52.003'),
(201, 25, 4, 1, NULL, N'VCU001-003', 8990000.00, 8990000.00, NULL, 23, NULL, 1, '2026-06-11T13:25:52.003');
INSERT INTO [dbo].[Vay_chi_tiet] ([id], [id_vay], [id_mau_sac], [id_kich_thuoc], [id_khuyen_mai], [ma_vay_chi_tiet], [gia_ban_goc], [gia_ban], [phan_tram_giam], [so_luong], [anh_url], [trang_thai], [ngay_tao]) VALUES
(202, 25, 4, 6, NULL, N'VCU001-004', 8990000.00, 8990000.00, NULL, 22, NULL, 1, '2026-06-11T13:25:52.003'),
(203, 25, 4, 2, NULL, N'VCU001-005', 8990000.00, 8990000.00, NULL, 12, NULL, 1, '2026-06-11T13:25:52.011'),
(204, 25, 5, 5, NULL, N'VCU001-006', 8990000.00, 8990000.00, NULL, 9, NULL, 1, '2026-06-11T13:25:52.013'),
(205, 25, 5, 4, NULL, N'VCU001-007', 8990000.00, 8990000.00, NULL, 6, NULL, 1, '2026-06-11T13:25:52.013'),
(206, 25, 5, 1, NULL, N'VCU001-008', 8990000.00, 8990000.00, NULL, 23, NULL, 1, '2026-06-11T13:25:52.013'),
(207, 25, 5, 6, NULL, N'VCU001-009', 8990000.00, 8990000.00, NULL, 10, NULL, 1, '2026-06-11T13:25:52.018'),
(208, 25, 5, 2, NULL, N'VCU001-010', 8990000.00, 8990000.00, NULL, 6, NULL, 1, '2026-06-11T13:25:52.021'),
(209, 25, 7, 5, NULL, N'VCU001-011', 8990000.00, 8990000.00, NULL, 12, NULL, 1, '2026-06-11T13:25:52.023'),
(210, 25, 7, 4, NULL, N'VCU001-012', 8990000.00, 8990000.00, NULL, 24, NULL, 1, '2026-06-11T13:25:52.023'),
(211, 25, 7, 1, NULL, N'VCU001-013', 8990000.00, 8990000.00, NULL, 24, NULL, 1, '2026-06-11T13:25:52.023'),
(212, 25, 7, 6, NULL, N'VCU001-014', 8990000.00, 8990000.00, NULL, 13, NULL, 1, '2026-06-11T13:25:52.023'),
(213, 25, 7, 2, NULL, N'VCU001-015', 8990000.00, 8990000.00, NULL, 22, NULL, 1, '2026-06-11T13:25:52.034'),
(214, 25, 6, 5, NULL, N'VCU001-016', 8990000.00, 8990000.00, NULL, 8, NULL, 1, '2026-06-11T13:25:52.034'),
(215, 25, 6, 4, NULL, N'VCU001-017', 8990000.00, 8990000.00, NULL, 19, NULL, 1, '2026-06-11T13:25:52.034'),
(216, 25, 6, 1, NULL, N'VCU001-018', 8990000.00, 8990000.00, NULL, 24, NULL, 1, '2026-06-11T13:25:52.041'),
(217, 25, 6, 6, NULL, N'VCU001-019', 8990000.00, 8990000.00, NULL, 11, NULL, 1, '2026-06-11T13:25:52.043'),
(218, 25, 6, 2, NULL, N'VCU001-020', 8990000.00, 8990000.00, NULL, 9, NULL, 1, '2026-06-11T13:25:52.043'),
(219, 26, 8, 3, NULL, N'VCU002-001', 12990000.00, 9990000.00, NULL, 19, NULL, 1, '2026-06-11T13:25:52.053'),
(220, 26, 8, 5, NULL, N'VCU002-002', 12990000.00, 9990000.00, NULL, 21, NULL, 1, '2026-06-11T13:25:52.053'),
(221, 26, 8, 1, NULL, N'VCU002-003', 12990000.00, 9990000.00, NULL, 6, NULL, 1, '2026-06-11T13:25:52.063'),
(222, 26, 8, 2, NULL, N'VCU002-004', 12990000.00, 9990000.00, NULL, 15, NULL, 1, '2026-06-11T13:25:52.066'),
(223, 26, 8, 6, NULL, N'VCU002-005', 12990000.00, 9990000.00, NULL, 12, NULL, 1, '2026-06-11T13:25:52.066'),
(224, 26, 2, 3, NULL, N'VCU002-006', 12990000.00, 9990000.00, NULL, 19, NULL, 1, '2026-06-11T13:25:52.074'),
(225, 26, 2, 5, NULL, N'VCU002-007', 12990000.00, 9990000.00, NULL, 9, NULL, 1, '2026-06-11T13:25:52.077'),
(226, 26, 2, 1, NULL, N'VCU002-008', 12990000.00, 9990000.00, NULL, 18, NULL, 1, '2026-06-11T13:25:52.084'),
(227, 26, 2, 2, NULL, N'VCU002-009', 12990000.00, 9990000.00, NULL, 6, NULL, 1, '2026-06-11T13:25:52.085'),
(228, 26, 2, 6, NULL, N'VCU002-010', 12990000.00, 9990000.00, NULL, 19, NULL, 1, '2026-06-11T13:25:52.090'),
(229, 27, 2, 6, NULL, N'VCU003-001', 7990000.00, 7990000.00, NULL, 19, NULL, 1, '2026-06-11T13:25:52.098'),
(230, 27, 2, 1, NULL, N'VCU003-002', 7990000.00, 7990000.00, NULL, 6, NULL, 1, '2026-06-11T13:25:52.104'),
(231, 27, 2, 4, NULL, N'VCU003-003', 7990000.00, 7990000.00, NULL, 22, NULL, 1, '2026-06-11T13:25:52.106'),
(232, 27, 2, 2, NULL, N'VCU003-004', 7990000.00, 7990000.00, NULL, 10, NULL, 1, '2026-06-11T13:25:52.106'),
(233, 27, 2, 5, NULL, N'VCU003-005', 7990000.00, 7990000.00, NULL, 16, NULL, 1, '2026-06-11T13:25:52.106'),
(234, 27, 4, 6, NULL, N'VCU003-006', 7990000.00, 7990000.00, NULL, 13, NULL, 1, '2026-06-11T13:25:52.116'),
(235, 27, 4, 1, NULL, N'VCU003-007', 7990000.00, 7990000.00, NULL, 20, NULL, 1, '2026-06-11T13:25:52.116'),
(236, 27, 4, 4, NULL, N'VCU003-008', 7990000.00, 7990000.00, NULL, 13, NULL, 1, '2026-06-11T13:25:52.126'),
(237, 27, 4, 2, NULL, N'VCU003-009', 7990000.00, 7990000.00, NULL, 19, NULL, 1, '2026-06-11T13:25:52.130'),
(238, 27, 4, 5, NULL, N'VCU003-010', 7990000.00, 7990000.00, NULL, 10, NULL, 1, '2026-06-11T13:25:52.135'),
(239, 28, 3, 2, NULL, N'VCU004-001', 6490000.00, 5490000.00, NULL, 16, NULL, 1, '2026-06-11T13:25:52.137'),
(240, 28, 3, 3, NULL, N'VCU004-002', 6490000.00, 5490000.00, NULL, 5, NULL, 1, '2026-06-11T13:25:52.145'),
(241, 28, 3, 6, NULL, N'VCU004-003', 6490000.00, 5490000.00, NULL, 6, NULL, 1, '2026-06-11T13:25:52.145'),
(242, 28, 3, 4, NULL, N'VCU004-004', 6490000.00, 5490000.00, NULL, 14, NULL, 1, '2026-06-11T13:25:52.147'),
(243, 28, 3, 1, NULL, N'VCU004-005', 6490000.00, 5490000.00, NULL, 19, NULL, 1, '2026-06-11T13:25:52.147'),
(244, 28, 6, 2, NULL, N'VCU004-006', 6490000.00, 5490000.00, NULL, 15, NULL, 1, '2026-06-11T13:25:52.147'),
(245, 28, 6, 3, NULL, N'VCU004-007', 6490000.00, 5490000.00, NULL, 11, NULL, 1, '2026-06-11T13:25:52.155'),
(246, 28, 6, 6, NULL, N'VCU004-008', 6490000.00, 5490000.00, NULL, 19, NULL, 1, '2026-06-11T13:25:52.157'),
(247, 28, 6, 4, NULL, N'VCU004-009', 6490000.00, 5490000.00, NULL, 12, NULL, 1, '2026-06-11T13:25:52.161'),
(248, 28, 6, 1, NULL, N'VCU004-010', 6490000.00, 5490000.00, NULL, 13, NULL, 1, '2026-06-11T13:25:52.161'),
(249, 28, 4, 2, NULL, N'VCU004-011', 6490000.00, 5490000.00, NULL, 10, NULL, 1, '2026-06-11T13:25:52.161'),
(250, 28, 4, 3, NULL, N'VCU004-012', 6490000.00, 5490000.00, NULL, 13, NULL, 1, '2026-06-11T13:25:52.168'),
(251, 28, 4, 6, NULL, N'VCU004-013', 6490000.00, 5490000.00, NULL, 5, NULL, 1, '2026-06-11T13:25:52.168'),
(252, 28, 4, 4, NULL, N'VCU004-014', 6490000.00, 5490000.00, NULL, 18, NULL, 1, '2026-06-11T13:25:52.176'),
(253, 28, 4, 1, NULL, N'VCU004-015', 6490000.00, 5490000.00, NULL, 21, NULL, 1, '2026-06-11T13:25:52.178'),
(254, 28, 7, 2, NULL, N'VCU004-016', 6490000.00, 5490000.00, NULL, 23, NULL, 1, '2026-06-11T13:25:52.178'),
(255, 28, 7, 3, NULL, N'VCU004-017', 6490000.00, 5490000.00, NULL, 17, NULL, 1, '2026-06-11T13:25:52.186'),
(256, 28, 7, 6, NULL, N'VCU004-018', 6490000.00, 5490000.00, NULL, 19, NULL, 1, '2026-06-11T13:25:52.188'),
(257, 28, 7, 4, NULL, N'VCU004-019', 6490000.00, 5490000.00, NULL, 12, NULL, 1, '2026-06-11T13:25:52.188'),
(258, 28, 7, 1, NULL, N'VCU004-020', 6490000.00, 5490000.00, NULL, 16, NULL, 1, '2026-06-11T13:25:52.193'),
(259, 29, 7, 2, NULL, N'VDT001-001', 2290000.00, 2290000.00, NULL, 14, NULL, 1, '2026-06-11T13:25:52.198'),
(260, 29, 7, 6, NULL, N'VDT001-002', 2290000.00, 2290000.00, NULL, 19, NULL, 1, '2026-06-11T13:25:52.198'),
(261, 29, 7, 4, NULL, N'VDT001-003', 2290000.00, 2290000.00, NULL, 7, NULL, 1, '2026-06-11T13:25:52.206'),
(262, 29, 7, 5, NULL, N'VDT001-004', 2290000.00, 2290000.00, NULL, 8, NULL, 1, '2026-06-11T13:25:52.209'),
(263, 29, 3, 2, NULL, N'VDT001-005', 2290000.00, 2290000.00, NULL, 19, NULL, 1, '2026-06-11T13:25:52.209'),
(264, 29, 3, 6, NULL, N'VDT001-006', 2290000.00, 2290000.00, NULL, 21, NULL, 1, '2026-06-11T13:25:52.209'),
(265, 29, 3, 4, NULL, N'VDT001-007', 2290000.00, 2290000.00, NULL, 15, NULL, 1, '2026-06-11T13:25:52.209'),
(266, 29, 3, 5, NULL, N'VDT001-008', 2290000.00, 2290000.00, NULL, 15, NULL, 1, '2026-06-11T13:25:52.217'),
(267, 30, 6, 3, NULL, N'VDT002-001', 3290000.00, 2690000.00, NULL, 14, NULL, 1, '2026-06-11T13:25:52.224'),
(268, 30, 6, 2, NULL, N'VDT002-002', 3290000.00, 2690000.00, NULL, 11, NULL, 1, '2026-06-11T13:25:52.229'),
(269, 30, 6, 4, NULL, N'VDT002-003', 3290000.00, 2690000.00, NULL, 24, NULL, 1, '2026-06-11T13:25:52.246'),
(270, 30, 1, 3, NULL, N'VDT002-004', 3290000.00, 2690000.00, NULL, 8, NULL, 1, '2026-06-11T13:25:52.249'),
(271, 30, 1, 2, NULL, N'VDT002-005', 3290000.00, 2690000.00, NULL, 18, NULL, 1, '2026-06-11T13:25:52.251'),
(272, 30, 1, 4, NULL, N'VDT002-006', 3290000.00, 2690000.00, NULL, 10, NULL, 1, '2026-06-11T13:25:52.256'),
(273, 30, 4, 3, NULL, N'VDT002-007', 3290000.00, 2690000.00, NULL, 20, NULL, 1, '2026-06-11T13:25:52.262'),
(274, 30, 4, 2, NULL, N'VDT002-008', 3290000.00, 2690000.00, NULL, 20, NULL, 1, '2026-06-11T13:25:52.262'),
(275, 30, 4, 4, NULL, N'VDT002-009', 3290000.00, 2690000.00, NULL, 23, NULL, 1, '2026-06-11T13:25:52.270'),
(276, 30, 7, 3, NULL, N'VDT002-010', 3290000.00, 2690000.00, NULL, 23, NULL, 1, '2026-06-11T13:25:52.272'),
(277, 30, 7, 2, NULL, N'VDT002-011', 3290000.00, 2690000.00, NULL, 7, NULL, 1, '2026-06-11T13:25:52.272'),
(278, 30, 7, 4, NULL, N'VDT002-012', 3290000.00, 2690000.00, NULL, 11, NULL, 1, '2026-06-11T13:25:52.281'),
(279, 31, 3, 6, NULL, N'VDT003-001', 2890000.00, 2890000.00, NULL, 15, NULL, 1, '2026-06-11T13:25:52.288'),
(280, 31, 3, 4, NULL, N'VDT003-002', 2890000.00, 2890000.00, NULL, 6, NULL, 1, '2026-06-11T13:25:52.291'),
(281, 31, 3, 5, NULL, N'VDT003-003', 2890000.00, 2890000.00, NULL, 22, NULL, 1, '2026-06-11T13:25:52.294'),
(282, 31, 3, 2, NULL, N'VDT003-004', 2890000.00, 2890000.00, NULL, 16, NULL, 1, '2026-06-11T13:25:52.294'),
(283, 31, 1, 6, NULL, N'VDT003-005', 2890000.00, 2890000.00, NULL, 23, NULL, 1, '2026-06-11T13:25:52.302'),
(284, 31, 1, 4, NULL, N'VDT003-006', 2890000.00, 2890000.00, NULL, 24, NULL, 1, '2026-06-11T13:25:52.304'),
(285, 31, 1, 5, NULL, N'VDT003-007', 2890000.00, 2890000.00, NULL, 7, NULL, 1, '2026-06-11T13:25:52.304'),
(286, 31, 1, 2, NULL, N'VDT003-008', 2890000.00, 2890000.00, NULL, 23, NULL, 1, '2026-06-11T13:25:52.304'),
(287, 31, 8, 6, NULL, N'VDT003-009', 2890000.00, 2890000.00, NULL, 8, NULL, 1, '2026-06-11T13:25:52.312'),
(288, 31, 8, 4, NULL, N'VDT003-010', 2890000.00, 2890000.00, NULL, 19, NULL, 1, '2026-06-11T13:25:52.315'),
(289, 31, 8, 5, NULL, N'VDT003-011', 2890000.00, 2890000.00, NULL, 16, NULL, 1, '2026-06-11T13:25:52.315'),
(290, 31, 8, 2, NULL, N'VDT003-012', 2890000.00, 2890000.00, NULL, 24, NULL, 1, '2026-06-11T13:25:52.320'),
(291, 31, 6, 6, NULL, N'VDT003-013', 2890000.00, 2890000.00, NULL, 13, NULL, 1, '2026-06-11T13:25:52.325'),
(292, 31, 6, 4, NULL, N'VDT003-014', 2890000.00, 2890000.00, NULL, 11, NULL, 1, '2026-06-11T13:25:52.325'),
(293, 31, 6, 5, NULL, N'VDT003-015', 2890000.00, 2890000.00, NULL, 24, NULL, 1, '2026-06-11T13:25:52.325'),
(294, 31, 6, 2, NULL, N'VDT003-016', 2890000.00, 2890000.00, NULL, 15, NULL, 1, '2026-06-11T13:25:52.333'),
(295, 32, 1, 4, NULL, N'VDT004-001', 3490000.00, 2890000.00, NULL, 20, NULL, 1, '2026-06-11T13:25:52.335'),
(296, 32, 1, 1, NULL, N'VDT004-002', 3490000.00, 2890000.00, NULL, 24, NULL, 1, '2026-06-11T13:25:52.346'),
(297, 32, 1, 2, NULL, N'VDT004-003', 3490000.00, 2890000.00, NULL, 11, NULL, 1, '2026-06-11T13:25:52.347'),
(298, 32, 1, 5, NULL, N'VDT004-004', 3490000.00, 2890000.00, NULL, 5, NULL, 1, '2026-06-11T13:25:52.352'),
(299, 32, 1, 6, NULL, N'VDT004-005', 3490000.00, 2890000.00, NULL, 14, NULL, 1, '2026-06-11T13:25:52.354'),
(300, 32, 2, 4, NULL, N'VDT004-006', 3490000.00, 2890000.00, NULL, 23, NULL, 1, '2026-06-11T13:25:52.356'),
(301, 32, 2, 1, NULL, N'VDT004-007', 3490000.00, 2890000.00, NULL, 12, NULL, 1, '2026-06-11T13:25:52.356'),
(302, 32, 2, 2, NULL, N'VDT004-008', 3490000.00, 2890000.00, NULL, 15, NULL, 1, '2026-06-11T13:25:52.356'),
(303, 32, 2, 5, NULL, N'VDT004-009', 3490000.00, 2890000.00, NULL, 14, NULL, 1, '2026-06-11T13:25:52.365'),
(304, 32, 2, 6, NULL, N'VDT004-010', 3490000.00, 2890000.00, NULL, 12, NULL, 1, '2026-06-11T13:25:52.367'),
(305, 32, 8, 4, NULL, N'VDT004-011', 3490000.00, 2890000.00, NULL, 5, NULL, 1, '2026-06-11T13:25:52.367'),
(306, 32, 8, 1, NULL, N'VDT004-012', 3490000.00, 2890000.00, NULL, 20, NULL, 1, '2026-06-11T13:25:52.367'),
(307, 32, 8, 2, NULL, N'VDT004-013', 3490000.00, 2890000.00, NULL, 6, NULL, 1, '2026-06-11T13:25:52.367'),
(308, 32, 8, 5, NULL, N'VDT004-014', 3490000.00, 2890000.00, NULL, 15, NULL, 1, '2026-06-11T13:25:52.377'),
(309, 32, 8, 6, NULL, N'VDT004-015', 3490000.00, 2890000.00, NULL, 8, NULL, 1, '2026-06-11T13:25:52.377'),
(310, 33, 2, 1, NULL, N'VTT006-001', 1590000.00, 1590000.00, NULL, 6, NULL, 1, '2026-06-11T20:19:58.836'),
(311, 33, 2, 2, NULL, N'VTT006-002', 1590000.00, 1590000.00, NULL, 7, NULL, 1, '2026-06-11T20:19:58.840'),
(312, 33, 2, 3, NULL, N'VTT006-003', 1590000.00, 1350000.00, 15.00, 8, NULL, 1, '2026-06-11T20:19:58.863'),
(313, 33, 2, 4, NULL, N'VTT006-004', 1590000.00, 1590000.00, NULL, 9, NULL, 1, '2026-06-11T20:19:58.866'),
(314, 33, 5, 1, NULL, N'VTT006-005', 1590000.00, 1590000.00, NULL, 10, NULL, 1, '2026-06-11T20:19:58.866'),
(315, 33, 5, 2, NULL, N'VTT006-006', 1590000.00, 1350000.00, 15.00, 11, NULL, 1, '2026-06-11T20:19:58.870'),
(316, 33, 5, 3, NULL, N'VTT006-007', 1590000.00, 1590000.00, NULL, 12, NULL, 1, '2026-06-11T20:19:58.870'),
(317, 33, 5, 4, NULL, N'VTT006-008', 1590000.00, 1590000.00, NULL, 13, NULL, 1, '2026-06-11T20:19:58.870'),
(318, 34, 2, 1, NULL, N'VTT007-009', 1590000.00, 1350000.00, 15.00, 14, NULL, 1, '2026-06-11T20:19:58.870'),
(319, 34, 2, 2, NULL, N'VTT007-010', 1590000.00, 1590000.00, NULL, 15, NULL, 1, '2026-06-11T20:19:58.870'),
(320, 34, 2, 3, NULL, N'VTT007-011', 1590000.00, 1590000.00, NULL, 16, NULL, 1, '2026-06-11T20:19:58.873'),
(321, 34, 2, 4, NULL, N'VTT007-012', 1590000.00, 1350000.00, 15.00, 17, NULL, 1, '2026-06-11T20:19:58.873'),
(322, 34, 5, 1, NULL, N'VTT007-013', 1590000.00, 1590000.00, NULL, 18, NULL, 1, '2026-06-11T20:19:58.873'),
(323, 34, 5, 2, NULL, N'VTT007-014', 1590000.00, 1590000.00, NULL, 19, NULL, 1, '2026-06-11T20:19:58.873'),
(324, 34, 5, 3, NULL, N'VTT007-015', 1590000.00, 1350000.00, 15.00, 20, NULL, 1, '2026-06-11T20:19:58.876'),
(325, 34, 5, 4, NULL, N'VTT007-016', 1590000.00, 1590000.00, NULL, 21, NULL, 1, '2026-06-11T20:19:58.876'),
(326, 35, 2, 1, NULL, N'VTT008-017', 1590000.00, 1590000.00, NULL, 22, NULL, 1, '2026-06-11T20:19:58.876'),
(327, 35, 2, 2, NULL, N'VTT008-018', 1590000.00, 1350000.00, 15.00, 23, NULL, 1, '2026-06-11T20:19:58.876'),
(328, 35, 2, 3, NULL, N'VTT008-019', 1590000.00, 1590000.00, NULL, 24, NULL, 1, '2026-06-11T20:19:58.880'),
(329, 35, 2, 4, NULL, N'VTT008-020', 1590000.00, 1590000.00, NULL, 5, NULL, 1, '2026-06-11T20:19:58.880'),
(330, 35, 5, 1, NULL, N'VTT008-021', 1590000.00, 1350000.00, 15.00, 6, NULL, 1, '2026-06-11T20:19:58.880'),
(331, 35, 5, 2, NULL, N'VTT008-022', 1590000.00, 1590000.00, NULL, 7, NULL, 1, '2026-06-11T20:19:58.880'),
(332, 35, 5, 3, NULL, N'VTT008-023', 1590000.00, 1590000.00, NULL, 8, NULL, 1, '2026-06-11T20:19:58.880'),
(333, 35, 5, 4, NULL, N'VTT008-024', 1590000.00, 1350000.00, 15.00, 9, NULL, 1, '2026-06-11T20:19:58.883'),
(334, 36, 2, 1, NULL, N'VTT009-025', 1590000.00, 1590000.00, NULL, 10, NULL, 1, '2026-06-11T20:19:58.883'),
(335, 36, 2, 2, NULL, N'VTT009-026', 1590000.00, 1590000.00, NULL, 11, NULL, 1, '2026-06-11T20:19:58.883'),
(336, 36, 2, 3, NULL, N'VTT009-027', 1590000.00, 1350000.00, 15.00, 12, NULL, 1, '2026-06-11T20:19:58.886'),
(337, 36, 2, 4, NULL, N'VTT009-028', 1590000.00, 1590000.00, NULL, 13, NULL, 1, '2026-06-11T20:19:58.886'),
(338, 36, 5, 1, NULL, N'VTT009-029', 1590000.00, 1590000.00, NULL, 14, NULL, 1, '2026-06-11T20:19:58.886'),
(339, 36, 5, 2, NULL, N'VTT009-030', 1590000.00, 1350000.00, 15.00, 15, NULL, 1, '2026-06-11T20:19:58.886'),
(340, 36, 5, 3, NULL, N'VTT009-031', 1590000.00, 1590000.00, NULL, 16, NULL, 1, '2026-06-11T20:19:58.890'),
(341, 36, 5, 4, NULL, N'VTT009-032', 1590000.00, 1590000.00, NULL, 17, NULL, 1, '2026-06-11T20:19:58.890'),
(342, 37, 2, 1, NULL, N'VTT010-033', 1590000.00, 1350000.00, 15.00, 18, NULL, 1, '2026-06-11T20:19:58.890'),
(343, 37, 2, 2, NULL, N'VTT010-034', 1590000.00, 1590000.00, NULL, 19, NULL, 1, '2026-06-11T20:19:58.893'),
(344, 37, 2, 3, NULL, N'VTT010-035', 1590000.00, 1590000.00, NULL, 20, NULL, 1, '2026-06-11T20:19:58.893'),
(345, 37, 2, 4, NULL, N'VTT010-036', 1590000.00, 1350000.00, 15.00, 21, NULL, 1, '2026-06-11T20:19:58.893'),
(346, 37, 5, 1, NULL, N'VTT010-037', 1590000.00, 1590000.00, NULL, 22, NULL, 1, '2026-06-11T20:19:58.896'),
(347, 37, 5, 2, NULL, N'VTT010-038', 1590000.00, 1590000.00, NULL, 23, NULL, 1, '2026-06-11T20:19:58.896'),
(348, 37, 5, 3, NULL, N'VTT010-039', 1590000.00, 1350000.00, 15.00, 24, NULL, 1, '2026-06-11T20:19:58.896'),
(349, 37, 5, 4, NULL, N'VTT010-040', 1590000.00, 1590000.00, NULL, 5, NULL, 1, '2026-06-11T20:19:58.900'),
(350, 38, 2, 1, NULL, N'VCT007-041', 1190000.00, 1190000.00, NULL, 6, NULL, 1, '2026-06-11T20:19:58.900'),
(351, 38, 2, 2, NULL, N'VCT007-042', 1190000.00, 1010000.00, 15.00, 7, NULL, 1, '2026-06-11T20:19:58.900'),
(352, 38, 2, 3, NULL, N'VCT007-043', 1190000.00, 1190000.00, NULL, 8, NULL, 1, '2026-06-11T20:19:58.900'),
(353, 38, 2, 4, NULL, N'VCT007-044', 1190000.00, 1190000.00, NULL, 9, NULL, 1, '2026-06-11T20:19:58.903'),
(354, 38, 5, 1, NULL, N'VCT007-045', 1190000.00, 1010000.00, 15.00, 10, NULL, 1, '2026-06-11T20:19:58.910'),
(355, 38, 5, 2, NULL, N'VCT007-046', 1190000.00, 1190000.00, NULL, 11, NULL, 1, '2026-06-11T20:19:58.910'),
(356, 38, 5, 3, NULL, N'VCT007-047', 1190000.00, 1190000.00, NULL, 12, NULL, 1, '2026-06-11T20:19:58.910'),
(357, 38, 5, 4, NULL, N'VCT007-048', 1190000.00, 1010000.00, 15.00, 13, NULL, 1, '2026-06-11T20:19:58.913'),
(358, 39, 2, 1, NULL, N'VCT008-049', 1190000.00, 1190000.00, NULL, 14, NULL, 1, '2026-06-11T20:19:58.913'),
(359, 39, 2, 2, NULL, N'VCT008-050', 1190000.00, 1190000.00, NULL, 15, NULL, 1, '2026-06-11T20:19:58.913'),
(360, 39, 2, 3, NULL, N'VCT008-051', 1190000.00, 1010000.00, 15.00, 16, NULL, 1, '2026-06-11T20:19:58.916'),
(361, 39, 2, 4, NULL, N'VCT008-052', 1190000.00, 1190000.00, NULL, 17, NULL, 1, '2026-06-11T20:19:58.916'),
(362, 39, 5, 1, NULL, N'VCT008-053', 1190000.00, 1190000.00, NULL, 18, NULL, 1, '2026-06-11T20:19:58.920'),
(363, 39, 5, 2, NULL, N'VCT008-054', 1190000.00, 1010000.00, 15.00, 19, NULL, 1, '2026-06-11T20:19:58.920'),
(364, 39, 5, 3, NULL, N'VCT008-055', 1190000.00, 1190000.00, NULL, 20, NULL, 1, '2026-06-11T20:19:58.920'),
(365, 39, 5, 4, NULL, N'VCT008-056', 1190000.00, 1190000.00, NULL, 21, NULL, 1, '2026-06-11T20:19:58.920'),
(366, 40, 2, 1, NULL, N'VCT009-057', 1190000.00, 1010000.00, 15.00, 22, NULL, 1, '2026-06-11T20:19:58.920'),
(367, 40, 2, 2, NULL, N'VCT009-058', 1190000.00, 1190000.00, NULL, 23, NULL, 1, '2026-06-11T20:19:58.923'),
(368, 40, 2, 3, NULL, N'VCT009-059', 1190000.00, 1190000.00, NULL, 24, NULL, 1, '2026-06-11T20:19:58.923'),
(369, 40, 2, 4, NULL, N'VCT009-060', 1190000.00, 1010000.00, 15.00, 5, NULL, 1, '2026-06-11T20:19:58.923'),
(370, 40, 5, 1, NULL, N'VCT009-061', 1190000.00, 1190000.00, NULL, 6, NULL, 1, '2026-06-11T20:19:58.926'),
(371, 40, 5, 2, NULL, N'VCT009-062', 1190000.00, 1190000.00, NULL, 7, NULL, 1, '2026-06-11T20:19:58.926'),
(372, 40, 5, 3, NULL, N'VCT009-063', 1190000.00, 1010000.00, 15.00, 8, NULL, 1, '2026-06-11T20:19:58.926'),
(373, 40, 5, 4, NULL, N'VCT009-064', 1190000.00, 1190000.00, NULL, 9, NULL, 1, '2026-06-11T20:19:58.926'),
(374, 41, 2, 1, NULL, N'VCT010-065', 1190000.00, 1190000.00, NULL, 10, NULL, 1, '2026-06-11T20:19:58.930'),
(375, 41, 2, 2, NULL, N'VCT010-066', 1190000.00, 1010000.00, 15.00, 11, NULL, 1, '2026-06-11T20:19:58.930'),
(376, 41, 2, 3, NULL, N'VCT010-067', 1190000.00, 1190000.00, NULL, 12, NULL, 1, '2026-06-11T20:19:58.930'),
(377, 41, 2, 4, NULL, N'VCT010-068', 1190000.00, 1190000.00, NULL, 13, NULL, 1, '2026-06-11T20:19:58.930'),
(378, 41, 5, 1, NULL, N'VCT010-069', 1190000.00, 1010000.00, 15.00, 14, NULL, 1, '2026-06-11T20:19:58.930'),
(379, 41, 5, 2, NULL, N'VCT010-070', 1190000.00, 1190000.00, NULL, 15, NULL, 1, '2026-06-11T20:19:58.930'),
(380, 41, 5, 3, NULL, N'VCT010-071', 1190000.00, 1190000.00, NULL, 16, NULL, 1, '2026-06-11T20:19:58.936'),
(381, 41, 5, 4, NULL, N'VCT010-072', 1190000.00, 1010000.00, 15.00, 17, NULL, 1, '2026-06-11T20:19:58.940'),
(382, 42, 2, 1, NULL, N'VCT011-073', 1190000.00, 1190000.00, NULL, 18, NULL, 1, '2026-06-11T20:19:58.940'),
(383, 42, 2, 2, NULL, N'VCT011-074', 1190000.00, 1190000.00, NULL, 19, NULL, 1, '2026-06-11T20:19:58.943'),
(384, 42, 2, 3, NULL, N'VCT011-075', 1190000.00, 1010000.00, 15.00, 20, NULL, 1, '2026-06-11T20:19:58.943'),
(385, 42, 2, 4, NULL, N'VCT011-076', 1190000.00, 1190000.00, NULL, 21, NULL, 1, '2026-06-11T20:19:58.946'),
(386, 42, 5, 1, NULL, N'VCT011-077', 1190000.00, 1190000.00, NULL, 22, NULL, 1, '2026-06-11T20:19:58.946'),
(387, 42, 5, 2, NULL, N'VCT011-078', 1190000.00, 1010000.00, 15.00, 23, NULL, 1, '2026-06-11T20:19:58.946'),
(388, 42, 5, 3, NULL, N'VCT011-079', 1190000.00, 1190000.00, NULL, 24, NULL, 1, '2026-06-11T20:19:58.946'),
(389, 42, 5, 4, NULL, N'VCT011-080', 1190000.00, 1190000.00, NULL, 5, NULL, 1, '2026-06-11T20:19:58.950'),
(390, 43, 2, 1, NULL, N'VDH006-081', 2990000.00, 2540000.00, 15.00, 6, NULL, 1, '2026-06-11T20:19:58.950'),
(391, 43, 2, 2, NULL, N'VDH006-082', 2990000.00, 2990000.00, NULL, 7, NULL, 1, '2026-06-11T20:19:58.950'),
(392, 43, 2, 3, NULL, N'VDH006-083', 2990000.00, 2990000.00, NULL, 8, NULL, 1, '2026-06-11T20:19:58.950'),
(393, 43, 2, 4, NULL, N'VDH006-084', 2990000.00, 2540000.00, 15.00, 9, NULL, 1, '2026-06-11T20:19:58.953'),
(394, 43, 5, 1, NULL, N'VDH006-085', 2990000.00, 2990000.00, NULL, 10, NULL, 1, '2026-06-11T20:19:58.953'),
(395, 43, 5, 2, NULL, N'VDH006-086', 2990000.00, 2990000.00, NULL, 11, NULL, 1, '2026-06-11T20:19:58.953'),
(396, 43, 5, 3, NULL, N'VDH006-087', 2990000.00, 2540000.00, 15.00, 12, NULL, 1, '2026-06-11T20:19:58.956'),
(397, 43, 5, 4, NULL, N'VDH006-088', 2990000.00, 2990000.00, NULL, 13, NULL, 1, '2026-06-11T20:19:58.956'),
(398, 44, 2, 1, NULL, N'VDH007-089', 2990000.00, 2990000.00, NULL, 14, NULL, 1, '2026-06-11T20:19:58.960'),
(399, 44, 2, 2, NULL, N'VDH007-090', 2990000.00, 2540000.00, 15.00, 15, NULL, 1, '2026-06-11T20:19:58.960'),
(400, 44, 2, 3, NULL, N'VDH007-091', 2990000.00, 2990000.00, NULL, 16, NULL, 1, '2026-06-11T20:19:58.960'),
(401, 44, 2, 4, NULL, N'VDH007-092', 2990000.00, 2990000.00, NULL, 17, NULL, 1, '2026-06-11T20:19:58.960');
INSERT INTO [dbo].[Vay_chi_tiet] ([id], [id_vay], [id_mau_sac], [id_kich_thuoc], [id_khuyen_mai], [ma_vay_chi_tiet], [gia_ban_goc], [gia_ban], [phan_tram_giam], [so_luong], [anh_url], [trang_thai], [ngay_tao]) VALUES
(402, 44, 5, 1, NULL, N'VDH007-093', 2990000.00, 2540000.00, 15.00, 18, NULL, 1, '2026-06-11T20:19:58.963'),
(403, 44, 5, 2, NULL, N'VDH007-094', 2990000.00, 2990000.00, NULL, 19, NULL, 1, '2026-06-11T20:19:58.963'),
(404, 44, 5, 3, NULL, N'VDH007-095', 2990000.00, 2990000.00, NULL, 20, NULL, 1, '2026-06-11T20:19:58.963'),
(405, 44, 5, 4, NULL, N'VDH007-096', 2990000.00, 2540000.00, 15.00, 21, NULL, 1, '2026-06-11T20:19:58.963'),
(406, 45, 2, 1, NULL, N'VDH008-097', 2990000.00, 2990000.00, NULL, 22, NULL, 1, '2026-06-11T20:19:58.966'),
(407, 45, 2, 2, NULL, N'VDH008-098', 2990000.00, 2990000.00, NULL, 23, NULL, 1, '2026-06-11T20:19:58.966'),
(408, 45, 2, 3, NULL, N'VDH008-099', 2990000.00, 2540000.00, 15.00, 24, NULL, 1, '2026-06-11T20:19:58.970'),
(409, 45, 2, 4, NULL, N'VDH008-100', 2990000.00, 2990000.00, NULL, 5, NULL, 1, '2026-06-11T20:19:58.970'),
(410, 45, 5, 1, NULL, N'VDH008-101', 2990000.00, 2990000.00, NULL, 6, NULL, 1, '2026-06-11T20:19:58.970'),
(411, 45, 5, 2, NULL, N'VDH008-102', 2990000.00, 2540000.00, 15.00, 7, NULL, 1, '2026-06-11T20:19:58.970'),
(412, 45, 5, 3, NULL, N'VDH008-103', 2990000.00, 2990000.00, NULL, 8, NULL, 1, '2026-06-11T20:19:58.970'),
(413, 45, 5, 4, NULL, N'VDH008-104', 2990000.00, 2990000.00, NULL, 9, NULL, 1, '2026-06-11T20:19:58.973'),
(414, 46, 2, 1, NULL, N'VDH009-105', 2990000.00, 2540000.00, 15.00, 10, NULL, 1, '2026-06-11T20:19:58.973'),
(415, 46, 2, 2, NULL, N'VDH009-106', 2990000.00, 2990000.00, NULL, 11, NULL, 1, '2026-06-11T20:19:58.973'),
(416, 46, 2, 3, NULL, N'VDH009-107', 2990000.00, 2990000.00, NULL, 12, NULL, 1, '2026-06-11T20:19:58.976'),
(417, 46, 2, 4, NULL, N'VDH009-108', 2990000.00, 2540000.00, 15.00, 13, NULL, 1, '2026-06-11T20:19:58.976'),
(418, 46, 5, 1, NULL, N'VDH009-109', 2990000.00, 2990000.00, NULL, 14, NULL, 1, '2026-06-11T20:19:58.980'),
(419, 46, 5, 2, NULL, N'VDH009-110', 2990000.00, 2990000.00, NULL, 15, NULL, 1, '2026-06-11T20:19:58.980'),
(420, 46, 5, 3, NULL, N'VDH009-111', 2990000.00, 2540000.00, 15.00, 16, NULL, 1, '2026-06-11T20:19:58.980'),
(421, 46, 5, 4, NULL, N'VDH009-112', 2990000.00, 2990000.00, NULL, 17, NULL, 1, '2026-06-11T20:19:58.980'),
(422, 47, 2, 1, NULL, N'VDH010-113', 2990000.00, 2990000.00, NULL, 18, NULL, 1, '2026-06-11T20:19:58.983'),
(423, 47, 2, 2, NULL, N'VDH010-114', 2990000.00, 2540000.00, 15.00, 19, NULL, 1, '2026-06-11T20:19:58.983'),
(424, 47, 2, 3, NULL, N'VDH010-115', 2990000.00, 2990000.00, NULL, 20, NULL, 1, '2026-06-11T20:19:58.983'),
(425, 47, 2, 4, NULL, N'VDH010-116', 2990000.00, 2990000.00, NULL, 21, NULL, 1, '2026-06-11T20:19:58.983'),
(426, 47, 5, 1, NULL, N'VDH010-117', 2990000.00, 2540000.00, 15.00, 22, NULL, 1, '2026-06-11T20:19:58.983'),
(427, 47, 5, 2, NULL, N'VDH010-118', 2990000.00, 2990000.00, NULL, 23, NULL, 1, '2026-06-11T20:19:58.986'),
(428, 47, 5, 3, NULL, N'VDH010-119', 2990000.00, 2990000.00, NULL, 24, NULL, 1, '2026-06-11T20:19:58.986'),
(429, 47, 5, 4, NULL, N'VDH010-120', 2990000.00, 2540000.00, 15.00, 5, NULL, 1, '2026-06-11T20:19:58.986'),
(430, 48, 2, 1, NULL, N'VC006-121', 4990000.00, 4990000.00, NULL, 6, NULL, 1, '2026-06-11T20:19:58.986'),
(431, 48, 2, 2, NULL, N'VC006-122', 4990000.00, 4990000.00, NULL, 7, NULL, 1, '2026-06-11T20:19:58.990'),
(432, 48, 2, 3, NULL, N'VC006-123', 4990000.00, 4240000.00, 15.00, 8, NULL, 1, '2026-06-11T20:19:58.990'),
(433, 48, 2, 4, NULL, N'VC006-124', 4990000.00, 4990000.00, NULL, 9, NULL, 1, '2026-06-11T20:19:58.990'),
(434, 48, 5, 1, NULL, N'VC006-125', 4990000.00, 4990000.00, NULL, 10, NULL, 1, '2026-06-11T20:19:58.990'),
(435, 48, 5, 2, NULL, N'VC006-126', 4990000.00, 4240000.00, 15.00, 11, NULL, 1, '2026-06-11T20:19:58.993'),
(436, 48, 5, 3, NULL, N'VC006-127', 4990000.00, 4990000.00, NULL, 12, NULL, 1, '2026-06-11T20:19:58.993'),
(437, 48, 5, 4, NULL, N'VC006-128', 4990000.00, 4990000.00, NULL, 13, NULL, 1, '2026-06-11T20:19:58.996'),
(438, 49, 2, 1, NULL, N'VC007-129', 4990000.00, 4240000.00, 15.00, 14, NULL, 1, '2026-06-11T20:19:58.996'),
(439, 49, 2, 2, NULL, N'VC007-130', 4990000.00, 4990000.00, NULL, 15, NULL, 1, '2026-06-11T20:19:58.996'),
(440, 49, 2, 3, NULL, N'VC007-131', 4990000.00, 4990000.00, NULL, 16, NULL, 1, '2026-06-11T20:19:58.996'),
(441, 49, 2, 4, NULL, N'VC007-132', 4990000.00, 4240000.00, 15.00, 17, NULL, 1, '2026-06-11T20:19:59.000'),
(442, 49, 5, 1, NULL, N'VC007-133', 4990000.00, 4990000.00, NULL, 18, NULL, 1, '2026-06-11T20:19:59.000'),
(443, 49, 5, 2, NULL, N'VC007-134', 4990000.00, 4990000.00, NULL, 19, NULL, 1, '2026-06-11T20:19:59.003'),
(444, 49, 5, 3, NULL, N'VC007-135', 4990000.00, 4240000.00, 15.00, 20, NULL, 1, '2026-06-11T20:19:59.003'),
(445, 49, 5, 4, NULL, N'VC007-136', 4990000.00, 4990000.00, NULL, 21, NULL, 1, '2026-06-11T20:19:59.006'),
(446, 50, 2, 1, NULL, N'VC008-137', 4990000.00, 4990000.00, NULL, 22, NULL, 1, '2026-06-11T20:19:59.006'),
(447, 50, 2, 2, NULL, N'VC008-138', 4990000.00, 4240000.00, 15.00, 23, NULL, 1, '2026-06-11T20:19:59.006'),
(448, 50, 2, 3, NULL, N'VC008-139', 4990000.00, 4990000.00, NULL, 24, NULL, 1, '2026-06-11T20:19:59.010'),
(449, 50, 2, 4, NULL, N'VC008-140', 4990000.00, 4990000.00, NULL, 5, NULL, 1, '2026-06-11T20:19:59.010'),
(450, 50, 5, 1, NULL, N'VC008-141', 4990000.00, 4240000.00, 15.00, 6, NULL, 1, '2026-06-11T20:19:59.010'),
(451, 50, 5, 2, NULL, N'VC008-142', 4990000.00, 4990000.00, NULL, 7, NULL, 1, '2026-06-11T20:19:59.013'),
(452, 50, 5, 3, NULL, N'VC008-143', 4990000.00, 4990000.00, NULL, 8, NULL, 1, '2026-06-11T20:19:59.013'),
(453, 50, 5, 4, NULL, N'VC008-144', 4990000.00, 4240000.00, 15.00, 9, NULL, 1, '2026-06-11T20:19:59.013'),
(454, 51, 2, 1, NULL, N'VC009-145', 4990000.00, 4990000.00, NULL, 10, NULL, 1, '2026-06-11T20:19:59.016'),
(455, 51, 2, 2, NULL, N'VC009-146', 4990000.00, 4990000.00, NULL, 11, NULL, 1, '2026-06-11T20:19:59.016'),
(456, 51, 2, 3, NULL, N'VC009-147', 4990000.00, 4240000.00, 15.00, 12, NULL, 1, '2026-06-11T20:19:59.016'),
(457, 51, 2, 4, NULL, N'VC009-148', 4990000.00, 4990000.00, NULL, 13, NULL, 1, '2026-06-11T20:19:59.020'),
(458, 51, 5, 1, NULL, N'VC009-149', 4990000.00, 4990000.00, NULL, 14, NULL, 1, '2026-06-11T20:19:59.020'),
(459, 51, 5, 2, NULL, N'VC009-150', 4990000.00, 4240000.00, 15.00, 15, NULL, 1, '2026-06-11T20:19:59.020'),
(460, 51, 5, 3, NULL, N'VC009-151', 4990000.00, 4990000.00, NULL, 16, NULL, 1, '2026-06-11T20:19:59.020'),
(461, 51, 5, 4, NULL, N'VC009-152', 4990000.00, 4990000.00, NULL, 17, NULL, 1, '2026-06-11T20:19:59.020'),
(462, 52, 2, 1, NULL, N'VC010-153', 4990000.00, 4240000.00, 15.00, 18, NULL, 1, '2026-06-11T20:19:59.023'),
(463, 52, 2, 2, NULL, N'VC010-154', 4990000.00, 4990000.00, NULL, 19, NULL, 1, '2026-06-11T20:19:59.023'),
(464, 52, 2, 3, NULL, N'VC010-155', 4990000.00, 4990000.00, NULL, 20, NULL, 1, '2026-06-11T20:19:59.023'),
(465, 52, 2, 4, NULL, N'VC010-156', 4990000.00, 4240000.00, 15.00, 21, NULL, 1, '2026-06-11T20:19:59.023'),
(466, 52, 5, 1, NULL, N'VC010-157', 4990000.00, 4990000.00, NULL, 22, NULL, 1, '2026-06-11T20:19:59.023'),
(467, 52, 5, 2, NULL, N'VC010-158', 4990000.00, 4990000.00, NULL, 23, NULL, 1, '2026-06-11T20:19:59.026'),
(468, 52, 5, 3, NULL, N'VC010-159', 4990000.00, 4240000.00, 15.00, 24, NULL, 1, '2026-06-11T20:19:59.026'),
(469, 52, 5, 4, NULL, N'VC010-160', 4990000.00, 4990000.00, NULL, 5, NULL, 1, '2026-06-11T20:19:59.030'),
(470, 53, 2, 1, NULL, N'VHS005-161', 450000.00, 450000.00, NULL, 6, NULL, 1, '2026-06-11T20:19:59.030'),
(471, 53, 2, 2, NULL, N'VHS005-162', 450000.00, 380000.00, 16.00, 7, NULL, 1, '2026-06-11T20:19:59.030'),
(472, 53, 2, 3, NULL, N'VHS005-163', 450000.00, 450000.00, NULL, 8, NULL, 1, '2026-06-11T20:19:59.030'),
(473, 53, 2, 4, NULL, N'VHS005-164', 450000.00, 450000.00, NULL, 9, NULL, 1, '2026-06-11T20:19:59.030'),
(474, 53, 5, 1, NULL, N'VHS005-165', 450000.00, 380000.00, 16.00, 10, NULL, 1, '2026-06-11T20:19:59.033'),
(475, 53, 5, 2, NULL, N'VHS005-166', 450000.00, 450000.00, NULL, 11, NULL, 1, '2026-06-11T20:19:59.033'),
(476, 53, 5, 3, NULL, N'VHS005-167', 450000.00, 450000.00, NULL, 12, NULL, 1, '2026-06-11T20:19:59.033'),
(477, 53, 5, 4, NULL, N'VHS005-168', 450000.00, 380000.00, 16.00, 13, NULL, 1, '2026-06-11T20:19:59.036'),
(478, 54, 2, 1, NULL, N'VHS006-169', 450000.00, 450000.00, NULL, 14, NULL, 1, '2026-06-11T20:19:59.036'),
(479, 54, 2, 2, NULL, N'VHS006-170', 450000.00, 450000.00, NULL, 15, NULL, 1, '2026-06-11T20:19:59.036'),
(480, 54, 2, 3, NULL, N'VHS006-171', 450000.00, 380000.00, 16.00, 16, NULL, 1, '2026-06-11T20:19:59.036'),
(481, 54, 2, 4, NULL, N'VHS006-172', 450000.00, 450000.00, NULL, 17, NULL, 1, '2026-06-11T20:19:59.040'),
(482, 54, 5, 1, NULL, N'VHS006-173', 450000.00, 450000.00, NULL, 18, NULL, 1, '2026-06-11T20:19:59.040'),
(483, 54, 5, 2, NULL, N'VHS006-174', 450000.00, 380000.00, 16.00, 19, NULL, 1, '2026-06-11T20:19:59.040'),
(484, 54, 5, 3, NULL, N'VHS006-175', 450000.00, 450000.00, NULL, 20, NULL, 1, '2026-06-11T20:19:59.040'),
(485, 54, 5, 4, NULL, N'VHS006-176', 450000.00, 450000.00, NULL, 21, NULL, 1, '2026-06-11T20:19:59.040'),
(486, 55, 2, 1, NULL, N'VHS007-177', 450000.00, 380000.00, 16.00, 22, NULL, 1, '2026-06-11T20:19:59.043'),
(487, 55, 2, 2, NULL, N'VHS007-178', 450000.00, 450000.00, NULL, 23, NULL, 1, '2026-06-11T20:19:59.043'),
(488, 55, 2, 3, NULL, N'VHS007-179', 450000.00, 450000.00, NULL, 24, NULL, 1, '2026-06-11T20:19:59.043'),
(489, 55, 2, 4, NULL, N'VHS007-180', 450000.00, 380000.00, 16.00, 5, NULL, 1, '2026-06-11T20:19:59.046'),
(490, 55, 5, 1, NULL, N'VHS007-181', 450000.00, 450000.00, NULL, 6, NULL, 1, '2026-06-11T20:19:59.046'),
(491, 55, 5, 2, NULL, N'VHS007-182', 450000.00, 450000.00, NULL, 7, NULL, 1, '2026-06-11T20:19:59.046'),
(492, 55, 5, 3, NULL, N'VHS007-183', 450000.00, 380000.00, 16.00, 8, NULL, 1, '2026-06-11T20:19:59.046'),
(493, 55, 5, 4, NULL, N'VHS007-184', 450000.00, 450000.00, NULL, 9, NULL, 1, '2026-06-11T20:19:59.050'),
(494, 56, 2, 1, NULL, N'VHS008-185', 450000.00, 450000.00, NULL, 10, NULL, 1, '2026-06-11T20:19:59.050'),
(495, 56, 2, 2, NULL, N'VHS008-186', 450000.00, 380000.00, 16.00, 11, NULL, 1, '2026-06-11T20:19:59.050'),
(496, 56, 2, 3, NULL, N'VHS008-187', 450000.00, 450000.00, NULL, 12, NULL, 1, '2026-06-11T20:19:59.053'),
(497, 56, 2, 4, NULL, N'VHS008-188', 450000.00, 450000.00, NULL, 13, NULL, 1, '2026-06-11T20:19:59.053'),
(498, 56, 5, 1, NULL, N'VHS008-189', 450000.00, 380000.00, 16.00, 14, NULL, 1, '2026-06-11T20:19:59.053'),
(499, 56, 5, 2, NULL, N'VHS008-190', 450000.00, 450000.00, NULL, 15, NULL, 1, '2026-06-11T20:19:59.053'),
(500, 56, 5, 3, NULL, N'VHS008-191', 450000.00, 450000.00, NULL, 16, NULL, 1, '2026-06-11T20:19:59.056'),
(501, 56, 5, 4, NULL, N'VHS008-192', 450000.00, 380000.00, 16.00, 17, NULL, 1, '2026-06-11T20:19:59.056'),
(502, 57, 2, 1, NULL, N'VHS009-193', 450000.00, 450000.00, NULL, 18, NULL, 1, '2026-06-11T20:19:59.056'),
(503, 57, 2, 2, NULL, N'VHS009-194', 450000.00, 450000.00, NULL, 19, NULL, 1, '2026-06-11T20:19:59.060'),
(504, 57, 2, 3, NULL, N'VHS009-195', 450000.00, 380000.00, 16.00, 20, NULL, 1, '2026-06-11T20:19:59.060'),
(505, 57, 2, 4, NULL, N'VHS009-196', 450000.00, 450000.00, NULL, 21, NULL, 1, '2026-06-11T20:19:59.060'),
(506, 57, 5, 1, NULL, N'VHS009-197', 450000.00, 450000.00, NULL, 22, NULL, 1, '2026-06-11T20:19:59.060'),
(507, 57, 5, 2, NULL, N'VHS009-198', 450000.00, 380000.00, 16.00, 23, NULL, 1, '2026-06-11T20:19:59.060'),
(508, 57, 5, 3, NULL, N'VHS009-199', 450000.00, 450000.00, NULL, 24, NULL, 1, '2026-06-11T20:19:59.063'),
(509, 57, 5, 4, NULL, N'VHS009-200', 450000.00, 450000.00, NULL, 5, NULL, 1, '2026-06-11T20:19:59.063'),
(510, 58, 2, 1, NULL, N'VCS005-201', 990000.00, 840000.00, 15.00, 6, NULL, 1, '2026-06-11T20:19:59.063'),
(511, 58, 2, 2, NULL, N'VCS005-202', 990000.00, 990000.00, NULL, 7, NULL, 1, '2026-06-11T20:19:59.063'),
(512, 58, 2, 3, NULL, N'VCS005-203', 990000.00, 990000.00, NULL, 8, NULL, 1, '2026-06-11T20:19:59.066'),
(513, 58, 2, 4, NULL, N'VCS005-204', 990000.00, 840000.00, 15.00, 9, NULL, 1, '2026-06-11T20:19:59.066'),
(514, 58, 5, 1, NULL, N'VCS005-205', 990000.00, 990000.00, NULL, 10, NULL, 1, '2026-06-11T20:19:59.066'),
(515, 58, 5, 2, NULL, N'VCS005-206', 990000.00, 990000.00, NULL, 11, NULL, 1, '2026-06-11T20:19:59.066'),
(516, 58, 5, 3, NULL, N'VCS005-207', 990000.00, 840000.00, 15.00, 12, NULL, 1, '2026-06-11T20:19:59.070'),
(517, 58, 5, 4, NULL, N'VCS005-208', 990000.00, 990000.00, NULL, 13, NULL, 1, '2026-06-11T20:19:59.070'),
(518, 59, 2, 1, NULL, N'VCS006-209', 990000.00, 990000.00, NULL, 14, NULL, 1, '2026-06-11T20:19:59.070'),
(519, 59, 2, 2, NULL, N'VCS006-210', 990000.00, 840000.00, 15.00, 15, NULL, 1, '2026-06-11T20:19:59.073'),
(520, 59, 2, 3, NULL, N'VCS006-211', 990000.00, 990000.00, NULL, 16, NULL, 1, '2026-06-11T20:19:59.073'),
(521, 59, 2, 4, NULL, N'VCS006-212', 990000.00, 990000.00, NULL, 17, NULL, 1, '2026-06-11T20:19:59.073'),
(522, 59, 5, 1, NULL, N'VCS006-213', 990000.00, 840000.00, 15.00, 18, NULL, 1, '2026-06-11T20:19:59.076'),
(523, 59, 5, 2, NULL, N'VCS006-214', 990000.00, 990000.00, NULL, 19, NULL, 1, '2026-06-11T20:19:59.076'),
(524, 59, 5, 3, NULL, N'VCS006-215', 990000.00, 990000.00, NULL, 20, NULL, 1, '2026-06-11T20:19:59.076'),
(525, 59, 5, 4, NULL, N'VCS006-216', 990000.00, 840000.00, 15.00, 21, NULL, 1, '2026-06-11T20:19:59.076'),
(526, 60, 2, 1, NULL, N'VCS007-217', 990000.00, 990000.00, NULL, 22, NULL, 1, '2026-06-11T20:19:59.080'),
(527, 60, 2, 2, NULL, N'VCS007-218', 990000.00, 990000.00, NULL, 23, NULL, 1, '2026-06-11T20:19:59.080'),
(528, 60, 2, 3, NULL, N'VCS007-219', 990000.00, 840000.00, 15.00, 24, NULL, 1, '2026-06-11T20:19:59.080'),
(529, 60, 2, 4, NULL, N'VCS007-220', 990000.00, 990000.00, NULL, 5, NULL, 1, '2026-06-11T20:19:59.080'),
(530, 60, 5, 1, NULL, N'VCS007-221', 990000.00, 990000.00, NULL, 6, NULL, 1, '2026-06-11T20:19:59.080'),
(531, 60, 5, 2, NULL, N'VCS007-222', 990000.00, 840000.00, 15.00, 7, NULL, 1, '2026-06-11T20:19:59.080'),
(532, 60, 5, 3, NULL, N'VCS007-223', 990000.00, 990000.00, NULL, 8, NULL, 1, '2026-06-11T20:19:59.083'),
(533, 60, 5, 4, NULL, N'VCS007-224', 990000.00, 990000.00, NULL, 9, NULL, 1, '2026-06-11T20:19:59.083'),
(534, 61, 2, 1, NULL, N'VCS008-225', 990000.00, 840000.00, 15.00, 10, NULL, 1, '2026-06-11T20:19:59.083'),
(535, 61, 2, 2, NULL, N'VCS008-226', 990000.00, 990000.00, NULL, 11, NULL, 1, '2026-06-11T20:19:59.083'),
(536, 61, 2, 3, NULL, N'VCS008-227', 990000.00, 990000.00, NULL, 12, NULL, 1, '2026-06-11T20:19:59.086'),
(537, 61, 2, 4, NULL, N'VCS008-228', 990000.00, 840000.00, 15.00, 13, NULL, 1, '2026-06-11T20:19:59.086'),
(538, 61, 5, 1, NULL, N'VCS008-229', 990000.00, 990000.00, NULL, 14, NULL, 1, '2026-06-11T20:19:59.086'),
(539, 61, 5, 2, NULL, N'VCS008-230', 990000.00, 990000.00, NULL, 15, NULL, 1, '2026-06-11T20:19:59.090'),
(540, 61, 5, 3, NULL, N'VCS008-231', 990000.00, 840000.00, 15.00, 16, NULL, 1, '2026-06-11T20:19:59.090'),
(541, 61, 5, 4, NULL, N'VCS008-232', 990000.00, 990000.00, NULL, 17, NULL, 1, '2026-06-11T20:19:59.090'),
(542, 62, 2, 1, NULL, N'VCS009-233', 990000.00, 990000.00, NULL, 18, NULL, 1, '2026-06-11T20:19:59.090'),
(543, 62, 2, 2, NULL, N'VCS009-234', 990000.00, 840000.00, 15.00, 19, NULL, 1, '2026-06-11T20:19:59.093'),
(544, 62, 2, 3, NULL, N'VCS009-235', 990000.00, 990000.00, NULL, 20, NULL, 1, '2026-06-11T20:19:59.093'),
(545, 62, 2, 4, NULL, N'VCS009-236', 990000.00, 990000.00, NULL, 21, NULL, 1, '2026-06-11T20:19:59.096'),
(546, 62, 5, 1, NULL, N'VCS009-237', 990000.00, 840000.00, 15.00, 22, NULL, 1, '2026-06-11T20:19:59.096'),
(547, 62, 5, 2, NULL, N'VCS009-238', 990000.00, 990000.00, NULL, 23, NULL, 1, '2026-06-11T20:19:59.096'),
(548, 62, 5, 3, NULL, N'VCS009-239', 990000.00, 990000.00, NULL, 24, NULL, 1, '2026-06-11T20:19:59.096'),
(549, 62, 5, 4, NULL, N'VCS009-240', 990000.00, 840000.00, 15.00, 5, NULL, 1, '2026-06-11T20:19:59.100');
SET IDENTITY_INSERT [dbo].[Vay_chi_tiet] OFF;
END
GO

-- ===== Anh =====
IF OBJECT_ID(N'dbo.Anh','U') IS NULL
BEGIN
CREATE TABLE [dbo].[Anh] (
  [id] int IDENTITY(1,1) NOT NULL,
  [id_vay] int NOT NULL,
  [anh_url] nvarchar(500) NOT NULL,
  [trang_thai] tinyint NULL,
  [ngay_tao] datetime2(7) NULL,
  CONSTRAINT [PK_Anh] PRIMARY KEY ([id])
);
END
GO
IF NOT EXISTS (SELECT 1 FROM [dbo].[Anh])
BEGIN
SET IDENTITY_INSERT [dbo].[Anh] ON;
INSERT INTO [dbo].[Anh] ([id], [id_vay], [anh_url], [trang_thai], [ngay_tao]) VALUES
(1, 1, N'/images/products/dress1.jpg', 1, '2026-06-12T17:38:33.610'),
(2, 1, N'/images/products/shirt1.jpg', 1, '2026-06-12T17:38:33.623'),
(3, 1, N'/images/products/pants1.jpg', 1, '2026-06-12T17:38:33.623'),
(4, 1, N'/images/products/accessories1.jpg', 1, '2026-06-12T17:38:33.623'),
(5, 2, N'/images/products/dress2.jpg', 1, '2026-06-12T17:38:33.626'),
(6, 2, N'/images/products/shirt2.jpg', 1, '2026-06-12T17:38:33.630'),
(7, 2, N'/images/products/pants2.jpg', 1, '2026-06-12T17:38:33.630'),
(8, 2, N'/images/products/accessories2.jpg', 1, '2026-06-12T17:38:33.630'),
(9, 3, N'/images/products/dress3.jpg', 1, '2026-06-12T17:38:33.633'),
(10, 3, N'/images/products/shirt3.jpg', 1, '2026-06-12T17:38:33.636'),
(11, 3, N'/images/products/pants3.jpg', 1, '2026-06-12T17:38:33.636'),
(12, 3, N'/images/products/accessories3.jpg', 1, '2026-06-12T17:38:33.636'),
(13, 4, N'/images/products/dress4.jpg', 1, '2026-06-12T17:38:33.640'),
(14, 4, N'/images/products/shirt4.jpg', 1, '2026-06-12T17:38:33.646'),
(15, 4, N'/images/products/pants4.jpg', 1, '2026-06-12T17:38:33.646'),
(16, 4, N'/images/products/accessories4.jpg', 1, '2026-06-12T17:38:33.646'),
(17, 5, N'/images/products/dress5.jpg', 1, '2026-06-12T17:38:33.646'),
(18, 5, N'/images/products/shirt5.jpg', 1, '2026-06-12T17:38:33.653'),
(19, 5, N'/images/products/pants5.jpg', 1, '2026-06-12T17:38:33.653'),
(20, 5, N'/images/products/accessories5.jpg', 1, '2026-06-12T17:38:33.653'),
(21, 6, N'/images/products/dress6.jpg', 1, '2026-06-12T17:38:33.653'),
(22, 6, N'/images/products/shirt6.jpg', 1, '2026-06-12T17:38:33.660'),
(23, 6, N'/images/products/pants6.jpg', 1, '2026-06-12T17:38:33.660'),
(24, 6, N'/images/products/accessories6.jpg', 1, '2026-06-12T17:38:33.660'),
(25, 9, N'/images/products/dress7.jpg', 1, '2026-06-12T17:38:33.660'),
(26, 9, N'/images/products/shirt7.jpg', 1, '2026-06-12T17:38:33.666'),
(27, 9, N'/images/products/pants7.jpg', 1, '2026-06-12T17:38:33.666'),
(28, 9, N'/images/products/accessories7.jpg', 1, '2026-06-12T17:38:33.666'),
(29, 10, N'/images/products/dress8.jpg', 1, '2026-06-12T17:38:33.670'),
(30, 10, N'/images/products/shirt8.jpg', 1, '2026-06-12T17:38:33.673'),
(31, 10, N'/images/products/pants8.jpg', 1, '2026-06-12T17:38:33.673'),
(32, 10, N'/images/products/accessories8.jpg', 1, '2026-06-12T17:38:33.673'),
(33, 11, N'/images/products/dress9.jpg', 1, '2026-06-12T17:38:33.676'),
(34, 11, N'/images/products/shirt9.jpg', 1, '2026-06-12T17:38:33.683'),
(35, 11, N'/images/products/pants9.jpg', 1, '2026-06-12T17:38:33.683'),
(36, 11, N'/images/products/accessories9.jpg', 1, '2026-06-12T17:38:33.683'),
(37, 12, N'/images/products/dress10.jpg', 1, '2026-06-12T17:38:33.683'),
(38, 12, N'/images/products/shirt10.jpg', 1, '2026-06-12T17:38:33.690'),
(39, 12, N'/images/products/pants10.jpg', 1, '2026-06-12T17:38:33.690'),
(40, 12, N'/images/products/accessories10.jpg', 1, '2026-06-12T17:38:33.690'),
(41, 13, N'/images/products/dress11.jpg', 1, '2026-06-12T17:38:33.690'),
(42, 13, N'/images/products/shirt11.jpg', 1, '2026-06-12T17:38:33.696'),
(43, 13, N'/images/products/pants11.jpg', 1, '2026-06-12T17:38:33.696'),
(44, 13, N'/images/products/accessories11.jpg', 1, '2026-06-12T17:38:33.696'),
(45, 14, N'/images/products/dress12.jpg', 1, '2026-06-12T17:38:33.696'),
(46, 14, N'/images/products/shirt12.jpg', 1, '2026-06-12T17:38:33.703'),
(47, 14, N'/images/products/pants12.jpg', 1, '2026-06-12T17:38:33.703'),
(48, 14, N'/images/products/accessories12.jpg', 1, '2026-06-12T17:38:33.703'),
(49, 15, N'/images/products/dress13.jpg', 1, '2026-06-12T17:38:33.706'),
(50, 15, N'/images/products/shirt13.jpg', 1, '2026-06-12T17:38:33.710'),
(51, 15, N'/images/products/pants13.jpg', 1, '2026-06-12T17:38:33.710'),
(52, 15, N'/images/products/accessories13.jpg', 1, '2026-06-12T17:38:33.710'),
(53, 16, N'/images/products/dress14.jpg', 1, '2026-06-12T17:38:33.713'),
(54, 16, N'/images/products/shirt14.jpg', 1, '2026-06-12T17:38:33.720'),
(55, 16, N'/images/products/pants14.jpg', 1, '2026-06-12T17:38:33.720'),
(56, 16, N'/images/products/accessories14.jpg', 1, '2026-06-12T17:38:33.720'),
(57, 17, N'/images/products/dress15.jpg', 1, '2026-06-12T17:38:33.720'),
(58, 17, N'/images/products/shirt15.jpg', 1, '2026-06-12T17:38:33.726'),
(59, 17, N'/images/products/pants15.jpg', 1, '2026-06-12T17:38:33.726'),
(60, 17, N'/images/products/accessories15.jpg', 1, '2026-06-12T17:38:33.726'),
(61, 18, N'/images/products/dress16.jpg', 1, '2026-06-12T17:38:33.726'),
(62, 18, N'/images/products/shirt17.jpg', 1, '2026-06-12T17:38:33.733'),
(63, 18, N'/images/products/pants16.jpg', 1, '2026-06-12T17:38:33.733'),
(64, 18, N'/images/products/accessories16.jpg', 1, '2026-06-12T17:38:33.733'),
(65, 19, N'/images/products/dress17.jpg', 1, '2026-06-12T17:38:33.736'),
(66, 19, N'/images/products/shirt18.jpg', 1, '2026-06-12T17:38:33.743'),
(67, 19, N'/images/products/pants17.jpg', 1, '2026-06-12T17:38:33.743'),
(68, 19, N'/images/products/accessories17.jpg', 1, '2026-06-12T17:38:33.743'),
(69, 20, N'/images/products/dress18.jpg', 1, '2026-06-12T17:38:33.743'),
(70, 20, N'/images/products/shirt19.jpg', 1, '2026-06-12T17:38:33.773'),
(71, 20, N'/images/products/pants18.jpg', 1, '2026-06-12T17:38:33.773'),
(72, 20, N'/images/products/accessories18.jpg', 1, '2026-06-12T17:38:33.773'),
(73, 21, N'/images/products/dress19.jpg', 1, '2026-06-12T17:38:33.776'),
(74, 21, N'/images/products/shirt20.jpg', 1, '2026-06-12T17:38:33.780'),
(75, 21, N'/images/products/pants19.jpg', 1, '2026-06-12T17:38:33.780'),
(76, 21, N'/images/products/accessories19.jpg', 1, '2026-06-12T17:38:33.780'),
(77, 22, N'/images/products/dress20.jpg', 1, '2026-06-12T17:38:33.783'),
(78, 22, N'/images/products/shirt1.jpg', 1, '2026-06-12T17:38:33.790'),
(79, 22, N'/images/products/pants20.jpg', 1, '2026-06-12T17:38:33.790'),
(80, 22, N'/images/products/accessories20.jpg', 1, '2026-06-12T17:38:33.790'),
(81, 23, N'/images/products/dress1.jpg', 1, '2026-06-12T17:38:33.790'),
(82, 23, N'/images/products/shirt2.jpg', 1, '2026-06-12T17:38:33.796'),
(83, 23, N'/images/products/pants1.jpg', 1, '2026-06-12T17:38:33.796'),
(84, 23, N'/images/products/accessories1.jpg', 1, '2026-06-12T17:38:33.796'),
(85, 24, N'/images/products/dress2.jpg', 1, '2026-06-12T17:38:33.800'),
(86, 24, N'/images/products/shirt3.jpg', 1, '2026-06-12T17:38:33.806'),
(87, 24, N'/images/products/pants2.jpg', 1, '2026-06-12T17:38:33.806'),
(88, 24, N'/images/products/accessories2.jpg', 1, '2026-06-12T17:38:33.806'),
(89, 25, N'/images/products/dress3.jpg', 1, '2026-06-12T17:38:33.806'),
(90, 25, N'/images/products/shirt4.jpg', 1, '2026-06-12T17:38:33.813'),
(91, 25, N'/images/products/pants3.jpg', 1, '2026-06-12T17:38:33.813'),
(92, 25, N'/images/products/accessories3.jpg', 1, '2026-06-12T17:38:33.813'),
(93, 26, N'/images/products/dress4.jpg', 1, '2026-06-12T17:38:33.816'),
(94, 26, N'/images/products/shirt5.jpg', 1, '2026-06-12T17:38:33.823'),
(95, 26, N'/images/products/pants4.jpg', 1, '2026-06-12T17:38:33.823'),
(96, 26, N'/images/products/accessories4.jpg', 1, '2026-06-12T17:38:33.823'),
(97, 27, N'/images/products/dress5.jpg', 1, '2026-06-12T17:38:33.823'),
(98, 27, N'/images/products/shirt6.jpg', 1, '2026-06-12T17:38:33.830'),
(99, 27, N'/images/products/pants5.jpg', 1, '2026-06-12T17:38:33.830'),
(100, 27, N'/images/products/accessories5.jpg', 1, '2026-06-12T17:38:33.830'),
(101, 28, N'/images/products/dress6.jpg', 1, '2026-06-12T17:38:33.830'),
(102, 28, N'/images/products/shirt7.jpg', 1, '2026-06-12T17:38:33.836'),
(103, 28, N'/images/products/pants6.jpg', 1, '2026-06-12T17:38:33.836'),
(104, 28, N'/images/products/accessories6.jpg', 1, '2026-06-12T17:38:33.836'),
(105, 29, N'/images/products/dress7.jpg', 1, '2026-06-12T17:38:33.840'),
(106, 29, N'/images/products/shirt8.jpg', 1, '2026-06-12T17:38:33.843'),
(107, 29, N'/images/products/pants7.jpg', 1, '2026-06-12T17:38:33.843'),
(108, 29, N'/images/products/accessories7.jpg', 1, '2026-06-12T17:38:33.843'),
(109, 30, N'/images/products/dress8.jpg', 1, '2026-06-12T17:38:33.846'),
(110, 30, N'/images/products/shirt9.jpg', 1, '2026-06-12T17:38:33.850'),
(111, 30, N'/images/products/pants8.jpg', 1, '2026-06-12T17:38:33.850'),
(112, 30, N'/images/products/accessories8.jpg', 1, '2026-06-12T17:38:33.850'),
(113, 31, N'/images/products/dress9.jpg', 1, '2026-06-12T17:38:33.853'),
(114, 31, N'/images/products/shirt10.jpg', 1, '2026-06-12T17:38:33.860'),
(115, 31, N'/images/products/pants9.jpg', 1, '2026-06-12T17:38:33.860'),
(116, 31, N'/images/products/accessories9.jpg', 1, '2026-06-12T17:38:33.860'),
(117, 32, N'/images/products/dress10.jpg', 1, '2026-06-12T17:38:33.860'),
(118, 32, N'/images/products/shirt11.jpg', 1, '2026-06-12T17:38:33.866'),
(119, 32, N'/images/products/pants10.jpg', 1, '2026-06-12T17:38:33.866'),
(120, 32, N'/images/products/accessories10.jpg', 1, '2026-06-12T17:38:33.866'),
(121, 33, N'/images/products/dress11.jpg', 1, '2026-06-12T17:38:33.870'),
(122, 33, N'/images/products/shirt12.jpg', 1, '2026-06-12T17:38:33.873'),
(123, 33, N'/images/products/pants11.jpg', 1, '2026-06-12T17:38:33.873'),
(124, 33, N'/images/products/accessories11.jpg', 1, '2026-06-12T17:38:33.873'),
(125, 34, N'/images/products/dress12.jpg', 1, '2026-06-12T17:38:33.876'),
(126, 34, N'/images/products/shirt13.jpg', 1, '2026-06-12T17:38:33.880'),
(127, 34, N'/images/products/pants12.jpg', 1, '2026-06-12T17:38:33.880'),
(128, 34, N'/images/products/accessories12.jpg', 1, '2026-06-12T17:38:33.880'),
(129, 35, N'/images/products/dress13.jpg', 1, '2026-06-12T17:38:33.883'),
(130, 35, N'/images/products/shirt14.jpg', 1, '2026-06-12T17:38:33.890'),
(131, 35, N'/images/products/pants13.jpg', 1, '2026-06-12T17:38:33.890'),
(132, 35, N'/images/products/accessories13.jpg', 1, '2026-06-12T17:38:33.890'),
(133, 36, N'/images/products/dress14.jpg', 1, '2026-06-12T17:38:33.890'),
(134, 36, N'/images/products/shirt15.jpg', 1, '2026-06-12T17:38:33.896'),
(135, 36, N'/images/products/pants14.jpg', 1, '2026-06-12T17:38:33.896'),
(136, 36, N'/images/products/accessories14.jpg', 1, '2026-06-12T17:38:33.896'),
(137, 37, N'/images/products/dress15.jpg', 1, '2026-06-12T17:38:33.896'),
(138, 37, N'/images/products/shirt17.jpg', 1, '2026-06-12T17:38:33.903'),
(139, 37, N'/images/products/pants15.jpg', 1, '2026-06-12T17:38:33.903'),
(140, 37, N'/images/products/accessories15.jpg', 1, '2026-06-12T17:38:33.903'),
(141, 38, N'/images/products/dress16.jpg', 1, '2026-06-12T17:38:33.903'),
(142, 38, N'/images/products/shirt18.jpg', 1, '2026-06-12T17:38:33.910'),
(143, 38, N'/images/products/pants16.jpg', 1, '2026-06-12T17:38:33.910'),
(144, 38, N'/images/products/accessories16.jpg', 1, '2026-06-12T17:38:33.910'),
(145, 39, N'/images/products/dress17.jpg', 1, '2026-06-12T17:38:33.910'),
(146, 39, N'/images/products/shirt19.jpg', 1, '2026-06-12T17:38:33.916'),
(147, 39, N'/images/products/pants17.jpg', 1, '2026-06-12T17:38:33.916'),
(148, 39, N'/images/products/accessories17.jpg', 1, '2026-06-12T17:38:33.916'),
(149, 40, N'/images/products/dress18.jpg', 1, '2026-06-12T17:38:33.916'),
(150, 40, N'/images/products/shirt20.jpg', 1, '2026-06-12T17:38:33.923'),
(151, 40, N'/images/products/pants18.jpg', 1, '2026-06-12T17:38:33.923'),
(152, 40, N'/images/products/accessories18.jpg', 1, '2026-06-12T17:38:33.923'),
(153, 41, N'/images/products/dress19.jpg', 1, '2026-06-12T17:38:33.926'),
(154, 41, N'/images/products/shirt1.jpg', 1, '2026-06-12T17:38:33.930'),
(155, 41, N'/images/products/pants19.jpg', 1, '2026-06-12T17:38:33.930'),
(156, 41, N'/images/products/accessories19.jpg', 1, '2026-06-12T17:38:33.930'),
(157, 42, N'/images/products/dress20.jpg', 1, '2026-06-12T17:38:33.933'),
(158, 42, N'/images/products/shirt2.jpg', 1, '2026-06-12T17:38:33.936'),
(159, 42, N'/images/products/pants20.jpg', 1, '2026-06-12T17:38:33.936'),
(160, 42, N'/images/products/accessories20.jpg', 1, '2026-06-12T17:38:33.936'),
(161, 43, N'/images/products/dress1.jpg', 1, '2026-06-12T17:38:33.940'),
(162, 43, N'/images/products/shirt3.jpg', 1, '2026-06-12T17:38:33.943'),
(163, 43, N'/images/products/pants1.jpg', 1, '2026-06-12T17:38:33.943'),
(164, 43, N'/images/products/accessories1.jpg', 1, '2026-06-12T17:38:33.943'),
(165, 44, N'/images/products/dress2.jpg', 1, '2026-06-12T17:38:33.946'),
(166, 44, N'/images/products/shirt4.jpg', 1, '2026-06-12T17:38:33.950'),
(167, 44, N'/images/products/pants2.jpg', 1, '2026-06-12T17:38:33.950'),
(168, 44, N'/images/products/accessories2.jpg', 1, '2026-06-12T17:38:33.950'),
(169, 45, N'/images/products/dress3.jpg', 1, '2026-06-12T17:38:33.953'),
(170, 45, N'/images/products/shirt5.jpg', 1, '2026-06-12T17:38:33.960'),
(171, 45, N'/images/products/pants3.jpg', 1, '2026-06-12T17:38:33.960'),
(172, 45, N'/images/products/accessories3.jpg', 1, '2026-06-12T17:38:33.960'),
(173, 46, N'/images/products/dress4.jpg', 1, '2026-06-12T17:38:33.960'),
(174, 46, N'/images/products/shirt6.jpg', 1, '2026-06-12T17:38:33.963'),
(175, 46, N'/images/products/pants4.jpg', 1, '2026-06-12T17:38:33.963'),
(176, 46, N'/images/products/accessories4.jpg', 1, '2026-06-12T17:38:33.963'),
(177, 47, N'/images/products/dress5.jpg', 1, '2026-06-12T17:38:33.966'),
(178, 47, N'/images/products/shirt7.jpg', 1, '2026-06-12T17:38:33.970'),
(179, 47, N'/images/products/pants5.jpg', 1, '2026-06-12T17:38:33.970'),
(180, 47, N'/images/products/accessories5.jpg', 1, '2026-06-12T17:38:33.970'),
(181, 48, N'/images/products/dress6.jpg', 1, '2026-06-12T17:38:33.973'),
(182, 48, N'/images/products/shirt8.jpg', 1, '2026-06-12T17:38:33.976'),
(183, 48, N'/images/products/pants6.jpg', 1, '2026-06-12T17:38:33.976'),
(184, 48, N'/images/products/accessories6.jpg', 1, '2026-06-12T17:38:33.976'),
(185, 49, N'/images/products/dress7.jpg', 1, '2026-06-12T17:38:33.980'),
(186, 49, N'/images/products/shirt9.jpg', 1, '2026-06-12T17:38:33.983'),
(187, 49, N'/images/products/pants7.jpg', 1, '2026-06-12T17:38:33.983'),
(188, 49, N'/images/products/accessories7.jpg', 1, '2026-06-12T17:38:33.983'),
(189, 50, N'/images/products/dress8.jpg', 1, '2026-06-12T17:38:33.986'),
(190, 50, N'/images/products/shirt10.jpg', 1, '2026-06-12T17:38:33.990'),
(191, 50, N'/images/products/pants8.jpg', 1, '2026-06-12T17:38:33.990'),
(192, 50, N'/images/products/accessories8.jpg', 1, '2026-06-12T17:38:33.990'),
(193, 51, N'/images/products/dress9.jpg', 1, '2026-06-12T17:38:33.993'),
(194, 51, N'/images/products/shirt11.jpg', 1, '2026-06-12T17:38:33.996'),
(195, 51, N'/images/products/pants9.jpg', 1, '2026-06-12T17:38:33.996'),
(196, 51, N'/images/products/accessories9.jpg', 1, '2026-06-12T17:38:33.996'),
(197, 52, N'/images/products/dress10.jpg', 1, '2026-06-12T17:38:34.000'),
(198, 52, N'/images/products/shirt12.jpg', 1, '2026-06-12T17:38:34.006'),
(199, 52, N'/images/products/pants10.jpg', 1, '2026-06-12T17:38:34.006'),
(200, 52, N'/images/products/accessories10.jpg', 1, '2026-06-12T17:38:34.006');
INSERT INTO [dbo].[Anh] ([id], [id_vay], [anh_url], [trang_thai], [ngay_tao]) VALUES
(201, 53, N'/images/products/dress11.jpg', 1, '2026-06-12T17:38:34.006'),
(202, 53, N'/images/products/shirt13.jpg', 1, '2026-06-12T17:38:34.013'),
(203, 53, N'/images/products/pants11.jpg', 1, '2026-06-12T17:38:34.013'),
(204, 53, N'/images/products/accessories11.jpg', 1, '2026-06-12T17:38:34.013'),
(205, 54, N'/images/products/dress12.jpg', 1, '2026-06-12T17:38:34.013'),
(206, 54, N'/images/products/shirt14.jpg', 1, '2026-06-12T17:38:34.020'),
(207, 54, N'/images/products/pants12.jpg', 1, '2026-06-12T17:38:34.020'),
(208, 54, N'/images/products/accessories12.jpg', 1, '2026-06-12T17:38:34.020'),
(209, 55, N'/images/products/dress13.jpg', 1, '2026-06-12T17:38:34.020'),
(210, 55, N'/images/products/shirt15.jpg', 1, '2026-06-12T17:38:34.026'),
(211, 55, N'/images/products/pants13.jpg', 1, '2026-06-12T17:38:34.026'),
(212, 55, N'/images/products/accessories13.jpg', 1, '2026-06-12T17:38:34.026'),
(213, 56, N'/images/products/dress14.jpg', 1, '2026-06-12T17:38:34.030'),
(214, 56, N'/images/products/shirt17.jpg', 1, '2026-06-12T17:38:34.033'),
(215, 56, N'/images/products/pants14.jpg', 1, '2026-06-12T17:38:34.033'),
(216, 56, N'/images/products/accessories14.jpg', 1, '2026-06-12T17:38:34.033'),
(217, 57, N'/images/products/dress15.jpg', 1, '2026-06-12T17:38:34.036'),
(218, 57, N'/images/products/shirt18.jpg', 1, '2026-06-12T17:38:34.040'),
(219, 57, N'/images/products/pants15.jpg', 1, '2026-06-12T17:38:34.040'),
(220, 57, N'/images/products/accessories15.jpg', 1, '2026-06-12T17:38:34.040'),
(221, 58, N'/images/products/dress16.jpg', 1, '2026-06-12T17:38:34.043'),
(222, 58, N'/images/products/shirt19.jpg', 1, '2026-06-12T17:38:34.050'),
(223, 58, N'/images/products/pants16.jpg', 1, '2026-06-12T17:38:34.050'),
(224, 58, N'/images/products/accessories16.jpg', 1, '2026-06-12T17:38:34.050'),
(225, 59, N'/images/products/dress17.jpg', 1, '2026-06-12T17:38:34.050'),
(226, 59, N'/images/products/shirt20.jpg', 1, '2026-06-12T17:38:34.056'),
(227, 59, N'/images/products/pants17.jpg', 1, '2026-06-12T17:38:34.056'),
(228, 59, N'/images/products/accessories17.jpg', 1, '2026-06-12T17:38:34.056'),
(229, 60, N'/images/products/dress18.jpg', 1, '2026-06-12T17:38:34.056'),
(230, 60, N'/images/products/shirt1.jpg', 1, '2026-06-12T17:38:34.063'),
(231, 60, N'/images/products/pants18.jpg', 1, '2026-06-12T17:38:34.063'),
(232, 60, N'/images/products/accessories18.jpg', 1, '2026-06-12T17:38:34.063'),
(233, 61, N'/images/products/dress19.jpg', 1, '2026-06-12T17:38:34.066'),
(234, 61, N'/images/products/shirt2.jpg', 1, '2026-06-12T17:38:34.070'),
(235, 61, N'/images/products/pants19.jpg', 1, '2026-06-12T17:38:34.070'),
(236, 61, N'/images/products/accessories19.jpg', 1, '2026-06-12T17:38:34.070'),
(237, 62, N'/images/products/dress20.jpg', 1, '2026-06-12T17:38:34.073'),
(238, 62, N'/images/products/shirt3.jpg', 1, '2026-06-12T17:38:34.080'),
(239, 62, N'/images/products/pants20.jpg', 1, '2026-06-12T17:38:34.080'),
(240, 62, N'/images/products/accessories20.jpg', 1, '2026-06-12T17:38:34.080');
SET IDENTITY_INSERT [dbo].[Anh] OFF;
END
GO

-- ===== Gio_hang =====
IF OBJECT_ID(N'dbo.Gio_hang','U') IS NULL
BEGIN
CREATE TABLE [dbo].[Gio_hang] (
  [id] int IDENTITY(1,1) NOT NULL,
  [id_khach_hang] int NOT NULL,
  [ngay_tao] datetime2(7) NULL,
  CONSTRAINT [PK_Gio_hang] PRIMARY KEY ([id])
);
END
GO
IF NOT EXISTS (SELECT 1 FROM [dbo].[Gio_hang])
BEGIN
SET IDENTITY_INSERT [dbo].[Gio_hang] ON;
INSERT INTO [dbo].[Gio_hang] ([id], [id_khach_hang], [ngay_tao]) VALUES
(1, 1, '2026-06-10T23:44:29.230'),
(2, 2, '2026-06-10T23:44:29.230'),
(3, 3, '2026-06-10T23:44:29.230');
SET IDENTITY_INSERT [dbo].[Gio_hang] OFF;
END
GO

-- ===== Gio_hang_chi_tiet =====
IF OBJECT_ID(N'dbo.Gio_hang_chi_tiet','U') IS NULL
BEGIN
CREATE TABLE [dbo].[Gio_hang_chi_tiet] (
  [id] int IDENTITY(1,1) NOT NULL,
  [id_gio_hang] int NOT NULL,
  [id_vay_chi_tiet] int NOT NULL,
  [so_luong] int NULL,
  [ngay_tao] datetime2(7) NULL,
  CONSTRAINT [PK_Gio_hang_chi_tiet] PRIMARY KEY ([id])
);
END
GO
IF NOT EXISTS (SELECT 1 FROM [dbo].[Gio_hang_chi_tiet])
BEGIN
SET IDENTITY_INSERT [dbo].[Gio_hang_chi_tiet] ON;
INSERT INTO [dbo].[Gio_hang_chi_tiet] ([id], [id_gio_hang], [id_vay_chi_tiet], [so_luong], [ngay_tao]) VALUES
(1, 1, 1, 1, '2026-06-10T23:44:29.243'),
(2, 1, 6, 2, '2026-06-10T23:44:29.243'),
(3, 2, 9, 1, '2026-06-10T23:44:29.243'),
(4, 2, 12, 1, '2026-06-10T23:44:29.243'),
(5, 3, 15, 1, '2026-06-10T23:44:29.243');
SET IDENTITY_INSERT [dbo].[Gio_hang_chi_tiet] OFF;
END
GO

-- ===== Hoa_don =====
IF OBJECT_ID(N'dbo.Hoa_don','U') IS NULL
BEGIN
CREATE TABLE [dbo].[Hoa_don] (
  [id] int IDENTITY(1,1) NOT NULL,
  [id_khach_hang] int NULL,
  [id_khuyen_mai] int NULL,
  [id_giam_gia] int NULL,
  [id_nhan_vien] int NULL,
  [ma_hoa_don] nvarchar(80) NOT NULL,
  [tong_tien] decimal(15,2) NOT NULL,
  [phi_van_chuyen] decimal(15,2) NULL,
  [giam_gia_khuyen_mai] decimal(15,2) NULL,
  [hinh_thuc_nhan_hang] tinyint NULL,
  [dia_chi_giao_hang] nvarchar(500) NULL,
  [trang_thai] tinyint NULL,
  [hinh_thuc_thanh_toan] nvarchar(50) NULL,
  [phuong_thuc_thanh_toan_online] nvarchar(50) NULL,
  [ghi_chu] nvarchar(max) NULL,
  [ngay_tao] datetime2(7) NULL,
  CONSTRAINT [PK_Hoa_don] PRIMARY KEY ([id])
);
END
GO
IF NOT EXISTS (SELECT 1 FROM [dbo].[Hoa_don])
BEGIN
SET IDENTITY_INSERT [dbo].[Hoa_don] ON;
INSERT INTO [dbo].[Hoa_don] ([id], [id_khach_hang], [id_khuyen_mai], [id_giam_gia], [id_nhan_vien], [ma_hoa_don], [tong_tien], [phi_van_chuyen], [giam_gia_khuyen_mai], [hinh_thuc_nhan_hang], [dia_chi_giao_hang], [trang_thai], [hinh_thuc_thanh_toan], [phuong_thuc_thanh_toan_online], [ghi_chu], [ngay_tao]) VALUES
(1, 1, NULL, NULL, 2, N'HD-2025-0001', 6070000.00, 0.00, 0.00, 1, N'128 Trần Hưng Đạo, Hoàn Kiếm, Hà Nội', 0, N'VNPay', NULL, NULL, '2026-06-10T23:44:29.256'),
(2, 1, NULL, NULL, 2, N'HD-2025-0002', 8580000.00, 0.00, 0.00, 1, N'128 Trần Hưng Đạo, Hoàn Kiếm, Hà Nội', 1, N'COD', NULL, NULL, '2026-06-10T23:44:29.256'),
(3, 2, NULL, NULL, 2, N'HD-2025-0003', 4290000.00, 0.00, 0.00, 1, N'45 Nguyễn Huệ, Quận 1, TP. Hồ Chí Minh', 2, N'Momo', NULL, NULL, '2026-06-10T23:44:29.256'),
(4, 3, NULL, NULL, 2, N'HD-2025-0004', 1290000.00, 0.00, 0.00, 2, NULL, 4, N'Ti?n m?t', NULL, NULL, '2026-06-10T23:44:29.256'),
(5, 2, NULL, NULL, 2, N'HD-2025-0005', 2780000.00, 0.00, 0.00, 1, N'45 Nguyễn Huệ, Quận 1, TP. Hồ Chí Minh', 3, N'VNPay', NULL, NULL, '2026-06-10T23:44:29.256'),
(6, NULL, NULL, NULL, NULL, N'HD2606111347176783', 2890000.00, 0.00, 0.00, 1, N'Ha Noi', 0, N'VNPAY', NULL, NULL, '2026-06-11T13:47:17.973'),
(7, 1, NULL, NULL, NULL, N'HD2606111353090522', 9960000.00, 0.00, 0.00, 1, N'123 Nguyen Hue, Quan 1, TP.HCM', 0, N'COD', NULL, N'', '2026-06-11T13:53:09.543'),
(8, NULL, NULL, NULL, NULL, N'HD2606111354569125', 1590000.00, 0.00, 0.00, 1, N'456 Le Loi, Q1, HCM', 1, N'VNPAY', N'VNPAY', NULL, '2026-06-11T13:54:56.363'),
(9, 2, NULL, NULL, NULL, N'HD2606111532350499', 8960000.00, 0.00, 0.00, 1, N'nguyen thi due', 0, N'MOMO', NULL, N'', '2026-06-11T15:32:35.619'),
(10, 2, NULL, NULL, NULL, N'HD2606112024161968', 2890000.00, 0.00, 0.00, 1, N'Nguyen Thi Due', 0, N'MOMO', NULL, N'', '2026-06-11T20:24:16.605'),
(11, 2, NULL, NULL, NULL, N'HD2606112025033498', 1590000.00, 0.00, 0.00, 1, N'dsgdsagsdfa', 0, N'COD', NULL, N'', '2026-06-11T20:25:03.378'),
(12, 2, NULL, NULL, NULL, N'HD2606112025367305', 2290000.00, 0.00, 0.00, 1, N'sdfh', 0, N'VNPAY', NULL, N'', '2026-06-11T20:25:36.473'),
(13, 2, NULL, NULL, NULL, N'HD2606112042420187', 1590000.00, 0.00, 0.00, 1, N'rutyj', 0, N'MOMO', NULL, N'', '2026-06-11T20:42:42.031'),
(14, 2, NULL, NULL, NULL, N'HD2606112100518437', 450000.00, 0.00, 0.00, 1, N'dfgbvv', 0, N'COD', NULL, N'', '2026-06-11T21:00:51.411'),
(15, 2, NULL, NULL, NULL, N'HD2606132334116643', 2890000.00, 0.00, 0.00, 1, N'áduiygthjkns', 0, N'VNPAY', NULL, N'', '2026-06-13T23:34:11.591'),
(16, 2, NULL, NULL, NULL, N'HD2606132335439207', 2890000.00, 0.00, 0.00, 1, N'trygjhk', 0, N'MOMO', NULL, N'', '2026-06-13T23:35:44.008'),
(17, NULL, NULL, NULL, NULL, N'HD2606132337193159', 4290000.00, 0.00, 0.00, 1, N'fdg', 0, N'VNPAY', NULL, N'', '2026-06-13T23:37:19.247'),
(18, NULL, NULL, NULL, NULL, N'HD2606132339031838', 4480000.00, 0.00, 0.00, 1, N'fsdhsdfhsdfh', 0, N'VNPAY', NULL, N'', '2026-06-13T23:39:03.522'),
(19, 2, NULL, NULL, NULL, N'HD2606141048179586', 2890000.00, 0.00, 0.00, 1, N'nịuodasfkugyhtadsfokjhig', 3, N'VNPAY', NULL, N'', '2026-06-14T10:48:17.814'),
(20, 2, NULL, NULL, NULL, N'HD2606160939088988', 2890000.00, 0.00, 0.00, 1, N'she', 0, N'VNPAY', NULL, N'', '2026-06-16T09:39:08.022');
SET IDENTITY_INSERT [dbo].[Hoa_don] OFF;
END
GO

-- ===== Hoa_don_chi_tiet =====
IF OBJECT_ID(N'dbo.Hoa_don_chi_tiet','U') IS NULL
BEGIN
CREATE TABLE [dbo].[Hoa_don_chi_tiet] (
  [id] int IDENTITY(1,1) NOT NULL,
  [id_hoa_don] int NOT NULL,
  [id_vay_chi_tiet] int NOT NULL,
  [so_luong] int NOT NULL,
  [don_gia] decimal(15,2) NOT NULL,
  [phan_tram_giam] decimal(5,2) NULL,
  [thanh_tien] decimal(15,2) NULL,
  CONSTRAINT [PK_Hoa_don_chi_tiet] PRIMARY KEY ([id])
);
END
GO
IF NOT EXISTS (SELECT 1 FROM [dbo].[Hoa_don_chi_tiet])
BEGIN
SET IDENTITY_INSERT [dbo].[Hoa_don_chi_tiet] ON;
INSERT INTO [dbo].[Hoa_don_chi_tiet] ([id], [id_hoa_don], [id_vay_chi_tiet], [so_luong], [don_gia], [phan_tram_giam], [thanh_tien]) VALUES
(1, 1, 1, 1, 2890000.00, 0.00, 2890000.00),
(2, 1, 6, 2, 1590000.00, 0.00, 3180000.00),
(3, 2, 9, 2, 4290000.00, 0.00, 8580000.00),
(4, 3, 9, 1, 4290000.00, 0.00, 4290000.00),
(5, 4, 12, 1, 1290000.00, 0.00, 1290000.00),
(6, 5, 6, 1, 1590000.00, 0.00, 1590000.00),
(7, 5, 15, 1, 1390000.00, 0.00, 1390000.00),
(8, 6, 1, 1, 2890000.00, 0.00, 2890000.00),
(9, 7, 1, 1, 2890000.00, 0.00, 2890000.00),
(10, 7, 9, 1, 4290000.00, 21.86, 3352206.00),
(11, 7, 15, 2, 1390000.00, 17.75, 2286550.00),
(12, 8, 6, 1, 1590000.00, 20.10, 1270410.00),
(13, 9, 1, 2, 2890000.00, 0.00, 5780000.00),
(14, 9, 6, 2, 1590000.00, 20.10, 2540820.00),
(15, 10, 1, 1, 2890000.00, 0.00, 2890000.00),
(16, 11, 6, 1, 1590000.00, 20.10, 1270410.00),
(17, 12, 83, 1, 2290000.00, 20.76, 1814596.00),
(18, 13, 6, 1, 1590000.00, 20.10, 1270410.00),
(19, 14, 470, 1, 450000.00, 0.00, 450000.00),
(20, 15, 1, 1, 2890000.00, 0.00, 2890000.00),
(21, 16, 1, 1, 2890000.00, 0.00, 2890000.00),
(22, 17, 9, 1, 4290000.00, 21.86, 3352206.00),
(23, 18, 6, 1, 1590000.00, 20.10, 1270410.00),
(24, 18, 1, 1, 2890000.00, 0.00, 2890000.00),
(25, 19, 1, 1, 2890000.00, 0.00, 2890000.00),
(26, 20, 1, 1, 2890000.00, 0.00, 2890000.00);
SET IDENTITY_INSERT [dbo].[Hoa_don_chi_tiet] OFF;
END
GO

-- ===== Lich_su_thanh_toan =====
IF OBJECT_ID(N'dbo.Lich_su_thanh_toan','U') IS NULL
BEGIN
CREATE TABLE [dbo].[Lich_su_thanh_toan] (
  [id] int IDENTITY(1,1) NOT NULL,
  [id_hoa_don] int NOT NULL,
  [so_tien] decimal(15,2) NOT NULL,
  [phuong_thuc] nvarchar(50) NOT NULL,
  [ma_giao_dich] nvarchar(200) NULL,
  [trang_thai] nvarchar(50) NULL,
  [noi_dung] nvarchar(max) NULL,
  [ngay_tao] datetime2(7) NULL,
  CONSTRAINT [PK_Lich_su_thanh_toan] PRIMARY KEY ([id])
);
END
GO
IF NOT EXISTS (SELECT 1 FROM [dbo].[Lich_su_thanh_toan])
BEGIN
SET IDENTITY_INSERT [dbo].[Lich_su_thanh_toan] ON;
INSERT INTO [dbo].[Lich_su_thanh_toan] ([id], [id_hoa_don], [so_tien], [phuong_thuc], [ma_giao_dich], [trang_thai], [noi_dung], [ngay_tao]) VALUES
(1, 1, 6070000.00, N'VNPay', N'VNP20250101001', N'success', NULL, '2026-06-10T23:44:29.296'),
(2, 3, 4290000.00, N'Momo', N'MOMO20250115001', N'success', NULL, '2026-06-10T23:44:29.296'),
(3, 4, 1290000.00, N'Tiền mặt', NULL, N'success', NULL, '2026-06-10T23:44:29.296'),
(4, 5, 2780000.00, N'VNPay', N'VNP20250120001', N'pending', NULL, '2026-06-10T23:44:29.296'),
(5, 8, 1590000.00, N'VNPAY', N'DEMO123456', N'SUCCESS', N'Thanh toán VNPay thành công - HD2606111354569125', '2026-06-11T13:55:09.146');
SET IDENTITY_INSERT [dbo].[Lich_su_thanh_toan] OFF;
END
GO

-- ===== Danh_gia =====
IF OBJECT_ID(N'dbo.Danh_gia','U') IS NULL
BEGIN
CREATE TABLE [dbo].[Danh_gia] (
  [id] int IDENTITY(1,1) NOT NULL,
  [id_khach_hang] int NOT NULL,
  [id_vay] int NOT NULL,
  [id_hoa_don] int NULL,
  [so_sao] tinyint NOT NULL,
  [noi_dung] nvarchar(max) NULL,
  [anh_danh_gia] nvarchar(500) NULL,
  [trang_thai] tinyint NULL,
  [ngay_tao] datetime2(7) NULL,
  CONSTRAINT [PK_Danh_gia] PRIMARY KEY ([id])
);
END
GO
IF NOT EXISTS (SELECT 1 FROM [dbo].[Danh_gia])
BEGIN
SET IDENTITY_INSERT [dbo].[Danh_gia] ON;
INSERT INTO [dbo].[Danh_gia] ([id], [id_khach_hang], [id_vay], [id_hoa_don], [so_sao], [noi_dung], [anh_danh_gia], [trang_thai], [ngay_tao]) VALUES
(1, 1, 1, 1, 5, N'Váy rất đẹp, chất liệu lụa mềm mại, mặc rất thoải mái!', NULL, 1, '2026-06-10T23:44:29.286'),
(2, 1, 2, 1, 4, N'Váy voan nhẹ nhàng, nhưng hơi dài so với mình', NULL, 1, '2026-06-10T23:44:29.286'),
(3, 2, 3, 3, 5, N'Váy dạ hội tuyệt vời, chất gấm sang trọng', NULL, 1, '2026-06-10T23:44:29.286'),
(4, 3, 4, 4, 4, N'Váy công sở đẹp, giá hợp lý', NULL, 1, '2026-06-10T23:44:29.286');
SET IDENTITY_INSERT [dbo].[Danh_gia] OFF;
END
GO

-- ===== San_pham_yeu_thich =====
IF OBJECT_ID(N'dbo.San_pham_yeu_thich','U') IS NULL
BEGIN
CREATE TABLE [dbo].[San_pham_yeu_thich] (
  [id] int IDENTITY(1,1) NOT NULL,
  [id_khach_hang] int NOT NULL,
  [id_vay] int NOT NULL,
  [ngay_tao] datetime2(7) NULL,
  CONSTRAINT [PK_San_pham_yeu_thich] PRIMARY KEY ([id])
);
END
GO

-- ===== Lich_su_xem =====
IF OBJECT_ID(N'dbo.Lich_su_xem','U') IS NULL
BEGIN
CREATE TABLE [dbo].[Lich_su_xem] (
  [id] int IDENTITY(1,1) NOT NULL,
  [id_khach_hang] int NULL,
  [id_vay] int NOT NULL,
  [ngay_xem] datetime2(7) NULL,
  CONSTRAINT [PK_Lich_su_xem] PRIMARY KEY ([id])
);
END
GO

-- ===== Thong_bao =====
IF OBJECT_ID(N'dbo.Thong_bao','U') IS NULL
BEGIN
CREATE TABLE [dbo].[Thong_bao] (
  [id] int IDENTITY(1,1) NOT NULL,
  [id_khach_hang] int NULL,
  [id_nhan_vien] int NULL,
  [tieu_de] nvarchar(255) NOT NULL,
  [noi_dung] nvarchar(max) NULL,
  [loai] nvarchar(50) NULL,
  [da_doc] tinyint NULL,
  [ngay_tao] datetime2(7) NULL,
  CONSTRAINT [PK_Thong_bao] PRIMARY KEY ([id])
);
END
GO

-- ===== Nhat_ky =====
IF OBJECT_ID(N'dbo.Nhat_ky','U') IS NULL
BEGIN
CREATE TABLE [dbo].[Nhat_ky] (
  [id] int IDENTITY(1,1) NOT NULL,
  [id_nhan_vien] int NULL,
  [hanh_dong] nvarchar(100) NOT NULL,
  [bang_tac_dong] nvarchar(100) NULL,
  [id_ban_ghi] int NULL,
  [chi_tiet] nvarchar(max) NULL,
  [dia_chi_ip] nvarchar(50) NULL,
  [ngay_tao] datetime2(7) NULL,
  CONSTRAINT [PK_Nhat_ky] PRIMARY KEY ([id])
);
END
GO
IF NOT EXISTS (SELECT 1 FROM [dbo].[Nhat_ky])
BEGIN
SET IDENTITY_INSERT [dbo].[Nhat_ky] ON;
INSERT INTO [dbo].[Nhat_ky] ([id], [id_nhan_vien], [hanh_dong], [bang_tac_dong], [id_ban_ghi], [chi_tiet], [dia_chi_ip], [ngay_tao]) VALUES
(1, NULL, N'UPDATE', N'Hoa_don', 1, N'Trạng thái đổi từ 3 sang 1', NULL, '2026-06-11T12:12:14.160'),
(2, NULL, N'UPDATE', N'Hoa_don', 1, N'Trạng thái đổi từ 1 sang 4', NULL, '2026-06-11T12:12:18.260'),
(3, NULL, N'UPDATE', N'Hoa_don', 1, N'Trạng thái đổi từ 4 sang 3', NULL, '2026-06-11T12:36:28.690'),
(4, NULL, N'UPDATE', N'Hoa_don', 2, N'Trạng thái đổi từ 2 sang 3', NULL, '2026-06-11T12:36:31.046'),
(5, NULL, N'UPDATE', N'Hoa_don', 4, N'Trạng thái đổi từ 1 sang 3', NULL, '2026-06-11T12:36:33.296'),
(6, NULL, N'UPDATE', N'Hoa_don', 5, N'Trạng thái đổi từ 0 sang 3', NULL, '2026-06-11T12:36:35.070'),
(7, NULL, N'UPDATE', N'Hoa_don', 1, N'Trạng thái đổi từ 3 sang 1', NULL, '2026-06-11T12:38:26.426'),
(8, NULL, N'UPDATE', N'Hoa_don', 2, N'Trạng thái đổi từ 3 sang 0', NULL, '2026-06-11T12:38:28.060'),
(9, NULL, N'UPDATE', N'Hoa_don', 3, N'Trạng thái đổi từ 3 sang 2', NULL, '2026-06-11T12:38:29.970'),
(10, NULL, N'UPDATE', N'Hoa_don', 4, N'Trạng thái đổi từ 3 sang 4', NULL, '2026-06-11T12:38:32.426'),
(11, NULL, N'UPDATE', N'Hoa_don', 8, N'Trạng thái đổi từ 0 sang 1', NULL, '2026-06-11T13:55:09.140'),
(12, NULL, N'UPDATE', N'Hoa_don', 2, N'Trạng thái đổi từ 0 sang 1', NULL, '2026-06-14T10:03:26.300'),
(13, NULL, N'UPDATE', N'Hoa_don', 1, N'Trạng thái đổi từ 1 sang 3', NULL, '2026-06-14T10:57:35.533'),
(14, NULL, N'UPDATE', N'Hoa_don', 1, N'Trạng thái đổi từ 3 sang 1', NULL, '2026-06-14T10:57:37.920'),
(15, NULL, N'UPDATE', N'Hoa_don', 1, N'Trạng thái đổi từ 1 sang 0', NULL, '2026-06-14T13:08:20.150'),
(16, NULL, N'UPDATE', N'Hoa_don', 19, N'Trạng thái đổi từ 0 sang 1', NULL, '2026-06-14T15:20:51.593'),
(17, NULL, N'UPDATE', N'Hoa_don', 19, N'Trạng thái đổi từ 1 sang 2', NULL, '2026-06-14T15:20:55.976'),
(18, NULL, N'UPDATE', N'Hoa_don', 19, N'Trạng thái đổi từ 2 sang 3', NULL, '2026-06-14T15:21:02.356');
SET IDENTITY_INSERT [dbo].[Nhat_ky] OFF;
END
GO

