package com.hungphu.crm.features.maintenance.job;

import com.hungphu.crm.features.maintenance.entity.MaintenanceSchedule;
import com.hungphu.crm.features.maintenance.repository.MaintenanceScheduleRepository;
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

    private final MaintenanceScheduleRepository scheduleRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 8 * * *", zone = "Asia/Ho_Chi_Minh")
    public void sendReminders() {
        log.info("Running maintenance reminder job");
        LocalDate upcoming = LocalDate.now().plusDays(7);

        List<MaintenanceSchedule> schedules = scheduleRepository.findPendingBefore(upcoming);
        schedules.forEach(schedule ->
                notificationService.createMaintenanceReminder(schedule));

        log.info("Sent {} maintenance reminders", schedules.size());
    }
}
