package com.hungphu.crm.features.user.dto;

import com.hungphu.crm.shared.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class UpdateUserRequest {

    @Size(max = 150)
    private String fullName;

    @Email
    @Size(max = 150)
    private String email;

    @Pattern(regexp = "^[0-9]{9,11}$", message = "Số điện thoại không hợp lệ")
    private String phone;

    @Size(max = 255)
    private String address;

    private LocalDate dob;

    @Size(max = 500)
    private String avatar;

    private UserRole role;

    private UUID managerId;

    private boolean clearManager;
}