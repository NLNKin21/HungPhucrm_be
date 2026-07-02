package com.hungphu.crm.features.project.mapper;

import com.hungphu.crm.features.consultation.entity.Consultation;
import com.hungphu.crm.features.customer.entity.Customer;
import com.hungphu.crm.features.project.dto.PaymentInstallmentResponse;
import com.hungphu.crm.features.project.dto.ProjectResponse;
import com.hungphu.crm.features.project.entity.PaymentInstallment;
import com.hungphu.crm.features.project.entity.Project;
import com.hungphu.crm.features.user.entity.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-30T11:27:41+0700",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.100.v20260624-0231, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class ProjectMapperImpl implements ProjectMapper {

    @Override
    public ProjectResponse toResponse(Project project) {
        if ( project == null ) {
            return null;
        }

        ProjectResponse.ProjectResponseBuilder projectResponse = ProjectResponse.builder();

        projectResponse.customer( customerToCustomerInfo( project.getCustomer() ) );
        projectResponse.supervisor( userToUserInfo( project.getSupervisor() ) );
        projectResponse.siteAddress( projectConsultationSiteAddress( project ) );
        projectResponse.createdAt( project.getCreatedAt() );
        projectResponse.elevatorType( project.getElevatorType() );
        projectResponse.id( project.getId() );
        projectResponse.name( project.getName() );
        projectResponse.projectStatus( project.getProjectStatus() );
        projectResponse.projectType( project.getProjectType() );
        projectResponse.updatedAt( project.getUpdatedAt() );

        return projectResponse.build();
    }

    @Override
    public PaymentInstallmentResponse toPaymentResponse(PaymentInstallment installment) {
        if ( installment == null ) {
            return null;
        }

        PaymentInstallmentResponse.PaymentInstallmentResponseBuilder paymentInstallmentResponse = PaymentInstallmentResponse.builder();

        paymentInstallmentResponse.amount( installment.getAmount() );
        paymentInstallmentResponse.createdAt( installment.getCreatedAt() );
        paymentInstallmentResponse.id( installment.getId() );
        paymentInstallmentResponse.installmentNo( installment.getInstallmentNo() );
        paymentInstallmentResponse.invoicePdfUrl( installment.getInvoicePdfUrl() );
        paymentInstallmentResponse.notes( installment.getNotes() );
        paymentInstallmentResponse.paymentDate( installment.getPaymentDate() );

        return paymentInstallmentResponse.build();
    }

    protected ProjectResponse.CustomerInfo customerToCustomerInfo(Customer customer) {
        if ( customer == null ) {
            return null;
        }

        ProjectResponse.CustomerInfo.CustomerInfoBuilder customerInfo = ProjectResponse.CustomerInfo.builder();

        customerInfo.id( customer.getId() );
        customerInfo.fullName( customer.getFullName() );
        customerInfo.phone( customer.getPhone() );

        return customerInfo.build();
    }

    protected ProjectResponse.UserInfo userToUserInfo(User user) {
        if ( user == null ) {
            return null;
        }

        ProjectResponse.UserInfo.UserInfoBuilder userInfo = ProjectResponse.UserInfo.builder();

        userInfo.id( user.getId() );
        userInfo.fullName( user.getFullName() );

        return userInfo.build();
    }

    private String projectConsultationSiteAddress(Project project) {
        if ( project == null ) {
            return null;
        }
        Consultation consultation = project.getConsultation();
        if ( consultation == null ) {
            return null;
        }
        String siteAddress = consultation.getSiteAddress();
        if ( siteAddress == null ) {
            return null;
        }
        return siteAddress;
    }
}
