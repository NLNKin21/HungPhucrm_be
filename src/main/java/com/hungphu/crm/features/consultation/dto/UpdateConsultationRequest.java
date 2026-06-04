package com.hungphu.crm.features.consultation.dto;

import com.hungphu.crm.shared.enums.PriorityLevel;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class UpdateConsultationRequest {

    @Size(max = 150, message = "Tên khách hàng tối đa 150 ký tự")
    private String customerName;

    @Size(max = 20, message = "Số điện thoại tối đa 20 ký tự")
    private String customerPhone;

    private String siteAddress;
    private PriorityLevel priority;
    private BigDecimal price;
    private String notes;
    private UUID assignedToId;
    private UUID assignedById;
}
