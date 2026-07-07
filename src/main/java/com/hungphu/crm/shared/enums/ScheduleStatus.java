package com.hungphu.crm.shared.enums;

public enum ScheduleStatus {
    CHO_THUC_HIEN,   // Chờ thực hiện
    QUA_HAN,          // Quá hạn
    CHO_DUYET,        // Đã gửi minh chứng, chờ supervisor duyệt
    CAN_BO_SUNG,      // Supervisor yêu cầu bổ sung minh chứng
    HOAN_THANH         // Supervisor đã duyệt → hoàn thành
}