package com.hungphu.crm.features.maintenance.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RejectTaskRequest {
    @NotBlank(message = "Vui lòng nhập lý do từ chối")
    private String reason;
}