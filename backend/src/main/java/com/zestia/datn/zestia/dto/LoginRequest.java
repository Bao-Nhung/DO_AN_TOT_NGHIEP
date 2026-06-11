package com.zestia.datn.zestia.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String username; // ten_nguoi_dung (NhanVien) or email (KhachHang)
    private String password;
}
