package com.hungphu.crm.shared.enums;

public enum NotificationType {
    // ── Existing ──
    MAINTENANCE_REMINDER,
    MAINTENANCE_OVERDUE,
    TASK_ASSIGNED,
    TASK_COMPLETED,
    TASK_REJECTED,
    CONSULTATION_ASSIGNED,

    // ── Mới cho bảo trì ──
    MAINTENANCE_REMINDER_7,        // Nhắc 7 ngày trước
    MAINTENANCE_REMINDER_3,        // Nhắc 3 ngày trước
    MAINTENANCE_REMINDER_1,        // Nhắc 1 ngày trước
    MAINTENANCE_OVERDUE_ALERT,     // Cảnh báo 17h ngày hết hạn
    MAINTENANCE_SUBMITTED,         // KTV gửi minh chứng chờ duyệt
    MAINTENANCE_APPROVED,          // Supervisor duyệt xong
    MAINTENANCE_REJECTED,          // Supervisor từ chối
    MAINTENANCE_MORE_EVIDENCE,     // Yêu cầu bổ sung minh chứng
}