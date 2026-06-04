-- =============================================================================
-- HPCRM - Schema hoàn chỉnh v2.0
-- Flyway migration: V1__init_schema.sql
--
-- Thay đổi chính so với bản cũ:
--   1. projects: construction_status → project_status (enum mới)
--   2. tasks: task_type enum mới (GIAM_SAT_XAY_DUNG, THI_CONG, BAN_GIAO, BAO_TRI)
--   3. consultations: thêm DA_CHUYEN_DU_AN vào enum status
--   4. users: tích hợp phone, address, dob, avatar, manager_id
--   5. customers: tích hợp assigned_user_id
--   6. Thêm bảng task_members, password_reset_tokens
-- =============================================================================

-- ─────────────────────────────────────────────────────────────────────────────
-- 1. USERS
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE users (
    id            BINARY(16)   NOT NULL,
    full_name     VARCHAR(150) NOT NULL,
    email         VARCHAR(255) NOT NULL,
    phone         VARCHAR(20),
    address       TEXT,
    dob           DATE,
    avatar        TEXT,
    password_hash TEXT         NOT NULL,
    role          ENUM('ADMIN','MANAGER','EMPLOYEE') NOT NULL DEFAULT 'EMPLOYEE',
    is_active     TINYINT(1)   NOT NULL DEFAULT 1,
    manager_id    BINARY(16),
    created_by    BINARY(16),
    created_at    DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at    DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uq_users_email (email),
    CONSTRAINT fk_users_manager    FOREIGN KEY (manager_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_users_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────────────────────────────────────
-- 2. PASSWORD RESET TOKENS
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE password_reset_tokens (
    id         BINARY(16)   NOT NULL,
    user_id    BINARY(16)   NOT NULL,
    token_hash VARCHAR(64)  NOT NULL COMMENT 'SHA-256 hex of raw token',
    expires_at DATETIME(6)  NOT NULL,
    used       TINYINT(1)   NOT NULL DEFAULT 0,
    created_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uq_prt_token_hash (token_hash),
    CONSTRAINT fk_prt_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────────────────────────────────────
-- 3. CUSTOMERS
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE customers (
    id               BINARY(16)   NOT NULL,
    full_name        VARCHAR(150) NOT NULL,
    phone            VARCHAR(20)  NOT NULL,
    address          TEXT,
    elevator_type    ENUM('GIA_DINH','KINH','HOMELIFT'),
    project_type     ENUM('CAI_TAO','XAY_MOI'),
    assigned_user_id BINARY(16),
    created_by       BINARY(16),
    created_at       DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at       DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_customers_assigned_user FOREIGN KEY (assigned_user_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_customers_created_by    FOREIGN KEY (created_by)       REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────────────────────────────────────
-- 4. CONSULTATIONS
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE consultations (
    id               BINARY(16)   NOT NULL,
    customer_id      BINARY(16),
    customer_name    VARCHAR(150) NOT NULL,
    customer_phone   VARCHAR(20)  NOT NULL,
    site_address     TEXT,
    priority         ENUM('CAO','TRUNG_BINH','THAP') NOT NULL DEFAULT 'TRUNG_BINH',
    price            DECIMAL(18,0),
    notes            TEXT,
    status           ENUM(
                         'CHO_TIEP_NHAN',
                         'DA_TIEP_NHAN',
                         'DA_LIEN_LAC',
                         'CHUA_LIEN_LAC_DUOC',
                         'DANG_BAO_GIA',
                         'THANH_CONG',
                         'THAT_BAI',
                         'DA_CHUYEN_DU_AN'
                     ) NOT NULL DEFAULT 'CHO_TIEP_NHAN',
    failure_reason   TEXT,
    assigned_by      BINARY(16),
    assigned_to      BINARY(16),
    accepted_at      DATETIME(6),
    created_at       DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at       DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_consult_customer    FOREIGN KEY (customer_id)  REFERENCES customers(id) ON DELETE SET NULL,
    CONSTRAINT fk_consult_assigned_by FOREIGN KEY (assigned_by)  REFERENCES users(id)     ON DELETE SET NULL,
    CONSTRAINT fk_consult_assigned_to FOREIGN KEY (assigned_to)  REFERENCES users(id)     ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────────────────────────────────────
-- 5. PROJECTS
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE projects (
    id              BINARY(16)   NOT NULL,
    name            VARCHAR(255) NOT NULL,
    customer_id     BINARY(16)   NOT NULL,
    consultation_id BINARY(16),
    elevator_type   ENUM('GIA_DINH','KINH','HOMELIFT') NOT NULL,
    project_type    ENUM('CAI_TAO','XAY_MOI') NOT NULL,
    project_status  ENUM(
                        'GIAM_SAT_XAY_DUNG',
                        'THI_CONG',
                        'BAN_GIAO',
                        'BAO_TRI',
                        'HET_HAN'
                    ) NOT NULL DEFAULT 'GIAM_SAT_XAY_DUNG',
    supervisor_id   BINARY(16),
    created_by      BINARY(16),
    created_at      DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uq_projects_consultation (consultation_id),
    CONSTRAINT fk_projects_customer     FOREIGN KEY (customer_id)     REFERENCES customers(id)     ON DELETE RESTRICT,
    CONSTRAINT fk_projects_consultation FOREIGN KEY (consultation_id) REFERENCES consultations(id) ON DELETE SET NULL,
    CONSTRAINT fk_projects_supervisor   FOREIGN KEY (supervisor_id)   REFERENCES users(id)         ON DELETE SET NULL,
    CONSTRAINT fk_projects_created_by   FOREIGN KEY (created_by)      REFERENCES users(id)         ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────────────────────────────────────
-- 6. PAYMENT INSTALLMENTS
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE payment_installments (
    id              BINARY(16)     NOT NULL,
    project_id      BINARY(16)     NOT NULL,
    installment_no  SMALLINT       NOT NULL,
    amount          DECIMAL(18,0)  NOT NULL,
    payment_date    DATE,
    invoice_pdf_url TEXT,
    notes           TEXT,
    created_by      BINARY(16),
    created_at      DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uq_payment_project_no (project_id, installment_no),
    CONSTRAINT fk_payment_project    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_payment_created_by FOREIGN KEY (created_by) REFERENCES users(id)    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────────────────────────────────────
-- 7. PROJECT DOCUMENTS
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE project_documents (
    id          BINARY(16)  NOT NULL,
    project_id  BINARY(16)  NOT NULL,
    label       VARCHAR(255),
    file_url    TEXT        NOT NULL,
    file_type   ENUM('IMAGE','PDF') NOT NULL,
    uploaded_by BINARY(16),
    uploaded_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_doc_project     FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_doc_uploaded_by FOREIGN KEY (uploaded_by) REFERENCES users(id)   ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────────────────────────────────────
-- 8. TASKS
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE tasks (
    id               BINARY(16)   NOT NULL,
    project_id       BINARY(16)   NOT NULL,
    title            VARCHAR(255) NOT NULL,
    site_address     TEXT,
    deadline         DATE,
    task_type        ENUM(
                         'GIAM_SAT_XAY_DUNG',
                         'THI_CONG',
                         'BAN_GIAO',
                         'BAO_TRI'
                     ) NOT NULL,
    status           ENUM(
                         'CHUA_THUC_HIEN',
                         'DANG_THUC_HIEN',
                         'CHO_DANH_GIA',
                         'HOAN_THANH',
                         'TU_CHOI'
                     ) NOT NULL DEFAULT 'CHUA_THUC_HIEN',
    rejection_reason TEXT,
    assigned_by      BINARY(16),
    assigned_to      BINARY(16)   NOT NULL,
    supervisor_id    BINARY(16),
    completed_at     DATETIME(6),
    created_at       DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at       DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_task_project     FOREIGN KEY (project_id)    REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_task_assigned_by FOREIGN KEY (assigned_by)   REFERENCES users(id)    ON DELETE SET NULL,
    CONSTRAINT fk_task_assigned_to FOREIGN KEY (assigned_to)   REFERENCES users(id)    ON DELETE RESTRICT,
    CONSTRAINT fk_task_supervisor  FOREIGN KEY (supervisor_id) REFERENCES users(id)    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────────────────────────────────────
-- 9. TASK MEMBERS
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE task_members (
    id          BINARY(16)  NOT NULL,
    task_id     BINARY(16)  NOT NULL,
    user_id     BINARY(16)  NOT NULL,
    member_role ENUM('LEAD','MEMBER') NOT NULL,
    created_at  DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uq_task_members_task_user (task_id, user_id),
    CONSTRAINT fk_task_members_task FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    CONSTRAINT fk_task_members_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────────────────────────────────────
-- 10. TASK EVIDENCES
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE task_evidences (
    id          BINARY(16)  NOT NULL,
    task_id     BINARY(16)  NOT NULL,
    file_url    TEXT        NOT NULL,
    file_type   ENUM('IMAGE','PDF') NOT NULL,
    uploaded_by BINARY(16),
    uploaded_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_evidence_task        FOREIGN KEY (task_id)     REFERENCES tasks(id) ON DELETE CASCADE,
    CONSTRAINT fk_evidence_uploaded_by FOREIGN KEY (uploaded_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────────────────────────────────────
-- 11. MAINTENANCE CONTRACTS
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE maintenance_contracts (
    id          BINARY(16)  NOT NULL,
    project_id  BINARY(16)  NOT NULL,
    customer_id BINARY(16)  NOT NULL,
    start_date  DATE        NOT NULL,
    end_date    DATE        NOT NULL,
    status      ENUM('MOI','SAP_HET_HAN','HET_HAN') NOT NULL DEFAULT 'MOI',
    assigned_to BINARY(16),
    created_by  BINARY(16),
    created_at  DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at  DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT chk_contract_dates CHECK (end_date > start_date),
    CONSTRAINT fk_mc_project     FOREIGN KEY (project_id)  REFERENCES projects(id)  ON DELETE RESTRICT,
    CONSTRAINT fk_mc_customer    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE RESTRICT,
    CONSTRAINT fk_mc_assigned_to FOREIGN KEY (assigned_to) REFERENCES users(id)     ON DELETE SET NULL,
    CONSTRAINT fk_mc_created_by  FOREIGN KEY (created_by)  REFERENCES users(id)     ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────────────────────────────────────
-- 12. MAINTENANCE SCHEDULES
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE maintenance_schedules (
    id             BINARY(16)  NOT NULL,
    contract_id    BINARY(16)  NOT NULL,
    scheduled_date DATE        NOT NULL,
    status         ENUM('CHO_THUC_HIEN','HOAN_THANH') NOT NULL DEFAULT 'CHO_THUC_HIEN',
    assigned_to    BINARY(16),
    completed_at   DATETIME(6),
    created_at     DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at     DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_ms_contract    FOREIGN KEY (contract_id) REFERENCES maintenance_contracts(id) ON DELETE CASCADE,
    CONSTRAINT fk_ms_assigned_to FOREIGN KEY (assigned_to) REFERENCES users(id)                 ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────────────────────────────────────
-- 13. MAINTENANCE EVIDENCES
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE maintenance_evidences (
    id          BINARY(16)  NOT NULL,
    schedule_id BINARY(16)  NOT NULL,
    file_url    TEXT        NOT NULL,
    file_type   ENUM('IMAGE','PDF') NOT NULL,
    uploaded_by BINARY(16),
    uploaded_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_me_schedule    FOREIGN KEY (schedule_id) REFERENCES maintenance_schedules(id) ON DELETE CASCADE,
    CONSTRAINT fk_me_uploaded_by FOREIGN KEY (uploaded_by) REFERENCES users(id)                 ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────────────────────────────────────
-- 14. NOTIFICATIONS
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE notifications (
    id           BINARY(16)   NOT NULL,
    user_id      BINARY(16)   NOT NULL,
    type         ENUM(
                     'MAINTENANCE_REMINDER',
                     'TASK_ASSIGNED',
                     'TASK_COMPLETED',
                     'TASK_REJECTED',
                     'CONSULTATION_ASSIGNED'
                 ) NOT NULL,
    title        VARCHAR(255) NOT NULL,
    body         TEXT,
    ref_type     VARCHAR(50),
    ref_id       BINARY(16),
    is_read      TINYINT(1)   NOT NULL DEFAULT 0,
    scheduled_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    sent_at      DATETIME(6),
    created_at   DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────────────────────────────────────
-- 15. INDEXES
-- ─────────────────────────────────────────────────────────────────────────────

-- Users
CREATE INDEX idx_users_role         ON users(role);
CREATE INDEX idx_users_manager      ON users(manager_id);
CREATE INDEX idx_users_active       ON users(is_active);

-- Password Reset Tokens
CREATE INDEX idx_prt_user_id        ON password_reset_tokens(user_id);
CREATE INDEX idx_prt_expires_at     ON password_reset_tokens(expires_at);

-- Customers
CREATE INDEX idx_customers_assigned ON customers(assigned_user_id);
CREATE INDEX idx_customers_phone    ON customers(phone);

-- Consultations
CREATE INDEX idx_consult_status     ON consultations(status);
CREATE INDEX idx_consult_assigned   ON consultations(assigned_to);
CREATE INDEX idx_consult_customer   ON consultations(customer_id);
CREATE INDEX idx_consult_priority   ON consultations(priority);

-- Projects
CREATE INDEX idx_project_customer   ON projects(customer_id);
CREATE INDEX idx_project_status     ON projects(project_status);
CREATE INDEX idx_project_supervisor ON projects(supervisor_id);
CREATE INDEX idx_project_created_by ON projects(created_by);

-- Tasks
CREATE INDEX idx_task_project       ON tasks(project_id);
CREATE INDEX idx_task_assigned_to   ON tasks(assigned_to);
CREATE INDEX idx_task_assigned_by   ON tasks(assigned_by);
CREATE INDEX idx_task_status        ON tasks(status);
CREATE INDEX idx_task_type          ON tasks(task_type);
CREATE INDEX idx_task_deadline      ON tasks(deadline);

-- Task Members
CREATE INDEX idx_tm_task            ON task_members(task_id);
CREATE INDEX idx_tm_user            ON task_members(user_id);

-- Maintenance Contracts
CREATE INDEX idx_mc_project         ON maintenance_contracts(project_id);
CREATE INDEX idx_mc_customer        ON maintenance_contracts(customer_id);
CREATE INDEX idx_mc_status          ON maintenance_contracts(status);
CREATE INDEX idx_mc_end_date        ON maintenance_contracts(end_date);

-- Maintenance Schedules
CREATE INDEX idx_ms_contract        ON maintenance_schedules(contract_id);
CREATE INDEX idx_ms_sched_date      ON maintenance_schedules(scheduled_date);
CREATE INDEX idx_ms_status          ON maintenance_schedules(status);

-- Notifications
CREATE INDEX idx_notif_user         ON notifications(user_id);
CREATE INDEX idx_notif_scheduled    ON notifications(scheduled_at);
CREATE INDEX idx_notif_unread       ON notifications(user_id, is_read);
CREATE INDEX idx_notif_type         ON notifications(type);