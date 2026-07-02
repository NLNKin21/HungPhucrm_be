-- V8__replace_schedules_with_tasks.sql

-- Xóa bảng cũ
DROP TABLE IF EXISTS maintenance_evidences;
DROP TABLE IF EXISTS maintenance_schedules;

-- Tạo bảng MaintenanceTask
CREATE TABLE maintenance_tasks (
    id BINARY(16) NOT NULL,
    contract_id BINARY(16) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    contact_phone VARCHAR(20),
    scheduled_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'CHO_THUC_HIEN',
    created_by BINARY(16),
    assigned_to BINARY(16),
    watcher_id BINARY(16),
    completed_at DATETIME,
    completed_late BOOLEAN NOT NULL DEFAULT FALSE,
    days_late INT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_mt_contract FOREIGN KEY (contract_id) REFERENCES maintenance_contracts(id) ON DELETE CASCADE,
    CONSTRAINT fk_mt_created_by FOREIGN KEY (created_by) REFERENCES users(id),
    CONSTRAINT fk_mt_assigned_to FOREIGN KEY (assigned_to) REFERENCES users(id),
    CONSTRAINT fk_mt_watcher FOREIGN KEY (watcher_id) REFERENCES users(id)
);

-- Tạo bảng MaintenanceComment
CREATE TABLE maintenance_comments (
    id BINARY(16) NOT NULL,
    task_id BINARY(16) NOT NULL,
    parent_id BINARY(16),
    user_id BINARY(16) NOT NULL,
    content TEXT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_mc_task FOREIGN KEY (task_id) REFERENCES maintenance_tasks(id) ON DELETE CASCADE,
    CONSTRAINT fk_mc_parent FOREIGN KEY (parent_id) REFERENCES maintenance_comments(id) ON DELETE CASCADE,
    CONSTRAINT fk_mc_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Tạo bảng MaintenanceAttachment
CREATE TABLE maintenance_attachments (
    id BINARY(16) NOT NULL,
    comment_id BINARY(16) NOT NULL,
    file_url TEXT NOT NULL,
    file_type VARCHAR(10) NOT NULL,
    file_size BIGINT,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_ma_comment FOREIGN KEY (comment_id) REFERENCES maintenance_comments(id) ON DELETE CASCADE
);

-- Indexes
CREATE INDEX idx_mt_contract ON maintenance_tasks(contract_id);
CREATE INDEX idx_mt_status ON maintenance_tasks(status);
CREATE INDEX idx_mt_scheduled_date ON maintenance_tasks(scheduled_date);
CREATE INDEX idx_mt_assigned_to ON maintenance_tasks(assigned_to);
CREATE INDEX idx_mc_task ON maintenance_comments(task_id);
CREATE INDEX idx_mc_parent ON maintenance_comments(parent_id);