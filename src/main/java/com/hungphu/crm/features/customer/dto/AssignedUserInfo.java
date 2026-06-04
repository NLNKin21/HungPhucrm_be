package com.hungphu.crm.features.customer.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class AssignedUserInfo {
    private UUID id;
    private String fullName;
}
