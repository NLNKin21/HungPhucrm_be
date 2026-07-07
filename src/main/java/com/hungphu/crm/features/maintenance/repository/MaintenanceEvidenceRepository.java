package com.hungphu.crm.features.maintenance.repository;

import com.hungphu.crm.features.maintenance.entity.MaintenanceEvidence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MaintenanceEvidenceRepository extends JpaRepository<MaintenanceEvidence, UUID> {
    List<MaintenanceEvidence> findByTaskIdOrderByUploadedAtDesc(UUID taskId);
    long countByTaskId(UUID taskId);
}