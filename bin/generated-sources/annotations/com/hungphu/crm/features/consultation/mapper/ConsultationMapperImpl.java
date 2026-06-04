package com.hungphu.crm.features.consultation.mapper;

import com.hungphu.crm.features.consultation.dto.ConsultationResponse;
import com.hungphu.crm.features.consultation.entity.Consultation;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-04T10:13:01+0700",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class ConsultationMapperImpl implements ConsultationMapper {

    @Override
    public ConsultationResponse toResponse(Consultation consultation) {
        if ( consultation == null ) {
            return null;
        }

        ConsultationResponse.ConsultationResponseBuilder consultationResponse = ConsultationResponse.builder();

        consultationResponse.assignedBy( toAssigneeInfo( consultation.getAssignedBy() ) );
        consultationResponse.assignedTo( toAssigneeInfo( consultation.getAssignedTo() ) );
        consultationResponse.acceptedAt( consultation.getAcceptedAt() );
        consultationResponse.createdAt( consultation.getCreatedAt() );
        consultationResponse.customerName( consultation.getCustomerName() );
        consultationResponse.customerPhone( consultation.getCustomerPhone() );
        consultationResponse.elevatorType( consultation.getElevatorType() );
        consultationResponse.failureReason( consultation.getFailureReason() );
        consultationResponse.id( consultation.getId() );
        consultationResponse.notes( consultation.getNotes() );
        consultationResponse.price( consultation.getPrice() );
        consultationResponse.priority( consultation.getPriority() );
        consultationResponse.projectType( consultation.getProjectType() );
        consultationResponse.siteAddress( consultation.getSiteAddress() );
        consultationResponse.status( consultation.getStatus() );
        consultationResponse.updatedAt( consultation.getUpdatedAt() );

        return consultationResponse.build();
    }
}
