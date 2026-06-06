package com.hungphu.crm.features.consultation.entity;

import com.hungphu.crm.features.customer.entity.Customer;
import com.hungphu.crm.features.user.entity.User;
import com.hungphu.crm.shared.enums.ConsultationStatus;
import com.hungphu.crm.shared.enums.ElevatorType;
import com.hungphu.crm.shared.enums.ProjectType;
import com.hungphu.crm.shared.enums.PriorityLevel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "consultations")
@Getter
@Setter
@NoArgsConstructor
public class Consultation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(name = "customer_name", nullable = false, length = 150)
    private String customerName;

    @Column(name = "customer_phone", nullable = false, length = 20)
    private String customerPhone;

    @Column(name = "site_address", columnDefinition = "TEXT")
    private String siteAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PriorityLevel priority = PriorityLevel.TRUNG_BINH;

    @Column(precision = 18, scale = 0)
    private BigDecimal price;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ConsultationStatus status = ConsultationStatus.DA_TIEP_NHAN;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    // ── Thêm mới: loại thang máy & loại dự án ──────────────────────────────
    // Được điền khi employee chuyển sang DANG_BAO_GIA.
    // Nullable vì các tư vấn cũ hoặc chưa đến bước báo giá chưa có giá trị.

    @Enumerated(EnumType.STRING)
    @Column(name = "elevator_type", length = 20)
    private ElevatorType elevatorType;

    @Enumerated(EnumType.STRING)
    @Column(name = "project_type", length = 20)
    private ProjectType projectType;

    // ───────────────────────────────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by")
    private User assignedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

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