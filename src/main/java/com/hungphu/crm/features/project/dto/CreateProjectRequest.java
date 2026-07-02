package com.hungphu.crm.features.project.dto;

import com.hungphu.crm.shared.enums.ElevatorType;
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

    @NotNull(message = "Loại thang máy không được trống")
    private ElevatorType elevatorType;

    @NotNull(message = "Loại dự án không được trống")
    private ProjectType projectType;

    private UUID supervisorId; // Optional
}