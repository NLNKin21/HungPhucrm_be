package com.hungphu.crm.features.task.dto;

import com.hungphu.crm.shared.enums.TaskType;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class UpdateTaskRequest {

    @Size(max = 255, message = "Tên công việc tối đa 255 ký tự")
    private String title;

    private String siteAddress;

    private LocalDate deadline;

    private TaskType taskType;

    // LEAD mới
    private UUID assignedTo;

    // MEMBER mới
    private List<UUID> memberIds;

    private UUID supervisorId;

    private boolean clearSupervisor;
}