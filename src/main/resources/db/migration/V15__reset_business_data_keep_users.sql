-- V15__reset_business_data_keep_users.sql
-- Reset business data, keep users + flyway history

SET FOREIGN_KEY_CHECKS = 0;

-- =========================================================
-- 1. Maintenance
-- =========================================================
DELETE FROM maintenance_approvals;
DELETE FROM maintenance_evidences;
DELETE FROM maintenance_attachments;
DELETE FROM maintenance_comments;
DELETE FROM maintenance_tasks;
DELETE FROM maintenance_contracts;
DELETE FROM maintenance_templates;

-- =========================================================
-- 2. Project Tasks
-- =========================================================
DELETE FROM task_evidences;
DELETE FROM task_members;
DELETE FROM tasks;

-- =========================================================
-- 3. Project / Consultation
-- =========================================================
DELETE FROM project_documents;
DELETE FROM payment_installments;
DELETE FROM projects;
DELETE FROM consultations;

-- =========================================================
-- 4. Customers
-- =========================================================
DELETE FROM customers;

-- =========================================================
-- 5. Other business/system data
-- =========================================================
DELETE FROM notifications;
DELETE FROM personal_events;

SET FOREIGN_KEY_CHECKS = 1;