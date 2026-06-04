package com.hungphu.crm.features.user.mapper;

import com.hungphu.crm.features.user.dto.UserResponse;
import com.hungphu.crm.features.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        if (user == null) return null;

        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .address(user.getAddress())
                .dob(user.getDob())
                .avatar(user.getAvatar())
                .role(user.getRole())
                .joinDate(user.getCreatedAt())
                .isActive(user.isActive())
                .manager(toManagerInfo(user.getManager()))
                .build();
    }

    private UserResponse.ManagerInfo toManagerInfo(User manager) {
        if (manager == null) return null;
        return UserResponse.ManagerInfo.builder()
                .id(manager.getId())
                .fullName(manager.getFullName())
                .build();
    }
}