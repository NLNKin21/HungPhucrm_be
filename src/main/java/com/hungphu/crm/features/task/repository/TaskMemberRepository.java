package com.hungphu.crm.features.task.repository;

import com.hungphu.crm.features.task.entity.TaskMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaskMemberRepository extends JpaRepository<TaskMember, UUID> {

    List<TaskMember> findByTaskId(UUID taskId);

    boolean existsByTaskIdAndUserId(UUID taskId, UUID userId);
}
