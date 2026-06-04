-- Migration: thêm 2 cột mới vào bảng consultations
-- Chạy file này trên DB hoặc thêm vào Flyway/Liquibase

ALTER TABLE consultations
    ADD COLUMN elevator_type VARCHAR(20) NULL COMMENT 'Loại thang máy, điền khi báo giá',
    ADD COLUMN project_type  VARCHAR(20) NULL COMMENT 'Loại dự án, điền khi báo giá';