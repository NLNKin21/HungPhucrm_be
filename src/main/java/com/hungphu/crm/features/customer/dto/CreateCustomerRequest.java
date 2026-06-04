package com.hungphu.crm.features.customer.dto;

import com.hungphu.crm.shared.enums.ElevatorType;
import com.hungphu.crm.shared.enums.ProjectType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CreateCustomerRequest {

    @NotBlank(message = "Họ tên không được trống")
    @Size(max = 150)
    private String fullName;

    @NotBlank(message = "Số điện thoại không được trống")
    @Size(max = 20)
    private String phone;

    private String address;
    private ElevatorType elevatorType;
    private ProjectType projectType;
    private UUID assignedUserId;
}
