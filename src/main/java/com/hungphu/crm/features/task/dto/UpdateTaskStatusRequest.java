package com.hungphu.crm.features.task.dto;

import com.hungphu.crm.shared.enums.TaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateTaskStatusRequest {

    @NotNull(message = "Trạng thái không được trống")
    private TaskStatus status;

    private String rejectionReason;
}
