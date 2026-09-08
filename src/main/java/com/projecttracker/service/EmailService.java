package com.projecttracker.service;

import com.projecttracker.exception.BusinessException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Service xử lý gửi Email thông qua Spring JavaMailSender (Gmail SMTP).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Gửi email mã OTP xác thực dạng HTML bất đồng bộ (Async).
     *
     * @param toEmail Email người nhận
     * @param otpCode Mã OTP 6 chữ số
     */
    @Async
    public void sendOtpEmail(String toEmail, String otpCode) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("🔑 Mã xác thực OTP - Project Tracker");

            String htmlContent = buildOtpEmailTemplate(otpCode);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Đã gửi thành công email OTP tới: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Lỗi khi gửi email OTP tới {}: {}", toEmail, e.getMessage(), e);
            throw new BusinessException("Không thể gửi email OTP. Vui lòng thử lại sau!");
        } catch (Exception e) {
            log.error("Lỗi không xác định khi gửi email: {}", e.getMessage(), e);
            throw new BusinessException("Hệ thống gửi email gặp sự cố: " + e.getMessage());
        }
    }

    /**
     * Gửi email thông báo khi người dùng có thông báo mới bất đồng bộ (Async).
     *
     * @param toEmail Email người nhận
     * @param title Tiêu đề thông báo
     * @param messageContent Nội dung thông báo
     */
    @Async
    public void sendNotificationEmail(String toEmail, String title, String messageContent) {
        if (toEmail == null || toEmail.isBlank()) return;
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("🔔 " + title + " - Project Tracker");

            String htmlContent = buildNotificationEmailTemplate(title, messageContent);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Đã gửi thành công email thông báo tới: {}", toEmail);
        } catch (Exception e) {
            log.error("Lỗi khi gửi email thông báo tới {}: {}", toEmail, e.getMessage());
        }
    }

    /**
     * Tạo template HTML cho Email thông báo hệ thống.
     */
    private String buildNotificationEmailTemplate(String title, String messageContent) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: 'Segoe UI', Arial, sans-serif; background-color: #f4f6f9; margin: 0; padding: 20px; }
                    .container { max-width: 550px; margin: 0 auto; background: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 15px rgba(0,0,0,0.08); }
                    .header { background: linear-gradient(135deg, #3b82f6, #6366f1); padding: 25px; text-align: center; color: white; }
                    .header h1 { margin: 0; font-size: 22px; font-weight: 700; }
                    .content { padding: 30px; color: #334155; }
                    .content h2 { margin-top: 0; font-size: 18px; color: #1e293b; }
                    .content p { font-size: 15px; line-height: 1.6; color: #475569; background: #f8fafc; padding: 15px; border-radius: 8px; border-left: 4px solid #6366f1; }
                    .footer { background-color: #f8fafc; padding: 20px; text-align: center; font-size: 13px; color: #94a3b8; border-top: 1px solid #e2e8f0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Project Tracker</h1>
                    </div>
                    <div class="content">
                        <h2>🔔 Thông Báo Mới</h2>
                        <p>%s</p>
                    </div>
                    <div class="footer">
                        <p>Đây là thông báo tự động từ hệ thống Project Tracker, vui lòng không phản hồi email này.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(messageContent);
    }

    /**
     * Tạo template HTML cho Email OTP với giao diện hiện đại, chuyên nghiệp.
     */
    private String buildOtpEmailTemplate(String otpCode) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: 'Segoe UI', Arial, sans-serif; background-color: #f4f6f9; margin: 0; padding: 20px; }
                    .container { max-width: 550px; margin: 0 auto; background: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 15px rgba(0,0,0,0.08); }
                    .header { background: linear-gradient(135deg, #4f46e5, #6366f1); padding: 30px; text-align: center; color: white; }
                    .header h1 { margin: 0; font-size: 24px; font-weight: 700; }
                    .content { padding: 30px; text-align: center; color: #334155; }
                    .content p { font-size: 15px; line-height: 1.6; margin-bottom: 20px; }
                    .otp-box { display: inline-block; background-color: #eef2ff; color: #4338ca; border: 2px dashed #6366f1; border-radius: 10px; padding: 15px 30px; font-size: 32px; font-weight: bold; letter-spacing: 6px; margin: 20px 0; }
                    .footer { background-color: #f8fafc; padding: 20px; text-align: center; font-size: 13px; color: #94a3b8; border-top: 1px solid #e2e8f0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Project Tracker</h1>
                    </div>
                    <div class="content">
                        <h2>Xác thực tài khoản của bạn</h2>
                        <p>Cảm ơn bạn đã đăng ký tài khoản tại <strong>Project Tracker</strong>. Dưới đây là mã xác thực OTP của bạn:</p>
                        <div class="otp-box">%s</div>
                        <p>Mã OTP này có hiệu lực trong vòng <strong>5 phút</strong>. Tuyệt đối không chia sẻ mã này với bất kỳ ai để bảo vệ tài khoản.</p>
                    </div>
                    <div class="footer">
                        <p>Đây là email tự động từ hệ thống Project Tracker, vui lòng không phản hồi email này.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(otpCode);
    }
}
