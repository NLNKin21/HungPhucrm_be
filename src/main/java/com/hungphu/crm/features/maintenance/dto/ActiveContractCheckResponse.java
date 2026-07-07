package com.hungphu.crm.features.maintenance.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class ActiveContractCheckResponse {
    private boolean hasActiveContract;
    private List<ActiveContractInfo> activeContracts;

    @Getter
    @Builder
    public static class ActiveContractInfo {
        private UUID id;
        private String customerName;
        private String projectName;
        private LocalDate startDate;
        private LocalDate endDate;
        private String status;
    }
}