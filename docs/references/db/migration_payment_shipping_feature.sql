-- ===================================================
-- Payment and Shipping Feature - Phase 1 Schema Migration
-- ===================================================
-- This migration adds support for:
-- 1. User addresses for shipping
-- 2. Payment tracking in participations
-- 3. Target amount and current amount tracking in group buys
-- ===================================================

-- Step 1: Create addresses table
-- ===================================================
CREATE TABLE IF NOT EXISTS addresses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    address_name VARCHAR(100) NOT NULL,
    recipient_name VARCHAR(50) NOT NULL,
    recipient_phone_number VARCHAR(20) NOT NULL,
    zipcode VARCHAR(10) NOT NULL,
    city VARCHAR(100) NOT NULL,
    street VARCHAR(255) NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_addresses_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_addresses_user_id (user_id),
    INDEX idx_addresses_is_default (user_id, is_default)
);

-- Step 2: Alter participations table
-- ===================================================
-- Add address reference, payment status, and total payment columns

-- Add address_id column (nullable for DIRECT delivery)
ALTER TABLE participations
ADD COLUMN address_id BIGINT NULL AFTER delivery_method;

-- Add foreign key constraint
ALTER TABLE participations
ADD CONSTRAINT fk_participations_address 
FOREIGN KEY (address_id) REFERENCES addresses(id) ON DELETE SET NULL;

-- Add index for address lookups
CREATE INDEX idx_participations_address_id ON participations(address_id);

-- Add status column for tracking participation status
ALTER TABLE participations
ADD COLUMN status VARCHAR(20) DEFAULT 'CONFIRMED' AFTER quantity;

-- Add total_payment column to track payment amount
ALTER TABLE participations
ADD COLUMN total_payment INT NOT NULL DEFAULT 0 AFTER status;

-- Add comment for status column
ALTER TABLE participations
MODIFY COLUMN status VARCHAR(20) DEFAULT 'CONFIRMED' COMMENT 'CONFIRMED, CANCELLED';

-- Step 3: Alter group_buys table
-- ===================================================
-- Rename total_price to target_amount and add current_amount column

-- Add current_amount column first (to track accumulated payment)
ALTER TABLE group_buys
ADD COLUMN current_amount INT NOT NULL DEFAULT 0 AFTER total_price;

-- Rename total_price to target_amount
ALTER TABLE group_buys
CHANGE COLUMN total_price target_amount INT NOT NULL;

-- Add comment for clarity
ALTER TABLE group_buys
MODIFY COLUMN target_amount INT NOT NULL COMMENT 'Target total amount to achieve';

ALTER TABLE group_buys
MODIFY COLUMN current_amount INT NOT NULL DEFAULT 0 COMMENT 'Current accumulated amount from participations';

-- Step 4: Update existing data (if any)
-- ===================================================
-- For existing participations, calculate and set total_payment
-- This assumes price per person = target_amount / target_headcount
UPDATE participations p
INNER JOIN group_buys gb ON p.group_buy_id = gb.id
SET p.total_payment = FLOOR((gb.target_amount / gb.target_headcount) * p.quantity);

-- For existing group_buys, calculate current_amount from participations
UPDATE group_buys gb
SET gb.current_amount = (
    SELECT COALESCE(SUM(p.total_payment), 0)
    FROM participations p
    WHERE p.group_buy_id = gb.id
    AND p.status = 'CONFIRMED'
);

-- Step 5: Verification queries
-- ===================================================
-- Run these queries to verify the migration

-- Check addresses table structure
-- DESCRIBE addresses;

-- Check participations table structure
-- DESCRIBE participations;

-- Check group_buys table structure
-- DESCRIBE group_buys;

-- Verify data integrity
-- SELECT gb.id, gb.title, gb.target_amount, gb.current_amount, 
--        COUNT(p.id) as participation_count,
--        SUM(p.total_payment) as calculated_amount
-- FROM group_buys gb
-- LEFT JOIN participations p ON gb.id = p.group_buy_id AND p.status = 'CONFIRMED'
-- GROUP BY gb.id;

-- ===================================================
-- Migration Notes:
-- ===================================================
-- 1. This migration is designed to be run on an existing database
-- 2. Existing data will be preserved and calculated appropriately
-- 3. The address_id column is nullable to support DIRECT delivery
-- 4. The status column in participations allows tracking confirmed/cancelled states
-- 5. The current_amount in group_buys is calculated from participation payments
-- 6. Run verification queries after migration to ensure data integrity
-- ===================================================
