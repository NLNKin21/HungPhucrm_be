package com.hungphu.crm.features.maintenance.job;

import com.hungphu.crm.features.maintenance.entity.MaintenanceTask;
import com.hungphu.crm.features.maintenance.repository.MaintenanceTaskRepository;
import com.hungphu.crm.features.notification.NotificationService;
import com.hungphu.crm.features.user.entity.User;
import com.hungphu.crm.features.user.repository.UserRepository;
import com.hungphu.crm.shared.mail.EmailService;
import com.hungphu.crm.shared.enums.NotificationType;
import com.hungphu.crm.shared.enums.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class MaintenanceOverdueAlertJob {

    private final MaintenanceTaskRepository taskRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final UserRepository userRepository;

    /**
     * 17h mỗi ngày — cảnh báo task hết hạn hôm nay mà chưa hoàn thành
     * Gửi cho: Người phụ trách + Supervisor + Admin + Manager
     */
    @Scheduled(cron = "0 0 17 * * *", zone = "Asia/Ho_Chi_Minh")
    @Transactional(readOnly = true)
    public void sendOverdueAlerts() {
        log.info("Running 17h overdue alert job...");

        LocalDate today = LocalDate.now();

        // Tìm task hẹn hôm nay mà chưa hoàn thành
        List<MaintenanceTask> dueTodayTasks = taskRepository.findDueOnDate(today);

        if (dueTodayTasks.isEmpty()) {
            log.info("No tasks due today that are unfinished");
            return;
        }

        // Lấy danh sách Admin + Manager
        List<User> adminsManagers = userRepository.findByRoleIn(
                List.of(UserRole.ADMIN, UserRole.MANAGER));

        int alertCount = 0;

        for (MaintenanceTask task : dueTodayTasks) {
            Set<UUID> notifiedUsers = new HashSet<>();

            String title = "⚠️ Tác vụ bảo trì hết hạn hôm nay";
            String body = String.format(
                    "Tác vụ \"%s\" hết hạn hôm nay (%s) nhưng chưa hoàn thành",
                    task.getTitle(), today
            );

            // ── Gửi cho người phụ trách ──
            if (task.getAssignedTo() != null) {
                sendAlert(task.getAssignedTo(), title, body, task);
                notifiedUsers.add(task.getAssignedTo().getId());
            }

            // ── Gửi cho supervisor ──
            if (task.getSupervisor() != null
                    && !notifiedUsers.contains(task.getSupervisor().getId())) {
                sendAlert(task.getSupervisor(), title, body, task);
                notifiedUsers.add(task.getSupervisor().getId());
            }

            // ── Gửi cho Admin + Manager ──
            for (User admin : adminsManagers) {
                if (!notifiedUsers.contains(admin.getId())) {
                    sendAlert(admin, title, body, task);
                    notifiedUsers.add(admin.getId());
                }
            }

            alertCount++;
        }

        log.info("17h alert: sent alerts for {} tasks", alertCount);
    }

    private void sendAlert(User user, String title, String body, MaintenanceTask task) {
        // In-app notification
        notificationService.createNotification(
                user, title, body,
                NotificationType.MAINTENANCE_OVERDUE_ALERT,
                "maintenance_task",
                task.getId()
        );

        // Email
        if (user.getEmail() != null) {
            emailService.sendMaintenanceOverdue(
                    user.getEmail(),
                    task.getTitle(),
                    task.getScheduledDate().toString(),
                    task.getAssignedTo() != null
                            ? task.getAssignedTo().getFullName() : "Chưa phân công",
                    0  // 0 ngày vì hôm nay mới hết hạn
            );
        }
    }
}