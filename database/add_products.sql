-- Add 30 new products (5 per category) to bring total to 60

-- Category 1: Váy truyền thống (5 more)
INSERT INTO Vay (id_nha_cung_cap, id_loai_vay, id_chat_lieu, ma_vay, ten_vay, trang_thai, mo_ta, ngay_tao) VALUES
(1, 1, 1, 'VTT006', N'Váy Truyền Thống Lụa Hồng', 1, N'Lụa tơ tằm hồng nhạt, thêu hoa sen tinh tế.', GETDATE()),
(2, 1, 2, 'VTT007', N'Váy Truyền Thống Gấm Hoàng Gia', 1, N'Gấm vàng hoàng gia, hoa văn rồng phượng cổ điển.', GETDATE()),
(1, 1, 5, 'VTT008', N'Váy Truyền Thống Đũi Tự Nhiên', 1, N'Chất đũi mềm mại, phong cách mộc mạc thanh lịch.', GETDATE()),
(2, 1, 3, 'VTT009', N'Váy Truyền Thống Voan Bay Bổng', 1, N'Voan mỏng nhẹ bay bổng, họa tiết hoa cúc trắng.', GETDATE()),
(1, 1, 6, 'VTT010', N'Váy Truyền Thống Nhung Tím', 1, N'Nhung tím quý phái, phù hợp dịp lễ hội truyền thống.', GETDATE());

-- Category 2: Váy cách tân (5 more)
INSERT INTO Vay (id_nha_cung_cap, id_loai_vay, id_chat_lieu, ma_vay, ten_vay, trang_thai, mo_ta, ngay_tao) VALUES
(1, 2, 1, 'VCT007', N'Váy Cách Tân Lụa Ombre', 1, N'Lụa loang màu ombre từ trắng sang hồng, phong cách hiện đại.', GETDATE()),
(2, 2, 4, 'VCT008', N'Váy Cách Tân Cotton Phố Cổ', 1, N'Cotton thoáng mát, in họa tiết phố cổ Hà Nội.', GETDATE()),
(1, 2, 3, 'VCT009', N'Váy Cách Tân Voan Tay Phồng', 1, N'Voan trắng tay phồng, kết hợp cổ áo mandarin hiện đại.', GETDATE()),
(2, 2, 2, 'VCT010', N'Váy Cách Tân Gấm Đỏ Xuân', 1, N'Gấm đỏ rực rỡ, thiết kế dáng ngắn năng động.', GETDATE()),
(1, 2, 5, 'VCT011', N'Váy Cách Tân Đũi Vintage', 1, N'Đũi nâu vintage, phối ren trắng cổ điển.', GETDATE());

-- Category 3: Váy dạ hội (5 more)
INSERT INTO Vay (id_nha_cung_cap, id_loai_vay, id_chat_lieu, ma_vay, ten_vay, trang_thai, mo_ta, ngay_tao) VALUES
(1, 3, 1, 'VDH006', N'Váy Dạ Hội Sequin Vàng', 1, N'Lụa phủ sequin vàng lấp lánh, dáng đuôi cá quyến rũ.', GETDATE()),
(2, 3, 3, 'VDH007', N'Váy Dạ Hội Voan Nhiều Tầng', 1, N'Voan xếp tầng bay bổng, tông pastel nhẹ nhàng.', GETDATE()),
(1, 3, 6, 'VDH008', N'Váy Dạ Hội Nhung Đen Huyền Bí', 1, N'Nhung đen sang trọng, xẻ tà cao thanh lịch.', GETDATE()),
(2, 3, 1, 'VDH009', N'Váy Dạ Hội Lụa Champagne', 1, N'Lụa tơ tằm màu champagne, đính pha lê Swarovski.', GETDATE()),
(1, 3, 2, 'VDH010', N'Váy Dạ Hội Gấm Xanh Ngọc', 1, N'Gấm xanh ngọc lục bảo, hoa văn phượng hoàng.', GETDATE());

-- Category 4: Váy cưới (5 more)
INSERT INTO Vay (id_nha_cung_cap, id_loai_vay, id_chat_lieu, ma_vay, ten_vay, trang_thai, mo_ta, ngay_tao) VALUES
(1, 4, 3, 'VC006', N'Váy Cưới Voan Công Chúa', 1, N'Voan trắng xếp tầng, đính hoa ren 3D lãng mạn.', GETDATE()),
(2, 4, 1, 'VC007', N'Váy Cưới Lụa Hoàng Cung', 1, N'Lụa ngà hoàng cung, thêu chỉ vàng thủ công.', GETDATE()),
(1, 4, 2, 'VC008', N'Váy Cưới Gấm Đỏ Truyền Thống', 1, N'Gấm đỏ truyền thống, thiết kế cô dâu Việt hiện đại.', GETDATE()),
(2, 4, 6, 'VC009', N'Váy Cưới Nhung Trắng Tối Giản', 1, N'Nhung trắng tối giản, dáng suông thanh lịch.', GETDATE()),
(1, 4, 3, 'VC010', N'Váy Cưới Voan Dài Thướt Tha', 1, N'Voan mỏng nhẹ, tà dài 2m ấn tượng trên lễ đường.', GETDATE());

-- Category 5: Váy học sinh (5 more)
INSERT INTO Vay (id_nha_cung_cap, id_loai_vay, id_chat_lieu, ma_vay, ten_vay, trang_thai, mo_ta, ngay_tao) VALUES
(1, 5, 4, 'VHS005', N'Váy Học Sinh Cotton Sọc', 1, N'Cotton mát mẻ, sọc xanh navy trẻ trung.', GETDATE()),
(2, 5, 4, 'VHS006', N'Váy Học Sinh Cotton Xếp Li', 1, N'Cotton xếp li cổ điển, kiểu Nhật Bản dễ thương.', GETDATE()),
(1, 5, 4, 'VHS007', N'Váy Học Sinh Kẻ Caro', 1, N'Cotton kẻ caro đỏ, phong cách preppy năng động.', GETDATE()),
(2, 5, 4, 'VHS008', N'Váy Học Sinh Thể Thao', 1, N'Cotton co giãn, thiết kế thể thao khỏe khoắn.', GETDATE()),
(1, 5, 3, 'VHS009', N'Váy Học Sinh Voan Nhẹ', 1, N'Voan nhẹ mát, phù hợp mùa hè thoải mái.', GETDATE());

-- Category 6: Váy công sở (5 more)
INSERT INTO Vay (id_nha_cung_cap, id_loai_vay, id_chat_lieu, ma_vay, ten_vay, trang_thai, mo_ta, ngay_tao) VALUES
(1, 6, 1, 'VCS005', N'Váy Công Sở Lụa Cổ Tim', 1, N'Lụa xanh navy cổ tim, phom ôm thanh lịch.', GETDATE()),
(2, 6, 4, 'VCS006', N'Váy Công Sở Cotton Dáng A', 1, N'Cotton trắng dáng A, phù hợp mọi vóc dáng.', GETDATE()),
(1, 6, 3, 'VCS007', N'Váy Công Sở Voan Xếp Nếp', 1, N'Voan xếp nếp tôn dáng, tông nude nhã nhặn.', GETDATE()),
(2, 6, 5, 'VCS008', N'Váy Công Sở Đũi Cổ Tròn', 1, N'Đũi nâu nhạt cổ tròn, đơn giản mà tinh tế.', GETDATE()),
(1, 6, 1, 'VCS009', N'Váy Công Sở Lụa Đen Sang Trọng', 1, N'Lụa đen bóng mượt, little black dress hoàn hảo.', GETDATE());
