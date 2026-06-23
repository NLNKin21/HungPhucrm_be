package com.hungphu.crm.features.maintenance.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class RenewContractRequest {

    @NotNull(message = "Ngày kết thúc mới không được trống")
    private LocalDate newEndDate;

    @Min(value = 1, message = "Chu kỳ tối thiểu 1 tháng")
    @Max(value = 12, message = "Chu kỳ tối đa 12 tháng")
    private Integer cycleMonths; // Null = giữ nguyên chu kỳ cũ

    private UUID assignedTo; // Null = giữ nguyên người phụ trách cũ
}