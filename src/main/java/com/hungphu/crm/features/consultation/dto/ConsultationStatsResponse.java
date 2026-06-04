package com.hungphu.crm.features.consultation.dto;

import com.hungphu.crm.shared.enums.ConsultationStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Builder
public class ConsultationStatsResponse {

    private Map<ConsultationStatus, Long> byStatus;
    private long totalActive;
    private int successRate;
    private List<EmployeeStats> byEmployee;

    @Getter
    @Builder
    public static class EmployeeStats {
        private UUID userId;
        private String fullName;
        private long total;
        private long success;
        private long inProgress;
    }
}
