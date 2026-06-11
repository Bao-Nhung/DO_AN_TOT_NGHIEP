package com.zestia.datn.zestia.config;

import com.zestia.datn.zestia.entity.KhachHang;
import com.zestia.datn.zestia.entity.NhanVien;
import com.zestia.datn.zestia.repository.KhachHangRepository;
import com.zestia.datn.zestia.repository.NhanVienRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final NhanVienRepository nhanVienRepo;
    private final KhachHangRepository khachHangRepo;
    private final PasswordEncoder encoder;

    @Override
    public void run(String... args) {
        // Re-hash passwords to ensure BCrypt compatibility with Spring Security
        List<NhanVien> nhanViens = nhanVienRepo.findAll();
        for (NhanVien nv : nhanViens) {
            if (nv.getMatKhau() != null && !nv.getMatKhau().startsWith("$2a$")) {
                nv.setMatKhau(encoder.encode("123456"));
                nhanVienRepo.save(nv);
                log.info("Re-hashed password for NhanVien: {}", nv.getTenNguoiDung());
            }
        }

        List<KhachHang> khachHangs = khachHangRepo.findAll();
        for (KhachHang kh : khachHangs) {
            if (kh.getMatKhau() != null && !kh.getMatKhau().startsWith("$2a$")) {
                kh.setMatKhau(encoder.encode("123456"));
                khachHangRepo.save(kh);
                log.info("Re-hashed password for KhachHang: {}", kh.getEmail());
            }
        }
    }
}
