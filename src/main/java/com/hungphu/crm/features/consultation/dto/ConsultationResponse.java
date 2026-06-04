package com.hungphu.crm.features.consultation.dto;

import com.hungphu.crm.shared.enums.ConsultationStatus;
import com.hungphu.crm.shared.enums.ElevatorType;
import com.hungphu.crm.shared.enums.ProjectType;
import com.hungphu.crm.shared.enums.PriorityLevel;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class ConsultationResponse {
    private UUID id;
    private String customerName;
    private String customerPhone;
    private String siteAddress;
    private PriorityLevel priority;
    private BigDecimal price;
    private String notes;
    private ConsultationStatus status;
    private String failureReason;
    private AssigneeInfo assignedBy;
    private AssigneeInfo assignedTo;
    private LocalDateTime acceptedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ── Thêm mới ───────────────────────────────────────────────────────────
    private ElevatorType elevatorType;
    private ProjectType  projectType;
    // ───────────────────────────────────────────────────────────────────────

    @Getter
    @Builder
    public static class AssigneeInfo {
        private UUID id;
        private String fullName;
    }
}