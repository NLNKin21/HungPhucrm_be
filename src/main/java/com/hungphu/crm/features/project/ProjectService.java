package com.hungphu.crm.features.project;

import com.hungphu.crm.features.project.dto.*;
import com.hungphu.crm.shared.enums.ProjectStatus;
import com.hungphu.crm.shared.response.ApiResponse;
import com.hungphu.crm.shared.security.UserDetailsImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface ProjectService {

    ApiResponse<List<ProjectResponse>> findAll(UUID customerId, boolean myProjectsOnly, UserDetailsImpl currentUser, Pageable pageable);

    ProjectResponse findById(UUID id);

    ProjectStatsResponse getStats(UUID projectId);

    ProjectResponse convertFromConsultation(UUID consultationId, ConvertToProjectRequest request,
                                            UserDetailsImpl currentUser);

    PaymentInstallmentResponse addPayment(UUID projectId, AddPaymentRequest request, UserDetailsImpl currentUser);

    List<PaymentInstallmentResponse> getPayments(UUID projectId);

    void uploadDocument(UUID projectId, MultipartFile file, String label, UserDetailsImpl currentUser);

    void deleteDocument(UUID projectId, UUID docId);

    ProjectResponse updateProjectStatus(UUID id, ProjectStatus newStatus, UserDetailsImpl currentUser);
    
    ProjectResponse createProject(CreateProjectRequest request, UserDetailsImpl currentUser);
}
