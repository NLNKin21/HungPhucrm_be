package com.hungphu.crm.features.notification;

import com.hungphu.crm.features.maintenance.entity.MaintenanceSchedule;
import com.hungphu.crm.features.notification.dto.NotificationResponse;
import com.hungphu.crm.features.notification.entity.Notification;
import com.hungphu.crm.features.notification.mapper.NotificationMapper;
import com.hungphu.crm.features.notification.repository.NotificationRepository;
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
    public void createMaintenanceReminder(MaintenanceSchedule schedule) {
        Notification notification = new Notification();
        notification.setUser(schedule.getAssignedTo());
        notification.setType(NotificationType.MAINTENANCE_REMINDER);
        notification.setTitle("Nhắc lịch bảo trì");
        notification.setBody("Lịch bảo trì hợp đồng #" + schedule.getContract().getId()
                + " đến hạn vào " + schedule.getScheduledDate());
        notification.setRefType("maintenance_schedule");
        notification.setRefId(schedule.getId());
        notificationRepository.save(notification);
        log.debug("Created maintenance reminder for schedule {}", schedule.getId());
    }
}
