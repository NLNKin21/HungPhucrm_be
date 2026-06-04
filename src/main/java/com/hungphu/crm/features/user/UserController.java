package com.hungphu.crm.features.user;

import com.hungphu.crm.features.user.dto.*;
import com.hungphu.crm.shared.response.ApiResponse;
import com.hungphu.crm.shared.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> findAll() {
        return ResponseEntity.ok(ApiResponse.success(userService.findAll()));
    }

    // ✅ Endpoint mới: lấy danh sách MANAGER active
    @GetMapping("/managers")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getManagers() {
        return ResponseEntity.ok(ApiResponse.success(userService.getManagers()));
    }

    @GetMapping("/my-employees")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> findMyEmployees(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.ok(ApiResponse.success(userService.findMyEmployees(currentUser)));
    }

    // ✅ Regex UUID để tránh /managers bị match nhầm vào {id}
    @GetMapping("/{id:[0-9a-fA-F\\-]{36}}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> findById(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(ApiResponse.success(userService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> create(
            @Valid @RequestBody CreateUserRequest request,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        UserResponse response = userService.create(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Tạo tài khoản thành công"));
    }

    @PatchMapping("/{id:[0-9a-fA-F\\-]{36}}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public ResponseEntity<ApiResponse<UserResponse>> update(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(ApiResponse.success(userService.update(id, request)));
    }

    @PatchMapping("/{id:[0-9a-fA-F\\-]{36}}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> updateRole(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdateUserRoleRequest request) {
        return ResponseEntity.ok(ApiResponse.success(userService.updateRole(id, request)));
    }

    @PatchMapping("/{id:[0-9a-fA-F\\-]{36}}/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> updateActive(
            @PathVariable("id") UUID id,
            @RequestParam("active") boolean active) {
        return ResponseEntity.ok(ApiResponse.success(userService.updateActive(id, active)));
    }

    @GetMapping("/assignable-employees")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAssignableEmployees(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.ok(ApiResponse.success(userService.getAssignableEmployees(currentUser)));
    }
}