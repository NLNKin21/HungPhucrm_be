package com.hungphu.crm.features.maintenance.entity;

import com.hungphu.crm.features.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "maintenance_templates")
@Getter
@Setter
@NoArgsConstructor
public class MaintenanceTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "cycle_months", nullable = false)
    private Integer cycleMonths = 2;

    @Column(name = "duration_months", nullable = false)
    private Integer durationMonths = 12;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_assigned_to")
    private User defaultAssignedTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_watcher_id")
    private User defaultWatcher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}