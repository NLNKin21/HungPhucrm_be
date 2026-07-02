package com.hungphu.crm.features.maintenance.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class TemplateResponse {
    private UUID id;
    private String title;
    private String description;
    private Integer cycleMonths;
    private Integer durationMonths;
    private UserInfo defaultAssignedTo;
    private UserInfo defaultWatcher;
    private UserInfo createdBy;
    private boolean active;
    private LocalDateTime createdAt;

    @Getter
    @Builder
    public static class UserInfo {
        private UUID id;
        private String fullName;
    }
}