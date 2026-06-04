package com.hungphu.crm.features.project.repository;

import com.hungphu.crm.features.project.entity.Project;
import com.hungphu.crm.shared.enums.ProjectStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    boolean existsByConsultationId(UUID consultationId);

    @Query(value = """
            SELECT p FROM Project p
            JOIN FETCH p.customer
            LEFT JOIN FETCH p.supervisor
            WHERE (:customerId IS NULL OR p.customer.id = :customerId)
            """,
            countQuery = """
            SELECT COUNT(p) FROM Project p
            WHERE (:customerId IS NULL OR p.customer.id = :customerId)
            """)
    Page<Project> findByFilters(@Param("customerId") UUID customerId, Pageable pageable);

    @Query(value = """
            SELECT DISTINCT p FROM Project p
            JOIN FETCH p.customer
            LEFT JOIN FETCH p.supervisor
            WHERE (:customerId IS NULL OR p.customer.id = :customerId)
              AND (
                    p.supervisor.id = :userId
                    OR EXISTS (
                        SELECT tm.id FROM TaskMember tm
                        WHERE tm.task.project = p
                          AND tm.user.id = :userId
                    )
              )
            """,
            countQuery = """
            SELECT COUNT(DISTINCT p.id) FROM Project p
            WHERE (:customerId IS NULL OR p.customer.id = :customerId)
              AND (
                    p.supervisor.id = :userId
                    OR EXISTS (
                        SELECT tm.id FROM TaskMember tm
                        WHERE tm.task.project = p
                          AND tm.user.id = :userId
                    )
              )
            """)
    Page<Project> findMyProjects(@Param("customerId") UUID customerId,
                                 @Param("userId") UUID userId,
                                 Pageable pageable);

    @Query(value = """
            SELECT DISTINCT p FROM Project p
            JOIN FETCH p.customer
            LEFT JOIN FETCH p.supervisor
            WHERE (:customerId IS NULL OR p.customer.id = :customerId)
              AND (
                    p.createdBy.id = :managerId
                    OR p.supervisor.id = :managerId
                    OR EXISTS (
                        SELECT tm.id FROM TaskMember tm
                        WHERE tm.task.project = p
                          AND tm.user.manager.id = :managerId
                    )
              )
            """,
            countQuery = """
            SELECT COUNT(DISTINCT p.id) FROM Project p
            WHERE (:customerId IS NULL OR p.customer.id = :customerId)
              AND (
                    p.createdBy.id = :managerId
                    OR p.supervisor.id = :managerId
                    OR EXISTS (
                        SELECT tm.id FROM TaskMember tm
                        WHERE tm.task.project = p
                          AND tm.user.manager.id = :managerId
                    )
              )
            """)
    Page<Project> findManagedProjects(@Param("customerId") UUID customerId,
                                      @Param("managerId") UUID managerId,
                                      Pageable pageable);

      /**
       * Tìm project đang BAO_TRI mà TẤT CẢ hợp đồng bảo trì đã hết hạn
       * và không có hợp đồng nào còn hiệu lực (MOI hoặc SAP_HET_HAN)
       */
      @Query("""
          SELECT p FROM Project p
          WHERE p.projectStatus = :status
            AND EXISTS (
                SELECT mc FROM MaintenanceContract mc
                WHERE mc.project = p
            )
            AND NOT EXISTS (
                SELECT mc FROM MaintenanceContract mc
                WHERE mc.project = p
                  AND mc.endDate >= :today
            )
          """)
      List<Project> findProjectsWithExpiredMaintenance(
              @Param("status") ProjectStatus status,
              @Param("today") LocalDate today
      );
}