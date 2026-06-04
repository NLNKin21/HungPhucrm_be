package com.hungphu.crm.features.task.repository;

import com.hungphu.crm.features.task.entity.TaskEvidence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaskEvidenceRepository extends JpaRepository<TaskEvidence, UUID> {

    List<TaskEvidence> findByTaskIdOrderByUploadedAtAsc(UUID taskId);

    int countByTaskId(UUID taskId);
}
