package com.hungphu.crm.features.maintenance.repository;

import com.hungphu.crm.features.maintenance.entity.MaintenanceAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MaintenanceAttachmentRepository extends JpaRepository<MaintenanceAttachment, UUID> {
}