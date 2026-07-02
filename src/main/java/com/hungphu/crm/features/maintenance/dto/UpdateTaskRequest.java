package com.hungphu.crm.features.maintenance.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class UpdateTaskRequest {
    private UUID assignedTo;
    private UUID watcherId;
    private LocalDate scheduledDate;
    private String contactPhone;
    private String description;
}