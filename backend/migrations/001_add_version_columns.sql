-- Migration Script: Add version column for Optimistic Locking
-- Date: 2025-12-06
-- Description: Add version column to carts and cart_items tables for concurrent update handling

-- Add version column to carts table
ALTER TABLE carts ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0 NOT NULL;

-- Add version column to cart_items table
ALTER TABLE cart_items ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0 NOT NULL;

-- Add comments
COMMENT ON COLUMN carts.version IS 'Optimistic locking version for concurrent update handling';
COMMENT ON COLUMN cart_items.version IS 'Optimistic locking version for concurrent update handling';

-- Verify changes
SELECT 'Migration completed successfully. Version columns added to carts and cart_items tables.' AS status;
