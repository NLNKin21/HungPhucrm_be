package com.hungphu.crm.features.user.dto;

import com.hungphu.crm.shared.enums.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRoleRequest {

    @NotNull(message = "Role không được trống")
    private UserRole role;
}
