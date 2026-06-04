package com.hungphu.crm.features.user.dto;

import com.hungphu.crm.shared.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class CreateUserRequest {

    @NotBlank(message = "Họ tên không được trống")
    @Size(max = 150)
    private String fullName;

    @NotBlank(message = "Email không được trống")
    @Email(message = "Email không hợp lệ")
    @Size(max = 255)
    private String email;

    @Size(max = 20)
    private String phone;

    private String address;

    private LocalDate dob;

    @NotNull(message = "Role không được trống")
    private UserRole role;

    private UUID managerId;
}