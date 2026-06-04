package com.hungphu.crm.features.consultation.dto;

import com.hungphu.crm.shared.enums.PriorityLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class CreateConsultationRequest {

    private UUID customerId;

    @NotBlank(message = "Tên khách hàng không được trống")
    @Size(max = 150)
    private String customerName;

    @NotBlank(message = "Số điện thoại không được trống")
    @Size(max = 20)
    private String customerPhone;

    private String siteAddress;

    @NotNull(message = "Mức độ ưu tiên không được trống")
    private PriorityLevel priority;

    private BigDecimal price;
    private String notes;

    @NotNull(message = "Nhân viên phụ trách không được trống")
    private UUID assignedTo;

    private UUID assignedById;
}
