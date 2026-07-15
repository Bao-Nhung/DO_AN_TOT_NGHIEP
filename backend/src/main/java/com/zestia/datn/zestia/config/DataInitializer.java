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
        List<NhanVien> nhanViens = nhanVienRepo.findAccountsWithLegacyPassword();
        for (NhanVien nv : nhanViens) {
            nv.setMatKhau(encoder.encode(nv.getMatKhau()));
        }
        if (!nhanViens.isEmpty()) nhanVienRepo.saveAll(nhanViens);
        logMigration("employee", nhanViens.size());
    }

    private void migrateKhachHangPasswords() {
        List<KhachHang> khachHangs = khachHangRepo.findAccountsWithLegacyPassword();
        for (KhachHang kh : khachHangs) {
            kh.setMatKhau(encoder.encode(kh.getMatKhau()));
        }
        if (!khachHangs.isEmpty()) khachHangRepo.saveAll(khachHangs);
        logMigration("customer", khachHangs.size());
    }

    private void logMigration(String accountType, int count) {
        if (count > 0) log.info("Migrated {} legacy {} passwords to BCrypt", count, accountType);
    }
}
