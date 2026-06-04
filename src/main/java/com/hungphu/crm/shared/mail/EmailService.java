package com.hungphu.crm.shared.mail;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    public void sendWelcomeEmail(String toEmail, String fullName, String plainPassword) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Thông tin đăng nhập hệ thống HungPhu CRM");
            helper.setText(buildWelcomeHtml(fullName, toEmail, plainPassword), true);
            mailSender.send(message);
            log.info("Welcome email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send welcome email to {}: {}", toEmail, e.getMessage());
        }
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String fullName, String resetLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Đặt lại mật khẩu – Hưng Phú CRM");
            helper.setText(buildResetPasswordHtml(fullName, resetLink), true);
            mailSender.send(message);
            log.info("Password reset email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage());
        }
    }

    private String buildResetPasswordHtml(String fullName, String resetLink) {
        return """
                <!DOCTYPE html>
                <html lang="vi">
                <body style="margin:0;padding:0;background:#f4f4f4;font-family:Arial,sans-serif">
                  <div style="max-width:560px;margin:40px auto;background:#fff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,.1)">
                    <div style="background:#1a73e8;padding:24px 32px">
                      <h1 style="margin:0;color:#fff;font-size:22px">HungPhu CRM</h1>
                    </div>
                    <div style="padding:32px">
                      <p style="margin:0 0 16px;font-size:16px">Xin chào <strong>%s</strong>,</p>
                      <p style="margin:0 0 20px;color:#555">Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn. Nhấn nút bên dưới để tiến hành:</p>
                      <div style="text-align:center;margin:28px 0">
                        <a href="%s"
                           style="display:inline-block;padding:14px 32px;background:#1a73e8;color:#fff;text-decoration:none;border-radius:6px;font-size:15px;font-weight:bold">
                          Đặt lại mật khẩu
                        </a>
                      </div>
                      <p style="margin:0 0 8px;color:#555;font-size:14px">Hoặc copy link sau vào trình duyệt:</p>
                      <p style="margin:0 0 20px;word-break:break-all;color:#1a73e8;font-size:13px">%s</p>
                      <p style="margin:0 0 8px;color:#d32f2f;font-size:13px">⚠ Link này sẽ hết hạn sau <strong>15 phút</strong>.</p>
                      <p style="margin:0 0 8px;color:#555;font-size:13px">Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này. Tài khoản của bạn vẫn an toàn.</p>
                      <p style="margin:20px 0 0;color:#999;font-size:12px">Email này được gửi tự động từ hệ thống, vui lòng không trả lời.</p>
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(fullName, resetLink, resetLink);
    }

    private String buildWelcomeHtml(String fullName, String email, String password) {
        return """
                <!DOCTYPE html>
                <html lang="vi">
                <body style="margin:0;padding:0;background:#f4f4f4;font-family:Arial,sans-serif">
                  <div style="max-width:560px;margin:40px auto;background:#fff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,.1)">
                    <div style="background:#1a73e8;padding:24px 32px">
                      <h1 style="margin:0;color:#fff;font-size:22px">HungPhu CRM</h1>
                    </div>
                    <div style="padding:32px">
                      <p style="margin:0 0 16px;font-size:16px">Xin chào <strong>%s</strong>,</p>
                      <p style="margin:0 0 20px;color:#555">Tài khoản của bạn đã được tạo thành công trên hệ thống HungPhu CRM. Dưới đây là thông tin đăng nhập:</p>
                      <div style="background:#f8f9fa;border:1px solid #e0e0e0;border-radius:6px;padding:20px;margin:0 0 20px">
                        <p style="margin:0 0 10px"><span style="color:#888;font-size:13px">Email đăng nhập</span><br>
                          <strong style="font-size:15px">%s</strong></p>
                        <p style="margin:0"><span style="color:#888;font-size:13px">Mật khẩu tạm thời</span><br>
                          <strong style="font-size:18px;letter-spacing:2px;color:#1a73e8">%s</strong></p>
                      </div>
                      <p style="margin:0 0 8px;color:#d32f2f;font-weight:bold">⚠ Vui lòng đổi mật khẩu ngay sau khi đăng nhập lần đầu.</p>
                      <p style="margin:0;color:#999;font-size:12px">Email này được gửi tự động từ hệ thống, vui lòng không trả lời.</p>
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(fullName, email, password);
    }
}
