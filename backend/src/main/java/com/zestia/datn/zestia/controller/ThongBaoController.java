package com.zestia.datn.zestia.controller;

import com.zestia.datn.zestia.entity.ThongBao;
import com.zestia.datn.zestia.entity.KhachHang;
import com.zestia.datn.zestia.repository.ThongBaoRepository;
import com.zestia.datn.zestia.repository.KhachHangRepository;
import com.zestia.datn.zestia.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/thong-bao")
@RequiredArgsConstructor
@Slf4j
public class ThongBaoController {

    private final ThongBaoRepository thongBaoRepo;
    private final KhachHangRepository khachHangRepo;
    private final EmailService emailService;

    @GetMapping
    public List<ThongBao> getAll() {
        return thongBaoRepo.findAllByOrderByNgayTaoDesc();
    }

    @GetMapping("/active")
    public List<ThongBao> getActive() {
        return thongBaoRepo.findByTrangThaiOrderByNgayTaoDesc((byte) 1);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody ThongBao body) {
        if (body.getTieuDe() == null || body.getTieuDe().trim().isEmpty() ||
            body.getNoiDung() == null || body.getNoiDung().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Vui lòng nhập đầy đủ tiêu đề và nội dung"));
        }
        body.setNgayTao(LocalDateTime.now());
        if (body.getTrangThai() == null) body.setTrangThai((byte) 1);
        if (body.getGuiEmail() == null) body.setGuiEmail((byte) 0);
        if (body.getDaGui() == null) body.setDaGui((byte) 0);

        // Hẹn giờ thì trạng thái sẽ là 2 (Scheduled)
        if (body.getTrangThai() == 2 || (body.getNgayGui() != null && body.getNgayGui().isAfter(LocalDateTime.now()))) {
            if (body.getNgayGui() == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Vui lòng chọn ngày giờ hẹn gửi"));
            }
            if (body.getNgayGui().isBefore(LocalDateTime.now())) {
                return ResponseEntity.badRequest().body(Map.of("message", "Ngày giờ hẹn gửi phải ở trong tương lai"));
            }
            body.setTrangThai((byte) 2);
        } else {
            body.setNgayGui(null);
        }

        ThongBao saved = thongBaoRepo.save(body);

        // Nếu trạng thái là active (1), gui_email = 1 và chưa gửi
        if (saved.getTrangThai() == 1 && saved.getGuiEmail() == 1 && (saved.getDaGui() == null || saved.getDaGui() == 0)) {
            saved.setDaGui((byte) 1);
            thongBaoRepo.save(saved);
            sendBulkEmailsAsync(saved);
        }

        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody ThongBao body) {
        return thongBaoRepo.findById(id).map(existing -> {
            if (body.getTieuDe() != null) existing.setTieuDe(body.getTieuDe());
            if (body.getNoiDung() != null) existing.setNoiDung(body.getNoiDung());
            if (body.getLoai() != null) existing.setLoai(body.getLoai());
            
            if (body.getGuiEmail() != null) {
                existing.setGuiEmail(body.getGuiEmail());
            }

            Byte newTrangThai = body.getTrangThai() != null ? body.getTrangThai() : existing.getTrangThai();
            LocalDateTime newNgayGui = body.getNgayGui(); // Có thể là null từ JSON

            if (newTrangThai == 2) {
                if (newNgayGui == null) {
                    return ResponseEntity.badRequest().body(Map.of("message", "Vui lòng chọn ngày giờ hẹn gửi"));
                }
                if (newNgayGui.isBefore(LocalDateTime.now())) {
                    return ResponseEntity.badRequest().body(Map.of("message", "Ngày giờ hẹn gửi phải ở trong tương lai"));
                }
                existing.setNgayGui(newNgayGui);
                existing.setTrangThai((byte) 2);
            } else {
                // Nếu trạng thái không phải 2 (Hẹn giờ), xóa ngày hẹn gửi
                existing.setNgayGui(null);
                existing.setTrangThai(newTrangThai);
                
                // Nếu chuyển về bản nháp (Draft), reset daGui về 0 để cho phép gửi lại sau này
                if (newTrangThai == 0) {
                    existing.setDaGui((byte) 0);
                }
            }

            if (body.getDaGui() != null) {
                existing.setDaGui(body.getDaGui());
            }

            ThongBao saved = thongBaoRepo.save(existing);

            // Nếu trạng thái đổi sang active (1) và được tích chọn gửi email và chưa gửi
            if (saved.getTrangThai() == 1 && saved.getGuiEmail() == 1 && (saved.getDaGui() == null || saved.getDaGui() == 0)) {
                saved.setDaGui((byte) 1);
                thongBaoRepo.save(saved);
                sendBulkEmailsAsync(saved);
            }

            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        thongBaoRepo.deleteById(id);
        return ResponseEntity.ok().build();
    }

    private void sendBulkEmailsAsync(ThongBao thongBao) {
        CompletableFuture.runAsync(() -> {
            try {
                List<KhachHang> khachHangs = khachHangRepo.findAll();
                log.info("Bắt đầu gửi email hàng loạt cho thông báo ID {} tới {} khách hàng", thongBao.getId(), khachHangs.size());
                int count = 0;
                for (KhachHang kh : khachHangs) {
                    if (kh.getEmail() != null && !kh.getEmail().trim().isEmpty()) {
                        emailService.sendAnnouncementEmail(kh.getEmail(), kh.getHoVaTen(), thongBao.getTieuDe(), thongBao.getNoiDung());
                        count++;
                    }
                }
                log.info("Đã gửi email hàng loạt thành công cho {} khách hàng", count);
            } catch (Exception e) {
                log.error("Lỗi khi gửi email hàng loạt cho thông báo ID " + thongBao.getId() + ": ", e);
            }
        });
    }
}