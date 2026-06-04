package com.hungphu.crm.features.task;

import com.hungphu.crm.features.task.dto.*;
import com.hungphu.crm.shared.enums.TaskStatus;
import com.hungphu.crm.shared.response.ApiResponse;
import com.hungphu.crm.shared.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping("/tasks/me")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> findMyTasks(
            @RequestParam(name = "status", required = false) TaskStatus status,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "limit", defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        var pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(taskService.findMyTasks(status, pageable, currentUser));
    }

    // PHẢI đặt trước /tasks/{id} để tránh Spring match "my-employees" như UUID
    @GetMapping("/tasks/my-employees")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> findMyEmployeesTasks(
            @RequestParam(name = "status", required = false) TaskStatus status,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "limit", defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        var pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(taskService.findMyEmployeesTasks(status, pageable, currentUser));
    }

    @GetMapping("/tasks")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> findAll(
            @RequestParam(name = "status", required = false) TaskStatus status,
            @RequestParam(name = "projectId", required = false) UUID projectId,
            @RequestParam(name = "assignedTo", required = false) UUID assignedTo,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "limit", defaultValue = "10") int size) {
        var pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(taskService.findAll(status, projectId, assignedTo, pageable));
    }

    @GetMapping("/projects/{projectId}/tasks")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> findByProject(
            @PathVariable("projectId") UUID projectId,
            @RequestParam(name = "assigneeId", required = false) UUID assigneeId,
            @RequestParam(name = "status", required = false) TaskStatus status,
            @RequestParam(name = "deadlineFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deadlineFrom,
            @RequestParam(name = "deadlineTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deadlineTo,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "limit", defaultValue = "50") int size) {
        if (assigneeId == null && status == null && deadlineFrom == null && deadlineTo == null) {
            return ResponseEntity.ok(ApiResponse.success(taskService.findByProject(projectId)));
        }
        var pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(taskService.findByProjectWithFilters(
                projectId, assigneeId, status, deadlineFrom, deadlineTo, pageable));
    }

    @PostMapping("/projects/{projectId}/tasks")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<TaskResponse>> create(
            @PathVariable("projectId") UUID projectId,
            @Valid @RequestBody CreateTaskRequest request,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(taskService.create(projectId, request, currentUser), "Giao công việc thành công"));
    }

    @GetMapping("/tasks/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public ResponseEntity<ApiResponse<TaskResponse>> findById(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.ok(ApiResponse.success(taskService.findById(id, currentUser)));
    }

    @PatchMapping("/tasks/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdateTaskRequest request,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.ok(ApiResponse.success(
                taskService.updateTask(id, request, currentUser), "Cập nhật công việc thành công"));
    }

    @PatchMapping("/tasks/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public ResponseEntity<ApiResponse<TaskResponse>> updateStatus(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdateTaskStatusRequest request,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.ok(ApiResponse.success(
                taskService.updateStatus(id, request, currentUser), "Cập nhật trạng thái thành công"));
    }

    @PostMapping("/tasks/{id}/evidences")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<Void>> addEvidence(
            @PathVariable("id") UUID id,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        taskService.addEvidence(id, file, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(null, "Upload minh chứng thành công"));
    }

    @DeleteMapping("/tasks/{id}/evidences/{evidenceId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteEvidence(
            @PathVariable("id") UUID id,
            @PathVariable("evidenceId") UUID evidenceId) {
        taskService.deleteEvidence(id, evidenceId);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa minh chứng thành công"));
    }
}