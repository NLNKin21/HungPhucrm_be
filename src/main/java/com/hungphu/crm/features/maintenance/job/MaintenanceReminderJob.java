package com.hungphu.crm.features.maintenance.job;

import com.hungphu.crm.features.maintenance.entity.MaintenanceTask;
import com.hungphu.crm.features.maintenance.repository.MaintenanceTaskRepository;
import com.hungphu.crm.features.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MaintenanceReminderJob {

    private final MaintenanceTaskRepository taskRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 8 * * *", zone = "Asia/Ho_Chi_Minh")
    public void sendReminders() {
        log.info("Running maintenance reminder job");
        LocalDate upcoming = LocalDate.now().plusDays(7);

        List<MaintenanceTask> tasks = taskRepository.findPendingBefore(upcoming);
        tasks.forEach(task -> notificationService.createMaintenanceReminder(task));

        log.info("Sent {} maintenance reminders", tasks.size());
    }
}