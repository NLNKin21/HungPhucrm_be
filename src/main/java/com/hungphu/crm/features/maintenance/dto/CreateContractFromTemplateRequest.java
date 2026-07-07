package com.hungphu.crm.features.maintenance.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class CreateContractFromTemplateRequest {

    @NotNull(message = "Khuôn mẫu không được trống")
    private UUID templateId;

    @NotNull(message = "Khách hàng không được trống")
    private UUID customerId;

    private UUID projectId;

    private LocalDate startDate; // Null = hôm nay

    // Override từ template (tuỳ chọn)
    private UUID assignedTo;
    private UUID watcherId;
    private UUID supervisorId;
    private Boolean firstMaintenanceImmediate;
}