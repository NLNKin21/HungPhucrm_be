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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/maintenance")
@RequiredArgsConstructor
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

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
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public ResponseEntity<ApiResponse<ContractResponse>> createContract(
            @Valid @RequestBody CreateContractRequest request,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(maintenanceService.createContract(request, currentUser),
                        "Tạo hợp đồng bảo trì thành công"));
    }

    @GetMapping("/contracts/{id}/schedules")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public ResponseEntity<ApiResponse<List<ScheduleResponse>>> findSchedules(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(ApiResponse.success(maintenanceService.findSchedulesByContract(id)));
    }

    /**
     * Hoàn thành lịch bảo trì.
     * Frontend gửi multipart/form-data với:
     *   - files: List<MultipartFile> (1 hoặc nhiều file minh chứng, tuỳ chọn)
     *   - notes: String (tuỳ chọn)
     *
     * Backend chấp nhận files là optional — có thể hoàn thành không cần upload.
     * Nếu muốn bắt buộc upload thì bỏ required = false.
     */
    @PatchMapping("/schedules/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public ResponseEntity<ApiResponse<ScheduleResponse>> completeSchedule(
            @PathVariable("id") UUID id,
            @RequestParam(name = "files", required = false) List<MultipartFile> files,
            @RequestParam(name = "notes", required = false) String notes,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.ok(ApiResponse.success(
                maintenanceService.completeSchedule(id, files, notes, currentUser),
                "Hoàn thành lịch bảo trì"));
    }

    // Thêm endpoint thống kê

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<MaintenanceStatsResponse>> getStats() {
        return ResponseEntity.ok(ApiResponse.success(maintenanceService.getStats()));
    }

    @GetMapping("/schedules/overdue")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<List<ScheduleResponse>>> getOverdueSchedules() {
        return ResponseEntity.ok(ApiResponse.success(maintenanceService.getOverdueSchedules()));
    }

    @GetMapping("/schedules/upcoming")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public ResponseEntity<ApiResponse<List<ScheduleResponse>>> getUpcomingSchedules(
            @RequestParam(name = "days", defaultValue = "7") int days) {
        return ResponseEntity.ok(ApiResponse.success(maintenanceService.getUpcomingSchedules(days)));
    }

    @PostMapping("/contracts/{id}/regenerate-schedules")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ContractResponse>> regenerateSchedules(
            @PathVariable("id") UUID id) {
        return ResponseEntity.ok(ApiResponse.success(
                maintenanceService.regenerateSchedules(id),
                "Đã tạo lại lịch bảo trì"));
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
}