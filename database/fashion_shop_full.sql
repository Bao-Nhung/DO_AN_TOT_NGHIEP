-- ============================================================
--  ZESTIA FASHION SHOP DATABASE SCHEMA
--  Du an tot nghiep - He thong quan ly cua hang thoi trang
--  Phien ban: SQL Server (T-SQL)
--  Gop tu 2 script: schema goc + tach bang Gio_hang
-- ============================================================

IF EXISTS (SELECT name FROM sys.databases WHERE name = N'fashion_shop')
    DROP DATABASE fashion_shop;
GO
CREATE DATABASE fashion_shop
    COLLATE Vietnamese_CI_AS;
GO
USE fashion_shop;
GO

-- ============================================================
--  1. NHA CUNG CAP
-- ============================================================
CREATE TABLE Nha_cung_cap (
    id               INT IDENTITY(1,1) PRIMARY KEY,
    ten_nha_cung_cap NVARCHAR(150) NOT NULL,
    dia_chi          NVARCHAR(255),
    so_dien_thoai    NVARCHAR(20),
    email            NVARCHAR(100),
    trang_thai       TINYINT      DEFAULT 1,
    mo_ta            NVARCHAR(MAX),
    ngay_tao         DATETIME2    DEFAULT GETDATE()
);
GO

-- ============================================================
--  2. MAU SAC
-- ============================================================
CREATE TABLE Mau_Sac (
    id          INT IDENTITY(1,1) PRIMARY KEY,
    ten_mau_sac NVARCHAR(100) NOT NULL,
    ma_hex      VARCHAR(7),
    trang_thai  TINYINT       DEFAULT 1,
    ngay_tao    DATETIME2     DEFAULT GETDATE()
);
GO

-- ============================================================
--  3. KICH THUOC
-- ============================================================
CREATE TABLE Kich_Thuoc (
    id             INT IDENTITY(1,1) PRIMARY KEY,
    ten_kich_thuoc NVARCHAR(50) NOT NULL,
    mo_ta          NVARCHAR(MAX),
    trang_thai     TINYINT      DEFAULT 1,
    ngay_tao       DATETIME2    DEFAULT GETDATE()
);
GO

-- ============================================================
--  4. CHAT LIEU
-- ============================================================
CREATE TABLE Chat_lieu (
    id            INT IDENTITY(1,1) PRIMARY KEY,
    ten_chat_lieu NVARCHAR(100) NOT NULL,
    trang_thai    TINYINT       DEFAULT 1,
    mo_ta         NVARCHAR(MAX),
    ngay_tao      DATETIME2     DEFAULT GETDATE()
);
GO

-- ============================================================
--  5. TAI TRO / THUONG HIEU
-- ============================================================
CREATE TABLE Tai_tro (
    id          INT IDENTITY(1,1) PRIMARY KEY,
    ten_tai_tro NVARCHAR(150) NOT NULL,
    trang_thai  TINYINT       DEFAULT 1,
    mo_ta       NVARCHAR(MAX),
    ngay_tao    DATETIME2     DEFAULT GETDATE()
);
GO

-- ============================================================
--  6. LOAI VAY / DANH MUC
-- ============================================================
CREATE TABLE Loai_vay (
    id           INT IDENTITY(1,1) PRIMARY KEY,
    ten_loai_vay NVARCHAR(150) NOT NULL,
    trang_thai   TINYINT       DEFAULT 1,
    mo_ta        NVARCHAR(MAX),
    ngay_tao     DATETIME2     DEFAULT GETDATE()
);
GO

-- ============================================================
--  7. SAN PHAM (Vay)
-- ============================================================
CREATE TABLE Vay (
    id              INT IDENTITY(1,1) PRIMARY KEY,
    id_nha_cung_cap INT,
    id_loai_vay     INT,
    id_chat_lieu    INT,
    id_tai_tro      INT,
    ma_vay          NVARCHAR(50)  NOT NULL,
    ten_vay         NVARCHAR(200) NOT NULL,
    link_youtube    NVARCHAR(500),
    trang_thai      TINYINT       DEFAULT 1,
    mo_ta           NVARCHAR(MAX),
    ngay_tao        DATETIME2     DEFAULT GETDATE(),
    CONSTRAINT UQ_Vay_ma_vay       UNIQUE (ma_vay),
    CONSTRAINT FK_Vay_NhaCungCap   FOREIGN KEY (id_nha_cung_cap) REFERENCES Nha_cung_cap(id),
    CONSTRAINT FK_Vay_LoaiVay      FOREIGN KEY (id_loai_vay)     REFERENCES Loai_vay(id),
    CONSTRAINT FK_Vay_ChatLieu     FOREIGN KEY (id_chat_lieu)    REFERENCES Chat_lieu(id),
    CONSTRAINT FK_Vay_TaiTro       FOREIGN KEY (id_tai_tro)      REFERENCES Tai_tro(id)
);
GO

-- ============================================================
--  8. KHUYEN MAI
-- ============================================================
CREATE TABLE Khuyen_mai (
    id             INT IDENTITY(1,1) PRIMARY KEY,
    ma_khuyen_mai  NVARCHAR(50)  NOT NULL,
    ten_khuyen_mai NVARCHAR(200) NOT NULL,
    phan_tram_giam DECIMAL(5,2)  DEFAULT 0,
    so_tien_giam   DECIMAL(15,2) DEFAULT 0,
    ngay_bat_dau   DATE          NOT NULL,
    ngay_ket_thuc  DATE          NOT NULL,
    mo_ta          NVARCHAR(MAX),
    ngay_tao       DATETIME2     DEFAULT GETDATE(),
    trang_thai     TINYINT       DEFAULT 1,
    CONSTRAINT UQ_KhuyenMai_ma UNIQUE (ma_khuyen_mai)
);
GO

-- ============================================================
--  9. GIAM GIA / VOUCHER
-- ============================================================
CREATE TABLE Giam_gia (
    id               INT IDENTITY(1,1) PRIMARY KEY,
    ma_giam_gia      NVARCHAR(50)  NOT NULL,
    ten_giam_gia     NVARCHAR(200),
    gia_tri_don_toi_thieu DECIMAL(15,2) DEFAULT 0,
    gio_tri_giam     DECIMAL(15,2),
    phan_tram_giam   DECIMAL(5,2),
    giam_toi_da      DECIMAL(15,2),
    so_luong         INT           DEFAULT 0,
    ngay_bat_dau     DATE,
    ngay_ket_thuc    DATE,
    trang_thai       TINYINT       DEFAULT 1,
    ngay_tao         DATETIME2     DEFAULT GETDATE(),
    CONSTRAINT UQ_GiamGia_ma UNIQUE (ma_giam_gia)
);
GO

-- ============================================================
-- 10. BIEN THE SAN PHAM (Vay_chi_tiet)
-- ============================================================
CREATE TABLE Vay_chi_tiet (
    id              INT IDENTITY(1,1) PRIMARY KEY,
    id_vay          INT           NOT NULL,
    id_mau_sac      INT,
    id_kich_thuoc   INT,
    id_khuyen_mai   INT,
    ma_vay_chi_tiet NVARCHAR(80),
    gia_ban_goc     DECIMAL(15,2) NOT NULL,
    gia_ban         DECIMAL(15,2) NOT NULL,
    phan_tram_giam  DECIMAL(5,2)  DEFAULT 0,
    so_luong        INT           DEFAULT 0,
    anh_url         NVARCHAR(500),
    trang_thai      TINYINT       DEFAULT 1,
    ngay_tao        DATETIME2     DEFAULT GETDATE(),
    CONSTRAINT UQ_VayCT_ma         UNIQUE (ma_vay_chi_tiet),
    CONSTRAINT FK_VayCT_Vay        FOREIGN KEY (id_vay)         REFERENCES Vay(id)        ON DELETE CASCADE,
    CONSTRAINT FK_VayCT_MauSac     FOREIGN KEY (id_mau_sac)     REFERENCES Mau_Sac(id),
    CONSTRAINT FK_VayCT_KichThuoc  FOREIGN KEY (id_kich_thuoc)  REFERENCES Kich_Thuoc(id),
    CONSTRAINT FK_VayCT_KhuyenMai  FOREIGN KEY (id_khuyen_mai)  REFERENCES Khuyen_mai(id)
);
GO

-- ============================================================
-- 11. ANH SAN PHAM
-- ============================================================
CREATE TABLE Anh (
    id         INT IDENTITY(1,1) PRIMARY KEY,
    id_vay     INT          NOT NULL,
    anh_url    NVARCHAR(500) NOT NULL,
    trang_thai TINYINT      DEFAULT 1,
    ngay_tao   DATETIME2    DEFAULT GETDATE(),
    CONSTRAINT FK_Anh_Vay FOREIGN KEY (id_vay) REFERENCES Vay(id) ON DELETE CASCADE
);
GO

-- ============================================================
-- 12. KHACH HANG
-- ============================================================
CREATE TABLE Khach_hang (
    id              INT IDENTITY(1,1) PRIMARY KEY,
    ma_khach_hang   NVARCHAR(50),
    ho_va_ten       NVARCHAR(150) NOT NULL,
    gioi_tinh       TINYINT,
    ngay_sinh       DATE,
    so_dien_thoai   NVARCHAR(20),
    email           NVARCHAR(150),
    mat_khau        NVARCHAR(255),
    ngay_tao        DATETIME2     DEFAULT GETDATE(),
    CONSTRAINT UQ_KH_ma        UNIQUE (ma_khach_hang),
    CONSTRAINT UQ_KH_sdt       UNIQUE (so_dien_thoai),
    CONSTRAINT UQ_KH_email     UNIQUE (email)
);
GO

-- ============================================================
-- 13. DIA CHI GIAO HANG
-- ============================================================
CREATE TABLE Dia_chi (
    id             INT IDENTITY(1,1) PRIMARY KEY,
    id_khach_hang  INT          NOT NULL,
    tinh_thanh_pho NVARCHAR(100),
    quan_huyen     NVARCHAR(100),
    xa_phuong      NVARCHAR(100),
    duong          NVARCHAR(255),
    mac_dinh       TINYINT      DEFAULT 0,
    CONSTRAINT FK_DiaChi_KH FOREIGN KEY (id_khach_hang) REFERENCES Khach_hang(id) ON DELETE CASCADE
);
GO

-- ============================================================
-- 14. VAI TRO
-- ============================================================
CREATE TABLE Vai_tro (
    id          INT IDENTITY(1,1) PRIMARY KEY,
    ten_vai_tro NVARCHAR(100) NOT NULL
);
GO

-- ============================================================
-- 15. NHAN VIEN
-- ============================================================
CREATE TABLE Nhan_vien (
    id                  INT IDENTITY(1,1) PRIMARY KEY,
    id_vai_tro          INT,
    ma_nhan_vien        NVARCHAR(50),
    ho_va_ten           NVARCHAR(150) NOT NULL,
    gioi_tinh           TINYINT,
    ngay_sinh           DATE,
    so_dien_thoai       NVARCHAR(20),
    dia_chi             NVARCHAR(255),
    email               NVARCHAR(150),
    ten_nguoi_dung      NVARCHAR(100),
    mat_khau            NVARCHAR(255),
    tinh_trang_lam_viec TINYINT       DEFAULT 1,
    ngay_tao            DATETIME2     DEFAULT GETDATE(),
    CONSTRAINT UQ_NV_ma        UNIQUE (ma_nhan_vien),
    CONSTRAINT UQ_NV_email     UNIQUE (email),
    CONSTRAINT UQ_NV_username  UNIQUE (ten_nguoi_dung),
    CONSTRAINT FK_NV_VaiTro    FOREIGN KEY (id_vai_tro) REFERENCES Vai_tro(id)
);
GO

-- ============================================================
-- 16. HOA DON
-- ============================================================
CREATE TABLE Hoa_don (
    id                           INT IDENTITY(1,1) PRIMARY KEY,
    id_khach_hang                INT,
    id_khuyen_mai                INT,
    id_giam_gia                  INT,
    id_nhan_vien                 INT,
    ma_hoa_don                   NVARCHAR(80)  NOT NULL,
    tong_tien                    DECIMAL(15,2) NOT NULL,
    phi_van_chuyen               DECIMAL(15,2) DEFAULT 0,
    giam_gia_khuyen_mai          DECIMAL(15,2) DEFAULT 0,
    hinh_thuc_nhan_hang          TINYINT       DEFAULT 1,
    dia_chi_giao_hang            NVARCHAR(500),
    trang_thai                   TINYINT       DEFAULT 0,
    hinh_thuc_thanh_toan         NVARCHAR(50),
    phuong_thuc_thanh_toan_online NVARCHAR(50),
    ghi_chu                      NVARCHAR(MAX),
    ngay_tao                     DATETIME2     DEFAULT GETDATE(),
    CONSTRAINT UQ_HD_ma          UNIQUE (ma_hoa_don),
    CONSTRAINT FK_HD_KhachHang   FOREIGN KEY (id_khach_hang) REFERENCES Khach_hang(id),
    CONSTRAINT FK_HD_KhuyenMai   FOREIGN KEY (id_khuyen_mai) REFERENCES Khuyen_mai(id),
    CONSTRAINT FK_HD_GiamGia     FOREIGN KEY (id_giam_gia)   REFERENCES Giam_gia(id),
    CONSTRAINT FK_HD_NhanVien    FOREIGN KEY (id_nhan_vien)  REFERENCES Nhan_vien(id)
);
GO

-- ============================================================
-- 17. CHI TIET HOA DON
-- ============================================================
CREATE TABLE Hoa_don_chi_tiet (
    id               INT IDENTITY(1,1) PRIMARY KEY,
    id_hoa_don       INT           NOT NULL,
    id_vay_chi_tiet  INT           NOT NULL,
    so_luong         INT           NOT NULL DEFAULT 1,
    don_gia          DECIMAL(15,2) NOT NULL,
    phan_tram_giam   DECIMAL(5,2)  DEFAULT 0,
    thanh_tien       AS (CAST(so_luong * don_gia * (1 - phan_tram_giam / 100.0) AS DECIMAL(15,2))) PERSISTED,
    CONSTRAINT FK_HDCT_HoaDon    FOREIGN KEY (id_hoa_don)      REFERENCES Hoa_don(id)      ON DELETE CASCADE,
    CONSTRAINT FK_HDCT_VayCT     FOREIGN KEY (id_vay_chi_tiet) REFERENCES Vay_chi_tiet(id)
);
GO

-- ============================================================
-- 18. GIO HANG (bang cha)
-- ============================================================
CREATE TABLE Gio_hang (
    id              INT IDENTITY(1,1) PRIMARY KEY,
    id_khach_hang   INT NOT NULL UNIQUE,
    ngay_tao        DATETIME2 DEFAULT GETDATE(),
    CONSTRAINT FK_GioHang_KhachHang
        FOREIGN KEY (id_khach_hang)
        REFERENCES Khach_hang(id)
        ON DELETE CASCADE
);
GO

-- ============================================================
-- 19. GIO HANG CHI TIET (bang con — tham chieu Gio_hang)
-- ============================================================
CREATE TABLE Gio_hang_chi_tiet (
    id              INT IDENTITY(1,1) PRIMARY KEY,
    id_gio_hang     INT  NOT NULL,
    id_vay_chi_tiet INT  NOT NULL,
    so_luong        INT  DEFAULT 1,
    ngay_tao        DATETIME2 DEFAULT GETDATE(),
    CONSTRAINT FK_GHCT_GioHang  FOREIGN KEY (id_gio_hang)     REFERENCES Gio_hang(id)     ON DELETE CASCADE,
    CONSTRAINT FK_GHCT_VayCT    FOREIGN KEY (id_vay_chi_tiet) REFERENCES Vay_chi_tiet(id)
);
GO

-- ============================================================
-- 20. DANH GIA SAN PHAM
-- ============================================================
CREATE TABLE Danh_gia (
    id            INT IDENTITY(1,1) PRIMARY KEY,
    id_khach_hang INT      NOT NULL,
    id_vay        INT      NOT NULL,
    id_hoa_don    INT,
    so_sao        TINYINT  NOT NULL,
    noi_dung      NVARCHAR(MAX),
    anh_danh_gia  NVARCHAR(500),
    trang_thai    TINYINT  DEFAULT 1,
    ngay_tao      DATETIME2 DEFAULT GETDATE(),
    CONSTRAINT CK_DanhGia_soSao  CHECK (so_sao BETWEEN 1 AND 5),
    CONSTRAINT FK_DG_KhachHang   FOREIGN KEY (id_khach_hang) REFERENCES Khach_hang(id) ON DELETE CASCADE,
    CONSTRAINT FK_DG_Vay         FOREIGN KEY (id_vay)        REFERENCES Vay(id),
    CONSTRAINT FK_DG_HoaDon      FOREIGN KEY (id_hoa_don)    REFERENCES Hoa_don(id)
);
GO

-- ============================================================
-- 21. LICH SU THANH TOAN
-- ============================================================
CREATE TABLE Lich_su_thanh_toan (
    id           INT IDENTITY(1,1) PRIMARY KEY,
    id_hoa_don   INT           NOT NULL,
    so_tien      DECIMAL(15,2) NOT NULL,
    phuong_thuc  NVARCHAR(50)  NOT NULL,
    ma_giao_dich NVARCHAR(200),
    trang_thai   NVARCHAR(50)  DEFAULT 'pending',
    noi_dung     NVARCHAR(MAX),
    ngay_tao     DATETIME2     DEFAULT GETDATE(),
    CONSTRAINT FK_LSTT_HoaDon FOREIGN KEY (id_hoa_don) REFERENCES Hoa_don(id) ON DELETE CASCADE
);
GO

-- ============================================================
-- 22. THONG BAO
-- ============================================================
CREATE TABLE Thong_bao (
    id            INT IDENTITY(1,1) PRIMARY KEY,
    id_khach_hang INT,
    id_nhan_vien  INT,
    tieu_de       NVARCHAR(255) NOT NULL,
    noi_dung      NVARCHAR(MAX),
    loai          NVARCHAR(50)  DEFAULT 'order',
    da_doc        TINYINT       DEFAULT 0,
    ngay_tao      DATETIME2     DEFAULT GETDATE(),
    CONSTRAINT FK_TB_KhachHang FOREIGN KEY (id_khach_hang) REFERENCES Khach_hang(id),
    CONSTRAINT FK_TB_NhanVien  FOREIGN KEY (id_nhan_vien)  REFERENCES Nhan_vien(id)
);
GO

-- ============================================================
-- 23. YEU THICH / WISHLIST
-- ============================================================
CREATE TABLE San_pham_yeu_thich (
    id            INT IDENTITY(1,1) PRIMARY KEY,
    id_khach_hang INT NOT NULL,
    id_vay        INT NOT NULL,
    ngay_tao      DATETIME2 DEFAULT GETDATE(),
    CONSTRAINT UQ_YeuThich          UNIQUE (id_khach_hang, id_vay),
    CONSTRAINT FK_YT_KhachHang      FOREIGN KEY (id_khach_hang) REFERENCES Khach_hang(id) ON DELETE CASCADE,
    CONSTRAINT FK_YT_Vay            FOREIGN KEY (id_vay)        REFERENCES Vay(id)
);
GO

-- ============================================================
-- 24. LICH SU XEM SAN PHAM
-- ============================================================
CREATE TABLE Lich_su_xem (
    id            INT IDENTITY(1,1) PRIMARY KEY,
    id_khach_hang INT,
    id_vay        INT NOT NULL,
    ngay_xem      DATETIME2 DEFAULT GETDATE(),
    CONSTRAINT FK_LSX_KhachHang FOREIGN KEY (id_khach_hang) REFERENCES Khach_hang(id),
    CONSTRAINT FK_LSX_Vay       FOREIGN KEY (id_vay)        REFERENCES Vay(id)
);
GO

-- ============================================================
-- 25. NHAT KY HE THONG
-- ============================================================
CREATE TABLE Nhat_ky (
    id           INT IDENTITY(1,1) PRIMARY KEY,
    id_nhan_vien INT,
    hanh_dong    NVARCHAR(100) NOT NULL,
    bang_tac_dong NVARCHAR(100),
    id_ban_ghi   INT,
    chi_tiet     NVARCHAR(MAX),
    dia_chi_ip   NVARCHAR(50),
    ngay_tao     DATETIME2     DEFAULT GETDATE(),
    CONSTRAINT FK_NK_NhanVien FOREIGN KEY (id_nhan_vien) REFERENCES Nhan_vien(id)
);
GO


-- ============================================================
--  DU LIEU MAU (SEED DATA)
-- ============================================================

-- Vai tro
INSERT INTO Vai_tro (ten_vai_tro) VALUES (N'Admin'), (N'Nhân viên'), (N'Quản lý kho');
GO

-- Mau sac
INSERT INTO Mau_Sac (ten_mau_sac, ma_hex) VALUES
(N'Đỏ',      '#FF0000'),
(N'Xanh Navy','#001F5B'),
(N'Vàng',    '#FFD700'),
(N'Trắng',   '#FFFFFF'),
(N'Đen',     '#000000'),
(N'Hồng',    '#FFC0CB'),
(N'Tím',     '#800080'),
(N'Xanh Lá', '#008000');
GO

-- Kich thuoc
INSERT INTO Kich_Thuoc (ten_kich_thuoc, mo_ta) VALUES
(N'S',         N'Small - Nhỏ'),
(N'M',         N'Medium - Vừa'),
(N'L',         N'Large - Lớn'),
(N'XL',        N'Extra Large'),
(N'XXL',       N'Double Extra Large'),
(N'Free size', N'Một kích thước');
GO

-- Chat lieu
INSERT INTO Chat_lieu (ten_chat_lieu) VALUES
(N'Lụa tơ tằm'), (N'Gấm'), (N'Voan'),
(N'Cotton'),     (N'Đũi'), (N'Nhung');
GO

-- Loai vay
INSERT INTO Loai_vay (ten_loai_vay) VALUES
(N'Váy truyền thống'), (N'Váy cách tân'),  (N'Váy dạ hội'),
(N'Váy cưới'),         (N'Váy học sinh'), (N'Váy công sở');
GO

-- Nha cung cap
INSERT INTO Nha_cung_cap (ten_nha_cung_cap, dia_chi, so_dien_thoai, email) VALUES
(N'Công ty Lụa Bảo Lộc', N'Bảo Lộc, Lâm Đồng',            '0263123456', 'baoloc@silk.vn'),
(N'Xưởng Gấm Hà Nội',    N'12 Hàng Bông, Hoàn Kiếm, HN',  '0243456789', 'gam@hanoi.vn');
GO

-- Khuyen mai mau
INSERT INTO Khuyen_mai (ma_khuyen_mai, ten_khuyen_mai, phan_tram_giam, ngay_bat_dau, ngay_ket_thuc) VALUES
('KM_TETNGUYEN', N'Khuyến mãi Tết Nguyên Đán', 20.00, '2025-01-01', '2025-02-15'),
('KM_8_3',       N'Sale mừng 8/3',              15.00, '2025-03-01', '2025-03-10');
GO

-- Tai tro / Thuong hieu
INSERT INTO Tai_tro (ten_tai_tro) VALUES
(N'Zestia Original'), (N'Local Brand'), (N'Premium Line');
GO

-- San pham mau
INSERT INTO Vay (id_nha_cung_cap, id_loai_vay, id_chat_lieu, id_tai_tro, ma_vay, ten_vay, mo_ta) VALUES
(1, 1, 1, 1, 'VAY001', N'Váy Lụa Tơ Tằm Cổ Điển',     N'Váy lụa tơ tằm mềm mại, phong cách cổ điển sang trọng'),
(1, 2, 3, 1, 'VAY002', N'Váy Voan Cách Tân Hiện Đại',   N'Váy voan nhẹ nhàng với thiết kế cách tân trẻ trung'),
(2, 3, 2, 2, 'VAY003', N'Váy Dạ Hội Gấm Hoàng Gia',    N'Váy dạ hội chất liệu gấm cao cấp cho những dịp đặc biệt'),
(1, 6, 4, 1, 'VAY004', N'Váy Cotton Công Sở Thanh Lịch', N'Váy cotton thoáng mát, phù hợp môi trường công sở'),
(2, 2, 5, 3, 'VAY005', N'Váy Đũi Cách Tân Mùa Hè',     N'Váy đũi thoáng mát với thiết kế hiện đại cho mùa hè'),
(1, 4, 1, 2, 'VAY006', N'Váy Cưới Lụa Trắng Tinh Khôi', N'Váy cưới lụa tơ tằm trắng tinh tế, thanh lịch');
GO

-- Bien the san pham mau
INSERT INTO Vay_chi_tiet (id_vay, id_mau_sac, id_kich_thuoc, ma_vay_chi_tiet, gia_ban_goc, gia_ban, so_luong) VALUES
(1, 4, 1, 'VAY001-TRANG-S',  2890000, 2890000, 15),
(1, 4, 2, 'VAY001-TRANG-M',  2890000, 2890000, 20),
(1, 4, 3, 'VAY001-TRANG-L',  2890000, 2890000, 10),
(1, 5, 1, 'VAY001-DEN-S',    2890000, 2890000, 12),
(1, 5, 2, 'VAY001-DEN-M',    2890000, 2890000, 18),
(2, 6, 2, 'VAY002-HONG-M',   1990000, 1590000, 25),
(2, 6, 3, 'VAY002-HONG-L',   1990000, 1590000, 20),
(2, 7, 2, 'VAY002-TIM-M',    1990000, 1790000, 15),
(3, 1, 2, 'VAY003-DO-M',     5490000, 4290000, 8),
(3, 1, 3, 'VAY003-DO-L',     5490000, 4290000, 5),
(3, 3, 2, 'VAY003-VANG-M',   5490000, 4890000, 6),
(4, 4, 1, 'VAY004-TRANG-S',  1290000, 1290000, 30),
(4, 4, 2, 'VAY004-TRANG-M',  1290000, 1290000, 35),
(4, 2, 2, 'VAY004-NAVY-M',   1290000, 1290000, 25),
(5, 8, 2, 'VAY005-XANHLA-M', 1690000, 1390000, 20),
(5, 8, 3, 'VAY005-XANHLA-L', 1690000, 1390000, 15),
(6, 4, 2, 'VAY006-TRANG-M',  8990000, 8990000, 3),
(6, 4, 3, 'VAY006-TRANG-L',  8990000, 8990000, 2);
GO

-- Khuyen mai cho mot so bien the
UPDATE Vay_chi_tiet SET id_khuyen_mai = 1, phan_tram_giam = 20.00 WHERE id IN (6, 7, 8);
UPDATE Vay_chi_tiet SET id_khuyen_mai = 2, phan_tram_giam = 15.00 WHERE id IN (9, 10, 11);
GO

-- Admin mac dinh (mat khau: admin123 — hash bcrypt trong ung dung)
INSERT INTO Nhan_vien (id_vai_tro, ma_nhan_vien, ho_va_ten, email, ten_nguoi_dung, mat_khau)
VALUES (1, 'NV001', N'Quản trị viên', 'admin@zestia.vn', 'admin',
        '$2b$10$XcQq3v8k5YmOWcVv8LyYxOd0QzGb5Nl7Dc2Yz1RxMp4UjKkHlCJi');
GO

-- Nhan vien ban hang
INSERT INTO Nhan_vien (id_vai_tro, ma_nhan_vien, ho_va_ten, so_dien_thoai, email, ten_nguoi_dung, mat_khau)
VALUES (2, 'NV002', N'Trần Minh Tuấn', '0901234567', 'tuan@zestia.vn', 'tuannv',
        '$2b$10$XcQq3v8k5YmOWcVv8LyYxOd0QzGb5Nl7Dc2Yz1RxMp4UjKkHlCJi');
GO

-- Khach hang mau
INSERT INTO Khach_hang (ma_khach_hang, ho_va_ten, gioi_tinh, ngay_sinh, so_dien_thoai, email, mat_khau) VALUES
('KH001', N'Nguyễn Lan Anh',  0, '1995-03-15', '0912345678', 'lananh@email.com',
 '$2b$10$XcQq3v8k5YmOWcVv8LyYxOd0QzGb5Nl7Dc2Yz1RxMp4UjKkHlCJi'),
('KH002', N'Phạm Thị Hương',  0, '1998-07-22', '0923456789', 'huong@email.com',
 '$2b$10$XcQq3v8k5YmOWcVv8LyYxOd0QzGb5Nl7Dc2Yz1RxMp4UjKkHlCJi'),
('KH003', N'Lê Văn Minh',     1, '1992-11-08', '0934567890', 'minh@email.com',
 '$2b$10$XcQq3v8k5YmOWcVv8LyYxOd0QzGb5Nl7Dc2Yz1RxMp4UjKkHlCJi');
GO

-- Dia chi khach hang
INSERT INTO Dia_chi (id_khach_hang, tinh_thanh_pho, quan_huyen, xa_phuong, duong, mac_dinh) VALUES
(1, N'Hà Nội',          N'Hoàn Kiếm',  N'Tràng Tiền',    N'128 Trần Hưng Đạo', 1),
(2, N'TP. Hồ Chí Minh', N'Quận 1',     N'Bến Nghé',      N'45 Nguyễn Huệ',     1),
(3, N'Đà Nẵng',         N'Hải Châu',   N'Thạch Thang',   N'22 Bạch Đằng',      1);
GO

-- Gio hang mau
INSERT INTO Gio_hang (id_khach_hang) VALUES (1), (2), (3);
GO

-- Gio hang chi tiet mau
INSERT INTO Gio_hang_chi_tiet (id_gio_hang, id_vay_chi_tiet, so_luong) VALUES
(1, 1,  1),
(1, 6,  2),
(2, 9,  1),
(2, 12, 1),
(3, 15, 1);
GO

-- Hoa don mau
INSERT INTO Hoa_don (id_khach_hang, id_nhan_vien, ma_hoa_don, tong_tien, hinh_thuc_nhan_hang, dia_chi_giao_hang, trang_thai, hinh_thuc_thanh_toan) VALUES
(1, 2, 'HD-2025-0001', 6070000,  1, N'128 Trần Hưng Đạo, Hoàn Kiếm, Hà Nội',          3, 'VNPay'),
(1, 2, 'HD-2025-0002', 8580000,  1, N'128 Trần Hưng Đạo, Hoàn Kiếm, Hà Nội',          2, 'COD'),
(2, 2, 'HD-2025-0003', 4290000,  1, N'45 Nguyễn Huệ, Quận 1, TP. Hồ Chí Minh',        3, 'Momo'),
(3, 2, 'HD-2025-0004', 1290000,  2, NULL,                                                1, 'Tiền mặt'),
(2, 2, 'HD-2025-0005', 2780000,  1, N'45 Nguyễn Huệ, Quận 1, TP. Hồ Chí Minh',        0, 'VNPay');
GO

-- Chi tiet hoa don mau
INSERT INTO Hoa_don_chi_tiet (id_hoa_don, id_vay_chi_tiet, so_luong, don_gia) VALUES
(1, 1,  1, 2890000),
(1, 6,  2, 1590000),
(2, 9,  2, 4290000),
(3, 9,  1, 4290000),
(4, 12, 1, 1290000),
(5, 6,  1, 1590000),
(5, 15, 1, 1390000);
GO

-- Danh gia mau
INSERT INTO Danh_gia (id_khach_hang, id_vay, id_hoa_don, so_sao, noi_dung) VALUES
(1, 1, 1, 5, N'Váy rất đẹp, chất liệu lụa mềm mại, mặc rất thoải mái!'),
(1, 2, 1, 4, N'Váy voan nhẹ nhàng, nhưng hơi dài so với mình'),
(2, 3, 3, 5, N'Váy dạ hội tuyệt vời, chất gấm sang trọng'),
(3, 4, 4, 4, N'Váy công sở đẹp, giá hợp lý');
GO

-- Lich su thanh toan
INSERT INTO Lich_su_thanh_toan (id_hoa_don, so_tien, phuong_thuc, ma_giao_dich, trang_thai) VALUES
(1, 6070000, 'VNPay',    'VNP20250101001', 'success'),
(3, 4290000, 'Momo',     'MOMO20250115001', 'success'),
(4, 1290000, N'Tiền mặt', NULL,              'success'),
(5, 2780000, 'VNPay',    'VNP20250120001', 'pending');
GO

-- Giam gia / Voucher mau
INSERT INTO Giam_gia (ma_giam_gia, ten_giam_gia, gia_tri_don_toi_thieu, phan_tram_giam, giam_toi_da, so_luong, ngay_bat_dau, ngay_ket_thuc) VALUES
('ZESTIA10',  N'Giảm 10% đơn đầu tiên',   500000,  10.00, 200000,  100, '2025-01-01', '2025-12-31'),
('FREESHIP',  N'Miễn phí vận chuyển',       300000,  NULL,  NULL,    200, '2025-01-01', '2025-06-30'),
('SUMMER20',  N'Giảm 20% hè rực rỡ',       1000000, 20.00, 500000,  50,  '2025-06-01', '2025-08-31');
GO


-- ============================================================
--  STORED PROCEDURES
-- ============================================================

-- 1. Thong ke doanh thu theo thang
CREATE OR ALTER PROCEDURE sp_doanh_thu_theo_thang
    @nam   INT,
    @thang INT = NULL
AS
BEGIN
    SET NOCOUNT ON;
    SELECT
        CAST(hd.ngay_tao AS DATE)  AS ngay,
        COUNT(hd.id)               AS so_don_hang,
        SUM(hd.tong_tien)          AS tong_doanh_thu
    FROM Hoa_don hd
    WHERE YEAR(hd.ngay_tao) = @nam
      AND (@thang IS NULL OR MONTH(hd.ngay_tao) = @thang)
      AND hd.trang_thai = 3
    GROUP BY CAST(hd.ngay_tao AS DATE)
    ORDER BY ngay;
END;
GO

-- 2. Kiem tra ton kho
CREATE OR ALTER PROCEDURE sp_kiem_tra_ton_kho
    @id_vay INT = NULL
AS
BEGIN
    SET NOCOUNT ON;
    SELECT
        v.ten_vay,
        ms.ten_mau_sac,
        kt.ten_kich_thuoc,
        vct.so_luong     AS ton_kho,
        vct.gia_ban,
        vct.trang_thai
    FROM Vay_chi_tiet vct
    JOIN Vay       v   ON v.id   = vct.id_vay
    JOIN Mau_Sac   ms  ON ms.id  = vct.id_mau_sac
    JOIN Kich_Thuoc kt ON kt.id  = vct.id_kich_thuoc
    WHERE (@id_vay IS NULL OR vct.id_vay = @id_vay)
    ORDER BY v.ten_vay, ms.ten_mau_sac, kt.ten_kich_thuoc;
END;
GO

-- 3. Tao hoa don tu gio hang (co Gio_hang)
CREATE OR ALTER PROCEDURE sp_tao_hoa_don
    @id_khach_hang  INT,
    @ma_hoa_don     NVARCHAR(80),
    @hinh_thuc      TINYINT,
    @dia_chi        NVARCHAR(500),
    @thanh_toan     NVARCHAR(50),
    @id_giam_gia    INT = NULL,
    @id_hoa_don     INT OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRY
        BEGIN TRANSACTION;

        INSERT INTO Hoa_don
            (id_khach_hang, ma_hoa_don, tong_tien, hinh_thuc_nhan_hang,
             dia_chi_giao_hang, hinh_thuc_thanh_toan, id_giam_gia)
        VALUES
            (@id_khach_hang, @ma_hoa_don, 0, @hinh_thuc,
             @dia_chi, @thanh_toan, @id_giam_gia);

        SET @id_hoa_don = SCOPE_IDENTITY();

        INSERT INTO Hoa_don_chi_tiet (id_hoa_don, id_vay_chi_tiet, so_luong, don_gia)
        SELECT @id_hoa_don, ghct.id_vay_chi_tiet, ghct.so_luong, vct.gia_ban
        FROM Gio_hang gh
        JOIN Gio_hang_chi_tiet ghct ON ghct.id_gio_hang = gh.id
        JOIN Vay_chi_tiet vct       ON vct.id = ghct.id_vay_chi_tiet
        WHERE gh.id_khach_hang = @id_khach_hang;

        UPDATE Hoa_don
        SET tong_tien = (
            SELECT ISNULL(SUM(thanh_tien), 0)
            FROM Hoa_don_chi_tiet
            WHERE id_hoa_don = @id_hoa_don
        )
        WHERE id = @id_hoa_don;

        UPDATE vct
        SET vct.so_luong = vct.so_luong - ghct.so_luong
        FROM Vay_chi_tiet vct
        JOIN Gio_hang_chi_tiet ghct ON ghct.id_vay_chi_tiet = vct.id
        JOIN Gio_hang gh            ON gh.id = ghct.id_gio_hang
        WHERE gh.id_khach_hang = @id_khach_hang;

        DELETE ghct
        FROM Gio_hang_chi_tiet ghct
        JOIN Gio_hang gh ON gh.id = ghct.id_gio_hang
        WHERE gh.id_khach_hang = @id_khach_hang;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        ROLLBACK TRANSACTION;
        SET @id_hoa_don = -1;
        THROW;
    END CATCH;
END;
GO


-- ============================================================
--  VIEWS
-- ============================================================

CREATE OR ALTER VIEW v_san_pham_day_du AS
SELECT
    v.id,
    v.ma_vay,
    v.ten_vay,
    lv.ten_loai_vay                     AS loai,
    cl.ten_chat_lieu                    AS chat_lieu,
    ncc.ten_nha_cung_cap                AS nha_cung_cap,
    MIN(vct.gia_ban)                    AS gia_tu,
    MAX(vct.gia_ban)                    AS gia_den,
    SUM(vct.so_luong)                   AS tong_ton_kho,
    ROUND(AVG(CAST(dg.so_sao AS FLOAT)),1) AS diem_trung_binh,
    COUNT(DISTINCT dg.id)               AS so_luot_danh_gia,
    v.trang_thai
FROM Vay v
LEFT JOIN Loai_vay        lv  ON lv.id  = v.id_loai_vay
LEFT JOIN Chat_lieu       cl  ON cl.id  = v.id_chat_lieu
LEFT JOIN Nha_cung_cap    ncc ON ncc.id = v.id_nha_cung_cap
LEFT JOIN Vay_chi_tiet    vct ON vct.id_vay = v.id
LEFT JOIN Danh_gia        dg  ON dg.id_vay  = v.id
GROUP BY v.id, v.ma_vay, v.ten_vay, lv.ten_loai_vay,
         cl.ten_chat_lieu, ncc.ten_nha_cung_cap, v.trang_thai;
GO

CREATE OR ALTER VIEW v_thong_ke_don_hang AS
SELECT
    CASE trang_thai
        WHEN 0 THEN N'Chờ xác nhận'
        WHEN 1 THEN N'Đã xác nhận'
        WHEN 2 THEN N'Đang giao hàng'
        WHEN 3 THEN N'Hoàn thành'
        WHEN 4 THEN N'Đã huỷ'
    END          AS trang_thai,
    COUNT(id)    AS so_don,
    SUM(tong_tien) AS tong_tien
FROM Hoa_don
GROUP BY trang_thai;
GO

CREATE OR ALTER VIEW v_top_ban_chay AS
SELECT TOP 100
    v.ten_vay,
    SUM(hdct.so_luong)   AS so_luong_ban,
    SUM(hdct.thanh_tien) AS doanh_thu
FROM Hoa_don_chi_tiet hdct
JOIN Vay_chi_tiet vct ON vct.id  = hdct.id_vay_chi_tiet
JOIN Vay          v   ON v.id    = vct.id_vay
JOIN Hoa_don      hd  ON hd.id   = hdct.id_hoa_don
WHERE hd.trang_thai = 3
GROUP BY v.id, v.ten_vay
ORDER BY so_luong_ban DESC;
GO


-- ============================================================
--  INDEX
-- ============================================================
CREATE INDEX idx_hoadon_khachhang ON Hoa_don(id_khach_hang);
CREATE INDEX idx_hoadon_ngaytao   ON Hoa_don(ngay_tao);
CREATE INDEX idx_hoadon_trangthai ON Hoa_don(trang_thai);
CREATE INDEX idx_hdct_hoadon      ON Hoa_don_chi_tiet(id_hoa_don);
CREATE INDEX idx_vayCT_vay        ON Vay_chi_tiet(id_vay);
CREATE INDEX idx_giohang_gh       ON Gio_hang_chi_tiet(id_gio_hang);
CREATE INDEX idx_danhgia_vay      ON Danh_gia(id_vay);
CREATE INDEX idx_anh_vay          ON Anh(id_vay);
GO


-- ============================================================
--  TRIGGERS
-- ============================================================

CREATE OR ALTER TRIGGER trg_canh_bao_ton_kho
ON Vay_chi_tiet
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    INSERT INTO Thong_bao (tieu_de, noi_dung, loai)
    SELECT
        N'Cảnh báo tồn kho thấp',
        N'Biến thể sản phẩm ID=' + CAST(i.id AS NVARCHAR) +
        N' còn ' + CAST(i.so_luong AS NVARCHAR) + N' sản phẩm',
        'system'
    FROM inserted i
    JOIN deleted  d ON d.id = i.id
    WHERE i.so_luong < 5 AND d.so_luong >= 5;
END;
GO

CREATE OR ALTER TRIGGER trg_log_hoa_don
ON Hoa_don
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    INSERT INTO Nhat_ky (hanh_dong, bang_tac_dong, id_ban_ghi, chi_tiet)
    SELECT
        'UPDATE',
        'Hoa_don',
        i.id,
        N'Trạng thái đổi từ ' + CAST(d.trang_thai AS NVARCHAR) +
        N' sang ' + CAST(i.trang_thai AS NVARCHAR)
    FROM inserted i
    JOIN deleted  d ON d.id = i.id
    WHERE i.trang_thai <> d.trang_thai;
END;
GO

-- ============================================================
--  KET THUC — Tong cong 25 bang
-- ============================================================
