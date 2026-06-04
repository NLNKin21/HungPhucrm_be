package com.hungphu.crm.features.user;

import com.hungphu.crm.features.user.dto.*;
import com.hungphu.crm.shared.security.UserDetailsImpl;

import java.util.List;
import java.util.UUID;

public interface UserService {

    List<UserResponse> findAll();

    // ✅ thêm
    List<UserResponse> getManagers();

    UserResponse findById(UUID id);

    List<UserResponse> findMyEmployees(UserDetailsImpl currentUser);

    List<UserResponse> getAssignableEmployees(UserDetailsImpl currentUser);

    UserResponse create(CreateUserRequest request, UserDetailsImpl currentUser);

    UserResponse update(UUID id, UpdateUserRequest request);

    UserResponse updateRole(UUID id, UpdateUserRoleRequest request);

    UserResponse updateActive(UUID id, boolean active);
}