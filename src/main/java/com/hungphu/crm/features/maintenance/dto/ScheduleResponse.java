package com.hungphu.crm.features.maintenance.dto;

import com.hungphu.crm.shared.enums.FileType;
import com.hungphu.crm.shared.enums.ScheduleStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class ScheduleResponse {
    private UUID id;
    private LocalDate scheduledDate;
    private ScheduleStatus status;
    private UserInfo assignedTo;
    private LocalDateTime completedAt;
    
    // ── Thêm mới ──
    private boolean completedLate;
    private Integer daysLate;
    private String notes;
    private boolean overdue; // Helper: true nếu QUA_HAN hoặc (CHO_THUC_HIEN && scheduledDate < today)
    private List<EvidenceInfo> evidences;
    // ──────────────
    
    private LocalDateTime createdAt;

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
        private UserInfo uploadedBy;
        private LocalDateTime uploadedAt;
    }
}