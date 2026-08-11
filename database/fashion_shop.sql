-- ============================================================
-- ZESTIA DATABASE fashion_shop
-- File SQL chinh de cai dat du lieu demo hoan chinh cho du an.
-- Cach dung: mo file nay trong SSMS/sqlcmd va Execute 1 lan.
-- Luu y: nen backup database cu truoc khi chay tren may that.
-- ============================================================
-- ============================================================
-- ZESTIA  -  fashion_shop  (schema + du lieu day du)
-- File chuan, tu dong export tu DB dang chay.
-- Cach dung: mo SSMS hoac sqlcmd, mo file nay, Execute 1 lan.
-- ============================================================
USE master;
GO
IF DB_ID('fashion_shop') IS NOT NULL
BEGIN
    ALTER DATABASE [fashion_shop] SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE [fashion_shop];
END
GO
CREATE DATABASE [fashion_shop];
GO
USE [fashion_shop];
GO
SET ANSI_NULLS ON;
SET ANSI_PADDING ON;
SET ANSI_WARNINGS ON;
SET ARITHABORT ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET NUMERIC_ROUNDABORT OFF;
SET QUOTED_IDENTIFIER ON;
GO
SET XACT_ABORT ON;
BEGIN TRANSACTION;
GO

-- ===== MIGRATION TỰ ĐỘNG TỪ BẢNG CŨ (NẾU CÓ) =====
IF OBJECT_ID(N'dbo.Loai_vay', 'U') IS NOT NULL AND OBJECT_ID(N'dbo.loai_san_pham', 'U') IS NULL
    EXEC sp_rename 'dbo.Loai_vay', 'loai_san_pham';

IF OBJECT_ID(N'dbo.Vay', 'U') IS NOT NULL AND OBJECT_ID(N'dbo.san_pham', 'U') IS NULL
    EXEC sp_rename 'dbo.Vay', 'san_pham';

IF OBJECT_ID(N'dbo.Vay_chi_tiet', 'U') IS NOT NULL AND OBJECT_ID(N'dbo.san_pham_chi_tiet', 'U') IS NULL
    EXEC sp_rename 'dbo.Vay_chi_tiet', 'san_pham_chi_tiet';

IF COL_LENGTH('dbo.loai_san_pham', 'ten_loai_vay') IS NOT NULL
    EXEC sp_rename 'dbo.loai_san_pham.ten_loai_vay', 'ten_loai_san_pham', 'COLUMN';

IF COL_LENGTH('dbo.san_pham', 'ma_vay') IS NOT NULL
    EXEC sp_rename 'dbo.san_pham.ma_vay', 'ma_san_pham', 'COLUMN';

IF COL_LENGTH('dbo.san_pham', 'ten_vay') IS NOT NULL
    EXEC sp_rename 'dbo.san_pham.ten_vay', 'ten_san_pham', 'COLUMN';

IF COL_LENGTH('dbo.san_pham', 'id_loai_vay') IS NOT NULL
    EXEC sp_rename 'dbo.san_pham.id_loai_vay', 'id_loai_san_pham', 'COLUMN';

IF COL_LENGTH('dbo.san_pham_chi_tiet', 'id_vay') IS NOT NULL
    EXEC sp_rename 'dbo.san_pham_chi_tiet.id_vay', 'id_san_pham', 'COLUMN';

IF COL_LENGTH('dbo.san_pham_chi_tiet', 'ma_vay_chi_tiet') IS NOT NULL
    EXEC sp_rename 'dbo.san_pham_chi_tiet.ma_vay_chi_tiet', 'ma_san_pham_chi_tiet', 'COLUMN';

IF COL_LENGTH('dbo.Anh', 'id_vay') IS NOT NULL
    EXEC sp_rename 'dbo.Anh.id_vay', 'id_san_pham', 'COLUMN';

IF COL_LENGTH('dbo.Danh_gia', 'id_vay') IS NOT NULL
    EXEC sp_rename 'dbo.Danh_gia.id_vay', 'id_san_pham', 'COLUMN';

IF COL_LENGTH('dbo.Gio_hang_chi_tiet', 'id_vay_chi_tiet') IS NOT NULL
    EXEC sp_rename 'dbo.Gio_hang_chi_tiet.id_vay_chi_tiet', 'id_san_pham_chi_tiet', 'COLUMN';

IF COL_LENGTH('dbo.Hoa_don_chi_tiet', 'id_vay_chi_tiet') IS NOT NULL
    EXEC sp_rename 'dbo.Hoa_don_chi_tiet.id_vay_chi_tiet', 'id_san_pham_chi_tiet', 'COLUMN';

IF COL_LENGTH('dbo.Bien_dong_ton_kho', 'id_vay_chi_tiet') IS NOT NULL
    EXEC sp_rename 'dbo.Bien_dong_ton_kho.id_vay_chi_tiet', 'id_san_pham_chi_tiet', 'COLUMN';

IF COL_LENGTH('dbo.Pham_vi_khuyen_mai', 'id_vay') IS NOT NULL
    EXEC sp_rename 'dbo.Pham_vi_khuyen_mai.id_vay', 'id_san_pham', 'COLUMN';

IF COL_LENGTH('dbo.Pham_vi_khuyen_mai', 'id_loai_vay') IS NOT NULL
    EXEC sp_rename 'dbo.Pham_vi_khuyen_mai.id_loai_vay', 'id_loai_san_pham', 'COLUMN';

IF COL_LENGTH('dbo.Huong_dan_kich_thuoc', 'id_vay') IS NOT NULL
    EXEC sp_rename 'dbo.Huong_dan_kich_thuoc.id_vay', 'id_san_pham', 'COLUMN';

IF COL_LENGTH('dbo.Lich_su_xem', 'id_vay') IS NOT NULL
    EXEC sp_rename 'dbo.Lich_su_xem.id_vay', 'id_san_pham', 'COLUMN';

IF COL_LENGTH('dbo.San_pham_yeu_thich', 'id_vay') IS NOT NULL
    EXEC sp_rename 'dbo.San_pham_yeu_thich.id_vay', 'id_san_pham', 'COLUMN';

IF COL_LENGTH('dbo.Pos_chi_tiet_giu_hang', 'id_vay_chi_tiet') IS NOT NULL
    EXEC sp_rename 'dbo.Pos_chi_tiet_giu_hang.id_vay_chi_tiet', 'id_san_pham_chi_tiet', 'COLUMN';

IF COL_LENGTH('dbo.Yeu_cau_doi_tra', 'id_vay_chi_tiet_cu') IS NOT NULL
    EXEC sp_rename 'dbo.Yeu_cau_doi_tra.id_vay_chi_tiet_cu', 'id_san_pham_chi_tiet_cu', 'COLUMN';

IF COL_LENGTH('dbo.Yeu_cau_doi_tra', 'id_vay_chi_tiet_moi') IS NOT NULL
    EXEC sp_rename 'dbo.Yeu_cau_doi_tra.id_vay_chi_tiet_moi', 'id_san_pham_chi_tiet_moi', 'COLUMN';
GO

-- ===== Vai_tro =====
IF OBJECT_ID(N'dbo.Vai_tro','U') IS NULL
BEGIN
CREATE TABLE [dbo].[Vai_tro] (
  [id] int IDENTITY(1,1) NOT NULL,
  [ten_vai_tro] nvarchar(100) NOT NULL,
  CONSTRAINT [PK_Vai_tro] PRIMARY KEY ([id]),
  CONSTRAINT [UQ_Vai_tro_ten] UNIQUE ([ten_vai_tro])
);
END
GO
IF NOT EXISTS (SELECT 1 FROM [dbo].[Vai_tro])
BEGIN
SET IDENTITY_INSERT [dbo].[Vai_tro] ON;
INSERT INTO [dbo].[Vai_tro] ([id], [ten_vai_tro]) VALUES
(1, N'Admin'),
(2, N'Nhân viên');
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
  CONSTRAINT [PK_Nhan_vien] PRIMARY KEY ([id]),
  CONSTRAINT [UQ_Nhan_vien_ma] UNIQUE ([ma_nhan_vien]),
  CONSTRAINT [UQ_Nhan_vien_email] UNIQUE ([email]),
  CONSTRAINT [UQ_Nhan_vien_ten_nguoi_dung] UNIQUE ([ten_nguoi_dung]),
  CONSTRAINT [FK_Nhan_vien_Vai_tro] FOREIGN KEY ([id_vai_tro]) REFERENCES [dbo].[Vai_tro]([id])
);
END
GO
IF NOT EXISTS (SELECT 1 FROM [dbo].[Nhan_vien])
BEGIN
SET IDENTITY_INSERT [dbo].[Nhan_vien] ON;
INSERT INTO [dbo].[Nhan_vien] ([id], [id_vai_tro], [ma_nhan_vien], [ho_va_ten], [gioi_tinh], [ngay_sinh], [so_dien_thoai], [dia_chi], [email], [ten_nguoi_dung], [mat_khau], [tinh_trang_lam_viec], [ngay_tao]) VALUES
(1, 1, N'NV001', N'Admin', NULL, NULL, NULL, NULL, N'admin@zestia.vn', N'admin', N'$2a$10$1319tfuwROs5099h0RHfbeEV.RarbCu15eZh09TwTuRsFznGC0Zze', 1, '2026-06-10T23:44:29.193'),
(2, 2, N'NV002', N'Trần Minh Tuấn', NULL, NULL, N'0901234567', NULL, N'tuan@zestia.vn', N'tuannv', N'$2a$10$csfRWdR./6P2bikv1yGV5uoJSBEiNQFgrdM9tqkWFv5CDE.BasFY6', 1, '2026-06-10T23:44:29.200'),
(3, 2, N'NV003', N'Lê Hoàng Phúc', 1, '1997-09-12', N'0907654321', N'Quận 1, TP.HCM', N'phuc@zestia.vn', N'phucnv', N'$2a$10$csfRWdR./6P2bikv1yGV5uoJSBEiNQFgrdM9tqkWFv5CDE.BasFY6', 1, '2026-06-23T09:00:00.000');
SET IDENTITY_INSERT [dbo].[Nhan_vien] OFF;
END
GO

-- ===== Lich_lam_viec =====
IF OBJECT_ID(N'dbo.Lich_lam_viec','U') IS NULL
BEGIN
CREATE TABLE [dbo].[Lich_lam_viec] (
  [id] int IDENTITY(1,1) NOT NULL,
  [id_nhan_vien] int NULL,
  [ngay_lam] date NOT NULL,
  [ca_lam] nvarchar(50) NULL,
  [gio_bat_dau] time(0) NOT NULL,
  [gio_ket_thuc] time(0) NOT NULL,
  [ghi_chu] nvarchar(255) NULL,
  [trang_thai] tinyint NULL,
  [ly_do_bao_ban] nvarchar(500) NULL,
  [phan_hoi_bao_ban] nvarchar(500) NULL,
  [thoi_gian_xac_nhan] datetime2(7) NULL,
  [thoi_gian_bao_ban] datetime2(7) NULL,
  [thoi_gian_duyet] datetime2(7) NULL,
  [nguoi_duyet] nvarchar(150) NULL,
  [gio_check_in] datetime2(7) NULL,
  [gio_check_out] datetime2(7) NULL,
  [ngay_tao] datetime2(7) NULL,
  CONSTRAINT [PK_Lich_lam_viec] PRIMARY KEY ([id]),
  CONSTRAINT [FK_Lich_lam_viec_Nhan_vien] FOREIGN KEY ([id_nhan_vien]) REFERENCES [dbo].[Nhan_vien]([id])
);
END
GO
IF COL_LENGTH('dbo.Lich_lam_viec', 'ly_do_bao_ban') IS NULL
  ALTER TABLE [dbo].[Lich_lam_viec] ADD [ly_do_bao_ban] nvarchar(500) NULL;
GO
IF COL_LENGTH('dbo.Lich_lam_viec', 'phan_hoi_bao_ban') IS NULL
  ALTER TABLE [dbo].[Lich_lam_viec] ADD [phan_hoi_bao_ban] nvarchar(500) NULL;
GO
IF COL_LENGTH('dbo.Lich_lam_viec', 'thoi_gian_xac_nhan') IS NULL
  ALTER TABLE [dbo].[Lich_lam_viec] ADD [thoi_gian_xac_nhan] datetime2(7) NULL;
GO
IF COL_LENGTH('dbo.Lich_lam_viec', 'thoi_gian_bao_ban') IS NULL
  ALTER TABLE [dbo].[Lich_lam_viec] ADD [thoi_gian_bao_ban] datetime2(7) NULL;
GO
IF COL_LENGTH('dbo.Lich_lam_viec', 'thoi_gian_duyet') IS NULL
  ALTER TABLE [dbo].[Lich_lam_viec] ADD [thoi_gian_duyet] datetime2(7) NULL;
GO
IF COL_LENGTH('dbo.Lich_lam_viec', 'nguoi_duyet') IS NULL
  ALTER TABLE [dbo].[Lich_lam_viec] ADD [nguoi_duyet] nvarchar(150) NULL;
GO
IF COL_LENGTH('dbo.Lich_lam_viec', 'gio_check_in') IS NULL
  ALTER TABLE [dbo].[Lich_lam_viec] ADD [gio_check_in] datetime2(7) NULL;
GO
IF COL_LENGTH('dbo.Lich_lam_viec', 'gio_check_out') IS NULL
  ALTER TABLE [dbo].[Lich_lam_viec] ADD [gio_check_out] datetime2(7) NULL;
GO
IF NOT EXISTS (SELECT 1 FROM [dbo].[Lich_lam_viec])
BEGIN
SET IDENTITY_INSERT [dbo].[Lich_lam_viec] ON;
INSERT INTO [dbo].[Lich_lam_viec] ([id], [id_nhan_vien], [ngay_lam], [ca_lam], [gio_bat_dau], [gio_ket_thuc], [ghi_chu], [trang_thai], [ngay_tao]) VALUES
(1, 1, '2026-06-22', N'Ca sáng', '08:00:00', '12:00:00', N'Trực quản lý cửa hàng', 1, '2026-06-22T08:00:00'),
(2, 2, '2026-06-22', N'Ca chiều', '13:00:00', '17:00:00', N'Tư vấn khách và kiểm hàng', 1, '2026-06-22T08:00:00'),
(3, 2, '2026-06-23', N'Ca sáng', '08:00:00', '12:00:00', N'Hỗ trợ bán tại quầy', 0, '2026-06-22T08:00:00'),
(4, 1, '2026-06-29', N'Ca sáng', '08:00:00', '12:00:00', N'Ca trực đầu tuần - Admin', 1, '2026-06-29T08:00:00'),
(5, 2, '2026-06-29', N'Ca chiều', '13:00:00', '17:00:00', N'Bán hàng ca chiều', 1, '2026-06-29T08:00:00'),
(6, 2, '2026-06-30', N'Ca sáng', '08:00:00', '12:00:00', N'Kiểm tra hàng tồn kho', 1, '2026-06-29T08:00:00'),
(7, 3, '2026-07-01', N'Ca sáng', '08:00:00', '12:00:00', N'Kiểm tra và sắp xếp hàng', 1, '2026-06-29T08:00:00'),
(8, 1, '2026-07-01', N'Ca chiều', '13:00:00', '17:00:00', N'Họp giao ban giữa tuần', 1, '2026-06-29T08:00:00'),
(9, 2, '2026-07-01', N'Ca tối', '18:00:00', '22:00:00', N'Trực ca tối bán hàng', 0, '2026-06-29T08:00:00'),
(10, 3, '2026-07-02', N'Ca sáng', '08:00:00', '12:00:00', N'Sắp xếp kệ hàng', 1, '2026-06-29T08:00:00'),
(11, 2, '2026-07-03', N'Ca chiều', '13:00:00', '17:00:00', N'Tư vấn trực tuyến', 1, '2026-06-29T08:00:00');
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
  [google_subject] nvarchar(100) NULL,
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
(2, N'KH002', N'Phạm Thị Hương', 1, '1998-07-22', N'0923456789', N'huong@email.com', N'$2a$10$ODu0aFkPU/3g7rm.k2CNMemX5S8xX1pQyK9wre2.m3H38pdJgO//K', '2026-06-10T23:44:29.206'),
(3, N'KH003', N'Lê Văn Minh', 1, '1992-11-08', N'0934567890', N'minh@email.com', N'$2a$10$TtnHfT8nBplciIuAZBeBzeB5bCClZ3hYtKSgZXfyvNryoHVBa5SXS', '2026-06-10T23:44:29.206'),
(4, N'KH00004', N'Test User', NULL, NULL, N'0901234567', N'test@zestia.vn', N'$2a$10$v57XvllkZqAlI0bAOZOZ4u5I/uXqKdzhesaLfCRhyRVj9FyUmBzxO', '2026-06-11T14:02:40.926'),
(5, N'KH005', N'Nguyễn Gia Bảo', NULL, NULL, N'0900000005', N'nguyengiabaoo2008@gmail.com', N'$2a$10$csfRWdR./6P2bikv1yGV5uoJSBEiNQFgrdM9tqkWFv5CDE.BasFY6', '2026-07-10T09:30:00.000'),
(6, N'KH006', N'Ngọc Anh', NULL, NULL, N'0900000006', N'ngocanh2701ss@gmail.com', N'$2a$10$csfRWdR./6P2bikv1yGV5uoJSBEiNQFgrdM9tqkWFv5CDE.BasFY6', '2026-07-10T09:30:00.000'),
(7, N'KH007', N'Hoàng Anh Minh', NULL, NULL, N'0900000007', N'hoanganhminh110706@gmail.com', N'$2a$10$csfRWdR./6P2bikv1yGV5uoJSBEiNQFgrdM9tqkWFv5CDE.BasFY6', '2026-07-10T09:30:00.000'),
(8, N'KH008', N'Thủy NP', NULL, NULL, N'0900000008', N'Thuynpth06788@gmail.com', N'$2a$10$csfRWdR./6P2bikv1yGV5uoJSBEiNQFgrdM9tqkWFv5CDE.BasFY6', '2026-07-10T09:30:00.000'),
(9, N'KH009', N'Tony VN', NULL, NULL, N'0900000009', N'tonyvn081106@gmail.com', N'$2a$10$csfRWdR./6P2bikv1yGV5uoJSBEiNQFgrdM9tqkWFv5CDE.BasFY6', '2026-07-10T09:30:00.000'),
(10, N'KH010', N'Nguyễn Thành', NULL, NULL, N'0900000010', N'nguyenthanh.hn090307@gmail.com', N'$2a$10$csfRWdR./6P2bikv1yGV5uoJSBEiNQFgrdM9tqkWFv5CDE.BasFY6', '2026-07-10T09:30:00.000'),
(11, N'KH011', N'Bảo Nguyễn', NULL, NULL, N'0900000011', N'baongts01859@gmail.com', N'$2a$10$csfRWdR./6P2bikv1yGV5uoJSBEiNQFgrdM9tqkWFv5CDE.BasFY6', '2026-07-10T09:30:00.000');
SET IDENTITY_INSERT [dbo].[Khach_hang] OFF;
END
GO
IF COL_LENGTH('dbo.Khach_hang', 'diem_tich_luy') IS NULL
  ALTER TABLE [dbo].[Khach_hang] ADD [diem_tich_luy] int NULL DEFAULT 0;
GO
IF COL_LENGTH('dbo.Khach_hang', 'tong_chi_tieu') IS NULL
  ALTER TABLE [dbo].[Khach_hang] ADD [tong_chi_tieu] decimal(18,2) NULL DEFAULT 0;
GO
IF COL_LENGTH('dbo.Khach_hang', 'hang_thanh_vien') IS NULL
  ALTER TABLE [dbo].[Khach_hang] ADD [hang_thanh_vien] nvarchar(50) NULL DEFAULT N'Đồng';
GO

-- ===== Newsletter_subscriber =====
IF OBJECT_ID(N'dbo.Newsletter_subscriber','U') IS NULL
BEGIN
CREATE TABLE [dbo].[Newsletter_subscriber] (
  [id] int IDENTITY(1,1) NOT NULL,
  [email] nvarchar(150) NOT NULL,
  [trang_thai] tinyint NULL DEFAULT 1,
  [ngay_dang_ky] datetime2(7) NULL DEFAULT GETDATE(),
  [ngay_cap_nhat] datetime2(7) NULL DEFAULT GETDATE(),
  CONSTRAINT [PK_Newsletter_subscriber] PRIMARY KEY ([id]),
  CONSTRAINT [UQ_Newsletter_subscriber_email] UNIQUE ([email])
);
END
GO

-- ===== Password_reset_token =====
IF OBJECT_ID(N'dbo.Password_reset_token','U') IS NULL
BEGIN
CREATE TABLE [dbo].[Password_reset_token] (
  [id] int IDENTITY(1,1) NOT NULL,
  [token] nvarchar(100) NOT NULL,
  [account_type] nvarchar(20) NOT NULL,
  [account_id] int NOT NULL,
  [expires_at] datetime2(7) NOT NULL,
  [used_at] datetime2(7) NULL,
  [created_at] datetime2(7) NULL DEFAULT GETDATE(),
  CONSTRAINT [PK_Password_reset_token] PRIMARY KEY ([id]),
  CONSTRAINT [UQ_Password_reset_token_token] UNIQUE ([token])
);
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

-- ===== loai_san_pham =====
IF OBJECT_ID(N'dbo.loai_san_pham','U') IS NULL
BEGIN
CREATE TABLE [dbo].[loai_san_pham] (
  [id] int IDENTITY(1,1) NOT NULL,
  [ten_loai_san_pham] nvarchar(150) NOT NULL,
  [trang_thai] tinyint NULL,
  [mo_ta] nvarchar(max) NULL,
  [ngay_tao] datetime2(7) NULL,
  [chieu_cao_nguoi_mau] int NULL,
  [can_nang_nguoi_mau] int NULL,
  [size_nguoi_mau] nvarchar(30) NULL,
  [mo_ta_phom] nvarchar(500) NULL,
  CONSTRAINT [PK_loai_san_pham] PRIMARY KEY ([id])
);
END
GO
IF NOT EXISTS (SELECT 1 FROM [dbo].[loai_san_pham])
BEGIN
SET IDENTITY_INSERT [dbo].[loai_san_pham] ON;
INSERT INTO [dbo].[loai_san_pham] ([id], [ten_loai_san_pham], [trang_thai], [mo_ta], [ngay_tao]) VALUES
(1, N'Áo thời trang', 1, N'Áo sơ mi, áo kiểu, áo thun thời trang nam nữ cao cấp', '2026-06-10T23:44:29.116'),
(2, N'Quần & Jeans', 1, N'Quần tây, quần jeans, quần culottes, quần short cao cấp', '2026-06-10T23:44:29.116'),
(3, N'Váy & Đầm', 1, N'Váy dạ hội, đầm dạo phố, đầm xòe, đầm ôm thanh lịch', '2026-06-10T23:44:29.116'),
(4, N'Phụ kiện thời trang', 1, N'Túi xách, thắt lưng, khăn lụa, mũ thời trang', '2026-06-10T23:44:29.116'),
(5, N'Trang phục công sở', 1, N'Set trang phục công sở thanh lịch, chuyên nghiệp', '2026-06-10T23:44:29.116'),
(6, N'Trang phục dự tiệc', 1, N'Thiết kế dành cho tiệc tối, sinh nhật và các dịp gặp gỡ.', '2026-06-10T23:44:29.116'),
(7, N'Áo khoác & Blazer', 1, N'Áo khoác dạ, blazer công sở, áo khoác mỏng cao cấp', '2026-06-10T23:44:29.116');
SET IDENTITY_INSERT [dbo].[loai_san_pham] OFF;
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
(1, N'ZESTIA10', N'Giảm 10% đơn đầu tiên', 500000.00, NULL, 10.00, 200000.00, 100, '2025-01-01', '2026-08-06T23:59:59', 1, '2026-06-10T23:44:29.303'),
(2, N'FREESHIP', N'Miễn phí vận chuyển', 300000.00, NULL, NULL, NULL, 200, '2025-01-01', '2026-08-06T23:59:59', 1, '2026-06-10T23:44:29.303'),
(3, N'SUMMER20', N'Giảm 20% hè rực rỡ', 1000000.00, NULL, 20.00, 500000.00, 50, '2025-01-01', '2026-08-06T23:59:59', 1, '2026-06-10T23:44:29.303');
SET IDENTITY_INSERT [dbo].[Giam_gia] OFF;
END
GO

-- ===== Vay =====
IF OBJECT_ID(N'dbo.san_pham','U') IS NULL
BEGIN
CREATE TABLE [dbo].[san_pham] (
  [id] int IDENTITY(1,1) NOT NULL,
  [id_nha_cung_cap] int NULL,
  [id_loai_san_pham] int NULL,
  [id_chat_lieu] int NULL,
  [id_tai_tro] int NULL,
  [ma_san_pham] nvarchar(50) NOT NULL,
  [ten_san_pham] nvarchar(200) NOT NULL,
  [link_youtube] nvarchar(500) NULL,
  [trang_thai] tinyint NULL,
  [mo_ta] nvarchar(max) NULL,
  [ngay_tao] datetime2(7) NULL,
  CONSTRAINT [PK_Vay] PRIMARY KEY ([id])
);
END
GO
IF NOT EXISTS (SELECT 1 FROM [dbo].[san_pham])
BEGIN
SET IDENTITY_INSERT [dbo].[san_pham] ON;
INSERT INTO [dbo].[san_pham] ([id], [id_nha_cung_cap], [id_loai_san_pham], [id_chat_lieu], [id_tai_tro], [ma_san_pham], [ten_san_pham], [link_youtube], [trang_thai], [mo_ta], [ngay_tao]) VALUES
(1, 1, 1, 1, 1, N'ASM001', N'Áo Sơ Mi Lụa Cổ Điển', NULL, 1, N'Áo sơ mi lụa tơ tằm mềm mại, phong cách cổ điển sang trọng thanh lịch', '2026-06-10T23:44:29.150'),
(2, 1, 2, 3, 1, N'QJN001', N'Quần Jeans Wide Leg Thời Trang', NULL, 1, N'Quần jeans ống rộng tôn dáng trẻ trung cá tính', '2026-06-10T23:44:29.150'),
(3, 2, 3, 2, 2, N'VDH001', N'Váy Dạ Hội Gấm Hoàng Gia', NULL, 1, N'Váy dạ hội chất liệu gấm cao cấp cho những dịp đặc biệt', '2026-06-10T23:44:29.150'),
(4, 1, 4, 4, 1, N'PKT001', N'Túi Xách Da Nữ Zestia Premium', NULL, 1, N'Túi xách da cao cấp phom dáng hiện đại sang trọng', '2026-06-10T23:44:29.150'),
(5, 2, 5, 5, 3, N'TCS001', N'Set Áo Blazer & Quần Tây Công Sở', NULL, 1, N'Set trang phục công sở đũi cao cấp thoáng mát hiện đại', '2026-06-10T23:44:29.150'),
(6, 1, 6, 1, 2, N'DTP001', N'Đầm Dự Tiệc Lụa Trắng Tinh Khôi', NULL, 1, N'Đầm lụa tơ tằm trắng tinh tế, phong cách dự tiệc thanh lịch', '2026-06-10T23:44:29.150'),
(9, 2, 1, 1, NULL, N'ASM002', N'Áo Kiểu Lụa Tơ Tằm Hoàng Gia', NULL, 1, N'Áo kiểu lụa tơ tằm cao cấp với họa tiết thêu thủ công tinh tế.', '2026-06-08T13:25:51.276'),
(10, 1, 1, 2, NULL, N'ASM003', N'Áo Kiểu Voan Cách Điệu Nữ Tính', NULL, 1, N'Áo kiểu chất liệu voan mềm mại, thiết kế xếp nếp duyên dáng.', '2026-04-14T13:25:51.366'),
(11, 1, 7, 3, NULL, N'AKH001', N'Áo Khoác Blazer Gấm Đỏ Nổi Bật', NULL, 1, N'Áo blazer gấm đỏ cao cấp với đường may tinh tế phong cách hiện đại.', '2026-06-03T13:25:51.406'),
(12, 2, 1, 4, NULL, N'ASM005', N'Áo Thun Cotton Form Rộng Premium', NULL, 1, N'Áo thun cotton cao cấp thoáng mát, họa tiết tối giản hiện đại.', '2026-06-09T13:25:51.452'),
(13, 1, 2, 1, NULL, N'QTY001', N'Quần Tây Ôm Dáng Công Sở', NULL, 1, N'Quần tây công sở chất liệu cao cấp, đường may sắc nét tôn dáng.', '2026-03-18T13:25:51.483'),
(14, 2, 2, 2, NULL, N'QTY002', N'Quần Culottes Lụa Xòe Nhẹ', NULL, 1, N'Quần culottes lụa mềm mại tạo cảm giác thoải mái và thanh thoát.', '2026-04-28T13:25:51.542'),
(15, 2, 2, 5, NULL, N'QTY003', N'Quần Short Đũi Mùa Hè', NULL, 1, N'Quần short đũi thoáng mát năng động phù hợp đi biển dạo phố.', '2026-03-22T13:25:51.575'),
(16, 1, 2, 6, NULL, N'QTY004', N'Quần Tây Dáng Tối Giản Minimalist', NULL, 1, N'Thiết kế tối giản sang trọng dành cho phụ nữ hiện đại.', '2026-04-13T13:25:51.595'),
(17, 2, 3, 3, NULL, N'VDH002', N'Đầm Dạ Hội Sequin Vàng', NULL, 1, N'Lấp lánh với sequin vàng cao cấp, nổi bật trong mọi bữa tiệc.', '2026-04-08T13:25:51.657'),
(18, 1, 3, 1, NULL, N'VDH003', N'Váy Dạ Hội Đen Huyền Bí', NULL, 1, N'Sự quyến rũ của sắc đen trên nền lụa satin, tạo nên vẻ đẹp bí ẩn.', '2026-03-28T13:25:51.703'),
(19, 2, 3, 2, NULL, N'VDH004', N'Váy Dạ Hội Xẻ Đùi Sang Trọng', NULL, 1, N'Thiết kế xẻ đùi gợi cảm nhưng vẫn giữ được sự thanh lịch.', '2026-05-03T13:25:51.734'),
(20, 2, 3, 4, NULL, N'VDH005', N'Váy Dạ Hội Ren Trắng Ngà', NULL, 1, N'Ren trắng ngà tinh khiết, lý tưởng cho các sự kiện trang trọng.', '2026-04-29T13:25:51.773'),
(21, 1, 5, 6, NULL, N'TCS002', N'Set Đầm Công Sở Thanh Lịch', NULL, 1, N'Thiết kế chuyên nghiệp, thoải mái suốt ngày làm việc.', '2026-05-19T13:25:51.834'),
(22, 2, 5, 5, NULL, N'TCS003', N'Set Áo Kiểu & Quần Tây Công Sở', NULL, 1, N'Tôn dáng thanh lịch nhẹ nhàng trong mọi cuộc họp.', '2026-03-21T13:25:51.899'),
(23, 2, 5, 1, NULL, N'TCS004', N'Set Trang Phục Kẻ Sọc Cao Cấp', NULL, 1, N'Họa tiết kẻ sọc cổ điển, phong cách Âu sang trọng.', '2026-05-07T13:25:51.940'),
(24, 2, 5, 2, NULL, N'TCS005', N'Set Trang Phục Dáng Chữ A', NULL, 1, N'Dáng chữ A thanh thoát, phù hợp cho nhiều vóc dáng.', '2026-04-14T13:25:51.972'),
(25, 1, 4, 1, NULL, N'PKT002', N'Túi Xách Lụa Thêu Hoa', NULL, 1, N'Túi xách lụa thêu thủ công cao cấp cho ngày trọng đại.', '2026-05-13T13:25:51.993'),
(26, 1, 4, 4, NULL, N'PKT003', N'Thắt Lưng Da Nữ Zestia Gold', NULL, 1, N'Thắt lưng da cao cấp khóa mạ vàng tinh tế phong cách hoàng gia.', '2026-06-04T13:25:52.053'),
(27, 1, 4, 2, NULL, N'PKT004', N'Khăn Lụa Vuông Zestia Signature', NULL, 1, N'Khăn lụa vuông họa tiết độc quyền tôn vóc dáng và thần thái.', '2026-04-26T13:25:52.095'),
(28, 2, 4, 3, NULL, N'PKT005', N'Mũ Vành Rộng Zestia Bohemian', NULL, 1, N'Phong cách bohemian lãng mạn cho các chuyến du lịch.', '2026-05-21T13:25:52.137'),
(29, 2, 6, 5, NULL, N'DTP002', N'Đầm Đi Tiệc Ngắn Trẻ Trung', NULL, 1, N'Thiết kế ngắn trẻ trung, hoàn hảo cho các buổi tiệc tối.', '2026-05-24T13:25:52.196'),
(30, 2, 6, 6, NULL, N'DTP003', N'Đầm Đi Tiệc Xòe Bồng', NULL, 1, N'Dáng xòe bồng bềnh thu hút như công chúa.', '2026-05-28T13:25:52.219'),
(31, 1, 6, 3, NULL, N'DTP004', N'Đầm Đi Tiệc Nhung Xanh Quý Phái', NULL, 1, N'Chất nhung xanh cổ vịt sang trọng, nổi bật trong đêm tiệc.', '2026-05-19T13:25:52.283'),
(32, 2, 6, 1, NULL, N'DTP005', N'Đầm Đi Tiệc Metallic Bạc', NULL, 1, N'Ánh metallic bạc hiện đại, thu hút mọi ánh nhìn.', '2026-05-25T13:25:52.335'),
(33, 1, 1, 1, NULL, N'ASM006', N'Áo Sơ Mi Lụa Hồng Pastel', NULL, 1, N'Lụa tơ tằm hồng nhạt, thêu hoa sen tinh tế.', '2026-06-11T20:19:36.620'),
(34, 2, 7, 2, NULL, N'AKH004', N'Áo Khoác Blazer Gấm Hoàng Gia', NULL, 1, N'Gấm vàng hoàng gia, hoa văn rồng phượng cổ điển.', '2026-06-11T20:19:36.620'),
(35, 1, 2, 5, NULL, N'QTY005', N'Quần Tây Đũi Tự Nhiên', NULL, 1, N'Chất đũi mềm mại, phong cách mộc mạc thanh lịch.', '2026-06-11T20:19:36.620'),
(36, 2, 1, 3, NULL, N'ASM007', N'Áo Kiểu Voan Bay Bổng', NULL, 1, N'Voan mỏng nhẹ bay bổng, họa tiết hoa cúc trắng.', '2026-06-11T20:19:36.620'),
(37, 1, 7, 6, NULL, N'AKH002', N'Áo Khoác Dạ Nhung Quý Phái', NULL, 1, N'Nhung tím quý phái, phù hợp dịp lễ hội thu đông.', '2026-06-11T20:19:36.620'),
(38, 1, 1, 1, NULL, N'ASM008', N'Áo Kiểu Lụa Loang Màu Ombre', NULL, 1, N'Lụa loang màu ombre từ trắng sang hồng.', '2026-06-11T20:19:36.636'),
(39, 2, 1, 4, NULL, N'ASM009', N'Áo Thun Cotton Phố Cổ', NULL, 1, N'Cotton thoáng mát, in họa tiết phố cổ Hà Nội.', '2026-06-11T20:19:36.636'),
(40, 1, 1, 3, NULL, N'ASM010', N'Áo Kiểu Voan Tay Phồng', NULL, 1, N'Voan trắng tay phồng, cổ áo kiểu hiện đại.', '2026-06-11T20:19:36.636'),
(41, 2, 7, 2, NULL, N'AKH003', N'Áo Khoác Blazer Gấm Đỏ', NULL, 1, N'Gấm đỏ rực rỡ, thiết kế dáng blazer ngắn năng động.', '2026-06-11T20:19:36.636'),
(42, 1, 2, 5, NULL, N'QTY006', N'Quần Tây Đũi Vintage', NULL, 1, N'Đũi nâu vintage, phối nếp gấp cổ điển.', '2026-06-11T20:19:36.636'),
(43, 1, 3, 1, NULL, N'VDH006', N'Đầm Dạ Hội Sequin Vàng', NULL, 1, N'Lụa phủ sequin vàng lấp lánh, dáng đuôi cá quyến rũ.', '2026-06-11T20:19:36.653'),
(44, 2, 3, 3, NULL, N'VDH007', N'Váy Dạ Hội Voan Nhiều Tầng', NULL, 1, N'Voan xếp tầng bay bổng, tông pastel nhẹ nhàng.', '2026-06-11T20:19:36.653'),
(45, 1, 3, 6, NULL, N'VDH008', N'Váy Dạ Hội Nhung Đen Huyền Bí', NULL, 1, N'Nhung đen sang trọng, xẻ tà cao thanh lịch.', '2026-06-11T20:19:36.653'),
(46, 2, 3, 1, NULL, N'VDH009', N'Váy Dạ Hội Lụa Champagne', NULL, 1, N'Lụa tơ tằm màu champagne, đính pha lê.', '2026-06-11T20:19:36.653'),
(47, 1, 3, 2, NULL, N'VDH010', N'Váy Dạ Hội Gấm Xanh Ngọc', NULL, 1, N'Gấm xanh ngọc lục bảo, hoa văn phượng hoàng.', '2026-06-11T20:19:36.653'),
(48, 1, 4, 3, NULL, N'PKT006', N'Ví Cầm Tay Voan Đính Đá', NULL, 1, N'Ví cầm tay lụa đính đá 3D lãng mạn lấp lánh.', '2026-06-11T20:19:36.673'),
(49, 2, 4, 1, NULL, N'PKT007', N'Túi Xách Lụa Thêu Chỉ Vàng', NULL, 1, N'Túi xách lụa ngà thêu chỉ vàng thủ công.', '2026-06-11T20:19:36.673'),
(50, 1, 4, 2, NULL, N'PKT008', N'Cài Áo Gấm Truyền Thống Zestia', NULL, 1, N'Cài áo gấm đỏ truyền thống hiện đại.', '2026-06-11T20:19:36.673'),
(51, 2, 4, 6, NULL, N'PKT009', N'Mũ Nồi Nhung Trắng Tối Giản', NULL, 1, N'Mũ nồi nhung trắng tối giản thanh lịch.', '2026-06-11T20:19:36.673'),
(52, 1, 4, 3, NULL, N'PKT010', N'Khăn Lụa Dài Thướt Tha', NULL, 1, N'Khăn lụa mỏng nhẹ, tà dài ấn tượng.', '2026-06-11T20:19:36.673'),
(53, 1, 1, 4, NULL, N'ASM011', N'Áo Kiểu Cotton Sọc', NULL, 1, N'Cotton mát mẻ, sọc xanh navy trẻ trung.', '2026-06-11T20:19:36.690'),
(54, 2, 3, 4, NULL, N'VDH011', N'Váy Xếp Li Cổ Điển', NULL, 1, N'Váy xếp li cổ điển thanh lịch sang trọng.', '2026-06-11T20:19:36.690'),
(55, 1, 1, 4, NULL, N'ASM012', N'Áo Sơ Mi Caro Preppy', NULL, 1, N'Cotton kẻ caro đỏ, phong cách preppy.', '2026-06-11T20:19:36.690'),
(56, 2, 1, 4, NULL, N'ASM013', N'Áo Polo Thể Thao Nữ', NULL, 1, N'Cotton co giãn, thiết kế áo polo thể thao khỏe khoắn.', '2026-06-11T20:19:36.690'),
(57, 1, 1, 3, NULL, N'ASM014', N'Áo Voan Nhẹ Mát Mùa Hè', NULL, 1, N'Voan nhẹ mát, phù hợp mùa hè.', '2026-06-11T20:19:36.690'),
(58, 1, 5, 1, NULL, N'TCS006', N'Set Áo Lụa & Váy Bút Chì', NULL, 1, N'Lụa xanh navy cổ tim, phom ôm công sở thanh lịch.', '2026-06-11T20:19:36.723'),
(59, 2, 5, 4, NULL, N'TCS007', N'Set Sơ Mi & Quần Tây Dáng A', NULL, 1, N'Set trang phục công sở dáng A, phù hợp mọi vóc dáng.', '2026-06-11T20:19:36.723'),
(60, 1, 5, 3, NULL, N'TCS008', N'Set Voan Xếp Nếp Công Sở', NULL, 1, N'Voan xếp nếp tôn dáng, tông nude nhã nhặn công sở.', '2026-06-11T20:19:36.723'),
(61, 2, 5, 5, NULL, N'TCS009', N'Set Đũi Cổ Tròn Công Sở', NULL, 1, N'Đũi nâu nhạt cổ tròn, đơn giản mà tinh tế.', '2026-06-11T20:19:36.723'),
(62, 1, 5, 1, NULL, N'TCS010', N'Set Lụa Đen Sang Trọng Công Sở', NULL, 1, N'Lụa đen bóng mượt sang trọng hiện đại.', '2026-06-11T20:19:36.723');
SET IDENTITY_INSERT [dbo].[san_pham] OFF;
END
GO

-- ===== san_pham_chi_tiet =====
IF OBJECT_ID(N'dbo.san_pham_chi_tiet','U') IS NULL
BEGIN
CREATE TABLE [dbo].[san_pham_chi_tiet] (
  [id] int IDENTITY(1,1) NOT NULL,
  [id_san_pham] int NOT NULL,
  [id_mau_sac] int NULL,
  [id_kich_thuoc] int NULL,
  [ma_san_pham_chi_tiet] nvarchar(80) NULL,
  [gia_ban_goc] decimal(15,2) NOT NULL,
  [gia_ban] decimal(15,2) NOT NULL,
  [phan_tram_giam] decimal(5,2) NULL,
  [so_luong] int NULL,
  [anh_url] nvarchar(500) NULL,
  [trang_thai] tinyint NULL,
  [ngay_tao] datetime2(7) NULL,
  [gia_nhap] decimal(18,2) NULL,
  CONSTRAINT [PK_san_pham_chi_tiet] PRIMARY KEY ([id])
);
END
GO
IF NOT EXISTS (SELECT 1 FROM [dbo].[san_pham_chi_tiet])
BEGIN
SET IDENTITY_INSERT [dbo].[san_pham_chi_tiet] ON;
INSERT INTO [dbo].[san_pham_chi_tiet] ([id], [id_san_pham], [id_mau_sac], [id_kich_thuoc], [ma_san_pham_chi_tiet], [gia_ban_goc], [gia_ban], [phan_tram_giam], [so_luong], [anh_url], [trang_thai], [ngay_tao], [gia_nhap]) VALUES
(1, 1, 4, 1, N'VAY001-TRANG-S', 2890000.00, 2890000.00, 0.00, 15, NULL, 1, '2026-06-10T23:44:29.170', 1878500.00),
(2, 1, 4, 2, N'VAY001-TRANG-M', 2890000.00, 2890000.00, 0.00, 20, NULL, 1, '2026-06-10T23:44:29.170', 1878500.00),
(3, 1, 4, 3, N'VAY001-TRANG-L', 2890000.00, 2890000.00, 0.00, 10, NULL, 1, '2026-06-10T23:44:29.170', 1878500.00),
(4, 1, 5, 1, N'VAY001-DEN-S', 2890000.00, 2890000.00, 0.00, 12, NULL, 1, '2026-06-10T23:44:29.170', 1878500.00),
(5, 1, 5, 2, N'VAY001-DEN-M', 2890000.00, 2890000.00, 0.00, 18, NULL, 1, '2026-06-10T23:44:29.170', 1878500.00),
(6, 2, 6, 2, N'VAY002-HONG-M', 1990000.00, 1590000.00, 20.00, 25, NULL, 1, '2026-06-10T23:44:29.170', 1033500.00),
(7, 2, 6, 3, N'VAY002-HONG-L', 1990000.00, 1590000.00, 20.00, 20, NULL, 1, '2026-06-10T23:44:29.170', 1033500.00),
(8, 2, 7, 2, N'VAY002-TIM-M', 1990000.00, 1790000.00, 20.00, 15, NULL, 1, '2026-06-10T23:44:29.170', 1163500.00),
(9, 3, 1, 2, N'VAY003-DO-M', 5490000.00, 4290000.00, 15.00, 8, NULL, 1, '2026-06-10T23:44:29.170', 2788500.00),
(10, 3, 1, 3, N'VAY003-DO-L', 5490000.00, 4290000.00, 15.00, 5, NULL, 1, '2026-06-10T23:44:29.170', 2788500.00),
(11, 3, 3, 2, N'VAY003-VANG-M', 5490000.00, 4890000.00, 15.00, 6, NULL, 1, '2026-06-10T23:44:29.170', 3178500.00),
(12, 4, 4, 1, N'VAY004-TRANG-S', 1290000.00, 1290000.00, 0.00, 30, NULL, 1, '2026-06-10T23:44:29.170', 838500.00),
(13, 4, 4, 2, N'VAY004-TRANG-M', 1290000.00, 1290000.00, 0.00, 35, NULL, 1, '2026-06-10T23:44:29.170', 838500.00),
(14, 4, 2, 2, N'VAY004-NAVY-M', 1290000.00, 1290000.00, 0.00, 25, NULL, 1, '2026-06-10T23:44:29.170', 838500.00),
(15, 5, 8, 2, N'VAY005-XANHLA-M', 1690000.00, 1390000.00, 0.00, 20, NULL, 1, '2026-06-10T23:44:29.170', 903500.00),
(16, 5, 8, 3, N'VAY005-XANHLA-L', 1690000.00, 1390000.00, 0.00, 15, NULL, 1, '2026-06-10T23:44:29.170', 903500.00),
(17, 6, 4, 2, N'VAY006-TRANG-M', 8990000.00, 8990000.00, 0.00, 3, NULL, 1, '2026-06-10T23:44:29.170', 5843500.00),
(18, 6, 4, 3, N'VAY006-TRANG-L', 8990000.00, 8990000.00, 0.00, 2, NULL, 1, '2026-06-10T23:44:29.170', 5843500.00),
(20, 9, 1, 1, N'VTT001-001', 3890000.00, 3890000.00, NULL, 21, NULL, 1, '2026-06-11T13:25:51.307', 2528500.00),
(21, 9, 1, 4, N'VTT001-002', 3890000.00, 3890000.00, NULL, 15, NULL, 1, '2026-06-11T13:25:51.322', 2528500.00),
(22, 9, 1, 6, N'VTT001-003', 3890000.00, 3890000.00, NULL, 8, NULL, 1, '2026-06-11T13:25:51.328', 2528500.00),
(23, 9, 1, 5, N'VTT001-004', 3890000.00, 3890000.00, NULL, 14, NULL, 1, '2026-06-11T13:25:51.328', 2528500.00),
(24, 9, 1, 2, N'VTT001-005', 3890000.00, 3890000.00, NULL, 5, NULL, 1, '2026-06-11T13:25:51.339', 2528500.00),
(25, 9, 2, 1, N'VTT001-006', 3890000.00, 3890000.00, NULL, 8, NULL, 1, '2026-06-11T13:25:51.339', 2528500.00),
(26, 9, 2, 4, N'VTT001-007', 3890000.00, 3890000.00, NULL, 11, NULL, 1, '2026-06-11T13:25:51.349', 2528500.00),
(27, 9, 2, 6, N'VTT001-008', 3890000.00, 3890000.00, NULL, 18, NULL, 1, '2026-06-11T13:25:51.355', 2528500.00),
(28, 9, 2, 5, N'VTT001-009', 3890000.00, 3890000.00, NULL, 8, NULL, 1, '2026-06-11T13:25:51.360', 2528500.00),
(29, 9, 2, 2, N'VTT001-010', 3890000.00, 3890000.00, NULL, 6, NULL, 1, '2026-06-11T13:25:51.360', 2528500.00),
(30, 10, 2, 1, N'VTT002-001', 2690000.00, 2690000.00, NULL, 8, NULL, 1, '2026-06-11T13:25:51.368', 1748500.00),
(31, 10, 2, 5, N'VTT002-002', 2690000.00, 2690000.00, NULL, 22, NULL, 1, '2026-06-11T13:25:51.371', 1748500.00),
(32, 10, 2, 3, N'VTT002-003', 2690000.00, 2690000.00, NULL, 20, NULL, 1, '2026-06-11T13:25:51.371', 1748500.00),
(33, 10, 8, 1, N'VTT002-004', 2690000.00, 2690000.00, NULL, 19, NULL, 1, '2026-06-11T13:25:51.381', 1748500.00),
(34, 10, 8, 5, N'VTT002-005', 2690000.00, 2690000.00, NULL, 12, NULL, 1, '2026-06-11T13:25:51.387', 1748500.00),
(35, 10, 8, 3, N'VTT002-006', 2690000.00, 2690000.00, NULL, 20, NULL, 1, '2026-06-11T13:25:51.391', 1748500.00),
(36, 10, 6, 1, N'VTT002-007', 2690000.00, 2690000.00, NULL, 22, NULL, 1, '2026-06-11T13:25:51.394', 1748500.00),
(37, 10, 6, 5, N'VTT002-008', 2690000.00, 2690000.00, NULL, 8, NULL, 1, '2026-06-11T13:25:51.394', 1748500.00),
(38, 10, 6, 3, N'VTT002-009', 2690000.00, 2690000.00, NULL, 18, NULL, 1, '2026-06-11T13:25:51.402', 1748500.00),
(39, 11, 8, 1, N'VTT003-001', 4290000.00, 3590000.00, NULL, 24, NULL, 1, '2026-06-11T13:25:51.410', 2333500.00),
(40, 11, 8, 5, N'VTT003-002', 4290000.00, 3590000.00, NULL, 20, NULL, 1, '2026-06-11T13:25:51.412', 2333500.00),
(41, 11, 8, 6, N'VTT003-003', 4290000.00, 3590000.00, NULL, 24, NULL, 1, '2026-06-11T13:25:51.412', 2333500.00),
(42, 11, 8, 4, N'VTT003-004', 4290000.00, 3590000.00, NULL, 15, NULL, 1, '2026-06-11T13:25:51.412', 2333500.00),
(43, 11, 8, 2, N'VTT003-005', 4290000.00, 3590000.00, NULL, 6, NULL, 1, '2026-06-11T13:25:51.420', 2333500.00),
(44, 11, 4, 1, N'VTT003-006', 4290000.00, 3590000.00, NULL, 18, NULL, 1, '2026-06-11T13:25:51.422', 2333500.00),
(45, 11, 4, 5, N'VTT003-007', 4290000.00, 3590000.00, NULL, 14, NULL, 1, '2026-06-11T13:25:51.422', 2333500.00),
(46, 11, 4, 6, N'VTT003-008', 4290000.00, 3590000.00, NULL, 18, NULL, 1, '2026-06-11T13:25:51.422', 2333500.00),
(47, 11, 4, 4, N'VTT003-009', 4290000.00, 3590000.00, NULL, 18, NULL, 1, '2026-06-11T13:25:51.432', 2333500.00),
(48, 11, 4, 2, N'VTT003-010', 4290000.00, 3590000.00, NULL, 11, NULL, 1, '2026-06-11T13:25:51.434', 2333500.00),
(49, 11, 5, 1, N'VTT003-011', 4290000.00, 3590000.00, NULL, 5, NULL, 1, '2026-06-11T13:25:51.442', 2333500.00),
(50, 11, 5, 5, N'VTT003-012', 4290000.00, 3590000.00, NULL, 10, NULL, 1, '2026-06-11T13:25:51.442', 2333500.00),
(51, 11, 5, 6, N'VTT003-013', 4290000.00, 3590000.00, NULL, 18, NULL, 1, '2026-06-11T13:25:51.450', 2333500.00),
(52, 11, 5, 4, N'VTT003-014', 4290000.00, 3590000.00, NULL, 5, NULL, 1, '2026-06-11T13:25:51.452', 2333500.00),
(53, 11, 5, 2, N'VTT003-015', 4290000.00, 3590000.00, NULL, 19, NULL, 1, '2026-06-11T13:25:51.452', 2333500.00),
(54, 12, 4, 2, N'VTT004-001', 3190000.00, 3190000.00, NULL, 21, NULL, 1, '2026-06-11T13:25:51.460', 2073500.00),
(55, 12, 4, 3, N'VTT004-002', 3190000.00, 3190000.00, NULL, 13, NULL, 1, '2026-06-11T13:25:51.462', 2073500.00),
(56, 12, 4, 1, N'VTT004-003', 3190000.00, 3190000.00, NULL, 15, NULL, 1, '2026-06-11T13:25:51.466', 2073500.00),
(57, 12, 6, 2, N'VTT004-004', 3190000.00, 3190000.00, NULL, 5, NULL, 1, '2026-06-11T13:25:51.466', 2073500.00),
(58, 12, 6, 3, N'VTT004-005', 3190000.00, 3190000.00, NULL, 17, NULL, 1, '2026-06-11T13:25:51.470', 2073500.00),
(59, 12, 6, 1, N'VTT004-006', 3190000.00, 3190000.00, NULL, 14, NULL, 1, '2026-06-11T13:25:51.472', 2073500.00),
(60, 12, 1, 2, N'VTT004-007', 3190000.00, 3190000.00, NULL, 8, NULL, 1, '2026-06-11T13:25:51.472', 2073500.00),
(61, 12, 1, 3, N'VTT004-008', 3190000.00, 3190000.00, NULL, 16, NULL, 1, '2026-06-11T13:25:51.481', 2073500.00),
(62, 12, 1, 1, N'VTT004-009', 3190000.00, 3190000.00, NULL, 14, NULL, 1, '2026-06-11T13:25:51.483', 2073500.00),
(63, 13, 8, 4, N'VCT001-001', 2490000.00, 2490000.00, NULL, 8, NULL, 1, '2026-06-11T13:25:51.483', 1618500.00),
(64, 13, 8, 2, N'VCT001-002', 2490000.00, 2490000.00, NULL, 17, NULL, 1, '2026-06-11T13:25:51.493', 1618500.00),
(65, 13, 8, 1, N'VCT001-003', 2490000.00, 2490000.00, NULL, 17, NULL, 1, '2026-06-11T13:25:51.493', 1618500.00),
(66, 13, 8, 6, N'VCT001-004', 2490000.00, 2490000.00, NULL, 7, NULL, 1, '2026-06-11T13:25:51.497', 1618500.00),
(67, 13, 8, 5, N'VCT001-005', 2490000.00, 2490000.00, NULL, 8, NULL, 1, '2026-06-11T13:25:51.501', 1618500.00),
(68, 13, 1, 4, N'VCT001-006', 2490000.00, 2490000.00, NULL, 18, NULL, 1, '2026-06-11T13:25:51.503', 1618500.00),
(69, 13, 1, 2, N'VCT001-007', 2490000.00, 2490000.00, NULL, 7, NULL, 1, '2026-06-11T13:25:51.503', 1618500.00),
(70, 13, 1, 1, N'VCT001-008', 2490000.00, 2490000.00, NULL, 17, NULL, 1, '2026-06-11T13:25:51.503', 1618500.00),
(71, 13, 1, 6, N'VCT001-009', 2490000.00, 2490000.00, NULL, 17, NULL, 1, '2026-06-11T13:25:51.511', 1618500.00),
(72, 13, 1, 5, N'VCT001-010', 2490000.00, 2490000.00, NULL, 23, NULL, 1, '2026-06-11T13:25:51.511', 1618500.00),
(73, 13, 4, 4, N'VCT001-011', 2490000.00, 2490000.00, NULL, 17, NULL, 1, '2026-06-11T13:25:51.513', 1618500.00),
(74, 13, 4, 2, N'VCT001-012', 2490000.00, 2490000.00, NULL, 17, NULL, 1, '2026-06-11T13:25:51.513', 1618500.00),
(75, 13, 4, 1, N'VCT001-013', 2490000.00, 2490000.00, NULL, 13, NULL, 1, '2026-06-11T13:25:51.522', 1618500.00),
(76, 13, 4, 6, N'VCT001-014', 2490000.00, 2490000.00, NULL, 10, NULL, 1, '2026-06-11T13:25:51.524', 1618500.00),
(77, 13, 4, 5, N'VCT001-015', 2490000.00, 2490000.00, NULL, 7, NULL, 1, '2026-06-11T13:25:51.524', 1618500.00),
(78, 13, 5, 4, N'VCT001-016', 2490000.00, 2490000.00, NULL, 12, NULL, 1, '2026-06-11T13:25:51.529', 1618500.00),
(79, 13, 5, 2, N'VCT001-017', 2490000.00, 2490000.00, NULL, 18, NULL, 1, '2026-06-11T13:25:51.532', 1618500.00),
(80, 13, 5, 1, N'VCT001-018', 2490000.00, 2490000.00, NULL, 18, NULL, 1, '2026-06-11T13:25:51.534', 1618500.00),
(81, 13, 5, 6, N'VCT001-019', 2490000.00, 2490000.00, NULL, 12, NULL, 1, '2026-06-11T13:25:51.534', 1618500.00),
(82, 13, 5, 5, N'VCT001-020', 2490000.00, 2490000.00, NULL, 6, NULL, 1, '2026-06-11T13:25:51.534', 1618500.00),
(83, 14, 7, 1, N'VCT002-001', 2890000.00, 2290000.00, NULL, 6, NULL, 1, '2026-06-11T13:25:51.544', 1488500.00),
(84, 14, 7, 6, N'VCT002-002', 2890000.00, 2290000.00, NULL, 9, NULL, 1, '2026-06-11T13:25:51.545', 1488500.00),
(85, 14, 7, 4, N'VCT002-003', 2890000.00, 2290000.00, NULL, 21, NULL, 1, '2026-06-11T13:25:51.549', 1488500.00),
(86, 14, 5, 1, N'VCT002-004', 2890000.00, 2290000.00, NULL, 5, NULL, 1, '2026-06-11T13:25:51.552', 1488500.00),
(87, 14, 5, 6, N'VCT002-005', 2890000.00, 2290000.00, NULL, 15, NULL, 1, '2026-06-11T13:25:51.554', 1488500.00),
(88, 14, 5, 4, N'VCT002-006', 2890000.00, 2290000.00, NULL, 10, NULL, 1, '2026-06-11T13:25:51.554', 1488500.00),
(89, 14, 3, 1, N'VCT002-007', 2890000.00, 2290000.00, NULL, 15, NULL, 1, '2026-06-11T13:25:51.554', 1488500.00),
(90, 14, 3, 6, N'VCT002-008', 2890000.00, 2290000.00, NULL, 15, NULL, 1, '2026-06-11T13:25:51.561', 1488500.00),
(91, 14, 3, 4, N'VCT002-009', 2890000.00, 2290000.00, NULL, 14, NULL, 1, '2026-06-11T13:25:51.562', 1488500.00),
(92, 14, 2, 1, N'VCT002-010', 2890000.00, 2290000.00, NULL, 14, NULL, 1, '2026-06-11T13:25:51.564', 1488500.00),
(93, 14, 2, 6, N'VCT002-011', 2890000.00, 2290000.00, NULL, 16, NULL, 1, '2026-06-11T13:25:51.564', 1488500.00),
(94, 14, 2, 4, N'VCT002-012', 2890000.00, 2290000.00, NULL, 9, NULL, 1, '2026-06-11T13:25:51.569', 1488500.00),
(95, 15, 7, 4, N'VCT003-001', 1890000.00, 1890000.00, NULL, 10, NULL, 1, '2026-06-11T13:25:51.577', 1228500.00),
(96, 15, 7, 2, N'VCT003-002', 1890000.00, 1890000.00, NULL, 17, NULL, 1, '2026-06-11T13:25:51.580', 1228500.00),
(97, 15, 7, 6, N'VCT003-003', 1890000.00, 1890000.00, NULL, 16, NULL, 1, '2026-06-11T13:25:51.583', 1228500.00),
(98, 15, 7, 1, N'VCT003-004', 1890000.00, 1890000.00, NULL, 23, NULL, 1, '2026-06-11T13:25:51.585', 1228500.00),
(99, 15, 2, 4, N'VCT003-005', 1890000.00, 1890000.00, NULL, 18, NULL, 1, '2026-06-11T13:25:51.585', 1228500.00),
(100, 15, 2, 2, N'VCT003-006', 1890000.00, 1890000.00, NULL, 11, NULL, 1, '2026-06-11T13:25:51.585', 1228500.00),
(101, 15, 2, 6, N'VCT003-007', 1890000.00, 1890000.00, NULL, 23, NULL, 1, '2026-06-11T13:25:51.585', 1228500.00),
(102, 15, 2, 1, N'VCT003-008', 1890000.00, 1890000.00, NULL, 11, NULL, 1, '2026-06-11T13:25:51.593', 1228500.00),
(103, 16, 1, 4, N'VCT004-001', 2190000.00, 2190000.00, NULL, 18, NULL, 1, '2026-06-11T13:25:51.595', 1423500.00),
(104, 16, 1, 5, N'VCT004-002', 2190000.00, 2190000.00, NULL, 13, NULL, 1, '2026-06-11T13:25:51.604', 1423500.00),
(105, 16, 1, 3, N'VCT004-003', 2190000.00, 2190000.00, NULL, 13, NULL, 1, '2026-06-11T13:25:51.604', 1423500.00),
(106, 16, 1, 6, N'VCT004-004', 2190000.00, 2190000.00, NULL, 19, NULL, 1, '2026-06-11T13:25:51.606', 1423500.00),
(107, 16, 7, 4, N'VCT004-005', 2190000.00, 2190000.00, NULL, 10, NULL, 1, '2026-06-11T13:25:51.608', 1423500.00),
(108, 16, 7, 5, N'VCT004-006', 2190000.00, 2190000.00, NULL, 24, NULL, 1, '2026-06-11T13:25:51.614', 1423500.00),
(109, 16, 7, 3, N'VCT004-007', 2190000.00, 2190000.00, NULL, 16, NULL, 1, '2026-06-11T13:25:51.616', 1423500.00),
(110, 16, 7, 6, N'VCT004-008', 2190000.00, 2190000.00, NULL, 8, NULL, 1, '2026-06-11T13:25:51.616', 1423500.00),
(111, 16, 6, 4, N'VCT004-009', 2190000.00, 2190000.00, NULL, 8, NULL, 1, '2026-06-11T13:25:51.616', 1423500.00),
(112, 16, 6, 5, N'VCT004-010', 2190000.00, 2190000.00, NULL, 20, NULL, 1, '2026-06-11T13:25:51.624', 1423500.00),
(113, 16, 6, 3, N'VCT004-011', 2190000.00, 2190000.00, NULL, 6, NULL, 1, '2026-06-11T13:25:51.626', 1423500.00),
(114, 16, 6, 6, N'VCT004-012', 2190000.00, 2190000.00, NULL, 16, NULL, 1, '2026-06-11T13:25:51.634', 1423500.00),
(115, 16, 5, 4, N'VCT004-013', 2190000.00, 2190000.00, NULL, 8, NULL, 1, '2026-06-11T13:25:51.640', 1423500.00),
(116, 16, 5, 5, N'VCT004-014', 2190000.00, 2190000.00, NULL, 18, NULL, 1, '2026-06-11T13:25:51.645', 1423500.00),
(117, 16, 5, 3, N'VCT004-015', 2190000.00, 2190000.00, NULL, 18, NULL, 1, '2026-06-11T13:25:51.647', 1423500.00),
(118, 16, 5, 6, N'VCT004-016', 2190000.00, 2190000.00, NULL, 21, NULL, 1, '2026-06-11T13:25:51.647', 1423500.00),
(119, 17, 3, 3, N'VDH001-001', 6490000.00, 6490000.00, NULL, 15, NULL, 1, '2026-06-11T13:25:51.666', 4218500.00),
(120, 17, 3, 4, N'VDH001-002', 6490000.00, 6490000.00, NULL, 13, NULL, 1, '2026-06-11T13:25:51.668', 4218500.00),
(121, 17, 3, 5, N'VDH001-003', 6490000.00, 6490000.00, NULL, 6, NULL, 1, '2026-06-11T13:25:51.671', 4218500.00),
(122, 17, 7, 3, N'VDH001-004', 6490000.00, 6490000.00, NULL, 6, NULL, 1, '2026-06-11T13:25:51.677', 4218500.00),
(123, 17, 7, 4, N'VDH001-005', 6490000.00, 6490000.00, NULL, 16, NULL, 1, '2026-06-11T13:25:51.679', 4218500.00),
(124, 17, 7, 5, N'VDH001-006', 6490000.00, 6490000.00, NULL, 15, NULL, 1, '2026-06-11T13:25:51.679', 4218500.00),
(125, 17, 5, 3, N'VDH001-007', 6490000.00, 6490000.00, NULL, 20, NULL, 1, '2026-06-11T13:25:51.687', 4218500.00),
(126, 17, 5, 4, N'VDH001-008', 6490000.00, 6490000.00, NULL, 18, NULL, 1, '2026-06-11T13:25:51.693', 4218500.00),
(127, 17, 5, 5, N'VDH001-009', 6490000.00, 6490000.00, NULL, 10, NULL, 1, '2026-06-11T13:25:51.693', 4218500.00),
(128, 18, 7, 1, N'VDH002-001', 5890000.00, 4890000.00, NULL, 24, NULL, 1, '2026-06-11T13:25:51.709', 3178500.00),
(129, 18, 7, 2, N'VDH002-002', 5890000.00, 4890000.00, NULL, 11, NULL, 1, '2026-06-11T13:25:51.713', 3178500.00),
(130, 18, 7, 4, N'VDH002-003', 5890000.00, 4890000.00, NULL, 19, NULL, 1, '2026-06-11T13:25:51.714', 3178500.00),
(131, 18, 8, 1, N'VDH002-004', 5890000.00, 4890000.00, NULL, 5, NULL, 1, '2026-06-11T13:25:51.721', 3178500.00),
(132, 18, 8, 2, N'VDH002-005', 5890000.00, 4890000.00, NULL, 13, NULL, 1, '2026-06-11T13:25:51.721', 3178500.00),
(133, 18, 8, 4, N'VDH002-006', 5890000.00, 4890000.00, NULL, 16, NULL, 1, '2026-06-11T13:25:51.731', 3178500.00),
(134, 19, 3, 3, N'VDH003-001', 7290000.00, 7290000.00, NULL, 5, NULL, 1, '2026-06-11T13:25:51.741', 4738500.00),
(135, 19, 3, 4, N'VDH003-002', 7290000.00, 7290000.00, NULL, 23, NULL, 1, '2026-06-11T13:25:51.743', 4738500.00),
(136, 19, 3, 6, N'VDH003-003', 7290000.00, 7290000.00, NULL, 24, NULL, 1, '2026-06-11T13:25:51.743', 4738500.00),
(137, 19, 3, 2, N'VDH003-004', 7290000.00, 7290000.00, NULL, 7, NULL, 1, '2026-06-11T13:25:51.752', 4738500.00),
(138, 19, 6, 3, N'VDH003-005', 7290000.00, 7290000.00, NULL, 12, NULL, 1, '2026-06-11T13:25:51.753', 4738500.00),
(139, 19, 6, 4, N'VDH003-006', 7290000.00, 7290000.00, NULL, 22, NULL, 1, '2026-06-11T13:25:51.760', 4738500.00),
(140, 19, 6, 6, N'VDH003-007', 7290000.00, 7290000.00, NULL, 8, NULL, 1, '2026-06-11T13:25:51.763', 4738500.00),
(141, 19, 6, 2, N'VDH003-008', 7290000.00, 7290000.00, NULL, 20, NULL, 1, '2026-06-11T13:25:51.765', 4738500.00),
(142, 20, 8, 4, N'VDH004-001', 5490000.00, 4590000.00, NULL, 8, NULL, 1, '2026-06-11T13:25:51.773', 2983500.00),
(143, 20, 8, 5, N'VDH004-002', 5490000.00, 4590000.00, NULL, 19, NULL, 1, '2026-06-11T13:25:51.781', 2983500.00),
(144, 20, 8, 2, N'VDH004-003', 5490000.00, 4590000.00, NULL, 20, NULL, 1, '2026-06-11T13:25:51.784', 2983500.00),
(145, 20, 8, 6, N'VDH004-004', 5490000.00, 4590000.00, NULL, 18, NULL, 1, '2026-06-11T13:25:51.784', 2983500.00),
(146, 20, 6, 4, N'VDH004-005', 5490000.00, 4590000.00, NULL, 10, NULL, 1, '2026-06-11T13:25:51.794', 2983500.00),
(147, 20, 6, 5, N'VDH004-006', 5490000.00, 4590000.00, NULL, 5, NULL, 1, '2026-06-11T13:25:51.797', 2983500.00),
(148, 20, 6, 2, N'VDH004-007', 5490000.00, 4590000.00, NULL, 12, NULL, 1, '2026-06-11T13:25:51.802', 2983500.00),
(149, 20, 6, 6, N'VDH004-008', 5490000.00, 4590000.00, NULL, 10, NULL, 1, '2026-06-11T13:25:51.804', 2983500.00),
(150, 20, 7, 4, N'VDH004-009', 5490000.00, 4590000.00, NULL, 19, NULL, 1, '2026-06-11T13:25:51.804', 2983500.00),
(151, 20, 7, 5, N'VDH004-010', 5490000.00, 4590000.00, NULL, 14, NULL, 1, '2026-06-11T13:25:51.804', 2983500.00),
(152, 20, 7, 2, N'VDH004-011', 5490000.00, 4590000.00, NULL, 13, NULL, 1, '2026-06-11T13:25:51.813', 2983500.00),
(153, 20, 7, 6, N'VDH004-012', 5490000.00, 4590000.00, NULL, 23, NULL, 1, '2026-06-11T13:25:51.816', 2983500.00),
(154, 20, 2, 4, N'VDH004-013', 5490000.00, 4590000.00, NULL, 14, NULL, 1, '2026-06-11T13:25:51.816', 2983500.00),
(155, 20, 2, 5, N'VDH004-014', 5490000.00, 4590000.00, NULL, 6, NULL, 1, '2026-06-11T13:25:51.821', 2983500.00),
(156, 20, 2, 2, N'VDH004-015', 5490000.00, 4590000.00, NULL, 11, NULL, 1, '2026-06-11T13:25:51.825', 2983500.00),
(157, 20, 2, 6, N'VDH004-016', 5490000.00, 4590000.00, NULL, 7, NULL, 1, '2026-06-11T13:25:51.826', 2983500.00),
(158, 21, 7, 5, N'VCS001-001', 1690000.00, 1690000.00, NULL, 7, NULL, 1, '2026-06-11T13:25:51.836', 1098500.00),
(159, 21, 7, 6, N'VCS001-002', 1690000.00, 1690000.00, NULL, 18, NULL, 1, '2026-06-11T13:25:51.845', 1098500.00),
(160, 21, 7, 4, N'VCS001-003', 1690000.00, 1690000.00, NULL, 24, NULL, 1, '2026-06-11T13:25:51.846', 1098500.00),
(161, 21, 7, 2, N'VCS001-004', 1690000.00, 1690000.00, NULL, 23, NULL, 1, '2026-06-11T13:25:51.854', 1098500.00),
(162, 21, 6, 5, N'VCS001-005', 1690000.00, 1690000.00, NULL, 9, NULL, 1, '2026-06-11T13:25:51.857', 1098500.00),
(163, 21, 6, 6, N'VCS001-006', 1690000.00, 1690000.00, NULL, 22, NULL, 1, '2026-06-11T13:25:51.867', 1098500.00),
(164, 21, 6, 4, N'VCS001-007', 1690000.00, 1690000.00, NULL, 6, NULL, 1, '2026-06-11T13:25:51.869', 1098500.00),
(165, 21, 6, 2, N'VCS001-008', 1690000.00, 1690000.00, NULL, 21, NULL, 1, '2026-06-11T13:25:51.869', 1098500.00),
(166, 21, 8, 5, N'VCS001-009', 1690000.00, 1690000.00, NULL, 5, NULL, 1, '2026-06-11T13:25:51.877', 1098500.00),
(167, 21, 8, 6, N'VCS001-010', 1690000.00, 1690000.00, NULL, 14, NULL, 1, '2026-06-11T13:25:51.880', 1098500.00),
(168, 21, 8, 4, N'VCS001-011', 1690000.00, 1690000.00, NULL, 11, NULL, 1, '2026-06-11T13:25:51.880', 1098500.00),
(169, 21, 8, 2, N'VCS001-012', 1690000.00, 1690000.00, NULL, 17, NULL, 1, '2026-06-11T13:25:51.886', 1098500.00),
(170, 21, 3, 5, N'VCS001-013', 1690000.00, 1690000.00, NULL, 12, NULL, 1, '2026-06-11T13:25:51.888', 1098500.00),
(171, 21, 3, 6, N'VCS001-014', 1690000.00, 1690000.00, NULL, 17, NULL, 1, '2026-06-11T13:25:51.888', 1098500.00),
(172, 21, 3, 4, N'VCS001-015', 1690000.00, 1690000.00, NULL, 8, NULL, 1, '2026-06-11T13:25:51.893', 1098500.00),
(173, 21, 3, 2, N'VCS001-016', 1690000.00, 1690000.00, NULL, 22, NULL, 1, '2026-06-11T13:25:51.897', 1098500.00),
(174, 22, 8, 1, N'VCS002-001', 1890000.00, 1490000.00, NULL, 10, NULL, 1, '2026-06-11T13:25:51.907', 968500.00),
(175, 22, 8, 5, N'VCS002-002', 1890000.00, 1490000.00, NULL, 13, NULL, 1, '2026-06-11T13:25:51.909', 968500.00),
(176, 22, 8, 2, N'VCS002-003', 1890000.00, 1490000.00, NULL, 7, NULL, 1, '2026-06-11T13:25:51.909', 968500.00),
(177, 22, 2, 1, N'VCS002-004', 1890000.00, 1490000.00, NULL, 17, NULL, 1, '2026-06-11T13:25:51.918', 968500.00),
(178, 22, 2, 5, N'VCS002-005', 1890000.00, 1490000.00, NULL, 17, NULL, 1, '2026-06-11T13:25:51.920', 968500.00),
(179, 22, 2, 2, N'VCS002-006', 1890000.00, 1490000.00, NULL, 10, NULL, 1, '2026-06-11T13:25:51.925', 968500.00),
(180, 22, 5, 1, N'VCS002-007', 1890000.00, 1490000.00, NULL, 7, NULL, 1, '2026-06-11T13:25:51.930', 968500.00),
(181, 22, 5, 5, N'VCS002-008', 1890000.00, 1490000.00, NULL, 12, NULL, 1, '2026-06-11T13:25:51.930', 968500.00),
(182, 22, 5, 2, N'VCS002-009', 1890000.00, 1490000.00, NULL, 17, NULL, 1, '2026-06-11T13:25:51.930', 968500.00),
(183, 23, 7, 3, N'VCS003-001', 1790000.00, 1790000.00, NULL, 8, NULL, 1, '2026-06-11T13:25:51.940', 1163500.00),
(184, 23, 7, 5, N'VCS003-002', 1790000.00, 1790000.00, NULL, 23, NULL, 1, '2026-06-11T13:25:51.951', 1163500.00),
(185, 23, 7, 4, N'VCS003-003', 1790000.00, 1790000.00, NULL, 18, NULL, 1, '2026-06-11T13:25:51.951', 1163500.00),
(186, 23, 7, 2, N'VCS003-004', 1790000.00, 1790000.00, NULL, 13, NULL, 1, '2026-06-11T13:25:51.956', 1163500.00),
(187, 23, 7, 6, N'VCS003-005', 1790000.00, 1790000.00, NULL, 6, NULL, 1, '2026-06-11T13:25:51.959', 1163500.00),
(188, 23, 4, 3, N'VCS003-006', 1790000.00, 1790000.00, NULL, 15, NULL, 1, '2026-06-11T13:25:51.961', 1163500.00),
(189, 23, 4, 5, N'VCS003-007', 1790000.00, 1790000.00, NULL, 7, NULL, 1, '2026-06-11T13:25:51.964', 1163500.00),
(190, 23, 4, 4, N'VCS003-008', 1790000.00, 1790000.00, NULL, 5, NULL, 1, '2026-06-11T13:25:51.966', 1163500.00),
(191, 23, 4, 2, N'VCS003-009', 1790000.00, 1790000.00, NULL, 16, NULL, 1, '2026-06-11T13:25:51.969', 1163500.00),
(192, 23, 4, 6, N'VCS003-010', 1790000.00, 1790000.00, NULL, 22, NULL, 1, '2026-06-11T13:25:51.972', 1163500.00),
(193, 24, 1, 4, N'VCS004-001', 1990000.00, 1990000.00, NULL, 18, NULL, 1, '2026-06-11T13:25:51.980', 1293500.00),
(194, 24, 1, 6, N'VCS004-002', 1990000.00, 1990000.00, NULL, 19, NULL, 1, '2026-06-11T13:25:51.983', 1293500.00),
(195, 24, 1, 5, N'VCS004-003', 1990000.00, 1990000.00, NULL, 14, NULL, 1, '2026-06-11T13:25:51.984', 1293500.00),
(196, 24, 4, 4, N'VCS004-004', 1990000.00, 1990000.00, NULL, 16, NULL, 1, '2026-06-11T13:25:51.987', 1293500.00),
(197, 24, 4, 6, N'VCS004-005', 1990000.00, 1990000.00, NULL, 13, NULL, 1, '2026-06-11T13:25:51.991', 1293500.00),
(198, 24, 4, 5, N'VCS004-006', 1990000.00, 1990000.00, NULL, 11, NULL, 1, '2026-06-11T13:25:51.993', 1293500.00),
(199, 25, 4, 5, N'VCU001-001', 8990000.00, 8990000.00, NULL, 23, NULL, 1, '2026-06-11T13:25:52.001', 5843500.00),
(200, 25, 4, 4, N'VCU001-002', 8990000.00, 8990000.00, NULL, 5, NULL, 1, '2026-06-11T13:25:52.003', 5843500.00),
(201, 25, 4, 1, N'VCU001-003', 8990000.00, 8990000.00, NULL, 23, NULL, 1, '2026-06-11T13:25:52.003', 5843500.00);
INSERT INTO [dbo].[san_pham_chi_tiet] ([id], [id_san_pham], [id_mau_sac], [id_kich_thuoc], [ma_san_pham_chi_tiet], [gia_ban_goc], [gia_ban], [phan_tram_giam], [so_luong], [anh_url], [trang_thai], [ngay_tao], [gia_nhap]) VALUES
(202, 25, 4, 6, N'VCU001-004', 8990000.00, 8990000.00, NULL, 22, NULL, 1, '2026-06-11T13:25:52.003', 5843500.00),
(203, 25, 4, 2, N'VCU001-005', 8990000.00, 8990000.00, NULL, 12, NULL, 1, '2026-06-11T13:25:52.011', 5843500.00),
(204, 25, 5, 5, N'VCU001-006', 8990000.00, 8990000.00, NULL, 9, NULL, 1, '2026-06-11T13:25:52.013', 5843500.00),
(205, 25, 5, 4, N'VCU001-007', 8990000.00, 8990000.00, NULL, 6, NULL, 1, '2026-06-11T13:25:52.013', 5843500.00),
(206, 25, 5, 1, N'VCU001-008', 8990000.00, 8990000.00, NULL, 23, NULL, 1, '2026-06-11T13:25:52.013', 5843500.00),
(207, 25, 5, 6, N'VCU001-009', 8990000.00, 8990000.00, NULL, 10, NULL, 1, '2026-06-11T13:25:52.018', 5843500.00),
(208, 25, 5, 2, N'VCU001-010', 8990000.00, 8990000.00, NULL, 6, NULL, 1, '2026-06-11T13:25:52.021', 5843500.00),
(209, 25, 7, 5, N'VCU001-011', 8990000.00, 8990000.00, NULL, 12, NULL, 1, '2026-06-11T13:25:52.023', 5843500.00),
(210, 25, 7, 4, N'VCU001-012', 8990000.00, 8990000.00, NULL, 24, NULL, 1, '2026-06-11T13:25:52.023', 5843500.00),
(211, 25, 7, 1, N'VCU001-013', 8990000.00, 8990000.00, NULL, 24, NULL, 1, '2026-06-11T13:25:52.023', 5843500.00),
(212, 25, 7, 6, N'VCU001-014', 8990000.00, 8990000.00, NULL, 13, NULL, 1, '2026-06-11T13:25:52.023', 5843500.00),
(213, 25, 7, 2, N'VCU001-015', 8990000.00, 8990000.00, NULL, 22, NULL, 1, '2026-06-11T13:25:52.034', 5843500.00),
(214, 25, 6, 5, N'VCU001-016', 8990000.00, 8990000.00, NULL, 8, NULL, 1, '2026-06-11T13:25:52.034', 5843500.00),
(215, 25, 6, 4, N'VCU001-017', 8990000.00, 8990000.00, NULL, 19, NULL, 1, '2026-06-11T13:25:52.034', 5843500.00),
(216, 25, 6, 1, N'VCU001-018', 8990000.00, 8990000.00, NULL, 24, NULL, 1, '2026-06-11T13:25:52.041', 5843500.00),
(217, 25, 6, 6, N'VCU001-019', 8990000.00, 8990000.00, NULL, 11, NULL, 1, '2026-06-11T13:25:52.043', 5843500.00),
(218, 25, 6, 2, N'VCU001-020', 8990000.00, 8990000.00, NULL, 9, NULL, 1, '2026-06-11T13:25:52.043', 5843500.00),
(219, 26, 8, 3, N'VCU002-001', 12990000.00, 9990000.00, NULL, 19, NULL, 1, '2026-06-11T13:25:52.053', 6493500.00),
(220, 26, 8, 5, N'VCU002-002', 12990000.00, 9990000.00, NULL, 21, NULL, 1, '2026-06-11T13:25:52.053', 6493500.00),
(221, 26, 8, 1, N'VCU002-003', 12990000.00, 9990000.00, NULL, 6, NULL, 1, '2026-06-11T13:25:52.063', 6493500.00),
(222, 26, 8, 2, N'VCU002-004', 12990000.00, 9990000.00, NULL, 15, NULL, 1, '2026-06-11T13:25:52.066', 6493500.00),
(223, 26, 8, 6, N'VCU002-005', 12990000.00, 9990000.00, NULL, 12, NULL, 1, '2026-06-11T13:25:52.066', 6493500.00),
(224, 26, 2, 3, N'VCU002-006', 12990000.00, 9990000.00, NULL, 19, NULL, 1, '2026-06-11T13:25:52.074', 6493500.00),
(225, 26, 2, 5, N'VCU002-007', 12990000.00, 9990000.00, NULL, 9, NULL, 1, '2026-06-11T13:25:52.077', 6493500.00),
(226, 26, 2, 1, N'VCU002-008', 12990000.00, 9990000.00, NULL, 18, NULL, 1, '2026-06-11T13:25:52.084', 6493500.00),
(227, 26, 2, 2, N'VCU002-009', 12990000.00, 9990000.00, NULL, 6, NULL, 1, '2026-06-11T13:25:52.085', 6493500.00),
(228, 26, 2, 6, N'VCU002-010', 12990000.00, 9990000.00, NULL, 19, NULL, 1, '2026-06-11T13:25:52.090', 6493500.00),
(229, 27, 2, 6, N'VCU003-001', 7990000.00, 7990000.00, NULL, 19, NULL, 1, '2026-06-11T13:25:52.098', 5193500.00),
(230, 27, 2, 1, N'VCU003-002', 7990000.00, 7990000.00, NULL, 6, NULL, 1, '2026-06-11T13:25:52.104', 5193500.00),
(231, 27, 2, 4, N'VCU003-003', 7990000.00, 7990000.00, NULL, 22, NULL, 1, '2026-06-11T13:25:52.106', 5193500.00),
(232, 27, 2, 2, N'VCU003-004', 7990000.00, 7990000.00, NULL, 10, NULL, 1, '2026-06-11T13:25:52.106', 5193500.00),
(233, 27, 2, 5, N'VCU003-005', 7990000.00, 7990000.00, NULL, 16, NULL, 1, '2026-06-11T13:25:52.106', 5193500.00),
(234, 27, 4, 6, N'VCU003-006', 7990000.00, 7990000.00, NULL, 13, NULL, 1, '2026-06-11T13:25:52.116', 5193500.00),
(235, 27, 4, 1, N'VCU003-007', 7990000.00, 7990000.00, NULL, 20, NULL, 1, '2026-06-11T13:25:52.116', 5193500.00),
(236, 27, 4, 4, N'VCU003-008', 7990000.00, 7990000.00, NULL, 13, NULL, 1, '2026-06-11T13:25:52.126', 5193500.00),
(237, 27, 4, 2, N'VCU003-009', 7990000.00, 7990000.00, NULL, 19, NULL, 1, '2026-06-11T13:25:52.130', 5193500.00),
(238, 27, 4, 5, N'VCU003-010', 7990000.00, 7990000.00, NULL, 10, NULL, 1, '2026-06-11T13:25:52.135', 5193500.00),
(239, 28, 3, 2, N'VCU004-001', 6490000.00, 5490000.00, NULL, 16, NULL, 1, '2026-06-11T13:25:52.137', 3568500.00),
(240, 28, 3, 3, N'VCU004-002', 6490000.00, 5490000.00, NULL, 5, NULL, 1, '2026-06-11T13:25:52.145', 3568500.00),
(241, 28, 3, 6, N'VCU004-003', 6490000.00, 5490000.00, NULL, 6, NULL, 1, '2026-06-11T13:25:52.145', 3568500.00),
(242, 28, 3, 4, N'VCU004-004', 6490000.00, 5490000.00, NULL, 14, NULL, 1, '2026-06-11T13:25:52.147', 3568500.00),
(243, 28, 3, 1, N'VCU004-005', 6490000.00, 5490000.00, NULL, 19, NULL, 1, '2026-06-11T13:25:52.147', 3568500.00),
(244, 28, 6, 2, N'VCU004-006', 6490000.00, 5490000.00, NULL, 15, NULL, 1, '2026-06-11T13:25:52.147', 3568500.00),
(245, 28, 6, 3, N'VCU004-007', 6490000.00, 5490000.00, NULL, 11, NULL, 1, '2026-06-11T13:25:52.155', 3568500.00),
(246, 28, 6, 6, N'VCU004-008', 6490000.00, 5490000.00, NULL, 19, NULL, 1, '2026-06-11T13:25:52.157', 3568500.00),
(247, 28, 6, 4, N'VCU004-009', 6490000.00, 5490000.00, NULL, 12, NULL, 1, '2026-06-11T13:25:52.161', 3568500.00),
(248, 28, 6, 1, N'VCU004-010', 6490000.00, 5490000.00, NULL, 13, NULL, 1, '2026-06-11T13:25:52.161', 3568500.00),
(249, 28, 4, 2, N'VCU004-011', 6490000.00, 5490000.00, NULL, 10, NULL, 1, '2026-06-11T13:25:52.161', 3568500.00),
(250, 28, 4, 3, N'VCU004-012', 6490000.00, 5490000.00, NULL, 13, NULL, 1, '2026-06-11T13:25:52.168', 3568500.00),
(251, 28, 4, 6, N'VCU004-013', 6490000.00, 5490000.00, NULL, 5, NULL, 1, '2026-06-11T13:25:52.168', 3568500.00),
(252, 28, 4, 4, N'VCU004-014', 6490000.00, 5490000.00, NULL, 18, NULL, 1, '2026-06-11T13:25:52.176', 3568500.00),
(253, 28, 4, 1, N'VCU004-015', 6490000.00, 5490000.00, NULL, 21, NULL, 1, '2026-06-11T13:25:52.178', 3568500.00),
(254, 28, 7, 2, N'VCU004-016', 6490000.00, 5490000.00, NULL, 23, NULL, 1, '2026-06-11T13:25:52.178', 3568500.00),
(255, 28, 7, 3, N'VCU004-017', 6490000.00, 5490000.00, NULL, 17, NULL, 1, '2026-06-11T13:25:52.186', 3568500.00),
(256, 28, 7, 6, N'VCU004-018', 6490000.00, 5490000.00, NULL, 19, NULL, 1, '2026-06-11T13:25:52.188', 3568500.00),
(257, 28, 7, 4, N'VCU004-019', 6490000.00, 5490000.00, NULL, 12, NULL, 1, '2026-06-11T13:25:52.188', 3568500.00),
(258, 28, 7, 1, N'VCU004-020', 6490000.00, 5490000.00, NULL, 16, NULL, 1, '2026-06-11T13:25:52.193', 3568500.00),
(259, 29, 7, 2, N'VDT001-001', 2290000.00, 2290000.00, NULL, 14, NULL, 1, '2026-06-11T13:25:52.198', 1488500.00),
(260, 29, 7, 6, N'VDT001-002', 2290000.00, 2290000.00, NULL, 19, NULL, 1, '2026-06-11T13:25:52.198', 1488500.00),
(261, 29, 7, 4, N'VDT001-003', 2290000.00, 2290000.00, NULL, 7, NULL, 1, '2026-06-11T13:25:52.206', 1488500.00),
(262, 29, 7, 5, N'VDT001-004', 2290000.00, 2290000.00, NULL, 8, NULL, 1, '2026-06-11T13:25:52.209', 1488500.00),
(263, 29, 3, 2, N'VDT001-005', 2290000.00, 2290000.00, NULL, 19, NULL, 1, '2026-06-11T13:25:52.209', 1488500.00),
(264, 29, 3, 6, N'VDT001-006', 2290000.00, 2290000.00, NULL, 21, NULL, 1, '2026-06-11T13:25:52.209', 1488500.00),
(265, 29, 3, 4, N'VDT001-007', 2290000.00, 2290000.00, NULL, 15, NULL, 1, '2026-06-11T13:25:52.209', 1488500.00),
(266, 29, 3, 5, N'VDT001-008', 2290000.00, 2290000.00, NULL, 15, NULL, 1, '2026-06-11T13:25:52.217', 1488500.00),
(267, 30, 6, 3, N'VDT002-001', 3290000.00, 2690000.00, NULL, 14, NULL, 1, '2026-06-11T13:25:52.224', 1748500.00),
(268, 30, 6, 2, N'VDT002-002', 3290000.00, 2690000.00, NULL, 11, NULL, 1, '2026-06-11T13:25:52.229', 1748500.00),
(269, 30, 6, 4, N'VDT002-003', 3290000.00, 2690000.00, NULL, 24, NULL, 1, '2026-06-11T13:25:52.246', 1748500.00),
(270, 30, 1, 3, N'VDT002-004', 3290000.00, 2690000.00, NULL, 8, NULL, 1, '2026-06-11T13:25:52.249', 1748500.00),
(271, 30, 1, 2, N'VDT002-005', 3290000.00, 2690000.00, NULL, 18, NULL, 1, '2026-06-11T13:25:52.251', 1748500.00),
(272, 30, 1, 4, N'VDT002-006', 3290000.00, 2690000.00, NULL, 10, NULL, 1, '2026-06-11T13:25:52.256', 1748500.00),
(273, 30, 4, 3, N'VDT002-007', 3290000.00, 2690000.00, NULL, 20, NULL, 1, '2026-06-11T13:25:52.262', 1748500.00),
(274, 30, 4, 2, N'VDT002-008', 3290000.00, 2690000.00, NULL, 20, NULL, 1, '2026-06-11T13:25:52.262', 1748500.00),
(275, 30, 4, 4, N'VDT002-009', 3290000.00, 2690000.00, NULL, 23, NULL, 1, '2026-06-11T13:25:52.270', 1748500.00),
(276, 30, 7, 3, N'VDT002-010', 3290000.00, 2690000.00, NULL, 23, NULL, 1, '2026-06-11T13:25:52.272', 1748500.00),
(277, 30, 7, 2, N'VDT002-011', 3290000.00, 2690000.00, NULL, 7, NULL, 1, '2026-06-11T13:25:52.272', 1748500.00),
(278, 30, 7, 4, N'VDT002-012', 3290000.00, 2690000.00, NULL, 11, NULL, 1, '2026-06-11T13:25:52.281', 1748500.00),
(279, 31, 3, 6, N'VDT003-001', 2890000.00, 2890000.00, NULL, 15, NULL, 1, '2026-06-11T13:25:52.288', 1878500.00),
(280, 31, 3, 4, N'VDT003-002', 2890000.00, 2890000.00, NULL, 6, NULL, 1, '2026-06-11T13:25:52.291', 1878500.00),
(281, 31, 3, 5, N'VDT003-003', 2890000.00, 2890000.00, NULL, 22, NULL, 1, '2026-06-11T13:25:52.294', 1878500.00),
(282, 31, 3, 2, N'VDT003-004', 2890000.00, 2890000.00, NULL, 16, NULL, 1, '2026-06-11T13:25:52.294', 1878500.00),
(283, 31, 1, 6, N'VDT003-005', 2890000.00, 2890000.00, NULL, 23, NULL, 1, '2026-06-11T13:25:52.302', 1878500.00),
(284, 31, 1, 4, N'VDT003-006', 2890000.00, 2890000.00, NULL, 24, NULL, 1, '2026-06-11T13:25:52.304', 1878500.00),
(285, 31, 1, 5, N'VDT003-007', 2890000.00, 2890000.00, NULL, 7, NULL, 1, '2026-06-11T13:25:52.304', 1878500.00),
(286, 31, 1, 2, N'VDT003-008', 2890000.00, 2890000.00, NULL, 23, NULL, 1, '2026-06-11T13:25:52.304', 1878500.00),
(287, 31, 8, 6, N'VDT003-009', 2890000.00, 2890000.00, NULL, 8, NULL, 1, '2026-06-11T13:25:52.312', 1878500.00),
(288, 31, 8, 4, N'VDT003-010', 2890000.00, 2890000.00, NULL, 19, NULL, 1, '2026-06-11T13:25:52.315', 1878500.00),
(289, 31, 8, 5, N'VDT003-011', 2890000.00, 2890000.00, NULL, 16, NULL, 1, '2026-06-11T13:25:52.315', 1878500.00),
(290, 31, 8, 2, N'VDT003-012', 2890000.00, 2890000.00, NULL, 24, NULL, 1, '2026-06-11T13:25:52.320', 1878500.00),
(291, 31, 6, 6, N'VDT003-013', 2890000.00, 2890000.00, NULL, 13, NULL, 1, '2026-06-11T13:25:52.325', 1878500.00),
(292, 31, 6, 4, N'VDT003-014', 2890000.00, 2890000.00, NULL, 11, NULL, 1, '2026-06-11T13:25:52.325', 1878500.00),
(293, 31, 6, 5, N'VDT003-015', 2890000.00, 2890000.00, NULL, 24, NULL, 1, '2026-06-11T13:25:52.325', 1878500.00),
(294, 31, 6, 2, N'VDT003-016', 2890000.00, 2890000.00, NULL, 15, NULL, 1, '2026-06-11T13:25:52.333', 1878500.00),
(295, 32, 1, 4, N'VDT004-001', 3490000.00, 2890000.00, NULL, 20, NULL, 1, '2026-06-11T13:25:52.335', 1878500.00),
(296, 32, 1, 1, N'VDT004-002', 3490000.00, 2890000.00, NULL, 24, NULL, 1, '2026-06-11T13:25:52.346', 1878500.00),
(297, 32, 1, 2, N'VDT004-003', 3490000.00, 2890000.00, NULL, 11, NULL, 1, '2026-06-11T13:25:52.347', 1878500.00),
(298, 32, 1, 5, N'VDT004-004', 3490000.00, 2890000.00, NULL, 5, NULL, 1, '2026-06-11T13:25:52.352', 1878500.00),
(299, 32, 1, 6, N'VDT004-005', 3490000.00, 2890000.00, NULL, 14, NULL, 1, '2026-06-11T13:25:52.354', 1878500.00),
(300, 32, 2, 4, N'VDT004-006', 3490000.00, 2890000.00, NULL, 23, NULL, 1, '2026-06-11T13:25:52.356', 1878500.00),
(301, 32, 2, 1, N'VDT004-007', 3490000.00, 2890000.00, NULL, 12, NULL, 1, '2026-06-11T13:25:52.356', 1878500.00),
(302, 32, 2, 2, N'VDT004-008', 3490000.00, 2890000.00, NULL, 15, NULL, 1, '2026-06-11T13:25:52.356', 1878500.00),
(303, 32, 2, 5, N'VDT004-009', 3490000.00, 2890000.00, NULL, 14, NULL, 1, '2026-06-11T13:25:52.365', 1878500.00),
(304, 32, 2, 6, N'VDT004-010', 3490000.00, 2890000.00, NULL, 12, NULL, 1, '2026-06-11T13:25:52.367', 1878500.00),
(305, 32, 8, 4, N'VDT004-011', 3490000.00, 2890000.00, NULL, 5, NULL, 1, '2026-06-11T13:25:52.367', 1878500.00),
(306, 32, 8, 1, N'VDT004-012', 3490000.00, 2890000.00, NULL, 20, NULL, 1, '2026-06-11T13:25:52.367', 1878500.00),
(307, 32, 8, 2, N'VDT004-013', 3490000.00, 2890000.00, NULL, 6, NULL, 1, '2026-06-11T13:25:52.367', 1878500.00),
(308, 32, 8, 5, N'VDT004-014', 3490000.00, 2890000.00, NULL, 15, NULL, 1, '2026-06-11T13:25:52.377', 1878500.00),
(309, 32, 8, 6, N'VDT004-015', 3490000.00, 2890000.00, NULL, 8, NULL, 1, '2026-06-11T13:25:52.377', 1878500.00),
(310, 33, 2, 1, N'VTT006-001', 1590000.00, 1590000.00, NULL, 6, NULL, 1, '2026-06-11T20:19:58.836', 1033500.00),
(311, 33, 2, 2, N'VTT006-002', 1590000.00, 1590000.00, NULL, 7, NULL, 1, '2026-06-11T20:19:58.840', 1033500.00),
(312, 33, 2, 3, N'VTT006-003', 1590000.00, 1350000.00, 15.00, 8, NULL, 1, '2026-06-11T20:19:58.863', 877500.00),
(313, 33, 2, 4, N'VTT006-004', 1590000.00, 1590000.00, NULL, 9, NULL, 1, '2026-06-11T20:19:58.866', 1033500.00),
(314, 33, 5, 1, N'VTT006-005', 1590000.00, 1590000.00, NULL, 10, NULL, 1, '2026-06-11T20:19:58.866', 1033500.00),
(315, 33, 5, 2, N'VTT006-006', 1590000.00, 1350000.00, 15.00, 11, NULL, 1, '2026-06-11T20:19:58.870', 877500.00),
(316, 33, 5, 3, N'VTT006-007', 1590000.00, 1590000.00, NULL, 12, NULL, 1, '2026-06-11T20:19:58.870', 1033500.00),
(317, 33, 5, 4, N'VTT006-008', 1590000.00, 1590000.00, NULL, 13, NULL, 1, '2026-06-11T20:19:58.870', 1033500.00),
(318, 34, 2, 1, N'VTT007-009', 1590000.00, 1350000.00, 15.00, 14, NULL, 1, '2026-06-11T20:19:58.870', 877500.00),
(319, 34, 2, 2, N'VTT007-010', 1590000.00, 1590000.00, NULL, 15, NULL, 1, '2026-06-11T20:19:58.870', 1033500.00),
(320, 34, 2, 3, N'VTT007-011', 1590000.00, 1590000.00, NULL, 16, NULL, 1, '2026-06-11T20:19:58.873', 1033500.00),
(321, 34, 2, 4, N'VTT007-012', 1590000.00, 1350000.00, 15.00, 17, NULL, 1, '2026-06-11T20:19:58.873', 877500.00),
(322, 34, 5, 1, N'VTT007-013', 1590000.00, 1590000.00, NULL, 18, NULL, 1, '2026-06-11T20:19:58.873', 1033500.00),
(323, 34, 5, 2, N'VTT007-014', 1590000.00, 1590000.00, NULL, 19, NULL, 1, '2026-06-11T20:19:58.873', 1033500.00),
(324, 34, 5, 3, N'VTT007-015', 1590000.00, 1350000.00, 15.00, 20, NULL, 1, '2026-06-11T20:19:58.876', 877500.00),
(325, 34, 5, 4, N'VTT007-016', 1590000.00, 1590000.00, NULL, 21, NULL, 1, '2026-06-11T20:19:58.876', 1033500.00),
(326, 35, 2, 1, N'VTT008-017', 1590000.00, 1590000.00, NULL, 22, NULL, 1, '2026-06-11T20:19:58.876', 1033500.00),
(327, 35, 2, 2, N'VTT008-018', 1590000.00, 1350000.00, 15.00, 23, NULL, 1, '2026-06-11T20:19:58.876', 877500.00),
(328, 35, 2, 3, N'VTT008-019', 1590000.00, 1590000.00, NULL, 24, NULL, 1, '2026-06-11T20:19:58.880', 1033500.00),
(329, 35, 2, 4, N'VTT008-020', 1590000.00, 1590000.00, NULL, 5, NULL, 1, '2026-06-11T20:19:58.880', 1033500.00),
(330, 35, 5, 1, N'VTT008-021', 1590000.00, 1350000.00, 15.00, 6, NULL, 1, '2026-06-11T20:19:58.880', 877500.00),
(331, 35, 5, 2, N'VTT008-022', 1590000.00, 1590000.00, NULL, 7, NULL, 1, '2026-06-11T20:19:58.880', 1033500.00),
(332, 35, 5, 3, N'VTT008-023', 1590000.00, 1590000.00, NULL, 8, NULL, 1, '2026-06-11T20:19:58.880', 1033500.00),
(333, 35, 5, 4, N'VTT008-024', 1590000.00, 1350000.00, 15.00, 9, NULL, 1, '2026-06-11T20:19:58.883', 877500.00),
(334, 36, 2, 1, N'VTT009-025', 1590000.00, 1590000.00, NULL, 10, NULL, 1, '2026-06-11T20:19:58.883', 1033500.00),
(335, 36, 2, 2, N'VTT009-026', 1590000.00, 1590000.00, NULL, 11, NULL, 1, '2026-06-11T20:19:58.883', 1033500.00),
(336, 36, 2, 3, N'VTT009-027', 1590000.00, 1350000.00, 15.00, 12, NULL, 1, '2026-06-11T20:19:58.886', 877500.00),
(337, 36, 2, 4, N'VTT009-028', 1590000.00, 1590000.00, NULL, 13, NULL, 1, '2026-06-11T20:19:58.886', 1033500.00),
(338, 36, 5, 1, N'VTT009-029', 1590000.00, 1590000.00, NULL, 14, NULL, 1, '2026-06-11T20:19:58.886', 1033500.00),
(339, 36, 5, 2, N'VTT009-030', 1590000.00, 1350000.00, 15.00, 15, NULL, 1, '2026-06-11T20:19:58.886', 877500.00),
(340, 36, 5, 3, N'VTT009-031', 1590000.00, 1590000.00, NULL, 16, NULL, 1, '2026-06-11T20:19:58.890', 1033500.00),
(341, 36, 5, 4, N'VTT009-032', 1590000.00, 1590000.00, NULL, 17, NULL, 1, '2026-06-11T20:19:58.890', 1033500.00),
(342, 37, 2, 1, N'VTT010-033', 1590000.00, 1350000.00, 15.00, 18, NULL, 1, '2026-06-11T20:19:58.890', 877500.00),
(343, 37, 2, 2, N'VTT010-034', 1590000.00, 1590000.00, NULL, 19, NULL, 1, '2026-06-11T20:19:58.893', 1033500.00),
(344, 37, 2, 3, N'VTT010-035', 1590000.00, 1590000.00, NULL, 20, NULL, 1, '2026-06-11T20:19:58.893', 1033500.00),
(345, 37, 2, 4, N'VTT010-036', 1590000.00, 1350000.00, 15.00, 21, NULL, 1, '2026-06-11T20:19:58.893', 877500.00),
(346, 37, 5, 1, N'VTT010-037', 1590000.00, 1590000.00, NULL, 22, NULL, 1, '2026-06-11T20:19:58.896', 1033500.00),
(347, 37, 5, 2, N'VTT010-038', 1590000.00, 1590000.00, NULL, 23, NULL, 1, '2026-06-11T20:19:58.896', 1033500.00),
(348, 37, 5, 3, N'VTT010-039', 1590000.00, 1350000.00, 15.00, 24, NULL, 1, '2026-06-11T20:19:58.896', 877500.00),
(349, 37, 5, 4, N'VTT010-040', 1590000.00, 1590000.00, NULL, 5, NULL, 1, '2026-06-11T20:19:58.900', 1033500.00),
(350, 38, 2, 1, N'VCT007-041', 1190000.00, 1190000.00, NULL, 6, NULL, 1, '2026-06-11T20:19:58.900', 773500.00),
(351, 38, 2, 2, N'VCT007-042', 1190000.00, 1010000.00, 15.00, 7, NULL, 1, '2026-06-11T20:19:58.900', 656500.00),
(352, 38, 2, 3, N'VCT007-043', 1190000.00, 1190000.00, NULL, 8, NULL, 1, '2026-06-11T20:19:58.900', 773500.00),
(353, 38, 2, 4, N'VCT007-044', 1190000.00, 1190000.00, NULL, 9, NULL, 1, '2026-06-11T20:19:58.903', 773500.00),
(354, 38, 5, 1, N'VCT007-045', 1190000.00, 1010000.00, 15.00, 10, NULL, 1, '2026-06-11T20:19:58.910', 656500.00),
(355, 38, 5, 2, N'VCT007-046', 1190000.00, 1190000.00, NULL, 11, NULL, 1, '2026-06-11T20:19:58.910', 773500.00),
(356, 38, 5, 3, N'VCT007-047', 1190000.00, 1190000.00, NULL, 12, NULL, 1, '2026-06-11T20:19:58.910', 773500.00),
(357, 38, 5, 4, N'VCT007-048', 1190000.00, 1010000.00, 15.00, 13, NULL, 1, '2026-06-11T20:19:58.913', 656500.00),
(358, 39, 2, 1, N'VCT008-049', 1190000.00, 1190000.00, NULL, 14, NULL, 1, '2026-06-11T20:19:58.913', 773500.00),
(359, 39, 2, 2, N'VCT008-050', 1190000.00, 1190000.00, NULL, 15, NULL, 1, '2026-06-11T20:19:58.913', 773500.00),
(360, 39, 2, 3, N'VCT008-051', 1190000.00, 1010000.00, 15.00, 16, NULL, 1, '2026-06-11T20:19:58.916', 656500.00),
(361, 39, 2, 4, N'VCT008-052', 1190000.00, 1190000.00, NULL, 17, NULL, 1, '2026-06-11T20:19:58.916', 773500.00),
(362, 39, 5, 1, N'VCT008-053', 1190000.00, 1190000.00, NULL, 18, NULL, 1, '2026-06-11T20:19:58.920', 773500.00),
(363, 39, 5, 2, N'VCT008-054', 1190000.00, 1010000.00, 15.00, 19, NULL, 1, '2026-06-11T20:19:58.920', 656500.00),
(364, 39, 5, 3, N'VCT008-055', 1190000.00, 1190000.00, NULL, 20, NULL, 1, '2026-06-11T20:19:58.920', 773500.00),
(365, 39, 5, 4, N'VCT008-056', 1190000.00, 1190000.00, NULL, 21, NULL, 1, '2026-06-11T20:19:58.920', 773500.00),
(366, 40, 2, 1, N'VCT009-057', 1190000.00, 1010000.00, 15.00, 22, NULL, 1, '2026-06-11T20:19:58.920', 656500.00),
(367, 40, 2, 2, N'VCT009-058', 1190000.00, 1190000.00, NULL, 23, NULL, 1, '2026-06-11T20:19:58.923', 773500.00),
(368, 40, 2, 3, N'VCT009-059', 1190000.00, 1190000.00, NULL, 24, NULL, 1, '2026-06-11T20:19:58.923', 773500.00),
(369, 40, 2, 4, N'VCT009-060', 1190000.00, 1010000.00, 15.00, 5, NULL, 1, '2026-06-11T20:19:58.923', 656500.00),
(370, 40, 5, 1, N'VCT009-061', 1190000.00, 1190000.00, NULL, 6, NULL, 1, '2026-06-11T20:19:58.926', 773500.00),
(371, 40, 5, 2, N'VCT009-062', 1190000.00, 1190000.00, NULL, 7, NULL, 1, '2026-06-11T20:19:58.926', 773500.00),
(372, 40, 5, 3, N'VCT009-063', 1190000.00, 1010000.00, 15.00, 8, NULL, 1, '2026-06-11T20:19:58.926', 656500.00),
(373, 40, 5, 4, N'VCT009-064', 1190000.00, 1190000.00, NULL, 9, NULL, 1, '2026-06-11T20:19:58.926', 773500.00),
(374, 41, 2, 1, N'VCT010-065', 1190000.00, 1190000.00, NULL, 10, NULL, 1, '2026-06-11T20:19:58.930', 773500.00),
(375, 41, 2, 2, N'VCT010-066', 1190000.00, 1010000.00, 15.00, 11, NULL, 1, '2026-06-11T20:19:58.930', 656500.00),
(376, 41, 2, 3, N'VCT010-067', 1190000.00, 1190000.00, NULL, 12, NULL, 1, '2026-06-11T20:19:58.930', 773500.00),
(377, 41, 2, 4, N'VCT010-068', 1190000.00, 1190000.00, NULL, 13, NULL, 1, '2026-06-11T20:19:58.930', 773500.00),
(378, 41, 5, 1, N'VCT010-069', 1190000.00, 1010000.00, 15.00, 14, NULL, 1, '2026-06-11T20:19:58.930', 656500.00),
(379, 41, 5, 2, N'VCT010-070', 1190000.00, 1190000.00, NULL, 15, NULL, 1, '2026-06-11T20:19:58.930', 773500.00),
(380, 41, 5, 3, N'VCT010-071', 1190000.00, 1190000.00, NULL, 16, NULL, 1, '2026-06-11T20:19:58.936', 773500.00),
(381, 41, 5, 4, N'VCT010-072', 1190000.00, 1010000.00, 15.00, 17, NULL, 1, '2026-06-11T20:19:58.940', 656500.00),
(382, 42, 2, 1, N'VCT011-073', 1190000.00, 1190000.00, NULL, 18, NULL, 1, '2026-06-11T20:19:58.940', 773500.00),
(383, 42, 2, 2, N'VCT011-074', 1190000.00, 1190000.00, NULL, 19, NULL, 1, '2026-06-11T20:19:58.943', 773500.00),
(384, 42, 2, 3, N'VCT011-075', 1190000.00, 1010000.00, 15.00, 20, NULL, 1, '2026-06-11T20:19:58.943', 656500.00),
(385, 42, 2, 4, N'VCT011-076', 1190000.00, 1190000.00, NULL, 21, NULL, 1, '2026-06-11T20:19:58.946', 773500.00),
(386, 42, 5, 1, N'VCT011-077', 1190000.00, 1190000.00, NULL, 22, NULL, 1, '2026-06-11T20:19:58.946', 773500.00),
(387, 42, 5, 2, N'VCT011-078', 1190000.00, 1010000.00, 15.00, 23, NULL, 1, '2026-06-11T20:19:58.946', 656500.00),
(388, 42, 5, 3, N'VCT011-079', 1190000.00, 1190000.00, NULL, 24, NULL, 1, '2026-06-11T20:19:58.946', 773500.00),
(389, 42, 5, 4, N'VCT011-080', 1190000.00, 1190000.00, NULL, 5, NULL, 1, '2026-06-11T20:19:58.950', 773500.00),
(390, 43, 2, 1, N'VDH006-081', 2990000.00, 2540000.00, 15.00, 6, NULL, 1, '2026-06-11T20:19:58.950', 1651000.00),
(391, 43, 2, 2, N'VDH006-082', 2990000.00, 2990000.00, NULL, 7, NULL, 1, '2026-06-11T20:19:58.950', 1943500.00),
(392, 43, 2, 3, N'VDH006-083', 2990000.00, 2990000.00, NULL, 8, NULL, 1, '2026-06-11T20:19:58.950', 1943500.00),
(393, 43, 2, 4, N'VDH006-084', 2990000.00, 2540000.00, 15.00, 9, NULL, 1, '2026-06-11T20:19:58.953', 1651000.00),
(394, 43, 5, 1, N'VDH006-085', 2990000.00, 2990000.00, NULL, 10, NULL, 1, '2026-06-11T20:19:58.953', 1943500.00),
(395, 43, 5, 2, N'VDH006-086', 2990000.00, 2990000.00, NULL, 11, NULL, 1, '2026-06-11T20:19:58.953', 1943500.00),
(396, 43, 5, 3, N'VDH006-087', 2990000.00, 2540000.00, 15.00, 12, NULL, 1, '2026-06-11T20:19:58.956', 1651000.00),
(397, 43, 5, 4, N'VDH006-088', 2990000.00, 2990000.00, NULL, 13, NULL, 1, '2026-06-11T20:19:58.956', 1943500.00),
(398, 44, 2, 1, N'VDH007-089', 2990000.00, 2990000.00, NULL, 14, NULL, 1, '2026-06-11T20:19:58.960', 1943500.00),
(399, 44, 2, 2, N'VDH007-090', 2990000.00, 2540000.00, 15.00, 15, NULL, 1, '2026-06-11T20:19:58.960', 1651000.00),
(400, 44, 2, 3, N'VDH007-091', 2990000.00, 2990000.00, NULL, 16, NULL, 1, '2026-06-11T20:19:58.960', 1943500.00),
(401, 44, 2, 4, N'VDH007-092', 2990000.00, 2990000.00, NULL, 17, NULL, 1, '2026-06-11T20:19:58.960', 1943500.00);
INSERT INTO [dbo].[san_pham_chi_tiet] ([id], [id_san_pham], [id_mau_sac], [id_kich_thuoc], [ma_san_pham_chi_tiet], [gia_ban_goc], [gia_ban], [phan_tram_giam], [so_luong], [anh_url], [trang_thai], [ngay_tao], [gia_nhap]) VALUES
(402, 44, 5, 1, N'VDH007-093', 2990000.00, 2540000.00, 15.00, 18, NULL, 1, '2026-06-11T20:19:58.963', 1651000.00),
(403, 44, 5, 2, N'VDH007-094', 2990000.00, 2990000.00, NULL, 19, NULL, 1, '2026-06-11T20:19:58.963', 1943500.00),
(404, 44, 5, 3, N'VDH007-095', 2990000.00, 2990000.00, NULL, 20, NULL, 1, '2026-06-11T20:19:58.963', 1943500.00),
(405, 44, 5, 4, N'VDH007-096', 2990000.00, 2540000.00, 15.00, 21, NULL, 1, '2026-06-11T20:19:58.963', 1651000.00),
(406, 45, 2, 1, N'VDH008-097', 2990000.00, 2990000.00, NULL, 22, NULL, 1, '2026-06-11T20:19:58.966', 1943500.00),
(407, 45, 2, 2, N'VDH008-098', 2990000.00, 2990000.00, NULL, 23, NULL, 1, '2026-06-11T20:19:58.966', 1943500.00),
(408, 45, 2, 3, N'VDH008-099', 2990000.00, 2540000.00, 15.00, 24, NULL, 1, '2026-06-11T20:19:58.970', 1651000.00),
(409, 45, 2, 4, N'VDH008-100', 2990000.00, 2990000.00, NULL, 5, NULL, 1, '2026-06-11T20:19:58.970', 1943500.00),
(410, 45, 5, 1, N'VDH008-101', 2990000.00, 2990000.00, NULL, 6, NULL, 1, '2026-06-11T20:19:58.970', 1943500.00),
(411, 45, 5, 2, N'VDH008-102', 2990000.00, 2540000.00, 15.00, 7, NULL, 1, '2026-06-11T20:19:58.970', 1651000.00),
(412, 45, 5, 3, N'VDH008-103', 2990000.00, 2990000.00, NULL, 8, NULL, 1, '2026-06-11T20:19:58.970', 1943500.00),
(413, 45, 5, 4, N'VDH008-104', 2990000.00, 2990000.00, NULL, 9, NULL, 1, '2026-06-11T20:19:58.973', 1943500.00),
(414, 46, 2, 1, N'VDH009-105', 2990000.00, 2540000.00, 15.00, 10, NULL, 1, '2026-06-11T20:19:58.973', 1651000.00),
(415, 46, 2, 2, N'VDH009-106', 2990000.00, 2990000.00, NULL, 11, NULL, 1, '2026-06-11T20:19:58.973', 1943500.00),
(416, 46, 2, 3, N'VDH009-107', 2990000.00, 2990000.00, NULL, 12, NULL, 1, '2026-06-11T20:19:58.976', 1943500.00),
(417, 46, 2, 4, N'VDH009-108', 2990000.00, 2540000.00, 15.00, 13, NULL, 1, '2026-06-11T20:19:58.976', 1651000.00),
(418, 46, 5, 1, N'VDH009-109', 2990000.00, 2990000.00, NULL, 14, NULL, 1, '2026-06-11T20:19:58.980', 1943500.00),
(419, 46, 5, 2, N'VDH009-110', 2990000.00, 2990000.00, NULL, 15, NULL, 1, '2026-06-11T20:19:58.980', 1943500.00),
(420, 46, 5, 3, N'VDH009-111', 2990000.00, 2540000.00, 15.00, 16, NULL, 1, '2026-06-11T20:19:58.980', 1651000.00),
(421, 46, 5, 4, N'VDH009-112', 2990000.00, 2990000.00, NULL, 17, NULL, 1, '2026-06-11T20:19:58.980', 1943500.00),
(422, 47, 2, 1, N'VDH010-113', 2990000.00, 2990000.00, NULL, 18, NULL, 1, '2026-06-11T20:19:58.983', 1943500.00),
(423, 47, 2, 2, N'VDH010-114', 2990000.00, 2540000.00, 15.00, 19, NULL, 1, '2026-06-11T20:19:58.983', 1651000.00),
(424, 47, 2, 3, N'VDH010-115', 2990000.00, 2990000.00, NULL, 20, NULL, 1, '2026-06-11T20:19:58.983', 1943500.00),
(425, 47, 2, 4, N'VDH010-116', 2990000.00, 2990000.00, NULL, 21, NULL, 1, '2026-06-11T20:19:58.983', 1943500.00),
(426, 47, 5, 1, N'VDH010-117', 2990000.00, 2540000.00, 15.00, 22, NULL, 1, '2026-06-11T20:19:58.983', 1651000.00),
(427, 47, 5, 2, N'VDH010-118', 2990000.00, 2990000.00, NULL, 23, NULL, 1, '2026-06-11T20:19:58.986', 1943500.00),
(428, 47, 5, 3, N'VDH010-119', 2990000.00, 2990000.00, NULL, 24, NULL, 1, '2026-06-11T20:19:58.986', 1943500.00),
(429, 47, 5, 4, N'VDH010-120', 2990000.00, 2540000.00, 15.00, 5, NULL, 1, '2026-06-11T20:19:58.986', 1651000.00),
(430, 48, 2, 1, N'VC006-121', 4990000.00, 4990000.00, NULL, 6, NULL, 1, '2026-06-11T20:19:58.986', 3243500.00),
(431, 48, 2, 2, N'VC006-122', 4990000.00, 4990000.00, NULL, 7, NULL, 1, '2026-06-11T20:19:58.990', 3243500.00),
(432, 48, 2, 3, N'VC006-123', 4990000.00, 4240000.00, 15.00, 8, NULL, 1, '2026-06-11T20:19:58.990', 2756000.00),
(433, 48, 2, 4, N'VC006-124', 4990000.00, 4990000.00, NULL, 9, NULL, 1, '2026-06-11T20:19:58.990', 3243500.00),
(434, 48, 5, 1, N'VC006-125', 4990000.00, 4990000.00, NULL, 10, NULL, 1, '2026-06-11T20:19:58.990', 3243500.00),
(435, 48, 5, 2, N'VC006-126', 4990000.00, 4240000.00, 15.00, 11, NULL, 1, '2026-06-11T20:19:58.993', 2756000.00),
(436, 48, 5, 3, N'VC006-127', 4990000.00, 4990000.00, NULL, 12, NULL, 1, '2026-06-11T20:19:58.993', 3243500.00),
(437, 48, 5, 4, N'VC006-128', 4990000.00, 4990000.00, NULL, 13, NULL, 1, '2026-06-11T20:19:58.996', 3243500.00),
(438, 49, 2, 1, N'VC007-129', 4990000.00, 4240000.00, 15.00, 14, NULL, 1, '2026-06-11T20:19:58.996', 2756000.00),
(439, 49, 2, 2, N'VC007-130', 4990000.00, 4990000.00, NULL, 15, NULL, 1, '2026-06-11T20:19:58.996', 3243500.00),
(440, 49, 2, 3, N'VC007-131', 4990000.00, 4990000.00, NULL, 16, NULL, 1, '2026-06-11T20:19:58.996', 3243500.00),
(441, 49, 2, 4, N'VC007-132', 4990000.00, 4240000.00, 15.00, 17, NULL, 1, '2026-06-11T20:19:59.000', 2756000.00),
(442, 49, 5, 1, N'VC007-133', 4990000.00, 4990000.00, NULL, 18, NULL, 1, '2026-06-11T20:19:59.000', 3243500.00),
(443, 49, 5, 2, N'VC007-134', 4990000.00, 4990000.00, NULL, 19, NULL, 1, '2026-06-11T20:19:59.003', 3243500.00),
(444, 49, 5, 3, N'VC007-135', 4990000.00, 4240000.00, 15.00, 20, NULL, 1, '2026-06-11T20:19:59.003', 2756000.00),
(445, 49, 5, 4, N'VC007-136', 4990000.00, 4990000.00, NULL, 21, NULL, 1, '2026-06-11T20:19:59.006', 3243500.00),
(446, 50, 2, 1, N'VC008-137', 4990000.00, 4990000.00, NULL, 22, NULL, 1, '2026-06-11T20:19:59.006', 3243500.00),
(447, 50, 2, 2, N'VC008-138', 4990000.00, 4240000.00, 15.00, 23, NULL, 1, '2026-06-11T20:19:59.006', 2756000.00),
(448, 50, 2, 3, N'VC008-139', 4990000.00, 4990000.00, NULL, 24, NULL, 1, '2026-06-11T20:19:59.010', 3243500.00),
(449, 50, 2, 4, N'VC008-140', 4990000.00, 4990000.00, NULL, 5, NULL, 1, '2026-06-11T20:19:59.010', 3243500.00),
(450, 50, 5, 1, N'VC008-141', 4990000.00, 4240000.00, 15.00, 6, NULL, 1, '2026-06-11T20:19:59.010', 2756000.00),
(451, 50, 5, 2, N'VC008-142', 4990000.00, 4990000.00, NULL, 7, NULL, 1, '2026-06-11T20:19:59.013', 3243500.00),
(452, 50, 5, 3, N'VC008-143', 4990000.00, 4990000.00, NULL, 8, NULL, 1, '2026-06-11T20:19:59.013', 3243500.00),
(453, 50, 5, 4, N'VC008-144', 4990000.00, 4240000.00, 15.00, 9, NULL, 1, '2026-06-11T20:19:59.013', 2756000.00),
(454, 51, 2, 1, N'VC009-145', 4990000.00, 4990000.00, NULL, 10, NULL, 1, '2026-06-11T20:19:59.016', 3243500.00),
(455, 51, 2, 2, N'VC009-146', 4990000.00, 4990000.00, NULL, 11, NULL, 1, '2026-06-11T20:19:59.016', 3243500.00),
(456, 51, 2, 3, N'VC009-147', 4990000.00, 4240000.00, 15.00, 12, NULL, 1, '2026-06-11T20:19:59.016', 2756000.00),
(457, 51, 2, 4, N'VC009-148', 4990000.00, 4990000.00, NULL, 13, NULL, 1, '2026-06-11T20:19:59.020', 3243500.00),
(458, 51, 5, 1, N'VC009-149', 4990000.00, 4990000.00, NULL, 14, NULL, 1, '2026-06-11T20:19:59.020', 3243500.00),
(459, 51, 5, 2, N'VC009-150', 4990000.00, 4240000.00, 15.00, 15, NULL, 1, '2026-06-11T20:19:59.020', 2756000.00),
(460, 51, 5, 3, N'VC009-151', 4990000.00, 4990000.00, NULL, 16, NULL, 1, '2026-06-11T20:19:59.020', 3243500.00),
(461, 51, 5, 4, N'VC009-152', 4990000.00, 4990000.00, NULL, 17, NULL, 1, '2026-06-11T20:19:59.020', 3243500.00),
(462, 52, 2, 1, N'VC010-153', 4990000.00, 4240000.00, 15.00, 18, NULL, 1, '2026-06-11T20:19:59.023', 2756000.00),
(463, 52, 2, 2, N'VC010-154', 4990000.00, 4990000.00, NULL, 19, NULL, 1, '2026-06-11T20:19:59.023', 3243500.00),
(464, 52, 2, 3, N'VC010-155', 4990000.00, 4990000.00, NULL, 20, NULL, 1, '2026-06-11T20:19:59.023', 3243500.00),
(465, 52, 2, 4, N'VC010-156', 4990000.00, 4240000.00, 15.00, 21, NULL, 1, '2026-06-11T20:19:59.023', 2756000.00),
(466, 52, 5, 1, N'VC010-157', 4990000.00, 4990000.00, NULL, 22, NULL, 1, '2026-06-11T20:19:59.023', 3243500.00),
(467, 52, 5, 2, N'VC010-158', 4990000.00, 4990000.00, NULL, 23, NULL, 1, '2026-06-11T20:19:59.026', 3243500.00),
(468, 52, 5, 3, N'VC010-159', 4990000.00, 4240000.00, 15.00, 24, NULL, 1, '2026-06-11T20:19:59.026', 2756000.00),
(469, 52, 5, 4, N'VC010-160', 4990000.00, 4990000.00, NULL, 5, NULL, 1, '2026-06-11T20:19:59.030', 3243500.00),
(470, 53, 2, 1, N'VHS005-161', 450000.00, 450000.00, NULL, 6, NULL, 1, '2026-06-11T20:19:59.030', 292500.00),
(471, 53, 2, 2, N'VHS005-162', 450000.00, 380000.00, 16.00, 7, NULL, 1, '2026-06-11T20:19:59.030', 247000.00),
(472, 53, 2, 3, N'VHS005-163', 450000.00, 450000.00, NULL, 8, NULL, 1, '2026-06-11T20:19:59.030', 292500.00),
(473, 53, 2, 4, N'VHS005-164', 450000.00, 450000.00, NULL, 9, NULL, 1, '2026-06-11T20:19:59.030', 292500.00),
(474, 53, 5, 1, N'VHS005-165', 450000.00, 380000.00, 16.00, 10, NULL, 1, '2026-06-11T20:19:59.033', 247000.00),
(475, 53, 5, 2, N'VHS005-166', 450000.00, 450000.00, NULL, 11, NULL, 1, '2026-06-11T20:19:59.033', 292500.00),
(476, 53, 5, 3, N'VHS005-167', 450000.00, 450000.00, NULL, 12, NULL, 1, '2026-06-11T20:19:59.033', 292500.00),
(477, 53, 5, 4, N'VHS005-168', 450000.00, 380000.00, 16.00, 13, NULL, 1, '2026-06-11T20:19:59.036', 247000.00),
(478, 54, 2, 1, N'VHS006-169', 450000.00, 450000.00, NULL, 14, NULL, 1, '2026-06-11T20:19:59.036', 292500.00),
(479, 54, 2, 2, N'VHS006-170', 450000.00, 450000.00, NULL, 15, NULL, 1, '2026-06-11T20:19:59.036', 292500.00),
(480, 54, 2, 3, N'VHS006-171', 450000.00, 380000.00, 16.00, 16, NULL, 1, '2026-06-11T20:19:59.036', 247000.00),
(481, 54, 2, 4, N'VHS006-172', 450000.00, 450000.00, NULL, 17, NULL, 1, '2026-06-11T20:19:59.040', 292500.00),
(482, 54, 5, 1, N'VHS006-173', 450000.00, 450000.00, NULL, 18, NULL, 1, '2026-06-11T20:19:59.040', 292500.00),
(483, 54, 5, 2, N'VHS006-174', 450000.00, 380000.00, 16.00, 19, NULL, 1, '2026-06-11T20:19:59.040', 247000.00),
(484, 54, 5, 3, N'VHS006-175', 450000.00, 450000.00, NULL, 20, NULL, 1, '2026-06-11T20:19:59.040', 292500.00),
(485, 54, 5, 4, N'VHS006-176', 450000.00, 450000.00, NULL, 21, NULL, 1, '2026-06-11T20:19:59.040', 292500.00),
(486, 55, 2, 1, N'VHS007-177', 450000.00, 380000.00, 16.00, 22, NULL, 1, '2026-06-11T20:19:59.043', 247000.00),
(487, 55, 2, 2, N'VHS007-178', 450000.00, 450000.00, NULL, 23, NULL, 1, '2026-06-11T20:19:59.043', 292500.00),
(488, 55, 2, 3, N'VHS007-179', 450000.00, 450000.00, NULL, 24, NULL, 1, '2026-06-11T20:19:59.043', 292500.00),
(489, 55, 2, 4, N'VHS007-180', 450000.00, 380000.00, 16.00, 5, NULL, 1, '2026-06-11T20:19:59.046', 247000.00),
(490, 55, 5, 1, N'VHS007-181', 450000.00, 450000.00, NULL, 6, NULL, 1, '2026-06-11T20:19:59.046', 292500.00),
(491, 55, 5, 2, N'VHS007-182', 450000.00, 450000.00, NULL, 7, NULL, 1, '2026-06-11T20:19:59.046', 292500.00),
(492, 55, 5, 3, N'VHS007-183', 450000.00, 380000.00, 16.00, 8, NULL, 1, '2026-06-11T20:19:59.046', 247000.00),
(493, 55, 5, 4, N'VHS007-184', 450000.00, 450000.00, NULL, 9, NULL, 1, '2026-06-11T20:19:59.050', 292500.00),
(494, 56, 2, 1, N'VHS008-185', 450000.00, 450000.00, NULL, 10, NULL, 1, '2026-06-11T20:19:59.050', 292500.00),
(495, 56, 2, 2, N'VHS008-186', 450000.00, 380000.00, 16.00, 11, NULL, 1, '2026-06-11T20:19:59.050', 247000.00),
(496, 56, 2, 3, N'VHS008-187', 450000.00, 450000.00, NULL, 12, NULL, 1, '2026-06-11T20:19:59.053', 292500.00),
(497, 56, 2, 4, N'VHS008-188', 450000.00, 450000.00, NULL, 13, NULL, 1, '2026-06-11T20:19:59.053', 292500.00),
(498, 56, 5, 1, N'VHS008-189', 450000.00, 380000.00, 16.00, 14, NULL, 1, '2026-06-11T20:19:59.053', 247000.00),
(499, 56, 5, 2, N'VHS008-190', 450000.00, 450000.00, NULL, 15, NULL, 1, '2026-06-11T20:19:59.053', 292500.00),
(500, 56, 5, 3, N'VHS008-191', 450000.00, 450000.00, NULL, 16, NULL, 1, '2026-06-11T20:19:59.056', 292500.00),
(501, 56, 5, 4, N'VHS008-192', 450000.00, 380000.00, 16.00, 17, NULL, 1, '2026-06-11T20:19:59.056', 247000.00),
(502, 57, 2, 1, N'VHS009-193', 450000.00, 450000.00, NULL, 18, NULL, 1, '2026-06-11T20:19:59.056', 292500.00),
(503, 57, 2, 2, N'VHS009-194', 450000.00, 450000.00, NULL, 19, NULL, 1, '2026-06-11T20:19:59.060', 292500.00),
(504, 57, 2, 3, N'VHS009-195', 450000.00, 380000.00, 16.00, 20, NULL, 1, '2026-06-11T20:19:59.060', 247000.00),
(505, 57, 2, 4, N'VHS009-196', 450000.00, 450000.00, NULL, 21, NULL, 1, '2026-06-11T20:19:59.060', 292500.00),
(506, 57, 5, 1, N'VHS009-197', 450000.00, 450000.00, NULL, 22, NULL, 1, '2026-06-11T20:19:59.060', 292500.00),
(507, 57, 5, 2, N'VHS009-198', 450000.00, 380000.00, 16.00, 23, NULL, 1, '2026-06-11T20:19:59.060', 247000.00),
(508, 57, 5, 3, N'VHS009-199', 450000.00, 450000.00, NULL, 24, NULL, 1, '2026-06-11T20:19:59.063', 292500.00),
(509, 57, 5, 4, N'VHS009-200', 450000.00, 450000.00, NULL, 5, NULL, 1, '2026-06-11T20:19:59.063', 292500.00),
(510, 58, 2, 1, N'VCS005-201', 990000.00, 840000.00, 15.00, 6, NULL, 1, '2026-06-11T20:19:59.063', 546000.00),
(511, 58, 2, 2, N'VCS005-202', 990000.00, 990000.00, NULL, 7, NULL, 1, '2026-06-11T20:19:59.063', 643500.00),
(512, 58, 2, 3, N'VCS005-203', 990000.00, 990000.00, NULL, 8, NULL, 1, '2026-06-11T20:19:59.066', 643500.00),
(513, 58, 2, 4, N'VCS005-204', 990000.00, 840000.00, 15.00, 9, NULL, 1, '2026-06-11T20:19:59.066', 546000.00),
(514, 58, 5, 1, N'VCS005-205', 990000.00, 990000.00, NULL, 10, NULL, 1, '2026-06-11T20:19:59.066', 643500.00),
(515, 58, 5, 2, N'VCS005-206', 990000.00, 990000.00, NULL, 11, NULL, 1, '2026-06-11T20:19:59.066', 643500.00),
(516, 58, 5, 3, N'VCS005-207', 990000.00, 840000.00, 15.00, 12, NULL, 1, '2026-06-11T20:19:59.070', 546000.00),
(517, 58, 5, 4, N'VCS005-208', 990000.00, 990000.00, NULL, 13, NULL, 1, '2026-06-11T20:19:59.070', 643500.00),
(518, 59, 2, 1, N'VCS006-209', 990000.00, 990000.00, NULL, 14, NULL, 1, '2026-06-11T20:19:59.070', 643500.00),
(519, 59, 2, 2, N'VCS006-210', 990000.00, 840000.00, 15.00, 15, NULL, 1, '2026-06-11T20:19:59.073', 546000.00),
(520, 59, 2, 3, N'VCS006-211', 990000.00, 990000.00, NULL, 16, NULL, 1, '2026-06-11T20:19:59.073', 643500.00),
(521, 59, 2, 4, N'VCS006-212', 990000.00, 990000.00, NULL, 17, NULL, 1, '2026-06-11T20:19:59.073', 643500.00),
(522, 59, 5, 1, N'VCS006-213', 990000.00, 840000.00, 15.00, 18, NULL, 1, '2026-06-11T20:19:59.076', 546000.00),
(523, 59, 5, 2, N'VCS006-214', 990000.00, 990000.00, NULL, 19, NULL, 1, '2026-06-11T20:19:59.076', 643500.00),
(524, 59, 5, 3, N'VCS006-215', 990000.00, 990000.00, NULL, 20, NULL, 1, '2026-06-11T20:19:59.076', 643500.00),
(525, 59, 5, 4, N'VCS006-216', 990000.00, 840000.00, 15.00, 21, NULL, 1, '2026-06-11T20:19:59.076', 546000.00),
(526, 60, 2, 1, N'VCS007-217', 990000.00, 990000.00, NULL, 22, NULL, 1, '2026-06-11T20:19:59.080', 643500.00),
(527, 60, 2, 2, N'VCS007-218', 990000.00, 990000.00, NULL, 23, NULL, 1, '2026-06-11T20:19:59.080', 643500.00),
(528, 60, 2, 3, N'VCS007-219', 990000.00, 840000.00, 15.00, 24, NULL, 1, '2026-06-11T20:19:59.080', 546000.00),
(529, 60, 2, 4, N'VCS007-220', 990000.00, 990000.00, NULL, 5, NULL, 1, '2026-06-11T20:19:59.080', 643500.00),
(530, 60, 5, 1, N'VCS007-221', 990000.00, 990000.00, NULL, 6, NULL, 1, '2026-06-11T20:19:59.080', 643500.00),
(531, 60, 5, 2, N'VCS007-222', 990000.00, 840000.00, 15.00, 7, NULL, 1, '2026-06-11T20:19:59.080', 546000.00),
(532, 60, 5, 3, N'VCS007-223', 990000.00, 990000.00, NULL, 8, NULL, 1, '2026-06-11T20:19:59.083', 643500.00),
(533, 60, 5, 4, N'VCS007-224', 990000.00, 990000.00, NULL, 9, NULL, 1, '2026-06-11T20:19:59.083', 643500.00),
(534, 61, 2, 1, N'VCS008-225', 990000.00, 840000.00, 15.00, 10, NULL, 1, '2026-06-11T20:19:59.083', 546000.00),
(535, 61, 2, 2, N'VCS008-226', 990000.00, 990000.00, NULL, 11, NULL, 1, '2026-06-11T20:19:59.083', 643500.00),
(536, 61, 2, 3, N'VCS008-227', 990000.00, 990000.00, NULL, 12, NULL, 1, '2026-06-11T20:19:59.086', 643500.00),
(537, 61, 2, 4, N'VCS008-228', 990000.00, 840000.00, 15.00, 13, NULL, 1, '2026-06-11T20:19:59.086', 546000.00),
(538, 61, 5, 1, N'VCS008-229', 990000.00, 990000.00, NULL, 14, NULL, 1, '2026-06-11T20:19:59.086', 643500.00),
(539, 61, 5, 2, N'VCS008-230', 990000.00, 990000.00, NULL, 15, NULL, 1, '2026-06-11T20:19:59.090', 643500.00),
(540, 61, 5, 3, N'VCS008-231', 990000.00, 840000.00, 15.00, 16, NULL, 1, '2026-06-11T20:19:59.090', 546000.00),
(541, 61, 5, 4, N'VCS008-232', 990000.00, 990000.00, NULL, 17, NULL, 1, '2026-06-11T20:19:59.090', 643500.00),
(542, 62, 2, 1, N'VCS009-233', 990000.00, 990000.00, NULL, 18, NULL, 1, '2026-06-11T20:19:59.090', 643500.00),
(543, 62, 2, 2, N'VCS009-234', 990000.00, 840000.00, 15.00, 19, NULL, 1, '2026-06-11T20:19:59.093', 546000.00),
(544, 62, 2, 3, N'VCS009-235', 990000.00, 990000.00, NULL, 20, NULL, 1, '2026-06-11T20:19:59.093', 643500.00),
(545, 62, 2, 4, N'VCS009-236', 990000.00, 990000.00, NULL, 21, NULL, 1, '2026-06-11T20:19:59.096', 643500.00),
(546, 62, 5, 1, N'VCS009-237', 990000.00, 840000.00, 15.00, 22, NULL, 1, '2026-06-11T20:19:59.096', 546000.00),
(547, 62, 5, 2, N'VCS009-238', 990000.00, 990000.00, NULL, 23, NULL, 1, '2026-06-11T20:19:59.096', 643500.00),
(548, 62, 5, 3, N'VCS009-239', 990000.00, 990000.00, NULL, 24, NULL, 1, '2026-06-11T20:19:59.096', 643500.00),
(549, 62, 5, 4, N'VCS009-240', 990000.00, 840000.00, 15.00, 5, NULL, 1, '2026-06-11T20:19:59.100', 546000.00);
SET IDENTITY_INSERT [dbo].[san_pham_chi_tiet] OFF;
END
GO

-- ===== Anh =====
IF OBJECT_ID(N'dbo.Anh','U') IS NULL
BEGIN
CREATE TABLE [dbo].[Anh] (
  [id] int IDENTITY(1,1) NOT NULL,
  [id_san_pham] int NOT NULL,
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
INSERT INTO [dbo].[Anh] ([id], [id_san_pham], [anh_url], [trang_thai], [ngay_tao]) VALUES
(1, 1, N'/images/products/shirt1.jpg', 1, '2026-06-12T17:38:33.610'),
(2, 1, N'/images/products/shirt2.jpg', 1, '2026-06-12T17:38:33.623'),
(3, 1, N'/images/products/shirt3.jpg', 1, '2026-06-12T17:38:33.623'),
(4, 1, N'/images/products/shirt4.jpg', 1, '2026-06-12T17:38:33.623'),
(5, 2, N'/images/products/pants1.jpg', 1, '2026-06-12T17:38:33.626'),
(6, 2, N'/images/products/pants2.jpg', 1, '2026-06-12T17:38:33.630'),
(7, 2, N'/images/products/pants3.jpg', 1, '2026-06-12T17:38:33.630'),
(8, 2, N'/images/products/pants4.jpg', 1, '2026-06-12T17:38:33.630'),
(9, 3, N'/images/products/dress1.jpg', 1, '2026-06-12T17:38:33.633'),
(10, 3, N'/images/products/dress2.jpg', 1, '2026-06-12T17:38:33.636'),
(11, 3, N'/images/products/dress3.jpg', 1, '2026-06-12T17:38:33.636'),
(12, 3, N'/images/products/dress4.jpg', 1, '2026-06-12T17:38:33.636'),
(13, 4, N'/images/products/accessories1.jpg', 1, '2026-06-12T17:38:33.640'),
(14, 4, N'/images/products/accessories2.jpg', 1, '2026-06-12T17:38:33.646'),
(15, 4, N'/images/products/accessories3.jpg', 1, '2026-06-12T17:38:33.646'),
(16, 4, N'/images/products/accessories4.jpg', 1, '2026-06-12T17:38:33.646'),
(17, 5, N'/images/products/shirt14.jpg', 1, '2026-06-12T17:38:33.646'),
(18, 5, N'/images/products/pants8.jpg', 1, '2026-06-12T17:38:33.653'),
(19, 5, N'/images/products/shirt15.jpg', 1, '2026-06-12T17:38:33.653'),
(20, 5, N'/images/products/pants9.jpg', 1, '2026-06-12T17:38:33.653'),
(21, 6, N'/images/products/dress15.jpg', 1, '2026-06-12T17:38:33.653'),
(22, 6, N'/images/products/dress16.jpg', 1, '2026-06-12T17:38:33.660'),
(23, 6, N'/images/products/dress17.jpg', 1, '2026-06-12T17:38:33.660'),
(24, 6, N'/images/products/dress18.jpg', 1, '2026-06-12T17:38:33.660'),
(25, 9, N'/images/products/shirt2.jpg', 1, '2026-06-12T17:38:33.660'),
(26, 9, N'/images/products/shirt4.jpg', 1, '2026-06-12T17:38:33.666'),
(27, 9, N'/images/products/shirt6.jpg', 1, '2026-06-12T17:38:33.666'),
(28, 9, N'/images/products/shirt8.jpg', 1, '2026-06-12T17:38:33.666'),
(29, 10, N'/images/products/shirt3.jpg', 1, '2026-06-12T17:38:33.670'),
(30, 10, N'/images/products/shirt5.jpg', 1, '2026-06-12T17:38:33.673'),
(31, 10, N'/images/products/shirt7.jpg', 1, '2026-06-12T17:38:33.673'),
(32, 10, N'/images/products/shirt9.jpg', 1, '2026-06-12T17:38:33.673'),
(33, 11, N'/images/products/shirt5.jpg', 1, '2026-06-12T17:38:33.676'),
(34, 11, N'/images/products/shirt6.jpg', 1, '2026-06-12T17:38:33.683'),
(35, 11, N'/images/products/shirt7.jpg', 1, '2026-06-12T17:38:33.683'),
(36, 11, N'/images/products/shirt8.jpg', 1, '2026-06-12T17:38:33.683'),
(37, 12, N'/images/products/shirt4.jpg', 1, '2026-06-12T17:38:33.683'),
(38, 12, N'/images/products/shirt7.jpg', 1, '2026-06-12T17:38:33.690'),
(39, 12, N'/images/products/shirt10.jpg', 1, '2026-06-12T17:38:33.690'),
(40, 12, N'/images/products/shirt13.jpg', 1, '2026-06-12T17:38:33.690'),
(41, 13, N'/images/products/pants2.jpg', 1, '2026-06-12T17:38:33.690'),
(42, 13, N'/images/products/pants4.jpg', 1, '2026-06-12T17:38:33.696'),
(43, 13, N'/images/products/pants6.jpg', 1, '2026-06-12T17:38:33.696'),
(44, 13, N'/images/products/pants8.jpg', 1, '2026-06-12T17:38:33.696'),
(45, 14, N'/images/products/pants3.jpg', 1, '2026-06-12T17:38:33.696'),
(46, 14, N'/images/products/pants5.jpg', 1, '2026-06-12T17:38:33.703'),
(47, 14, N'/images/products/pants7.jpg', 1, '2026-06-12T17:38:33.703'),
(48, 14, N'/images/products/pants9.jpg', 1, '2026-06-12T17:38:33.703'),
(49, 15, N'/images/products/pants4.jpg', 1, '2026-06-12T17:38:33.706'),
(50, 15, N'/images/products/pants6.jpg', 1, '2026-06-12T17:38:33.710'),
(51, 15, N'/images/products/pants8.jpg', 1, '2026-06-12T17:38:33.710'),
(52, 15, N'/images/products/pants10.jpg', 1, '2026-06-12T17:38:33.710'),
(53, 16, N'/images/products/pants5.jpg', 1, '2026-06-12T17:38:33.713'),
(54, 16, N'/images/products/pants7.jpg', 1, '2026-06-12T17:38:33.720'),
(55, 16, N'/images/products/pants9.jpg', 1, '2026-06-12T17:38:33.720'),
(56, 16, N'/images/products/pants11.jpg', 1, '2026-06-12T17:38:33.720'),
(57, 17, N'/images/products/dress2.jpg', 1, '2026-06-12T17:38:33.720'),
(58, 17, N'/images/products/dress4.jpg', 1, '2026-06-12T17:38:33.726'),
(59, 17, N'/images/products/dress6.jpg', 1, '2026-06-12T17:38:33.726'),
(60, 17, N'/images/products/dress8.jpg', 1, '2026-06-12T17:38:33.726'),
(61, 18, N'/images/products/dress3.jpg', 1, '2026-06-12T17:38:33.726'),
(62, 18, N'/images/products/dress5.jpg', 1, '2026-06-12T17:38:33.733'),
(63, 18, N'/images/products/dress7.jpg', 1, '2026-06-12T17:38:33.733'),
(64, 18, N'/images/products/dress9.jpg', 1, '2026-06-12T17:38:33.733'),
(65, 19, N'/images/products/dress4.jpg', 1, '2026-06-12T17:38:33.736'),
(66, 19, N'/images/products/dress6.jpg', 1, '2026-06-12T17:38:33.743'),
(67, 19, N'/images/products/dress8.jpg', 1, '2026-06-12T17:38:33.743'),
(68, 19, N'/images/products/dress10.jpg', 1, '2026-06-12T17:38:33.743'),
(69, 20, N'/images/products/dress5.jpg', 1, '2026-06-12T17:38:33.743'),
(70, 20, N'/images/products/dress7.jpg', 1, '2026-06-12T17:38:33.773'),
(71, 20, N'/images/products/dress9.jpg', 1, '2026-06-12T17:38:33.773'),
(72, 20, N'/images/products/dress11.jpg', 1, '2026-06-12T17:38:33.773'),
(73, 21, N'/images/products/dress12.jpg', 1, '2026-06-12T17:38:33.776'),
(74, 21, N'/images/products/shirt15.jpg', 1, '2026-06-12T17:38:33.780'),
(75, 21, N'/images/products/dress13.jpg', 1, '2026-06-12T17:38:33.780'),
(76, 21, N'/images/products/shirt16.jpg', 1, '2026-06-12T17:38:33.780'),
(77, 22, N'/images/products/shirt17.jpg', 1, '2026-06-12T17:38:33.783'),
(78, 22, N'/images/products/pants9.jpg', 1, '2026-06-12T17:38:33.790'),
(79, 22, N'/images/products/shirt18.jpg', 1, '2026-06-12T17:38:33.790'),
(80, 22, N'/images/products/pants10.jpg', 1, '2026-06-12T17:38:33.790'),
(81, 23, N'/images/products/pants10.jpg', 1, '2026-06-12T17:38:33.790'),
(82, 23, N'/images/products/shirt18.jpg', 1, '2026-06-12T17:38:33.796'),
(83, 23, N'/images/products/pants11.jpg', 1, '2026-06-12T17:38:33.796'),
(84, 23, N'/images/products/shirt19.jpg', 1, '2026-06-12T17:38:33.796'),
(85, 24, N'/images/products/dress13.jpg', 1, '2026-06-12T17:38:33.800'),
(86, 24, N'/images/products/shirt19.jpg', 1, '2026-06-12T17:38:33.806'),
(87, 24, N'/images/products/dress14.jpg', 1, '2026-06-12T17:38:33.806'),
(88, 24, N'/images/products/shirt20.jpg', 1, '2026-06-12T17:38:33.806'),
(89, 25, N'/images/products/accessories2.jpg', 1, '2026-06-12T17:38:33.806'),
(90, 25, N'/images/products/accessories4.jpg', 1, '2026-06-12T17:38:33.813'),
(91, 25, N'/images/products/accessories6.jpg', 1, '2026-06-12T17:38:33.813'),
(92, 25, N'/images/products/accessories8.jpg', 1, '2026-06-12T17:38:33.813'),
(93, 26, N'/images/products/accessories3.jpg', 1, '2026-06-12T17:38:33.816'),
(94, 26, N'/images/products/accessories5.jpg', 1, '2026-06-12T17:38:33.823'),
(95, 26, N'/images/products/accessories7.jpg', 1, '2026-06-12T17:38:33.823'),
(96, 26, N'/images/products/accessories9.jpg', 1, '2026-06-12T17:38:33.823'),
(97, 27, N'/images/products/accessories4.jpg', 1, '2026-06-12T17:38:33.823'),
(98, 27, N'/images/products/accessories6.jpg', 1, '2026-06-12T17:38:33.830'),
(99, 27, N'/images/products/accessories8.jpg', 1, '2026-06-12T17:38:33.830'),
(100, 27, N'/images/products/accessories10.jpg', 1, '2026-06-12T17:38:33.830'),
(101, 28, N'/images/products/accessories5.jpg', 1, '2026-06-12T17:38:33.830'),
(102, 28, N'/images/products/accessories7.jpg', 1, '2026-06-12T17:38:33.836'),
(103, 28, N'/images/products/accessories9.jpg', 1, '2026-06-12T17:38:33.836'),
(104, 28, N'/images/products/accessories11.jpg', 1, '2026-06-12T17:38:33.836'),
(105, 29, N'/images/products/dress16.jpg', 1, '2026-06-12T17:38:33.840'),
(106, 29, N'/images/products/dress17.jpg', 1, '2026-06-12T17:38:33.843'),
(107, 29, N'/images/products/dress18.jpg', 1, '2026-06-12T17:38:33.843'),
(108, 29, N'/images/products/dress19.jpg', 1, '2026-06-12T17:38:33.843'),
(109, 30, N'/images/products/dress17.jpg', 1, '2026-06-12T17:38:33.846'),
(110, 30, N'/images/products/dress18.jpg', 1, '2026-06-12T17:38:33.850'),
(111, 30, N'/images/products/dress19.jpg', 1, '2026-06-12T17:38:33.850'),
(112, 30, N'/images/products/dress20.jpg', 1, '2026-06-12T17:38:33.850'),
(113, 31, N'/images/products/dress18.jpg', 1, '2026-06-12T17:38:33.853'),
(114, 31, N'/images/products/dress19.jpg', 1, '2026-06-12T17:38:33.860'),
(115, 31, N'/images/products/dress20.jpg', 1, '2026-06-12T17:38:33.860'),
(116, 31, N'/images/products/dress1.jpg', 1, '2026-06-12T17:38:33.860'),
(117, 32, N'/images/products/dress19.jpg', 1, '2026-06-12T17:38:33.860'),
(118, 32, N'/images/products/dress20.jpg', 1, '2026-06-12T17:38:33.866'),
(119, 32, N'/images/products/dress1.jpg', 1, '2026-06-12T17:38:33.866'),
(120, 32, N'/images/products/dress2.jpg', 1, '2026-06-12T17:38:33.866'),
(121, 33, N'/images/products/shirt5.jpg', 1, '2026-06-12T17:38:33.870'),
(122, 33, N'/images/products/shirt8.jpg', 1, '2026-06-12T17:38:33.873'),
(123, 33, N'/images/products/shirt10.jpg', 1, '2026-06-12T17:38:33.873'),
(124, 33, N'/images/products/shirt12.jpg', 1, '2026-06-12T17:38:33.873'),
(125, 34, N'/images/products/shirt9.jpg', 1, '2026-06-12T17:38:33.876'),
(126, 34, N'/images/products/shirt10.jpg', 1, '2026-06-12T17:38:33.880'),
(127, 34, N'/images/products/shirt11.jpg', 1, '2026-06-12T17:38:33.880'),
(128, 34, N'/images/products/shirt12.jpg', 1, '2026-06-12T17:38:33.880'),
(129, 35, N'/images/products/pants6.jpg', 1, '2026-06-12T17:38:33.883'),
(130, 35, N'/images/products/pants8.jpg', 1, '2026-06-12T17:38:33.890'),
(131, 35, N'/images/products/pants10.jpg', 1, '2026-06-12T17:38:33.890'),
(132, 35, N'/images/products/pants12.jpg', 1, '2026-06-12T17:38:33.890'),
(133, 36, N'/images/products/shirt6.jpg', 1, '2026-06-12T17:38:33.890'),
(134, 36, N'/images/products/shirt10.jpg', 1, '2026-06-12T17:38:33.896'),
(135, 36, N'/images/products/shirt14.jpg', 1, '2026-06-12T17:38:33.896'),
(136, 36, N'/images/products/shirt18.jpg', 1, '2026-06-12T17:38:33.896'),
(137, 37, N'/images/products/shirt11.jpg', 1, '2026-06-12T17:38:33.896'),
(138, 37, N'/images/products/shirt12.jpg', 1, '2026-06-12T17:38:33.903'),
(139, 37, N'/images/products/shirt13.jpg', 1, '2026-06-12T17:38:33.903'),
(140, 37, N'/images/products/shirt14.jpg', 1, '2026-06-12T17:38:33.903'),
(141, 38, N'/images/products/shirt7.jpg', 1, '2026-06-12T17:38:33.903'),
(142, 38, N'/images/products/shirt13.jpg', 1, '2026-06-12T17:38:33.910'),
(143, 38, N'/images/products/shirt15.jpg', 1, '2026-06-12T17:38:33.910'),
(144, 38, N'/images/products/shirt17.jpg', 1, '2026-06-12T17:38:33.910'),
(145, 39, N'/images/products/shirt8.jpg', 1, '2026-06-12T17:38:33.910'),
(146, 39, N'/images/products/shirt15.jpg', 1, '2026-06-12T17:38:33.916'),
(147, 39, N'/images/products/shirt17.jpg', 1, '2026-06-12T17:38:33.916'),
(148, 39, N'/images/products/shirt19.jpg', 1, '2026-06-12T17:38:33.916'),
(149, 40, N'/images/products/shirt9.jpg', 1, '2026-06-12T17:38:33.916'),
(150, 40, N'/images/products/shirt18.jpg', 1, '2026-06-12T17:38:33.923'),
(151, 40, N'/images/products/shirt20.jpg', 1, '2026-06-12T17:38:33.923'),
(152, 40, N'/images/products/shirt1.jpg', 1, '2026-06-12T17:38:33.923'),
(153, 41, N'/images/products/shirt15.jpg', 1, '2026-06-12T17:38:33.926'),
(154, 41, N'/images/products/shirt2.jpg', 1, '2026-06-12T17:38:33.930'),
(155, 41, N'/images/products/shirt3.jpg', 1, '2026-06-12T17:38:33.930'),
(156, 41, N'/images/products/shirt4.jpg', 1, '2026-06-12T17:38:33.930'),
(157, 42, N'/images/products/pants7.jpg', 1, '2026-06-12T17:38:33.933'),
(158, 42, N'/images/products/pants13.jpg', 1, '2026-06-12T17:38:33.936'),
(159, 42, N'/images/products/pants15.jpg', 1, '2026-06-12T17:38:33.936'),
(160, 42, N'/images/products/pants17.jpg', 1, '2026-06-12T17:38:33.936'),
(161, 43, N'/images/products/dress6.jpg', 1, '2026-06-12T17:38:33.940'),
(162, 43, N'/images/products/dress11.jpg', 1, '2026-06-12T17:38:33.943'),
(163, 43, N'/images/products/dress12.jpg', 1, '2026-06-12T17:38:33.943'),
(164, 43, N'/images/products/dress13.jpg', 1, '2026-06-12T17:38:33.943'),
(165, 44, N'/images/products/dress7.jpg', 1, '2026-06-12T17:38:33.946'),
(166, 44, N'/images/products/dress13.jpg', 1, '2026-06-12T17:38:33.950'),
(167, 44, N'/images/products/dress14.jpg', 1, '2026-06-12T17:38:33.950'),
(168, 44, N'/images/products/dress15.jpg', 1, '2026-06-12T17:38:33.950'),
(169, 45, N'/images/products/dress8.jpg', 1, '2026-06-12T17:38:33.953'),
(170, 45, N'/images/products/dress15.jpg', 1, '2026-06-12T17:38:33.960'),
(171, 45, N'/images/products/dress16.jpg', 1, '2026-06-12T17:38:33.960'),
(172, 45, N'/images/products/dress17.jpg', 1, '2026-06-12T17:38:33.960'),
(173, 46, N'/images/products/dress9.jpg', 1, '2026-06-12T17:38:33.960'),
(174, 46, N'/images/products/dress17.jpg', 1, '2026-06-12T17:38:33.963'),
(175, 46, N'/images/products/dress18.jpg', 1, '2026-06-12T17:38:33.963'),
(176, 46, N'/images/products/dress19.jpg', 1, '2026-06-12T17:38:33.963'),
(177, 47, N'/images/products/dress10.jpg', 1, '2026-06-12T17:38:33.966'),
(178, 47, N'/images/products/dress19.jpg', 1, '2026-06-12T17:38:33.970'),
(179, 47, N'/images/products/dress20.jpg', 1, '2026-06-12T17:38:33.970'),
(180, 47, N'/images/products/dress1.jpg', 1, '2026-06-12T17:38:33.970'),
(181, 48, N'/images/products/accessories6.jpg', 1, '2026-06-12T17:38:33.973'),
(182, 48, N'/images/products/accessories11.jpg', 1, '2026-06-12T17:38:33.976'),
(183, 48, N'/images/products/accessories12.jpg', 1, '2026-06-12T17:38:33.976'),
(184, 48, N'/images/products/accessories13.jpg', 1, '2026-06-12T17:38:33.976'),
(185, 49, N'/images/products/accessories7.jpg', 1, '2026-06-12T17:38:33.980'),
(186, 49, N'/images/products/accessories13.jpg', 1, '2026-06-12T17:38:33.983'),
(187, 49, N'/images/products/accessories14.jpg', 1, '2026-06-12T17:38:33.983'),
(188, 49, N'/images/products/accessories15.jpg', 1, '2026-06-12T17:38:33.983'),
(189, 50, N'/images/products/accessories8.jpg', 1, '2026-06-12T17:38:33.986'),
(190, 50, N'/images/products/accessories15.jpg', 1, '2026-06-12T17:38:33.990'),
(191, 50, N'/images/products/accessories16.jpg', 1, '2026-06-12T17:38:33.990'),
(192, 50, N'/images/products/accessories17.jpg', 1, '2026-06-12T17:38:33.990'),
(193, 51, N'/images/products/accessories9.jpg', 1, '2026-06-12T17:38:33.993'),
(194, 51, N'/images/products/accessories17.jpg', 1, '2026-06-12T17:38:33.996'),
(195, 51, N'/images/products/accessories18.jpg', 1, '2026-06-12T17:38:33.996'),
(196, 51, N'/images/products/accessories19.jpg', 1, '2026-06-12T17:38:33.996'),
(197, 52, N'/images/products/accessories10.jpg', 1, '2026-06-12T17:38:34.000'),
(198, 52, N'/images/products/accessories19.jpg', 1, '2026-06-12T17:38:34.006'),
(199, 52, N'/images/products/accessories20.jpg', 1, '2026-06-12T17:38:34.006'),
(200, 52, N'/images/products/accessories1.jpg', 1, '2026-06-12T17:38:34.006');
INSERT INTO [dbo].[Anh] ([id], [id_san_pham], [anh_url], [trang_thai], [ngay_tao]) VALUES
(201, 53, N'/images/products/shirt10.jpg', 1, '2026-06-12T17:38:34.006'),
(202, 53, N'/images/products/shirt20.jpg', 1, '2026-06-12T17:38:34.013'),
(203, 53, N'/images/products/shirt1.jpg', 1, '2026-06-12T17:38:34.013'),
(204, 53, N'/images/products/shirt2.jpg', 1, '2026-06-12T17:38:34.013'),
(205, 54, N'/images/products/dress11.jpg', 1, '2026-06-12T17:38:34.013'),
(206, 54, N'/images/products/dress18.jpg', 1, '2026-06-12T17:38:34.020'),
(207, 54, N'/images/products/dress19.jpg', 1, '2026-06-12T17:38:34.020'),
(208, 54, N'/images/products/dress20.jpg', 1, '2026-06-12T17:38:34.020'),
(209, 55, N'/images/products/shirt11.jpg', 1, '2026-06-12T17:38:34.020'),
(210, 55, N'/images/products/shirt18.jpg', 1, '2026-06-12T17:38:34.026'),
(211, 55, N'/images/products/shirt3.jpg', 1, '2026-06-12T17:38:34.026'),
(212, 55, N'/images/products/shirt4.jpg', 1, '2026-06-12T17:38:34.026'),
(213, 56, N'/images/products/shirt12.jpg', 1, '2026-06-12T17:38:34.030'),
(214, 56, N'/images/products/shirt19.jpg', 1, '2026-06-12T17:38:34.033'),
(215, 56, N'/images/products/shirt5.jpg', 1, '2026-06-12T17:38:34.033'),
(216, 56, N'/images/products/shirt6.jpg', 1, '2026-06-12T17:38:34.033'),
(217, 57, N'/images/products/shirt13.jpg', 1, '2026-06-12T17:38:34.036'),
(218, 57, N'/images/products/shirt20.jpg', 1, '2026-06-12T17:38:34.040'),
(219, 57, N'/images/products/shirt7.jpg', 1, '2026-06-12T17:38:34.040'),
(220, 57, N'/images/products/shirt8.jpg', 1, '2026-06-12T17:38:34.040'),
(221, 58, N'/images/products/shirt20.jpg', 1, '2026-06-12T17:38:34.043'),
(222, 58, N'/images/products/dress14.jpg', 1, '2026-06-12T17:38:34.050'),
(223, 58, N'/images/products/shirt1.jpg', 1, '2026-06-12T17:38:34.050'),
(224, 58, N'/images/products/dress15.jpg', 1, '2026-06-12T17:38:34.050'),
(225, 59, N'/images/products/shirt1.jpg', 1, '2026-06-12T17:38:34.050'),
(226, 59, N'/images/products/pants11.jpg', 1, '2026-06-12T17:38:34.056'),
(227, 59, N'/images/products/shirt2.jpg', 1, '2026-06-12T17:38:34.056'),
(228, 59, N'/images/products/pants12.jpg', 1, '2026-06-12T17:38:34.056'),
(229, 60, N'/images/products/shirt2.jpg', 1, '2026-06-12T17:38:34.056'),
(230, 60, N'/images/products/pants12.jpg', 1, '2026-06-12T17:38:34.063'),
(231, 60, N'/images/products/shirt3.jpg', 1, '2026-06-12T17:38:34.063'),
(232, 60, N'/images/products/pants13.jpg', 1, '2026-06-12T17:38:34.063'),
(233, 61, N'/images/products/shirt3.jpg', 1, '2026-06-12T17:38:34.066'),
(234, 61, N'/images/products/pants13.jpg', 1, '2026-06-12T17:38:34.070'),
(235, 61, N'/images/products/shirt4.jpg', 1, '2026-06-12T17:38:34.070'),
(236, 61, N'/images/products/pants14.jpg', 1, '2026-06-12T17:38:34.070'),
(237, 62, N'/images/products/shirt4.jpg', 1, '2026-06-12T17:38:34.073'),
(238, 62, N'/images/products/pants14.jpg', 1, '2026-06-12T17:38:34.080'),
(239, 62, N'/images/products/shirt5.jpg', 1, '2026-06-12T17:38:34.080'),
(240, 62, N'/images/products/pants15.jpg', 1, '2026-06-12T17:38:34.080');
SET IDENTITY_INSERT [dbo].[Anh] OFF;
END
GO

-- Tự động bổ sung ảnh cho tất cả sản phẩm bị thiếu trong bảng Anh
INSERT INTO [dbo].[Anh] ([id_san_pham], [anh_url], [trang_thai], [ngay_tao])
SELECT sp.id,
       CASE 
         WHEN sp.ma_san_pham LIKE 'ASM%' OR sp.ma_san_pham LIKE 'AKH%' THEN N'/images/products/shirt' + CAST(((sp.id % 20) + 1) AS NVARCHAR) + N'.jpg'
         WHEN sp.ma_san_pham LIKE 'QJN%' OR sp.ma_san_pham LIKE 'QTY%' THEN N'/images/products/pants' + CAST(((sp.id % 20) + 1) AS NVARCHAR) + N'.jpg'
         WHEN sp.ma_san_pham LIKE 'PKT%' THEN N'/images/products/accessories' + CAST(((sp.id % 20) + 1) AS NVARCHAR) + N'.jpg'
         ELSE N'/images/products/dress' + CAST(((sp.id % 20) + 1) AS NVARCHAR) + N'.jpg'
       END,
       1,
       GETDATE()
FROM [dbo].[san_pham] sp
WHERE NOT EXISTS (SELECT 1 FROM [dbo].[Anh] a WHERE a.id_san_pham = sp.id AND a.trang_thai = 1);
GO

-- Cập nhật anh_url cho san_pham_chi_tiet nếu đang bị NULL
UPDATE vct
SET vct.anh_url = a.anh_url
FROM [dbo].[san_pham_chi_tiet] vct
JOIN (
    SELECT id_san_pham, MIN(anh_url) AS anh_url
    FROM [dbo].[Anh]
    WHERE trang_thai = 1
    GROUP BY id_san_pham
) a ON a.id_san_pham = vct.id_san_pham
WHERE vct.anh_url IS NULL;
GO

-- ===== Gio_hang =====
IF OBJECT_ID(N'dbo.Gio_hang','U') IS NULL
BEGIN
CREATE TABLE [dbo].[Gio_hang] (
  [id] int IDENTITY(1,1) NOT NULL,
  [id_khach_hang] int NOT NULL,
  [ngay_tao] datetime2(7) NULL,
  CONSTRAINT [PK_Gio_hang] PRIMARY KEY ([id]),
  CONSTRAINT [UQ_Gio_hang_khach_hang] UNIQUE ([id_khach_hang]),
  CONSTRAINT [FK_Gio_hang_Khach_hang] FOREIGN KEY ([id_khach_hang]) REFERENCES [dbo].[Khach_hang]([id])
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
  [id_san_pham_chi_tiet] int NOT NULL,
  [so_luong] int NULL,
  [ngay_tao] datetime2(7) NULL,
  CONSTRAINT [PK_Gio_hang_chi_tiet] PRIMARY KEY ([id]),
  CONSTRAINT [UQ_Gio_hang_chi_tiet_GioHang_BienThe] UNIQUE ([id_gio_hang], [id_san_pham_chi_tiet]),
  CONSTRAINT [FK_Gio_hang_chi_tiet_Gio_hang] FOREIGN KEY ([id_gio_hang]) REFERENCES [dbo].[Gio_hang]([id]),
  CONSTRAINT [FK_Gio_hang_chi_tiet_san_pham_chi_tiet] FOREIGN KEY ([id_san_pham_chi_tiet]) REFERENCES [dbo].[san_pham_chi_tiet]([id]),
  CONSTRAINT [CK_Gio_hang_chi_tiet_so_luong] CHECK ([so_luong] > 0)
);
END
GO
IF NOT EXISTS (SELECT 1 FROM [dbo].[Gio_hang_chi_tiet])
BEGIN
SET IDENTITY_INSERT [dbo].[Gio_hang_chi_tiet] ON;
INSERT INTO [dbo].[Gio_hang_chi_tiet] ([id], [id_gio_hang], [id_san_pham_chi_tiet], [so_luong], [ngay_tao]) VALUES
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
  [id_giam_gia] int NULL,
  [id_nhan_vien] int NULL,
  [ma_hoa_don] nvarchar(80) NOT NULL,
  [ma_yeu_cau] nvarchar(100) NULL,
  [ma_giao_dich_cong] nvarchar(120) NULL,
  [tong_tien] decimal(15,2) NOT NULL,
  [phi_van_chuyen] decimal(15,2) NULL,
  [giam_gia_voucher] decimal(15,2) NULL,
  [hinh_thuc_nhan_hang] tinyint NULL,
  [dia_chi_giao_hang] nvarchar(500) NULL,
  [trang_thai] tinyint NULL,
  [hinh_thuc_thanh_toan] nvarchar(50) NULL,
  [phuong_thuc_thanh_toan_online] nvarchar(50) NULL,
  [ghi_chu] nvarchar(max) NULL,
  [thong_tin_hoan_tien] nvarchar(500) NULL,
  [ngay_tao] datetime2(7) NULL,
  [da_thanh_toan] bit NULL,
  [da_hoan_ton_kho] bit NOT NULL DEFAULT 0,
  [ten_khach_hang] nvarchar(150) NULL,
  [so_dien_thoai] nvarchar(20) NULL,
  [email_khach_hang] nvarchar(150) NULL,
  [huy_don_otp_hash] nvarchar(100) NULL,
  [huy_don_otp_het_han] datetime2(7) NULL,
  [huy_don_otp_so_lan_sai] int NULL,
  [huy_don_otp_gui_luc] datetime2(7) NULL,
  [yeu_cau_vat] bit NULL DEFAULT 0,
  [ten_cong_ty_vat] nvarchar(255) NULL,
  [ma_so_thue_vat] varchar(50) NULL,
  [email_vat] varchar(100) NULL,
  [dia_chi_vat] nvarchar(500) NULL,
  [so_hoa_don_vat] varchar(50) NULL,
  [ma_tra_cuu_vat] varchar(50) NULL,
  [trang_thai_vat] nvarchar(50) NULL,
  [ngay_phat_hanh_vat] datetime2(7) NULL,
  CONSTRAINT [PK_Hoa_don] PRIMARY KEY ([id]),
  CONSTRAINT [UQ_Hoa_don_ma] UNIQUE ([ma_hoa_don]),
  CONSTRAINT [CK_Hoa_don_trang_thai] CHECK ([trang_thai] BETWEEN 0 AND 9),
  CONSTRAINT [CK_Hoa_don_tong_tien] CHECK ([tong_tien] >= 0)
);
END
GO
IF COL_LENGTH('dbo.Hoa_don', 'email_khach_hang') IS NULL
BEGIN
  ALTER TABLE [dbo].[Hoa_don] ADD [email_khach_hang] nvarchar(150) NULL;
END
GO
IF COL_LENGTH('dbo.Hoa_don', 'giam_gia_voucher') IS NULL
BEGIN
  ALTER TABLE [dbo].[Hoa_don] ADD [giam_gia_voucher] decimal(15,2) NULL;
END
GO
IF COL_LENGTH('dbo.Hoa_don', 'huy_don_otp_hash') IS NULL
BEGIN
  ALTER TABLE [dbo].[Hoa_don] ADD [huy_don_otp_hash] nvarchar(100) NULL;
END
GO
IF COL_LENGTH('dbo.Hoa_don', 'huy_don_otp_het_han') IS NULL
BEGIN
  ALTER TABLE [dbo].[Hoa_don] ADD [huy_don_otp_het_han] datetime2(7) NULL;
END
GO
IF COL_LENGTH('dbo.Hoa_don', 'huy_don_otp_so_lan_sai') IS NULL
BEGIN
  ALTER TABLE [dbo].[Hoa_don] ADD [huy_don_otp_so_lan_sai] int NULL;
END
GO
IF COL_LENGTH('dbo.Hoa_don', 'huy_don_otp_gui_luc') IS NULL
BEGIN
  ALTER TABLE [dbo].[Hoa_don] ADD [huy_don_otp_gui_luc] datetime2(7) NULL;
END
GO
IF COL_LENGTH('dbo.Hoa_don', 'thong_tin_hoan_tien') IS NULL
BEGIN
  ALTER TABLE [dbo].[Hoa_don] ADD [thong_tin_hoan_tien] nvarchar(500) NULL;
END
GO
IF COL_LENGTH('dbo.Hoa_don', 'yeu_cau_vat') IS NULL
  ALTER TABLE [dbo].[Hoa_don] ADD [yeu_cau_vat] bit NULL DEFAULT 0;
GO
IF COL_LENGTH('dbo.Hoa_don', 'ten_cong_ty_vat') IS NULL
  ALTER TABLE [dbo].[Hoa_don] ADD [ten_cong_ty_vat] nvarchar(255) NULL;
GO
IF COL_LENGTH('dbo.Hoa_don', 'ma_so_thue_vat') IS NULL
  ALTER TABLE [dbo].[Hoa_don] ADD [ma_so_thue_vat] varchar(50) NULL;
GO
IF COL_LENGTH('dbo.Hoa_don', 'email_vat') IS NULL
  ALTER TABLE [dbo].[Hoa_don] ADD [email_vat] varchar(100) NULL;
GO
IF COL_LENGTH('dbo.Hoa_don', 'dia_chi_vat') IS NULL
  ALTER TABLE [dbo].[Hoa_don] ADD [dia_chi_vat] nvarchar(500) NULL;
GO
IF COL_LENGTH('dbo.Hoa_don', 'so_hoa_don_vat') IS NULL
  ALTER TABLE [dbo].[Hoa_don] ADD [so_hoa_don_vat] varchar(50) NULL;
GO
IF COL_LENGTH('dbo.Hoa_don', 'ma_tra_cuu_vat') IS NULL
  ALTER TABLE [dbo].[Hoa_don] ADD [ma_tra_cuu_vat] varchar(50) NULL;
GO
IF COL_LENGTH('dbo.Hoa_don', 'trang_thai_vat') IS NULL
  ALTER TABLE [dbo].[Hoa_don] ADD [trang_thai_vat] nvarchar(50) NULL;
GO
IF COL_LENGTH('dbo.Hoa_don', 'ngay_phat_hanh_vat') IS NULL
  ALTER TABLE [dbo].[Hoa_don] ADD [ngay_phat_hanh_vat] datetime2(7) NULL;
GO
IF NOT EXISTS (SELECT 1 FROM [dbo].[Hoa_don])
BEGIN
SET IDENTITY_INSERT [dbo].[Hoa_don] ON;
INSERT INTO [dbo].[Hoa_don] ([id], [id_khach_hang], [id_giam_gia], [id_nhan_vien], [ma_hoa_don], [tong_tien], [phi_van_chuyen], [giam_gia_voucher], [hinh_thuc_nhan_hang], [dia_chi_giao_hang], [trang_thai], [hinh_thuc_thanh_toan], [phuong_thuc_thanh_toan_online], [ghi_chu], [ngay_tao], [da_thanh_toan]) VALUES
(1, 1, NULL, 2, N'HD-2025-0001', 6070000.00, 0.00, 0.00, 1, N'128 Trần Hưng Đạo, Hoàn Kiếm, Hà Nội', 0, N'MOMO', NULL, NULL, '2026-06-10T23:44:29.256', 0),
(2, 1, NULL, 2, N'HD-2025-0002', 8580000.00, 0.00, 0.00, 1, N'128 Trần Hưng Đạo, Hoàn Kiếm, Hà Nội', 1, N'COD', NULL, NULL, '2026-06-10T23:44:29.256', 0),
(3, 2, NULL, 2, N'HD-2025-0003', 4290000.00, 0.00, 0.00, 1, N'45 Nguyễn Huệ, Quận 1, TP. Hồ Chí Minh', 2, N'Momo', NULL, NULL, '2026-06-10T23:44:29.256', 0),
(4, 3, NULL, 2, N'HD-2025-0004', 1290000.00, 0.00, 0.00, 2, NULL, 4, N'Ti?n m?t', NULL, NULL, '2026-06-10T23:44:29.256', 0),
(5, 2, NULL, 2, N'HD-2025-0005', 2780000.00, 0.00, 0.00, 1, N'45 Nguyễn Huệ, Quận 1, TP. Hồ Chí Minh', 3, N'MOMO', NULL, NULL, '2026-06-10T23:44:29.256', 1),
(6, NULL, NULL, NULL, N'HD2606111347176783', 2890000.00, 0.00, 0.00, 1, N'Ha Noi', 0, N'MOMO', NULL, NULL, '2026-06-11T13:47:17.973', 0),
(7, 1, NULL, NULL, N'HD2606111353090522', 9960000.00, 0.00, 0.00, 1, N'123 Nguyen Hue, Quan 1, TP.HCM', 0, N'COD', NULL, N'', '2026-06-11T13:53:09.543', 0),
(8, NULL, NULL, NULL, N'HD2606111354569125', 1590000.00, 0.00, 0.00, 1, N'456 Le Loi, Q1, HCM', 1, N'MOMO', N'MOMO', NULL, '2026-06-11T13:54:56.363', 1),
(9, 2, NULL, NULL, N'HD2606111532350499', 8960000.00, 0.00, 0.00, 1, N'nguyen thi due', 0, N'MOMO', NULL, N'', '2026-06-11T15:32:35.619', 0),
(10, 2, NULL, NULL, N'HD2606112024161968', 2890000.00, 0.00, 0.00, 1, N'Nguyen Thi Due', 0, N'MOMO', NULL, N'', '2026-06-11T20:24:16.605', 0),
(11, 2, NULL, NULL, N'HD2606112025033498', 1590000.00, 0.00, 0.00, 1, N'dsgdsagsdfa', 0, N'COD', NULL, N'', '2026-06-11T20:25:03.378', 0),
(12, 2, NULL, NULL, N'HD2606112025367305', 2290000.00, 0.00, 0.00, 1, N'sdfh', 0, N'MOMO', NULL, N'', '2026-06-11T20:25:36.473', 0),
(13, 2, NULL, NULL, N'HD2606112042420187', 1590000.00, 0.00, 0.00, 1, N'rutyj', 0, N'MOMO', NULL, N'', '2026-06-11T20:42:42.031', 0),
(14, 2, NULL, NULL, N'HD2606112100518437', 450000.00, 0.00, 0.00, 1, N'dfgbvv', 0, N'COD', NULL, N'', '2026-06-11T21:00:51.411', 0),
(15, 2, NULL, NULL, N'HD2606132334116643', 2890000.00, 0.00, 0.00, 1, N'áduiygthjkns', 0, N'MOMO', NULL, N'', '2026-06-13T23:34:11.591', 0),
(16, 2, NULL, NULL, N'HD2606132335439207', 2890000.00, 0.00, 0.00, 1, N'trygjhk', 0, N'MOMO', NULL, N'', '2026-06-13T23:35:44.008', 0),
(17, NULL, NULL, NULL, N'HD2606132337193159', 4290000.00, 0.00, 0.00, 1, N'fdg', 0, N'MOMO', NULL, N'', '2026-06-13T23:37:19.247', 0),
(18, NULL, NULL, NULL, N'HD2606132339031838', 4480000.00, 0.00, 0.00, 1, N'fsdhsdfhsdfh', 0, N'MOMO', NULL, N'', '2026-06-13T23:39:03.522', 0),
(19, 2, NULL, NULL, N'HD2606141048179586', 2890000.00, 0.00, 0.00, 1, N'nịuodasfkugyhtadsfokjhig', 3, N'MOMO', NULL, N'', '2026-06-14T10:48:17.814', 1),
(20, 2, NULL, NULL, N'HD2606160939088988', 2890000.00, 0.00, 0.00, 1, N'she', 0, N'MOMO', NULL, N'', '2026-06-16T09:39:08.022', 0);
SET IDENTITY_INSERT [dbo].[Hoa_don] OFF;
END
GO

-- ===== Hoa_don_chi_tiet =====
IF OBJECT_ID(N'dbo.Hoa_don_chi_tiet','U') IS NULL
BEGIN
CREATE TABLE [dbo].[Hoa_don_chi_tiet] (
  [id] int IDENTITY(1,1) NOT NULL,
  [id_hoa_don] int NOT NULL,
  [id_san_pham_chi_tiet] int NOT NULL,
  [so_luong] int NOT NULL,
  [don_gia] decimal(15,2) NOT NULL,
  [phan_tram_giam] decimal(5,2) NULL,
  [thanh_tien] decimal(15,2) NULL,
  [gia_nhap] decimal(18,2) NULL,
  CONSTRAINT [PK_Hoa_don_chi_tiet] PRIMARY KEY ([id])
);
END
GO
IF NOT EXISTS (SELECT 1 FROM [dbo].[Hoa_don_chi_tiet])
BEGIN
SET IDENTITY_INSERT [dbo].[Hoa_don_chi_tiet] ON;
INSERT INTO [dbo].[Hoa_don_chi_tiet] ([id], [id_hoa_don], [id_san_pham_chi_tiet], [so_luong], [don_gia], [phan_tram_giam], [thanh_tien], [gia_nhap]) VALUES
(1, 1, 1, 1, 2890000.00, 0.00, 2890000.00, 1878500.00),
(2, 1, 6, 2, 1590000.00, 0.00, 3180000.00, 1033500.00),
(3, 2, 9, 2, 4290000.00, 0.00, 8580000.00, 2788500.00),
(4, 3, 9, 1, 4290000.00, 0.00, 4290000.00, 2788500.00),
(5, 4, 12, 1, 1290000.00, 0.00, 1290000.00, 838500.00),
(6, 5, 6, 1, 1590000.00, 0.00, 1590000.00, 1033500.00),
(7, 5, 15, 1, 1390000.00, 0.00, 1390000.00, 903500.00),
(8, 6, 1, 1, 2890000.00, 0.00, 2890000.00, 1878500.00),
(9, 7, 1, 1, 2890000.00, 0.00, 2890000.00, 1878500.00),
(10, 7, 9, 1, 4290000.00, 21.86, 3352206.00, 2788500.00),
(11, 7, 15, 2, 1390000.00, 17.75, 2286550.00, 903500.00),
(12, 8, 6, 1, 1590000.00, 20.10, 1270410.00, 1033500.00),
(13, 9, 1, 2, 2890000.00, 0.00, 5780000.00, 1878500.00),
(14, 9, 6, 2, 1590000.00, 20.10, 2540820.00, 1033500.00),
(15, 10, 1, 1, 2890000.00, 0.00, 2890000.00, 1878500.00),
(16, 11, 6, 1, 1590000.00, 20.10, 1270410.00, 1033500.00),
(17, 12, 83, 1, 2290000.00, 20.76, 1814596.00, 1488500.00),
(18, 13, 6, 1, 1590000.00, 20.10, 1270410.00, 1033500.00),
(19, 14, 470, 1, 450000.00, 0.00, 450000.00, 292500.00),
(20, 15, 1, 1, 2890000.00, 0.00, 2890000.00, 1878500.00),
(21, 16, 1, 1, 2890000.00, 0.00, 2890000.00, 1878500.00),
(22, 17, 9, 1, 4290000.00, 21.86, 3352206.00, 2788500.00),
(23, 18, 6, 1, 1590000.00, 20.10, 1270410.00, 1033500.00),
(24, 18, 1, 1, 2890000.00, 0.00, 2890000.00, 1878500.00),
(25, 19, 1, 1, 2890000.00, 0.00, 2890000.00, 1878500.00),
(26, 20, 1, 1, 2890000.00, 0.00, 2890000.00, 1878500.00);
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
(1, 1, 6070000.00, N'MOMO', N'MOMO20250101001', N'success', NULL, '2026-06-10T23:44:29.296'),
(2, 3, 4290000.00, N'Momo', N'MOMO20250115001', N'success', NULL, '2026-06-10T23:44:29.296'),
(3, 4, 1290000.00, N'Tiền mặt', NULL, N'success', NULL, '2026-06-10T23:44:29.296'),
(4, 5, 2780000.00, N'MOMO', N'MOMO20250120001', N'pending', NULL, '2026-06-10T23:44:29.296'),
(5, 8, 1590000.00, N'MOMO', N'DEMO123456', N'SUCCESS', N'Thanh toán MoMo thành công - HD2606111354569125', '2026-06-11T13:55:09.146');
SET IDENTITY_INSERT [dbo].[Lich_su_thanh_toan] OFF;
END
GO

-- ===== Danh_gia =====
IF OBJECT_ID(N'dbo.Danh_gia','U') IS NULL
BEGIN
CREATE TABLE [dbo].[Danh_gia] (
  [id] int IDENTITY(1,1) NOT NULL,
  [id_khach_hang] int NOT NULL,
  [id_san_pham] int NOT NULL,
  [id_hoa_don] int NOT NULL,
  [so_sao] tinyint NOT NULL,
  [noi_dung] nvarchar(max) NULL,
  [anh_danh_gia] nvarchar(max) NULL,
  [trang_thai] tinyint NULL,
  [ngay_tao] datetime2(7) NULL,
  CONSTRAINT [PK_Danh_gia] PRIMARY KEY ([id]),
  CONSTRAINT [UQ_Danh_gia_Khach_Vay_HoaDon] UNIQUE ([id_khach_hang], [id_san_pham], [id_hoa_don]),
  CONSTRAINT [CK_Danh_gia_so_sao] CHECK ([so_sao] BETWEEN 1 AND 5)
);
END
GO
IF NOT EXISTS (SELECT 1 FROM [dbo].[Danh_gia])
BEGIN
SET IDENTITY_INSERT [dbo].[Danh_gia] ON;
INSERT INTO [dbo].[Danh_gia] ([id], [id_khach_hang], [id_san_pham], [id_hoa_don], [so_sao], [noi_dung], [anh_danh_gia], [trang_thai], [ngay_tao]) VALUES
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
  [id_san_pham] int NOT NULL,
  [ngay_tao] datetime2(7) NULL,
  CONSTRAINT [PK_San_pham_yeu_thich] PRIMARY KEY ([id]),
  CONSTRAINT [UQ_San_pham_yeu_thich_Khach_Vay] UNIQUE ([id_khach_hang], [id_san_pham])
);
END
GO

-- ===== Lich_su_xem =====
IF OBJECT_ID(N'dbo.Lich_su_xem','U') IS NULL
BEGIN
CREATE TABLE [dbo].[Lich_su_xem] (
  [id] int IDENTITY(1,1) NOT NULL,
  [id_khach_hang] int NULL,
  [id_san_pham] int NOT NULL,
  [ngay_xem] datetime2(7) NULL,
  CONSTRAINT [PK_Lich_su_xem] PRIMARY KEY ([id]),
  CONSTRAINT [UQ_Lich_su_xem_Khach_Vay] UNIQUE ([id_khach_hang], [id_san_pham])
);
END
GO

-- ===== Huong_dan_kich_thuoc =====
IF OBJECT_ID(N'dbo.Huong_dan_kich_thuoc','U') IS NULL
BEGIN
CREATE TABLE [dbo].[Huong_dan_kich_thuoc] (
  [id] int IDENTITY(1,1) NOT NULL,
  [id_san_pham] int NOT NULL,
  [id_kich_thuoc] int NOT NULL,
  [chieu_cao_tu] int NULL,
  [chieu_cao_den] int NULL,
  [can_nang_tu] int NULL,
  [can_nang_den] int NULL,
  [vong_nguc_tu] int NULL,
  [vong_nguc_den] int NULL,
  [vong_eo_tu] int NULL,
  [vong_eo_den] int NULL,
  [vong_mong_tu] int NULL,
  [vong_mong_den] int NULL,
  [ghi_chu] nvarchar(500) NULL,
  CONSTRAINT [PK_Huong_dan_kich_thuoc] PRIMARY KEY ([id]),
  CONSTRAINT [UQ_Huong_dan_size_Vay_KichThuoc] UNIQUE ([id_san_pham], [id_kich_thuoc]),
  CONSTRAINT [FK_Huong_dan_size_san_pham] FOREIGN KEY ([id_san_pham]) REFERENCES [dbo].[san_pham]([id]),
  CONSTRAINT [FK_Huong_dan_size_KichThuoc] FOREIGN KEY ([id_kich_thuoc]) REFERENCES [dbo].[Kich_Thuoc]([id])
);
END
GO

-- ===== Chinh_sach_cua_hang =====
IF OBJECT_ID(N'dbo.Chinh_sach_cua_hang','U') IS NULL
BEGIN
CREATE TABLE [dbo].[Chinh_sach_cua_hang] (
  [id] int IDENTITY(1,1) NOT NULL,
  [ma_chinh_sach] nvarchar(50) NOT NULL,
  [tieu_de] nvarchar(200) NOT NULL,
  [tom_tat] nvarchar(500) NULL,
  [noi_dung] nvarchar(max) NULL,
  [gia_tri_so] int NULL,
  [don_vi] nvarchar(30) NULL,
  [trang_thai] tinyint NULL DEFAULT 1,
  [thu_tu] int NULL,
  [ngay_cap_nhat] datetime2(7) NULL DEFAULT GETDATE(),
  CONSTRAINT [PK_Chinh_sach_cua_hang] PRIMARY KEY ([id]),
  CONSTRAINT [UQ_Chinh_sach_cua_hang_ma] UNIQUE ([ma_chinh_sach])
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
  [trang_thai] tinyint NULL DEFAULT 1,
  [da_doc] tinyint NULL,
  [gui_email] tinyint NULL DEFAULT 0,
  [da_gui] tinyint NULL DEFAULT 0,
  [ngay_gui] datetime2(7) NULL,
  [ngay_tao] datetime2(7) NULL,
  CONSTRAINT [PK_Thong_bao] PRIMARY KEY ([id])
);
END
GO

IF COL_LENGTH('dbo.Thong_bao', 'trang_thai') IS NULL
    ALTER TABLE [dbo].[Thong_bao] ADD [trang_thai] tinyint NULL DEFAULT 1;
GO

IF COL_LENGTH('dbo.Thong_bao', 'gui_email') IS NULL
    ALTER TABLE [dbo].[Thong_bao] ADD [gui_email] tinyint NULL DEFAULT 0;
GO

IF COL_LENGTH('dbo.Thong_bao', 'da_gui') IS NULL
    ALTER TABLE [dbo].[Thong_bao] ADD [da_gui] tinyint NULL DEFAULT 0;
GO

IF COL_LENGTH('dbo.Thong_bao', 'ngay_gui') IS NULL
    ALTER TABLE [dbo].[Thong_bao] ADD [ngay_gui] datetime2(7) NULL;
GO

-- Read state belongs to each customer, not to the global announcement row.
IF OBJECT_ID(N'dbo.Thong_bao_da_doc', 'U') IS NULL
BEGIN
CREATE TABLE [dbo].[Thong_bao_da_doc] (
  [id] bigint IDENTITY(1,1) NOT NULL,
  [id_khach_hang] int NOT NULL,
  [id_thong_bao] int NOT NULL,
  [ngay_doc] datetime2(7) NOT NULL CONSTRAINT [DF_ThongBaoDaDoc_NgayDoc] DEFAULT SYSDATETIME(),
  CONSTRAINT [PK_Thong_bao_da_doc] PRIMARY KEY ([id]),
  CONSTRAINT [UQ_ThongBaoDaDoc_KhachHang_ThongBao] UNIQUE ([id_khach_hang], [id_thong_bao]),
  CONSTRAINT [FK_ThongBaoDaDoc_KhachHang] FOREIGN KEY ([id_khach_hang]) REFERENCES [dbo].[Khach_hang]([id]) ON DELETE CASCADE,
  CONSTRAINT [FK_ThongBaoDaDoc_ThongBao] FOREIGN KEY ([id_thong_bao]) REFERENCES [dbo].[Thong_bao]([id]) ON DELETE CASCADE
);
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.Thong_bao_da_doc') AND name = N'IX_ThongBaoDaDoc_KhachHang_NgayDoc')
    CREATE INDEX [IX_ThongBaoDaDoc_KhachHang_NgayDoc] ON [dbo].[Thong_bao_da_doc] ([id_khach_hang], [ngay_doc] DESC);
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

-- Tracking columns are safe to run repeatedly.
IF COL_LENGTH('dbo.Hoa_don', 'trang_thai_tracking') IS NULL
    ALTER TABLE [dbo].[Hoa_don] ADD [trang_thai_tracking] nvarchar(50) DEFAULT 'pending';
GO
IF COL_LENGTH('dbo.Hoa_don', 'ngay_giao_hang_du_kien') IS NULL
    ALTER TABLE [dbo].[Hoa_don] ADD [ngay_giao_hang_du_kien] datetime2(7) NULL;
GO
IF COL_LENGTH('dbo.Hoa_don', 'ngay_giao_hang_thuc_te') IS NULL
    ALTER TABLE [dbo].[Hoa_don] ADD [ngay_giao_hang_thuc_te] datetime2(7) NULL;
GO
IF COL_LENGTH('dbo.Hoa_don', 'ma_yeu_cau') IS NULL
    ALTER TABLE [dbo].[Hoa_don] ADD [ma_yeu_cau] nvarchar(100) NULL;
GO
IF COL_LENGTH('dbo.Hoa_don', 'ma_giao_dich_cong') IS NULL
    ALTER TABLE [dbo].[Hoa_don] ADD [ma_giao_dich_cong] nvarchar(120) NULL;
GO
IF COL_LENGTH('dbo.Hoa_don', 'da_hoan_ton_kho') IS NULL
    ALTER TABLE [dbo].[Hoa_don] ADD [da_hoan_ton_kho] bit NOT NULL CONSTRAINT [DF_Hoa_don_da_hoan_ton_kho] DEFAULT 0 WITH VALUES;
GO
IF COL_LENGTH('dbo.Khach_hang', 'google_subject') IS NULL
    ALTER TABLE [dbo].[Khach_hang] ADD [google_subject] nvarchar(100) NULL;
GO
IF COL_LENGTH('dbo.san_pham', 'chieu_cao_nguoi_mau') IS NULL
    ALTER TABLE [dbo].[san_pham] ADD [chieu_cao_nguoi_mau] int NULL;
GO
IF COL_LENGTH('dbo.san_pham', 'can_nang_nguoi_mau') IS NULL
    ALTER TABLE [dbo].[san_pham] ADD [can_nang_nguoi_mau] int NULL;
GO
IF COL_LENGTH('dbo.san_pham', 'size_nguoi_mau') IS NULL
    ALTER TABLE [dbo].[san_pham] ADD [size_nguoi_mau] nvarchar(30) NULL;
GO
IF COL_LENGTH('dbo.san_pham', 'mo_ta_phom') IS NULL
    ALTER TABLE [dbo].[san_pham] ADD [mo_ta_phom] nvarchar(500) NULL;
GO
IF COL_LENGTH('dbo.Danh_gia', 'anh_danh_gia') IS NOT NULL
    ALTER TABLE [dbo].[Danh_gia] ALTER COLUMN [anh_danh_gia] nvarchar(max) NULL;
GO

IF OBJECT_ID(N'dbo.Lich_su_tracking','U') IS NULL
BEGIN
CREATE TABLE [dbo].[Lich_su_tracking] (
  [id] int IDENTITY(1,1) NOT NULL,
  [id_hoa_don] int NOT NULL,
  [trang_thai] nvarchar(50) NOT NULL,
  [mo_ta] nvarchar(max) NULL,
  [ngay_cap_nhat] datetime2(7) NOT NULL DEFAULT GETUTCDATE(),
  CONSTRAINT [PK_Lich_su_tracking] PRIMARY KEY ([id]),
  CONSTRAINT [FK_Lich_su_tracking_Hoa_don] FOREIGN KEY ([id_hoa_don]) REFERENCES [dbo].[Hoa_don]([id])
);
END
GO

IF OBJECT_ID(N'dbo.Hoa_don_audit_log','U') IS NULL
BEGIN
CREATE TABLE [dbo].[Hoa_don_audit_log] (
  [id] int IDENTITY(1,1) NOT NULL,
  [id_hoa_don] int NULL,
  [hanh_dong] nvarchar(80) NULL,
  [trang_thai_cu] tinyint NULL,
  [trang_thai_moi] tinyint NULL,
  [nguoi_thuc_hien] nvarchar(150) NULL,
  [vai_tro] nvarchar(40) NULL,
  [ghi_chu] nvarchar(max) NULL,
  [ngay_tao] datetime2(7) NULL DEFAULT GETDATE(),
  CONSTRAINT [PK_Hoa_don_audit_log] PRIMARY KEY ([id]),
  CONSTRAINT [FK_Hoa_don_audit_log_Hoa_don] FOREIGN KEY ([id_hoa_don]) REFERENCES [dbo].[Hoa_don]([id])
);
END
GO

IF NOT EXISTS (SELECT 1 FROM [dbo].[Thong_bao])
BEGIN
SET IDENTITY_INSERT [dbo].[Thong_bao] ON;
INSERT INTO [dbo].[Thong_bao] ([id], [tieu_de], [noi_dung], [loai], [trang_thai], [ngay_tao]) VALUES
(1, N'Chào mừng khai trương cơ sở mới', N'Chúng tôi hân hạnh thông báo khai trương cơ sở Zestia mới tại 45 Nguyễn Huệ, Quận 1, TP.HCM với nhiều ưu đãi lớn lên đến 30%!', N'HeThong', 1, '2026-06-25T10:00:00'),
(2, N'Ưu đãi tháng 7 - Giảm giá đến 50%', N'Chương trình voucher siêu sale tháng 7 bắt đầu từ ngày 01/07. Áp dụng cho toàn bộ các mẫu váy đầm dạ hội và váy cưới thiết kế mới nhất!', N'Voucher', 1, '2026-07-01T00:00:00'),
(3, N'Bảo trì hệ thống thanh toán ZaloPay', N'Cổng thanh toán ZaloPay sẽ tạm dừng hoạt động bảo trì từ 01:00 đến 03:00 ngày 05/07/2026. Quý khách vui lòng chọn MoMo hoặc COD thay thế.', N'HeThong', 1, '2026-07-01T15:30:00');
SET IDENTITY_INSERT [dbo].[Thong_bao] OFF;
END
GO

-- ============================================================
-- ZESTIA DEMO REFRESH 2026
-- Phan nay chuan hoa du lieu mau, role, voucher, don offline,
-- don thanh toan that bai va bien the san pham theo logic moi.
-- ============================================================
USE fashion_shop;
GO

SET NOCOUNT ON;
GO

/* Zestia demo data refresh - 2026
   Run after backing up the database. The shared demo password is "123456"
   and every password value below is stored as a BCrypt hash.
*/

DECLARE @adminRoleId INT;
DECLARE @staffRoleId INT;

IF NOT EXISTS (SELECT 1 FROM dbo.Vai_tro WHERE ten_vai_tro = N'Admin')
    INSERT INTO dbo.Vai_tro (ten_vai_tro) VALUES (N'Admin');

IF NOT EXISTS (SELECT 1 FROM dbo.Vai_tro WHERE ten_vai_tro = N'Nhân viên')
    INSERT INTO dbo.Vai_tro (ten_vai_tro) VALUES (N'Nhân viên');

SELECT @adminRoleId = MIN(id) FROM dbo.Vai_tro WHERE ten_vai_tro = N'Admin';
SELECT @staffRoleId = MIN(id) FROM dbo.Vai_tro WHERE ten_vai_tro = N'Nhân viên';

-- Move employees from legacy/duplicate roles before deleting those roles.
UPDATE dbo.Nhan_vien
SET id_vai_tro = @adminRoleId
WHERE id_vai_tro IN (SELECT id FROM dbo.Vai_tro WHERE ten_vai_tro = N'Admin')
  AND id_vai_tro <> @adminRoleId;

UPDATE nv
SET id_vai_tro = @staffRoleId
FROM dbo.Nhan_vien nv
JOIN dbo.Vai_tro vt ON vt.id = nv.id_vai_tro
WHERE vt.id <> @staffRoleId
  AND vt.ten_vai_tro IN (N'Nhân viên', N'NhanVien', N'Nhan Vien', N'NhÃ¢n viÃªn');

-- The project has only Admin and Nhân viên. Accounts on obsolete roles are
-- retained as employees so their schedules and audit history are not lost.
UPDATE nv
SET id_vai_tro = @staffRoleId,
    ten_nguoi_dung = CONCAT(N'nhanvien_', nv.id, N'_', RIGHT(REPLACE(CONVERT(NVARCHAR(36), NEWID()), N'-', N''), 8))
FROM dbo.Nhan_vien nv
JOIN dbo.Vai_tro vt ON vt.id = nv.id_vai_tro
WHERE vt.id NOT IN (@adminRoleId, @staffRoleId);

DELETE FROM dbo.Vai_tro
WHERE id <> @staffRoleId
  AND ten_vai_tro IN (N'Nhân viên', N'NhanVien', N'Nhan Vien', N'NhÃ¢n viÃªn');

DELETE FROM dbo.Vai_tro
WHERE id NOT IN (@adminRoleId, @staffRoleId);

DELETE FROM dbo.Vai_tro WHERE id <> @adminRoleId AND ten_vai_tro = N'Admin';

IF NOT EXISTS (SELECT 1 FROM dbo.Nhan_vien WHERE ten_nguoi_dung = N'admin')
BEGIN
    INSERT INTO dbo.Nhan_vien
        (id_vai_tro, ma_nhan_vien, ho_va_ten, gioi_tinh, ngay_sinh, so_dien_thoai, dia_chi, email, ten_nguoi_dung, mat_khau, tinh_trang_lam_viec, ngay_tao)
    VALUES
        (@adminRoleId, N'NV001', N'Admin', 1, '1998-01-10', N'0900000001', N'Hà Nội', N'admin@zestia.vn', N'admin', N'$2a$10$csfRWdR./6P2bikv1yGV5uoJSBEiNQFgrdM9tqkWFv5CDE.BasFY6', 1, '2026-06-10T09:00:00');
END
ELSE
BEGIN
    UPDATE dbo.Nhan_vien
    SET id_vai_tro = @adminRoleId,
        ho_va_ten = N'Admin',
        email = N'admin@zestia.vn',
        tinh_trang_lam_viec = 1
    WHERE ten_nguoi_dung = N'admin';
END

IF EXISTS (SELECT 1 FROM dbo.Nhan_vien WHERE ten_nguoi_dung = N'nv_pos')
BEGIN
    UPDATE dbo.Nhan_vien
    SET id_vai_tro = @staffRoleId,
        ho_va_ten = N'Linh Mai - Nhân viên bán hàng',
        ma_nhan_vien = CASE
            WHEN NOT EXISTS (
                SELECT 1 FROM dbo.Nhan_vien nv_other
                WHERE nv_other.ma_nhan_vien = N'NV004'
                  AND nv_other.ten_nguoi_dung <> N'nv_pos'
            ) THEN N'NV004'
            ELSE ma_nhan_vien
        END,
        email = CASE
            WHEN NOT EXISTS (
                SELECT 1 FROM dbo.Nhan_vien nv_other
                WHERE nv_other.email = N'nv.pos@zestia.vn'
                  AND nv_other.ten_nguoi_dung <> N'nv_pos'
            ) THEN N'nv.pos@zestia.vn'
            ELSE email
        END,
        tinh_trang_lam_viec = 1
    WHERE ten_nguoi_dung = N'nv_pos';
END
ELSE IF EXISTS (SELECT 1 FROM dbo.Nhan_vien WHERE ma_nhan_vien = N'NV004' OR email = N'nv.pos@zestia.vn')
BEGIN
    UPDATE dbo.Nhan_vien
    SET id_vai_tro = @staffRoleId,
        ho_va_ten = N'Linh Mai - Nhân viên bán hàng',
        ma_nhan_vien = N'NV004',
        email = N'nv.pos@zestia.vn',
        ten_nguoi_dung = N'nv_pos',
        mat_khau = COALESCE(mat_khau, N'$2a$10$csfRWdR./6P2bikv1yGV5uoJSBEiNQFgrdM9tqkWFv5CDE.BasFY6'),
        tinh_trang_lam_viec = 1
    WHERE id = (
        SELECT TOP 1 id
        FROM dbo.Nhan_vien
        WHERE ma_nhan_vien = N'NV004' OR email = N'nv.pos@zestia.vn'
        ORDER BY id
    );
END
ELSE
BEGIN
    INSERT INTO dbo.Nhan_vien
        (id_vai_tro, ma_nhan_vien, ho_va_ten, gioi_tinh, ngay_sinh, so_dien_thoai, dia_chi, email, ten_nguoi_dung, mat_khau, tinh_trang_lam_viec, ngay_tao)
    VALUES
        (@staffRoleId, N'NV004', N'Linh Mai - Nhân viên bán hàng', 0, '2001-05-18', N'0900000002', N'Zestia Flagship Store', N'nv.pos@zestia.vn', N'nv_pos', N'$2a$10$csfRWdR./6P2bikv1yGV5uoJSBEiNQFgrdM9tqkWFv5CDE.BasFY6', 1, '2026-08-03T09:00:00');
END

IF NOT EXISTS (SELECT 1 FROM dbo.Khach_hang WHERE email = N'khach.demo@zestia.vn')
BEGIN
    INSERT INTO dbo.Khach_hang
        (ma_khach_hang, ho_va_ten, gioi_tinh, ngay_sinh, so_dien_thoai, email, mat_khau, ngay_tao)
    VALUES
        (N'KHDEMO', N'Minh Anh', 0, '2002-09-20', N'0911111111', N'khach.demo@zestia.vn', N'$2a$10$csfRWdR./6P2bikv1yGV5uoJSBEiNQFgrdM9tqkWFv5CDE.BasFY6', '2026-07-10T09:00:00');
END

DECLARE @demoCustomerId INT = (SELECT TOP 1 id FROM dbo.Khach_hang WHERE email = N'khach.demo@zestia.vn');
IF @demoCustomerId IS NOT NULL
AND NOT EXISTS (SELECT 1 FROM dbo.Dia_chi WHERE id_khach_hang = @demoCustomerId AND mac_dinh = 1)
BEGIN
    INSERT INTO dbo.Dia_chi
        (id_khach_hang, tinh_thanh_pho, quan_huyen, xa_phuong, duong, mac_dinh)
    VALUES
        (@demoCustomerId, N'Thành phố Hà Nội', N'Quận Cầu Giấy', N'Phường Dịch Vọng', N'24 Xuân Thủy', 1);
END

UPDATE dbo.Giam_gia
SET trang_thai = 0
WHERE ngay_ket_thuc < CAST(GETDATE() AS DATE);

MERGE dbo.Giam_gia AS target
USING (VALUES
    (N'ZESTIA10', N'Giảm 10% cho đơn từ 500K', 500000, NULL, 10.00, 100000, 200),
    (N'ZESTIA50', N'Giảm 50K cho đơn từ 800K', 800000, 50000, NULL, NULL, 150),
    (N'FREESHIP', N'Hỗ trợ phí vận chuyển', 300000, 30000, NULL, NULL, 300)
) AS src(ma_giam_gia, ten_giam_gia, gia_tri_don_toi_thieu, gio_tri_giam, phan_tram_giam, giam_toi_da, so_luong)
ON target.ma_giam_gia = src.ma_giam_gia
WHEN MATCHED THEN
    UPDATE SET
        ten_giam_gia = src.ten_giam_gia,
        gia_tri_don_toi_thieu = src.gia_tri_don_toi_thieu,
        gio_tri_giam = src.gio_tri_giam,
        phan_tram_giam = src.phan_tram_giam,
        giam_toi_da = src.giam_toi_da,
        so_luong = src.so_luong,
        ngay_bat_dau = CAST('2026-07-30T00:00:00' AS datetime2),
        ngay_ket_thuc = CAST('2026-08-06T23:59:59' AS datetime2),
        trang_thai = 1
WHEN NOT MATCHED THEN
    INSERT (ma_giam_gia, ten_giam_gia, gia_tri_don_toi_thieu, gio_tri_giam, phan_tram_giam, giam_toi_da, so_luong, ngay_bat_dau, ngay_ket_thuc, trang_thai, ngay_tao)
    VALUES (src.ma_giam_gia, src.ten_giam_gia, src.gia_tri_don_toi_thieu, src.gio_tri_giam, src.phan_tram_giam, src.giam_toi_da, src.so_luong, CAST('2026-07-30T00:00:00' AS datetime2), CAST('2026-08-06T23:59:59' AS datetime2), 1, '2026-07-30T09:00:00');

-- July 2026 Vouchers
MERGE dbo.Giam_gia AS target
USING (VALUES
    (N'JULY15', N'Giảm 15% cho đơn từ 400K', 400000.00, NULL, 15.00, 80000.00, 100, '2026-07-01', '2026-07-30'),
    (N'JULY30', N'Giảm 30K cho đơn từ 600K', 600000.00, 30000.00, NULL, NULL, 150, '2026-07-01', '2026-07-30'),
    (N'JULY50', N'Giảm 50K cho đơn từ 1.2M', 1200000.00, 50000.00, NULL, NULL, 200, '2026-07-01', '2026-07-30'),
    (N'JULYVIP', N'Giảm 15% cho đơn từ 2M', 2000000.00, NULL, 15.00, 400000.00, 50, '2026-07-01', '2026-07-30'),
    (N'LUADO', N'Giảm 100K cho đơn từ 1.5M', 1500000.00, 100000.00, NULL, NULL, 80, '2026-07-01', '2026-07-30'),
    (N'GAMVIP', N'Giảm 300K cho đơn từ 3M', 3000000.00, 300000.00, NULL, NULL, 30, '2026-07-01', '2026-07-30'),
    (N'VOANXINH', N'Giảm 15K cho đơn từ 200K', 200000.00, 15000.00, NULL, NULL, 120, '2026-07-01', '2026-07-30'),
    (N'ZESTIAPOS', N'Giảm 10K cho đơn hàng tại quầy', 0.00, 10000.00, NULL, NULL, 500, '2026-07-01', '2026-07-30'),
    (N'HAPPYWEEK', N'Giảm 10% cho đơn từ 800K', 800000.00, NULL, 10.00, 120000.00, 100, '2026-07-01', '2026-07-30')
) AS src(ma_giam_gia, ten_giam_gia, gia_tri_don_toi_thieu, gio_tri_giam, phan_tram_giam, giam_toi_da, so_luong, ngay_bat_dau, ngay_ket_thuc)
ON target.ma_giam_gia = src.ma_giam_gia
WHEN MATCHED THEN
    UPDATE SET
        ten_giam_gia = src.ten_giam_gia,
        gia_tri_don_toi_thieu = src.gia_tri_don_toi_thieu,
        gio_tri_giam = src.gio_tri_giam,
        phan_tram_giam = src.phan_tram_giam,
        giam_toi_da = src.giam_toi_da,
        so_luong = src.so_luong,
        ngay_bat_dau = src.ngay_bat_dau,
        ngay_ket_thuc = src.ngay_ket_thuc,
        trang_thai = 1
WHEN NOT MATCHED THEN
    INSERT (ma_giam_gia, ten_giam_gia, gia_tri_don_toi_thieu, gio_tri_giam, phan_tram_giam, giam_toi_da, so_luong, ngay_bat_dau, ngay_ket_thuc, trang_thai, ngay_tao)
    VALUES (src.ma_giam_gia, src.ten_giam_gia, src.gia_tri_don_toi_thieu, src.gio_tri_giam, src.phan_tram_giam, src.giam_toi_da, src.so_luong, src.ngay_bat_dau, src.ngay_ket_thuc, 1, '2026-06-25T09:00:00');

-- August 2026 vouchers cover both website checkout and POS demonstrations.
MERGE dbo.Giam_gia AS target
USING (VALUES
    (N'AUGUST10', N'Tháng 8 giảm 10% cho đơn từ 500K', 500000.00, NULL, 10.00, 150000.00, 240, '2026-08-01', '2026-08-06T23:59:59'),
    (N'AUGUST50', N'Tháng 8 giảm 50K cho đơn từ 900K', 900000.00, 50000.00, NULL, NULL, 180, '2026-08-01', '2026-08-06T23:59:59'),
    (N'AUGUSTVIP', N'Khách hàng thân thiết giảm 15%', 1800000.00, NULL, 15.00, 450000.00, 80, '2026-08-01', '2026-08-06T23:59:59'),
    (N'BACK2WORK', N'Ưu đãi váy công sở tháng 8', 700000.00, 80000.00, NULL, NULL, 150, '2026-08-01', '2026-08-06T23:59:59'),
    (N'AUGPOS', N'Ưu đãi 30K khi mua tại quầy', 500000.00, 30000.00, NULL, NULL, 300, '2026-08-01', '2026-08-06T23:59:59'),
    (N'AUGFREESHIP', N'Hỗ trợ 30K phí giao hàng tháng 8', 300000.00, 30000.00, NULL, NULL, 260, '2026-08-01', '2026-08-06T23:59:59')
) AS src(ma_giam_gia, ten_giam_gia, gia_tri_don_toi_thieu, gio_tri_giam, phan_tram_giam, giam_toi_da, so_luong, ngay_bat_dau, ngay_ket_thuc)
ON target.ma_giam_gia = src.ma_giam_gia
WHEN MATCHED THEN
    UPDATE SET
        ten_giam_gia = src.ten_giam_gia,
        gia_tri_don_toi_thieu = src.gia_tri_don_toi_thieu,
        gio_tri_giam = src.gio_tri_giam,
        phan_tram_giam = src.phan_tram_giam,
        giam_toi_da = src.giam_toi_da,
        so_luong = src.so_luong,
        ngay_bat_dau = src.ngay_bat_dau,
        ngay_ket_thuc = src.ngay_ket_thuc,
        trang_thai = 1
WHEN NOT MATCHED THEN
    INSERT (ma_giam_gia, ten_giam_gia, gia_tri_don_toi_thieu, gio_tri_giam, phan_tram_giam, giam_toi_da, so_luong, ngay_bat_dau, ngay_ket_thuc, trang_thai, ngay_tao)
    VALUES (src.ma_giam_gia, src.ten_giam_gia, src.gia_tri_don_toi_thieu, src.gio_tri_giam, src.phan_tram_giam, src.giam_toi_da, src.so_luong, src.ngay_bat_dau, src.ngay_ket_thuc, 1, '2026-07-25T09:00:00');

UPDATE dbo.Giam_gia
SET ngay_ket_thuc = CAST('2026-08-06T23:59:59' AS datetime2)
WHERE ma_giam_gia IN (
    N'ZESTIA10', N'ZESTIA50', N'FREESHIP', N'SUMMER20',
    N'AUGUST10', N'AUGUST50', N'AUGUSTVIP', N'BACK2WORK', N'AUGPOS', N'AUGFREESHIP'
)
  AND ngay_ket_thuc > CAST('2026-08-06T23:59:59' AS datetime2);

-- The application exposes one selling price. The legacy column remains only for
-- compatibility with older databases and always mirrors gia_ban.
UPDATE dbo.san_pham_chi_tiet
SET gia_ban_goc = gia_ban,
    phan_tram_giam = 0
WHERE gia_ban IS NOT NULL;

UPDATE dbo.san_pham_chi_tiet
SET gia_nhap = ROUND(gia_ban * 0.65, 0)
WHERE gia_ban IS NOT NULL
  AND gia_nhap IS NULL;

UPDATE dbo.san_pham_chi_tiet
SET so_luong = 12
WHERE so_luong IS NULL OR so_luong < 0;

UPDATE dbo.san_pham_chi_tiet
SET trang_thai = 1
WHERE trang_thai IS NULL;

UPDATE dbo.san_pham
SET chieu_cao_nguoi_mau = COALESCE(chieu_cao_nguoi_mau, 168 + (id % 6)),
    can_nang_nguoi_mau = COALESCE(can_nang_nguoi_mau, 49 + (id % 7)),
    size_nguoi_mau = COALESCE(size_nguoi_mau, CASE WHEN id % 3 = 0 THEN N'L' WHEN id % 2 = 0 THEN N'M' ELSE N'S' END),
    mo_ta_phom = COALESCE(mo_ta_phom,
        CASE
            WHEN id_loai_san_pham IN (3, 5) THEN N'Phom ôm nhẹ phần thân trên, chân váy có độ rủ. Nếu ở giữa hai size, ưu tiên size lớn hơn.'
            WHEN id_loai_san_pham IN (4, 6) THEN N'Phom vừa, dễ vận động. Chọn theo vòng ngực và vòng eo thực tế.'
            ELSE N'Phom tiêu chuẩn của Zestia. Đối chiếu đủ vòng ngực, eo và mông trước khi chọn.'
        END)
WHERE chieu_cao_nguoi_mau IS NULL
   OR can_nang_nguoi_mau IS NULL
   OR size_nguoi_mau IS NULL
   OR mo_ta_phom IS NULL;

MERGE dbo.Huong_dan_kich_thuoc AS target
USING (
    SELECT DISTINCT vct.id_san_pham, kt.id AS id_kich_thuoc, UPPER(LTRIM(RTRIM(kt.ten_kich_thuoc))) AS size_name
    FROM dbo.san_pham_chi_tiet vct
    JOIN dbo.Kich_Thuoc kt ON kt.id = vct.id_kich_thuoc
    WHERE ISNULL(vct.trang_thai, 1) = 1
) AS src
ON target.id_san_pham = src.id_san_pham AND target.id_kich_thuoc = src.id_kich_thuoc
WHEN MATCHED THEN UPDATE SET
    chieu_cao_tu = CASE src.size_name WHEN N'XS' THEN 148 WHEN N'S' THEN 150 WHEN N'M' THEN 152 WHEN N'L' THEN 155 ELSE 158 END,
    chieu_cao_den = CASE src.size_name WHEN N'XS' THEN 160 WHEN N'S' THEN 163 WHEN N'M' THEN 168 WHEN N'L' THEN 170 WHEN N'XL' THEN 175 ELSE 178 END,
    can_nang_tu = CASE src.size_name WHEN N'XS' THEN 36 WHEN N'S' THEN 40 WHEN N'M' THEN 49 WHEN N'L' THEN 57 WHEN N'XL' THEN 65 ELSE 73 END,
    can_nang_den = CASE src.size_name WHEN N'XS' THEN 43 WHEN N'S' THEN 48 WHEN N'M' THEN 56 WHEN N'L' THEN 64 WHEN N'XL' THEN 72 ELSE 82 END,
    vong_nguc_tu = CASE src.size_name WHEN N'XS' THEN 76 WHEN N'S' THEN 80 WHEN N'M' THEN 84 WHEN N'L' THEN 88 WHEN N'XL' THEN 92 ELSE 98 END,
    vong_nguc_den = CASE src.size_name WHEN N'XS' THEN 80 WHEN N'S' THEN 84 WHEN N'M' THEN 88 WHEN N'L' THEN 92 WHEN N'XL' THEN 98 ELSE 104 END,
    vong_eo_tu = CASE src.size_name WHEN N'XS' THEN 58 WHEN N'S' THEN 62 WHEN N'M' THEN 66 WHEN N'L' THEN 70 WHEN N'XL' THEN 74 ELSE 80 END,
    vong_eo_den = CASE src.size_name WHEN N'XS' THEN 62 WHEN N'S' THEN 66 WHEN N'M' THEN 70 WHEN N'L' THEN 74 WHEN N'XL' THEN 80 ELSE 86 END,
    vong_mong_tu = CASE src.size_name WHEN N'XS' THEN 82 WHEN N'S' THEN 86 WHEN N'M' THEN 90 WHEN N'L' THEN 94 WHEN N'XL' THEN 98 ELSE 104 END,
    vong_mong_den = CASE src.size_name WHEN N'XS' THEN 86 WHEN N'S' THEN 90 WHEN N'M' THEN 94 WHEN N'L' THEN 98 WHEN N'XL' THEN 104 ELSE 110 END,
    ghi_chu = N'Số đo tham khảo theo centimet; ưu tiên vòng lớn nhất khi các số đo thuộc nhiều size.'
WHEN NOT MATCHED THEN INSERT
    (id_san_pham, id_kich_thuoc, chieu_cao_tu, chieu_cao_den, can_nang_tu, can_nang_den,
     vong_nguc_tu, vong_nguc_den, vong_eo_tu, vong_eo_den, vong_mong_tu, vong_mong_den, ghi_chu)
VALUES
    (src.id_san_pham, src.id_kich_thuoc,
     CASE src.size_name WHEN N'XS' THEN 148 WHEN N'S' THEN 150 WHEN N'M' THEN 152 WHEN N'L' THEN 155 ELSE 158 END,
     CASE src.size_name WHEN N'XS' THEN 160 WHEN N'S' THEN 163 WHEN N'M' THEN 168 WHEN N'L' THEN 170 WHEN N'XL' THEN 175 ELSE 178 END,
     CASE src.size_name WHEN N'XS' THEN 36 WHEN N'S' THEN 40 WHEN N'M' THEN 49 WHEN N'L' THEN 57 WHEN N'XL' THEN 65 ELSE 73 END,
     CASE src.size_name WHEN N'XS' THEN 43 WHEN N'S' THEN 48 WHEN N'M' THEN 56 WHEN N'L' THEN 64 WHEN N'XL' THEN 72 ELSE 82 END,
     CASE src.size_name WHEN N'XS' THEN 76 WHEN N'S' THEN 80 WHEN N'M' THEN 84 WHEN N'L' THEN 88 WHEN N'XL' THEN 92 ELSE 98 END,
     CASE src.size_name WHEN N'XS' THEN 80 WHEN N'S' THEN 84 WHEN N'M' THEN 88 WHEN N'L' THEN 92 WHEN N'XL' THEN 98 ELSE 104 END,
     CASE src.size_name WHEN N'XS' THEN 58 WHEN N'S' THEN 62 WHEN N'M' THEN 66 WHEN N'L' THEN 70 WHEN N'XL' THEN 74 ELSE 80 END,
     CASE src.size_name WHEN N'XS' THEN 62 WHEN N'S' THEN 66 WHEN N'M' THEN 70 WHEN N'L' THEN 74 WHEN N'XL' THEN 80 ELSE 86 END,
     CASE src.size_name WHEN N'XS' THEN 82 WHEN N'S' THEN 86 WHEN N'M' THEN 90 WHEN N'L' THEN 94 WHEN N'XL' THEN 98 ELSE 104 END,
     CASE src.size_name WHEN N'XS' THEN 86 WHEN N'S' THEN 90 WHEN N'M' THEN 94 WHEN N'L' THEN 98 WHEN N'XL' THEN 104 ELSE 110 END,
     N'Số đo tham khảo theo centimet; ưu tiên vòng lớn nhất khi các số đo thuộc nhiều size.');

MERGE dbo.Chinh_sach_cua_hang AS target
USING (VALUES
    (N'SHIPPING', N'Chính sách giao hàng', N'Miễn phí cho đơn từ 1.000.000đ; Cầu Giấy miễn phí, nội thành Hà Nội 30.000đ, tỉnh khác 50.000đ.',
     N'Đơn được chuẩn bị sau khi xác nhận thanh toán. Thời gian giao dự kiến từ 2 đến 5 ngày làm việc tùy khu vực. Khách hàng kiểm tra tình trạng kiện hàng trước khi nhận.', 1000000, N'VND', 1),
    (N'SIZE_EXCHANGE', N'Đổi size', N'Gửi yêu cầu đổi size trong 24 giờ kể từ khi nhận hàng.',
     N'Sản phẩm cần còn tem, chưa qua sử dụng, chưa giặt và không có dấu hiệu hư hỏng do người dùng. Zestia hỗ trợ đổi một lần sang size còn tồn kho.', 24, N'giờ', 2),
    (N'RETURN', N'Đổi trả', N'Hỗ trợ yêu cầu đổi/trả trong 30 ngày với sản phẩm đủ điều kiện.',
     N'Không áp dụng cho sản phẩm đã chỉnh sửa theo số đo, sản phẩm mất tem hoặc có dấu hiệu đã sử dụng. Khách hàng cung cấp mã đơn, lý do và ảnh tình trạng sản phẩm.', 30, N'ngày', 3),
    (N'REFUND', N'Hoàn tiền', N'Hoàn tiền trong tối đa 7 ngày làm việc sau khi Zestia nhận và kiểm tra hàng.',
     N'Khoản hoàn được thực hiện theo phương thức đã thanh toán khi cổng hỗ trợ; trường hợp khác, nhân viên liên hệ xác nhận tài khoản nhận hoàn.', 7, N'ngày', 4)
) AS src(ma_chinh_sach, tieu_de, tom_tat, noi_dung, gia_tri_so, don_vi, thu_tu)
ON target.ma_chinh_sach = src.ma_chinh_sach
WHEN MATCHED THEN UPDATE SET
    tieu_de = src.tieu_de, tom_tat = src.tom_tat, noi_dung = src.noi_dung,
    gia_tri_so = src.gia_tri_so, don_vi = src.don_vi, trang_thai = 1,
    thu_tu = src.thu_tu, ngay_cap_nhat = CAST('2026-08-01T09:00:00' AS datetime2)
WHEN NOT MATCHED THEN INSERT
    (ma_chinh_sach, tieu_de, tom_tat, noi_dung, gia_tri_so, don_vi, trang_thai, thu_tu, ngay_cap_nhat)
VALUES
    (src.ma_chinh_sach, src.tieu_de, src.tom_tat, src.noi_dung, src.gia_tri_so, src.don_vi, 1, src.thu_tu, CAST('2026-08-01T09:00:00' AS datetime2));

DECLARE @defaultStaffId INT = (SELECT TOP 1 id FROM dbo.Nhan_vien WHERE ten_nguoi_dung = N'nv_pos');

UPDATE dbo.Hoa_don
SET hinh_thuc_nhan_hang = 0,
    dia_chi_giao_hang = N'Mua trực tiếp tại cửa hàng',
    id_nhan_vien = COALESCE(id_nhan_vien, @defaultStaffId),
    da_thanh_toan = 1,
    ghi_chu = COALESCE(NULLIF(ghi_chu, N''), N'[Tại quầy] Dữ liệu demo đã chuẩn hóa')
WHERE hinh_thuc_nhan_hang = 0
   OR ghi_chu LIKE N'%Tại quầy%'
   OR ghi_chu LIKE N'%tai quay%';

UPDATE dbo.Hoa_don
SET trang_thai = 7,
    da_thanh_toan = 0,
    phuong_thuc_thanh_toan_online = N'FAILED'
WHERE phuong_thuc_thanh_toan_online = N'FAILED'
   OR (trang_thai = 5 AND hinh_thuc_thanh_toan IN (N'MOMO', N'ZALOPAY') AND ISNULL(da_thanh_toan, 0) = 0);

-- Historical demo orders did not reserve stock. Mark terminal/demo rows as
-- reconciled so a later retry cannot add their quantities back a second time.
UPDATE dbo.Hoa_don
SET da_hoan_ton_kho = 1
WHERE ISNULL(da_hoan_ton_kho, 0) = 0
  AND (trang_thai IN (5, 7, 9) OR ma_hoa_don LIKE N'HDS%');

-- Merge duplicate carts left by older application versions.
DECLARE @cart_map TABLE (duplicate_id INT PRIMARY KEY, canonical_id INT NOT NULL);
INSERT INTO @cart_map (duplicate_id, canonical_id)
SELECT id, MIN(id) OVER (PARTITION BY id_khach_hang)
FROM dbo.Gio_hang;

DELETE FROM @cart_map WHERE duplicate_id = canonical_id;

MERGE dbo.Gio_hang_chi_tiet AS target
USING (
    SELECT cm.canonical_id AS id_gio_hang, ghct.id_san_pham_chi_tiet,
           SUM(CASE WHEN ghct.so_luong > 0 THEN ghct.so_luong ELSE 1 END) AS so_luong,
           MIN(ghct.ngay_tao) AS ngay_tao
    FROM dbo.Gio_hang_chi_tiet ghct
    JOIN @cart_map cm ON cm.duplicate_id = ghct.id_gio_hang
    GROUP BY cm.canonical_id, ghct.id_san_pham_chi_tiet
) AS src
ON target.id_gio_hang = src.id_gio_hang
   AND target.id_san_pham_chi_tiet = src.id_san_pham_chi_tiet
WHEN MATCHED THEN UPDATE SET target.so_luong = ISNULL(target.so_luong, 0) + src.so_luong
WHEN NOT MATCHED THEN INSERT (id_gio_hang, id_san_pham_chi_tiet, so_luong, ngay_tao)
VALUES (src.id_gio_hang, src.id_san_pham_chi_tiet, src.so_luong, src.ngay_tao);

DELETE ghct
FROM dbo.Gio_hang_chi_tiet ghct
JOIN @cart_map cm ON cm.duplicate_id = ghct.id_gio_hang;

DELETE gh
FROM dbo.Gio_hang gh
JOIN @cart_map cm ON cm.duplicate_id = gh.id;

;WITH item_totals AS (
    SELECT id_gio_hang, id_san_pham_chi_tiet, MIN(id) AS keeper_id,
           SUM(CASE WHEN so_luong > 0 THEN so_luong ELSE 1 END) AS total_quantity
    FROM dbo.Gio_hang_chi_tiet
    GROUP BY id_gio_hang, id_san_pham_chi_tiet
)
UPDATE keeper
SET so_luong = totals.total_quantity
FROM dbo.Gio_hang_chi_tiet keeper
JOIN item_totals totals ON totals.keeper_id = keeper.id;

;WITH duplicate_items AS (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY id_gio_hang, id_san_pham_chi_tiet ORDER BY id) AS rn
    FROM dbo.Gio_hang_chi_tiet
)
DELETE FROM duplicate_items WHERE rn > 1;

DELETE FROM dbo.Gio_hang_chi_tiet
WHERE so_luong IS NULL OR so_luong <= 0
   OR NOT EXISTS (SELECT 1 FROM dbo.Gio_hang gh WHERE gh.id = id_gio_hang)
   OR NOT EXISTS (SELECT 1 FROM dbo.san_pham_chi_tiet vct WHERE vct.id = id_san_pham_chi_tiet);

;WITH duplicate_wishlist AS (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY id_khach_hang, id_san_pham ORDER BY id) AS rn
    FROM dbo.San_pham_yeu_thich
)
DELETE FROM duplicate_wishlist WHERE rn > 1;

DELETE FROM dbo.San_pham_yeu_thich
WHERE NOT EXISTS (SELECT 1 FROM dbo.Khach_hang kh WHERE kh.id = id_khach_hang)
   OR NOT EXISTS (SELECT 1 FROM dbo.san_pham v WHERE v.id = id_san_pham);

;WITH duplicate_recent AS (
    SELECT id, ROW_NUMBER() OVER (
        PARTITION BY id_khach_hang, id_san_pham
        ORDER BY CASE WHEN ngay_xem IS NULL THEN 1 ELSE 0 END, ngay_xem DESC, id DESC
    ) AS rn
    FROM dbo.Lich_su_xem
    WHERE id_khach_hang IS NOT NULL
)
DELETE FROM duplicate_recent WHERE rn > 1;

DELETE FROM dbo.Lich_su_xem
WHERE id_khach_hang IS NULL
   OR NOT EXISTS (SELECT 1 FROM dbo.Khach_hang kh WHERE kh.id = id_khach_hang)
   OR NOT EXISTS (SELECT 1 FROM dbo.san_pham v WHERE v.id = id_san_pham);

-- Keep gateway and checkout idempotency keys unique without deleting orders.
;WITH duplicate_order_codes AS (
    SELECT id, ma_hoa_don,
           ROW_NUMBER() OVER (PARTITION BY ma_hoa_don ORDER BY id) AS rn
    FROM dbo.Hoa_don
)
UPDATE hd
SET ma_hoa_don = LEFT(doc.ma_hoa_don, 68) + N'-D' + CONVERT(NVARCHAR(10), hd.id)
FROM dbo.Hoa_don hd
JOIN duplicate_order_codes doc ON doc.id = hd.id
WHERE doc.rn > 1;

;WITH duplicate_requests AS (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY ma_yeu_cau ORDER BY id) AS rn
    FROM dbo.Hoa_don
    WHERE ma_yeu_cau IS NOT NULL
)
UPDATE hd SET ma_yeu_cau = NULL
FROM dbo.Hoa_don hd
JOIN duplicate_requests dr ON dr.id = hd.id
WHERE dr.rn > 1;

;WITH duplicate_gateway_ids AS (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY ma_giao_dich_cong ORDER BY id) AS rn
    FROM dbo.Hoa_don
    WHERE ma_giao_dich_cong IS NOT NULL
)
UPDATE hd SET ma_giao_dich_cong = NULL
FROM dbo.Hoa_don hd
JOIN duplicate_gateway_ids dg ON dg.id = hd.id
WHERE dg.rn > 1;

;WITH duplicate_payment_events AS (
    SELECT id, ROW_NUMBER() OVER (
        PARTITION BY UPPER(phuong_thuc), ma_giao_dich, UPPER(trang_thai)
        ORDER BY id
    ) AS rn
    FROM dbo.Lich_su_thanh_toan
    WHERE ma_giao_dich IS NOT NULL
)
DELETE FROM duplicate_payment_events WHERE rn > 1;

IF NOT EXISTS (SELECT 1 FROM dbo.Thong_bao WHERE tieu_de = N'Bộ sưu tập mới đã lên kệ')
BEGIN
    INSERT INTO dbo.Thong_bao (tieu_de, noi_dung, loai, trang_thai, ngay_tao)
    VALUES
        (N'Bộ sưu tập mới đã lên kệ', N'Zestia cập nhật các mẫu váy mới cho mùa lễ hội và sự kiện.', N'HeThong', 1, '2026-08-01T08:00:00'),
        (N'Voucher ZESTIA10 đang hoạt động', N'Khách hàng có thể nhập ZESTIA10 để giảm 10% cho đơn đủ điều kiện.', N'Voucher', 1, '2026-08-02T08:00:00');
END

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_SanPham_TrangThai_NgayTao' AND object_id = OBJECT_ID(N'dbo.san_pham'))
    CREATE INDEX IX_SanPham_TrangThai_NgayTao ON dbo.san_pham (trang_thai, ngay_tao DESC);
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_SanPhamChiTiet_SanPham_TrangThai' AND object_id = OBJECT_ID(N'dbo.san_pham_chi_tiet'))
    CREATE INDEX IX_SanPhamChiTiet_SanPham_TrangThai ON dbo.san_pham_chi_tiet (id_san_pham, trang_thai, id_mau_sac, id_kich_thuoc) INCLUDE (gia_ban, gia_ban_goc, so_luong);
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_HoaDon_NgayTao' AND object_id = OBJECT_ID(N'dbo.Hoa_don'))
    CREATE INDEX IX_HoaDon_NgayTao ON dbo.Hoa_don (ngay_tao DESC) INCLUDE (trang_thai, hinh_thuc_nhan_hang, id_khach_hang, id_nhan_vien, tong_tien, da_thanh_toan);
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_HoaDon_Loai_NgayTao' AND object_id = OBJECT_ID(N'dbo.Hoa_don'))
    CREATE INDEX IX_HoaDon_Loai_NgayTao ON dbo.Hoa_don (hinh_thuc_nhan_hang, ngay_tao DESC) INCLUDE (trang_thai, tong_tien, da_thanh_toan);
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_HoaDonChiTiet_HoaDon' AND object_id = OBJECT_ID(N'dbo.Hoa_don_chi_tiet'))
    CREATE INDEX IX_HoaDonChiTiet_HoaDon ON dbo.Hoa_don_chi_tiet (id_hoa_don) INCLUDE (id_san_pham_chi_tiet, so_luong, don_gia, thanh_tien);
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_HoaDonChiTiet_SanPhamChiTiet' AND object_id = OBJECT_ID(N'dbo.Hoa_don_chi_tiet'))
    CREATE INDEX IX_HoaDonChiTiet_SanPhamChiTiet ON dbo.Hoa_don_chi_tiet (id_san_pham_chi_tiet);
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_GiamGia_Ma_TrangThai' AND object_id = OBJECT_ID(N'dbo.Giam_gia'))
    CREATE INDEX IX_GiamGia_Ma_TrangThai ON dbo.Giam_gia (ma_giam_gia, trang_thai) INCLUDE (ngay_bat_dau, ngay_ket_thuc, so_luong);
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_ThongBao_TrangThai_NgayTao' AND object_id = OBJECT_ID(N'dbo.Thong_bao'))
    CREATE INDEX IX_ThongBao_TrangThai_NgayTao ON dbo.Thong_bao (trang_thai, ngay_tao DESC);
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_HoaDonAuditLog_HoaDon_NgayTao' AND object_id = OBJECT_ID(N'dbo.Hoa_don_audit_log'))
    CREATE INDEX IX_HoaDonAuditLog_HoaDon_NgayTao ON dbo.Hoa_don_audit_log (id_hoa_don, ngay_tao DESC);
GO

-- Uniqueness and lookup indexes used by authentication, idempotent checkout,
-- customer account synchronization and verified reviews.
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.Vai_tro') AND name IN (N'UQ_Vai_tro_ten', N'UX_VaiTro_Ten'))
    CREATE UNIQUE INDEX UX_VaiTro_Ten ON dbo.Vai_tro (ten_vai_tro);
GO

UPDATE dbo.Khach_hang
SET ma_khach_hang = NULLIF(LTRIM(RTRIM(ma_khach_hang)), N''),
    email = NULLIF(LOWER(LTRIM(RTRIM(email))), N''),
    so_dien_thoai = NULLIF(LTRIM(RTRIM(so_dien_thoai)), N''),
    google_subject = NULLIF(LTRIM(RTRIM(google_subject)), N'');
GO

;WITH duplicate_customer_codes AS (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY ma_khach_hang ORDER BY id) AS rn
    FROM dbo.Khach_hang
    WHERE ma_khach_hang IS NOT NULL
)
UPDATE kh SET ma_khach_hang = NULL
FROM dbo.Khach_hang kh
JOIN duplicate_customer_codes d ON d.id = kh.id
WHERE d.rn > 1;
GO

;WITH duplicate_customer_emails AS (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY email ORDER BY id) AS rn
    FROM dbo.Khach_hang
    WHERE email IS NOT NULL
)
UPDATE kh SET email = NULL
FROM dbo.Khach_hang kh
JOIN duplicate_customer_emails d ON d.id = kh.id
WHERE d.rn > 1;
GO

;WITH duplicate_customer_phones AS (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY so_dien_thoai ORDER BY id) AS rn
    FROM dbo.Khach_hang
    WHERE so_dien_thoai IS NOT NULL
)
UPDATE kh SET so_dien_thoai = NULL
FROM dbo.Khach_hang kh
JOIN duplicate_customer_phones d ON d.id = kh.id
WHERE d.rn > 1;
GO

;WITH duplicate_google_subjects AS (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY google_subject ORDER BY id) AS rn
    FROM dbo.Khach_hang
    WHERE google_subject IS NOT NULL
)
UPDATE kh SET google_subject = NULL
FROM dbo.Khach_hang kh
JOIN duplicate_google_subjects d ON d.id = kh.id
WHERE d.rn > 1;
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.Khach_hang') AND name = N'UX_KhachHang_GoogleSubject')
    CREATE UNIQUE INDEX UX_KhachHang_GoogleSubject ON dbo.Khach_hang (google_subject) WHERE google_subject IS NOT NULL;
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.Khach_hang') AND name = N'UX_KhachHang_Ma')
    CREATE UNIQUE INDEX UX_KhachHang_Ma ON dbo.Khach_hang (ma_khach_hang) WHERE ma_khach_hang IS NOT NULL;
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.Khach_hang') AND name = N'UX_KhachHang_Email')
    CREATE UNIQUE INDEX UX_KhachHang_Email ON dbo.Khach_hang (email) WHERE email IS NOT NULL;
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.Khach_hang') AND name = N'UX_KhachHang_SoDienThoai')
    CREATE UNIQUE INDEX UX_KhachHang_SoDienThoai ON dbo.Khach_hang (so_dien_thoai) WHERE so_dien_thoai IS NOT NULL;
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.Hoa_don') AND name IN (N'UQ_Hoa_don_ma', N'UX_HoaDon_Ma'))
    CREATE UNIQUE INDEX UX_HoaDon_Ma ON dbo.Hoa_don (ma_hoa_don);
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.Hoa_don') AND name = N'UX_HoaDon_MaYeuCau')
    CREATE UNIQUE INDEX UX_HoaDon_MaYeuCau ON dbo.Hoa_don (ma_yeu_cau) WHERE ma_yeu_cau IS NOT NULL;
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.Hoa_don') AND name = N'UX_HoaDon_MaGiaoDichCong')
    CREATE UNIQUE INDEX UX_HoaDon_MaGiaoDichCong ON dbo.Hoa_don (ma_giao_dich_cong) WHERE ma_giao_dich_cong IS NOT NULL;
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.Gio_hang') AND name IN (N'UQ_Gio_hang_khach_hang', N'UX_GioHang_KhachHang'))
    CREATE UNIQUE INDEX UX_GioHang_KhachHang ON dbo.Gio_hang (id_khach_hang);
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.Gio_hang_chi_tiet') AND name IN (N'UQ_Gio_hang_chi_tiet_GioHang_BienThe', N'UX_GioHangChiTiet_GioHang_BienThe'))
    CREATE UNIQUE INDEX UX_GioHangChiTiet_GioHang_BienThe ON dbo.Gio_hang_chi_tiet (id_gio_hang, id_san_pham_chi_tiet);
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.San_pham_yeu_thich') AND name IN (N'UQ_San_pham_yeu_thich_Khach_Vay', N'UX_YeuThich_Khach_Vay'))
    CREATE UNIQUE INDEX UX_YeuThich_Khach_Vay ON dbo.San_pham_yeu_thich (id_khach_hang, id_san_pham);
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.Lich_su_xem') AND name IN (N'UQ_Lich_su_xem_Khach_Vay', N'UX_LichSuXem_Khach_Vay'))
    CREATE UNIQUE INDEX UX_LichSuXem_Khach_Vay ON dbo.Lich_su_xem (id_khach_hang, id_san_pham) WHERE id_khach_hang IS NOT NULL;
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.Lich_su_thanh_toan') AND name = N'UX_LichSuThanhToan_GatewayEvent')
    CREATE UNIQUE INDEX UX_LichSuThanhToan_GatewayEvent
    ON dbo.Lich_su_thanh_toan (phuong_thuc, ma_giao_dich, trang_thai)
    WHERE ma_giao_dich IS NOT NULL;
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.Lich_su_thanh_toan') AND name = N'IX_LichSuThanhToan_HoaDon_NgayTao')
    CREATE INDEX IX_LichSuThanhToan_HoaDon_NgayTao ON dbo.Lich_su_thanh_toan (id_hoa_don, ngay_tao DESC);
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.San_pham_yeu_thich') AND name = N'IX_YeuThich_Khach_NgayTao')
    CREATE INDEX IX_YeuThich_Khach_NgayTao ON dbo.San_pham_yeu_thich (id_khach_hang, ngay_tao DESC);
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.Lich_su_xem') AND name = N'IX_LichSuXem_Khach_NgayXem')
    CREATE INDEX IX_LichSuXem_Khach_NgayXem ON dbo.Lich_su_xem (id_khach_hang, ngay_xem DESC);
GO

-- ============================================================
-- GENERATE SAMPLE ORDERS FOR CUSTOMERS (10 to 30 orders per customer)
-- Date range: 01/06/2026 to 12/07/2026
-- ============================================================
-- Normalize the seeded orders and orders created by older versions of the
-- demo generator. These updates are deliberately limited to demo data.
UPDATE hd
SET ten_khach_hang = COALESCE(NULLIF(LTRIM(RTRIM(hd.ten_khach_hang)), N''),
                              NULLIF(LTRIM(RTRIM(kh.ho_va_ten)), N''), N'Khách lẻ'),
    so_dien_thoai = COALESCE(NULLIF(LTRIM(RTRIM(hd.so_dien_thoai)), N''),
                             NULLIF(LTRIM(RTRIM(kh.so_dien_thoai)), N''),
                             N'090000' + RIGHT(N'0000' + CONVERT(NVARCHAR(10), hd.id), 4)),
    email_khach_hang = COALESCE(NULLIF(LTRIM(RTRIM(hd.email_khach_hang)), N''),
                                NULLIF(LTRIM(RTRIM(kh.email)), N''))
FROM dbo.Hoa_don hd
LEFT JOIN dbo.Khach_hang kh ON kh.id = hd.id_khach_hang
WHERE hd.id BETWEEN 1 AND 20
   OR hd.ma_hoa_don LIKE N'HDS%';

UPDATE hd
SET hinh_thuc_nhan_hang = CASE WHEN hd.hinh_thuc_nhan_hang = 2 THEN 0 ELSE hd.hinh_thuc_nhan_hang END,
    dia_chi_giao_hang = CASE WHEN hd.hinh_thuc_nhan_hang IN (0, 2)
                             THEN N'Mua trực tiếp tại cửa hàng'
                             ELSE hd.dia_chi_giao_hang END,
    hinh_thuc_thanh_toan = CASE
        WHEN hd.hinh_thuc_nhan_hang = 2 THEN N'Tiền mặt'
        WHEN UPPER(LTRIM(RTRIM(hd.hinh_thuc_thanh_toan))) = N'MOMO' THEN N'MOMO'
        WHEN UPPER(LTRIM(RTRIM(hd.hinh_thuc_thanh_toan))) = N'ZALOPAY' THEN N'ZALOPAY'
        WHEN UPPER(LTRIM(RTRIM(hd.hinh_thuc_thanh_toan))) = N'COD' THEN N'COD'
        WHEN hd.hinh_thuc_nhan_hang = 0 THEN N'Tiền mặt'
        ELSE hd.hinh_thuc_thanh_toan
    END
FROM dbo.Hoa_don hd
WHERE hd.id BETWEEN 1 AND 20
   OR hd.ma_hoa_don LIKE N'HDS%';

UPDATE hd
SET phuong_thuc_thanh_toan_online = CASE
        WHEN hd.hinh_thuc_thanh_toan IN (N'MOMO', N'ZALOPAY') THEN hd.hinh_thuc_thanh_toan
        ELSE NULL
    END
FROM dbo.Hoa_don hd
WHERE hd.id BETWEEN 1 AND 20
   OR hd.ma_hoa_don LIKE N'HDS%';

-- Current application code stores the actual selling price in don_gia. Older
-- rows stored the pre-discount price and put the actual amount in thanh_tien.
UPDATE ct
SET don_gia = ROUND(ct.thanh_tien / CONVERT(DECIMAL(15,2), ct.so_luong), 2)
FROM dbo.Hoa_don_chi_tiet ct
JOIN dbo.Hoa_don hd ON hd.id = ct.id_hoa_don
WHERE (hd.id BETWEEN 1 AND 20 OR hd.ma_hoa_don LIKE N'HDS%')
  AND ct.so_luong > 0
  AND ct.thanh_tien IS NOT NULL
  AND ABS(ct.thanh_tien - (ct.don_gia * ct.so_luong)) > 0.01;

UPDATE ct
SET thanh_tien = ct.don_gia * ct.so_luong
FROM dbo.Hoa_don_chi_tiet ct
JOIN dbo.Hoa_don hd ON hd.id = ct.id_hoa_don
WHERE (hd.id BETWEEN 1 AND 20 OR hd.ma_hoa_don LIKE N'HDS%')
  AND (ct.thanh_tien IS NULL OR ABS(ct.thanh_tien - (ct.don_gia * ct.so_luong)) > 0.01);

-- Preserve any old order-level discount that was not linked to a voucher.
;WITH order_subtotals AS (
    SELECT ct.id_hoa_don, SUM(ct.thanh_tien) AS subtotal
    FROM dbo.Hoa_don_chi_tiet ct
    GROUP BY ct.id_hoa_don
)
UPDATE hd
SET giam_gia_voucher = os.subtotal + COALESCE(hd.phi_van_chuyen, 0) - hd.tong_tien
FROM dbo.Hoa_don hd
JOIN order_subtotals os ON os.id_hoa_don = hd.id
WHERE (hd.id BETWEEN 1 AND 20 OR hd.ma_hoa_don LIKE N'HDS%')
  AND hd.tong_tien < os.subtotal + COALESCE(hd.phi_van_chuyen, 0) - COALESCE(hd.giam_gia_voucher, 0);

;WITH order_subtotals AS (
    SELECT ct.id_hoa_don, SUM(ct.thanh_tien) AS subtotal
    FROM dbo.Hoa_don_chi_tiet ct
    GROUP BY ct.id_hoa_don
)
UPDATE hd
SET tong_tien = CASE
        WHEN os.subtotal + COALESCE(hd.phi_van_chuyen, 0) - COALESCE(hd.giam_gia_voucher, 0) < 0 THEN 0
        ELSE os.subtotal + COALESCE(hd.phi_van_chuyen, 0) - COALESCE(hd.giam_gia_voucher, 0)
    END
FROM dbo.Hoa_don hd
JOIN order_subtotals os ON os.id_hoa_don = hd.id
WHERE hd.id BETWEEN 1 AND 20
   OR hd.ma_hoa_don LIKE N'HDS%';

UPDATE ls
SET trang_thai = UPPER(LTRIM(RTRIM(ls.trang_thai))),
    phuong_thuc = CASE
        WHEN UPPER(LTRIM(RTRIM(ls.phuong_thuc))) = N'MOMO' THEN N'MOMO'
        WHEN UPPER(LTRIM(RTRIM(ls.phuong_thuc))) = N'ZALOPAY' THEN N'ZALOPAY'
        WHEN UPPER(LTRIM(RTRIM(ls.phuong_thuc))) = N'COD' THEN N'COD'
        ELSE ls.phuong_thuc
    END
FROM dbo.Lich_su_thanh_toan ls
JOIN dbo.Hoa_don hd ON hd.id = ls.id_hoa_don
WHERE hd.id BETWEEN 1 AND 20
   OR hd.ma_hoa_don LIKE N'HDS%';

-- A successful payment record is authoritative for old seeded data.
UPDATE hd
SET da_thanh_toan = 1
FROM dbo.Hoa_don hd
WHERE (hd.id BETWEEN 1 AND 20 OR hd.ma_hoa_don LIKE N'HDS%')
  AND EXISTS (
      SELECT 1
      FROM dbo.Lich_su_thanh_toan ls
      WHERE ls.id_hoa_don = hd.id
        AND UPPER(ls.trang_thai) = N'SUCCESS'
  );

-- POS orders complete immediately. Cancelled demo POS orders remain unpaid.
UPDATE dbo.Hoa_don
SET trang_thai = 4,
    da_thanh_toan = 1,
    tong_tien = CASE
        WHEN tong_tien - COALESCE(phi_van_chuyen, 0) < 0 THEN 0
        ELSE tong_tien - COALESCE(phi_van_chuyen, 0)
    END,
    phi_van_chuyen = 0,
    dia_chi_giao_hang = N'Mua trực tiếp tại cửa hàng',
    id_nhan_vien = COALESCE(id_nhan_vien, (
        SELECT TOP 1 nv.id
        FROM dbo.Nhan_vien nv
        JOIN dbo.Vai_tro vt ON vt.id = nv.id_vai_tro
        WHERE vt.ten_vai_tro = N'Nhân viên'
        ORDER BY nv.id
    ))
WHERE ma_hoa_don LIKE N'HDS%'
  AND hinh_thuc_nhan_hang = 0
  AND trang_thai BETWEEN 0 AND 4;

UPDATE dbo.Hoa_don
SET da_thanh_toan = 0
WHERE ma_hoa_don LIKE N'HDS%'
  AND hinh_thuc_nhan_hang = 0
  AND trang_thai IN (5, 6, 7);

-- COD is paid only after delivery is complete.
UPDATE dbo.Hoa_don
SET da_thanh_toan = CASE WHEN trang_thai = 4 THEN 1 ELSE 0 END
WHERE ma_hoa_don LIKE N'HDS%'
  AND hinh_thuc_nhan_hang = 1
  AND hinh_thuc_thanh_toan = N'COD';

-- Historical gateway orders cannot remain indefinitely unpaid and actionable.
-- Paid gateway orders start at confirmed; unpaid ones become payment failures.
UPDATE dbo.Hoa_don
SET trang_thai = CASE
        WHEN da_thanh_toan = 1 AND trang_thai = 0 THEN 1
        WHEN da_thanh_toan = 0 AND trang_thai BETWEEN 0 AND 4 THEN 7
        ELSE trang_thai
    END,
    da_thanh_toan = CASE WHEN trang_thai IN (5, 6, 7) THEN 0 ELSE da_thanh_toan END,
    phuong_thuc_thanh_toan_online = CASE
        WHEN da_thanh_toan = 0 AND trang_thai BETWEEN 0 AND 4 THEN N'FAILED'
        WHEN trang_thai = 7 THEN N'FAILED'
        ELSE hinh_thuc_thanh_toan
    END
WHERE (id BETWEEN 1 AND 20 OR ma_hoa_don LIKE N'HDS%')
  AND hinh_thuc_nhan_hang = 1
  AND hinh_thuc_thanh_toan IN (N'MOMO', N'ZALOPAY');

-- Remove impossible successes, promote old pending records for paid orders,
-- then fill any missing success/failure history records.
DELETE ls
FROM dbo.Lich_su_thanh_toan ls
JOIN dbo.Hoa_don hd ON hd.id = ls.id_hoa_don
WHERE (hd.id BETWEEN 1 AND 20 OR hd.ma_hoa_don LIKE N'HDS%')
  AND hd.da_thanh_toan = 0
  AND UPPER(ls.trang_thai) = N'SUCCESS';

UPDATE ls
SET trang_thai = N'SUCCESS',
    so_tien = hd.tong_tien,
    noi_dung = COALESCE(NULLIF(ls.noi_dung, N''), N'Thanh toán đơn hàng mẫu thành công')
FROM dbo.Lich_su_thanh_toan ls
JOIN dbo.Hoa_don hd ON hd.id = ls.id_hoa_don
WHERE (hd.id BETWEEN 1 AND 20 OR hd.ma_hoa_don LIKE N'HDS%')
  AND hd.da_thanh_toan = 1
  AND UPPER(ls.trang_thai) = N'PENDING'
  AND NOT EXISTS (
      SELECT 1 FROM dbo.Lich_su_thanh_toan success_row
      WHERE success_row.id_hoa_don = hd.id
        AND UPPER(success_row.trang_thai) = N'SUCCESS'
  );

INSERT INTO dbo.Lich_su_thanh_toan
    (id_hoa_don, so_tien, phuong_thuc, ma_giao_dich, trang_thai, noi_dung, ngay_tao)
SELECT hd.id, hd.tong_tien, hd.hinh_thuc_thanh_toan,
       N'DEMO-SUCCESS-' + CONVERT(NVARCHAR(20), hd.id), N'SUCCESS',
       N'Thanh toán đơn hàng mẫu thành công', COALESCE(hd.ngay_tao, GETDATE())
FROM dbo.Hoa_don hd
WHERE (hd.id BETWEEN 1 AND 20 OR hd.ma_hoa_don LIKE N'HDS%')
  AND hd.da_thanh_toan = 1
  AND NOT EXISTS (
      SELECT 1 FROM dbo.Lich_su_thanh_toan ls
      WHERE ls.id_hoa_don = hd.id
        AND UPPER(ls.trang_thai) = N'SUCCESS'
  );

UPDATE ls
SET so_tien = hd.tong_tien,
    phuong_thuc = hd.hinh_thuc_thanh_toan
FROM dbo.Lich_su_thanh_toan ls
JOIN dbo.Hoa_don hd ON hd.id = ls.id_hoa_don
WHERE (hd.id BETWEEN 1 AND 20 OR hd.ma_hoa_don LIKE N'HDS%')
  AND UPPER(ls.trang_thai) = N'SUCCESS';

INSERT INTO dbo.Lich_su_thanh_toan
    (id_hoa_don, so_tien, phuong_thuc, ma_giao_dich, trang_thai, noi_dung, ngay_tao)
SELECT hd.id, 0, hd.hinh_thuc_thanh_toan,
       N'DEMO-FAILED-' + CONVERT(NVARCHAR(20), hd.id), N'FAILED',
       N'Thanh toán online thất bại - ' + hd.ma_hoa_don, COALESCE(hd.ngay_tao, GETDATE())
FROM dbo.Hoa_don hd
WHERE (hd.id BETWEEN 1 AND 20 OR hd.ma_hoa_don LIKE N'HDS%')
  AND hd.trang_thai = 7
  AND hd.hinh_thuc_thanh_toan IN (N'MOMO', N'ZALOPAY')
  AND NOT EXISTS (
      SELECT 1 FROM dbo.Lich_su_thanh_toan ls
      WHERE ls.id_hoa_don = hd.id
        AND UPPER(ls.trang_thai) = N'FAILED'
  );

INSERT INTO dbo.Lich_su_tracking (id_hoa_don, trang_thai, mo_ta, ngay_cap_nhat)
SELECT hd.id, N'payment_failed',
       N'Thanh toán online thất bại. Đơn hàng không được phép xử lý tiếp.',
       COALESCE(hd.ngay_tao, GETDATE())
FROM dbo.Hoa_don hd
WHERE (hd.id BETWEEN 1 AND 20 OR hd.ma_hoa_don LIKE N'HDS%')
  AND hd.trang_thai = 7
  AND NOT EXISTS (
      SELECT 1 FROM dbo.Lich_su_tracking tracking
      WHERE tracking.id_hoa_don = hd.id
        AND tracking.trang_thai = N'payment_failed'
  );

IF NOT EXISTS (SELECT 1 FROM dbo.Hoa_don WHERE ma_hoa_don LIKE 'HDS%')
BEGIN
    PRINT N'Generating sample orders for customers...';

    -- Declare variables
    DECLARE @customer_id INT;
    DECLARE @order_count INT;
    DECLARE @order_index INT;
    DECLARE @ngay_tao DATETIME2(7);
    DECLARE @days_range INT = DATEDIFF(DAY, '2026-06-01', '2026-07-12');
    DECLARE @random_days INT;
    DECLARE @random_hours INT;
    DECLARE @random_minutes INT;
    DECLARE @random_seconds INT;
    DECLARE @ma_hoa_don NVARCHAR(80);
    DECLARE @tong_tien DECIMAL(15,2);
    DECLARE @phi_van_chuyen DECIMAL(15,2);
    DECLARE @hinh_thuc_nhan_hang TINYINT;
    DECLARE @dia_chi_giao_hang NVARCHAR(500);
    DECLARE @trang_thai TINYINT;
    DECLARE @hinh_thuc_thanh_toan NVARCHAR(50);
    DECLARE @da_thanh_toan BIT;
    DECLARE @ten_khach_hang NVARCHAR(150);
    DECLARE @so_dien_thoai NVARCHAR(20);
    DECLARE @email_khach_hang NVARCHAR(150);
    DECLARE @new_hoa_don_id INT;
    DECLARE @pos_employee_id INT = (
        SELECT TOP 1 nv.id
        FROM dbo.Nhan_vien nv
        JOIN dbo.Vai_tro vt ON vt.id = nv.id_vai_tro
        WHERE vt.ten_vai_tro = N'Nhân viên'
          AND nv.tinh_trang_lam_viec = 1
        ORDER BY CASE WHEN nv.ma_nhan_vien = N'NV002' THEN 0 ELSE 1 END, nv.id
    );

    -- Cursor to loop through all customers
    DECLARE customer_cursor CURSOR FOR 
    SELECT id, ho_va_ten, so_dien_thoai, email FROM dbo.Khach_hang;

    OPEN customer_cursor;
    FETCH NEXT FROM customer_cursor INTO @customer_id, @ten_khach_hang, @so_dien_thoai, @email_khach_hang;

    WHILE @@FETCH_STATUS = 0
    BEGIN
        -- Determine random order count between 10 and 30 for this customer
        -- Seeded by RAND() or using CHECKSUM(NEWID())
        SET @order_count = ABS(CHECKSUM(NEWID())) % 21 + 10;
        SET @order_index = 1;

        WHILE @order_index <= @order_count
        BEGIN
            -- Generate random datetime in date range [2026-06-01, 2026-07-12]
            SET @random_days = ABS(CHECKSUM(NEWID())) % (@days_range + 1);
            SET @random_hours = ABS(CHECKSUM(NEWID())) % 24;
            SET @random_minutes = ABS(CHECKSUM(NEWID())) % 60;
            SET @random_seconds = ABS(CHECKSUM(NEWID())) % 60;
            
            SET @ngay_tao = DATEADD(SECOND, @random_seconds, 
                                DATEADD(MINUTE, @random_minutes, 
                                    DATEADD(HOUR, @random_hours, 
                                        DATEADD(DAY, @random_days, '2026-06-01 00:00:00'))));

            -- Generate unique invoice code starting with HDS
            SET @ma_hoa_don = 'HDS' 
                + REPLACE(CONVERT(VARCHAR(10), @ngay_tao, 120), '-', '') 
                + RIGHT('00' + CAST(@customer_id AS VARCHAR), 2) 
                + RIGHT('00' + CAST(@order_index AS VARCHAR), 2)
                + RIGHT('000' + CAST(ABS(CHECKSUM(NEWID())) % 1000 AS VARCHAR), 3);

            -- Shipping type: 0 (Direct at store), 1 (Delivery)
            DECLARE @rand_receive INT = ABS(CHECKSUM(NEWID())) % 100;
            IF @rand_receive < 30
            BEGIN
                SET @hinh_thuc_nhan_hang = 0;
                SET @dia_chi_giao_hang = N'Mua trực tiếp tại cửa hàng';
                SET @phi_van_chuyen = 0;
            END
            ELSE
            BEGIN
                SET @hinh_thuc_nhan_hang = 1;
                SET @phi_van_chuyen = CASE WHEN ABS(CHECKSUM(NEWID())) % 2 = 0 THEN 30000 ELSE 0 END;
                
                -- Get customer's default address if exists, otherwise generate a random one
                SET @dia_chi_giao_hang = NULL;
                SELECT TOP 1 @dia_chi_giao_hang = CONCAT(duong, N', ', xa_phuong, N', ', quan_huyen, N', ', tinh_thanh_pho)
                FROM dbo.Dia_chi 
                WHERE id_khach_hang = @customer_id AND mac_dinh = 1;

                IF @dia_chi_giao_hang IS NULL
                BEGIN
                    -- Generate realistic Vietnamese address
                    DECLARE @rand_addr INT = ABS(CHECKSUM(NEWID())) % 5;
                    IF @rand_addr = 0
                        SET @dia_chi_giao_hang = N'Số ' + CAST(ABS(CHECKSUM(NEWID())) % 150 + 1 AS VARCHAR) + N' Cầu Giấy, Phường Dịch Vọng, Quận Cầu Giấy, Hà Nội';
                    ELSE IF @rand_addr = 1
                        SET @dia_chi_giao_hang = N'Số ' + CAST(ABS(CHECKSUM(NEWID())) % 200 + 1 AS VARCHAR) + N' Nguyễn Trãi, Phường Thanh Xuân Trung, Quận Thanh Xuân, Hà Nội';
                    ELSE IF @rand_addr = 2
                        SET @dia_chi_giao_hang = N'Hẻm ' + CAST(ABS(CHECKSUM(NEWID())) % 100 + 1 AS VARCHAR) + N' Điện Biên Phủ, Phường 15, Quận Bình Thạnh, TP. Hồ Chí Minh';
                    ELSE IF @rand_addr = 3
                        SET @dia_chi_giao_hang = N'Số ' + CAST(ABS(CHECKSUM(NEWID())) % 80 + 1 AS VARCHAR) + N' Lê Lợi, Phường Bến Nghé, Quận 1, TP. Hồ Chí Minh';
                    ELSE
                        SET @dia_chi_giao_hang = N'12 Bạch Đằng, Phường Thạch Thang, Quận Hải Châu, Đà Nẵng';
                END
            END

            -- Payment method
            IF @hinh_thuc_nhan_hang = 0
                SET @hinh_thuc_thanh_toan = CASE WHEN ABS(CHECKSUM(NEWID())) % 2 = 0 THEN N'Tiền mặt' ELSE N'MOMO' END;
            ELSE
            BEGIN
                DECLARE @rand_payment INT = ABS(CHECKSUM(NEWID())) % 3;
                SET @hinh_thuc_thanh_toan = CASE @rand_payment
                    WHEN 0 THEN N'COD'
                    WHEN 1 THEN N'MOMO'
                    ELSE N'ZALOPAY'
                END;
            END

            -- Keep generated states aligned with the real checkout workflows.
            DECLARE @rand_status INT = ABS(CHECKSUM(NEWID())) % 100;
            IF @hinh_thuc_nhan_hang = 0
            BEGIN
                -- A POS order is completed and paid in the same transaction.
                SET @trang_thai = 4;
                SET @da_thanh_toan = 1;
            END
            ELSE IF @hinh_thuc_thanh_toan = N'COD'
            BEGIN
                IF @rand_status < 15 SET @trang_thai = 0;
                ELSE IF @rand_status < 25 SET @trang_thai = 1;
                ELSE IF @rand_status < 35 SET @trang_thai = 2;
                ELSE IF @rand_status < 45 SET @trang_thai = 3;
                ELSE IF @rand_status < 90 SET @trang_thai = 4;
                ELSE SET @trang_thai = 5;

                SET @da_thanh_toan = CASE WHEN @trang_thai = 4 THEN 1 ELSE 0 END;
            END
            ELSE
            BEGIN
                -- MoMo/ZaloPay orders can only advance after a successful payment.
                IF @rand_status < 10
                BEGIN
                    SET @trang_thai = 7;
                    SET @da_thanh_toan = 0;
                END
                ELSE IF @rand_status < 18
                BEGIN
                    SET @trang_thai = 5;
                    SET @da_thanh_toan = 0;
                END
                ELSE
                BEGIN
                    SET @trang_thai = CASE
                        WHEN @rand_status < 30 THEN 1
                        WHEN @rand_status < 42 THEN 2
                        WHEN @rand_status < 54 THEN 3
                        ELSE 4
                    END;
                    SET @da_thanh_toan = 1;
                END
            END

            -- Insert order
            INSERT INTO dbo.Hoa_don 
                (id_khach_hang, id_giam_gia, id_nhan_vien, ma_hoa_don, tong_tien, phi_van_chuyen, giam_gia_voucher, hinh_thuc_nhan_hang, dia_chi_giao_hang, trang_thai, hinh_thuc_thanh_toan, phuong_thuc_thanh_toan_online, ghi_chu, ngay_tao, da_thanh_toan, ten_khach_hang, so_dien_thoai, email_khach_hang)
            VALUES
                (@customer_id, NULL, CASE WHEN @hinh_thuc_nhan_hang = 0 THEN @pos_employee_id ELSE NULL END, @ma_hoa_don, 0, @phi_van_chuyen, 0, @hinh_thuc_nhan_hang, @dia_chi_giao_hang, @trang_thai, @hinh_thuc_thanh_toan, CASE WHEN @trang_thai = 7 THEN N'FAILED' WHEN @hinh_thuc_thanh_toan IN (N'MOMO', N'ZALOPAY') THEN @hinh_thuc_thanh_toan ELSE NULL END, N'Đơn hàng mẫu sinh tự động', @ngay_tao, @da_thanh_toan, @ten_khach_hang, @so_dien_thoai, @email_khach_hang);

            SET @new_hoa_don_id = SCOPE_IDENTITY();

            -- Insert order details (1 to 3 items)
            DECLARE @item_count INT = ABS(CHECKSUM(NEWID())) % 3 + 1;
            DECLARE @item_index INT = 1;
            DECLARE @variant_id INT;
            DECLARE @gia_ban DECIMAL(15,2);
            DECLARE @gia_nhap DECIMAL(18,2);
            DECLARE @so_luong INT;
            DECLARE @thanh_tien DECIMAL(15,2);
            DECLARE @subtotal DECIMAL(15,2) = 0;

            WHILE @item_index <= @item_count
            BEGIN
                -- Get random variant
                SELECT TOP 1 @variant_id = id, @gia_ban = gia_ban, @gia_nhap = gia_nhap
                FROM dbo.san_pham_chi_tiet
                ORDER BY NEWID();

                SET @so_luong = ABS(CHECKSUM(NEWID())) % 2 + 1; -- 1 or 2
                SET @thanh_tien = @so_luong * @gia_ban;
                SET @subtotal = @subtotal + @thanh_tien;

                INSERT INTO dbo.Hoa_don_chi_tiet
                    (id_hoa_don, id_san_pham_chi_tiet, so_luong, don_gia, phan_tram_giam, thanh_tien, gia_nhap)
                VALUES
                    (@new_hoa_don_id, @variant_id, @so_luong, @gia_ban, 0, @thanh_tien, @gia_nhap);

                SET @item_index = @item_index + 1;
            END

            -- Update order total
            UPDATE dbo.Hoa_don 
            SET tong_tien = @subtotal + @phi_van_chuyen
            WHERE id = @new_hoa_don_id;

            -- If paid, insert payment history
            IF @da_thanh_toan = 1
            BEGIN
                INSERT INTO dbo.Lich_su_thanh_toan
                    (id_hoa_don, so_tien, phuong_thuc, ma_giao_dich, trang_thai, noi_dung, ngay_tao)
                VALUES
                    (@new_hoa_don_id, @subtotal + @phi_van_chuyen, @hinh_thuc_thanh_toan, N'TXN-' + CONVERT(NVARCHAR(36), NEWID()), N'SUCCESS', N'Thanh toán đơn hàng mẫu', @ngay_tao);
            END
            ELSE IF @trang_thai = 7
            BEGIN
                INSERT INTO dbo.Lich_su_thanh_toan
                    (id_hoa_don, so_tien, phuong_thuc, ma_giao_dich, trang_thai, noi_dung, ngay_tao)
                VALUES
                    (@new_hoa_don_id, 0, @hinh_thuc_thanh_toan, N'TXN-' + CONVERT(NVARCHAR(36), NEWID()), N'FAILED', N'Thanh toán online thất bại - ' + @ma_hoa_don, @ngay_tao);

                INSERT INTO dbo.Lich_su_tracking (id_hoa_don, trang_thai, mo_ta, ngay_cap_nhat)
                VALUES (@new_hoa_don_id, N'payment_failed', N'Thanh toán online thất bại. Đơn hàng không được phép xử lý tiếp.', @ngay_tao);
            END

            SET @order_index = @order_index + 1;
        END

        FETCH NEXT FROM customer_cursor INTO @customer_id, @ten_khach_hang, @so_dien_thoai, @email_khach_hang;
    END

    CLOSE customer_cursor;
    DEALLOCATE customer_cursor;

    PRINT N'Sample orders generated successfully.';
END
GO

-- Remove legacy reviews that are not backed by a delivered purchase of the
-- reviewed product. Reviews are then seeded only from real customer/order rows.
DELETE dg
FROM dbo.Danh_gia dg
WHERE dg.id_hoa_don IS NULL
   OR NOT EXISTS (
        SELECT 1
        FROM dbo.Hoa_don hd
        JOIN dbo.Hoa_don_chi_tiet hdct ON hdct.id_hoa_don = hd.id
        JOIN dbo.san_pham_chi_tiet vct ON vct.id = hdct.id_san_pham_chi_tiet
        WHERE hd.id = dg.id_hoa_don
          AND hd.id_khach_hang = dg.id_khach_hang
          AND hd.trang_thai = 4
          AND vct.id_san_pham = dg.id_san_pham
   );

DECLARE @demo_review_texts TABLE (noi_dung NVARCHAR(400) PRIMARY KEY);
INSERT INTO @demo_review_texts (noi_dung) VALUES
    (N'Sản phẩm đúng hình, đường may gọn và tư vấn size phù hợp.'),
    (N'Mình đã mặc đi làm, phom lên đẹp và chất liệu dễ chịu.'),
    (N'Giao hàng cẩn thận, màu thực tế đúng với ảnh trên website.'),
    (N'Váy vừa số đo, phần eo ôm vừa phải và di chuyển thoải mái.'),
    (N'Đóng gói đẹp, sản phẩm không có chỉ thừa và mặc khá tôn dáng.'),
    (N'Mình chọn theo bảng size của Zestia và nhận được size rất vừa.'),
    (N'Chất vải ổn trong tầm giá, mình sẽ tiếp tục mua mẫu khác.'),
    (N'Nhận hàng đúng hẹn, nhân viên hỗ trợ đổi size nhanh và rõ ràng.');

-- Older revisions appended 48 demo reviews on every run. Trim only rows that
-- exactly match the known demo templates; customer-written reviews are kept.
;WITH ranked_demo_reviews AS (
    SELECT dg.id,
           ROW_NUMBER() OVER (ORDER BY dg.ngay_tao DESC, dg.id DESC) AS rn
    FROM dbo.Danh_gia dg
    JOIN @demo_review_texts template ON template.noi_dung = dg.noi_dung
    WHERE dg.anh_danh_gia IS NULL
)
DELETE dg
FROM dbo.Danh_gia dg
JOIN ranked_demo_reviews ranked ON ranked.id = dg.id
WHERE ranked.rn > 48;

DECLARE @demo_review_count INT = (
    SELECT COUNT(*)
    FROM dbo.Danh_gia dg
    JOIN @demo_review_texts template ON template.noi_dung = dg.noi_dung
    WHERE dg.anh_danh_gia IS NULL
);
DECLARE @demo_reviews_needed INT = CASE
    WHEN @demo_review_count < 48 THEN 48 - @demo_review_count
    ELSE 0
END;

;WITH duplicate_reviews AS (
    SELECT id, ROW_NUMBER() OVER (
        PARTITION BY id_khach_hang, id_san_pham, id_hoa_don
        ORDER BY id
    ) AS rn
    FROM dbo.Danh_gia
)
DELETE FROM duplicate_reviews WHERE rn > 1;

;WITH purchased_raw AS (
    SELECT DISTINCT hd.id AS id_hoa_don, hd.id_khach_hang, vct.id_san_pham, hd.ngay_tao
    FROM dbo.Hoa_don hd
    JOIN dbo.Hoa_don_chi_tiet hdct ON hdct.id_hoa_don = hd.id
    JOIN dbo.san_pham_chi_tiet vct ON vct.id = hdct.id_san_pham_chi_tiet
    WHERE hd.id_khach_hang IS NOT NULL
      AND hd.trang_thai = 4
      AND hd.da_thanh_toan = 1
), purchased_products AS (
    SELECT pr.*,
           ROW_NUMBER() OVER (ORDER BY pr.ngay_tao DESC, pr.id_hoa_don DESC, pr.id_san_pham) AS rn
    FROM purchased_raw pr
), review_candidates AS (
    SELECT TOP (@demo_reviews_needed) pp.*
    FROM purchased_products pp
    WHERE NOT EXISTS (
        SELECT 1 FROM dbo.Danh_gia dg
        WHERE dg.id_khach_hang = pp.id_khach_hang
          AND dg.id_san_pham = pp.id_san_pham
          AND dg.id_hoa_don = pp.id_hoa_don
    )
    ORDER BY pp.rn
)
INSERT INTO dbo.Danh_gia
    (id_khach_hang, id_san_pham, id_hoa_don, so_sao, noi_dung, anh_danh_gia, trang_thai, ngay_tao)
SELECT id_khach_hang, id_san_pham, id_hoa_don,
       CASE WHEN rn % 11 = 0 THEN 3 WHEN rn % 4 = 0 THEN 4 ELSE 5 END,
       CASE rn % 8
           WHEN 0 THEN N'Sản phẩm đúng hình, đường may gọn và tư vấn size phù hợp.'
           WHEN 1 THEN N'Mình đã mặc đi làm, phom lên đẹp và chất liệu dễ chịu.'
           WHEN 2 THEN N'Giao hàng cẩn thận, màu thực tế đúng với ảnh trên website.'
           WHEN 3 THEN N'Váy vừa số đo, phần eo ôm vừa phải và di chuyển thoải mái.'
           WHEN 4 THEN N'Đóng gói đẹp, sản phẩm không có chỉ thừa và mặc khá tôn dáng.'
           WHEN 5 THEN N'Mình chọn theo bảng size của Zestia và nhận được size rất vừa.'
           WHEN 6 THEN N'Chất vải ổn trong tầm giá, mình sẽ tiếp tục mua mẫu khác.'
           ELSE N'Nhận hàng đúng hẹn, nhân viên hỗ trợ đổi size nhanh và rõ ràng.'
       END,
       NULL, 1, DATEADD(DAY, 2 + (rn % 9), ngay_tao)
FROM review_candidates;
GO

IF EXISTS (
    SELECT 1 FROM sys.columns
    WHERE object_id = OBJECT_ID(N'dbo.Danh_gia')
      AND name = N'id_hoa_don' AND is_nullable = 1
)
    ALTER TABLE dbo.Danh_gia ALTER COLUMN id_hoa_don INT NOT NULL;
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.Danh_gia') AND name IN (N'UQ_Danh_gia_Khach_Vay_HoaDon', N'UX_DanhGia_Khach_Vay_HoaDon'))
    CREATE UNIQUE INDEX UX_DanhGia_Khach_Vay_HoaDon ON dbo.Danh_gia (id_khach_hang, id_san_pham, id_hoa_don);
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.Danh_gia') AND name = N'IX_DanhGia_Vay_TrangThai_NgayTao')
    CREATE INDEX IX_DanhGia_Vay_TrangThai_NgayTao ON dbo.Danh_gia (id_san_pham, trang_thai, ngay_tao DESC) INCLUDE (so_sao, id_khach_hang, id_hoa_don);
GO

-- ============================================================
-- GENERATE EMPLOYEES AND WORK SHIFTS (06/07/2026 to 31/08/2026)
-- ============================================================
PRINT N'Creating or refreshing demo employees and work schedule...';

DECLARE @demo_staff_role_id INT = (SELECT TOP 1 id FROM dbo.Vai_tro WHERE ten_vai_tro = N'Nhân viên' ORDER BY id);

UPDATE dbo.Nhan_vien
SET id_vai_tro = @demo_staff_role_id
WHERE ma_nhan_vien IN (N'NV003', N'NV007')
  AND id_vai_tro <> @demo_staff_role_id;

MERGE dbo.Nhan_vien AS target
USING (VALUES
    (@demo_staff_role_id, N'NV005', N'Nguyễn Văn Hùng', CAST(1 AS TINYINT), CAST('1999-05-12' AS DATE), N'0988777666', N'Cầu Giấy, Hà Nội', N'hungnv@zestia.vn', N'hungnv'),
    (@demo_staff_role_id, N'NV006', N'Phạm Thanh Hương', CAST(0 AS TINYINT), CAST('2001-08-25' AS DATE), N'0977666555', N'Thanh Xuân, Hà Nội', N'huongnv@zestia.vn', N'huongnv'),
    (@demo_staff_role_id, N'NV007', N'Đỗ Gia Bảo', CAST(1 AS TINYINT), CAST('1995-12-03' AS DATE), N'0966555444', N'Đống Đa, Hà Nội', N'baonv@zestia.vn', N'baonv')
) AS src(id_vai_tro, ma_nhan_vien, ho_va_ten, gioi_tinh, ngay_sinh, so_dien_thoai, dia_chi, email, ten_nguoi_dung)
ON target.ma_nhan_vien = src.ma_nhan_vien
WHEN MATCHED THEN
    UPDATE SET
        id_vai_tro = src.id_vai_tro,
        ho_va_ten = src.ho_va_ten,
        gioi_tinh = src.gioi_tinh,
        ngay_sinh = src.ngay_sinh,
        so_dien_thoai = src.so_dien_thoai,
        dia_chi = src.dia_chi,
        email = src.email,
        ten_nguoi_dung = src.ten_nguoi_dung,
        tinh_trang_lam_viec = 1
WHEN NOT MATCHED THEN
    INSERT (id_vai_tro, ma_nhan_vien, ho_va_ten, gioi_tinh, ngay_sinh, so_dien_thoai, dia_chi, email, ten_nguoi_dung, mat_khau, tinh_trang_lam_viec, ngay_tao)
    VALUES (src.id_vai_tro, src.ma_nhan_vien, src.ho_va_ten, src.gioi_tinh, src.ngay_sinh, src.so_dien_thoai, src.dia_chi, src.email, src.ten_nguoi_dung, N'$2a$10$csfRWdR./6P2bikv1yGV5uoJSBEiNQFgrdM9tqkWFv5CDE.BasFY6', 1, '2026-08-03T09:00:00');

DECLARE @previous_datefirst INT = @@DATEFIRST;
SET DATEFIRST 7;

DECLARE @start_date DATE = '2026-07-06';
DECLARE @end_date DATE = '2026-08-31';
DECLARE @curr_date DATE = @start_date;
DECLARE @admin_id INT = (SELECT id FROM dbo.Nhan_vien WHERE ma_nhan_vien = N'NV001');
DECLARE @emp2 INT = (SELECT id FROM dbo.Nhan_vien WHERE ma_nhan_vien = N'NV002');
DECLARE @emp3 INT = (SELECT id FROM dbo.Nhan_vien WHERE ma_nhan_vien = N'NV003');
DECLARE @emp4 INT = (SELECT id FROM dbo.Nhan_vien WHERE ma_nhan_vien = N'NV004');
DECLARE @emp5 INT = (SELECT id FROM dbo.Nhan_vien WHERE ma_nhan_vien = N'NV005');
DECLARE @emp6 INT = (SELECT id FROM dbo.Nhan_vien WHERE ma_nhan_vien = N'NV006');
DECLARE @emp7 INT = (SELECT id FROM dbo.Nhan_vien WHERE ma_nhan_vien = N'NV007');

-- Remove Friday-night rows produced by the previous DATEFIRST condition.
DELETE FROM dbo.Lich_lam_viec
WHERE ngay_lam BETWEEN @start_date AND @end_date
  AND ca_lam = N'Ca tối'
  AND ghi_chu = N'Bán hàng ca tối cuối tuần'
  AND DATEPART(WEEKDAY, ngay_lam) = 6;

DECLARE @demo_schedule TABLE (
    id_nhan_vien INT NOT NULL,
    ngay_lam DATE NOT NULL,
    ca_lam NVARCHAR(50) NOT NULL,
    gio_bat_dau TIME NOT NULL,
    gio_ket_thuc TIME NOT NULL,
    ghi_chu NVARCHAR(255) NULL,
    trang_thai TINYINT NOT NULL,
    PRIMARY KEY (id_nhan_vien, ngay_lam, ca_lam)
);

WHILE @curr_date <= @end_date
BEGIN
    DECLARE @wday INT = DATEPART(WEEKDAY, @curr_date);
    DECLARE @day_num INT = DATEPART(DAY, @curr_date);

    -- Admin works Monday through Friday.
    IF @wday BETWEEN 2 AND 6 AND @admin_id IS NOT NULL
        INSERT INTO @demo_schedule VALUES (@admin_id, @curr_date, N'Ca sáng', '08:00:00', '12:00:00', N'Trực quản trị cửa hàng', 1);

    DECLARE @inventory_staff INT = CASE WHEN @day_num % 2 = 0 THEN @emp3 ELSE @emp7 END;
    IF @inventory_staff IS NOT NULL
        INSERT INTO @demo_schedule VALUES (@inventory_staff, @curr_date, N'Ca sáng', '08:00:00', '12:00:00', N'Kiểm tra hàng tồn kho và nhập xuất', 1);

    IF @day_num % 2 = 0
    BEGIN
        IF @emp2 IS NOT NULL INSERT INTO @demo_schedule VALUES (@emp2, @curr_date, N'Ca sáng', '08:00:00', '12:00:00', N'Tư vấn trực quầy', 1);
        IF @emp5 IS NOT NULL INSERT INTO @demo_schedule VALUES (@emp5, @curr_date, N'Ca sáng', '08:00:00', '12:00:00', N'Tư vấn trực quầy', 1);
        IF @emp4 IS NOT NULL INSERT INTO @demo_schedule VALUES (@emp4, @curr_date, N'Ca chiều', '13:00:00', '17:00:00', N'Bán hàng tại quầy', 1);
        IF @emp6 IS NOT NULL INSERT INTO @demo_schedule VALUES (@emp6, @curr_date, N'Ca chiều', '13:00:00', '17:00:00', N'Tư vấn trực tuyến', 1);
    END
    ELSE
    BEGIN
        IF @emp4 IS NOT NULL INSERT INTO @demo_schedule VALUES (@emp4, @curr_date, N'Ca sáng', '08:00:00', '12:00:00', N'Tư vấn trực quầy', 1);
        IF @emp6 IS NOT NULL INSERT INTO @demo_schedule VALUES (@emp6, @curr_date, N'Ca sáng', '08:00:00', '12:00:00', N'Tư vấn trực tuyến', 1);
        IF @emp2 IS NOT NULL INSERT INTO @demo_schedule VALUES (@emp2, @curr_date, N'Ca chiều', '13:00:00', '17:00:00', N'Bán hàng tại quầy', 1);
        IF @emp5 IS NOT NULL INSERT INTO @demo_schedule VALUES (@emp5, @curr_date, N'Ca chiều', '13:00:00', '17:00:00', N'Tư vấn trực quầy', 1);
    END

    -- With DATEFIRST 7, Sunday is 1 and Saturday is 7.
    IF @wday IN (1, 7)
    BEGIN
        DECLARE @night_emp1 INT = CASE WHEN @day_num % 3 = 0 THEN @emp2 WHEN @day_num % 3 = 1 THEN @emp4 ELSE @emp5 END;
        DECLARE @night_emp2 INT = CASE WHEN @day_num % 3 = 0 THEN @emp6 WHEN @day_num % 3 = 1 THEN @emp5 ELSE @emp4 END;

        IF @night_emp1 IS NOT NULL INSERT INTO @demo_schedule VALUES (@night_emp1, @curr_date, N'Ca tối', '18:00:00', '22:00:00', N'Bán hàng ca tối cuối tuần', 1);
        IF @night_emp2 IS NOT NULL INSERT INTO @demo_schedule VALUES (@night_emp2, @curr_date, N'Ca tối', '18:00:00', '22:00:00', N'Bán hàng ca tối cuối tuần', 1);
    END

    SET @curr_date = DATEADD(DAY, 1, @curr_date);
END

-- Repair shift names created by an older non-UTF-8 sqlcmd run before merging.
UPDATE dbo.Lich_lam_viec
SET ca_lam = CASE
        WHEN gio_bat_dau = CAST('08:00:00' AS time) AND gio_ket_thuc = CAST('12:00:00' AS time) THEN N'Ca sáng'
        WHEN gio_bat_dau = CAST('13:00:00' AS time) AND gio_ket_thuc = CAST('17:00:00' AS time) THEN N'Ca chiều'
        WHEN gio_bat_dau = CAST('18:00:00' AS time) AND gio_ket_thuc = CAST('22:00:00' AS time) THEN N'Ca tối'
        ELSE ca_lam
    END
WHERE ngay_lam BETWEEN @start_date AND @end_date;

;WITH duplicate_shifts AS (
    SELECT id,
           ROW_NUMBER() OVER (
               PARTITION BY id_nhan_vien, ngay_lam, ca_lam
               ORDER BY
                   CASE WHEN gio_check_in IS NOT NULL THEN 0 ELSE 1 END,
                   CASE WHEN gio_check_out IS NOT NULL THEN 0 ELSE 1 END,
                   id DESC
           ) AS rn
    FROM dbo.Lich_lam_viec
    WHERE ngay_lam BETWEEN @start_date AND @end_date
)
DELETE schedule
FROM dbo.Lich_lam_viec schedule
JOIN duplicate_shifts duplicate_row ON duplicate_row.id = schedule.id
WHERE duplicate_row.rn > 1;

MERGE dbo.Lich_lam_viec AS target
USING @demo_schedule AS src
ON target.id_nhan_vien = src.id_nhan_vien
   AND target.ngay_lam = src.ngay_lam
   AND target.ca_lam = src.ca_lam
WHEN MATCHED THEN
    UPDATE SET
        gio_bat_dau = src.gio_bat_dau,
        gio_ket_thuc = src.gio_ket_thuc,
        ghi_chu = src.ghi_chu,
        trang_thai = src.trang_thai
WHEN NOT MATCHED THEN
    INSERT (id_nhan_vien, ngay_lam, ca_lam, gio_bat_dau, gio_ket_thuc, ghi_chu, trang_thai, ngay_tao)
    VALUES (src.id_nhan_vien, src.ngay_lam, src.ca_lam, src.gio_bat_dau, src.gio_ket_thuc, src.ghi_chu, src.trang_thai, GETDATE());

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE object_id = OBJECT_ID(N'dbo.Lich_lam_viec')
      AND name = N'UX_Lich_lam_viec_nhan_vien_ngay_ca'
)
    CREATE UNIQUE INDEX UX_Lich_lam_viec_nhan_vien_ngay_ca
        ON dbo.Lich_lam_viec(id_nhan_vien, ngay_lam, ca_lam);

SET DATEFIRST @previous_datefirst;
PRINT N'Demo employees and shifts refreshed successfully.';
GO

-- Keep product categories aligned with the product-code contract, including legacy databases.
IF NOT EXISTS (SELECT 1 FROM dbo.loai_san_pham WHERE ten_loai_san_pham = N'Váy dự tiệc')
    INSERT INTO dbo.loai_san_pham (ten_loai_san_pham, trang_thai, mo_ta, ngay_tao)
    VALUES (N'Váy dự tiệc', 1, N'Thiết kế dành cho tiệc tối, sinh nhật và các dịp gặp gỡ.', '2026-08-01T09:00:00');

DECLARE @loai_truyen_thong INT = (SELECT TOP 1 id FROM dbo.loai_san_pham WHERE ten_loai_san_pham = N'Váy truyền thống' ORDER BY id);
DECLARE @loai_cach_tan INT = (SELECT TOP 1 id FROM dbo.loai_san_pham WHERE ten_loai_san_pham = N'Váy cách tân' ORDER BY id);
DECLARE @loai_da_hoi INT = (SELECT TOP 1 id FROM dbo.loai_san_pham WHERE ten_loai_san_pham = N'Váy dạ hội' ORDER BY id);
DECLARE @loai_cuoi INT = (SELECT TOP 1 id FROM dbo.loai_san_pham WHERE ten_loai_san_pham = N'Váy cưới' ORDER BY id);
DECLARE @loai_hoc_sinh INT = (SELECT TOP 1 id FROM dbo.loai_san_pham WHERE ten_loai_san_pham = N'Váy học sinh' ORDER BY id);
DECLARE @loai_cong_so INT = (SELECT TOP 1 id FROM dbo.loai_san_pham WHERE ten_loai_san_pham = N'Váy công sở' ORDER BY id);
DECLARE @loai_du_tiec INT = (SELECT TOP 1 id FROM dbo.loai_san_pham WHERE ten_loai_san_pham = N'Váy dự tiệc' ORDER BY id);

-- Repair a legacy mojibake duplicate created when an older script was executed
-- without UTF-8 input. Repoint every reference before removing the bad row.
UPDATE dbo.san_pham
SET id_loai_san_pham = @loai_du_tiec
WHERE id_loai_san_pham IN (
    SELECT id
    FROM dbo.loai_san_pham
    WHERE id <> @loai_du_tiec
      AND (ten_loai_san_pham LIKE N'%Ã%' OR ten_loai_san_pham LIKE N'%áº%' OR ten_loai_san_pham LIKE N'%á»%')
);

-- This migration also runs on a completely new database. The promotion scope
-- table is created later, so only reference it when upgrading an older schema.
IF OBJECT_ID(N'dbo.Pham_vi_khuyen_mai', N'U') IS NOT NULL
BEGIN
    UPDATE dbo.Pham_vi_khuyen_mai
    SET id_loai_san_pham = @loai_du_tiec
    WHERE id_loai_san_pham IN (
        SELECT id
        FROM dbo.loai_san_pham
        WHERE id <> @loai_du_tiec
          AND (ten_loai_san_pham LIKE N'%Ã%' OR ten_loai_san_pham LIKE N'%áº%' OR ten_loai_san_pham LIKE N'%á»%')
    );

    DELETE dbo.loai_san_pham
    WHERE id <> @loai_du_tiec
      AND (ten_loai_san_pham LIKE N'%Ã%' OR ten_loai_san_pham LIKE N'%áº%' OR ten_loai_san_pham LIKE N'%á»%')
      AND NOT EXISTS (SELECT 1 FROM dbo.san_pham WHERE id_loai_san_pham = dbo.loai_san_pham.id)
      AND NOT EXISTS (SELECT 1 FROM dbo.Pham_vi_khuyen_mai WHERE id_loai_san_pham = dbo.loai_san_pham.id);
END
ELSE
BEGIN
    DELETE dbo.loai_san_pham
    WHERE id <> @loai_du_tiec
      AND (ten_loai_san_pham LIKE N'%Ã%' OR ten_loai_san_pham LIKE N'%áº%' OR ten_loai_san_pham LIKE N'%á»%')
      AND NOT EXISTS (SELECT 1 FROM dbo.san_pham WHERE id_loai_san_pham = dbo.loai_san_pham.id);
END

UPDATE dbo.san_pham
SET id_loai_san_pham = CASE
    WHEN ma_san_pham LIKE N'VTT%' THEN @loai_truyen_thong
    WHEN ma_san_pham LIKE N'VCT%' THEN @loai_cach_tan
    WHEN ma_san_pham LIKE N'VDH%' THEN @loai_da_hoi
    WHEN ma_san_pham LIKE N'VDT%' THEN @loai_du_tiec
    WHEN ma_san_pham LIKE N'VHS%' THEN @loai_hoc_sinh
    WHEN ma_san_pham LIKE N'VCS%' THEN @loai_cong_so
    WHEN ma_san_pham LIKE N'VCU%' THEN @loai_cuoi
    WHEN LEFT(ma_san_pham, 2) = N'VC' THEN @loai_cuoi
    ELSE id_loai_san_pham
END
WHERE ma_san_pham LIKE N'VTT%'
   OR ma_san_pham LIKE N'VCT%'
   OR ma_san_pham LIKE N'VDH%'
   OR ma_san_pham LIKE N'VDT%'
   OR ma_san_pham LIKE N'VHS%'
   OR ma_san_pham LIKE N'VCS%'
   OR ma_san_pham LIKE N'VCU%'
   OR LEFT(ma_san_pham, 2) = N'VC';
GO

-- Normalize legacy state combinations before enforcing the final contract.
UPDATE dbo.Hoa_don
SET trang_thai = 7,
    da_thanh_toan = 0,
    phuong_thuc_thanh_toan_online = N'FAILED',
    da_hoan_ton_kho = 1
WHERE UPPER(ISNULL(hinh_thuc_thanh_toan, N'')) IN (N'MOMO', N'ZALOPAY')
  AND ISNULL(da_thanh_toan, 0) = 0
  AND trang_thai IN (1, 2, 3, 4, 6, 8, 9);

UPDATE dbo.Hoa_don SET trang_thai = 0 WHERE trang_thai IS NULL OR trang_thai NOT BETWEEN 0 AND 9;
UPDATE dbo.Hoa_don SET tong_tien = 0 WHERE tong_tien IS NULL OR tong_tien < 0;
UPDATE dbo.Hoa_don SET da_thanh_toan = 1 WHERE hinh_thuc_nhan_hang = 0 OR (trang_thai = 4 AND UPPER(ISNULL(hinh_thuc_thanh_toan, N'')) = N'COD');
UPDATE dbo.Hoa_don SET da_thanh_toan = 0 WHERE trang_thai = 7;
UPDATE dbo.Danh_gia SET so_sao = CASE WHEN so_sao < 1 THEN 1 WHEN so_sao > 5 THEN 5 ELSE ISNULL(so_sao, 5) END;

-- Legacy demo galleries mixed shirt, pants and accessory placeholders into dress products.
-- Keep the correct dress cover; never remove images uploaded later from the admin UI.
DELETE FROM dbo.Anh
WHERE anh_url LIKE N'/images/products/shirt%.jpg'
   OR anh_url LIKE N'/images/products/pants%.jpg'
   OR anh_url LIKE N'/images/products/accessories%.jpg';

-- Backfill a deterministic color image from each product gallery. Explicit images
-- uploaded from the admin screen are preserved because only NULL rows are updated.
;WITH distinct_colors AS (
    SELECT id_san_pham, id_mau_sac,
           ROW_NUMBER() OVER (PARTITION BY id_san_pham ORDER BY id_mau_sac) AS color_rank
    FROM (SELECT DISTINCT id_san_pham, id_mau_sac FROM dbo.san_pham_chi_tiet WHERE id_mau_sac IS NOT NULL) colors
), product_images AS (
    SELECT id_san_pham, anh_url,
           ROW_NUMBER() OVER (PARTITION BY id_san_pham ORDER BY id) AS image_rank,
           COUNT(*) OVER (PARTITION BY id_san_pham) AS image_count
    FROM dbo.Anh
    WHERE trang_thai = 1 AND anh_url IS NOT NULL
)
UPDATE variant
SET anh_url = image.anh_url
FROM dbo.san_pham_chi_tiet variant
JOIN distinct_colors color
  ON color.id_san_pham = variant.id_san_pham AND color.id_mau_sac = variant.id_mau_sac
JOIN product_images image
  ON image.id_san_pham = color.id_san_pham
 AND image.image_rank = ((color.color_rank - 1) % image.image_count) + 1
WHERE variant.anh_url IS NULL;

DELETE ghct
FROM dbo.Gio_hang_chi_tiet ghct
JOIN dbo.Gio_hang gh ON gh.id = ghct.id_gio_hang
WHERE NOT EXISTS (SELECT 1 FROM dbo.Khach_hang kh WHERE kh.id = gh.id_khach_hang);
DELETE FROM dbo.Gio_hang WHERE NOT EXISTS (SELECT 1 FROM dbo.Khach_hang kh WHERE kh.id = id_khach_hang);
DELETE FROM dbo.Hoa_don_chi_tiet
WHERE NOT EXISTS (SELECT 1 FROM dbo.Hoa_don hd WHERE hd.id = id_hoa_don)
   OR NOT EXISTS (SELECT 1 FROM dbo.san_pham_chi_tiet vct WHERE vct.id = id_san_pham_chi_tiet);
DELETE FROM dbo.Lich_su_thanh_toan WHERE NOT EXISTS (SELECT 1 FROM dbo.Hoa_don hd WHERE hd.id = id_hoa_don);
DELETE FROM dbo.Huong_dan_kich_thuoc
WHERE NOT EXISTS (SELECT 1 FROM dbo.san_pham v WHERE v.id = id_san_pham)
   OR NOT EXISTS (SELECT 1 FROM dbo.Kich_Thuoc kt WHERE kt.id = id_kich_thuoc);
GO

IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE parent_object_id = OBJECT_ID(N'dbo.Hoa_don') AND name = N'CK_Hoa_don_trang_thai')
    ALTER TABLE dbo.Hoa_don WITH CHECK ADD CONSTRAINT CK_Hoa_don_trang_thai CHECK (trang_thai BETWEEN 0 AND 9);
GO
IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE parent_object_id = OBJECT_ID(N'dbo.Hoa_don') AND name = N'CK_Hoa_don_tong_tien')
    ALTER TABLE dbo.Hoa_don WITH CHECK ADD CONSTRAINT CK_Hoa_don_tong_tien CHECK (tong_tien >= 0);
GO
IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE parent_object_id = OBJECT_ID(N'dbo.Gio_hang_chi_tiet') AND name = N'CK_Gio_hang_chi_tiet_so_luong')
    ALTER TABLE dbo.Gio_hang_chi_tiet WITH CHECK ADD CONSTRAINT CK_Gio_hang_chi_tiet_so_luong CHECK (so_luong > 0);
GO
IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE parent_object_id = OBJECT_ID(N'dbo.san_pham_chi_tiet') AND name = N'CK_san_pham_chi_tiet_gia_ton')
    ALTER TABLE dbo.san_pham_chi_tiet WITH CHECK ADD CONSTRAINT CK_san_pham_chi_tiet_gia_ton
        CHECK (gia_ban > 0 AND gia_ban_goc > 0 AND (gia_nhap IS NULL OR gia_nhap >= 0) AND ISNULL(so_luong, 0) >= 0);
GO
IF EXISTS (SELECT ma_san_pham FROM dbo.san_pham GROUP BY ma_san_pham HAVING COUNT(*) > 1)
    THROW 51019, N'Mã sản phẩm đang bị trùng, không thể tạo ràng buộc duy nhất.', 1;
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.san_pham') AND name = N'UX_Vay_MaVay')
    CREATE UNIQUE INDEX UX_Vay_MaVay ON dbo.san_pham(ma_san_pham);
GO
IF EXISTS (SELECT ma_san_pham_chi_tiet FROM dbo.san_pham_chi_tiet WHERE ma_san_pham_chi_tiet IS NOT NULL GROUP BY ma_san_pham_chi_tiet HAVING COUNT(*) > 1)
    THROW 51020, N'Mã biến thể đang bị trùng, không thể tạo ràng buộc duy nhất.', 1;
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.san_pham_chi_tiet') AND name = N'UX_SanPhamChiTiet_Ma')
    CREATE UNIQUE INDEX UX_SanPhamChiTiet_Ma ON dbo.san_pham_chi_tiet(ma_san_pham_chi_tiet) WHERE ma_san_pham_chi_tiet IS NOT NULL;
GO
IF EXISTS (
    SELECT id_san_pham, id_mau_sac, id_kich_thuoc
    FROM dbo.san_pham_chi_tiet
    WHERE id_mau_sac IS NOT NULL AND id_kich_thuoc IS NOT NULL
    GROUP BY id_san_pham, id_mau_sac, id_kich_thuoc
    HAVING COUNT(*) > 1
)
    THROW 51021, N'Một sản phẩm đang có biến thể trùng màu và kích thước.', 1;
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.san_pham_chi_tiet') AND name = N'UX_SanPhamChiTiet_Vay_Mau_Size')
    CREATE UNIQUE INDEX UX_SanPhamChiTiet_Vay_Mau_Size
        ON dbo.san_pham_chi_tiet(id_san_pham, id_mau_sac, id_kich_thuoc)
        WHERE id_mau_sac IS NOT NULL AND id_kich_thuoc IS NOT NULL;
GO
IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE parent_object_id = OBJECT_ID(N'dbo.Hoa_don_chi_tiet') AND name = N'CK_Hoa_don_chi_tiet_gia_ton')
    ALTER TABLE dbo.Hoa_don_chi_tiet WITH CHECK ADD CONSTRAINT CK_Hoa_don_chi_tiet_gia_ton
        CHECK (so_luong > 0 AND don_gia > 0 AND (gia_nhap IS NULL OR gia_nhap >= 0));
GO
IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE parent_object_id = OBJECT_ID(N'dbo.Nhan_vien') AND name = N'CK_Nhan_vien_gioi_tinh')
    ALTER TABLE dbo.Nhan_vien WITH CHECK ADD CONSTRAINT CK_Nhan_vien_gioi_tinh CHECK (gioi_tinh IS NULL OR gioi_tinh IN (0, 1));
GO
IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE parent_object_id = OBJECT_ID(N'dbo.Nhan_vien') AND name = N'CK_Nhan_vien_tinh_trang')
    ALTER TABLE dbo.Nhan_vien WITH CHECK ADD CONSTRAINT CK_Nhan_vien_tinh_trang CHECK (tinh_trang_lam_viec IS NULL OR tinh_trang_lam_viec IN (0, 1));
GO
IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE parent_object_id = OBJECT_ID(N'dbo.Danh_gia') AND name = N'CK_Danh_gia_so_sao')
    ALTER TABLE dbo.Danh_gia WITH CHECK ADD CONSTRAINT CK_Danh_gia_so_sao CHECK (so_sao BETWEEN 1 AND 5);
GO

IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE parent_object_id = OBJECT_ID(N'dbo.Hoa_don_chi_tiet') AND name = N'FK_HoaDonChiTiet_HoaDon')
    ALTER TABLE dbo.Hoa_don_chi_tiet WITH CHECK ADD CONSTRAINT FK_HoaDonChiTiet_HoaDon FOREIGN KEY (id_hoa_don) REFERENCES dbo.Hoa_don(id);
GO
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE parent_object_id = OBJECT_ID(N'dbo.san_pham_chi_tiet') AND name = N'FK_SanPhamChiTiet_Vay')
    ALTER TABLE dbo.san_pham_chi_tiet WITH CHECK ADD CONSTRAINT FK_SanPhamChiTiet_Vay FOREIGN KEY (id_san_pham) REFERENCES dbo.san_pham(id);
GO
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE parent_object_id = OBJECT_ID(N'dbo.san_pham_chi_tiet') AND name = N'FK_SanPhamChiTiet_MauSac')
    ALTER TABLE dbo.san_pham_chi_tiet WITH CHECK ADD CONSTRAINT FK_SanPhamChiTiet_MauSac FOREIGN KEY (id_mau_sac) REFERENCES dbo.Mau_Sac(id);
GO
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE parent_object_id = OBJECT_ID(N'dbo.san_pham_chi_tiet') AND name = N'FK_SanPhamChiTiet_KichThuoc')
    ALTER TABLE dbo.san_pham_chi_tiet WITH CHECK ADD CONSTRAINT FK_SanPhamChiTiet_KichThuoc FOREIGN KEY (id_kich_thuoc) REFERENCES dbo.Kich_Thuoc(id);
GO
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE parent_object_id = OBJECT_ID(N'dbo.san_pham') AND name = N'FK_Vay_LoaiVay')
    ALTER TABLE dbo.san_pham WITH CHECK ADD CONSTRAINT FK_Vay_LoaiVay FOREIGN KEY (id_loai_san_pham) REFERENCES dbo.loai_san_pham(id);
GO
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE parent_object_id = OBJECT_ID(N'dbo.san_pham') AND name = N'FK_Vay_ChatLieu')
    ALTER TABLE dbo.san_pham WITH CHECK ADD CONSTRAINT FK_Vay_ChatLieu FOREIGN KEY (id_chat_lieu) REFERENCES dbo.Chat_lieu(id);
GO
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE parent_object_id = OBJECT_ID(N'dbo.san_pham') AND name = N'FK_Vay_NhaCungCap')
    ALTER TABLE dbo.san_pham WITH CHECK ADD CONSTRAINT FK_Vay_NhaCungCap FOREIGN KEY (id_nha_cung_cap) REFERENCES dbo.Nha_cung_cap(id);
GO
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE parent_object_id = OBJECT_ID(N'dbo.Hoa_don') AND name = N'FK_HoaDon_KhachHang')
    ALTER TABLE dbo.Hoa_don WITH CHECK ADD CONSTRAINT FK_HoaDon_KhachHang FOREIGN KEY (id_khach_hang) REFERENCES dbo.Khach_hang(id);
GO
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE parent_object_id = OBJECT_ID(N'dbo.Hoa_don') AND name = N'FK_HoaDon_NhanVien')
    ALTER TABLE dbo.Hoa_don WITH CHECK ADD CONSTRAINT FK_HoaDon_NhanVien FOREIGN KEY (id_nhan_vien) REFERENCES dbo.Nhan_vien(id);
GO
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE parent_object_id = OBJECT_ID(N'dbo.Hoa_don') AND name = N'FK_HoaDon_GiamGia')
    ALTER TABLE dbo.Hoa_don WITH CHECK ADD CONSTRAINT FK_HoaDon_GiamGia FOREIGN KEY (id_giam_gia) REFERENCES dbo.Giam_gia(id);
GO
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE parent_object_id = OBJECT_ID(N'dbo.Dia_chi') AND name = N'FK_DiaChi_KhachHang')
    ALTER TABLE dbo.Dia_chi WITH CHECK ADD CONSTRAINT FK_DiaChi_KhachHang FOREIGN KEY (id_khach_hang) REFERENCES dbo.Khach_hang(id);
GO
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE parent_object_id = OBJECT_ID(N'dbo.Hoa_don_chi_tiet') AND name = N'FK_HoaDonChiTiet_SanPhamChiTiet')
    ALTER TABLE dbo.Hoa_don_chi_tiet WITH CHECK ADD CONSTRAINT FK_HoaDonChiTiet_SanPhamChiTiet FOREIGN KEY (id_san_pham_chi_tiet) REFERENCES dbo.san_pham_chi_tiet(id);
GO
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE parent_object_id = OBJECT_ID(N'dbo.Lich_su_thanh_toan') AND name = N'FK_LichSuThanhToan_HoaDon')
    ALTER TABLE dbo.Lich_su_thanh_toan WITH CHECK ADD CONSTRAINT FK_LichSuThanhToan_HoaDon FOREIGN KEY (id_hoa_don) REFERENCES dbo.Hoa_don(id);
GO
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE parent_object_id = OBJECT_ID(N'dbo.Danh_gia') AND name = N'FK_DanhGia_KhachHang')
    ALTER TABLE dbo.Danh_gia WITH CHECK ADD CONSTRAINT FK_DanhGia_KhachHang FOREIGN KEY (id_khach_hang) REFERENCES dbo.Khach_hang(id);
GO
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE parent_object_id = OBJECT_ID(N'dbo.Danh_gia') AND name = N'FK_DanhGia_Vay')
    ALTER TABLE dbo.Danh_gia WITH CHECK ADD CONSTRAINT FK_DanhGia_Vay FOREIGN KEY (id_san_pham) REFERENCES dbo.san_pham(id);
GO
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE parent_object_id = OBJECT_ID(N'dbo.Danh_gia') AND name = N'FK_DanhGia_HoaDon')
    ALTER TABLE dbo.Danh_gia WITH CHECK ADD CONSTRAINT FK_DanhGia_HoaDon FOREIGN KEY (id_hoa_don) REFERENCES dbo.Hoa_don(id);
GO
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE parent_object_id = OBJECT_ID(N'dbo.San_pham_yeu_thich') AND name = N'FK_YeuThich_KhachHang')
    ALTER TABLE dbo.San_pham_yeu_thich WITH CHECK ADD CONSTRAINT FK_YeuThich_KhachHang FOREIGN KEY (id_khach_hang) REFERENCES dbo.Khach_hang(id);
GO
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE parent_object_id = OBJECT_ID(N'dbo.San_pham_yeu_thich') AND name = N'FK_YeuThich_Vay')
    ALTER TABLE dbo.San_pham_yeu_thich WITH CHECK ADD CONSTRAINT FK_YeuThich_Vay FOREIGN KEY (id_san_pham) REFERENCES dbo.san_pham(id);
GO
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE parent_object_id = OBJECT_ID(N'dbo.Lich_su_xem') AND name = N'FK_LichSuXem_KhachHang')
    ALTER TABLE dbo.Lich_su_xem WITH CHECK ADD CONSTRAINT FK_LichSuXem_KhachHang FOREIGN KEY (id_khach_hang) REFERENCES dbo.Khach_hang(id);
GO
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE parent_object_id = OBJECT_ID(N'dbo.Lich_su_xem') AND name = N'FK_LichSuXem_Vay')
    ALTER TABLE dbo.Lich_su_xem WITH CHECK ADD CONSTRAINT FK_LichSuXem_Vay FOREIGN KEY (id_san_pham) REFERENCES dbo.san_pham(id);
GO

-- Reconcile real delivery-failed orders created before automatic restoration was added.
-- Demo HDS rows were never deducted and were marked reconciled earlier in this script.
IF OBJECT_ID(N'dbo.Ho_tro_chat', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.Ho_tro_chat (
        id int IDENTITY(1,1) NOT NULL CONSTRAINT PK_Ho_tro_chat PRIMARY KEY,
        ma_phien nvarchar(64) NOT NULL,
        id_khach_hang int NULL,
        id_nhan_vien int NULL,
        tieu_de nvarchar(255) NULL,
        trang_thai nvarchar(20) NOT NULL,
        ngay_tao datetime2(7) NOT NULL,
        ngay_nhan datetime2(7) NULL,
        ngay_dong datetime2(7) NULL,
        ngay_cap_nhat datetime2(7) NOT NULL,
        CONSTRAINT UQ_Ho_tro_chat_ma_phien UNIQUE (ma_phien),
        CONSTRAINT FK_Ho_tro_chat_Khach_hang FOREIGN KEY (id_khach_hang) REFERENCES dbo.Khach_hang(id),
        CONSTRAINT FK_Ho_tro_chat_Nhan_vien FOREIGN KEY (id_nhan_vien) REFERENCES dbo.Nhan_vien(id),
        CONSTRAINT CK_Ho_tro_chat_trang_thai CHECK (trang_thai IN (N'WAITING', N'ACTIVE', N'CLOSED'))
    );
END
GO

IF OBJECT_ID(N'dbo.Tin_nhan_ho_tro', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.Tin_nhan_ho_tro (
        id int IDENTITY(1,1) NOT NULL CONSTRAINT PK_Tin_nhan_ho_tro PRIMARY KEY,
        id_ho_tro_chat int NOT NULL,
        loai_nguoi_gui nvarchar(20) NOT NULL,
        ten_nguoi_gui nvarchar(150) NULL,
        noi_dung nvarchar(1000) NOT NULL,
        ngay_tao datetime2(7) NOT NULL,
        CONSTRAINT FK_Tin_nhan_ho_tro_Ho_tro_chat FOREIGN KEY (id_ho_tro_chat) REFERENCES dbo.Ho_tro_chat(id),
        CONSTRAINT CK_Tin_nhan_ho_tro_nguoi_gui CHECK (loai_nguoi_gui IN (N'CUSTOMER', N'EMPLOYEE', N'SYSTEM'))
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.Ho_tro_chat') AND name = N'IX_Ho_tro_chat_trang_thai_cap_nhat')
    CREATE INDEX IX_Ho_tro_chat_trang_thai_cap_nhat ON dbo.Ho_tro_chat(trang_thai, ngay_cap_nhat DESC);
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.Tin_nhan_ho_tro') AND name = N'IX_Tin_nhan_ho_tro_phien')
    CREATE INDEX IX_Tin_nhan_ho_tro_phien ON dbo.Tin_nhan_ho_tro(id_ho_tro_chat, id);
GO

;WITH stock_to_restore AS (
    SELECT hdct.id_san_pham_chi_tiet, SUM(ISNULL(hdct.so_luong, 0)) AS restore_quantity
    FROM dbo.Hoa_don hd
    JOIN dbo.Hoa_don_chi_tiet hdct ON hdct.id_hoa_don = hd.id
    WHERE hd.trang_thai = 6
      AND ISNULL(hd.da_hoan_ton_kho, 0) = 0
      AND hd.ma_hoa_don NOT LIKE N'HDS%'
    GROUP BY hdct.id_san_pham_chi_tiet
)
UPDATE variant
SET variant.so_luong = ISNULL(variant.so_luong, 0) + source.restore_quantity
FROM dbo.san_pham_chi_tiet variant
JOIN stock_to_restore source ON source.id_san_pham_chi_tiet = variant.id;
GO

;WITH voucher_to_restore AS (
    SELECT hd.id_giam_gia, COUNT(*) AS restore_quantity
    FROM dbo.Hoa_don hd
    WHERE hd.trang_thai = 6
      AND ISNULL(hd.da_hoan_ton_kho, 0) = 0
      AND hd.ma_hoa_don NOT LIKE N'HDS%'
      AND hd.id_giam_gia IS NOT NULL
    GROUP BY hd.id_giam_gia
)
UPDATE voucher
SET voucher.so_luong = voucher.so_luong + source.restore_quantity
FROM dbo.Giam_gia voucher
JOIN voucher_to_restore source ON source.id_giam_gia = voucher.id
WHERE voucher.so_luong IS NOT NULL;
GO

UPDATE dbo.Hoa_don
SET da_hoan_ton_kho = 1
WHERE trang_thai = 6
  AND ISNULL(da_hoan_ton_kho, 0) = 0;
GO

-- Historical seed orders are reporting fixtures and never reserve live stock.
UPDATE dbo.Hoa_don
SET da_hoan_ton_kho = 1
WHERE (id BETWEEN 1 AND 20 OR ma_hoa_don LIKE N'HDS%')
  AND trang_thai IN (5, 6, 7, 9)
  AND ISNULL(da_hoan_ton_kho, 0) = 0;
GO

UPDATE dbo.Lich_lam_viec
SET trang_thai = 0
WHERE trang_thai IS NULL OR trang_thai NOT BETWEEN 0 AND 3;
GO

IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = N'CK_Lich_lam_viec_trang_thai')
    ALTER TABLE dbo.Lich_lam_viec WITH CHECK ADD CONSTRAINT CK_Lich_lam_viec_trang_thai CHECK (trang_thai BETWEEN 0 AND 3);
GO
IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = N'CK_Lich_lam_viec_cham_cong')
    ALTER TABLE dbo.Lich_lam_viec WITH CHECK ADD CONSTRAINT CK_Lich_lam_viec_cham_cong CHECK (gio_check_out IS NULL OR gio_check_in IS NULL OR gio_check_out >= gio_check_in);
GO
IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = N'CK_Lich_lam_viec_thoi_gian')
    ALTER TABLE dbo.Lich_lam_viec WITH CHECK ADD CONSTRAINT CK_Lich_lam_viec_thoi_gian CHECK (gio_ket_thuc > gio_bat_dau);
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.Lich_lam_viec') AND name = N'IX_Lich_lam_viec_nhan_vien_ngay')
    CREATE INDEX IX_Lich_lam_viec_nhan_vien_ngay
        ON dbo.Lich_lam_viec(id_nhan_vien, ngay_lam, trang_thai, gio_bat_dau, gio_ket_thuc);
GO

-- Promotion campaigns. A scope row is an AND combination of its selected
-- product/category/color/size; multiple rows in one campaign are OR alternatives.
IF OBJECT_ID(N'dbo.Dot_khuyen_mai', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.Dot_khuyen_mai (
        id int IDENTITY(1,1) NOT NULL CONSTRAINT PK_Dot_khuyen_mai PRIMARY KEY,
        ma_dot nvarchar(50) NOT NULL,
        ten_dot nvarchar(150) NOT NULL,
        loai_giam nvarchar(20) NOT NULL,
        gia_tri_giam decimal(15,2) NOT NULL,
        ngay_bat_dau datetime2(7) NOT NULL,
        ngay_ket_thuc datetime2(7) NOT NULL,
        trang_thai tinyint NOT NULL CONSTRAINT DF_Dot_khuyen_mai_trang_thai DEFAULT 1,
        do_uu_tien int NOT NULL CONSTRAINT DF_Dot_khuyen_mai_do_uu_tien DEFAULT 0,
        ngay_tao datetime2(7) NOT NULL CONSTRAINT DF_Dot_khuyen_mai_ngay_tao DEFAULT SYSDATETIME(),
        CONSTRAINT UQ_Dot_khuyen_mai_ma UNIQUE (ma_dot),
        CONSTRAINT CK_Dot_khuyen_mai_loai CHECK (loai_giam IN (N'PERCENT', N'FIXED')),
        CONSTRAINT CK_Dot_khuyen_mai_gia_tri CHECK (gia_tri_giam > 0 AND (loai_giam <> N'PERCENT' OR gia_tri_giam <= 100)),
        CONSTRAINT CK_Dot_khuyen_mai_thoi_gian CHECK (ngay_ket_thuc > ngay_bat_dau),
        CONSTRAINT CK_Dot_khuyen_mai_trang_thai CHECK (trang_thai IN (0, 1))
    );
END
GO

IF OBJECT_ID(N'dbo.Pham_vi_khuyen_mai', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.Pham_vi_khuyen_mai (
        id int IDENTITY(1,1) NOT NULL CONSTRAINT PK_Pham_vi_khuyen_mai PRIMARY KEY,
        id_dot_khuyen_mai int NOT NULL,
        id_san_pham int NULL,
        id_loai_san_pham int NULL,
        id_mau_sac int NULL,
        id_kich_thuoc int NULL,
        CONSTRAINT FK_PhamViKM_Dot FOREIGN KEY (id_dot_khuyen_mai) REFERENCES dbo.Dot_khuyen_mai(id),
        CONSTRAINT FK_PhamViKM_Vay FOREIGN KEY (id_san_pham) REFERENCES dbo.san_pham(id),
        CONSTRAINT FK_PhamViKM_LoaiVay FOREIGN KEY (id_loai_san_pham) REFERENCES dbo.loai_san_pham(id),
        CONSTRAINT FK_PhamViKM_MauSac FOREIGN KEY (id_mau_sac) REFERENCES dbo.Mau_Sac(id),
        CONSTRAINT FK_PhamViKM_KichThuoc FOREIGN KEY (id_kich_thuoc) REFERENCES dbo.Kich_Thuoc(id)
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.Dot_khuyen_mai') AND name = N'IX_DotKM_Active_Time')
    CREATE INDEX IX_DotKM_Active_Time ON dbo.Dot_khuyen_mai(trang_thai, ngay_bat_dau, ngay_ket_thuc, do_uu_tien DESC);
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.Pham_vi_khuyen_mai') AND name = N'IX_PhamViKM_Dot')
    CREATE INDEX IX_PhamViKM_Dot ON dbo.Pham_vi_khuyen_mai(id_dot_khuyen_mai) INCLUDE (id_san_pham, id_loai_san_pham, id_mau_sac, id_kich_thuoc);
GO

MERGE dbo.Dot_khuyen_mai AS target
USING (VALUES
    (N'WELCOME0726', N'Ưu đãi tháng 7', N'PERCENT', CAST(10 AS decimal(15,2)), CAST('2026-07-01T00:00:00' AS datetime2), CAST('2026-07-31T23:59:59' AS datetime2), 1, 10),
    (N'AUGUST26', N'Chào tháng 8', N'PERCENT', CAST(8 AS decimal(15,2)), CAST('2026-08-01T00:00:00' AS datetime2), CAST('2026-08-06T23:59:59' AS datetime2), 1, 20),
    (N'AUGOFFICE15', N'Tháng 8 thanh lịch nơi công sở', N'PERCENT', CAST(15 AS decimal(15,2)), CAST('2026-08-03T00:00:00' AS datetime2), CAST('2026-08-06T23:59:59' AS datetime2), 1, 30)
) AS source(ma_dot, ten_dot, loai_giam, gia_tri_giam, ngay_bat_dau, ngay_ket_thuc, trang_thai, do_uu_tien)
ON target.ma_dot = source.ma_dot
WHEN MATCHED THEN UPDATE SET ten_dot = source.ten_dot, loai_giam = source.loai_giam,
    gia_tri_giam = source.gia_tri_giam, ngay_bat_dau = source.ngay_bat_dau,
    ngay_ket_thuc = source.ngay_ket_thuc, trang_thai = source.trang_thai, do_uu_tien = source.do_uu_tien
WHEN NOT MATCHED THEN INSERT (ma_dot, ten_dot, loai_giam, gia_tri_giam, ngay_bat_dau, ngay_ket_thuc, trang_thai, do_uu_tien, ngay_tao)
    VALUES (source.ma_dot, source.ten_dot, source.loai_giam, source.gia_tri_giam, source.ngay_bat_dau, source.ngay_ket_thuc, source.trang_thai, source.do_uu_tien, CAST('2026-07-25T09:00:00' AS datetime2));
GO

-- The AUGPARTY120 campaign starts after the 06/08/2026 demo cutoff, so it is
-- intentionally omitted. Remove it as well when upgrading a database that ran
-- an older version of this seed file.
DELETE scope_row
FROM dbo.Pham_vi_khuyen_mai scope_row
JOIN dbo.Dot_khuyen_mai campaign ON campaign.id = scope_row.id_dot_khuyen_mai
WHERE campaign.ma_dot = N'AUGPARTY120';
DELETE FROM dbo.Dot_khuyen_mai WHERE ma_dot = N'AUGPARTY120';
GO
IF NOT EXISTS (
    SELECT 1 FROM dbo.Pham_vi_khuyen_mai p
    JOIN dbo.Dot_khuyen_mai d ON d.id = p.id_dot_khuyen_mai
    WHERE d.ma_dot = N'WELCOME0726'
)
    INSERT INTO dbo.Pham_vi_khuyen_mai(id_dot_khuyen_mai)
    SELECT id FROM dbo.Dot_khuyen_mai WHERE ma_dot = N'WELCOME0726';
GO
IF NOT EXISTS (
    SELECT 1 FROM dbo.Pham_vi_khuyen_mai p
    JOIN dbo.Dot_khuyen_mai d ON d.id = p.id_dot_khuyen_mai
    WHERE d.ma_dot = N'AUGUST26'
)
    INSERT INTO dbo.Pham_vi_khuyen_mai(id_dot_khuyen_mai)
    SELECT id FROM dbo.Dot_khuyen_mai WHERE ma_dot = N'AUGUST26';
GO
IF NOT EXISTS (
    SELECT 1 FROM dbo.Pham_vi_khuyen_mai p
    JOIN dbo.Dot_khuyen_mai d ON d.id = p.id_dot_khuyen_mai
    WHERE d.ma_dot = N'AUGOFFICE15'
)
    INSERT INTO dbo.Pham_vi_khuyen_mai(id_dot_khuyen_mai, id_loai_san_pham)
    SELECT d.id, lv.id
    FROM dbo.Dot_khuyen_mai d
    CROSS JOIN dbo.loai_san_pham lv
    WHERE d.ma_dot = N'AUGOFFICE15' AND lv.ten_loai_san_pham = N'Váy công sở';
GO
-- Return/exchange requests are line-item based, preventing one request from
-- accidentally changing the status or inventory of an entire mixed order.
IF OBJECT_ID(N'dbo.Yeu_cau_doi_tra', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.Yeu_cau_doi_tra (
        id int IDENTITY(1,1) NOT NULL CONSTRAINT PK_Yeu_cau_doi_tra PRIMARY KEY,
        id_hoa_don int NOT NULL,
        id_hoa_don_chi_tiet int NOT NULL,
        id_bien_the_doi int NULL,
        id_khach_hang int NOT NULL,
        id_nhan_vien_xu_ly int NULL,
        loai_yeu_cau nvarchar(10) NOT NULL,
        nguon nvarchar(10) NOT NULL,
        trang_thai nvarchar(30) NOT NULL,
        so_luong int NOT NULL,
        ly_do nvarchar(1000) NOT NULL,
        tinh_trang_hang nvarchar(2000) NULL,
        thong_tin_hoan_tien nvarchar(500) NULL,
        so_tien_hoan decimal(18,2) NULL,
        ma_giao_dich_hoan nvarchar(150) NULL,
        phan_hoi_cong nvarchar(2000) NULL,
        ngay_yeu_cau_hoan datetime2(7) NULL,
        ly_do_tu_choi nvarchar(1000) NULL,
        ghi_chu_nhan_vien nvarchar(1000) NULL,
        da_hoan_ton_kho bit NOT NULL CONSTRAINT DF_Yeu_cau_doi_tra_hoan_ton DEFAULT 0,
        ngay_tao datetime2(7) NOT NULL CONSTRAINT DF_Yeu_cau_doi_tra_ngay_tao DEFAULT SYSDATETIME(),
        ngay_duyet datetime2(7) NULL,
        ngay_nhan_hang datetime2(7) NULL,
        ngay_hoan_tat datetime2(7) NULL,
        CONSTRAINT FK_YeuCauDoiTra_HoaDon FOREIGN KEY (id_hoa_don) REFERENCES dbo.Hoa_don(id),
        CONSTRAINT FK_YeuCauDoiTra_ChiTiet FOREIGN KEY (id_hoa_don_chi_tiet) REFERENCES dbo.Hoa_don_chi_tiet(id),
        CONSTRAINT FK_YeuCauDoiTra_BienThe FOREIGN KEY (id_bien_the_doi) REFERENCES dbo.san_pham_chi_tiet(id),
        CONSTRAINT FK_YeuCauDoiTra_KhachHang FOREIGN KEY (id_khach_hang) REFERENCES dbo.Khach_hang(id),
        CONSTRAINT FK_YeuCauDoiTra_NhanVien FOREIGN KEY (id_nhan_vien_xu_ly) REFERENCES dbo.Nhan_vien(id),
        CONSTRAINT CK_YeuCauDoiTra_Loai CHECK (loai_yeu_cau IN (N'DOI', N'TRA')),
        CONSTRAINT CK_YeuCauDoiTra_Nguon CHECK (nguon IN (N'ONLINE', N'OFFLINE')),
        CONSTRAINT CK_YeuCauDoiTra_TrangThai CHECK (trang_thai IN (N'CHO_DUYET', N'CHO_NHAN_HANG', N'CHO_HOAN_TAT', N'CHO_XAC_NHAN_HOAN_TIEN', N'TU_CHOI', N'TRA_LAI_KHACH', N'DA_DOI', N'DA_HOAN_TIEN')),
        CONSTRAINT CK_YeuCauDoiTra_SoLuong CHECK (so_luong > 0)
    );
END
GO
IF COL_LENGTH('dbo.Yeu_cau_doi_tra', 'so_tien_hoan') IS NULL
    ALTER TABLE dbo.Yeu_cau_doi_tra ADD so_tien_hoan decimal(18,2) NULL;
IF COL_LENGTH('dbo.Yeu_cau_doi_tra', 'ma_giao_dich_hoan') IS NULL
    ALTER TABLE dbo.Yeu_cau_doi_tra ADD ma_giao_dich_hoan nvarchar(150) NULL;
IF COL_LENGTH('dbo.Yeu_cau_doi_tra', 'phan_hoi_cong') IS NULL
    ALTER TABLE dbo.Yeu_cau_doi_tra ADD phan_hoi_cong nvarchar(2000) NULL;
IF COL_LENGTH('dbo.Yeu_cau_doi_tra', 'ngay_yeu_cau_hoan') IS NULL
    ALTER TABLE dbo.Yeu_cau_doi_tra ADD ngay_yeu_cau_hoan datetime2(7) NULL;
GO
IF EXISTS (
    SELECT 1 FROM sys.check_constraints
    WHERE parent_object_id = OBJECT_ID(N'dbo.Yeu_cau_doi_tra')
      AND name = N'CK_YeuCauDoiTra_TrangThai'
)
    ALTER TABLE dbo.Yeu_cau_doi_tra DROP CONSTRAINT CK_YeuCauDoiTra_TrangThai;
ALTER TABLE dbo.Yeu_cau_doi_tra WITH CHECK ADD CONSTRAINT CK_YeuCauDoiTra_TrangThai
    CHECK (trang_thai IN (N'CHO_DUYET', N'CHO_NHAN_HANG', N'CHO_HOAN_TAT',
                         N'CHO_XAC_NHAN_HOAN_TIEN', N'TU_CHOI', N'TRA_LAI_KHACH',
                         N'DA_DOI', N'DA_HOAN_TIEN'));
GO

IF OBJECT_ID(N'dbo.Anh_doi_tra', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.Anh_doi_tra (
        id int IDENTITY(1,1) NOT NULL CONSTRAINT PK_Anh_doi_tra PRIMARY KEY,
        id_yeu_cau int NOT NULL,
        anh_url nvarchar(500) NOT NULL,
        ngay_tao datetime2(7) NOT NULL CONSTRAINT DF_Anh_doi_tra_ngay_tao DEFAULT SYSDATETIME(),
        CONSTRAINT FK_AnhDoiTra_YeuCau FOREIGN KEY (id_yeu_cau) REFERENCES dbo.Yeu_cau_doi_tra(id)
    );
END
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.Yeu_cau_doi_tra') AND name = N'UX_YeuCauDoiTra_ChiTiet')
    CREATE UNIQUE INDEX UX_YeuCauDoiTra_ChiTiet ON dbo.Yeu_cau_doi_tra(id_hoa_don_chi_tiet);
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.Yeu_cau_doi_tra') AND name = N'IX_YeuCauDoiTra_TrangThai_NgayTao')
    CREATE INDEX IX_YeuCauDoiTra_TrangThai_NgayTao ON dbo.Yeu_cau_doi_tra(loai_yeu_cau, trang_thai, ngay_tao DESC) INCLUDE (id_hoa_don, id_khach_hang);
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.Anh_doi_tra') AND name = N'IX_AnhDoiTra_YeuCau')
    CREATE INDEX IX_AnhDoiTra_YeuCau ON dbo.Anh_doi_tra(id_yeu_cau, id);
GO

IF OBJECT_ID(N'dbo.Bien_dong_ton_kho', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.Bien_dong_ton_kho (
        id bigint IDENTITY(1,1) NOT NULL CONSTRAINT PK_Bien_dong_ton_kho PRIMARY KEY,
        id_san_pham_chi_tiet int NOT NULL,
        so_luong_truoc int NOT NULL,
        so_luong_thay_doi int NOT NULL,
        so_luong_sau int NOT NULL,
        loai_bien_dong nvarchar(40) NOT NULL,
        ma_tham_chieu nvarchar(100) NULL,
        nguoi_thuc_hien nvarchar(150) NULL,
        ghi_chu nvarchar(500) NULL,
        ngay_tao datetime2(7) NOT NULL CONSTRAINT DF_Bien_dong_ton_kho_ngay_tao DEFAULT SYSDATETIME(),
        CONSTRAINT FK_BienDongTonKho_SanPhamChiTiet FOREIGN KEY (id_san_pham_chi_tiet) REFERENCES dbo.san_pham_chi_tiet(id),
        CONSTRAINT CK_BienDongTonKho_SoLuong CHECK (
            so_luong_truoc >= 0 AND so_luong_sau >= 0
            AND so_luong_sau = so_luong_truoc + so_luong_thay_doi
        )
    );
END
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.Bien_dong_ton_kho') AND name = N'IX_BienDongTonKho_BienThe_NgayTao')
    CREATE INDEX IX_BienDongTonKho_BienThe_NgayTao
        ON dbo.Bien_dong_ton_kho(id_san_pham_chi_tiet, ngay_tao DESC);
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.Bien_dong_ton_kho') AND name = N'IX_BienDongTonKho_ThamChieu')
    CREATE INDEX IX_BienDongTonKho_ThamChieu
        ON dbo.Bien_dong_ton_kho(ma_tham_chieu) WHERE ma_tham_chieu IS NOT NULL;
GO

-- POS inventory and voucher reservations. Stock is deducted when an item is
-- added and restored when the session is cleared or expires.
IF OBJECT_ID(N'dbo.Pos_phien_giu_hang', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.Pos_phien_giu_hang (
        id int IDENTITY(1,1) NOT NULL CONSTRAINT PK_Pos_phien_giu_hang PRIMARY KEY,
        ma_phien nvarchar(64) NOT NULL,
        id_nhan_vien int NOT NULL,
        id_giam_gia int NULL,
        id_hoa_don int NULL,
        trang_thai nvarchar(20) NOT NULL,
        ngay_tao datetime2(7) NOT NULL,
        cap_nhat_luc datetime2(7) NOT NULL,
        het_han_luc datetime2(7) NOT NULL,
        CONSTRAINT UQ_Pos_phien_ma UNIQUE (ma_phien),
        CONSTRAINT FK_PosPhien_NhanVien FOREIGN KEY (id_nhan_vien) REFERENCES dbo.Nhan_vien(id),
        CONSTRAINT FK_PosPhien_GiamGia FOREIGN KEY (id_giam_gia) REFERENCES dbo.Giam_gia(id),
        CONSTRAINT FK_PosPhien_HoaDon FOREIGN KEY (id_hoa_don) REFERENCES dbo.Hoa_don(id),
        CONSTRAINT CK_PosPhien_TrangThai CHECK (trang_thai IN (N'ACTIVE', N'COMPLETED', N'RELEASED', N'EXPIRED')),
        CONSTRAINT CK_PosPhien_ThoiGian CHECK (het_han_luc >= ngay_tao)
    );
END
GO

IF OBJECT_ID(N'dbo.Pos_chi_tiet_giu_hang', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.Pos_chi_tiet_giu_hang (
        id int IDENTITY(1,1) NOT NULL CONSTRAINT PK_Pos_chi_tiet_giu_hang PRIMARY KEY,
        id_phien int NOT NULL,
        id_san_pham_chi_tiet int NOT NULL,
        so_luong int NOT NULL,
        don_gia decimal(18,2) NOT NULL,
        ngay_tao datetime2(7) NOT NULL,
        cap_nhat_luc datetime2(7) NOT NULL,
        CONSTRAINT UQ_Pos_chi_tiet_phien_bien_the UNIQUE (id_phien, id_san_pham_chi_tiet),
        CONSTRAINT FK_PosChiTiet_Phien FOREIGN KEY (id_phien) REFERENCES dbo.Pos_phien_giu_hang(id),
        CONSTRAINT FK_PosChiTiet_BienThe FOREIGN KEY (id_san_pham_chi_tiet) REFERENCES dbo.san_pham_chi_tiet(id),
        CONSTRAINT CK_PosChiTiet_SoLuong CHECK (so_luong > 0 AND so_luong <= 100),
        CONSTRAINT CK_PosChiTiet_DonGia CHECK (don_gia >= 0)
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.Pos_phien_giu_hang') AND name = N'UX_Pos_phien_hoa_don')
    CREATE UNIQUE INDEX UX_Pos_phien_hoa_don
        ON dbo.Pos_phien_giu_hang(id_hoa_don) WHERE id_hoa_don IS NOT NULL;
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.Pos_phien_giu_hang') AND name = N'IX_Pos_phien_het_han')
    CREATE INDEX IX_Pos_phien_het_han
        ON dbo.Pos_phien_giu_hang(trang_thai, het_han_luc)
        INCLUDE (id_nhan_vien, id_giam_gia);
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.Pos_chi_tiet_giu_hang') AND name = N'IX_Pos_chi_tiet_phien')
    CREATE INDEX IX_Pos_chi_tiet_phien
        ON dbo.Pos_chi_tiet_giu_hang(id_phien, id);
GO

-- Connect legacy guest/POS orders to the canonical customer profile. Customer
-- phone/email are already normalized and uniquely indexed earlier in this script.
UPDATE hd
SET id_khach_hang = kh.id
FROM dbo.Hoa_don hd
JOIN dbo.Khach_hang kh ON kh.so_dien_thoai = NULLIF(LTRIM(RTRIM(hd.so_dien_thoai)), N'')
WHERE hd.id_khach_hang IS NULL AND kh.so_dien_thoai IS NOT NULL;
GO
UPDATE hd
SET id_khach_hang = kh.id
FROM dbo.Hoa_don hd
JOIN dbo.Khach_hang kh ON kh.email = LOWER(NULLIF(LTRIM(RTRIM(hd.email_khach_hang)), N''))
WHERE hd.id_khach_hang IS NULL AND kh.email IS NOT NULL;
GO

-- Demonstrate that one customer can keep several structured addresses.
INSERT INTO dbo.Dia_chi(id_khach_hang, tinh_thanh_pho, quan_huyen, xa_phuong, duong, mac_dinh)
SELECT TOP (5) kh.id, N'Hà Nội', N'Quận Cầu Giấy', N'Phường Dịch Vọng',
       N'Địa chỉ phụ ' + kh.ma_khach_hang, 0
FROM dbo.Khach_hang kh
WHERE kh.ma_khach_hang IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM dbo.Dia_chi dc
      WHERE dc.id_khach_hang = kh.id AND dc.duong = N'Địa chỉ phụ ' + kh.ma_khach_hang
  )
ORDER BY kh.id;
GO

-- ============================================================
-- COMPLETE AUGUST 2026 DEMO DATA THROUGH 06/08/2026
-- 124 orders distributed across the first six days of August, with matching
-- payment/tracking/audit rows, verified reviews,
-- customer activity, support conversations and return requests.
-- All demo keys are deterministic so this block is repeat-safe.
-- ============================================================
DECLARE @aug_customers TABLE (
    rn int NOT NULL PRIMARY KEY,
    id int NOT NULL,
    ho_va_ten nvarchar(150) NOT NULL,
    so_dien_thoai nvarchar(20) NULL,
    email nvarchar(150) NULL
);
INSERT INTO @aug_customers(rn, id, ho_va_ten, so_dien_thoai, email)
SELECT ROW_NUMBER() OVER (ORDER BY id), id, ho_va_ten, so_dien_thoai, email
FROM dbo.Khach_hang
WHERE so_dien_thoai IS NOT NULL;

DECLARE @aug_employees TABLE (
    rn int NOT NULL PRIMARY KEY,
    id int NOT NULL,
    ho_va_ten nvarchar(150) NOT NULL
);
INSERT INTO @aug_employees(rn, id, ho_va_ten)
SELECT ROW_NUMBER() OVER (ORDER BY nv.id), nv.id, nv.ho_va_ten
FROM dbo.Nhan_vien nv
JOIN dbo.Vai_tro vt ON vt.id = nv.id_vai_tro
WHERE nv.tinh_trang_lam_viec = 1
  AND vt.ten_vai_tro IN (N'Admin', N'Nhân viên');

-- Undo interactive stock restorations for deterministic demo orders first.
-- A tester may have cancelled one of these orders since the previous refresh;
-- keeping its HOAN_DON movement would make stock drift upward on every reset.
;WITH prior_demo_restore AS (
    SELECT id_san_pham_chi_tiet, SUM(so_luong_thay_doi) AS total_change
    FROM dbo.Bien_dong_ton_kho
    WHERE loai_bien_dong = N'HOAN_DON'
      AND ma_tham_chieu LIKE N'HDAUG26%'
    GROUP BY id_san_pham_chi_tiet
)
UPDATE variant
SET so_luong = ISNULL(variant.so_luong, 0) - prior.total_change
FROM dbo.san_pham_chi_tiet variant
JOIN prior_demo_restore prior ON prior.id_san_pham_chi_tiet = variant.id;

DELETE FROM dbo.Bien_dong_ton_kho
WHERE loai_bien_dong = N'HOAN_DON'
  AND ma_tham_chieu LIKE N'HDAUG26%';

-- Reverse only the aggregate stock movement owned by the previous August seed
-- before selecting eligible variants for this refresh.
;WITH prior_seed_movement AS (
    SELECT id_san_pham_chi_tiet, SUM(so_luong_thay_doi) AS total_change
    FROM dbo.Bien_dong_ton_kho
    WHERE loai_bien_dong = N'BAN_HANG_DEMO_T8'
      AND ma_tham_chieu = N'AUGUST-2026-SEED'
    GROUP BY id_san_pham_chi_tiet
)
UPDATE variant
SET so_luong = ISNULL(variant.so_luong, 0) - prior.total_change
FROM dbo.san_pham_chi_tiet variant
JOIN prior_seed_movement prior ON prior.id_san_pham_chi_tiet = variant.id;

DELETE FROM dbo.Bien_dong_ton_kho
WHERE loai_bien_dong = N'BAN_HANG_DEMO_T8'
  AND ma_tham_chieu = N'AUGUST-2026-SEED';

DECLARE @aug_variants TABLE (
    rn int NOT NULL PRIMARY KEY,
    id int NOT NULL,
    gia_ban decimal(15,2) NOT NULL,
    gia_nhap decimal(18,2) NULL
);
INSERT INTO @aug_variants(rn, id, gia_ban, gia_nhap)
SELECT ROW_NUMBER() OVER (ORDER BY vct.id), vct.id, vct.gia_ban, vct.gia_nhap
FROM dbo.san_pham_chi_tiet vct
JOIN dbo.san_pham v ON v.id = vct.id_san_pham
WHERE ISNULL(vct.trang_thai, 1) = 1
  AND ISNULL(v.trang_thai, 1) = 1
  AND vct.gia_ban > 0
  AND ISNULL(vct.so_luong, 0) >= 20;

DECLARE @aug_customer_count int = (SELECT COUNT(*) FROM @aug_customers);
DECLARE @aug_employee_count int = (SELECT COUNT(*) FROM @aug_employees);
DECLARE @aug_variant_count int = (SELECT COUNT(*) FROM @aug_variants);
IF @aug_customer_count = 0 OR @aug_employee_count = 0 OR @aug_variant_count = 0
    THROW 51020, N'Không đủ khách hàng, nhân viên hoặc biến thể để tạo dữ liệu tháng 8.', 1;

DECLARE @aug_voucher_percent int = (SELECT id FROM dbo.Giam_gia WHERE ma_giam_gia = N'AUGUST10');
DECLARE @aug_voucher_fixed int = (SELECT id FROM dbo.Giam_gia WHERE ma_giam_gia = N'AUGUST50');

-- Rebuild all dependent August fixtures on every run. User-created history and
-- non-demo orders remain untouched.
DELETE return_image
FROM dbo.Anh_doi_tra return_image
JOIN dbo.Yeu_cau_doi_tra request ON request.id = return_image.id_yeu_cau
JOIN dbo.Hoa_don order_row ON order_row.id = request.id_hoa_don
WHERE order_row.ma_hoa_don LIKE N'HDAUG26%';

DELETE request
FROM dbo.Yeu_cau_doi_tra request
JOIN dbo.Hoa_don order_row ON order_row.id = request.id_hoa_don
WHERE order_row.ma_hoa_don LIKE N'HDAUG26%';

DELETE review
FROM dbo.Danh_gia review
JOIN dbo.Hoa_don order_row ON order_row.id = review.id_hoa_don
WHERE order_row.ma_hoa_don LIKE N'HDAUG26%';

DELETE payment
FROM dbo.Lich_su_thanh_toan payment
JOIN dbo.Hoa_don order_row ON order_row.id = payment.id_hoa_don
WHERE order_row.ma_hoa_don LIKE N'HDAUG26%';

DELETE tracking
FROM dbo.Lich_su_tracking tracking
JOIN dbo.Hoa_don order_row ON order_row.id = tracking.id_hoa_don
WHERE order_row.ma_hoa_don LIKE N'HDAUG26%';

DELETE audit
FROM dbo.Hoa_don_audit_log audit
JOIN dbo.Hoa_don order_row ON order_row.id = audit.id_hoa_don
WHERE order_row.ma_hoa_don LIKE N'HDAUG26%';

DECLARE @aug_orders TABLE (
    seq int NOT NULL PRIMARY KEY,
    ma_hoa_don nvarchar(80) NOT NULL,
    id_khach_hang int NOT NULL,
    id_nhan_vien int NULL,
    id_giam_gia int NULL,
    hinh_thuc_nhan_hang tinyint NOT NULL,
    dia_chi_giao_hang nvarchar(500) NOT NULL,
    trang_thai tinyint NOT NULL,
    hinh_thuc_thanh_toan nvarchar(50) NOT NULL,
    phuong_thuc_thanh_toan_online nvarchar(50) NULL,
    ngay_tao datetime2(7) NOT NULL,
    da_thanh_toan bit NOT NULL,
    da_hoan_ton_kho bit NOT NULL,
    ten_khach_hang nvarchar(150) NOT NULL,
    so_dien_thoai nvarchar(20) NULL,
    email_khach_hang nvarchar(150) NULL
);

;WITH numbers AS (
    SELECT 1 AS seq
    UNION ALL
    SELECT seq + 1 FROM numbers WHERE seq < 124
),
base AS (
    SELECT
        seq,
        CAST(CASE WHEN seq % 7 = 0 THEN 1 ELSE 0 END AS bit) AS is_offline,
        seq % 20 AS profile,
        DATEADD(
            MINUTE,
            ((seq - 1) / 6) * 30,
            DATEADD(HOUR, 8, CAST(DATEADD(DAY, (seq - 1) % 6, CAST('2026-08-01' AS date)) AS datetime2))
        ) AS created_at
    FROM numbers
),
classified AS (
    SELECT
        base.*,
        CAST(CASE
            WHEN is_offline = 1 AND seq % 35 = 0 THEN 9
            WHEN is_offline = 1 THEN 4
            WHEN profile = 0 THEN 0
            WHEN profile = 1 THEN 1
            WHEN profile = 2 THEN 2
            WHEN profile = 3 THEN 3
            WHEN profile BETWEEN 4 AND 10 THEN 4
            WHEN profile = 11 THEN 5
            WHEN profile = 12 THEN 6
            WHEN profile = 13 THEN 7
            WHEN profile = 14 THEN 8
            WHEN profile = 15 THEN 9
            ELSE 4
        END AS tinyint) AS order_status
    FROM base
)
INSERT INTO @aug_orders (
    seq, ma_hoa_don, id_khach_hang, id_nhan_vien, id_giam_gia,
    hinh_thuc_nhan_hang, dia_chi_giao_hang, trang_thai,
    hinh_thuc_thanh_toan, phuong_thuc_thanh_toan_online,
    ngay_tao, da_thanh_toan, da_hoan_ton_kho,
    ten_khach_hang, so_dien_thoai, email_khach_hang
)
SELECT
    classified.seq,
    N'HDAUG26' + RIGHT(N'000' + CONVERT(nvarchar(3), classified.seq), 3),
    customer.id,
    CASE WHEN classified.is_offline = 1 THEN employee.id ELSE NULL END,
    CASE
        WHEN classified.order_status IN (1, 2, 3, 4, 8, 9) AND classified.seq % 10 = 0 THEN @aug_voucher_fixed
        WHEN classified.order_status IN (1, 2, 3, 4, 8, 9) AND classified.seq % 6 = 0 THEN @aug_voucher_percent
        ELSE NULL
    END,
    CASE WHEN classified.is_offline = 1 THEN 0 ELSE 1 END,
    CASE
        WHEN classified.is_offline = 1 THEN N'Mua trực tiếp tại cửa hàng'
        WHEN classified.seq % 3 = 0 THEN N'39 Nguyễn Thị Duệ, Yên Hòa, Cầu Giấy, Hà Nội'
        WHEN classified.seq % 3 = 1 THEN N'24 Xuân Thủy, Dịch Vọng Hậu, Cầu Giấy, Hà Nội'
        ELSE N'128 Trần Hưng Đạo, Hoàn Kiếm, Hà Nội'
    END,
    classified.order_status,
    CASE
        WHEN classified.is_offline = 1 AND classified.seq % 8 = 0 THEN N'MOMO'
        WHEN classified.is_offline = 1 THEN N'Tiền mặt'
        WHEN classified.order_status IN (0, 5, 6) THEN N'COD'
        WHEN classified.seq % 2 = 0 THEN N'MOMO'
        ELSE N'ZALOPAY'
    END,
    CASE
        WHEN classified.order_status = 7 THEN N'FAILED'
        WHEN classified.is_offline = 0 AND classified.order_status NOT IN (0, 5, 6)
            THEN CASE WHEN classified.seq % 2 = 0 THEN N'MOMO' ELSE N'ZALOPAY' END
        ELSE NULL
    END,
    classified.created_at,
    CASE
        WHEN classified.is_offline = 1 THEN 1
        WHEN classified.order_status IN (1, 2, 3, 4, 8, 9) THEN 1
        ELSE 0
    END,
    CASE WHEN classified.order_status IN (5, 6, 7, 9) THEN 1 ELSE 0 END,
    customer.ho_va_ten,
    customer.so_dien_thoai,
    customer.email
FROM classified
JOIN @aug_customers customer ON customer.rn = ((classified.seq - 1) % @aug_customer_count) + 1
JOIN @aug_employees employee ON employee.rn = ((classified.seq - 1) % @aug_employee_count) + 1
OPTION (MAXRECURSION 124);

MERGE dbo.Hoa_don AS target
USING @aug_orders AS source
ON target.ma_hoa_don = source.ma_hoa_don
WHEN MATCHED THEN
    UPDATE SET
        id_khach_hang = source.id_khach_hang,
        id_giam_gia = source.id_giam_gia,
        id_nhan_vien = source.id_nhan_vien,
        ma_yeu_cau = N'DEMO-AUG-CHECKOUT-' + RIGHT(N'000' + CONVERT(nvarchar(3), source.seq), 3),
        ma_giao_dich_cong = CASE
            WHEN source.hinh_thuc_nhan_hang = 1 AND source.hinh_thuc_thanh_toan IN (N'MOMO', N'ZALOPAY')
                THEN N'DEMO-AUG-GATEWAY-' + RIGHT(N'000' + CONVERT(nvarchar(3), source.seq), 3)
            ELSE NULL
        END,
        phi_van_chuyen = CASE WHEN source.hinh_thuc_nhan_hang = 0 OR source.seq % 3 = 0 THEN 0 ELSE 30000 END,
        hinh_thuc_nhan_hang = source.hinh_thuc_nhan_hang,
        dia_chi_giao_hang = source.dia_chi_giao_hang,
        trang_thai = source.trang_thai,
        hinh_thuc_thanh_toan = source.hinh_thuc_thanh_toan,
        phuong_thuc_thanh_toan_online = source.phuong_thuc_thanh_toan_online,
        ghi_chu = N'Dữ liệu nghiệp vụ tháng 8/2026',
        ngay_tao = source.ngay_tao,
        da_thanh_toan = source.da_thanh_toan,
        da_hoan_ton_kho = source.da_hoan_ton_kho,
        ten_khach_hang = source.ten_khach_hang,
        so_dien_thoai = source.so_dien_thoai,
        email_khach_hang = source.email_khach_hang
WHEN NOT MATCHED THEN
    INSERT (
        id_khach_hang, id_giam_gia, id_nhan_vien, ma_hoa_don, ma_yeu_cau,
        ma_giao_dich_cong, tong_tien, phi_van_chuyen, giam_gia_voucher,
        hinh_thuc_nhan_hang, dia_chi_giao_hang, trang_thai,
        hinh_thuc_thanh_toan, phuong_thuc_thanh_toan_online, ghi_chu,
        ngay_tao, da_thanh_toan, da_hoan_ton_kho,
        ten_khach_hang, so_dien_thoai, email_khach_hang
    )
    VALUES (
        source.id_khach_hang, source.id_giam_gia, source.id_nhan_vien, source.ma_hoa_don,
        N'DEMO-AUG-CHECKOUT-' + RIGHT(N'000' + CONVERT(nvarchar(3), source.seq), 3),
        CASE
            WHEN source.hinh_thuc_nhan_hang = 1 AND source.hinh_thuc_thanh_toan IN (N'MOMO', N'ZALOPAY')
                THEN N'DEMO-AUG-GATEWAY-' + RIGHT(N'000' + CONVERT(nvarchar(3), source.seq), 3)
            ELSE NULL
        END,
        0,
        CASE WHEN source.hinh_thuc_nhan_hang = 0 OR source.seq % 3 = 0 THEN 0 ELSE 30000 END,
        0,
        source.hinh_thuc_nhan_hang, source.dia_chi_giao_hang, source.trang_thai,
        source.hinh_thuc_thanh_toan, source.phuong_thuc_thanh_toan_online,
        N'Dữ liệu nghiệp vụ tháng 8/2026', source.ngay_tao,
        source.da_thanh_toan, source.da_hoan_ton_kho,
        source.ten_khach_hang, source.so_dien_thoai, source.email_khach_hang
    );

-- Reconcile the remaining quantity of August vouchers from their configured
-- capacity and every order that still owns its reservation. This also removes
-- drift after a demo order was cancelled and later reset by this script.
;WITH voucher_capacity AS (
    SELECT * FROM (VALUES
        (N'AUGUST10', 240),
        (N'AUGUST50', 180),
        (N'AUGUSTVIP', 80),
        (N'BACK2WORK', 150),
        (N'AUGPOS', 300),
        (N'AUGFREESHIP', 260)
    ) source(ma_giam_gia, capacity)
), voucher_usage AS (
    SELECT order_row.id_giam_gia, COUNT(*) AS used_quantity
    FROM dbo.Hoa_don order_row
    WHERE order_row.id_giam_gia IS NOT NULL
      AND ISNULL(order_row.da_hoan_ton_kho, 0) = 0
    GROUP BY order_row.id_giam_gia
)
UPDATE voucher
SET voucher.so_luong = capacity.capacity - ISNULL(usage.used_quantity, 0)
FROM dbo.Giam_gia voucher
JOIN voucher_capacity capacity ON capacity.ma_giam_gia = voucher.ma_giam_gia
LEFT JOIN voucher_usage usage ON usage.id_giam_gia = voucher.id;

IF EXISTS (
    SELECT 1 FROM dbo.Giam_gia
    WHERE ma_giam_gia IN (N'AUGUST10', N'AUGUST50', N'AUGUSTVIP', N'BACK2WORK', N'AUGPOS', N'AUGFREESHIP')
      AND ISNULL(so_luong, 0) < 0
)
    THROW 51039, N'Số lượt voucher tháng 8 không đủ cho dữ liệu đơn hàng hiện có.', 1;

INSERT INTO dbo.Hoa_don_chi_tiet (
    id_hoa_don, id_san_pham_chi_tiet, so_luong, don_gia,
    phan_tram_giam, thanh_tien, gia_nhap
)
SELECT
    order_row.id,
    variant.id,
    CASE WHEN seed.seq % 5 = 0 THEN 2 ELSE 1 END,
    variant.gia_ban,
    0,
    variant.gia_ban * CASE WHEN seed.seq % 5 = 0 THEN 2 ELSE 1 END,
    variant.gia_nhap
FROM @aug_orders seed
JOIN dbo.Hoa_don order_row ON order_row.ma_hoa_don = seed.ma_hoa_don
JOIN @aug_variants variant ON variant.rn = ((seed.seq - 1) % @aug_variant_count) + 1
WHERE NOT EXISTS (
    SELECT 1 FROM dbo.Hoa_don_chi_tiet detail
    WHERE detail.id_hoa_don = order_row.id
);

INSERT INTO dbo.Hoa_don_chi_tiet (
    id_hoa_don, id_san_pham_chi_tiet, so_luong, don_gia,
    phan_tram_giam, thanh_tien, gia_nhap
)
SELECT
    order_row.id,
    variant.id,
    1,
    variant.gia_ban,
    0,
    variant.gia_ban,
    variant.gia_nhap
FROM @aug_orders seed
JOIN dbo.Hoa_don order_row ON order_row.ma_hoa_don = seed.ma_hoa_don
JOIN @aug_variants variant ON variant.rn = ((seed.seq + 16) % @aug_variant_count) + 1
WHERE seed.seq % 3 = 0
  AND NOT EXISTS (
      SELECT 1 FROM dbo.Hoa_don_chi_tiet detail
      WHERE detail.id_hoa_don = order_row.id
        AND detail.id_san_pham_chi_tiet = variant.id
  );

;WITH order_totals AS (
    SELECT detail.id_hoa_don, SUM(detail.thanh_tien) AS subtotal
    FROM dbo.Hoa_don_chi_tiet detail
    JOIN dbo.Hoa_don order_row ON order_row.id = detail.id_hoa_don
    WHERE order_row.ma_hoa_don LIKE N'HDAUG26%'
    GROUP BY detail.id_hoa_don
),
calculated AS (
    SELECT
        order_row.id,
        totals.subtotal,
        CASE
            WHEN voucher.id IS NULL THEN CAST(0 AS decimal(15,2))
            WHEN voucher.phan_tram_giam > 0 THEN
                CASE
                    WHEN voucher.giam_toi_da > 0
                         AND ROUND(totals.subtotal * voucher.phan_tram_giam / 100, 0) > voucher.giam_toi_da
                        THEN voucher.giam_toi_da
                    ELSE ROUND(totals.subtotal * voucher.phan_tram_giam / 100, 0)
                END
            WHEN voucher.gio_tri_giam > 0 THEN voucher.gio_tri_giam
            ELSE CAST(0 AS decimal(15,2))
        END AS raw_discount
    FROM dbo.Hoa_don order_row
    JOIN order_totals totals ON totals.id_hoa_don = order_row.id
    LEFT JOIN dbo.Giam_gia voucher ON voucher.id = order_row.id_giam_gia
)
UPDATE order_row
SET giam_gia_voucher = CASE
        WHEN calculated.raw_discount > calculated.subtotal THEN calculated.subtotal
        ELSE calculated.raw_discount
    END,
    tong_tien = calculated.subtotal + ISNULL(order_row.phi_van_chuyen, 0)
        - CASE
            WHEN calculated.raw_discount > calculated.subtotal THEN calculated.subtotal
            ELSE calculated.raw_discount
          END
FROM dbo.Hoa_don order_row
JOIN calculated ON calculated.id = order_row.id;

-- Apply inventory once for demo orders that still own stock. Ended orders are
-- already represented as restored and therefore do not reduce live inventory.
DECLARE @aug_stock TABLE (
    id_san_pham_chi_tiet int NOT NULL PRIMARY KEY,
    quantity int NOT NULL
);
INSERT INTO @aug_stock(id_san_pham_chi_tiet, quantity)
SELECT detail.id_san_pham_chi_tiet, SUM(detail.so_luong)
FROM dbo.Hoa_don order_row
JOIN dbo.Hoa_don_chi_tiet detail ON detail.id_hoa_don = order_row.id
WHERE order_row.ma_hoa_don LIKE N'HDAUG26%'
  AND order_row.trang_thai NOT IN (5, 6, 7, 9)
  AND NOT EXISTS (
      SELECT 1 FROM dbo.Bien_dong_ton_kho movement
      WHERE movement.id_san_pham_chi_tiet = detail.id_san_pham_chi_tiet
        AND movement.loai_bien_dong = N'BAN_HANG_DEMO_T8'
        AND movement.ma_tham_chieu = N'AUGUST-2026-SEED'
  )
GROUP BY detail.id_san_pham_chi_tiet;

IF EXISTS (
    SELECT 1
    FROM @aug_stock seed
    JOIN dbo.san_pham_chi_tiet variant ON variant.id = seed.id_san_pham_chi_tiet
    WHERE ISNULL(variant.so_luong, 0) < seed.quantity
)
    THROW 51021, N'Tồn kho không đủ để tạo lịch sử bán hàng tháng 8.', 1;

INSERT INTO dbo.Bien_dong_ton_kho (
    id_san_pham_chi_tiet, so_luong_truoc, so_luong_thay_doi, so_luong_sau,
    loai_bien_dong, ma_tham_chieu, nguoi_thuc_hien, ghi_chu, ngay_tao
)
SELECT
    variant.id, variant.so_luong, -seed.quantity, variant.so_luong - seed.quantity,
    N'BAN_HANG_DEMO_T8', N'AUGUST-2026-SEED', N'Hệ thống dữ liệu mẫu',
    N'Tổng hợp lượng bán của dữ liệu tháng 8/2026', '2026-08-06T22:30:00'
FROM @aug_stock seed
JOIN dbo.san_pham_chi_tiet variant ON variant.id = seed.id_san_pham_chi_tiet;

UPDATE variant
SET so_luong = variant.so_luong - seed.quantity
FROM dbo.san_pham_chi_tiet variant
JOIN @aug_stock seed ON seed.id_san_pham_chi_tiet = variant.id;

INSERT INTO dbo.Lich_su_thanh_toan (
    id_hoa_don, so_tien, phuong_thuc, ma_giao_dich,
    trang_thai, noi_dung, ngay_tao
)
SELECT
    order_row.id, order_row.tong_tien, order_row.hinh_thuc_thanh_toan,
    N'AUG-SUCCESS-' + order_row.ma_hoa_don,
    N'SUCCESS', N'Thanh toán dữ liệu mẫu tháng 8 thành công',
    DATEADD(MINUTE, 5, order_row.ngay_tao)
FROM dbo.Hoa_don order_row
WHERE order_row.ma_hoa_don LIKE N'HDAUG26%'
  AND order_row.da_thanh_toan = 1
  AND NOT EXISTS (
      SELECT 1 FROM dbo.Lich_su_thanh_toan payment
      WHERE payment.id_hoa_don = order_row.id AND UPPER(payment.trang_thai) = N'SUCCESS'
  );

INSERT INTO dbo.Lich_su_thanh_toan (
    id_hoa_don, so_tien, phuong_thuc, ma_giao_dich,
    trang_thai, noi_dung, ngay_tao
)
SELECT
    order_row.id, 0, order_row.hinh_thuc_thanh_toan,
    N'AUG-FAILED-' + order_row.ma_hoa_don,
    N'FAILED', N'Thanh toán dữ liệu mẫu tháng 8 thất bại',
    DATEADD(MINUTE, 5, order_row.ngay_tao)
FROM dbo.Hoa_don order_row
WHERE order_row.ma_hoa_don LIKE N'HDAUG26%'
  AND order_row.trang_thai = 7
  AND NOT EXISTS (
      SELECT 1 FROM dbo.Lich_su_thanh_toan payment
      WHERE payment.id_hoa_don = order_row.id AND UPPER(payment.trang_thai) = N'FAILED'
  );

INSERT INTO dbo.Lich_su_thanh_toan (
    id_hoa_don, so_tien, phuong_thuc, ma_giao_dich,
    trang_thai, noi_dung, ngay_tao
)
SELECT
    order_row.id, order_row.tong_tien, order_row.hinh_thuc_thanh_toan,
    N'AUG-REFUND-' + order_row.ma_hoa_don,
    N'REFUNDED', N'Hoàn tiền dữ liệu mẫu tháng 8',
    DATEADD(MINUTE, 60, order_row.ngay_tao)
FROM dbo.Hoa_don order_row
WHERE order_row.ma_hoa_don LIKE N'HDAUG26%'
  AND order_row.trang_thai = 9
  AND NOT EXISTS (
      SELECT 1 FROM dbo.Lich_su_thanh_toan payment
      WHERE payment.id_hoa_don = order_row.id AND UPPER(payment.trang_thai) = N'REFUNDED'
  );

INSERT INTO dbo.Lich_su_tracking(id_hoa_don, trang_thai, mo_ta, ngay_cap_nhat)
SELECT
    order_row.id,
    CASE order_row.trang_thai
        WHEN 0 THEN N'pending' WHEN 1 THEN N'confirmed' WHEN 2 THEN N'processing'
        WHEN 3 THEN N'shipped' WHEN 4 THEN N'delivered' WHEN 5 THEN N'cancelled'
        WHEN 6 THEN N'failed' WHEN 7 THEN N'payment_failed'
        WHEN 8 THEN N'return_requested' ELSE N'refunded'
    END,
    N'Trạng thái dữ liệu nghiệp vụ tháng 8/2026',
    DATEADD(HOUR, 2, order_row.ngay_tao)
FROM dbo.Hoa_don order_row
WHERE order_row.ma_hoa_don LIKE N'HDAUG26%'
  AND NOT EXISTS (
      SELECT 1 FROM dbo.Lich_su_tracking tracking
      WHERE tracking.id_hoa_don = order_row.id
  );

INSERT INTO dbo.Hoa_don_audit_log (
    id_hoa_don, hanh_dong, trang_thai_cu, trang_thai_moi,
    nguoi_thuc_hien, vai_tro, ghi_chu, ngay_tao
)
SELECT
    order_row.id, N'TAO_DU_LIEU_DEMO', NULL, order_row.trang_thai,
    COALESCE(employee.ho_va_ten, N'Hệ thống'), N'SYSTEM',
    N'Khởi tạo dữ liệu nghiệp vụ tháng 8/2026', order_row.ngay_tao
FROM dbo.Hoa_don order_row
LEFT JOIN dbo.Nhan_vien employee ON employee.id = order_row.id_nhan_vien
WHERE order_row.ma_hoa_don LIKE N'HDAUG26%'
  AND NOT EXISTS (
      SELECT 1 FROM dbo.Hoa_don_audit_log audit
      WHERE audit.id_hoa_don = order_row.id AND audit.hanh_dong = N'TAO_DU_LIEU_DEMO'
  );

INSERT INTO dbo.Danh_gia (
    id_khach_hang, id_san_pham, id_hoa_don, so_sao,
    noi_dung, anh_danh_gia, trang_thai, ngay_tao
)
SELECT
    order_row.id_khach_hang,
    variant.id_san_pham,
    order_row.id,
    CASE WHEN order_row.id % 9 = 0 THEN 3 WHEN order_row.id % 4 = 0 THEN 4 ELSE 5 END,
    CASE order_row.id % 5
        WHEN 0 THEN N'Phom váy vừa vặn, chất liệu dễ chịu và tư vấn size chính xác.'
        WHEN 1 THEN N'Sản phẩm đúng mô tả, đóng gói cẩn thận và giao hàng đúng hẹn.'
        WHEN 2 THEN N'Màu sắc thực tế đẹp, đường may gọn và mặc đi làm rất phù hợp.'
        WHEN 3 THEN N'Mình đã mặc đi dự tiệc, váy lên dáng đẹp và di chuyển thoải mái.'
        ELSE N'Nhân viên hỗ trợ nhiệt tình, mình sẽ tiếp tục chọn Zestia cho dịp tới.'
    END,
    CASE WHEN order_row.id % 4 = 0 THEN variant.anh_url ELSE NULL END,
    1,
    DATEADD(HOUR, 3, order_row.ngay_tao)
FROM dbo.Hoa_don order_row
CROSS APPLY (
    SELECT TOP 1 detail.id_san_pham_chi_tiet
    FROM dbo.Hoa_don_chi_tiet detail
    WHERE detail.id_hoa_don = order_row.id
    ORDER BY detail.id
) first_detail
JOIN dbo.san_pham_chi_tiet variant ON variant.id = first_detail.id_san_pham_chi_tiet
WHERE order_row.ma_hoa_don LIKE N'HDAUG26%'
  AND order_row.trang_thai = 4
  AND NOT EXISTS (
      SELECT 1 FROM dbo.Danh_gia review
      WHERE review.id_khach_hang = order_row.id_khach_hang
        AND review.id_san_pham = variant.id_san_pham
        AND review.id_hoa_don = order_row.id
  )
ORDER BY order_row.ngay_tao;

INSERT INTO dbo.Newsletter_subscriber(email, trang_thai, ngay_dang_ky, ngay_cap_nhat)
SELECT TOP (20)
    LOWER(customer.email), 1,
    DATEADD(DAY, (ROW_NUMBER() OVER (ORDER BY customer.id) - 1) % 6, CAST('2026-08-01' AS datetime2)),
    DATEADD(DAY, (ROW_NUMBER() OVER (ORDER BY customer.id) - 1) % 6, CAST('2026-08-01' AS datetime2))
FROM dbo.Khach_hang customer
WHERE customer.email IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM dbo.Newsletter_subscriber subscriber
      WHERE subscriber.email = LOWER(customer.email)
  )
ORDER BY customer.id;

INSERT INTO dbo.San_pham_yeu_thich(id_khach_hang, id_san_pham, ngay_tao)
SELECT TOP (40)
    customer.id, product.id,
    DATEADD(DAY, (customer.id + product.id) % 6, CAST('2026-08-01' AS datetime2))
FROM dbo.Khach_hang customer
CROSS JOIN dbo.san_pham product
WHERE product.trang_thai = 1
  AND (customer.id + product.id) % 7 = 0
  AND NOT EXISTS (
      SELECT 1 FROM dbo.San_pham_yeu_thich favorite
      WHERE favorite.id_khach_hang = customer.id AND favorite.id_san_pham = product.id
  )
ORDER BY customer.id, product.id;

INSERT INTO dbo.Lich_su_xem(id_khach_hang, id_san_pham, ngay_xem)
SELECT TOP (80)
    customer.id, product.id,
    DATEADD(HOUR, product.id % 12, DATEADD(DAY, (customer.id + product.id) % 6, CAST('2026-08-01' AS datetime2)))
FROM dbo.Khach_hang customer
CROSS JOIN dbo.san_pham product
WHERE product.trang_thai = 1
  AND (customer.id + product.id) % 5 = 0
  AND NOT EXISTS (
      SELECT 1 FROM dbo.Lich_su_xem history
      WHERE history.id_khach_hang = customer.id AND history.id_san_pham = product.id
  )
ORDER BY customer.id, product.id;

-- Repair rows created by an older August seed without touching real data from
-- other months. Every generated customer activity remains at or before 06/08.
UPDATE dbo.Newsletter_subscriber
SET ngay_dang_ky = CASE WHEN ngay_dang_ky >= '2026-08-07' AND ngay_dang_ky < '2026-09-01'
        THEN DATEADD(MINUTE, id % 600, CAST('2026-08-06T08:00:00' AS datetime2)) ELSE ngay_dang_ky END,
    ngay_cap_nhat = CASE WHEN ngay_cap_nhat >= '2026-08-07' AND ngay_cap_nhat < '2026-09-01'
        THEN DATEADD(MINUTE, id % 600, CAST('2026-08-06T08:00:00' AS datetime2)) ELSE ngay_cap_nhat END;

UPDATE dbo.San_pham_yeu_thich
SET ngay_tao = DATEADD(MINUTE, id % 600, CAST('2026-08-06T08:00:00' AS datetime2))
WHERE ngay_tao >= '2026-08-07' AND ngay_tao < '2026-09-01';

UPDATE dbo.Lich_su_xem
SET ngay_xem = DATEADD(MINUTE, id % 600, CAST('2026-08-06T08:00:00' AS datetime2))
WHERE ngay_xem >= '2026-08-07' AND ngay_xem < '2026-09-01';

INSERT INTO dbo.Thong_bao (
    tieu_de, noi_dung, loai, trang_thai,
    da_doc, gui_email, da_gui, ngay_gui, ngay_tao
)
SELECT source.tieu_de, source.noi_dung, source.loai, 1, 0, 0, 0, source.ngay_tao, source.ngay_tao
FROM (VALUES
    (N'Chào tháng 8 cùng Zestia', N'Khám phá bộ sưu tập tháng 8 và các voucher đang hiển thị ngay tại giỏ hàng.', N'Voucher', CAST('2026-08-01T08:00:00' AS datetime2)),
    (N'Lookbook đi làm tháng 8', N'Các thiết kế công sở mới đã được sắp xếp theo phom, chất liệu và size để bạn dễ lựa chọn.', N'SanPham', CAST('2026-08-05T09:00:00' AS datetime2))
) source(tieu_de, noi_dung, loai, ngay_tao)
WHERE NOT EXISTS (
    SELECT 1 FROM dbo.Thong_bao notification
    WHERE notification.tieu_de = source.tieu_de
);

DELETE FROM dbo.Thong_bao
WHERE tieu_de = N'Lịch hoạt động showroom tháng 8'
  AND ngay_tao >= '2026-08-07';

;WITH chat_numbers AS (
    SELECT 1 AS seq
    UNION ALL SELECT seq + 1 FROM chat_numbers WHERE seq < 8
)
INSERT INTO dbo.Ho_tro_chat (
    ma_phien, id_khach_hang, id_nhan_vien, tieu_de, trang_thai,
    ngay_tao, ngay_nhan, ngay_dong, ngay_cap_nhat
)
SELECT
    N'AUG-CHAT-' + RIGHT(N'00' + CONVERT(nvarchar(2), numbers.seq), 2),
    customer.id,
    CASE WHEN numbers.seq % 3 = 1 THEN NULL ELSE employee.id END,
    CASE numbers.seq % 3
        WHEN 0 THEN N'Tư vấn chọn size'
        WHEN 1 THEN N'Hỏi tình trạng đơn hàng'
        ELSE N'Tư vấn voucher phù hợp'
    END,
    CASE numbers.seq % 3 WHEN 0 THEN N'CLOSED' WHEN 1 THEN N'WAITING' ELSE N'ACTIVE' END,
    DATEADD(MINUTE, ((numbers.seq - 1) / 6) * 30, DATEADD(DAY, (numbers.seq - 1) % 6, CAST('2026-08-01T09:00:00' AS datetime2))),
    CASE WHEN numbers.seq % 3 = 1 THEN NULL ELSE DATEADD(MINUTE, 3, DATEADD(MINUTE, ((numbers.seq - 1) / 6) * 30, DATEADD(DAY, (numbers.seq - 1) % 6, CAST('2026-08-01T09:00:00' AS datetime2)))) END,
    CASE WHEN numbers.seq % 3 = 0 THEN DATEADD(MINUTE, 18, DATEADD(MINUTE, ((numbers.seq - 1) / 6) * 30, DATEADD(DAY, (numbers.seq - 1) % 6, CAST('2026-08-01T09:00:00' AS datetime2)))) ELSE NULL END,
    DATEADD(MINUTE, 12, DATEADD(MINUTE, ((numbers.seq - 1) / 6) * 30, DATEADD(DAY, (numbers.seq - 1) % 6, CAST('2026-08-01T09:00:00' AS datetime2))))
FROM chat_numbers numbers
JOIN @aug_customers customer ON customer.rn = ((numbers.seq - 1) % @aug_customer_count) + 1
JOIN @aug_employees employee ON employee.rn = ((numbers.seq - 1) % @aug_employee_count) + 1
WHERE NOT EXISTS (
    SELECT 1 FROM dbo.Ho_tro_chat chat
    WHERE chat.ma_phien = N'AUG-CHAT-' + RIGHT(N'00' + CONVERT(nvarchar(2), numbers.seq), 2)
)
OPTION (MAXRECURSION 8);

-- Re-date chat fixtures that may already exist from an older seed run.
UPDATE chat
SET ngay_tao = fixture.created_at,
    ngay_nhan = CASE WHEN parsed.seq % 3 = 1 THEN NULL ELSE DATEADD(MINUTE, 3, fixture.created_at) END,
    ngay_dong = CASE WHEN parsed.seq % 3 = 0 THEN DATEADD(MINUTE, 18, fixture.created_at) ELSE NULL END,
    ngay_cap_nhat = DATEADD(MINUTE, 12, fixture.created_at)
FROM dbo.Ho_tro_chat chat
CROSS APPLY (SELECT TRY_CONVERT(int, RIGHT(chat.ma_phien, 2)) AS seq) parsed
CROSS APPLY (
    SELECT DATEADD(MINUTE, ((parsed.seq - 1) / 6) * 30,
        DATEADD(DAY, (parsed.seq - 1) % 6, CAST('2026-08-01T09:00:00' AS datetime2))) AS created_at
) fixture
WHERE chat.ma_phien LIKE N'AUG-CHAT-%'
  AND parsed.seq IS NOT NULL;

INSERT INTO dbo.Tin_nhan_ho_tro(id_ho_tro_chat, loai_nguoi_gui, ten_nguoi_gui, noi_dung, ngay_tao)
SELECT
    chat.id, message.loai_nguoi_gui, message.ten_nguoi_gui, message.noi_dung,
    DATEADD(MINUTE, message.minute_offset, chat.ngay_tao)
FROM dbo.Ho_tro_chat chat
CROSS APPLY (VALUES
    (N'CUSTOMER', N'Khách hàng', N'Mình cần nhân viên hỗ trợ thêm về sản phẩm và đơn hàng.', 0),
    (N'SYSTEM', N'Zestia', N'Yêu cầu đã được ghi nhận và chuyển tới nhân viên đang trong ca.', 1)
) message(loai_nguoi_gui, ten_nguoi_gui, noi_dung, minute_offset)
WHERE chat.ma_phien LIKE N'AUG-CHAT-%'
  AND NOT EXISTS (
      SELECT 1 FROM dbo.Tin_nhan_ho_tro existing
      WHERE existing.id_ho_tro_chat = chat.id
  );

;WITH ordered_demo_messages AS (
    SELECT message.id, chat.ngay_tao,
           ROW_NUMBER() OVER (PARTITION BY message.id_ho_tro_chat ORDER BY message.id) - 1 AS minute_offset
    FROM dbo.Tin_nhan_ho_tro message
    JOIN dbo.Ho_tro_chat chat ON chat.id = message.id_ho_tro_chat
    WHERE chat.ma_phien LIKE N'AUG-CHAT-%'
)
UPDATE message
SET ngay_tao = DATEADD(MINUTE, ordered.minute_offset, ordered.ngay_tao)
FROM dbo.Tin_nhan_ho_tro message
JOIN ordered_demo_messages ordered ON ordered.id = message.id;

INSERT INTO dbo.Yeu_cau_doi_tra (
    id_hoa_don, id_hoa_don_chi_tiet, id_bien_the_doi,
    id_khach_hang, id_nhan_vien_xu_ly, loai_yeu_cau, nguon,
    trang_thai, so_luong, ly_do, tinh_trang_hang,
    thong_tin_hoan_tien, so_tien_hoan, ma_giao_dich_hoan,
    ghi_chu_nhan_vien, da_hoan_ton_kho, ngay_tao,
    ngay_duyet, ngay_nhan_hang, ngay_hoan_tat
)
SELECT
    order_row.id,
    first_detail.id,
    CASE WHEN order_row.id % 2 = 0 THEN replacement.id ELSE NULL END,
    order_row.id_khach_hang,
    COALESCE(order_row.id_nhan_vien, employee.id),
    CASE WHEN order_row.id % 2 = 0 THEN N'DOI' ELSE N'TRA' END,
    CASE WHEN order_row.hinh_thuc_nhan_hang = 0 THEN N'OFFLINE' ELSE N'ONLINE' END,
    CASE
        WHEN order_row.trang_thai = 8 AND order_row.id % 2 = 0 THEN N'CHO_NHAN_HANG'
        WHEN order_row.trang_thai = 8 THEN N'CHO_XAC_NHAN_HOAN_TIEN'
        WHEN order_row.id % 2 = 0 THEN N'DA_DOI'
        ELSE N'DA_HOAN_TIEN'
    END,
    1,
    CASE WHEN order_row.id % 2 = 0 THEN N'Cần đổi sang size phù hợp hơn.' ELSE N'Sản phẩm không phù hợp với dịp sử dụng.' END,
    N'Sản phẩm còn nguyên tem, chưa qua giặt và có ảnh tình trạng kèm theo.',
    CASE WHEN order_row.id % 2 = 1 THEN N'VCB - 9869167207 - NGUYEN TIEN THANH' ELSE NULL END,
    CASE WHEN order_row.id % 2 = 1 THEN first_detail.thanh_tien ELSE NULL END,
    CASE WHEN order_row.trang_thai = 9 AND order_row.id % 2 = 1 THEN N'AUG-REFUND-' + order_row.ma_hoa_don ELSE NULL END,
    CASE WHEN order_row.trang_thai = 9 THEN N'Đã hoàn tất xử lý dữ liệu mẫu.' ELSE N'Đang chờ bước xử lý tiếp theo.' END,
    CASE WHEN order_row.trang_thai = 9 THEN 1 ELSE 0 END,
    DATEADD(MINUTE, 30, order_row.ngay_tao),
    CASE WHEN order_row.trang_thai = 9 THEN DATEADD(MINUTE, 60, order_row.ngay_tao) ELSE NULL END,
    CASE WHEN order_row.trang_thai = 9 THEN DATEADD(MINUTE, 90, order_row.ngay_tao) ELSE NULL END,
    CASE WHEN order_row.trang_thai = 9 THEN DATEADD(MINUTE, 120, order_row.ngay_tao) ELSE NULL END
FROM dbo.Hoa_don order_row
CROSS APPLY (
    SELECT TOP 1 detail.*
    FROM dbo.Hoa_don_chi_tiet detail
    WHERE detail.id_hoa_don = order_row.id
    ORDER BY detail.id
) first_detail
JOIN dbo.san_pham_chi_tiet current_variant ON current_variant.id = first_detail.id_san_pham_chi_tiet
OUTER APPLY (
    SELECT TOP 1 candidate.id
    FROM dbo.san_pham_chi_tiet candidate
    WHERE candidate.id_san_pham = current_variant.id_san_pham
      AND candidate.id <> current_variant.id
      AND candidate.trang_thai = 1
    ORDER BY candidate.id
) replacement
CROSS APPLY (
    SELECT TOP 1 id FROM dbo.Nhan_vien WHERE tinh_trang_lam_viec = 1 ORDER BY id
) employee
WHERE order_row.ma_hoa_don LIKE N'HDAUG26%'
  AND order_row.trang_thai IN (8, 9)
  AND order_row.id_khach_hang IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM dbo.Yeu_cau_doi_tra request
      WHERE request.id_hoa_don_chi_tiet = first_detail.id
  );

INSERT INTO dbo.Anh_doi_tra(id_yeu_cau, anh_url, ngay_tao)
SELECT
    request.id,
    COALESCE(variant.anh_url, N'/images/products/dress1.jpg'),
    DATEADD(MINUTE, 2, request.ngay_tao)
FROM dbo.Yeu_cau_doi_tra request
JOIN dbo.Hoa_don order_row ON order_row.id = request.id_hoa_don
JOIN dbo.Hoa_don_chi_tiet detail ON detail.id = request.id_hoa_don_chi_tiet
JOIN dbo.san_pham_chi_tiet variant ON variant.id = detail.id_san_pham_chi_tiet
WHERE order_row.ma_hoa_don LIKE N'HDAUG26%'
  AND request.nguon = N'ONLINE'
  AND NOT EXISTS (
      SELECT 1 FROM dbo.Anh_doi_tra image
      WHERE image.id_yeu_cau = request.id
  );

-- Normalize every insert-only demo row as well. This makes the script repair
-- data created by an older sqlcmd run that did not use UTF-8 input.
UPDATE payment
SET payment.noi_dung = CASE UPPER(payment.trang_thai)
        WHEN N'SUCCESS' THEN N'Thanh toán dữ liệu mẫu tháng 8 thành công'
        WHEN N'FAILED' THEN N'Thanh toán dữ liệu mẫu tháng 8 thất bại'
        WHEN N'REFUNDED' THEN N'Hoàn tiền dữ liệu mẫu tháng 8'
        ELSE payment.noi_dung
    END,
    payment.ngay_tao = CASE WHEN UPPER(payment.trang_thai) = N'REFUNDED'
        THEN DATEADD(MINUTE, 60, order_row.ngay_tao)
        ELSE DATEADD(MINUTE, 5, order_row.ngay_tao) END
FROM dbo.Lich_su_thanh_toan payment
JOIN dbo.Hoa_don order_row ON order_row.id = payment.id_hoa_don
WHERE order_row.ma_hoa_don LIKE N'HDAUG26%'
  AND payment.ma_giao_dich LIKE N'AUG-%';

UPDATE tracking
SET tracking.mo_ta = N'Trạng thái dữ liệu nghiệp vụ tháng 8/2026',
    tracking.ngay_cap_nhat = DATEADD(HOUR, 2, order_row.ngay_tao)
FROM dbo.Lich_su_tracking tracking
JOIN dbo.Hoa_don order_row ON order_row.id = tracking.id_hoa_don
WHERE order_row.ma_hoa_don LIKE N'HDAUG26%';

UPDATE audit
SET audit.nguoi_thuc_hien = COALESCE(employee.ho_va_ten, N'Hệ thống'),
    audit.ghi_chu = N'Khởi tạo dữ liệu nghiệp vụ tháng 8/2026',
    audit.ngay_tao = order_row.ngay_tao
FROM dbo.Hoa_don_audit_log audit
JOIN dbo.Hoa_don order_row ON order_row.id = audit.id_hoa_don
LEFT JOIN dbo.Nhan_vien employee ON employee.id = order_row.id_nhan_vien
WHERE order_row.ma_hoa_don LIKE N'HDAUG26%'
  AND audit.hanh_dong = N'TAO_DU_LIEU_DEMO';

UPDATE review
SET review.noi_dung = CASE order_row.id % 5
        WHEN 0 THEN N'Phom váy vừa vặn, chất liệu dễ chịu và tư vấn size chính xác.'
        WHEN 1 THEN N'Sản phẩm đúng mô tả, đóng gói cẩn thận và giao hàng đúng hẹn.'
        WHEN 2 THEN N'Màu sắc thực tế đẹp, đường may gọn và mặc đi làm rất phù hợp.'
        WHEN 3 THEN N'Mình đã mặc đi dự tiệc, váy lên dáng đẹp và di chuyển thoải mái.'
        ELSE N'Nhân viên hỗ trợ nhiệt tình, mình sẽ tiếp tục chọn Zestia cho dịp tới.'
    END,
    review.ngay_tao = DATEADD(HOUR, 3, order_row.ngay_tao)
FROM dbo.Danh_gia review
JOIN dbo.Hoa_don order_row ON order_row.id = review.id_hoa_don
WHERE order_row.ma_hoa_don LIKE N'HDAUG26%';

-- HDS orders are generated fixtures as well, not customer-authored reviews.
UPDATE review
SET review.noi_dung = CASE review.id % 8
        WHEN 0 THEN N'Sản phẩm đúng hình, đường may gọn và tư vấn size phù hợp.'
        WHEN 1 THEN N'Mình đã mặc đi làm, phom lên đẹp và chất liệu dễ chịu.'
        WHEN 2 THEN N'Giao hàng cẩn thận, màu thực tế đúng với ảnh trên website.'
        WHEN 3 THEN N'Váy vừa số đo, phần eo ôm vừa phải và di chuyển thoải mái.'
        WHEN 4 THEN N'Đóng gói đẹp, sản phẩm không có chỉ thừa và mặc khá tôn dáng.'
        WHEN 5 THEN N'Mình chọn theo bảng size của Zestia và nhận được size rất vừa.'
        WHEN 6 THEN N'Chất vải ổn trong tầm giá, mình sẽ tiếp tục mua mẫu khác.'
        ELSE N'Nhận hàng đúng hẹn, nhân viên hỗ trợ đổi size nhanh và rõ ràng.'
    END
FROM dbo.Danh_gia review
JOIN dbo.Hoa_don order_row ON order_row.id = review.id_hoa_don
WHERE order_row.ma_hoa_don LIKE N'HDS%';

UPDATE notification
SET notification.tieu_de = source.tieu_de,
    notification.noi_dung = source.noi_dung,
    notification.loai = source.loai
FROM dbo.Thong_bao notification
JOIN (VALUES
    (CAST('2026-08-01T08:00:00' AS datetime2), N'Chào tháng 8 cùng Zestia', N'Khám phá bộ sưu tập tháng 8 và các voucher đang hiển thị ngay tại giỏ hàng.', N'Voucher'),
    (CAST('2026-08-05T09:00:00' AS datetime2), N'Lookbook đi làm tháng 8', N'Các thiết kế công sở mới đã được sắp xếp theo phom, chất liệu và size để bạn dễ lựa chọn.', N'SanPham')
) source(ngay_tao, tieu_de, noi_dung, loai)
    ON notification.ngay_tao = source.ngay_tao;

UPDATE notification
SET notification.tieu_de = N'Bộ sưu tập mới đã lên kệ',
    notification.noi_dung = N'Zestia cập nhật các mẫu váy mới cho mùa lễ hội và sự kiện.'
FROM dbo.Thong_bao notification
WHERE notification.loai = N'HeThong'
  AND notification.ngay_tao >= '2026-07-13'
  AND notification.ngay_tao < '2026-08-01'
  AND notification.noi_dung LIKE N'%Zestia%';

UPDATE notification
SET notification.tieu_de = N'Voucher ZESTIA10 đang hoạt động',
    notification.noi_dung = N'Khách hàng có thể nhập ZESTIA10 để giảm 10% cho đơn đủ điều kiện.'
FROM dbo.Thong_bao notification
WHERE notification.loai = N'Voucher'
  AND notification.ngay_tao >= '2026-07-13'
  AND notification.ngay_tao < '2026-08-01'
  AND (notification.tieu_de LIKE N'%ZESTIA10%' OR notification.noi_dung LIKE N'%ZESTIA10%');

;WITH duplicate_seed_notifications AS (
    SELECT id,
           ROW_NUMBER() OVER (
               PARTITION BY tieu_de
               ORDER BY ngay_tao, id
           ) AS rn
    FROM dbo.Thong_bao
    WHERE tieu_de IN (
        N'Bộ sưu tập mới đã lên kệ',
        N'Voucher ZESTIA10 đang hoạt động'
    )
)
DELETE notification
FROM dbo.Thong_bao notification
JOIN duplicate_seed_notifications duplicate_row ON duplicate_row.id = notification.id
WHERE duplicate_row.rn > 1;

UPDATE chat
SET chat.tieu_de = CASE CONVERT(int, RIGHT(chat.ma_phien, 2)) % 3
        WHEN 0 THEN N'Tư vấn chọn size'
        WHEN 1 THEN N'Hỏi tình trạng đơn hàng'
        ELSE N'Tư vấn voucher phù hợp'
    END
FROM dbo.Ho_tro_chat chat
WHERE chat.ma_phien LIKE N'AUG-CHAT-%';

UPDATE message
SET message.ten_nguoi_gui = CASE message.loai_nguoi_gui
        WHEN N'CUSTOMER' THEN N'Khách hàng'
        ELSE N'Zestia'
    END,
    message.noi_dung = CASE message.loai_nguoi_gui
        WHEN N'CUSTOMER' THEN N'Mình cần nhân viên hỗ trợ thêm về sản phẩm và đơn hàng.'
        ELSE N'Yêu cầu đã được ghi nhận và chuyển tới nhân viên đang trong ca.'
    END
FROM dbo.Tin_nhan_ho_tro message
JOIN dbo.Ho_tro_chat chat ON chat.id = message.id_ho_tro_chat
WHERE chat.ma_phien LIKE N'AUG-CHAT-%';

UPDATE request
SET request.ly_do = CASE request.loai_yeu_cau
        WHEN N'DOI' THEN N'Cần đổi sang size phù hợp hơn.'
        ELSE N'Sản phẩm không phù hợp với dịp sử dụng.'
    END,
    request.tinh_trang_hang = N'Sản phẩm còn nguyên tem, chưa qua giặt và có ảnh tình trạng kèm theo.',
    request.ghi_chu_nhan_vien = CASE
        WHEN request.trang_thai IN (N'DA_DOI', N'DA_HOAN_TIEN') THEN N'Đã hoàn tất xử lý dữ liệu mẫu.'
        ELSE N'Đang chờ bước xử lý tiếp theo.'
    END,
    request.ngay_tao = DATEADD(MINUTE, 30, order_row.ngay_tao),
    request.ngay_duyet = CASE WHEN order_row.trang_thai = 9 THEN DATEADD(MINUTE, 60, order_row.ngay_tao) ELSE NULL END,
    request.ngay_nhan_hang = CASE WHEN order_row.trang_thai = 9 THEN DATEADD(MINUTE, 90, order_row.ngay_tao) ELSE NULL END,
    request.ngay_hoan_tat = CASE WHEN order_row.trang_thai = 9 THEN DATEADD(MINUTE, 120, order_row.ngay_tao) ELSE NULL END
FROM dbo.Yeu_cau_doi_tra request
JOIN dbo.Hoa_don order_row ON order_row.id = request.id_hoa_don
WHERE order_row.ma_hoa_don LIKE N'HDAUG26%';

UPDATE return_image
SET return_image.ngay_tao = DATEADD(MINUTE, 2, request.ngay_tao)
FROM dbo.Anh_doi_tra return_image
JOIN dbo.Yeu_cau_doi_tra request ON request.id = return_image.id_yeu_cau
JOIN dbo.Hoa_don order_row ON order_row.id = request.id_hoa_don
WHERE order_row.ma_hoa_don LIKE N'HDAUG26%';

UPDATE movement
SET movement.nguoi_thuc_hien = N'Hệ thống dữ liệu mẫu',
    movement.ghi_chu = N'Tổng hợp lượng bán của dữ liệu tháng 8/2026',
    movement.ngay_tao = CAST('2026-08-06T22:30:00' AS datetime2)
FROM dbo.Bien_dong_ton_kho movement
WHERE movement.loai_bien_dong = N'BAN_HANG_DEMO_T8'
  AND movement.ma_tham_chieu = N'AUGUST-2026-SEED';

UPDATE address
SET address.tinh_thanh_pho = N'Hà Nội',
    address.quan_huyen = N'Quận Cầu Giấy',
    address.xa_phuong = N'Phường Dịch Vọng',
    address.duong = N'Địa chỉ phụ ' + customer.ma_khach_hang
FROM dbo.Dia_chi address
JOIN dbo.Khach_hang customer ON customer.id = address.id_khach_hang
WHERE address.mac_dinh = 0
  AND customer.ma_khach_hang IS NOT NULL
  AND RIGHT(address.duong, LEN(customer.ma_khach_hang)) = customer.ma_khach_hang;

-- Mixed August shift states make confirm/unavailable/admin-review screens
-- demonstrable without corrupting check-in/check-out history.
UPDATE dbo.Lich_lam_viec
SET trang_thai = CASE
        WHEN DAY(ngay_lam) % 13 = 0 THEN 2
        WHEN DAY(ngay_lam) % 11 = 0 THEN 3
        WHEN DAY(ngay_lam) % 7 = 0 THEN 0
        ELSE 1
    END,
    thoi_gian_xac_nhan = CASE
        WHEN DAY(ngay_lam) % 7 <> 0 AND DAY(ngay_lam) % 11 <> 0 AND DAY(ngay_lam) % 13 <> 0
            THEN DATEADD(DAY, -3, CAST(ngay_lam AS datetime2))
        ELSE NULL
    END,
    ly_do_bao_ban = CASE
        WHEN DAY(ngay_lam) % 13 = 0 THEN N'Có lịch học và đã báo trước khi xác nhận ca.'
        WHEN DAY(ngay_lam) % 11 = 0 THEN N'Phát sinh việc gia đình, xin admin duyệt báo bận.'
        ELSE NULL
    END,
    thoi_gian_bao_ban = CASE
        WHEN DAY(ngay_lam) % 13 = 0 OR DAY(ngay_lam) % 11 = 0
            THEN DATEADD(DAY, -2, CAST(ngay_lam AS datetime2))
        ELSE NULL
    END,
    phan_hoi_bao_ban = CASE
        WHEN DAY(ngay_lam) % 13 = 0 THEN N'Đã đọc lý do và chấp nhận yêu cầu báo bận.'
        ELSE NULL
    END,
    thoi_gian_duyet = CASE
        WHEN DAY(ngay_lam) % 13 = 0 THEN DATEADD(DAY, -1, CAST(ngay_lam AS datetime2))
        ELSE NULL
    END,
    nguoi_duyet = CASE WHEN DAY(ngay_lam) % 13 = 0 THEN N'Admin' ELSE NULL END
WHERE ngay_lam BETWEEN '2026-08-01' AND '2026-08-31';
GO

-- Normalize legacy busy requests created before admin review became mandatory.
UPDATE dbo.Lich_lam_viec
SET ly_do_bao_ban = COALESCE(NULLIF(LTRIM(RTRIM(ly_do_bao_ban)), N''), N'Yêu cầu báo bận được chuyển đổi từ dữ liệu phiên bản cũ.'),
    thoi_gian_bao_ban = COALESCE(thoi_gian_bao_ban, CAST(ngay_lam AS datetime2)),
    phan_hoi_bao_ban = COALESCE(NULLIF(LTRIM(RTRIM(phan_hoi_bao_ban)), N''), N'Đã đọc lý do và chấp nhận yêu cầu báo bận.'),
    thoi_gian_duyet = COALESCE(thoi_gian_duyet, DATEADD(MINUTE, 1, COALESCE(thoi_gian_bao_ban, CAST(ngay_lam AS datetime2)))),
    nguoi_duyet = COALESCE(NULLIF(LTRIM(RTRIM(nguoi_duyet)), N''), N'Admin')
WHERE trang_thai = 2;

UPDATE dbo.Lich_lam_viec
SET ly_do_bao_ban = COALESCE(NULLIF(LTRIM(RTRIM(ly_do_bao_ban)), N''), N'Yêu cầu báo bận được chuyển đổi từ dữ liệu phiên bản cũ.'),
    thoi_gian_bao_ban = COALESCE(thoi_gian_bao_ban, CAST(ngay_lam AS datetime2)),
    phan_hoi_bao_ban = NULL,
    thoi_gian_duyet = NULL,
    nguoi_duyet = NULL
WHERE trang_thai = 3;
GO

-- Final one-price invariant. gia_ban_goc is retained only because older local
-- databases declare it NOT NULL; backend and frontend never expose it.
UPDATE dbo.san_pham_chi_tiet
SET gia_ban_goc = gia_ban,
    phan_tram_giam = 0
WHERE gia_ban IS NOT NULL;
GO

UPDATE dbo.Nhan_vien
SET ho_va_ten = N'Admin'
WHERE ten_nguoi_dung = N'admin';
GO

-- Lucky wheel is intentionally isolated from vouchers, product stock and order
-- history. Orders are read only to verify eligibility; all outcomes live here.
IF OBJECT_ID(N'dbo.Vong_quay_may_man', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.Vong_quay_may_man (
        id int IDENTITY(1,1) NOT NULL CONSTRAINT PK_Vong_quay_may_man PRIMARY KEY,
        ma_chien_dich nvarchar(50) NOT NULL,
        ten_chien_dich nvarchar(150) NOT NULL,
        mo_ta nvarchar(500) NULL,
        gia_tri_don_toi_thieu decimal(15,2) NOT NULL,
        ngay_bat_dau datetime2(7) NOT NULL,
        ngay_ket_thuc datetime2(7) NOT NULL,
        trang_thai tinyint NOT NULL CONSTRAINT DF_Vong_quay_trang_thai DEFAULT 1,
        ngay_tao datetime2(7) NOT NULL CONSTRAINT DF_Vong_quay_ngay_tao DEFAULT SYSDATETIME(),
        CONSTRAINT UQ_Vong_quay_ma UNIQUE (ma_chien_dich),
        CONSTRAINT CK_Vong_quay_gia_tri CHECK (gia_tri_don_toi_thieu > 0),
        CONSTRAINT CK_Vong_quay_thoi_gian CHECK (ngay_ket_thuc > ngay_bat_dau),
        CONSTRAINT CK_Vong_quay_trang_thai CHECK (trang_thai IN (0, 1))
    );
END;

IF OBJECT_ID(N'dbo.Phan_thuong_vong_quay', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.Phan_thuong_vong_quay (
        id int IDENTITY(1,1) NOT NULL CONSTRAINT PK_Phan_thuong_vong_quay PRIMARY KEY,
        id_chien_dich int NOT NULL,
        ten_phan_thuong nvarchar(150) NOT NULL,
        loai_phan_thuong nvarchar(20) NOT NULL,
        so_luong_ban_dau int NULL,
        so_luong_con int NULL,
        trong_so int NOT NULL,
        mau_hien_thi varchar(7) NOT NULL,
        bieu_tuong varchar(50) NOT NULL,
        anh_bieu_tuong nvarchar(500) NULL,
        thu_tu int NOT NULL CONSTRAINT DF_Phan_thuong_thu_tu DEFAULT 0,
        trang_thai tinyint NOT NULL CONSTRAINT DF_Phan_thuong_trang_thai DEFAULT 1,
        ngay_tao datetime2(7) NOT NULL CONSTRAINT DF_Phan_thuong_ngay_tao DEFAULT SYSDATETIME(),
        CONSTRAINT FK_Phan_thuong_Vong_quay FOREIGN KEY (id_chien_dich) REFERENCES dbo.Vong_quay_may_man(id),
        CONSTRAINT CK_Phan_thuong_loai CHECK (loai_phan_thuong IN (N'VAT_PHAM', N'KHONG_TRUNG')),
        CONSTRAINT CK_Phan_thuong_so_luong CHECK (
            (loai_phan_thuong = N'VAT_PHAM' AND so_luong_ban_dau >= 0 AND so_luong_con >= 0 AND so_luong_con <= so_luong_ban_dau)
            OR (loai_phan_thuong = N'KHONG_TRUNG' AND so_luong_ban_dau IS NULL AND so_luong_con IS NULL)
        ),
        CONSTRAINT CK_Phan_thuong_trong_so CHECK (trong_so > 0),
        CONSTRAINT CK_Phan_thuong_trang_thai CHECK (trang_thai IN (0, 1))
    );
END;

IF COL_LENGTH(N'dbo.Phan_thuong_vong_quay', N'anh_bieu_tuong') IS NULL
    ALTER TABLE dbo.Phan_thuong_vong_quay ADD anh_bieu_tuong nvarchar(500) NULL;
GO

IF OBJECT_ID(N'dbo.Luot_quay_may_man', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.Luot_quay_may_man (
        id int IDENTITY(1,1) NOT NULL CONSTRAINT PK_Luot_quay_may_man PRIMARY KEY,
        id_chien_dich int NOT NULL,
        id_phan_thuong int NOT NULL,
        ma_hoa_don nvarchar(80) NOT NULL,
        ten_khach_hang nvarchar(150) NOT NULL,
        so_dien_thoai nvarchar(20) NOT NULL,
        email_khach_hang nvarchar(150) NULL,
        gia_tri_don decimal(15,2) NOT NULL,
        ten_ket_qua nvarchar(150) NOT NULL,
        trung_thuong bit NOT NULL,
        ma_nhan_thuong nvarchar(30) NOT NULL,
        trang_thai_nhan nvarchar(20) NOT NULL,
        ngay_quay datetime2(7) NOT NULL,
        ngay_trao datetime2(7) NULL,
        nguoi_trao nvarchar(150) NULL,
        CONSTRAINT FK_Luot_quay_Chien_dich FOREIGN KEY (id_chien_dich) REFERENCES dbo.Vong_quay_may_man(id),
        CONSTRAINT FK_Luot_quay_Phan_thuong FOREIGN KEY (id_phan_thuong) REFERENCES dbo.Phan_thuong_vong_quay(id),
        CONSTRAINT UQ_Luot_quay_chien_dich_don UNIQUE (id_chien_dich, ma_hoa_don),
        CONSTRAINT UQ_Luot_quay_ma_nhan UNIQUE (ma_nhan_thuong),
        CONSTRAINT CK_Luot_quay_gia_tri CHECK (gia_tri_don >= 0),
        CONSTRAINT CK_Luot_quay_trang_thai CHECK (trang_thai_nhan IN (N'KHONG_TRUNG', N'CHO_NHAN', N'DA_TRA'))
    );
END;

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.Vong_quay_may_man') AND name = N'IX_Vong_quay_active_time')
    CREATE INDEX IX_Vong_quay_active_time ON dbo.Vong_quay_may_man(trang_thai, ngay_bat_dau, ngay_ket_thuc);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.Phan_thuong_vong_quay') AND name = N'IX_Phan_thuong_chien_dich')
    CREATE INDEX IX_Phan_thuong_chien_dich ON dbo.Phan_thuong_vong_quay(id_chien_dich, trang_thai, thu_tu) INCLUDE (trong_so, so_luong_con);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.Luot_quay_may_man') AND name = N'IX_Luot_quay_trang_thai')
    CREATE INDEX IX_Luot_quay_trang_thai ON dbo.Luot_quay_may_man(id_chien_dich, trang_thai_nhan, ngay_quay DESC);

MERGE dbo.Vong_quay_may_man AS target
USING (VALUES (
    N'VQ-THANG8-2026',
    N'Vòng quay rực rỡ tháng 8',
    N'Mỗi đơn đã giao thành công từ 1.000.000đ nhận một lượt quay. Quà tặng là hiện vật và nhận tại showroom Zestia.',
    CAST(1000000 AS decimal(15,2)),
    CAST('2026-08-01T00:00:00' AS datetime2),
    CAST('2026-08-06T23:59:59' AS datetime2),
    CAST(1 AS tinyint)
)) AS source(ma_chien_dich, ten_chien_dich, mo_ta, gia_tri_don_toi_thieu, ngay_bat_dau, ngay_ket_thuc, trang_thai)
ON target.ma_chien_dich = source.ma_chien_dich
WHEN MATCHED THEN UPDATE SET
    ten_chien_dich = source.ten_chien_dich,
    mo_ta = source.mo_ta,
    gia_tri_don_toi_thieu = source.gia_tri_don_toi_thieu,
    ngay_bat_dau = source.ngay_bat_dau,
    ngay_ket_thuc = source.ngay_ket_thuc,
    trang_thai = source.trang_thai
WHEN NOT MATCHED THEN INSERT (
    ma_chien_dich, ten_chien_dich, mo_ta, gia_tri_don_toi_thieu,
    ngay_bat_dau, ngay_ket_thuc, trang_thai, ngay_tao
) VALUES (
    source.ma_chien_dich, source.ten_chien_dich, source.mo_ta, source.gia_tri_don_toi_thieu,
    source.ngay_bat_dau, source.ngay_ket_thuc, source.trang_thai, CAST('2026-08-01T09:00:00' AS datetime2)
);

DECLARE @aug_lucky_campaign int = (
    SELECT id FROM dbo.Vong_quay_may_man WHERE ma_chien_dich = N'VQ-THANG8-2026'
);

MERGE dbo.Phan_thuong_vong_quay AS target
USING (VALUES
    (@aug_lucky_campaign, N'iPhone 16', N'VAT_PHAM', 1, 1, N'#17171A', N'bi-phone', CAST(NULL AS nvarchar(500)), 1, CAST(1 AS tinyint)),
    (@aug_lucky_campaign, N'Nước hoa mini Zestia', N'VAT_PHAM', 8, 7, N'#7C2F4A', N'bi-droplet', CAST(NULL AS nvarchar(500)), 2, CAST(1 AS tinyint)),
    (@aug_lucky_campaign, N'Balo thời trang Zestia', N'VAT_PHAM', 12, 12, N'#C64F47', N'bi-backpack', CAST(NULL AS nvarchar(500)), 3, CAST(1 AS tinyint)),
    (@aug_lucky_campaign, N'Túi tote Zestia', N'VAT_PHAM', 20, 16, N'#2F6F73', N'bi-bag-heart', CAST(NULL AS nvarchar(500)), 4, CAST(1 AS tinyint)),
    (@aug_lucky_campaign, N'Khăn lụa Zestia', N'VAT_PHAM', 14, 10, N'#4F627E', N'bi-gem', CAST(NULL AS nvarchar(500)), 5, CAST(1 AS tinyint)),
    (@aug_lucky_campaign, N'Gương trang điểm', N'VAT_PHAM', 25, 18, N'#935F79', N'bi-circle-half', CAST(NULL AS nvarchar(500)), 6, CAST(1 AS tinyint)),
    (@aug_lucky_campaign, N'Ô gấp Zestia', N'VAT_PHAM', 30, 25, N'#5D6A58', N'bi-umbrella', CAST(NULL AS nvarchar(500)), 7, CAST(1 AS tinyint)),
    (@aug_lucky_campaign, N'Chúc bạn may mắn lần sau', N'KHONG_TRUNG', NULL, 70, N'#A15A52', N'bi-stars', CAST(NULL AS nvarchar(500)), 8, CAST(1 AS tinyint))
) AS source(id_chien_dich, ten_phan_thuong, loai_phan_thuong, so_luong_ban_dau, trong_so, mau_hien_thi, bieu_tuong, anh_bieu_tuong, thu_tu, trang_thai)
ON target.id_chien_dich = source.id_chien_dich
AND target.ten_phan_thuong = source.ten_phan_thuong
WHEN MATCHED THEN UPDATE SET
    loai_phan_thuong = source.loai_phan_thuong,
    trong_so = source.trong_so,
    mau_hien_thi = source.mau_hien_thi,
    bieu_tuong = source.bieu_tuong,
    thu_tu = source.thu_tu,
    trang_thai = source.trang_thai
WHEN NOT MATCHED THEN INSERT (
    id_chien_dich, ten_phan_thuong, loai_phan_thuong,
    so_luong_ban_dau, so_luong_con, trong_so, mau_hien_thi,
    bieu_tuong, anh_bieu_tuong, thu_tu, trang_thai, ngay_tao
) VALUES (
    source.id_chien_dich, source.ten_phan_thuong, source.loai_phan_thuong,
    source.so_luong_ban_dau, source.so_luong_ban_dau, source.trong_so, source.mau_hien_thi,
    source.bieu_tuong, source.anh_bieu_tuong, source.thu_tu, source.trang_thai, CAST('2026-08-01T09:00:00' AS datetime2)
);
GO

-- A successful final message is emitted only after every invariant passes.
IF (SELECT COUNT(*) FROM dbo.Vai_tro) <> 2
   OR EXISTS (SELECT 1 FROM dbo.Vai_tro WHERE ten_vai_tro NOT IN (N'Admin', N'Nhân viên'))
    THROW 51001, N'Database chỉ được có vai trò Admin và Nhân viên.', 1;
IF EXISTS (SELECT 1 FROM dbo.san_pham_chi_tiet WHERE so_luong < 0)
    THROW 51003, N'Tồn kho biến thể không được âm.', 1;
IF EXISTS (SELECT 1 FROM dbo.san_pham_chi_tiet WHERE gia_ban IS NOT NULL AND (gia_ban_goc IS NULL OR gia_ban_goc <> gia_ban OR ISNULL(phan_tram_giam, 0) <> 0))
    THROW 51004, N'Dữ liệu sản phẩm phải dùng một giá bán duy nhất.', 1;
IF EXISTS (
    SELECT 1 FROM dbo.Hoa_don
    WHERE UPPER(ISNULL(hinh_thuc_thanh_toan, N'')) IN (N'MOMO', N'ZALOPAY')
      AND ISNULL(da_thanh_toan, 0) = 0
      AND trang_thai IN (1, 2, 3, 4, 6, 8, 9)
)
    THROW 51005, N'Có đơn thanh toán online chưa thành công nhưng đã đi tiếp trong quy trình.', 1;
IF EXISTS (
    SELECT 1 FROM dbo.Hoa_don
    WHERE hinh_thuc_nhan_hang = 0
      AND (id_nhan_vien IS NULL OR dia_chi_giao_hang <> N'Mua trực tiếp tại cửa hàng')
)
    THROW 51006, N'Đơn tại quầy phải có nhân viên tạo và phương thức nhận trực tiếp.', 1;
IF EXISTS (
    SELECT 1 FROM dbo.Danh_gia dg
    WHERE NOT EXISTS (
        SELECT 1
        FROM dbo.Hoa_don hd
        JOIN dbo.Hoa_don_chi_tiet hdct ON hdct.id_hoa_don = hd.id
        JOIN dbo.san_pham_chi_tiet vct ON vct.id = hdct.id_san_pham_chi_tiet
        WHERE hd.id = dg.id_hoa_don
          AND hd.id_khach_hang = dg.id_khach_hang
          AND hd.trang_thai = 4
          AND vct.id_san_pham = dg.id_san_pham
    )
)
    THROW 51007, N'Có đánh giá không thuộc giao dịch mua hàng đã hoàn thành.', 1;
IF EXISTS (SELECT ma_hoa_don FROM dbo.Hoa_don GROUP BY ma_hoa_don HAVING COUNT(*) > 1)
    THROW 51008, N'Mã hóa đơn đang bị trùng.', 1;
IF EXISTS (SELECT id_khach_hang FROM dbo.Gio_hang GROUP BY id_khach_hang HAVING COUNT(*) > 1)
    THROW 51009, N'Một khách hàng đang có nhiều hơn một giỏ hàng.', 1;
IF EXISTS (SELECT 1 FROM dbo.Hoa_don WHERE trang_thai IN (5, 6, 7, 9) AND ISNULL(da_hoan_ton_kho, 0) = 0)
    THROW 51016, N'Có đơn kết thúc chưa được đối soát tồn kho/voucher.', 1;
IF EXISTS (
    SELECT 1 FROM dbo.Bien_dong_ton_kho
    WHERE loai_bien_dong = N'HOAN_DON'
      AND ma_tham_chieu LIKE N'HDAUG26%'
)
    THROW 51040, N'Dữ liệu refresh còn sót bút toán hoàn kho của đơn tháng 8.', 1;
IF EXISTS (SELECT 1 FROM dbo.Lich_lam_viec WHERE gio_check_out IS NOT NULL AND (gio_check_in IS NULL OR gio_check_out < gio_check_in))
    THROW 51017, N'Dữ liệu check-in/check-out của ca làm không hợp lệ.', 1;
IF EXISTS (SELECT 1 FROM dbo.Lich_lam_viec WHERE gio_ket_thuc <= gio_bat_dau)
    THROW 51018, N'Giờ kết thúc ca làm phải sau giờ bắt đầu.', 1;
IF EXISTS (
    SELECT 1 FROM dbo.Lich_lam_viec
    WHERE trang_thai IN (2, 3)
      AND (NULLIF(LTRIM(RTRIM(ly_do_bao_ban)), N'') IS NULL OR thoi_gian_bao_ban IS NULL)
)
    THROW 51034, N'Ca báo bận phải có lý do và thời gian gửi yêu cầu.', 1;
IF EXISTS (
    SELECT 1 FROM dbo.Lich_lam_viec
    WHERE trang_thai = 2
      AND (thoi_gian_duyet IS NULL OR NULLIF(LTRIM(RTRIM(nguoi_duyet)), N'') IS NULL)
)
    THROW 51035, N'Ca đã được nghỉ bận phải có người duyệt và thời gian duyệt.', 1;
IF EXISTS (
    SELECT 1 FROM dbo.Lich_lam_viec
    WHERE trang_thai = 3
      AND (thoi_gian_duyet IS NOT NULL OR nguoi_duyet IS NOT NULL)
)
    THROW 51036, N'Ca chờ duyệt báo bận không được có kết quả duyệt trước.', 1;
IF EXISTS (
    SELECT 1
    FROM dbo.Lich_lam_viec first_shift
    JOIN dbo.Lich_lam_viec second_shift
      ON second_shift.id_nhan_vien = first_shift.id_nhan_vien
     AND second_shift.ngay_lam = first_shift.ngay_lam
     AND second_shift.id > first_shift.id
     AND second_shift.gio_bat_dau < first_shift.gio_ket_thuc
     AND second_shift.gio_ket_thuc > first_shift.gio_bat_dau
)
    THROW 51037, N'Lịch làm việc đang có ca trùng thời gian.', 1;
IF EXISTS (SELECT ma_khach_hang FROM dbo.Khach_hang WHERE ma_khach_hang IS NOT NULL GROUP BY ma_khach_hang HAVING COUNT(*) > 1)
    THROW 51010, N'Mã khách hàng đang bị trùng.', 1;
IF EXISTS (SELECT email FROM dbo.Khach_hang WHERE email IS NOT NULL GROUP BY email HAVING COUNT(*) > 1)
    THROW 51011, N'Email khách hàng đang bị trùng.', 1;
IF EXISTS (SELECT so_dien_thoai FROM dbo.Khach_hang WHERE so_dien_thoai IS NOT NULL GROUP BY so_dien_thoai HAVING COUNT(*) > 1)
    THROW 51012, N'Số điện thoại khách hàng đang bị trùng.', 1;
IF EXISTS (SELECT ma_yeu_cau FROM dbo.Hoa_don WHERE ma_yeu_cau IS NOT NULL GROUP BY ma_yeu_cau HAVING COUNT(*) > 1)
    THROW 51013, N'Mã yêu cầu checkout đang bị trùng.', 1;
IF EXISTS (SELECT ma_giao_dich_cong FROM dbo.Hoa_don WHERE ma_giao_dich_cong IS NOT NULL GROUP BY ma_giao_dich_cong HAVING COUNT(*) > 1)
    THROW 51014, N'Mã giao dịch cổng thanh toán đang bị trùng.', 1;
IF EXISTS (
    SELECT 1
    FROM dbo.san_pham v
    JOIN dbo.loai_san_pham lv ON lv.id = v.id_loai_san_pham
    WHERE (v.ma_san_pham LIKE N'VCS%' AND lv.ten_loai_san_pham <> N'Váy công sở')
       OR (v.ma_san_pham LIKE N'VCU%' AND lv.ten_loai_san_pham <> N'Váy cưới')
       OR (v.ma_san_pham LIKE N'VDT%' AND lv.ten_loai_san_pham <> N'Váy dự tiệc')
)
    THROW 51015, N'Phân loại sản phẩm không khớp với mã sản phẩm.', 1;
IF EXISTS (
    SELECT 1
    FROM dbo.loai_san_pham
    WHERE ten_loai_san_pham LIKE N'%Ã%'
       OR ten_loai_san_pham LIKE N'%áº%'
       OR ten_loai_san_pham LIKE N'%á»%'
)
    THROW 51029, N'Danh mục sản phẩm còn dữ liệu lỗi mã hóa UTF-8.', 1;
IF OBJECT_ID(N'dbo.Pos_phien_giu_hang', N'U') IS NULL
   OR OBJECT_ID(N'dbo.Pos_chi_tiet_giu_hang', N'U') IS NULL
    THROW 51022, N'Thiếu bảng giữ tồn cho giỏ POS.', 1;
IF OBJECT_ID(N'dbo.Vong_quay_may_man', N'U') IS NULL
   OR OBJECT_ID(N'dbo.Phan_thuong_vong_quay', N'U') IS NULL
   OR OBJECT_ID(N'dbo.Luot_quay_may_man', N'U') IS NULL
    THROW 51030, N'Thiếu cấu trúc dữ liệu vòng quay may mắn.', 1;
IF NOT EXISTS (
    SELECT 1 FROM dbo.Vong_quay_may_man campaign
    WHERE campaign.ma_chien_dich = N'VQ-THANG8-2026'
      AND campaign.gia_tri_don_toi_thieu = 1000000
)
    THROW 51031, N'Dữ liệu chiến dịch vòng quay tháng 8 chưa đúng.', 1;
IF COL_LENGTH(N'dbo.Phan_thuong_vong_quay', N'anh_bieu_tuong') IS NULL
    THROW 51033, N'Thiếu cột ảnh biểu tượng phần thưởng vòng quay.', 1;
IF (
    SELECT COUNT(*)
    FROM dbo.Phan_thuong_vong_quay prize
    JOIN dbo.Vong_quay_may_man campaign ON campaign.id = prize.id_chien_dich
    WHERE campaign.ma_chien_dich = N'VQ-THANG8-2026' AND prize.trang_thai = 1
) < 8
    THROW 51034, N'Chiến dịch vòng quay tháng 8 phải có ít nhất 8 lựa chọn quà.', 1;
IF EXISTS (
    SELECT 1 FROM dbo.Phan_thuong_vong_quay
    WHERE anh_bieu_tuong IS NOT NULL
      AND (anh_bieu_tuong NOT LIKE N'/images/lucky-wheel/%' OR anh_bieu_tuong LIKE N'%..%' OR anh_bieu_tuong LIKE N'%\%')
)
    THROW 51035, N'Đường dẫn ảnh biểu tượng vòng quay không hợp lệ.', 1;
IF EXISTS (
    SELECT 1 FROM dbo.Luot_quay_may_man
    GROUP BY id_chien_dich, ma_hoa_don
    HAVING COUNT(*) > 1
)
    THROW 51032, N'Một đơn hàng đang có nhiều hơn một lượt quay trong cùng chiến dịch.', 1;
IF (SELECT COUNT(*) FROM dbo.Hoa_don WHERE ma_hoa_don LIKE N'HDAUG26%') <> 124
    THROW 51023, N'Dữ liệu tháng 8 phải có đúng 124 đơn hàng mẫu.', 1;
IF (
    SELECT COUNT(DISTINCT CAST(ngay_tao AS date))
    FROM dbo.Hoa_don
    WHERE ma_hoa_don LIKE N'HDAUG26%'
) <> 6
    THROW 51024, N'Dữ liệu đơn hàng tháng 8 phải phủ đủ từ 01/08 đến 06/08/2026.', 1;

DECLARE @demo_data_cutoff datetime2(7) = CAST('2026-08-07T00:00:00' AS datetime2);
IF EXISTS (
    SELECT order_row.id FROM dbo.Hoa_don order_row
    WHERE order_row.ma_hoa_don LIKE N'HDAUG26%' AND order_row.ngay_tao >= @demo_data_cutoff
    UNION ALL
    SELECT payment.id FROM dbo.Lich_su_thanh_toan payment
    JOIN dbo.Hoa_don order_row ON order_row.id = payment.id_hoa_don
    WHERE order_row.ma_hoa_don LIKE N'HDAUG26%' AND payment.ngay_tao >= @demo_data_cutoff
    UNION ALL
    SELECT tracking.id FROM dbo.Lich_su_tracking tracking
    JOIN dbo.Hoa_don order_row ON order_row.id = tracking.id_hoa_don
    WHERE order_row.ma_hoa_don LIKE N'HDAUG26%' AND tracking.ngay_cap_nhat >= @demo_data_cutoff
    UNION ALL
    SELECT audit.id FROM dbo.Hoa_don_audit_log audit
    JOIN dbo.Hoa_don order_row ON order_row.id = audit.id_hoa_don
    WHERE order_row.ma_hoa_don LIKE N'HDAUG26%' AND audit.ngay_tao >= @demo_data_cutoff
    UNION ALL
    SELECT review.id FROM dbo.Danh_gia review
    JOIN dbo.Hoa_don order_row ON order_row.id = review.id_hoa_don
    WHERE order_row.ma_hoa_don LIKE N'HDAUG26%' AND review.ngay_tao >= @demo_data_cutoff
    UNION ALL
    SELECT request.id FROM dbo.Yeu_cau_doi_tra request
    JOIN dbo.Hoa_don order_row ON order_row.id = request.id_hoa_don
    WHERE order_row.ma_hoa_don LIKE N'HDAUG26%'
      AND (request.ngay_tao >= @demo_data_cutoff OR request.ngay_duyet >= @demo_data_cutoff
           OR request.ngay_nhan_hang >= @demo_data_cutoff OR request.ngay_hoan_tat >= @demo_data_cutoff)
    UNION ALL
    SELECT return_image.id FROM dbo.Anh_doi_tra return_image
    JOIN dbo.Yeu_cau_doi_tra request ON request.id = return_image.id_yeu_cau
    JOIN dbo.Hoa_don order_row ON order_row.id = request.id_hoa_don
    WHERE order_row.ma_hoa_don LIKE N'HDAUG26%' AND return_image.ngay_tao >= @demo_data_cutoff
    UNION ALL
    SELECT movement.id FROM dbo.Bien_dong_ton_kho movement
    WHERE movement.ma_tham_chieu = N'AUGUST-2026-SEED' AND movement.ngay_tao >= @demo_data_cutoff
    UNION ALL
    SELECT chat.id FROM dbo.Ho_tro_chat chat
    WHERE chat.ma_phien LIKE N'AUG-CHAT-%'
      AND (chat.ngay_tao >= @demo_data_cutoff OR chat.ngay_nhan >= @demo_data_cutoff
           OR chat.ngay_dong >= @demo_data_cutoff OR chat.ngay_cap_nhat >= @demo_data_cutoff)
    UNION ALL
    SELECT message.id FROM dbo.Tin_nhan_ho_tro message
    JOIN dbo.Ho_tro_chat chat ON chat.id = message.id_ho_tro_chat
    WHERE chat.ma_phien LIKE N'AUG-CHAT-%' AND message.ngay_tao >= @demo_data_cutoff
    UNION ALL
    SELECT subscriber.id FROM dbo.Newsletter_subscriber subscriber
    WHERE (subscriber.ngay_dang_ky >= @demo_data_cutoff OR subscriber.ngay_cap_nhat >= @demo_data_cutoff)
      AND (subscriber.ngay_dang_ky < '2026-09-01' OR subscriber.ngay_cap_nhat < '2026-09-01')
    UNION ALL
    SELECT favorite.id FROM dbo.San_pham_yeu_thich favorite
    WHERE favorite.ngay_tao >= @demo_data_cutoff AND favorite.ngay_tao < '2026-09-01'
    UNION ALL
    SELECT history.id FROM dbo.Lich_su_xem history
    WHERE history.ngay_xem >= @demo_data_cutoff AND history.ngay_xem < '2026-09-01'
    UNION ALL
    SELECT notification.id FROM dbo.Thong_bao notification
    WHERE notification.tieu_de = N'Lịch hoạt động showroom tháng 8' AND notification.ngay_tao >= @demo_data_cutoff
    UNION ALL
    SELECT voucher.id FROM dbo.Giam_gia voucher
    WHERE voucher.ma_giam_gia IN (N'ZESTIA10', N'ZESTIA50', N'FREESHIP', N'SUMMER20', N'AUGUST10', N'AUGUST50', N'AUGUSTVIP', N'BACK2WORK', N'AUGPOS', N'AUGFREESHIP')
      AND voucher.ngay_ket_thuc >= @demo_data_cutoff
    UNION ALL
    SELECT campaign.id FROM dbo.Dot_khuyen_mai campaign
    WHERE campaign.ma_dot IN (N'AUGUST26', N'AUGOFFICE15', N'AUGPARTY120')
      AND (campaign.ngay_bat_dau >= @demo_data_cutoff OR campaign.ngay_ket_thuc >= @demo_data_cutoff)
    UNION ALL
    SELECT wheel.id FROM dbo.Vong_quay_may_man wheel
    WHERE wheel.ma_chien_dich = N'VQ-THANG8-2026'
      AND (wheel.ngay_bat_dau >= @demo_data_cutoff OR wheel.ngay_ket_thuc >= @demo_data_cutoff)
)
    THROW 51038, N'Dữ liệu demo nghiệp vụ không được có thời điểm sau 06/08/2026; lịch làm việc là ngoại lệ.', 1;
IF (SELECT COUNT(*) FROM dbo.Lich_lam_viec WHERE ngay_lam BETWEEN '2026-08-01' AND '2026-08-31') < 31
   OR (SELECT COUNT(DISTINCT ngay_lam) FROM dbo.Lich_lam_viec WHERE ngay_lam BETWEEN '2026-08-01' AND '2026-08-31') <> 31
    THROW 51027, N'Dữ liệu ca làm tháng 8 phải phủ đủ 31 ngày.', 1;
IF EXISTS (
    SELECT id_nhan_vien, ngay_lam, ca_lam
    FROM dbo.Lich_lam_viec
    GROUP BY id_nhan_vien, ngay_lam, ca_lam
    HAVING COUNT(*) > 1
)
    THROW 51028, N'Lịch làm việc đang có ca trùng nhân viên, ngày và tên ca.', 1;
IF EXISTS (
    SELECT 1
    FROM dbo.Hoa_don order_row
    WHERE order_row.ma_hoa_don LIKE N'HDAUG26%'
      AND NOT EXISTS (
          SELECT 1 FROM dbo.Hoa_don_chi_tiet detail
          WHERE detail.id_hoa_don = order_row.id
      )
)
    THROW 51025, N'Có đơn hàng tháng 8 chưa có chi tiết sản phẩm.', 1;
IF EXISTS (
    SELECT 1
    FROM dbo.Hoa_don order_row
    WHERE order_row.ma_hoa_don LIKE N'HDAUG26%'
      AND order_row.da_thanh_toan = 1
      AND NOT EXISTS (
          SELECT 1 FROM dbo.Lich_su_thanh_toan payment
          WHERE payment.id_hoa_don = order_row.id
            AND UPPER(payment.trang_thai) = N'SUCCESS'
      )
)
    THROW 51026, N'Có đơn hàng tháng 8 đã thanh toán nhưng thiếu giao dịch thành công.', 1;
-- ===== BẢNG LƯU VẾT AI CHAT (Ai_chat_log) =====
IF OBJECT_ID(N'dbo.Ai_chat_log','U') IS NULL
BEGIN
CREATE TABLE [dbo].[Ai_chat_log] (
  [id] int IDENTITY(1,1) NOT NULL,
  [id_khach_hang] int NULL,
  [mode] nvarchar(50) NOT NULL,
  [user_prompt] nvarchar(1200) NOT NULL,
  [ai_response] nvarchar(max) NOT NULL,
  [model_name] nvarchar(50) NULL,
  [execution_time_ms] int NULL,
  [ngay_tao] datetime2(7) NULL DEFAULT GETDATE(),
  CONSTRAINT [PK_Ai_chat_log] PRIMARY KEY ([id]),
  CONSTRAINT [FK_Ai_chat_log_Khach_hang] FOREIGN KEY ([id_khach_hang]) REFERENCES [dbo].[Khach_hang]([id])
);
END
GO

-- ===== BẢNG LƯU VẾT TÌM BẰNG ẢNH AI (Ai_visual_search_log) =====
IF OBJECT_ID(N'dbo.Ai_visual_search_log','U') IS NULL
BEGIN
CREATE TABLE [dbo].[Ai_visual_search_log] (
  [id] int IDENTITY(1,1) NOT NULL,
  [id_khach_hang] int NULL,
  [image_filename] nvarchar(255) NULL,
  [detected_category] nvarchar(100) NULL,
  [detected_color] nvarchar(100) NULL,
  [matched_product_ids] nvarchar(500) NULL,
  [highest_score] int NULL,
  [ngay_tao] datetime2(7) NULL DEFAULT GETDATE(),
  CONSTRAINT [PK_Ai_visual_search_log] PRIMARY KEY ([id]),
  CONSTRAINT [FK_Ai_visual_search_log_Khach_hang] FOREIGN KEY ([id_khach_hang]) REFERENCES [dbo].[Khach_hang]([id])
);
END
GO

-- ===== BẢNG LƯU BẢN BẢN GỢI Ý PHỐI ĐỒ AI (Ai_recommendations) =====
IF OBJECT_ID(N'dbo.Ai_recommendations','U') IS NULL
BEGIN
CREATE TABLE [dbo].[Ai_recommendations] (
  [id] int IDENTITY(1,1) NOT NULL,
  [id_san_pham_chinh] int NOT NULL,
  [id_san_pham_goi_y] int NOT NULL,
  [loai_goi_y] nvarchar(50) NULL,
  [score_do_phu_hop] decimal(5,2) NULL,
  [ngay_tao] datetime2(7) NULL DEFAULT GETDATE(),
  CONSTRAINT [PK_Ai_recommendations] PRIMARY KEY ([id]),
  CONSTRAINT [FK_Ai_recommendations_SP_Chinh] FOREIGN KEY ([id_san_pham_chinh]) REFERENCES [dbo].[san_pham]([id]),
  CONSTRAINT [FK_Ai_recommendations_SP_GoiY] FOREIGN KEY ([id_san_pham_goi_y]) REFERENCES [dbo].[san_pham]([id])
);
END
GO

IF XACT_STATE() <> 1
    THROW 51099, N'Cài đặt dữ liệu đã bị lỗi và không thể commit. Không có thông báo thành công giả.', 1;
COMMIT TRANSACTION;
SET NOCOUNT OFF;
PRINT N'Zestia demo data refresh completed.';
GO
