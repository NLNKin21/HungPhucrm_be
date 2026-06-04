package com.hungphu.crm.features.project.repository;

import com.hungphu.crm.features.project.entity.PaymentInstallment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PaymentInstallmentRepository extends JpaRepository<PaymentInstallment, UUID> {

    List<PaymentInstallment> findByProjectIdOrderByInstallmentNoAsc(UUID projectId);

    boolean existsByProjectIdAndInstallmentNo(UUID projectId, Short installmentNo);
}
