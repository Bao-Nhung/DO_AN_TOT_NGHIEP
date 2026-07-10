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
        migrateNhanVienPasswords();
        migrateKhachHangPasswords();
    }

    private void migrateNhanVienPasswords() {
        List<NhanVien> nhanViens = nhanVienRepo.findAll();
        for (NhanVien nv : nhanViens) {
            String password = nv.getMatKhau();
            if (password != null && !password.startsWith("$2")) {
                nv.setMatKhau(encoder.encode(password));
                nhanVienRepo.save(nv);
                log.info("Migrated BCrypt password for employee: {}", nv.getTenNguoiDung());
            }
        }
    }

    private void migrateKhachHangPasswords() {
        List<KhachHang> khachHangs = khachHangRepo.findAll();
        for (KhachHang kh : khachHangs) {
            String password = kh.getMatKhau();
            if (password != null && !password.startsWith("$2")) {
                kh.setMatKhau(encoder.encode(password));
                khachHangRepo.save(kh);
                log.info("Migrated BCrypt password for customer: {}", kh.getEmail());
            }
        }
    }
}
