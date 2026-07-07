package com.hungphu.crm.features.maintenance;

import com.hungphu.crm.features.maintenance.dto.*;
import com.hungphu.crm.shared.enums.ScheduleStatus;
import com.hungphu.crm.shared.security.UserDetailsImpl;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface MaintenanceService {

    // ── Contracts ──
    List<ContractResponse> findAllContracts();
    ContractResponse findContractById(UUID id);
    ContractResponse createContract(CreateContractRequest request, UserDetailsImpl currentUser);
    ContractResponse createContractInternal(CreateContractRequest request);
    ContractResponse updateContract(UUID contractId, UpdateContractRequest request, UserDetailsImpl currentUser);
    void deleteContract(UUID contractId);
    ContractResponse renewContract(UUID contractId, RenewContractRequest request, UserDetailsImpl currentUser);

    // ── Tasks ──
    List<MaintenanceTaskResponse> findTasksByContract(UUID contractId);
    List<MaintenanceTaskResponse> findAllTasks(
            ScheduleStatus status,
            UUID assignedTo,
            UUID contractId,
            UUID customerId,
            String from,
            String to,
            UserDetailsImpl currentUser
    );
    MaintenanceTaskResponse findTaskById(UUID taskId);
    MaintenanceTaskResponse updateTask(UUID taskId, UpdateTaskRequest request, UserDetailsImpl currentUser);
    MaintenanceTaskResponse completeTask(UUID taskId, UserDetailsImpl currentUser);

    // ── Comments ──
    List<CommentResponse> findCommentsByTask(UUID taskId);
    CommentResponse addComment(UUID taskId, String content, UUID parentId,
                               List<MultipartFile> files, UserDetailsImpl currentUser);

    // ── Stats ──
    MaintenanceStatsResponse getStats();

    // ── Templates ──
    List<TemplateResponse> findAllTemplates();
    TemplateResponse findTemplateById(UUID id);
    TemplateResponse createTemplate(CreateTemplateRequest request, UserDetailsImpl currentUser);
    TemplateResponse updateTemplate(UUID id, UpdateTemplateRequest request, UserDetailsImpl currentUser);
    void deleteTemplate(UUID id);

    // ── Contract from Template ──
    ContractResponse createContractFromTemplate(CreateContractFromTemplateRequest request, UserDetailsImpl currentUser);
    List<ContractResponse> findActiveContractsByCustomer(UUID customerId, UUID projectId);
    
    // ── Evidence ──
    List<EvidenceResponse> findEvidencesByTask(UUID taskId);
    EvidenceResponse addEvidence(UUID taskId, String description,
                                MultipartFile file, UserDetailsImpl currentUser);
    void deleteEvidence(UUID taskId, UUID evidenceId, UserDetailsImpl currentUser);

    // ── Workflow ──
    MaintenanceTaskResponse submitTask(UUID taskId, UserDetailsImpl currentUser);
    MaintenanceTaskResponse approveTask(UUID taskId, UserDetailsImpl currentUser);
    MaintenanceTaskResponse rejectTask(UUID taskId, String reason, UserDetailsImpl currentUser);

    // ── Approvals ──
    List<ApprovalResponse> findApprovalsByTask(UUID taskId);

    // ── Customer Lookup (public) ──
    CustomerLookupResponse lookupByPhone(String phone);
}