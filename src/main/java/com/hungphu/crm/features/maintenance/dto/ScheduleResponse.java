package com.hungphu.crm.features.maintenance.dto;

import com.hungphu.crm.shared.enums.ScheduleStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class ScheduleResponse {
    private UUID id;
    private LocalDate scheduledDate;
    private ScheduleStatus status;
    private UserInfo assignedTo;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;

    @Getter
    @Builder
    public static class UserInfo {
        private UUID id;
        private String fullName;
    }
}
