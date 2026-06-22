package com.zestia.datn.zestia.dto;

import java.util.List;

import com.zestia.datn.zestia.dto.response.ThongKeDoanhThuDTO;
import com.zestia.datn.zestia.dto.response.ThongKeDoanhThuThoiGianDTO;
import com.zestia.datn.zestia.dto.response.ThongKeKhachHangDTO;
import com.zestia.datn.zestia.dto.response.ThongKeSoLuongDTO;
import com.zestia.datn.zestia.dto.response.ThongKeTongQuanDTO;
import com.zestia.datn.zestia.dto.response.ThongKeTrangThaiDTO;

import lombok.Data;

@Data
public class ThongKeResponse {

    private ThongKeTongQuanDTO tongQuan;

    private List<ThongKeDoanhThuThoiGianDTO> doanhThuLoiNhuan;

    private List<ThongKeTrangThaiDTO> trangThaiDonHang;

    private List<ThongKeKhachHangDTO> tangTruongKhachHang;

    private List<ThongKeSoLuongDTO> topSanPham;

    private List<ThongKeSoLuongDTO> theoSize;

    private List<ThongKeSoLuongDTO> theoMau;

    private List<ThongKeDoanhThuDTO> doanhThuDanhMuc;
    
}
