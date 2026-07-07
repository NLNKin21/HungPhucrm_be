package com.hungphu.crm.features.maintenance.repository;

import com.hungphu.crm.features.maintenance.entity.MaintenanceComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MaintenanceCommentRepository extends JpaRepository<MaintenanceComment, UUID> {

    @Query("""
        SELECT DISTINCT c FROM MaintenanceComment c
        LEFT JOIN FETCH c.user u
        LEFT JOIN FETCH c.attachments
        WHERE c.task.id = :taskId
        AND c.parent IS NULL
        ORDER BY c.createdAt ASC
        """)
    List<MaintenanceComment> findRootCommentsByTaskId(@Param("taskId") UUID taskId);

    long countByTaskId(UUID taskId);
}