package com.zestia.datn.zestia.service;

import com.zestia.datn.zestia.entity.ThongBao;
import com.zestia.datn.zestia.entity.KhachHang;
import com.zestia.datn.zestia.repository.ThongBaoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final ThongBaoRepository thongBaoRepo;

    public ThongBao createCustomerNotification(KhachHang customer, String title, String content, String type) {
        if (customer == null) return null;
        try {
            ThongBao thongBao = ThongBao.builder()
                    .khachHang(customer)
                    .tieuDe(title)
                    .noiDung(content)
                    .loai(type)
                    .trangThai((byte) 1)
                    .guiEmail((byte) 0)
                    .daGui((byte) 0)
                    .ngayTao(LocalDateTime.now())
                    .build();
            ThongBao saved = thongBaoRepo.save(thongBao);
            log.info("Đã tạo thông báo khách hàng {} cho tài khoản {}", saved.getId(), customer.getId());
            return saved;
        } catch (Exception e) {
            log.error("Không thể tạo thông báo cho khách hàng {}", customer.getId(), e);
            return null;
        }
    }
}
