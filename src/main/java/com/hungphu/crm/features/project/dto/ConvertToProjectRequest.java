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
public class ConvertToProjectRequest {

    @NotBlank(message = "Tên dự án không được trống")
    @Size(max = 255)
    private String projectName;

    private UUID customerId;
    private NewCustomerInfo newCustomer;

    @NotNull(message = "Loại thang máy không được trống")
    private ElevatorType elevatorType;

    @NotNull(message = "Loại dự án không được trống")
    private ProjectType projectType;

    private UUID supervisorId;

    @Getter
    @Setter
    public static class NewCustomerInfo {
        @NotBlank
        private String fullName;
        @NotBlank
        private String phone;
        private String address;
    }
}
