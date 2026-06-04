package com.hungphu.crm.features.customer.mapper;

import com.hungphu.crm.features.customer.dto.AssignedUserInfo;
import com.hungphu.crm.features.customer.dto.CustomerResponse;
import com.hungphu.crm.features.customer.entity.Customer;
import com.hungphu.crm.features.user.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    CustomerResponse toResponse(Customer customer);

    AssignedUserInfo toAssignedUserInfo(User user);
}
