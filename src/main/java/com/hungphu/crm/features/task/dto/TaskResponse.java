package com.hungphu.crm.features.task.dto;

import com.hungphu.crm.shared.enums.FileType;
import com.hungphu.crm.shared.enums.TaskStatus;
import com.hungphu.crm.shared.enums.TaskType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class TaskResponse {
    private UUID id;
    private String title;
    private String siteAddress;
    private LocalDate deadline;
    private TaskType taskType;
    private TaskStatus status;
    private String rejectionReason;
    private UserInfo assignedBy;
    private UserInfo assignedTo; // LEAD
    private UserInfo supervisor;
    private List<UserInfo> members; // MEMBERs, không bao gồm LEAD
    private List<EvidenceInfo> evidences;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Getter
    @Builder
    public static class UserInfo {
        private UUID id;
        private String fullName;
    }

    @Getter
    @Builder
    public static class EvidenceInfo {
        private UUID id;
        private String fileUrl;
        private FileType fileType;
        private LocalDateTime uploadedAt;
    }
}