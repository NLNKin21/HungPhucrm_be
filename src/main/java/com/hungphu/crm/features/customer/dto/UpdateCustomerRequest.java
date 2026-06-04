package com.hungphu.crm.features.customer.dto;

import com.hungphu.crm.shared.enums.ElevatorType;
import com.hungphu.crm.shared.enums.ProjectType;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UpdateCustomerRequest {

    @Size(max = 150)
    private String fullName;

    @Size(max = 20)
    private String phone;

    private String address;
    private ElevatorType elevatorType;
    private ProjectType projectType;
    private UUID assignedUserId;
}
