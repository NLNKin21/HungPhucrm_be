package com.hungphu.crm.features.maintenance.mapper;

import com.hungphu.crm.features.customer.entity.Customer;
import com.hungphu.crm.features.maintenance.dto.ContractResponse;
import com.hungphu.crm.features.maintenance.dto.ScheduleResponse;
import com.hungphu.crm.features.maintenance.entity.MaintenanceContract;
import com.hungphu.crm.features.maintenance.entity.MaintenanceSchedule;
import com.hungphu.crm.features.project.entity.Project;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-23T20:55:30+0700",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class MaintenanceMapperImpl implements MaintenanceMapper {

    @Override
    public ContractResponse toContractResponse(MaintenanceContract contract) {
        if ( contract == null ) {
            return null;
        }

        ContractResponse.ContractResponseBuilder contractResponse = ContractResponse.builder();

        contractResponse.project( projectToProjectInfo( contract.getProject() ) );
        contractResponse.customer( customerToCustomerInfo( contract.getCustomer() ) );
        contractResponse.assignedTo( toContractUserInfo( contract.getAssignedTo() ) );
        contractResponse.cycleMonths( contract.getCycleMonths() );
        contractResponse.id( contract.getId() );
        contractResponse.startDate( contract.getStartDate() );
        contractResponse.endDate( contract.getEndDate() );
        contractResponse.status( contract.getStatus() );
        contractResponse.createdAt( contract.getCreatedAt() );

        contractResponse.schedulesGenerated( contract.getSchedules().size() );

        return contractResponse.build();
    }

    @Override
    public ScheduleResponse toScheduleResponse(MaintenanceSchedule schedule) {
        if ( schedule == null ) {
            return null;
        }

        ScheduleResponse.ScheduleResponseBuilder scheduleResponse = ScheduleResponse.builder();

        scheduleResponse.assignedTo( toUserInfo( schedule.getAssignedTo() ) );
        scheduleResponse.evidences( toEvidenceInfoList( schedule.getEvidences() ) );
        scheduleResponse.id( schedule.getId() );
        scheduleResponse.scheduledDate( schedule.getScheduledDate() );
        scheduleResponse.status( schedule.getStatus() );
        scheduleResponse.completedAt( schedule.getCompletedAt() );
        scheduleResponse.completedLate( schedule.isCompletedLate() );
        scheduleResponse.daysLate( schedule.getDaysLate() );
        scheduleResponse.notes( schedule.getNotes() );
        scheduleResponse.createdAt( schedule.getCreatedAt() );

        scheduleResponse.overdue( isOverdue(schedule) );

        return scheduleResponse.build();
    }

    protected ContractResponse.ProjectInfo projectToProjectInfo(Project project) {
        if ( project == null ) {
            return null;
        }

        ContractResponse.ProjectInfo.ProjectInfoBuilder projectInfo = ContractResponse.ProjectInfo.builder();

        projectInfo.id( project.getId() );
        projectInfo.name( project.getName() );

        return projectInfo.build();
    }

    protected ContractResponse.CustomerInfo customerToCustomerInfo(Customer customer) {
        if ( customer == null ) {
            return null;
        }

        ContractResponse.CustomerInfo.CustomerInfoBuilder customerInfo = ContractResponse.CustomerInfo.builder();

        customerInfo.id( customer.getId() );
        customerInfo.fullName( customer.getFullName() );

        return customerInfo.build();
    }
}
