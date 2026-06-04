package com.hungphu.crm.features.customer.repository;

import com.hungphu.crm.features.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    boolean existsByPhone(String phone);
}
