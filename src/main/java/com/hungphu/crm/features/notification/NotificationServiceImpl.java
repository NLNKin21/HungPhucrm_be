package com.hungphu.crm.features.notification;

import com.hungphu.crm.features.maintenance.entity.MaintenanceTask;
import com.hungphu.crm.features.notification.dto.NotificationResponse;
import com.hungphu.crm.features.notification.entity.Notification;
import com.hungphu.crm.features.notification.mapper.NotificationMapper;
import com.hungphu.crm.features.notification.repository.NotificationRepository;
import com.hungphu.crm.features.user.entity.User;
import com.hungphu.crm.features.user.repository.UserRepository;
import com.hungphu.crm.shared.enums.NotificationType;
import com.hungphu.crm.shared.exception.ResourceNotFoundException;
import com.hungphu.crm.shared.response.ApiResponse;
import com.hungphu.crm.shared.response.PageMeta;
import com.hungphu.crm.shared.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<NotificationResponse>> findMine(UserDetailsImpl currentUser, Pageable pageable) {
        Page<Notification> page = notificationRepository.findByUserIdOrderByCreatedAtDesc(
                currentUser.getId(), pageable);
        List<NotificationResponse> data = page.getContent().stream()
                .map(notificationMapper::toResponse).toList();
        PageMeta meta = PageMeta.builder()
                .page(pageable.getPageNumber() + 1)
                .limit(pageable.getPageSize())
                .total(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
        return ApiResponse.success(data, meta);
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnread(UserDetailsImpl currentUser) {
        return notificationRepository.countByUserIdAndReadFalse(currentUser.getId());
    }

    @Override
    @Transactional
    public void markRead(UUID id, UserDetailsImpl currentUser) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Thông báo", id));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllRead(UserDetailsImpl currentUser) {
        notificationRepository.markAllRead(currentUser.getId());
    }

    @Override
    @Transactional
    public void createMaintenanceReminder(MaintenanceTask task) {
        if (task.getAssignedTo() == null) {
            log.warn("Cannot create maintenance reminder: task {} has no assignee", task.getId());
            return;
        }

        String contractInfo = task.getContract().getProject() != null
                ? task.getContract().getProject().getName()
                : task.getContract().getCustomer().getFullName();

        Notification notification = new Notification();
        notification.setUser(task.getAssignedTo());
        notification.setType(NotificationType.MAINTENANCE_REMINDER);
        notification.setTitle("Nhắc lịch bảo trì");
        notification.setBody(String.format("Tác vụ \"%s\" đến hạn vào %s",
                task.getTitle(), task.getScheduledDate()));
        notification.setRefType("maintenance_task");
        notification.setRefId(task.getId());
        notificationRepository.save(notification);
        log.debug("Created maintenance reminder for task {}", task.getId());
    }

    @Override
    @Transactional
    public void createOverdueMaintenanceAlert(MaintenanceTask task, int daysOverdue) {
        String title = "Tác vụ bảo trì quá hạn";
        String body = String.format(
                "⚠️ Tác vụ \"%s\" đã quá hạn %d ngày (dự kiến: %s)",
                task.getTitle(),
                daysOverdue,
                task.getScheduledDate()
        );

        // Gửi cho người thực hiện
        if (task.getAssignedTo() != null) {
            createNotification(
                    task.getAssignedTo(),
                    title,
                    body,
                    NotificationType.MAINTENANCE_OVERDUE,
                    "maintenance_task",
                    task.getId()
            );
        }

        // Gửi cho người theo dõi (nếu có và khác người thực hiện)
        if (task.getWatcher() != null && !task.getWatcher().equals(task.getAssignedTo())) {
            createNotification(
                    task.getWatcher(),
                    title,
                    body,
                    NotificationType.MAINTENANCE_OVERDUE,
                    "maintenance_task",
                    task.getId()
            );
        }

        // Gửi cho chủ sở hữu (nếu có và khác 2 người trên)
        if (task.getCreatedBy() != null
                && !task.getCreatedBy().equals(task.getAssignedTo())
                && !task.getCreatedBy().equals(task.getWatcher())) {
            createNotification(
                    task.getCreatedBy(),
                    title,
                    body,
                    NotificationType.MAINTENANCE_OVERDUE,
                    "maintenance_task",
                    task.getId()
            );
        }

        log.info("Created overdue alerts for task {} ({} days late)", task.getId(), daysOverdue);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Private helper
    // ══════════════════════════════════════════════════════════════════════════

    private void createNotification(User user, String title, String body,
                                    NotificationType type, String refType, UUID refId) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setBody(body);
        notification.setType(type);
        notification.setRefType(refType);
        notification.setRefId(refId);
        notificationRepository.save(notification);
    }
}