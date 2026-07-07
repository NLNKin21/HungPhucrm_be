package com.hungphu.crm.features.maintenance.dto;

import com.hungphu.crm.shared.enums.MaintenanceStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class ContractResponse {
    private UUID id;
    private ProjectInfo project;
    private CustomerInfo customer;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer cycleMonths;
    private MaintenanceStatus status;
    private UserInfo assignedTo;
    private UserInfo supervisor;                    // ★ MỚI
    private boolean firstMaintenanceImmediate;      // ★ MỚI
    private int schedulesGenerated;
    private LocalDateTime createdAt;

    @Getter
    @Builder
    public static class ProjectInfo {
        private UUID id;
        private String name;
    }

    @Getter
    @Builder
    public static class CustomerInfo {
        private UUID id;
        private String fullName;
        private String phone;                       // ★ MỚI — cho frontend hiển thị
    }

    @Getter
    @Builder
    public static class UserInfo {
        private UUID id;
        private String fullName;
    }
}