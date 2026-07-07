package com.hungphu.crm.features.maintenance.repository;

import com.hungphu.crm.features.maintenance.entity.MaintenanceApproval;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MaintenanceApprovalRepository extends JpaRepository<MaintenanceApproval, UUID> {
    List<MaintenanceApproval> findByTaskIdOrderByCreatedAtDesc(UUID taskId);
}