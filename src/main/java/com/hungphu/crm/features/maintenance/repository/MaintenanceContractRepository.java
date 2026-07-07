package com.hungphu.crm.features.maintenance.repository;

import com.hungphu.crm.features.maintenance.entity.MaintenanceContract;
import com.hungphu.crm.shared.enums.MaintenanceStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MaintenanceContractRepository extends JpaRepository<MaintenanceContract, UUID> {

    @Query("""
        SELECT DISTINCT c FROM MaintenanceContract c
        LEFT JOIN FETCH c.project
        LEFT JOIN FETCH c.customer
        LEFT JOIN FETCH c.assignedTo
        LEFT JOIN FETCH c.tasks
        ORDER BY c.createdAt DESC
        """)
    List<MaintenanceContract> findAllWithDetails();

    boolean existsByProjectId(UUID projectId);

    List<MaintenanceContract> findByCustomerIdAndStatusIn(
        UUID customerId, List<MaintenanceStatus> statuses);

    List<MaintenanceContract> findByCustomerIdAndProjectIdAndStatusIn(
        UUID customerId, UUID projectId, List<MaintenanceStatus> statuses);

    @Query("""
        SELECT c FROM MaintenanceContract c
        LEFT JOIN FETCH c.project
        LEFT JOIN FETCH c.customer
        LEFT JOIN FETCH c.assignedTo
        LEFT JOIN FETCH c.tasks
        WHERE c.project.id = :projectId
        """)
    List<MaintenanceContract> findByProjectId(@Param("projectId") UUID projectId);
    
    // Tìm contracts theo phone của customer
    @Query("""
        SELECT c FROM MaintenanceContract c
        JOIN FETCH c.customer cu
        LEFT JOIN FETCH c.project p
        WHERE cu.phone = :phone
        ORDER BY c.startDate DESC
        """)
    List<MaintenanceContract> findByCustomerPhone(@Param("phone") String phone);
}