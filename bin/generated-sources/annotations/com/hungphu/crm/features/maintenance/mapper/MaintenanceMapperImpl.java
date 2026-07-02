package com.hungphu.crm.features.maintenance.mapper;

import com.hungphu.crm.features.customer.entity.Customer;
import com.hungphu.crm.features.maintenance.dto.CommentResponse;
import com.hungphu.crm.features.maintenance.dto.ContractResponse;
import com.hungphu.crm.features.maintenance.dto.MaintenanceTaskResponse;
import com.hungphu.crm.features.maintenance.dto.TemplateResponse;
import com.hungphu.crm.features.maintenance.entity.MaintenanceComment;
import com.hungphu.crm.features.maintenance.entity.MaintenanceContract;
import com.hungphu.crm.features.maintenance.entity.MaintenanceTask;
import com.hungphu.crm.features.maintenance.entity.MaintenanceTemplate;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-30T23:15:22+0700",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.100.v20260624-0231, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class MaintenanceMapperImpl implements MaintenanceMapper {

    @Override
    public ContractResponse toContractResponse(MaintenanceContract contract) {
        if ( contract == null ) {
            return null;
        }

        ContractResponse.ContractResponseBuilder contractResponse = ContractResponse.builder();

        contractResponse.customer( customerToCustomerInfo( contract.getCustomer() ) );
        contractResponse.project( toProjectInfo( contract.getProject() ) );
        contractResponse.assignedTo( toContractUserInfo( contract.getAssignedTo() ) );
        contractResponse.cycleMonths( contract.getCycleMonths() );
        contractResponse.id( contract.getId() );
        contractResponse.startDate( contract.getStartDate() );
        contractResponse.endDate( contract.getEndDate() );
        contractResponse.status( contract.getStatus() );
        contractResponse.createdAt( contract.getCreatedAt() );

        contractResponse.schedulesGenerated( contract.getTasks().size() );

        return contractResponse.build();
    }

    @Override
    public MaintenanceTaskResponse toTaskResponse(MaintenanceTask task) {
        if ( task == null ) {
            return null;
        }

        MaintenanceTaskResponse.MaintenanceTaskResponseBuilder maintenanceTaskResponse = MaintenanceTaskResponse.builder();

        maintenanceTaskResponse.createdBy( toTaskUserInfo( task.getCreatedBy() ) );
        maintenanceTaskResponse.assignedTo( toTaskUserInfo( task.getAssignedTo() ) );
        maintenanceTaskResponse.watcher( toTaskUserInfo( task.getWatcher() ) );
        maintenanceTaskResponse.contract( toTaskContractInfo( task.getContract() ) );
        maintenanceTaskResponse.id( task.getId() );
        maintenanceTaskResponse.title( task.getTitle() );
        maintenanceTaskResponse.description( task.getDescription() );
        maintenanceTaskResponse.contactPhone( task.getContactPhone() );
        maintenanceTaskResponse.scheduledDate( task.getScheduledDate() );
        maintenanceTaskResponse.status( task.getStatus() );
        maintenanceTaskResponse.completedLate( task.isCompletedLate() );
        maintenanceTaskResponse.daysLate( task.getDaysLate() );
        maintenanceTaskResponse.completedAt( task.getCompletedAt() );
        maintenanceTaskResponse.createdAt( task.getCreatedAt() );

        maintenanceTaskResponse.overdue( isOverdue(task) );
        maintenanceTaskResponse.commentCount( task.getComments().size() );

        return maintenanceTaskResponse.build();
    }

    @Override
    public CommentResponse toCommentResponse(MaintenanceComment comment) {
        if ( comment == null ) {
            return null;
        }

        CommentResponse.CommentResponseBuilder commentResponse = CommentResponse.builder();

        commentResponse.parentId( commentParentId( comment ) );
        commentResponse.user( toCommentUserInfo( comment.getUser() ) );
        commentResponse.attachments( toAttachmentInfoList( comment.getAttachments() ) );
        commentResponse.replies( toCommentResponseList( comment.getReplies() ) );
        commentResponse.id( comment.getId() );
        commentResponse.content( comment.getContent() );
        commentResponse.createdAt( comment.getCreatedAt() );

        return commentResponse.build();
    }

    @Override
    public TemplateResponse toTemplateResponse(MaintenanceTemplate template) {
        if ( template == null ) {
            return null;
        }

        TemplateResponse.TemplateResponseBuilder templateResponse = TemplateResponse.builder();

        templateResponse.defaultAssignedTo( toTemplateUserInfo( template.getDefaultAssignedTo() ) );
        templateResponse.defaultWatcher( toTemplateUserInfo( template.getDefaultWatcher() ) );
        templateResponse.createdBy( toTemplateUserInfo( template.getCreatedBy() ) );
        templateResponse.id( template.getId() );
        templateResponse.title( template.getTitle() );
        templateResponse.description( template.getDescription() );
        templateResponse.cycleMonths( template.getCycleMonths() );
        templateResponse.durationMonths( template.getDurationMonths() );
        templateResponse.active( template.isActive() );
        templateResponse.createdAt( template.getCreatedAt() );

        return templateResponse.build();
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

    private UUID commentParentId(MaintenanceComment maintenanceComment) {
        if ( maintenanceComment == null ) {
            return null;
        }
        MaintenanceComment parent = maintenanceComment.getParent();
        if ( parent == null ) {
            return null;
        }
        UUID id = parent.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
