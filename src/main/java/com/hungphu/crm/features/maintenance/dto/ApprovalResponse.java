package com.hungphu.crm.features.maintenance.dto;

import com.hungphu.crm.shared.enums.ApprovalAction;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class ApprovalResponse {
    private UUID id;
    private ApprovalAction action;
    private String reason;
    private UserInfo approvedBy;
    private LocalDateTime createdAt;

    @Getter
    @Builder
    public static class UserInfo {
        private UUID id;
        private String fullName;
    }
}