package com.hungphu.crm.features.maintenance.mapper;

import com.hungphu.crm.features.maintenance.dto.ApprovalResponse;
import com.hungphu.crm.features.maintenance.dto.CommentResponse;
import com.hungphu.crm.features.maintenance.dto.ContractResponse;
import com.hungphu.crm.features.maintenance.dto.EvidenceResponse;
import com.hungphu.crm.features.maintenance.dto.MaintenanceTaskResponse;
import com.hungphu.crm.features.maintenance.dto.TemplateResponse;
import com.hungphu.crm.features.maintenance.entity.MaintenanceApproval;
import com.hungphu.crm.features.maintenance.entity.MaintenanceComment;
import com.hungphu.crm.features.maintenance.entity.MaintenanceContract;
import com.hungphu.crm.features.maintenance.entity.MaintenanceEvidence;
import com.hungphu.crm.features.maintenance.entity.MaintenanceTask;
import com.hungphu.crm.features.maintenance.entity.MaintenanceTemplate;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-07T01:15:43+0700",
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

        contractResponse.project( toProjectInfo( contract.getProject() ) );
        contractResponse.customer( toCustomerInfo( contract.getCustomer() ) );
        contractResponse.assignedTo( toContractUserInfo( contract.getAssignedTo() ) );
        contractResponse.supervisor( toContractUserInfo( contract.getSupervisor() ) );
        contractResponse.cycleMonths( contract.getCycleMonths() );
        contractResponse.firstMaintenanceImmediate( contract.isFirstMaintenanceImmediate() );
        contractResponse.createdAt( contract.getCreatedAt() );
        contractResponse.endDate( contract.getEndDate() );
        contractResponse.id( contract.getId() );
        contractResponse.startDate( contract.getStartDate() );
        contractResponse.status( contract.getStatus() );

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
        maintenanceTaskResponse.supervisor( toTaskUserInfo( task.getSupervisor() ) );
        maintenanceTaskResponse.contract( toTaskContractInfo( task.getContract() ) );
        maintenanceTaskResponse.submittedAt( task.getSubmittedAt() );
        maintenanceTaskResponse.completedAt( task.getCompletedAt() );
        maintenanceTaskResponse.completedLate( task.isCompletedLate() );
        maintenanceTaskResponse.contactPhone( task.getContactPhone() );
        maintenanceTaskResponse.createdAt( task.getCreatedAt() );
        maintenanceTaskResponse.daysLate( task.getDaysLate() );
        maintenanceTaskResponse.description( task.getDescription() );
        maintenanceTaskResponse.id( task.getId() );
        maintenanceTaskResponse.scheduledDate( task.getScheduledDate() );
        maintenanceTaskResponse.status( task.getStatus() );
        maintenanceTaskResponse.title( task.getTitle() );

        maintenanceTaskResponse.overdue( isOverdue(task) );
        maintenanceTaskResponse.commentCount( task.getComments() != null ? task.getComments().size() : 0 );
        maintenanceTaskResponse.evidenceCount( task.getEvidences() != null ? task.getEvidences().size() : 0 );

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
        commentResponse.content( comment.getContent() );
        commentResponse.createdAt( comment.getCreatedAt() );
        commentResponse.id( comment.getId() );

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
        templateResponse.active( template.isActive() );
        templateResponse.createdAt( template.getCreatedAt() );
        templateResponse.cycleMonths( template.getCycleMonths() );
        templateResponse.description( template.getDescription() );
        templateResponse.durationMonths( template.getDurationMonths() );
        templateResponse.id( template.getId() );
        templateResponse.title( template.getTitle() );

        return templateResponse.build();
    }

    @Override
    public EvidenceResponse toEvidenceResponse(MaintenanceEvidence evidence) {
        if ( evidence == null ) {
            return null;
        }

        EvidenceResponse.EvidenceResponseBuilder evidenceResponse = EvidenceResponse.builder();

        evidenceResponse.uploadedBy( toEvidenceUserInfo( evidence.getUploadedBy() ) );
        evidenceResponse.description( evidence.getDescription() );
        evidenceResponse.fileSize( evidence.getFileSize() );
        evidenceResponse.fileType( evidence.getFileType() );
        evidenceResponse.fileUrl( evidence.getFileUrl() );
        evidenceResponse.id( evidence.getId() );
        evidenceResponse.uploadedAt( evidence.getUploadedAt() );

        return evidenceResponse.build();
    }

    @Override
    public ApprovalResponse toApprovalResponse(MaintenanceApproval approval) {
        if ( approval == null ) {
            return null;
        }

        ApprovalResponse.ApprovalResponseBuilder approvalResponse = ApprovalResponse.builder();

        approvalResponse.approvedBy( toApprovalUserInfo( approval.getApprovedBy() ) );
        approvalResponse.action( approval.getAction() );
        approvalResponse.createdAt( approval.getCreatedAt() );
        approvalResponse.id( approval.getId() );
        approvalResponse.reason( approval.getReason() );

        return approvalResponse.build();
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
