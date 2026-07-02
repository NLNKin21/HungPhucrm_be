package com.hungphu.crm.features.maintenance.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CreateTemplateRequest {

    @NotBlank(message = "Tên khuôn mẫu không được trống")
    @Size(max = 255)
    private String title;

    private String description;

    @Min(value = 1, message = "Chu kỳ tối thiểu 1 tháng")
    @Max(value = 12, message = "Chu kỳ tối đa 12 tháng")
    private Integer cycleMonths = 2;

    @Min(value = 1, message = "Thời hạn tối thiểu 1 tháng")
    @Max(value = 60, message = "Thời hạn tối đa 60 tháng")
    private Integer durationMonths = 12;

    private UUID defaultAssignedTo;
    private UUID defaultWatcherId;
}