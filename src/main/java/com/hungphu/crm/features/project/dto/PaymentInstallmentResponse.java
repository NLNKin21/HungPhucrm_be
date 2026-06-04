package com.hungphu.crm.features.project.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class PaymentInstallmentResponse {
    private UUID id;
    private Short installmentNo;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private String invoicePdfUrl;
    private String notes;
    private LocalDateTime createdAt;
}
