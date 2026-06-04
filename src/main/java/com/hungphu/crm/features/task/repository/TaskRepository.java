package com.hungphu.crm.features.task.repository;

import com.hungphu.crm.features.task.entity.Task;
import com.hungphu.crm.features.user.entity.User;
import com.hungphu.crm.shared.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    @Query("""
            SELECT DISTINCT t FROM Task t
            JOIN FETCH t.assignedTo
            LEFT JOIN FETCH t.supervisor
            LEFT JOIN FETCH t.assignedBy
            LEFT JOIN FETCH t.members tm
            LEFT JOIN FETCH tm.user
            WHERE t.project.id = :projectId
            ORDER BY t.createdAt DESC
            """)
    List<Task> findByProjectIdWithDetails(@Param("projectId") UUID projectId);

    @Query(value = """
            SELECT t FROM Task t
            JOIN FETCH t.assignedTo
            LEFT JOIN FETCH t.supervisor
            LEFT JOIN FETCH t.assignedBy
            WHERE (:status IS NULL OR t.status = :status)
              AND EXISTS (
                    SELECT tm.id FROM TaskMember tm
                    WHERE tm.task = t
                      AND tm.user.id = :userId
              )
            ORDER BY t.createdAt DESC
            """,
            countQuery = """
            SELECT COUNT(t) FROM Task t
            WHERE (:status IS NULL OR t.status = :status)
              AND EXISTS (
                    SELECT tm.id FROM TaskMember tm
                    WHERE tm.task = t
                      AND tm.user.id = :userId
              )
            """)
    Page<Task> findMyTasks(@Param("userId") UUID userId,
                           @Param("status") TaskStatus status,
                           Pageable pageable);

    @Query(value = """
            SELECT t FROM Task t
            JOIN FETCH t.assignedTo
            LEFT JOIN FETCH t.supervisor
            LEFT JOIN FETCH t.assignedBy
            WHERE (:status IS NULL OR t.status = :status)
              AND (:projectId IS NULL OR t.project.id = :projectId)
              AND (:assignedTo IS NULL OR t.assignedTo.id = :assignedTo)
            ORDER BY t.createdAt DESC
            """,
            countQuery = """
            SELECT COUNT(t) FROM Task t
            WHERE (:status IS NULL OR t.status = :status)
              AND (:projectId IS NULL OR t.project.id = :projectId)
              AND (:assignedTo IS NULL OR t.assignedTo.id = :assignedTo)
            """)
    Page<Task> findAllByFilters(@Param("status") TaskStatus status,
                                @Param("projectId") UUID projectId,
                                @Param("assignedTo") UUID assignedTo,
                                Pageable pageable);

    @Query(value = """
            SELECT t FROM Task t
            JOIN FETCH t.assignedTo
            LEFT JOIN FETCH t.supervisor
            LEFT JOIN FETCH t.assignedBy
            WHERE t.project.id = :projectId
              AND (:status IS NULL OR t.status = :status)
              AND (:deadlineFrom IS NULL OR t.deadline >= :deadlineFrom)
              AND (:deadlineTo IS NULL OR t.deadline <= :deadlineTo)
              AND (
                    :assigneeId IS NULL OR EXISTS (
                        SELECT tm.id FROM TaskMember tm
                        WHERE tm.task = t
                          AND tm.user.id = :assigneeId
                    )
              )
            ORDER BY t.createdAt DESC
            """,
            countQuery = """
            SELECT COUNT(t) FROM Task t
            WHERE t.project.id = :projectId
              AND (:status IS NULL OR t.status = :status)
              AND (:deadlineFrom IS NULL OR t.deadline >= :deadlineFrom)
              AND (:deadlineTo IS NULL OR t.deadline <= :deadlineTo)
              AND (
                    :assigneeId IS NULL OR EXISTS (
                        SELECT tm.id FROM TaskMember tm
                        WHERE tm.task = t
                          AND tm.user.id = :assigneeId
                    )
              )
            """)
    Page<Task> findByProjectWithFilters(@Param("projectId") UUID projectId,
                                        @Param("assigneeId") UUID assigneeId,
                                        @Param("status") TaskStatus status,
                                        @Param("deadlineFrom") LocalDate deadlineFrom,
                                        @Param("deadlineTo") LocalDate deadlineTo,
                                        Pageable pageable);

    @Query("""
            SELECT t.status, COUNT(t) FROM Task t
            WHERE t.project.id = :projectId
            GROUP BY t.status
            """)
    List<Object[]> countByStatusForProject(@Param("projectId") UUID projectId);

    @Query("""
            SELECT DISTINCT tm.user FROM TaskMember tm
            WHERE tm.task.project.id = :projectId
            """)
    List<User> findTeamMembersByProject(@Param("projectId") UUID projectId);

    // Tasks của nhân viên thuộc quyền quản lý của manager (bao gồm cả task của chính manager nếu được assign)
    @Query(value = """
            SELECT t FROM Task t
            JOIN FETCH t.assignedTo u
            LEFT JOIN FETCH t.supervisor
            LEFT JOIN FETCH t.assignedBy
            WHERE (:status IS NULL OR t.status = :status)
              AND EXISTS (
                    SELECT tm.id FROM TaskMember tm
                    WHERE tm.task = t
                      AND (
                            tm.user.manager.id = :managerId
                            OR tm.user.id = :managerId
                      )
              )
            ORDER BY t.createdAt DESC
            """,
            countQuery = """
            SELECT COUNT(t) FROM Task t
            WHERE (:status IS NULL OR t.status = :status)
              AND EXISTS (
                    SELECT tm.id FROM TaskMember tm
                    WHERE tm.task = t
                      AND (
                            tm.user.manager.id = :managerId
                            OR tm.user.id = :managerId
                      )
              )
            """)
    Page<Task> findMyEmployeesTasks(@Param("managerId") UUID managerId,
                                    @Param("status") TaskStatus status,
                                    Pageable pageable);
}