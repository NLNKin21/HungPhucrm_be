package com.hungphu.crm.features.maintenance.repository;

import com.hungphu.crm.features.maintenance.entity.MaintenanceContract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MaintenanceContractRepository extends JpaRepository<MaintenanceContract, UUID> {

    /**
     * Fetch tất cả contract kèm schedules để mapper tính schedulesGenerated.
     * Dùng LEFT JOIN FETCH schedules tránh LazyInitializationException.
     *
     * Lưu ý: JOIN FETCH nhiều collection cùng lúc gây MultipleBagFetchException.
     * Giải pháp: fetch schedules trong 1 query, các relation khác fetch riêng
     * hoặc dùng @BatchSize. Ở đây chỉ fetch schedules là đủ.
     */
    @Query("""
        SELECT DISTINCT c FROM MaintenanceContract c
        LEFT JOIN FETCH c.project
        LEFT JOIN FETCH c.customer
        LEFT JOIN FETCH c.assignedTo
        LEFT JOIN FETCH c.schedules
        ORDER BY c.createdAt DESC
        """)
    List<MaintenanceContract> findAllWithDetails();

    /**
     * Dùng trong MaintenanceContractListener để đảm bảo idempotent —
     * không tạo 2 hợp đồng cho cùng 1 project.
     */
    boolean existsByProjectId(UUID projectId);

    /**
     * Tìm contract theo projectId — dùng khi cần load contract sau khi tạo tự động.
     */
    @Query("""
        SELECT c FROM MaintenanceContract c
        LEFT JOIN FETCH c.project
        LEFT JOIN FETCH c.customer
        LEFT JOIN FETCH c.assignedTo
        LEFT JOIN FETCH c.schedules
        WHERE c.project.id = :projectId
        """)
    List<MaintenanceContract> findByProjectId(@Param("projectId") UUID projectId);
}