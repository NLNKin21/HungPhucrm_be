package com.hungphu.crm.features.project.dto;

import com.hungphu.crm.shared.enums.ElevatorType;
import com.hungphu.crm.shared.enums.ProjectStatus;
import com.hungphu.crm.shared.enums.ProjectType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CreateProjectRequest {

    @NotBlank(message = "Tên dự án không được trống")
    @Size(max = 255)
    private String name;

    @NotNull(message = "Khách hàng không được trống")
    private UUID customerId;

    // ★ BỎ @NotNull để có thể tạo nhanh
    private ElevatorType elevatorType;
    private ProjectType projectType;

    private UUID supervisorId; // Optional

    // ★ THÊM field này để flow bảo trì gọi
    private ProjectStatus initialStatus;
}