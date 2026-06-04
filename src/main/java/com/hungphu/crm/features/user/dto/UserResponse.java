package com.hungphu.crm.features.user.dto;

import com.hungphu.crm.shared.enums.UserRole;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class UserResponse {
    private UUID id;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private LocalDate dob;
    private String avatar;
    private UserRole role;
    private LocalDateTime joinDate;
    private Boolean isActive;

    private ManagerInfo manager;

    @Getter
    @Builder
    public static class ManagerInfo {
        private UUID id;
        private String fullName;
    }
}
