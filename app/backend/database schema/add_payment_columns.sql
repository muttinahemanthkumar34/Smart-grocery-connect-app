-- Add upi_id column to shops table for payment processing
ALTER TABLE `shops` ADD COLUMN `upi_id` VARCHAR(100) NULL AFTER `notifications_enabled`;

-- Add payment_method column to orders table to track payment type
ALTER TABLE `orders` ADD COLUMN `payment_method` VARCHAR(20) DEFAULT 'COD' AFTER `total_amount`;
