package com.hungphu.crm.features.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @NotBlank(message = "Email không được trống")
    @Email
    private String email;

    @NotBlank(message = "Mật khẩu không được trống")
    private String password;
}
