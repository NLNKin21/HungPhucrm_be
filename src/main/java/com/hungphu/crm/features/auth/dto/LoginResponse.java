package com.hungphu.crm.features.auth.dto;

import com.hungphu.crm.shared.enums.UserRole;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private UserInfo user;

    @Getter
    @Builder
    public static class UserInfo {
        private UUID id;
        private String fullName;
        private String email;
        private UserRole role;
    }
}
