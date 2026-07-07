package com.hungphu.crm.features.maintenance.repository;

import com.hungphu.crm.features.maintenance.entity.MaintenanceTask;
import com.hungphu.crm.shared.enums.ScheduleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MaintenanceTaskRepository extends JpaRepository<MaintenanceTask, UUID> {

    @Query("""
            SELECT t FROM MaintenanceTask t
            JOIN FETCH t.contract c
            LEFT JOIN FETCH c.customer
            LEFT JOIN FETCH c.project
            LEFT JOIN FETCH t.assignedTo
            LEFT JOIN FETCH t.watcher
            LEFT JOIN FETCH t.createdBy
            WHERE c.id = :contractId
            ORDER BY t.scheduledDate ASC
            """)
    List<MaintenanceTask> findByContractIdWithDetailsOrderByScheduledDateAsc(@Param("contractId") UUID contractId);

    @Query("""
            SELECT t FROM MaintenanceTask t
            JOIN FETCH t.contract c
            LEFT JOIN FETCH c.customer
            LEFT JOIN FETCH c.project
            LEFT JOIN FETCH t.assignedTo
            LEFT JOIN FETCH t.watcher
            LEFT JOIN FETCH t.createdBy
            WHERE t.id = :id
            """)
    Optional<MaintenanceTask> findByIdWithDetails(@Param("id") UUID id);

    @Query("""
            SELECT t FROM MaintenanceTask t
            JOIN FETCH t.contract c
            LEFT JOIN FETCH t.assignedTo
            WHERE t.status = 'CHO_THUC_HIEN'
              AND t.scheduledDate <= :upcomingDate
            """)
    List<MaintenanceTask> findPendingBefore(@Param("upcomingDate") LocalDate upcomingDate);

    @Query("""
            SELECT t FROM MaintenanceTask t
            WHERE t.status = 'CHO_THUC_HIEN'
              AND t.scheduledDate < :today
            """)
    List<MaintenanceTask> findOverdueTasks(@Param("today") LocalDate today);

    @Query("SELECT COUNT(t) FROM MaintenanceTask t WHERE t.status = 'QUA_HAN'")
    long countOverdue();

    @Query("SELECT COUNT(t) FROM MaintenanceTask t WHERE t.status = 'HOAN_THANH' AND t.completedLate = true")
    long countCompletedLate();

    @Query("SELECT t.status, COUNT(t) FROM MaintenanceTask t GROUP BY t.status")
    List<Object[]> countByStatus();

    @Query("""
            SELECT t FROM MaintenanceTask t
            JOIN FETCH t.contract c
            LEFT JOIN FETCH c.project
            LEFT JOIN FETCH c.customer
            LEFT JOIN FETCH t.assignedTo
            WHERE t.status IN ('CHO_THUC_HIEN', 'QUA_HAN')
              AND t.scheduledDate BETWEEN :today AND :endDate
            ORDER BY t.scheduledDate ASC
            """)
    List<MaintenanceTask> findUpcomingTasks(
            @Param("today") LocalDate today,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
            SELECT t FROM MaintenanceTask t
            JOIN FETCH t.contract c
            LEFT JOIN FETCH c.project
            LEFT JOIN FETCH c.customer
            LEFT JOIN FETCH t.assignedTo
            WHERE t.status = 'QUA_HAN'
            ORDER BY t.scheduledDate ASC
            """)
    List<MaintenanceTask> findAllOverdue();

    @Query("""
            SELECT t FROM MaintenanceTask t
            JOIN FETCH t.contract c
            LEFT JOIN FETCH c.customer cu
            LEFT JOIN FETCH c.project p
            LEFT JOIN FETCH t.assignedTo a
            LEFT JOIN FETCH t.watcher w
            LEFT JOIN FETCH t.createdBy cb
            WHERE
                (:status IS NULL OR t.status = :status)
                AND (:assignedTo IS NULL OR a.id = :assignedTo)
                AND (:contractId IS NULL OR c.id = :contractId)
                AND (:customerId IS NULL OR cu.id = :customerId)
                AND (:fromDate IS NULL OR t.scheduledDate >= :fromDate)
                AND (:toDate IS NULL OR t.scheduledDate <= :toDate)
                AND (:visibleTo IS NULL
                     OR a.id = :visibleTo
                     OR w.id = :visibleTo
                     OR cb.id = :visibleTo)
            ORDER BY t.scheduledDate ASC
            """)
    List<MaintenanceTask> findAllWithFilters(
            @Param("status") ScheduleStatus status,
            @Param("assignedTo") UUID assignedTo,
            @Param("contractId") UUID contractId,
            @Param("customerId") UUID customerId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("visibleTo") UUID visibleToUserId
    );

    @Query("""
        SELECT t FROM MaintenanceTask t
        JOIN FETCH t.contract c
        LEFT JOIN FETCH t.assignedTo
        LEFT JOIN FETCH t.supervisor
        WHERE t.status IN ('CHO_THUC_HIEN')
        AND t.scheduledDate = :targetDate
        """)
        List<MaintenanceTask> findByScheduledDateAndPending(
                @Param("targetDate") LocalDate targetDate);

        @Query("""
        SELECT t FROM MaintenanceTask t
        JOIN FETCH t.contract c
        LEFT JOIN FETCH t.assignedTo
        LEFT JOIN FETCH t.supervisor
        WHERE t.status IN ('CHO_THUC_HIEN', 'CAN_BO_SUNG')
        AND t.scheduledDate = :targetDate
        """)
        List<MaintenanceTask> findDueOnDate(@Param("targetDate") LocalDate targetDate);

        // Tìm tasks của contract có evidences đã duyệt
        @Query("""
        SELECT t FROM MaintenanceTask t
        LEFT JOIN FETCH t.assignedTo a
        LEFT JOIN FETCH t.evidences ev
        WHERE t.contract.id = :contractId
        ORDER BY t.scheduledDate ASC
        """)
        List<MaintenanceTask> findByContractIdWithEvidences(
                @Param("contractId") UUID contractId);
}