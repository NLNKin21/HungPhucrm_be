package com.hungphu.crm.features.customer.dto;

import com.hungphu.crm.shared.enums.ElevatorType;
import com.hungphu.crm.shared.enums.ProjectType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class CustomerResponse {
    private UUID id;
    private String fullName;
    private String phone;
    private String address;
    private ElevatorType elevatorType;
    private ProjectType projectType;
    private AssignedUserInfo assignedUser;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
