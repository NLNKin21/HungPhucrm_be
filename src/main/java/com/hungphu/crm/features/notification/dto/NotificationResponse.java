package com.hungphu.crm.features.notification.dto;

import com.hungphu.crm.shared.enums.NotificationType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class NotificationResponse {
    private UUID id;
    private NotificationType type;
    private String title;
    private String body;
    private String refType;
    private UUID refId;
    private boolean read;
    private LocalDateTime createdAt;
}
