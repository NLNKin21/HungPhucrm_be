package com.hungphu.crm.features.maintenance.repository;

import com.hungphu.crm.features.maintenance.entity.MaintenanceSchedule;
import com.hungphu.crm.shared.enums.ScheduleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface MaintenanceScheduleRepository extends JpaRepository<MaintenanceSchedule, UUID> {

    List<MaintenanceSchedule> findByContractIdOrderByScheduledDateAsc(UUID contractId);

    @Query("""
            SELECT s FROM MaintenanceSchedule s
            JOIN FETCH s.contract c
            JOIN FETCH s.assignedTo u
            WHERE s.status = 'CHO_THUC_HIEN'
              AND s.scheduledDate <= :upcomingDate
            """)
    List<MaintenanceSchedule> findPendingBefore(@Param("upcomingDate") LocalDate upcomingDate);
}
