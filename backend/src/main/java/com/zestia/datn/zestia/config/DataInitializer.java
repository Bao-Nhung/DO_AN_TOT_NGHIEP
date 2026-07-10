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

        // Thêm danh sách email khách hàng mới do người dùng yêu cầu
        String[][] newCustomers = {
            {"nguyengiabaoo2008@gmail.com", "Nguyễn Gia Bảo", "KH005", "0900000005"},
            {"ngocanh2701ss@gmail.com", "Ngọc Anh", "KH006", "0900000006"},
            {"hoanganhminh110706@gmail.com", "Hoàng Anh Minh", "KH007", "0900000007"},
            {"Thuynpth06788@gmail.com", "Thủy NP", "KH008", "0900000008"},
            {"tonyvn081106@gmail.com", "Tony VN", "KH009", "0900000009"},
            {"nguyenthanh.hn090307@gmail.com", "Nguyễn Thành", "KH010", "0900000010"},
            {"baongts01859@gmail.com", "Bảo Nguyễn", "KH011", "0900000011"}
        };

        for (String[] cust : newCustomers) {
            String email = cust[0];
            String name = cust[1];
            String code = cust[2];
            String phone = cust[3];
            if (!khachHangRepo.existsByEmail(email)) {
                KhachHang kh = KhachHang.builder()
                        .email(email)
                        .hoVaTen(name)
                        .maKhachHang(code)
                        .soDienThoai(phone)
                        .matKhau(encoder.encode("123456"))
                        .ngayTao(java.time.LocalDateTime.now())
                        .build();
                khachHangRepo.save(kh);
                log.info("Đã khởi tạo khách hàng mới tự động: {} - {}", email, name);
            }
        }
    }
}
