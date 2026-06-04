package com.hungphu.crm.features.consultation;

import com.hungphu.crm.features.consultation.dto.*;
import com.hungphu.crm.shared.enums.ConsultationStatus;
import com.hungphu.crm.shared.enums.PriorityLevel;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/consultations")
@RequiredArgsConstructor
public class ConsultationController {

    private final ConsultationService consultationService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public ResponseEntity<ApiResponse<List<ConsultationResponse>>> findAll(
            @RequestParam(name = "status",       required = false) ConsultationStatus status,
            @RequestParam(name = "assignedToId", required = false) UUID assignedToId,
            @RequestParam(name = "assignedById", required = false) UUID assignedById,
            @RequestParam(name = "priority",     required = false) PriorityLevel priority,
            @RequestParam(name = "page",  defaultValue = "1")  int page,
            @RequestParam(name = "limit", defaultValue = "10") int limit,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        var pageable = PageRequest.of(page - 1, limit, Sort.by("createdAt").descending());
        return ResponseEntity.ok(
                consultationService.findAll(status, assignedToId, assignedById, priority, pageable, currentUser));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<ConsultationStatsResponse>> getStats(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.ok(ApiResponse.success(consultationService.getStats(currentUser)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public ResponseEntity<ApiResponse<ConsultationResponse>> findById(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(ApiResponse.success(consultationService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<ConsultationResponse>> create(
            @Valid @RequestBody CreateConsultationRequest request,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        consultationService.create(request, currentUser),
                        "Tạo số tư vấn thành công"));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<ConsultationResponse>> update(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdateConsultationRequest request,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.ok(ApiResponse.success(
                consultationService.update(id, request, currentUser),
                "Cập nhật tư vấn thành công"));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public ResponseEntity<ApiResponse<ConsultationResponse>> updateStatus(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdateConsultationStatusRequest request,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.ok(ApiResponse.success(
                consultationService.updateStatus(id, request, currentUser),
                "Cập nhật trạng thái thành công"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("id") UUID id) {
        consultationService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa tư vấn thành công"));
    }
}