package com.hungphu.crm.features.maintenance.dto;

import com.hungphu.crm.shared.enums.ScheduleStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class MaintenanceTaskResponse {
    private UUID id;
    private String title;
    private String description;
    private String contactPhone;
    private LocalDate scheduledDate;
    private ScheduleStatus status;
    private boolean overdue;
    private UserInfo createdBy;
    private UserInfo assignedTo;
    private UserInfo watcher;
    private UserInfo supervisor;       // ★ THÊM DÒNG NÀY
    private boolean completedLate;
    private Integer daysLate;
    private int commentCount;
    private int evidenceCount;         // nếu bạn đã map evidenceCount thì thêm luôn
    private LocalDateTime submittedAt; // nếu bạn đã map submittedAt thì thêm luôn
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private ContractInfo contract;

    @Getter
    @Builder
    public static class UserInfo {
        private UUID id;
        private String fullName;
    }

    @Getter
    @Builder
    public static class ContractInfo {
        private UUID id;
        private UUID customerId;
        private String customerName;
        private UUID projectId;
        private String projectName;
    }
}