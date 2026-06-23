package com.hungphu.crm.features.maintenance.repository;

import com.hungphu.crm.features.maintenance.entity.MaintenanceSchedule;
import com.hungphu.crm.shared.enums.ScheduleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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
            LEFT JOIN FETCH s.assignedTo u
            WHERE s.status = 'CHO_THUC_HIEN'
              AND s.scheduledDate <= :upcomingDate
            """)
    List<MaintenanceSchedule> findPendingBefore(@Param("upcomingDate") LocalDate upcomingDate);

    // ── Thêm mới: Tìm schedules quá hạn cần update ──
    @Query("""
            SELECT s FROM MaintenanceSchedule s
            WHERE s.status = 'CHO_THUC_HIEN'
              AND s.scheduledDate < :today
            """)
    List<MaintenanceSchedule> findOverdueSchedules(@Param("today") LocalDate today);

    // ── Thêm mới: Đếm schedules theo status ──
    @Query("""
            SELECT s.status, COUNT(s) FROM MaintenanceSchedule s
            GROUP BY s.status
            """)
    List<Object[]> countByStatus();

    // ── Thêm mới: Đếm schedules quá hạn ──
    @Query("""
            SELECT COUNT(s) FROM MaintenanceSchedule s
            WHERE s.status = 'QUA_HAN'
            """)
    long countOverdue();

    // ── Thêm mới: Đếm hoàn thành trễ ──
    @Query("""
            SELECT COUNT(s) FROM MaintenanceSchedule s
            WHERE s.status = 'HOAN_THANH' AND s.completedLate = true
            """)
    long countCompletedLate();

    // ── Thêm mới: Lấy schedules sắp tới (7 ngày) ──
    @Query("""
            SELECT s FROM MaintenanceSchedule s
            JOIN FETCH s.contract c
            JOIN FETCH c.project p
            LEFT JOIN FETCH s.assignedTo
            WHERE s.status IN ('CHO_THUC_HIEN', 'QUA_HAN')
              AND s.scheduledDate BETWEEN :today AND :endDate
            ORDER BY s.scheduledDate ASC
            """)
    List<MaintenanceSchedule> findUpcomingSchedules(
            @Param("today") LocalDate today,
            @Param("endDate") LocalDate endDate
    );

    // ── Thêm mới: Lấy tất cả schedules quá hạn ──
    @Query("""
            SELECT s FROM MaintenanceSchedule s
            JOIN FETCH s.contract c
            JOIN FETCH c.project p
            LEFT JOIN FETCH s.assignedTo
            WHERE s.status = 'QUA_HAN'
            ORDER BY s.scheduledDate ASC
            """)
    List<MaintenanceSchedule> findAllOverdue();
}