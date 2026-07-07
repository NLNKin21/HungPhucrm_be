package com.hungphu.crm.features.maintenance.mapper;

import com.hungphu.crm.features.maintenance.dto.*;
import com.hungphu.crm.features.maintenance.entity.*;
import com.hungphu.crm.features.user.entity.User;
import com.hungphu.crm.shared.enums.ScheduleStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDate;
import java.util.List;

@Mapper(componentModel = "spring")
public interface MaintenanceMapper {

    // ══════════════════════════════════════════════════════════════
    // Contract
    // ══════════════════════════════════════════════════════════════

    @Mapping(target = "project", source = "project")
    @Mapping(target = "customer", source = "customer")
    @Mapping(target = "assignedTo", source = "assignedTo")
    @Mapping(target = "supervisor", source = "supervisor")
    @Mapping(target = "cycleMonths", source = "cycleMonths")
    @Mapping(target = "firstMaintenanceImmediate", source = "firstMaintenanceImmediate")
    @Mapping(target = "schedulesGenerated", expression = "java(contract.getTasks().size())")
    ContractResponse toContractResponse(MaintenanceContract contract);

    // ══════════════════════════════════════════════════════════════
    // Task
    // ══════════════════════════════════════════════════════════════

    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "assignedTo", source = "assignedTo")
    @Mapping(target = "watcher", source = "watcher")
    @Mapping(target = "supervisor", source = "supervisor")
    @Mapping(target = "contract", source = "contract")
    @Mapping(target = "overdue", expression = "java(isOverdue(task))")
    @Mapping(target = "commentCount", expression = "java(task.getComments() != null ? task.getComments().size() : 0)")
    @Mapping(target = "evidenceCount", expression = "java(task.getEvidences() != null ? task.getEvidences().size() : 0)")
    @Mapping(target = "submittedAt", source = "submittedAt")
    MaintenanceTaskResponse toTaskResponse(MaintenanceTask task);

    // ══════════════════════════════════════════════════════════════
    // Comment
    // ══════════════════════════════════════════════════════════════

    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "user", source = "user")
    @Mapping(target = "attachments", source = "attachments")
    @Mapping(target = "replies", source = "replies")
    CommentResponse toCommentResponse(MaintenanceComment comment);

    // ══════════════════════════════════════════════════════════════
    // Template
    // ══════════════════════════════════════════════════════════════

    @Mapping(target = "defaultAssignedTo", source = "defaultAssignedTo")
    @Mapping(target = "defaultWatcher", source = "defaultWatcher")
    @Mapping(target = "createdBy", source = "createdBy")
    TemplateResponse toTemplateResponse(MaintenanceTemplate template);

    // ══════════════════════════════════════════════════════════════
    // Evidence ★ MỚI
    // ══════════════════════════════════════════════════════════════

    @Mapping(target = "uploadedBy", source = "uploadedBy")
    EvidenceResponse toEvidenceResponse(MaintenanceEvidence evidence);

    // ══════════════════════════════════════════════════════════════
    // Approval ★ MỚI
    // ══════════════════════════════════════════════════════════════

    @Mapping(target = "approvedBy", source = "approvedBy")
    ApprovalResponse toApprovalResponse(MaintenanceApproval approval);

    // ══════════════════════════════════════════════════════════════
    // Helpers
    // ══════════════════════════════════════════════════════════════

    default boolean isOverdue(MaintenanceTask task) {
        if (task.getStatus() == ScheduleStatus.QUA_HAN) return true;
        if (task.getStatus() == ScheduleStatus.CHO_THUC_HIEN
                || task.getStatus() == ScheduleStatus.CAN_BO_SUNG) {
            return task.getScheduledDate().isBefore(LocalDate.now());
        }
        return false;
    }

    // ── User Info mappers ──

    default MaintenanceTaskResponse.UserInfo toTaskUserInfo(User user) {
        if (user == null) return null;
        return MaintenanceTaskResponse.UserInfo.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .build();
    }

    default MaintenanceTaskResponse.ContractInfo toTaskContractInfo(MaintenanceContract contract) {
        if (contract == null) return null;
        return MaintenanceTaskResponse.ContractInfo.builder()
                .id(contract.getId())
                .customerId(contract.getCustomer() != null ? contract.getCustomer().getId() : null)
                .customerName(contract.getCustomer() != null ? contract.getCustomer().getFullName() : null)
                .projectId(contract.getProject() != null ? contract.getProject().getId() : null)
                .projectName(contract.getProject() != null ? contract.getProject().getName() : null)
                .build();
    }

    default ContractResponse.UserInfo toContractUserInfo(User user) {
        if (user == null) return null;
        return ContractResponse.UserInfo.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .build();
    }

    default ContractResponse.ProjectInfo toProjectInfo(
            com.hungphu.crm.features.project.entity.Project project) {
        if (project == null) return null;
        return ContractResponse.ProjectInfo.builder()
                .id(project.getId())
                .name(project.getName())
                .build();
    }

    default ContractResponse.CustomerInfo toCustomerInfo(
            com.hungphu.crm.features.customer.entity.Customer customer) {
        if (customer == null) return null;
        return ContractResponse.CustomerInfo.builder()
                .id(customer.getId())
                .fullName(customer.getFullName())
                .phone(customer.getPhone())
                .build();
    }

    default TemplateResponse.UserInfo toTemplateUserInfo(User user) {
        if (user == null) return null;
        return TemplateResponse.UserInfo.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .build();
    }

    // ── Evidence mapper ──

    default EvidenceResponse.UserInfo toEvidenceUserInfo(User user) {
        if (user == null) return null;
        return EvidenceResponse.UserInfo.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .build();
    }

    // ── Approval mapper ──

    default ApprovalResponse.UserInfo toApprovalUserInfo(User user) {
        if (user == null) return null;
        return ApprovalResponse.UserInfo.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .build();
    }

    // ── Comment helpers (giữ nguyên) ──

    default CommentResponse.UserInfo toCommentUserInfo(User user) {
        if (user == null) return null;
        return CommentResponse.UserInfo.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .avatar(user.getAvatar())
                .build();
    }

    default CommentResponse.AttachmentInfo toAttachmentInfo(MaintenanceAttachment attachment) {
        if (attachment == null) return null;
        return CommentResponse.AttachmentInfo.builder()
                .id(attachment.getId())
                .fileUrl(attachment.getFileUrl())
                .fileType(attachment.getFileType())
                .fileSize(attachment.getFileSize())
                .build();
    }

    default List<CommentResponse.AttachmentInfo> toAttachmentInfoList(
            List<MaintenanceAttachment> attachments) {
        if (attachments == null) return null;
        return attachments.stream().map(this::toAttachmentInfo).toList();
    }

    default List<CommentResponse> toCommentResponseList(List<MaintenanceComment> comments) {
        if (comments == null) return null;
        return comments.stream().map(this::toCommentResponse).toList();
    }
}