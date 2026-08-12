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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final ApiLocalizationService localizationService;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    /**
     * Gửi email xác nhận đặt hàng
     * @param hoaDon Đơn hàng vừa tạo
     */
    @Async
    public void sendOrderConfirmationEmail(HoaDon hoaDon) {
        try {
            String to = resolveRecipientEmail(hoaDon);
            if (to == null) {
                log.warn("Không thể gửi email - khách hàng không có email");
                return;
            }

            String subject = "Đơn hàng / Order #" + hoaDon.getMaHoaDon() + " - Xác nhận / Confirmation";

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
    @Async
    public void sendOrderStatusUpdateEmail(HoaDon hoaDon, String trangThaiMoi, String moTa) {
        try {
            String to = resolveRecipientEmail(hoaDon);
            if (to == null) {
                log.warn("Không thể gửi email - khách hàng không có email");
                return;
            }

            String subject = "Đơn hàng / Order #" + hoaDon.getMaHoaDon() + " - Cập nhật / Status update";

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
    @Async
    public void sendDeliveryConfirmationEmail(HoaDon hoaDon) {
        try {
            String to = resolveRecipientEmail(hoaDon);
            if (to == null) {
                log.warn("Không thể gửi email - khách hàng không có email");
                return;
            }

            String subject = "Đơn hàng / Order #" + hoaDon.getMaHoaDon() + " - Đã giao / Delivered";

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
    @Async
    public void sendOrderCancellationEmail(HoaDon hoaDon, String lyDo) {
        try {
            String to = resolveRecipientEmail(hoaDon);
            if (to == null) {
                log.warn("Không thể gửi email - khách hàng không có email");
                return;
            }

            String subject = "Đơn hàng / Order #" + hoaDon.getMaHoaDon() + " - Đã hủy / Cancelled";

            String htmlContent = buildOrderCancellationEmailHtml(hoaDon, lyDo);

            sendHtmlEmail(to, subject, htmlContent);
            log.info("Email thông báo hủy đơn đã gửi cho: {}", to);

        } catch (Exception e) {
            log.error("Lỗi khi gửi email hủy đơn: ", e);
        }
    }

    @Async
    public void sendOrderCancellationOtpEmail(HoaDon hoaDon, String otp, LocalDateTime expiresAt) {
        try {
            String to = resolveRecipientEmail(hoaDon);
            if (to == null) {
                log.warn("Không thể gửi OTP hủy đơn - khách hàng không có email");
                return;
            }

            String expiresText = expiresAt != null
                    ? expiresAt.format(DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy"))
                    : "sau 5 phút";
            String subject = "Đơn hàng / Order #" + hoaDon.getMaHoaDon() + " - OTP hủy đơn / Cancellation OTP";
            StringBuilder html = new StringBuilder();
            html.append("<html><head><meta charset='UTF-8'></head><body style='font-family: Arial, sans-serif;'>");
            html.append("<div style='max-width: 600px; margin: 0 auto; background-color: #f5f5f5; padding: 20px;'>");
            html.append("<div style='background-color: #111111; color: white; padding: 22px; text-align: center; border-radius: 6px 6px 0 0;'>");
            html.append("<h1 style='margin: 0; font-size: 24px;'>ZESTIA FASHION</h1></div>");
            html.append("<div style='background-color: white; padding: 24px; border-radius: 0 0 6px 6px;'>");
            html.append("<h2 style='margin-top: 0; color: #D4564E;'>Xác nhận hủy đơn / Confirm cancellation</h2>");
            html.append("<p>Xin chào / Hello <strong>").append(escapeHtml(resolveRecipientName(hoaDon))).append("</strong>,</p>");
            html.append("<p>Mã OTP để hủy đơn / Your OTP to cancel order <strong>")
                    .append(escapeHtml(hoaDon.getMaHoaDon())).append("</strong>:</p>");
            html.append("<div style='font-size: 32px; font-weight: 700; letter-spacing: 8px; text-align: center; padding: 18px; background: #f5f5f5; border-radius: 6px;'>")
                    .append(escapeHtml(otp)).append("</div>");
            html.append("<p>Mã có hiệu lực đến / Valid until <strong>").append(escapeHtml(expiresText))
                    .append("</strong> và chỉ sử dụng một lần / and can be used once.</p>");
            html.append("<p style='color: #666; font-size: 12px;'>Nếu bạn không yêu cầu hủy đơn, không cung cấp mã này cho bất kỳ ai. / If you did not request cancellation, do not share this code.</p>");
            html.append("</div></div></body></html>");

            sendHtmlEmail(to, subject, html.toString());
            log.info("OTP hủy đơn {} đã gửi cho {}", hoaDon.getMaHoaDon(), to);
        } catch (Exception e) {
            log.error("Lỗi khi gửi OTP hủy đơn: ", e);
        }
    }

    // ===== HTML BUILDERS =====

    private String buildOrderConfirmationEmailHtml(HoaDon hoaDon, List<HoaDonChiTiet> chiTiets) {
        NumberFormat nf = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));

        StringBuilder html = new StringBuilder();
        html.append("<html><head><meta charset='UTF-8'></head><body style='font-family: Arial, sans-serif;'>");
        html.append("<div style='max-width: 600px; margin: 0 auto; background-color: #f5f5f5; padding: 20px;'>");

        // Header
        html.append("<div style='background-color: #008000; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0;'>");
        html.append("<h1>Xác nhận đặt hàng / Order confirmation</h1>");
        html.append("</div>");

        // Content
        html.append("<div style='background-color: white; padding: 20px; border-radius: 0 0 5px 5px;'>");
        html.append("<p>Xin chào / Hello ").append(escapeHtml(resolveRecipientName(hoaDon))).append(",</p>");
        html.append("<p>Cảm ơn bạn đã đặt hàng tại ZESTIA. / Thank you for shopping with ZESTIA.</p>");

        // Order Info
        html.append("<h3 style='color: #008000;'>Thông tin đơn hàng / Order information</h3>");
        html.append("<table style='width: 100%; border-collapse: collapse;'>");
        html.append("<tr><td><strong>Mã đơn / Order code:</strong></td><td>").append(escapeHtml(hoaDon.getMaHoaDon())).append("</td></tr>");
        html.append("<tr><td><strong>Ngày đặt / Order date:</strong></td><td>").append(hoaDon.getNgayTao()).append("</td></tr>");
        html.append("<tr><td><strong>Địa chỉ giao hàng / Delivery address:</strong></td><td>").append(escapeHtml(hoaDon.getDiaChiGiaoHang())).append("</td></tr>");
        html.append("<tr><td><strong>Thanh toán / Payment:</strong></td><td>")
                .append(escapeHtml(bilingualValue(hoaDon.getHinhThucThanhToan()))).append("</td></tr>");
        html.append("</table>");

        // Order Items
        html.append("<h3 style='color: #008000;'>Chi tiết sản phẩm / Items</h3>");
        html.append("<table style='width: 100%; border-collapse: collapse; border: 1px solid #ddd;'>");
        html.append("<tr style='background-color: #f0f0f0;'>");
        html.append("<th style='padding: 10px; text-align: left; border: 1px solid #ddd;'>Sản phẩm<br>Product</th>");
        html.append("<th style='padding: 10px; text-align: center; border: 1px solid #ddd;'>Cỡ<br>Size</th>");
        html.append("<th style='padding: 10px; text-align: center; border: 1px solid #ddd;'>Màu<br>Color</th>");
        html.append("<th style='padding: 10px; text-align: center; border: 1px solid #ddd;'>SL<br>Qty</th>");
        html.append("<th style='padding: 10px; text-align: right; border: 1px solid #ddd;'>Đơn giá<br>Price</th>");
        html.append("<th style='padding: 10px; text-align: right; border: 1px solid #ddd;'>Thành tiền<br>Amount</th>");
        html.append("</tr>");

        BigDecimal total = BigDecimal.ZERO;
        for (HoaDonChiTiet ct : chiTiets) {
            BigDecimal donGia = ct.getDonGia() != null ? ct.getDonGia() : BigDecimal.ZERO;
            int soLuong = ct.getSoLuong() != null ? ct.getSoLuong() : 0;
            BigDecimal thanhTien = donGia.multiply(BigDecimal.valueOf(soLuong));
            total = total.add(thanhTien);

            html.append("<tr>");
            html.append("<td style='padding: 10px; border: 1px solid #ddd;'>")
                    .append(escapeHtml(productName(ct)))
                    .append("</td>");
            html.append("<td style='padding: 10px; text-align: center; border: 1px solid #ddd;'>")
                    .append(escapeHtml(sizeName(ct)))
                    .append("</td>");
            html.append("<td style='padding: 10px; text-align: center; border: 1px solid #ddd;'>")
                    .append(escapeHtml(bilingualValue(colorName(ct))))
                    .append("</td>");
            html.append("<td style='padding: 10px; text-align: center; border: 1px solid #ddd;'>")
                    .append(soLuong)
                    .append("</td>");
            html.append("<td style='padding: 10px; text-align: right; border: 1px solid #ddd;'>")
                    .append(nf.format(donGia))
                    .append("đ</td>");
            html.append("<td style='padding: 10px; text-align: right; border: 1px solid #ddd;'>")
                    .append(nf.format(thanhTien))
                    .append("đ</td>");
            html.append("</tr>");
        }
        html.append("</table>");

        // Summary
        html.append("<div style='text-align: right; margin-top: 20px;'>");
        html.append("<p style='font-size: 18px; font-weight: bold; color: #008000;'>Tổng tiền / Total: ")
                .append(nf.format(hoaDon.getTongTien()))
                .append("đ</p>");
        html.append("</div>");

        // Tracking Link
        html.append("<div style='background-color: #f0f0f0; padding: 15px; margin-top: 20px; border-radius: 5px;'>");
        html.append("<p><strong>Theo dõi đơn hàng / Track your order:</strong></p>");
        html.append("<p><a href='").append(escapeHtml(trackingUrl(hoaDon)))
                .append("' style='color: #008000; text-decoration: none;'>Xem trạng thái / View order status</a></p>");
        html.append("</div>");

        // Footer
        html.append("<div style='margin-top: 30px; padding-top: 20px; border-top: 1px solid #ddd; color: #666; font-size: 12px;'>");
        html.append("<p>Nếu cần hỗ trợ, hãy liên hệ / Need help? Contact us:</p>");
        html.append("<p>Email: nguyenthanh.hn090307@gmail.com | Hotline: 0869 167 207</p>");
        html.append("<p>39, Nguyễn Thị Duệ, Yên Hòa, Hà Nội</p>");
        html.append("<p>Cảm ơn bạn đã mua hàng tại ZESTIA! / Thank you for shopping with ZESTIA!</p>");
        html.append("</div>");

        html.append("</div></div></body></html>");
        return html.toString();
    }

    private String buildOrderStatusUpdateEmailHtml(HoaDon hoaDon, String trangThaiMoi, String moTa) {
        StringBuilder html = new StringBuilder();
        html.append("<html><head><meta charset='UTF-8'></head><body style='font-family: Arial, sans-serif;'>");
        html.append("<div style='max-width: 600px; margin: 0 auto; background-color: #f5f5f5; padding: 20px;'>");

        html.append("<div style='background-color: #008000; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0;'>");
        html.append("<h1>Cập nhật đơn hàng / Order update</h1>");
        html.append("</div>");

        html.append("<div style='background-color: white; padding: 20px; border-radius: 0 0 5px 5px;'>");
        html.append("<p>Xin chào / Hello ").append(escapeHtml(resolveRecipientName(hoaDon))).append(",</p>");
        html.append("<p>Đơn hàng của bạn có cập nhật mới. / Your order has a new update.</p>");

        html.append("<div style='background-color: #e8f5e9; padding: 15px; margin: 20px 0; border-left: 4px solid #008000;'>");
        html.append("<p><strong>Mã đơn / Order code:</strong> ").append(escapeHtml(hoaDon.getMaHoaDon())).append("</p>");
        html.append("<p><strong>Trạng thái / Status:</strong> ").append(escapeHtml(bilingualValue(trangThaiMoi))).append("</p>");
        html.append("<p><strong>Mô tả / Details:</strong> ").append(escapeHtml(bilingualValue(moTa))).append("</p>");
        html.append("</div>");

        html.append("<p><a href='").append(escapeHtml(trackingUrl(hoaDon)))
                .append("' style='color: #008000; text-decoration: none;'>Xem chi tiết / View details</a></p>");

        html.append("</div></div></body></html>");
        return html.toString();
    }

    private String buildDeliveryConfirmationEmailHtml(HoaDon hoaDon) {
        StringBuilder html = new StringBuilder();
        html.append("<html><head><meta charset='UTF-8'></head><body style='font-family: Arial, sans-serif;'>");
        html.append("<div style='max-width: 600px; margin: 0 auto; background-color: #f5f5f5; padding: 20px;'>");

        html.append("<div style='background-color: #4CAF50; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0;'>");
        html.append("<h1>Đã giao thành công / Delivered</h1>");
        html.append("</div>");

        html.append("<div style='background-color: white; padding: 20px; border-radius: 0 0 5px 5px;'>");
        html.append("<p>Xin chào / Hello ").append(escapeHtml(resolveRecipientName(hoaDon))).append(",</p>");
        html.append("<p>Đơn hàng của bạn đã được giao thành công! / Your order has been delivered.</p>");

        html.append("<div style='background-color: #e8f5e9; padding: 15px; margin: 20px 0; border-left: 4px solid #4CAF50;'>");
        html.append("<p><strong>Mã đơn / Order code:</strong> ").append(escapeHtml(hoaDon.getMaHoaDon())).append("</p>");
        html.append("<p><strong>Ngày giao / Delivered at:</strong> ").append(hoaDon.getNgayGiaoHangThucTe()).append("</p>");
        html.append("</div>");

        html.append("<p>Vui lòng kiểm tra hàng và chia sẻ đánh giá. / Please inspect your items and share a review.</p>");
        html.append("<p>Cảm ơn đã tin tưởng ZESTIA! / Thank you for choosing ZESTIA!</p>");

        html.append("</div></div></body></html>");
        return html.toString();
    }

    private String buildOrderCancellationEmailHtml(HoaDon hoaDon, String lyDo) {
        StringBuilder html = new StringBuilder();
        html.append("<html><head><meta charset='UTF-8'></head><body style='font-family: Arial, sans-serif;'>");
        html.append("<div style='max-width: 600px; margin: 0 auto; background-color: #f5f5f5; padding: 20px;'>");

        html.append("<div style='background-color: #f44336; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0;'>");
        html.append("<h1>Đơn hàng đã hủy / Order cancelled</h1>");
        html.append("</div>");

        html.append("<div style='background-color: white; padding: 20px; border-radius: 0 0 5px 5px;'>");
        html.append("<p>Xin chào / Hello ").append(escapeHtml(resolveRecipientName(hoaDon))).append(",</p>");
        html.append("<p>Đơn hàng của bạn đã bị hủy. / Your order has been cancelled.</p>");

        html.append("<div style='background-color: #ffebee; padding: 15px; margin: 20px 0; border-left: 4px solid #f44336;'>");
        html.append("<p><strong>Mã đơn / Order code:</strong> ").append(escapeHtml(hoaDon.getMaHoaDon())).append("</p>");
        html.append("<p><strong>Lý do / Reason:</strong> ")
                .append(escapeHtml(bilingualValue(lyDo != null ? lyDo : "Không rõ"))).append("</p>");
        html.append("</div>");

        html.append("<p>Nếu cần hỗ trợ, hãy liên hệ Zestia. / Contact Zestia if you need assistance.</p>");

        html.append("</div></div></body></html>");
        return html.toString();
    }

    // ===== HELPER METHODS =====

    /**
     * Gửi email HTML
     */
    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        if (!isConfigured()) {
            log.warn("Bỏ qua gửi email đến {} vì MAIL_USERNAME/MAIL_PASSWORD chưa được cấu hình", to);
            return;
        }
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
        if (!isConfigured()) {
            log.warn("Bỏ qua gửi email đến {} vì MAIL_USERNAME/MAIL_PASSWORD chưa được cấu hình", to);
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }

    /**
     * Gửi email thông báo từ cửa hàng
     */
    @Async
    public void sendAnnouncementEmail(String to, String hoVaTen, String tieuDe, String noiDung) {
        sendAnnouncementEmailNow(to, hoVaTen, tieuDe, noiDung);
    }

    public boolean sendAnnouncementEmailNow(String to, String hoVaTen, String tieuDe, String noiDung) {
        try {
            if (!isConfigured()) return false;
            String htmlContent = buildAnnouncementEmailHtml(hoVaTen, tieuDe, noiDung);
            sendHtmlEmail(to, "Zestia - Thông báo / Announcement - " + tieuDe, htmlContent);
            log.info("Email thông báo đã gửi cho: {}", to);
            return true;
        } catch (Exception e) {
            log.error("Lỗi khi gửi email thông báo đến " + to + ": ", e);
            return false;
        }
    }

    @Async
    public void sendNewsletterWelcomeEmail(String to) {
        try {
            String subject = "Zestia - Đăng ký nhận tin thành công / Newsletter confirmed";
            String htmlContent = buildNewsletterWelcomeEmailHtml();
            sendHtmlEmail(to, subject, htmlContent);
            log.info("Email xác nhận newsletter đã gửi cho: {}", to);
        } catch (Exception e) {
            log.error("Lỗi khi gửi email xác nhận newsletter đến " + to + ": ", e);
        }
    }

    @Async
    public void sendPasswordResetEmail(String to, String hoVaTen, String token, LocalDateTime expiresAt) {
        try {
            String subject = "Zestia - Đặt lại mật khẩu / Password reset";
            String htmlContent = buildPasswordResetEmailHtml(hoVaTen, token, expiresAt);
            sendHtmlEmail(to, subject, htmlContent);
            log.info("Email đặt lại mật khẩu đã gửi cho: {}", to);
        } catch (Exception e) {
            log.error("Lỗi khi gửi email đặt lại mật khẩu đến " + to + ": ", e);
        }
    }

    private String buildAnnouncementEmailHtml(String hoVaTen, String tieuDe, String noiDung) {
        StringBuilder html = new StringBuilder();
        html.append("<html><head><meta charset='UTF-8'></head><body style='font-family: Arial, sans-serif;'>");
        html.append("<div style='max-width: 600px; margin: 0 auto; background-color: #f5f5f5; padding: 20px;'>");

        // Header
        html.append("<div style='background-color: #D4564E; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0;'>");
        html.append("<h1 style='margin: 0; font-size: 24px; font-weight: bold;'>ZESTIA FASHION</h1>");
        html.append("</div>");

        // Content
        html.append("<div style='background-color: white; padding: 20px; border-radius: 0 0 5px 5px;'>");
        html.append("<p>Xin chào / Hello <strong>").append(escapeHtml(hoVaTen)).append("</strong>,</p>");
        html.append("<h2 style='color: #D4564E; margin-top: 20px; font-size: 18px;'>").append(escapeHtml(tieuDe)).append("</h2>");
        html.append("<div style='line-height: 1.6; font-size: 14px; white-space: pre-wrap; margin-top: 15px; color: #333;'>");
        html.append(escapeHtml(noiDung));
        html.append("</div>");

        // CTA
        html.append("<div style='text-align: center; margin: 30px 0;'>");
        html.append("<a href='").append(frontendUrl).append("' style='background-color: #D4564E; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block;'>Ghé thăm Zestia / Visit Zestia</a>");
        html.append("</div>");

        // Footer
        html.append("<div style='margin-top: 30px; padding-top: 20px; border-top: 1px solid #ddd; color: #666; font-size: 12px; text-align: center;'>");
        html.append("<p>Nếu cần hỗ trợ, hãy liên hệ / Need help? Contact us:</p>");
        html.append("<p>Email: nguyenthanh.hn090307@gmail.com | Hotline: 0869 167 207</p>");
        html.append("<p>39, Nguyễn Thị Duệ, Yên Hòa, Hà Nội</p>");
        html.append("<p>Cảm ơn bạn đã đồng hành cùng ZESTIA! / Thank you for being with ZESTIA!</p>");
        html.append("</div>");

        html.append("</div></div></body></html>");
        return html.toString();
    }

    private String buildNewsletterWelcomeEmailHtml() {
        StringBuilder html = new StringBuilder();
        html.append("<html><head><meta charset='UTF-8'></head><body style='font-family: Arial, sans-serif;'>");
        html.append("<div style='max-width: 600px; margin: 0 auto; background-color: #f5f5f5; padding: 20px;'>");
        html.append("<div style='background-color: #111111; color: white; padding: 22px; text-align: center; border-radius: 6px 6px 0 0;'>");
        html.append("<h1 style='margin: 0; font-size: 24px;'>ZESTIA FASHION</h1>");
        html.append("</div>");
        html.append("<div style='background-color: white; padding: 24px; border-radius: 0 0 6px 6px;'>");
        html.append("<h2 style='margin-top: 0; color: #D4564E;'>Đăng ký nhận tin thành công / Subscription confirmed</h2>");
        html.append("<p>Cảm ơn bạn đã đăng ký nhận ưu đãi từ Zestia. / Thank you for subscribing to Zestia.</p>");
        html.append("<p>Bạn sẽ nhận thông tin bộ sưu tập, voucher và gợi ý phối đồ. / You will receive collection news, vouchers and styling ideas.</p>");
        html.append("<div style='text-align: center; margin: 28px 0;'>");
        html.append("<a href='").append(frontendUrl).append("' style='background-color: #111111; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block;'>Khám phá / Explore Zestia</a>");
        html.append("</div>");
        html.append("<p style='color: #666; font-size: 12px;'>Nếu bạn không đăng ký, hãy bỏ qua email này. / If you did not subscribe, please ignore this email.</p>");
        html.append("</div></div></body></html>");
        return html.toString();
    }

    private String buildPasswordResetEmailHtml(String hoVaTen, String token, LocalDateTime expiresAt) {
        String resetUrl = passwordResetUrl(token);
        String expiresText = expiresAt != null
                ? expiresAt.format(DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy"))
                : "30 phút";

        StringBuilder html = new StringBuilder();
        html.append("<html><head><meta charset='UTF-8'></head><body style='font-family: Arial, sans-serif;'>");
        html.append("<div style='max-width: 600px; margin: 0 auto; background-color: #f5f5f5; padding: 20px;'>");
        html.append("<div style='background-color: #111111; color: white; padding: 22px; text-align: center; border-radius: 6px 6px 0 0;'>");
        html.append("<h1 style='margin: 0; font-size: 24px;'>ZESTIA FASHION</h1>");
        html.append("</div>");
        html.append("<div style='background-color: white; padding: 24px; border-radius: 0 0 6px 6px;'>");
        html.append("<h2 style='margin-top: 0; color: #D4564E;'>Đặt lại mật khẩu / Password reset</h2>");
        html.append("<p>Xin chào / Hello <strong>").append(escapeHtml(hoVaTen != null ? hoVaTen : "thành viên Zestia")).append("</strong>,</p>");
        html.append("<p>Hệ thống nhận được yêu cầu đặt lại mật khẩu. / We received a password reset request for your account.</p>");
        html.append("<div style='text-align: center; margin: 28px 0;'>");
        html.append("<a href='").append(escapeHtml(resetUrl)).append("' style='background-color: #111111; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block;'>Đặt lại mật khẩu / Reset password</a>");
        html.append("</div>");
        html.append("<p>Đường dẫn có hiệu lực đến / Link valid until <strong>").append(escapeHtml(expiresText)).append("</strong> và chỉ sử dụng một lần / and can be used once.</p>");
        html.append("<p style='color: #666; font-size: 12px;'>Nếu bạn không yêu cầu, hãy bỏ qua email. / If you did not request this, please ignore the email.</p>");
        html.append("</div></div></body></html>");
        return html.toString();
    }

    private String resolveRecipientEmail(HoaDon hoaDon) {
        if (hoaDon == null) return null;
        KhachHang khachHang = hoaDon.getKhachHang();
        if (khachHang != null && khachHang.getEmail() != null && !khachHang.getEmail().isBlank()) {
            return khachHang.getEmail().trim();
        }
        String email = hoaDon.getEmailKhachHang();
        return email != null && !email.isBlank() ? email.trim() : null;
    }

    private String resolveRecipientName(HoaDon hoaDon) {
        if (hoaDon == null) return "Quý khách";
        KhachHang khachHang = hoaDon.getKhachHang();
        if (khachHang != null && khachHang.getHoVaTen() != null && !khachHang.getHoVaTen().isBlank()) {
            return khachHang.getHoVaTen();
        }
        if (hoaDon.getTenKhachHang() != null && !hoaDon.getTenKhachHang().isBlank()) {
            return hoaDon.getTenKhachHang();
        }
        return "Quý khách";
    }

    private String resolveRecipientPhone(HoaDon hoaDon) {
        if (hoaDon == null) return null;
        KhachHang khachHang = hoaDon.getKhachHang();
        if (khachHang != null && khachHang.getSoDienThoai() != null && !khachHang.getSoDienThoai().isBlank()) {
            return khachHang.getSoDienThoai().trim();
        }
        return hoaDon.getSoDienThoai() != null && !hoaDon.getSoDienThoai().isBlank()
                ? hoaDon.getSoDienThoai().trim()
                : null;
    }

    private String trackingUrl(HoaDon hoaDon) {
        String base = frontendUrl != null && frontendUrl.endsWith("/")
                ? frontendUrl.substring(0, frontendUrl.length() - 1)
                : frontendUrl;
        String url = (base != null && !base.isBlank() ? base : "http://localhost:5173") + "/tracking";
        String maHoaDon = hoaDon != null ? hoaDon.getMaHoaDon() : null;
        String phone = resolveRecipientPhone(hoaDon);
        if (maHoaDon == null || maHoaDon.isBlank() || phone == null || phone.isBlank()) {
            return url;
        }
        return url + "?maHoaDon=" + urlEncode(maHoaDon) + "&soDienThoai=" + urlEncode(phone);
    }

    private String passwordResetUrl(String token) {
        String base = frontendUrl != null && frontendUrl.endsWith("/")
                ? frontendUrl.substring(0, frontendUrl.length() - 1)
                : frontendUrl;
        String url = (base != null && !base.isBlank() ? base : "http://localhost:5173") + "/login";
        return url + "?resetToken=" + urlEncode(token);
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    private String productName(HoaDonChiTiet ct) {
        if (ct != null && ct.getTenSanPhamSnapshot() != null && !ct.getTenSanPhamSnapshot().isBlank()) {
            return ct.getTenSanPhamSnapshot();
        }
        if (ct == null || ct.getSanPhamChiTiet() == null || ct.getSanPhamChiTiet().getSanPham() == null) return "Sản phẩm";
        return ct.getSanPhamChiTiet().getSanPham().getTenSanPham();
    }

    private String sizeName(HoaDonChiTiet ct) {
        if (ct != null && ct.getKichThuocSnapshot() != null && !ct.getKichThuocSnapshot().isBlank()) {
            return ct.getKichThuocSnapshot();
        }
        if (ct == null || ct.getSanPhamChiTiet() == null || ct.getSanPhamChiTiet().getKichThuoc() == null) return "N/A";
        return ct.getSanPhamChiTiet().getKichThuoc().getTenKichThuoc();
    }

    private String colorName(HoaDonChiTiet ct) {
        if (ct != null && ct.getMauSacSnapshot() != null && !ct.getMauSacSnapshot().isBlank()) {
            return ct.getMauSacSnapshot();
        }
        if (ct == null || ct.getSanPhamChiTiet() == null || ct.getSanPhamChiTiet().getMauSac() == null) return "N/A";
        return ct.getSanPhamChiTiet().getMauSac().getTenMauSac();
    }

    private String bilingualValue(String value) {
        if (value == null || value.isBlank()) return "";
        String english = localizationService.translate(value);
        return value.equalsIgnoreCase(english) ? value : value + " / " + english;
    }

    public boolean isConfigured() {
        return fromEmail != null
                && !fromEmail.isBlank()
                && !"your_email@gmail.com".equalsIgnoreCase(fromEmail.trim())
                && mailPassword != null
                && !mailPassword.isBlank()
                && !"your_app_password".equalsIgnoreCase(mailPassword.trim());
    }

    private String escapeHtml(Object value) {
        if (value == null) return "";
        return String.valueOf(value)
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
