package com.hungphu.crm.features.maintenance.dto;

import com.hungphu.crm.shared.enums.FileType;
import com.hungphu.crm.shared.enums.MaintenanceStatus;
import com.hungphu.crm.shared.enums.ScheduleStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class CustomerLookupResponse {

    private String customerName;
    private String phone;
    private List<ContractSummary> contracts;

    @Getter
    @Builder
    public static class ContractSummary {
        private UUID id;
        private String projectName;
        private LocalDate startDate;
        private LocalDate endDate;
        private MaintenanceStatus status;
        private int totalTasks;
        private int completedTasks;
        private List<TaskSummary> tasks;
    }

    @Getter
    @Builder
    public static class TaskSummary {
        private UUID id;
        private String title;
        private LocalDate scheduledDate;
        private ScheduleStatus status;
        private String assigneeName;
        private String techNote;            // ghi chú kỹ thuật từ description
        private LocalDateTime completedAt;
        private List<EvidenceSummary> evidences;
    }

    @Getter
    @Builder
    public static class EvidenceSummary {
        private UUID id;
        private String fileUrl;
        private FileType fileType;
        private String description;
        private LocalDateTime uploadedAt;
    }
}