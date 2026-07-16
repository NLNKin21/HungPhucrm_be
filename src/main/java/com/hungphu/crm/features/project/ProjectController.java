package com.hungphu.crm.features.project;

import com.hungphu.crm.features.project.dto.*;
import com.hungphu.crm.shared.enums.ProjectStatus;
import com.hungphu.crm.shared.response.ApiResponse;
import com.hungphu.crm.shared.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping("/projects")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> findAll(
            @RequestParam(name = "customerId", required = false) UUID customerId,
            @RequestParam(name = "myProjectsOnly", defaultValue = "false") boolean myProjectsOnly,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "limit", defaultValue = "10") int limit,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        var pageable = PageRequest.of(page - 1, limit, Sort.by("createdAt").descending());
        return ResponseEntity.ok(projectService.findAll(customerId, myProjectsOnly, currentUser, pageable));
    }

    @GetMapping("/projects/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public ResponseEntity<ApiResponse<ProjectResponse>> findById(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(ApiResponse.success(projectService.findById(id)));
    }

    @GetMapping("/projects/{id}/stats")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public ResponseEntity<ApiResponse<ProjectStatsResponse>> getStats(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(ApiResponse.success(projectService.getStats(id)));
    }

    @PostMapping("/consultations/{consultationId}/convert")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public ResponseEntity<ApiResponse<ProjectResponse>> convert(
            @PathVariable("consultationId") UUID consultationId,
            @Valid @RequestBody ConvertToProjectRequest request,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        projectService.convertFromConsultation(consultationId, request, currentUser),
                        "Chuyển tư vấn thành dự án thành công"));
    }

    @GetMapping("/projects/{id}/payments")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public ResponseEntity<ApiResponse<List<PaymentInstallmentResponse>>> getPayments(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(ApiResponse.success(projectService.getPayments(id)));
    }

    @PostMapping("/projects")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(
            @Valid @RequestBody CreateProjectRequest request,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        projectService.createProject(request, currentUser),
                        "Tạo dự án thành công"));
    }

    @PostMapping("/projects/{id}/payments")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<PaymentInstallmentResponse>> addPayment(
            @PathVariable("id") UUID id,
            @Valid @RequestBody AddPaymentRequest request,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(projectService.addPayment(id, request, currentUser), "Thêm đợt thanh toán thành công"));
    }

    @PostMapping("/projects/{id}/documents")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public ResponseEntity<ApiResponse<Void>> uploadDocument(
            @PathVariable("id") UUID id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(name = "label", required = false) String label,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        projectService.uploadDocument(id, file, label, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(null, "Upload tài liệu thành công"));
    }

    @DeleteMapping("/projects/{id}/documents/{docId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(
            @PathVariable("id") UUID id,
            @PathVariable("docId") UUID docId) {
        projectService.deleteDocument(id, docId);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa tài liệu thành công"));
    }

    @PatchMapping("/projects/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateStatus(
            @PathVariable("id") UUID id,
            @RequestParam("status") ProjectStatus status,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.ok(ApiResponse.success(
                projectService.updateProjectStatus(id, status, currentUser),
                "Cập nhật tiến độ dự án thành công"));
    }
}
