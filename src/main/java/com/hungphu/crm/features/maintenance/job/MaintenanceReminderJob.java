package com.hungphu.crm.features.maintenance.job;

import com.hungphu.crm.features.maintenance.entity.MaintenanceTask;
import com.hungphu.crm.features.maintenance.repository.MaintenanceTaskRepository;
import com.hungphu.crm.features.notification.NotificationService;
import com.hungphu.crm.shared.enums.NotificationType;
import com.hungphu.crm.shared.enums.ScheduleStatus;
import com.hungphu.crm.shared.mail.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MaintenanceReminderJob {

    private final MaintenanceTaskRepository taskRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;

    private static final int[] REMINDER_DAYS = {7, 3, 1};

    /**
     * 8h sáng mỗi ngày — nhắc lịch bảo trì 7, 3, 1 ngày trước
     */
    @Scheduled(cron = "0 0 8 * * *", zone = "Asia/Ho_Chi_Minh")
    @Transactional(readOnly = true)
    public void sendReminders() {
        log.info("Running maintenance reminder job (7-3-1 days)...");

        LocalDate today = LocalDate.now();
        int totalSent = 0;

        for (int days : REMINDER_DAYS) {
            LocalDate targetDate = today.plusDays(days);

            List<MaintenanceTask> tasks = taskRepository
                    .findByScheduledDateAndPending(targetDate);

            for (MaintenanceTask task : tasks) {
                sendReminderForTask(task, days);
                totalSent++;
            }

            log.info("Reminder {} days: found {} tasks", days, tasks.size());
        }

        log.info("Total reminders sent: {}", totalSent);
    }

    private void sendReminderForTask(MaintenanceTask task, int daysBeforeDue) {
        NotificationType type = switch (daysBeforeDue) {
            case 7 -> NotificationType.MAINTENANCE_REMINDER_7;
            case 3 -> NotificationType.MAINTENANCE_REMINDER_3;
            case 1 -> NotificationType.MAINTENANCE_REMINDER_1;
            default -> NotificationType.MAINTENANCE_REMINDER;
        };

        String title = String.format("Nhắc bảo trì — còn %d ngày", daysBeforeDue);
        String body = String.format(
                "Lịch bảo trì \"%s\" đến hạn vào ngày %s (còn %d ngày)",
                task.getTitle(),
                task.getScheduledDate(),
                daysBeforeDue
        );

        // ── Gửi cho người phụ trách ──
        if (task.getAssignedTo() != null) {
            // In-app notification
            notificationService.createNotification(
                    task.getAssignedTo(), title, body,
                    type, "maintenance_task", task.getId()
            );

            // Email
            if (task.getAssignedTo().getEmail() != null) {
                emailService.sendMaintenanceReminder(
                        task.getAssignedTo().getEmail(),
                        task.getTitle(),
                        task.getScheduledDate().toString(),
                        task.getAssignedTo().getFullName(),
                        daysBeforeDue
                );
            }

            log.debug("Reminder sent to assignee {} for task {} ({} days)",
                    task.getAssignedTo().getFullName(), task.getId(), daysBeforeDue);
        }

        // ── Gửi cho supervisor (nếu có) ──
        if (task.getSupervisor() != null
                && !task.getSupervisor().equals(task.getAssignedTo())) {
            notificationService.createNotification(
                    task.getSupervisor(), title, body,
                    type, "maintenance_task", task.getId()
            );

            if (task.getSupervisor().getEmail() != null) {
                emailService.sendMaintenanceReminder(
                        task.getSupervisor().getEmail(),
                        task.getTitle(),
                        task.getScheduledDate().toString(),
                        task.getAssignedTo() != null
                                ? task.getAssignedTo().getFullName() : "Chưa phân công",
                        daysBeforeDue
                );
            }
        }
    }
}