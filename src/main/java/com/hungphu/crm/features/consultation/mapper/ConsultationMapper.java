package com.hungphu.crm.features.consultation.mapper;

import com.hungphu.crm.features.consultation.dto.ConsultationResponse;
import com.hungphu.crm.features.consultation.entity.Consultation;
import com.hungphu.crm.features.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ConsultationMapper {

    @Mapping(target = "assignedBy", source = "assignedBy")
    @Mapping(target = "assignedTo", source = "assignedTo")
    ConsultationResponse toResponse(Consultation consultation);

    default ConsultationResponse.AssigneeInfo toAssigneeInfo(User user) {
        if (user == null) return null;
        return ConsultationResponse.AssigneeInfo.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .build();
    }
}
