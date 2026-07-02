package com.hungphu.crm.features.maintenance.repository;

import com.hungphu.crm.features.maintenance.entity.MaintenanceContract;
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

    @Query("""
        SELECT c FROM MaintenanceContract c
        LEFT JOIN FETCH c.project
        LEFT JOIN FETCH c.customer
        LEFT JOIN FETCH c.assignedTo
        LEFT JOIN FETCH c.tasks
        WHERE c.project.id = :projectId
        """)
    List<MaintenanceContract> findByProjectId(@Param("projectId") UUID projectId);
}