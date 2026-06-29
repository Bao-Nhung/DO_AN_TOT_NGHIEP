package com.zestia.datn.zestia.service;

import com.zestia.datn.zestia.entity.HoaDon;
import com.zestia.datn.zestia.entity.HoaDonChiTiet;
import com.zestia.datn.zestia.entity.KhachHang;
import com.zestia.datn.zestia.repository.HoaDonChiTietRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    /**
     * Gửi email xác nhận đặt hàng
     * @param hoaDon Đơn hàng vừa tạo
     */
    public void sendOrderConfirmationEmail(HoaDon hoaDon) {
        try {
            KhachHang khachHang = hoaDon.getKhachHang();
            if (khachHang == null || khachHang.getEmail() == null) {
                log.warn("Không thể gửi email - khách hàng không có email");
                return;
            }

            String to = khachHang.getEmail();
            String subject = "Đơn hàng #" + hoaDon.getMaHoaDon() + " - Xác nhận đặt hàng";

            // Lấy chi tiết đơn hàng
            List<HoaDonChiTiet> chiTiets = hoaDonChiTietRepository.findByHoaDonId(hoaDon.getId());

            // Tạo HTML email
            String htmlContent = buildOrderConfirmationEmailHtml(hoaDon, chiTiets);

            // Gửi email
            sendHtmlEmail(to, subject, htmlContent);
            log.info("Email xác nhận đặt hàng đã gửi cho: {}", to);

        } catch (Exception e) {
            log.error("Lỗi khi gửi email xác nhận đặt hàng: ", e);
        }
    }

    /**
     * Gửi email thông báo cập nhật trạng thái đơn hàng
     * @param hoaDon Đơn hàng
     * @param trangThaiMoi Trạng thái mới
     * @param moTa Mô tả trạng thái
     */
    public void sendOrderStatusUpdateEmail(HoaDon hoaDon, String trangThaiMoi, String moTa) {
        try {
            KhachHang khachHang = hoaDon.getKhachHang();
            if (khachHang == null || khachHang.getEmail() == null) {
                log.warn("Không thể gửi email - khách hàng không có email");
                return;
            }

            String to = khachHang.getEmail();
            String subject = "Đơn hàng #" + hoaDon.getMaHoaDon() + " - Cập nhật trạng thái";

            String htmlContent = buildOrderStatusUpdateEmailHtml(hoaDon, trangThaiMoi, moTa);

            sendHtmlEmail(to, subject, htmlContent);
            log.info("Email cập nhật trạng thái đã gửi cho: {}", to);

        } catch (Exception e) {
            log.error("Lỗi khi gửi email cập nhật trạng thái: ", e);
        }
    }

    /**
     * Gửi email thông báo đơn hàng đã giao
     * @param hoaDon Đơn hàng
     */
    public void sendDeliveryConfirmationEmail(HoaDon hoaDon) {
        try {
            KhachHang khachHang = hoaDon.getKhachHang();
            if (khachHang == null || khachHang.getEmail() == null) {
                log.warn("Không thể gửi email - khách hàng không có email");
                return;
            }

            String to = khachHang.getEmail();
            String subject = "Đơn hàng #" + hoaDon.getMaHoaDon() + " - Đã giao thành công";

            String htmlContent = buildDeliveryConfirmationEmailHtml(hoaDon);

            sendHtmlEmail(to, subject, htmlContent);
            log.info("Email xác nhận giao hàng đã gửi cho: {}", to);

        } catch (Exception e) {
            log.error("Lỗi khi gửi email xác nhận giao hàng: ", e);
        }
    }

    /**
     * Gửi email thông báo đơn hàng bị hủy
     * @param hoaDon Đơn hàng bị hủy
     * @param lyDo Lý do hủy
     */
    public void sendOrderCancellationEmail(HoaDon hoaDon, String lyDo) {
        try {
            KhachHang khachHang = hoaDon.getKhachHang();
            if (khachHang == null || khachHang.getEmail() == null) {
                log.warn("Không thể gửi email - khách hàng không có email");
                return;
            }

            String to = khachHang.getEmail();
            String subject = "Đơn hàng #" + hoaDon.getMaHoaDon() + " - Đã bị hủy";

            String htmlContent = buildOrderCancellationEmailHtml(hoaDon, lyDo);

            sendHtmlEmail(to, subject, htmlContent);
            log.info("Email thông báo hủy đơn đã gửi cho: {}", to);

        } catch (Exception e) {
            log.error("Lỗi khi gửi email hủy đơn: ", e);
        }
    }

    // ===== HTML BUILDERS =====

    private String buildOrderConfirmationEmailHtml(HoaDon hoaDon, List<HoaDonChiTiet> chiTiets) {
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));

        StringBuilder html = new StringBuilder();
        html.append("<html><head><meta charset='UTF-8'></head><body style='font-family: Arial, sans-serif;'>");
        html.append("<div style='max-width: 600px; margin: 0 auto; background-color: #f5f5f5; padding: 20px;'>");

        // Header
        html.append("<div style='background-color: #008000; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0;'>");
        html.append("<h1>Xác nhận đặt hàng</h1>");
        html.append("</div>");

        // Content
        html.append("<div style='background-color: white; padding: 20px; border-radius: 0 0 5px 5px;'>");
        html.append("<p>Xin chào ").append(hoaDon.getKhachHang().getHoVaTen()).append(",</p>");
        html.append("<p>Cảm ơn bạn đã đặt hàng tại ZESTIA. Đây là email xác nhận đơn hàng của bạn.</p>");

        // Order Info
        html.append("<h3 style='color: #008000;'>Thông tin đơn hàng</h3>");
        html.append("<table style='width: 100%; border-collapse: collapse;'>");
        html.append("<tr><td><strong>Mã đơn:</strong></td><td>").append(hoaDon.getMaHoaDon()).append("</td></tr>");
        html.append("<tr><td><strong>Ngày đặt:</strong></td><td>").append(hoaDon.getNgayTao()).append("</td></tr>");
        html.append("<tr><td><strong>Địa chỉ giao hàng:</strong></td><td>").append(hoaDon.getDiaChiGiaoHang()).append("</td></tr>");
        html.append("<tr><td><strong>Hình thức thanh toán:</strong></td><td>").append(hoaDon.getHinhThucThanhToan()).append("</td></tr>");
        html.append("</table>");

        // Order Items
        html.append("<h3 style='color: #008000;'>Chi tiết sản phẩm</h3>");
        html.append("<table style='width: 100%; border-collapse: collapse; border: 1px solid #ddd;'>");
        html.append("<tr style='background-color: #f0f0f0;'>");
        html.append("<th style='padding: 10px; text-align: left; border: 1px solid #ddd;'>Sản phẩm</th>");
        html.append("<th style='padding: 10px; text-align: center; border: 1px solid #ddd;'>Kích thước</th>");
        html.append("<th style='padding: 10px; text-align: center; border: 1px solid #ddd;'>Màu sắc</th>");
        html.append("<th style='padding: 10px; text-align: center; border: 1px solid #ddd;'>Số lượng</th>");
        html.append("<th style='padding: 10px; text-align: right; border: 1px solid #ddd;'>Đơn giá</th>");
        html.append("<th style='padding: 10px; text-align: right; border: 1px solid #ddd;'>Thành tiền</th>");
        html.append("</tr>");

        BigDecimal total = BigDecimal.ZERO;
        for (HoaDonChiTiet ct : chiTiets) {
            BigDecimal thanhTien = ct.getDonGia().multiply(BigDecimal.valueOf(ct.getSoLuong()));
            total = total.add(thanhTien);

            html.append("<tr>");
            html.append("<td style='padding: 10px; border: 1px solid #ddd;'>")
                    .append(ct.getVayChiTiet().getVay().getTenVay())
                    .append("</td>");
            html.append("<td style='padding: 10px; text-align: center; border: 1px solid #ddd;'>")
                    .append(ct.getVayChiTiet().getKichThuoc().getTenKichThuoc())
                    .append("</td>");
            html.append("<td style='padding: 10px; text-align: center; border: 1px solid #ddd;'>")
                    .append(ct.getVayChiTiet().getMauSac().getTenMauSac())
                    .append("</td>");
            html.append("<td style='padding: 10px; text-align: center; border: 1px solid #ddd;'>")
                    .append(ct.getSoLuong())
                    .append("</td>");
            html.append("<td style='padding: 10px; text-align: right; border: 1px solid #ddd;'>")
                    .append(nf.format(ct.getDonGia()))
                    .append("đ</td>");
            html.append("<td style='padding: 10px; text-align: right; border: 1px solid #ddd;'>")
                    .append(nf.format(thanhTien))
                    .append("đ</td>");
            html.append("</tr>");
        }
        html.append("</table>");

        // Summary
        html.append("<div style='text-align: right; margin-top: 20px;'>");
        html.append("<p style='font-size: 18px; font-weight: bold; color: #008000;'>Tổng tiền: ")
                .append(nf.format(hoaDon.getTongTien()))
                .append("đ</p>");
        html.append("</div>");

        // Tracking Link
        html.append("<div style='background-color: #f0f0f0; padding: 15px; margin-top: 20px; border-radius: 5px;'>");
        html.append("<p><strong>Theo dõi đơn hàng:</strong></p>");
        html.append("<p><a href='").append(frontendUrl).append("/track/").append(hoaDon.getMaHoaDon())
                .append("' style='color: #008000; text-decoration: none;'>Nhấp vào đây để xem trạng thái đơn hàng</a></p>");
        html.append("</div>");

        // Footer
        html.append("<div style='margin-top: 30px; padding-top: 20px; border-top: 1px solid #ddd; color: #666; font-size: 12px;'>");
        html.append("<p>Nếu bạn có câu hỏi, vui lòng liên hệ với chúng tôi:</p>");
        html.append("<p>Email: support@zestia.vn | Hotline: 0123 456 789</p>");
        html.append("<p>Cảm ơn bạn đã mua hàng tại ZESTIA!</p>");
        html.append("</div>");

        html.append("</div></div></body></html>");
        return html.toString();
    }

    private String buildOrderStatusUpdateEmailHtml(HoaDon hoaDon, String trangThaiMoi, String moTa) {
        StringBuilder html = new StringBuilder();
        html.append("<html><head><meta charset='UTF-8'></head><body style='font-family: Arial, sans-serif;'>");
        html.append("<div style='max-width: 600px; margin: 0 auto; background-color: #f5f5f5; padding: 20px;'>");

        html.append("<div style='background-color: #008000; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0;'>");
        html.append("<h1>Cập nhật trạng thái đơn hàng</h1>");
        html.append("</div>");

        html.append("<div style='background-color: white; padding: 20px; border-radius: 0 0 5px 5px;'>");
        html.append("<p>Xin chào ").append(hoaDon.getKhachHang().getHoVaTen()).append(",</p>");
        html.append("<p>Đơn hàng của bạn có cập nhật mới:</p>");

        html.append("<div style='background-color: #e8f5e9; padding: 15px; margin: 20px 0; border-left: 4px solid #008000;'>");
        html.append("<p><strong>Mã đơn:</strong> ").append(hoaDon.getMaHoaDon()).append("</p>");
        html.append("<p><strong>Trạng thái:</strong> ").append(trangThaiMoi).append("</p>");
        html.append("<p><strong>Mô tả:</strong> ").append(moTa).append("</p>");
        html.append("</div>");

        html.append("<p><a href='").append(frontendUrl).append("/track/").append(hoaDon.getMaHoaDon())
                .append("' style='color: #008000; text-decoration: none;'>Xem chi tiết đơn hàng</a></p>");

        html.append("</div></div></body></html>");
        return html.toString();
    }

    private String buildDeliveryConfirmationEmailHtml(HoaDon hoaDon) {
        StringBuilder html = new StringBuilder();
        html.append("<html><head><meta charset='UTF-8'></head><body style='font-family: Arial, sans-serif;'>");
        html.append("<div style='max-width: 600px; margin: 0 auto; background-color: #f5f5f5; padding: 20px;'>");

        html.append("<div style='background-color: #4CAF50; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0;'>");
        html.append("<h1>✓ Đơn hàng đã giao thành công</h1>");
        html.append("</div>");

        html.append("<div style='background-color: white; padding: 20px; border-radius: 0 0 5px 5px;'>");
        html.append("<p>Xin chào ").append(hoaDon.getKhachHang().getHoVaTen()).append(",</p>");
        html.append("<p>Đơn hàng của bạn đã được giao thành công!</p>");

        html.append("<div style='background-color: #e8f5e9; padding: 15px; margin: 20px 0; border-left: 4px solid #4CAF50;'>");
        html.append("<p><strong>Mã đơn:</strong> ").append(hoaDon.getMaHoaDon()).append("</p>");
        html.append("<p><strong>Ngày giao:</strong> ").append(hoaDon.getNgayGiaoHangThucTe()).append("</p>");
        html.append("</div>");

        html.append("<p>Vui lòng kiểm tra hàng và phản hồi trải nghiệm mua sắm của bạn.</p>");
        html.append("<p>Cảm ơn đã tin tưởng ZESTIA!</p>");

        html.append("</div></div></body></html>");
        return html.toString();
    }

    private String buildOrderCancellationEmailHtml(HoaDon hoaDon, String lyDo) {
        StringBuilder html = new StringBuilder();
        html.append("<html><head><meta charset='UTF-8'></head><body style='font-family: Arial, sans-serif;'>");
        html.append("<div style='max-width: 600px; margin: 0 auto; background-color: #f5f5f5; padding: 20px;'>");

        html.append("<div style='background-color: #f44336; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0;'>");
        html.append("<h1>Đơn hàng đã bị hủy</h1>");
        html.append("</div>");

        html.append("<div style='background-color: white; padding: 20px; border-radius: 0 0 5px 5px;'>");
        html.append("<p>Xin chào ").append(hoaDon.getKhachHang().getHoVaTen()).append(",</p>");
        html.append("<p>Đơn hàng của bạn đã bị hủy.</p>");

        html.append("<div style='background-color: #ffebee; padding: 15px; margin: 20px 0; border-left: 4px solid #f44336;'>");
        html.append("<p><strong>Mã đơn:</strong> ").append(hoaDon.getMaHoaDon()).append("</p>");
        html.append("<p><strong>Lý do:</strong> ").append(lyDo != null ? lyDo : "Không rõ").append("</p>");
        html.append("</div>");

        html.append("<p>Nếu bạn có câu hỏi, vui lòng liên hệ với chúng tôi.</p>");

        html.append("</div></div></body></html>");
        return html.toString();
    }

    // ===== HELPER METHODS =====

    /**
     * Gửi email HTML
     */
    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true); // true = HTML

        mailSender.send(message);
    }

    /**
     * Gửi email văn bản
     */
    private void sendSimpleEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }
}