package com.hungphu.crm.features.maintenance.repository;

import com.hungphu.crm.features.maintenance.entity.MaintenanceTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface MaintenanceTemplateRepository extends JpaRepository<MaintenanceTemplate, UUID> {

    @Query("""
        SELECT t FROM MaintenanceTemplate t
        LEFT JOIN FETCH t.defaultAssignedTo
        LEFT JOIN FETCH t.defaultWatcher
        LEFT JOIN FETCH t.createdBy
        WHERE t.active = true
        ORDER BY t.createdAt DESC
        """)
    List<MaintenanceTemplate> findAllActive();

    @Query("""
        SELECT t FROM MaintenanceTemplate t
        LEFT JOIN FETCH t.defaultAssignedTo
        LEFT JOIN FETCH t.defaultWatcher
        LEFT JOIN FETCH t.createdBy
        ORDER BY t.createdAt DESC
        """)
    List<MaintenanceTemplate> findAllWithDetails();
}