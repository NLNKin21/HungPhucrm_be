package com.hungphu.crm.features.notification;

import com.hungphu.crm.features.maintenance.entity.MaintenanceTask;
import com.hungphu.crm.features.notification.dto.NotificationResponse;
import com.hungphu.crm.features.user.entity.User;
import com.hungphu.crm.shared.enums.NotificationType;
import com.hungphu.crm.shared.response.ApiResponse;
import com.hungphu.crm.shared.security.UserDetailsImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface NotificationService {

    ApiResponse<List<NotificationResponse>> findMine(UserDetailsImpl currentUser, Pageable pageable);

    long countUnread(UserDetailsImpl currentUser);

    void markRead(UUID id, UserDetailsImpl currentUser);

    void markAllRead(UserDetailsImpl currentUser);

    void createMaintenanceReminder(MaintenanceTask task);

    void createOverdueMaintenanceAlert(MaintenanceTask task, int daysOverdue);
    // Thêm method mới:
    void createNotification(User user, String title, String body,
                        NotificationType type, String refType, UUID refId);
}