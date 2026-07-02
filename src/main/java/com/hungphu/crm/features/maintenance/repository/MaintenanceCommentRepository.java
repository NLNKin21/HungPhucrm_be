package com.hungphu.crm.features.maintenance.repository;

import com.hungphu.crm.features.maintenance.entity.MaintenanceComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MaintenanceCommentRepository extends JpaRepository<MaintenanceComment, UUID> {

    @Query("""
            SELECT c FROM MaintenanceComment c
            LEFT JOIN FETCH c.user
            LEFT JOIN FETCH c.attachments
            LEFT JOIN FETCH c.replies r
            LEFT JOIN FETCH r.user
            LEFT JOIN FETCH r.attachments
            WHERE c.task.id = :taskId AND c.parent IS NULL
            ORDER BY c.createdAt ASC
            """)
    List<MaintenanceComment> findRootCommentsByTaskId(@Param("taskId") UUID taskId);

    long countByTaskId(UUID taskId);
}