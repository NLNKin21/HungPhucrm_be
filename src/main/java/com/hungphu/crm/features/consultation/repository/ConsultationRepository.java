package com.hungphu.crm.features.consultation.repository;

import com.hungphu.crm.features.consultation.entity.Consultation;
import com.hungphu.crm.shared.enums.ConsultationStatus;
import com.hungphu.crm.shared.enums.PriorityLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConsultationRepository extends JpaRepository<Consultation, UUID> {

        @Query(value = """
                SELECT c FROM Consultation c
                LEFT JOIN FETCH c.assignedTo
                LEFT JOIN FETCH c.assignedBy
                WHERE c.status <> com.hungphu.crm.shared.enums.ConsultationStatus.DA_CHUYEN_DU_AN
                AND (:status IS NULL OR c.status = :status)
                AND (:assignedToId IS NULL OR c.assignedTo.id = :assignedToId)
                AND (:assignedById IS NULL OR c.assignedBy.id = :assignedById)
                AND (:priority IS NULL OR c.priority = :priority)
                ORDER BY c.createdAt DESC
                """,
                countQuery = """
                SELECT COUNT(c) FROM Consultation c
                WHERE c.status <> com.hungphu.crm.shared.enums.ConsultationStatus.DA_CHUYEN_DU_AN
                AND (:status IS NULL OR c.status = :status)
                AND (:assignedToId IS NULL OR c.assignedTo.id = :assignedToId)
                AND (:assignedById IS NULL OR c.assignedBy.id = :assignedById)
                AND (:priority IS NULL OR c.priority = :priority)
                """)
        Page<Consultation> findByFilters(@Param("status") ConsultationStatus status,
                                        @Param("assignedToId") UUID assignedToId,
                                        @Param("assignedById") UUID assignedById,
                                        @Param("priority") PriorityLevel priority,
                                        Pageable pageable);

        @Query("""
                SELECT c.status, COUNT(c) FROM Consultation c
                WHERE c.status <> com.hungphu.crm.shared.enums.ConsultationStatus.DA_CHUYEN_DU_AN
                AND (:assignedById IS NULL OR c.assignedBy.id = :assignedById)
                GROUP BY c.status
                """)
        List<Object[]> countByStatus(@Param("assignedById") UUID assignedById);

        @Query("""
                SELECT c.assignedTo,
                COUNT(c),
                SUM(CASE WHEN c.status = com.hungphu.crm.shared.enums.ConsultationStatus.THANH_CONG THEN 1L ELSE 0L END),
                SUM(CASE WHEN c.status NOT IN (
                        com.hungphu.crm.shared.enums.ConsultationStatus.THANH_CONG,
                        com.hungphu.crm.shared.enums.ConsultationStatus.THAT_BAI,
                        com.hungphu.crm.shared.enums.ConsultationStatus.DA_CHUYEN_DU_AN
                ) THEN 1L ELSE 0L END)
                FROM Consultation c
                WHERE c.assignedTo IS NOT NULL
                AND c.status <> com.hungphu.crm.shared.enums.ConsultationStatus.DA_CHUYEN_DU_AN
                AND (:assignedById IS NULL OR c.assignedBy.id = :assignedById)
                GROUP BY c.assignedTo
                """)
        List<Object[]> findEmployeeStats(@Param("assignedById") UUID assignedById);

        // Tìm consultation active của 1 customer (chưa kết thúc)
        Optional<Consultation> findFirstByCustomer_IdAndStatusNotInOrderByCreatedAtDesc(
                UUID customerId,
                Collection<ConsultationStatus> excludedStatuses
        );
        
}