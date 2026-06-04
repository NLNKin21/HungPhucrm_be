package com.hungphu.crm.features.consultation.dto;

import com.hungphu.crm.shared.enums.ConsultationStatus;
import com.hungphu.crm.shared.enums.ElevatorType;
import com.hungphu.crm.shared.enums.ProjectType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UpdateConsultationStatusRequest {

    @NotNull(message = "Trạng thái không được trống")
    private ConsultationStatus status;

    private String failureReason;

    // ── Bắt buộc khi status = DANG_BAO_GIA ────────────────────────────────
    // Validate logic nằm trong ConsultationServiceImpl.validateStatusPayload()
    // thay vì @NotNull ở đây, để message lỗi rõ ràng hơn cho từng trường hợp.

    private BigDecimal price;

    private ElevatorType elevatorType;

    private ProjectType projectType;
}