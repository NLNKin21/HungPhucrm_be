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

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    // ══════════════════════════════════════════════════════════════
    // Existing — giữ nguyên
    // ══════════════════════════════════════════════════════════════

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

    // ══════════════════════════════════════════════════════════════
    // ★ MỚI — Maintenance emails
    // ══════════════════════════════════════════════════════════════

    @Async
    public void sendMaintenanceReminder(String toEmail, String taskTitle,
                                        String scheduledDate, String assigneeName,
                                        int daysBeforeDue) {
        String subject = String.format("🔔 Nhắc lịch bảo trì — còn %d ngày", daysBeforeDue);
        String html = buildMaintenanceEmail(
                "Nhắc lịch bảo trì",
                String.format("Tác vụ <strong>\"%s\"</strong> sẽ đến hạn trong "
                        + "<strong>%d ngày</strong> nữa.", taskTitle, daysBeforeDue),
                new String[][]{
                        {"Tác vụ", taskTitle},
                        {"Ngày hẹn", scheduledDate},
                        {"Phụ trách", assigneeName},
                        {"Còn lại", daysBeforeDue + " ngày"},
                },
                "#D97706", "⏰"
        );
        sendHtml(toEmail, subject, html);
    }

    @Async
    public void sendMaintenanceOverdue(String toEmail, String taskTitle,
                                       String scheduledDate, String assigneeName,
                                       int daysOverdue) {
        String subject = daysOverdue == 0
                ? "⚠️ Tác vụ bảo trì hết hạn hôm nay"
                : String.format("⚠️ Tác vụ bảo trì quá hạn %d ngày", daysOverdue);
        String html = buildMaintenanceEmail(
                "Cảnh báo quá hạn",
                daysOverdue == 0
                        ? String.format("Tác vụ <strong>\"%s\"</strong> hết hạn hôm nay "
                                + "nhưng chưa hoàn thành.", taskTitle)
                        : String.format("Tác vụ <strong>\"%s\"</strong> đã quá hạn "
                                + "<strong>%d ngày</strong>. Vui lòng xử lý ngay.",
                                taskTitle, daysOverdue),
                new String[][]{
                        {"Tác vụ", taskTitle},
                        {"Ngày hẹn", scheduledDate},
                        {"Phụ trách", assigneeName},
                        {"Quá hạn", daysOverdue == 0 ? "Hôm nay" : daysOverdue + " ngày"},
                },
                "#DC2626", "🚨"
        );
        sendHtml(toEmail, subject, html);
    }

    @Async
    public void sendMaintenanceSubmitted(String toEmail, String taskTitle,
                                         String submitterName) {
        String subject = "📋 Tác vụ bảo trì chờ duyệt — " + taskTitle;
        String html = buildMaintenanceEmail(
                "Tác vụ chờ duyệt",
                String.format("KTV <strong>%s</strong> đã gửi minh chứng cho tác vụ "
                        + "<strong>\"%s\"</strong>. Vui lòng duyệt.",
                        submitterName, taskTitle),
                new String[][]{
                        {"Tác vụ", taskTitle},
                        {"Người gửi", submitterName},
                },
                "#D97706", "📋"
        );
        sendHtml(toEmail, subject, html);
    }

    @Async
    public void sendMaintenanceApproved(String toEmail, String taskTitle,
                                        String approverName) {
        String subject = "✅ Tác vụ bảo trì đã duyệt — " + taskTitle;
        String html = buildMaintenanceEmail(
                "Tác vụ đã được duyệt",
                String.format("Tác vụ <strong>\"%s\"</strong> đã được "
                        + "<strong>%s</strong> duyệt hoàn thành.",
                        taskTitle, approverName),
                new String[][]{
                        {"Tác vụ", taskTitle},
                        {"Người duyệt", approverName},
                },
                "#16A34A", "✅"
        );
        sendHtml(toEmail, subject, html);
    }

    @Async
    public void sendMaintenanceRejected(String toEmail, String taskTitle,
                                        String approverName, String reason) {
        String subject = "❌ Tác vụ bảo trì bị từ chối — " + taskTitle;
        String html = buildMaintenanceEmail(
                "Tác vụ bị từ chối",
                String.format("Tác vụ <strong>\"%s\"</strong> đã bị "
                        + "<strong>%s</strong> từ chối. Vui lòng bổ sung minh chứng.",
                        taskTitle, approverName),
                new String[][]{
                        {"Tác vụ", taskTitle},
                        {"Người duyệt", approverName},
                        {"Lý do", reason},
                },
                "#DC2626", "❌"
        );
        sendHtml(toEmail, subject, html);
    }

    // ══════════════════════════════════════════════════════════════
    // Private helpers
    // ══════════════════════════════════════════════════════════════

    private void sendHtml(String toEmail, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("Email sent to {}: {}", toEmail, subject);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
        }
    }

    private String buildMaintenanceEmail(String title, String messageText,
                                         String[][] details, String accentColor,
                                         String emoji) {
        StringBuilder detailRows = new StringBuilder();
        for (String[] row : details) {
            detailRows.append(String.format(
                    "<tr>"
                  + "<td style=\"padding:8px 12px;color:#64748B;font-size:13px;white-space:nowrap;\">%s</td>"
                  + "<td style=\"padding:8px 12px;color:#0F172A;font-size:13px;font-weight:600;\">%s</td>"
                  + "</tr>",
                    row[0], row[1]
            ));
        }

        return """
            <!DOCTYPE html>
            <html lang="vi">
            <head><meta charset="UTF-8"></head>
            <body style="margin:0;padding:0;background:#F8FAFC;font-family:'Segoe UI',Roboto,Arial,sans-serif;">
              <div style="max-width:560px;margin:32px auto;background:#FFFFFF;border-radius:12px;
                          border:1px solid #E2E8F0;overflow:hidden;">

                <!-- Header -->
                <div style="background:%s;padding:24px 32px;">
                  <div style="font-size:28px;margin-bottom:8px;">%s</div>
                  <h2 style="margin:0;color:#FFFFFF;font-size:18px;font-weight:700;">%s</h2>
                </div>

                <!-- Body -->
                <div style="padding:24px 32px;">
                  <p style="color:#334155;font-size:14px;line-height:1.6;margin:0 0 20px;">
                    %s
                  </p>

                  <!-- Details -->
                  <table style="width:100%%;border-collapse:collapse;background:#F8FAFC;
                                border-radius:8px;overflow:hidden;border:1px solid #E2E8F0;">
                    %s
                  </table>

                  <!-- CTA -->
                  <div style="text-align:center;margin-top:24px;">
                    <a href="%s" style="display:inline-block;padding:10px 24px;
                       background:%s;color:#FFFFFF;text-decoration:none;
                       border-radius:8px;font-size:14px;font-weight:600;">
                      Xem chi tiết trên CRM
                    </a>
                  </div>
                </div>

                <!-- Footer -->
                <div style="padding:16px 32px;background:#F8FAFC;border-top:1px solid #E2E8F0;
                            text-align:center;">
                  <p style="margin:0;color:#94A3B8;font-size:11px;">
                    Email tự động từ Hưng Phú CRM · Vui lòng không trả lời email này
                  </p>
                </div>
              </div>
            </body>
            </html>
            """.formatted(
                accentColor, emoji, title,
                messageText,
                detailRows.toString(),
                frontendUrl,
                accentColor
        );
    }

    // ── Existing HTML builders (giữ nguyên) ──

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