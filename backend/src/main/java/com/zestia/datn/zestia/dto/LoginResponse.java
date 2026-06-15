package com.zestia.datn.zestia.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String username;
    private String hoVaTen;
    private String email;
    private String soDienThoai;
    private String role;
    private Integer userId;
    private Byte gioiTinh;
}
