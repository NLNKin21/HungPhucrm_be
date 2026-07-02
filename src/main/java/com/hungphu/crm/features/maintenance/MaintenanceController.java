package com.hungphu.crm.features.maintenance;

import com.hungphu.crm.features.maintenance.dto.*;
import com.hungphu.crm.shared.response.ApiResponse;
import com.hungphu.crm.shared.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.hungphu.crm.shared.enums.ScheduleStatus;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/maintenance")
@RequiredArgsConstructor
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    // ── Contracts ──

    @GetMapping("/contracts")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public ResponseEntity<ApiResponse<List<ContractResponse>>> findAllContracts() {
        return ResponseEntity.ok(ApiResponse.success(maintenanceService.findAllContracts()));
    }

    @GetMapping("/contracts/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public ResponseEntity<ApiResponse<ContractResponse>> findContractById(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(ApiResponse.success(maintenanceService.findContractById(id)));
    }

    @PostMapping("/contracts")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<ContractResponse>> createContract(
            @Valid @RequestBody CreateContractRequest request,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(maintenanceService.createContract(request, currentUser),
                        "Tạo hợp đồng bảo trì thành công"));
    }

    @PutMapping("/contracts/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ContractResponse>> updateContract(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdateContractRequest request,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.ok(ApiResponse.success(
                maintenanceService.updateContract(id, request, currentUser),
                "Cập nhật hợp đồng thành công"));
    }

    @DeleteMapping("/contracts/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteContract(@PathVariable("id") UUID id) {
        maintenanceService.deleteContract(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa hợp đồng thành công"));
    }

    @PostMapping("/contracts/{id}/renew")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ContractResponse>> renewContract(
            @PathVariable("id") UUID id,
            @Valid @RequestBody RenewContractRequest request,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.ok(ApiResponse.success(
                maintenanceService.renewContract(id, request, currentUser),
                "Gia hạn hợp đồng thành công"));
    }

    // ── Tasks ──

    @GetMapping("/contracts/{id}/tasks")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public ResponseEntity<ApiResponse<List<MaintenanceTaskResponse>>> findTasks(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(ApiResponse.success(maintenanceService.findTasksByContract(id)));
    }

    @GetMapping("/tasks/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public ResponseEntity<ApiResponse<MaintenanceTaskResponse>> findTaskById(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(ApiResponse.success(maintenanceService.findTaskById(id)));
    }

    @GetMapping("/tasks")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public ResponseEntity<ApiResponse<List<MaintenanceTaskResponse>>> findAllTasks(
            @RequestParam(required = false, name = "status") ScheduleStatus status,
            @RequestParam(required = false, name = "assignedTo") UUID assignedTo,
            @RequestParam(required = false,name = "contractId") UUID contractId,
            @RequestParam(required = false,name = "customerId") UUID customerId,
            @RequestParam(required = false,name = "from") String from,
            @RequestParam(required = false,name = "to") String to,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.ok(ApiResponse.success(
                maintenanceService.findAllTasks(
                        status, assignedTo, contractId, customerId, from, to, currentUser
                )));
    }

    @PatchMapping("/tasks/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<MaintenanceTaskResponse>> updateTask(
            @PathVariable("id") UUID id,
            @RequestBody UpdateTaskRequest request,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.ok(ApiResponse.success(
                maintenanceService.updateTask(id, request, currentUser),
                "Cập nhật tác vụ thành công"));
    }

    @PatchMapping("/tasks/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public ResponseEntity<ApiResponse<MaintenanceTaskResponse>> completeTask(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.ok(ApiResponse.success(
                maintenanceService.completeTask(id, currentUser),
                "Hoàn thành tác vụ bảo trì"));
    }

    // ── Comments ──

    @GetMapping("/tasks/{taskId}/comments")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> findComments(@PathVariable("taskId") UUID taskId) {
        return ResponseEntity.ok(ApiResponse.success(maintenanceService.findCommentsByTask(taskId)));
    }

    @PostMapping("/tasks/{taskId}/comments")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public ResponseEntity<ApiResponse<CommentResponse>> addComment(
            @PathVariable("taskId") UUID taskId,
            @RequestParam(name = "content") String content,
            @RequestParam(name = "parentId", required = false) UUID parentId,
            @RequestParam(name = "files", required = false) List<MultipartFile> files,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        maintenanceService.addComment(taskId, content, parentId, files, currentUser),
                        "Thêm bình luận thành công"));
    }

    // ── Stats ──

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<MaintenanceStatsResponse>> getStats() {
        return ResponseEntity.ok(ApiResponse.success(maintenanceService.getStats()));
    }

    // ── Templates ──
    @GetMapping("/templates")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<List<TemplateResponse>>> findAllTemplates() {
        return ResponseEntity.ok(ApiResponse.success(maintenanceService.findAllTemplates()));
    }

    @GetMapping("/templates/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<TemplateResponse>> findTemplateById(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(ApiResponse.success(maintenanceService.findTemplateById(id)));
    }

    @PostMapping("/templates")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<TemplateResponse>> createTemplate(
            @Valid @RequestBody CreateTemplateRequest request,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(maintenanceService.createTemplate(request, currentUser),
                        "Tạo khuôn mẫu thành công"));
    }

    @PutMapping("/templates/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<TemplateResponse>> updateTemplate(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdateTemplateRequest request,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.ok(ApiResponse.success(
                maintenanceService.updateTemplate(id, request, currentUser),
                "Cập nhật khuôn mẫu thành công"));
    }

    @DeleteMapping("/templates/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteTemplate(@PathVariable("id") UUID id) {
        maintenanceService.deleteTemplate(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa khuôn mẫu thành công"));
    }
    

    // ── Tạo HĐ từ template ──

    @PostMapping("/contracts/from-template")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<ContractResponse>> createContractFromTemplate(
            @Valid @RequestBody CreateContractFromTemplateRequest request,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        maintenanceService.createContractFromTemplate(request, currentUser),
                        "Tạo hợp đồng từ khuôn mẫu thành công"));
    }
}