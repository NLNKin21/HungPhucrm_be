package com.hungphu.crm.features.task.dto;

import com.hungphu.crm.shared.enums.TaskType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class CreateTaskRequest {

    @NotBlank(message = "Tên công việc không được trống")
    @Size(max = 255)
    private String title;

    private String siteAddress;

    @Future(message = "Hạn phải là ngày trong tương lai")
    private LocalDate deadline;

    @NotNull(message = "Loại công việc không được trống")
    private TaskType taskType;

    // LEAD
    @NotNull(message = "Trưởng task không được trống")
    private UUID assignedTo;

    // MEMBER
    private List<UUID> memberIds;

    private UUID supervisorId;
}