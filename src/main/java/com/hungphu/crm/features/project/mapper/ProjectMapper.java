package com.hungphu.crm.features.project.mapper;

import com.hungphu.crm.features.project.dto.PaymentInstallmentResponse;
import com.hungphu.crm.features.project.dto.ProjectResponse;
import com.hungphu.crm.features.project.entity.PaymentInstallment;
import com.hungphu.crm.features.project.entity.Project;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    @Mapping(target = "customer.id", source = "customer.id")
    @Mapping(target = "customer.fullName", source = "customer.fullName")
    @Mapping(target = "customer.phone", source = "customer.phone")
    @Mapping(target = "supervisor.id", source = "supervisor.id")
    @Mapping(target = "supervisor.fullName", source = "supervisor.fullName")
    ProjectResponse toResponse(Project project);

    PaymentInstallmentResponse toPaymentResponse(PaymentInstallment installment);
}
