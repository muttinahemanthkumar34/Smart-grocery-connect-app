-- Add is_online column to shops table
ALTER TABLE shops ADD COLUMN is_online TINYINT(1) DEFAULT 1;
