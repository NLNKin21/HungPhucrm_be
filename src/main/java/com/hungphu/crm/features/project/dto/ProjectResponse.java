package com.hungphu.crm.features.project.dto;

import com.hungphu.crm.shared.enums.ElevatorType;
import com.hungphu.crm.shared.enums.ProjectStatus;
import com.hungphu.crm.shared.enums.ProjectType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class ProjectResponse {
    private UUID id;
    private String name;
    private CustomerInfo customer;
    private ElevatorType elevatorType;
    private ProjectType projectType;
    private ProjectStatus projectStatus;
    private UserInfo supervisor;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Getter
    @Builder
    public static class CustomerInfo {
        private UUID id;
        private String fullName;
        private String phone;
    }

    @Getter
    @Builder
    public static class UserInfo {
        private UUID id;
        private String fullName;
    }
}