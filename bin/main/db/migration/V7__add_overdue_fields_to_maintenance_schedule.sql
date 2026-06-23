-- V1.x__add_overdue_fields_to_maintenance_schedule.sql

-- Thêm trạng thái QUA_HAN vào enum (nếu dùng CHECK constraint)
-- Với MySQL/MariaDB dùng ENUM string thì không cần migrate, chỉ cần update Java enum

-- Thêm fields tracking hoàn thành trễ
ALTER TABLE maintenance_schedules 
ADD COLUMN completed_late BOOLEAN NOT NULL DEFAULT FALSE AFTER completed_at,
ADD COLUMN days_late INT DEFAULT NULL AFTER completed_late,
ADD COLUMN notes TEXT DEFAULT NULL AFTER days_late;