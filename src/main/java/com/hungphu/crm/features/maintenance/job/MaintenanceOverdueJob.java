package com.hungphu.crm.features.maintenance.job;

import com.hungphu.crm.features.maintenance.entity.MaintenanceTask;
import com.hungphu.crm.features.maintenance.repository.MaintenanceTaskRepository;
import com.hungphu.crm.features.notification.NotificationService;
import com.hungphu.crm.shared.enums.ScheduleStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MaintenanceOverdueJob {

    private final MaintenanceTaskRepository taskRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 5 0 * * *", zone = "Asia/Ho_Chi_Minh")
    @Transactional
    public void markOverdueTasks() {
        log.info("Running maintenance overdue job...");

        LocalDate today = LocalDate.now();
        List<MaintenanceTask> overdueList = taskRepository.findOverdueTasks(today);

        if (overdueList.isEmpty()) {
            log.info("No overdue tasks found");
            return;
        }

        int count = 0;
        for (MaintenanceTask task : overdueList) {
            task.setStatus(ScheduleStatus.QUA_HAN);
            taskRepository.save(task);

            long daysOverdue = ChronoUnit.DAYS.between(task.getScheduledDate(), today);
            notificationService.createOverdueMaintenanceAlert(task, (int) daysOverdue);

            count++;
            log.warn("Task {} marked as overdue ({} days): {}",
                    task.getId(), daysOverdue, task.getTitle());
        }

        log.info("Marked {} tasks as overdue", count);
    }
}