-- Migration: Add ingredients column to group_buys table
-- Version: Manual migration for existing databases
-- Date: 2025-11-07
-- Description: Adds ingredients field to separate recipe ingredients from content

-- Add ingredients column to group_buys table
ALTER TABLE group_buys 
ADD COLUMN ingredients VARCHAR(1000);

-- Add comment
COMMENT ON COLUMN group_buys.ingredients IS '레시피 재료 목록 (레시피 기반 공구만 해당)';

-- Optional: For H2 database (if using H2 syntax)
-- H2 doesn't support COMMENT ON COLUMN, so just add the column:
-- ALTER TABLE group_buys ADD COLUMN IF NOT EXISTS ingredients VARCHAR(1000);
