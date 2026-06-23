-- V1.x__add_cycle_months_to_maintenance_contract.sql
ALTER TABLE maintenance_contracts 
ADD COLUMN cycle_months INT NOT NULL DEFAULT 2 AFTER end_date;