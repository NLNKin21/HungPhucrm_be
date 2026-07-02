-- V9__create_maintenance_templates.sql

CREATE TABLE maintenance_templates (
    id BINARY(16) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    cycle_months INT NOT NULL DEFAULT 2,
    duration_months INT NOT NULL DEFAULT 12,
    default_assigned_to BINARY(16),
    default_watcher_id BINARY(16),
    created_by BINARY(16) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_tpl_assigned_to FOREIGN KEY (default_assigned_to) REFERENCES users(id),
    CONSTRAINT fk_tpl_watcher FOREIGN KEY (default_watcher_id) REFERENCES users(id),
    CONSTRAINT fk_tpl_created_by FOREIGN KEY (created_by) REFERENCES users(id)
);

-- Thêm template_id vào maintenance_contracts
ALTER TABLE maintenance_contracts
ADD COLUMN template_id BINARY(16) DEFAULT NULL AFTER cycle_months,
ADD CONSTRAINT fk_contract_template FOREIGN KEY (template_id) REFERENCES maintenance_templates(id);

-- Index
CREATE INDEX idx_tpl_active ON maintenance_templates(is_active);