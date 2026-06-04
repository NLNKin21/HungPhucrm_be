package com.hungphu.crm.features.customer;

import com.hungphu.crm.features.customer.dto.*;
import com.hungphu.crm.shared.security.UserDetailsImpl;

import java.util.List;
import java.util.UUID;

public interface CustomerService {

    List<CustomerResponse> findAll();

    CustomerResponse findById(UUID id);

    CustomerResponse create(CreateCustomerRequest request, UserDetailsImpl currentUser);

    CustomerResponse update(UUID id, UpdateCustomerRequest request, UserDetailsImpl currentUser);
}
