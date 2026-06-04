package com.hungphu.crm.features.project.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class AddPaymentRequest {

    @NotNull(message = "Số đợt không được trống")
    @Min(value = 1)
    private Short installmentNo;

    @NotNull(message = "Số tiền không được trống")
    @Min(value = 0)
    private BigDecimal amount;

    private LocalDate paymentDate;
    private String notes;
}
