package com.hungphu.crm.features.maintenance.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MaintenanceStatsResponse {
    
    // Contract stats
    private long totalContracts;
    private long activeContracts;      // MOI
    private long expiringContracts;    // SAP_HET_HAN
    private long expiredContracts;     // HET_HAN
    
    // Schedule stats
    private long totalSchedules;
    private long pendingSchedules;     // CHO_THUC_HIEN
    private long overdueSchedules;     // QUA_HAN
    private long completedSchedules;   // HOAN_THANH
    private long completedLateCount;   // Hoàn thành trễ
    
    // Lists for dashboard
    private List<MaintenanceTaskResponse> upcomingSchedules;  // 7 ngày tới
    private List<MaintenanceTaskResponse> overdueList;        // Danh sách quá hạn
}