-- V10__add_maintenance_workflow_phase_1A.sql

-- =========================================================
-- 1) maintenance_contracts
-- =========================================================
ALTER TABLE maintenance_contracts
    ADD COLUMN supervisor_id BINARY(16) NULL,
    ADD COLUMN first_maintenance_immediate BIT(1) NOT NULL DEFAULT b'1';

ALTER TABLE maintenance_contracts
    ADD CONSTRAINT fk_maintenance_contracts_supervisor
        FOREIGN KEY (supervisor_id) REFERENCES users(id) ON DELETE SET NULL;

-- =========================================================
-- 2) maintenance_tasks
-- =========================================================
ALTER TABLE maintenance_tasks
    ADD COLUMN supervisor_id BINARY(16) NULL,
    ADD COLUMN submitted_at DATETIME(6) NULL;

ALTER TABLE maintenance_tasks
    ADD CONSTRAINT fk_maintenance_tasks_supervisor
        FOREIGN KEY (supervisor_id) REFERENCES users(id) ON DELETE SET NULL;

-- Update enum status cho workflow mới
ALTER TABLE maintenance_tasks
    MODIFY COLUMN status ENUM(
        'CHO_THUC_HIEN',
        'QUA_HAN',
        'CHO_DUYET',
        'CAN_BO_SUNG',
        'HOAN_THANH'
    ) NOT NULL DEFAULT 'CHO_THUC_HIEN';

-- =========================================================
-- 3) file_type enum đang dùng chung
--    Nếu đã thêm VIDEO vào Java enum thì DB cũng phải có VIDEO
-- =========================================================
ALTER TABLE maintenance_attachments
    MODIFY COLUMN file_type ENUM('IMAGE', 'PDF', 'VIDEO') NOT NULL;

ALTER TABLE task_evidences
    MODIFY COLUMN file_type ENUM('IMAGE', 'PDF', 'VIDEO') NOT NULL;

-- =========================================================
-- 4) maintenance_evidences
-- =========================================================
CREATE TABLE maintenance_evidences (
    id BINARY(16) NOT NULL,
    task_id BINARY(16) NOT NULL,
    uploaded_by BINARY(16) NULL,
    file_url TEXT NOT NULL,
    file_type ENUM('IMAGE', 'PDF', 'VIDEO') NOT NULL,
    file_size BIGINT NULL,
    description TEXT NULL,
    uploaded_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_maintenance_evidences_task
        FOREIGN KEY (task_id) REFERENCES maintenance_tasks(id) ON DELETE CASCADE,
    CONSTRAINT fk_maintenance_evidences_uploaded_by
        FOREIGN KEY (uploaded_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- 5) maintenance_approvals
-- =========================================================
CREATE TABLE maintenance_approvals (
    id BINARY(16) NOT NULL,
    task_id BINARY(16) NOT NULL,
    approved_by BINARY(16) NULL,
    action ENUM('APPROVED', 'REJECTED', 'REQUEST_MORE_EVIDENCE') NOT NULL,
    reason TEXT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_maintenance_approvals_task
        FOREIGN KEY (task_id) REFERENCES maintenance_tasks(id) ON DELETE CASCADE,
    CONSTRAINT fk_maintenance_approvals_approved_by
        FOREIGN KEY (approved_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;