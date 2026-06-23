package com.hungphu.crm.features.maintenance.job;

import com.hungphu.crm.features.maintenance.entity.MaintenanceSchedule;
import com.hungphu.crm.features.maintenance.repository.MaintenanceScheduleRepository;
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

/**
 * Job chạy hàng ngày lúc 00:05 để:
 * 1. Đánh dấu các schedule CHO_THUC_HIEN đã qua ngày → QUA_HAN
 * 2. Gửi notification cảnh báo cho người phụ trách và Admin
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MaintenanceOverdueJob {

    private final MaintenanceScheduleRepository scheduleRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 5 0 * * *", zone = "Asia/Ho_Chi_Minh") // 00:05 mỗi ngày
    @Transactional
    public void markOverdueSchedules() {
        log.info("Running maintenance overdue job...");
        
        LocalDate today = LocalDate.now();
        List<MaintenanceSchedule> overdueList = scheduleRepository.findOverdueSchedules(today);

        if (overdueList.isEmpty()) {
            log.info("No overdue schedules found");
            return;
        }

        int count = 0;
        for (MaintenanceSchedule schedule : overdueList) {
            // Cập nhật trạng thái
            schedule.setStatus(ScheduleStatus.QUA_HAN);
            scheduleRepository.save(schedule);

            // Tính số ngày quá hạn
            long daysOverdue = ChronoUnit.DAYS.between(schedule.getScheduledDate(), today);

            // Gửi notification
            notificationService.createOverdueMaintenanceAlert(schedule, (int) daysOverdue);

            count++;
            log.warn("Schedule {} marked as overdue ({} days), contract: {}, project: {}",
                    schedule.getId(),
                    daysOverdue,
                    schedule.getContract().getId(),
                    schedule.getContract().getProject().getName());
        }

        log.info("Marked {} schedules as overdue", count);
    }
}