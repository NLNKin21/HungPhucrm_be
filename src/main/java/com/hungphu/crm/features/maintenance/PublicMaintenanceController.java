package com.hungphu.crm.features.maintenance;

import com.hungphu.crm.features.maintenance.dto.CustomerLookupResponse;
import com.hungphu.crm.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/public/maintenance")
@RequiredArgsConstructor
public class PublicMaintenanceController {

    private final MaintenanceService maintenanceService;

    /**
     * Public endpoint — không cần auth
     * Khách hàng tra cứu lịch sử bảo trì bằng số điện thoại
     */
    @GetMapping("/lookup")
    public ResponseEntity<ApiResponse<CustomerLookupResponse>> lookupByPhone(
            @RequestParam String phone) {
        return ResponseEntity.ok(ApiResponse.success(
                maintenanceService.lookupByPhone(phone)));
    }
}