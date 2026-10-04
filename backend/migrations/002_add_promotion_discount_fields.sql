-- Migration: Add fixed_amount and max_discount to promotions table
-- Date: 2025-12-27
-- Description: Support both fixed amount and percentage-based vouchers with max discount cap

-- Add fixed_amount column for fixed discount vouchers
ALTER TABLE promotions
ADD COLUMN IF NOT EXISTS fixed_amount DOUBLE PRECISION;

-- Add max_discount column to cap percentage-based discounts
ALTER TABLE promotions
ADD COLUMN IF NOT EXISTS max_discount DOUBLE PRECISION;

-- Add comments for documentation
COMMENT ON COLUMN promotions.fixed_amount IS 'Fixed discount amount (VND) - takes priority over percent_discount';
COMMENT ON COLUMN promotions.max_discount IS 'Maximum discount cap (VND) for percentage-based promotions';

-- Add check constraint: enforce valid discount configuration while allowing
-- promotions with no numeric discount (e.g., free-shipping promotions).
-- Rules:
-- - percent_discount, when present, must be > 0
-- - fixed_amount, when present, must be > 0
-- - percent_discount and fixed_amount cannot both be set at the same time
ALTER TABLE promotions
DROP CONSTRAINT IF EXISTS chk_promotion_discount_type;

ALTER TABLE promotions
ADD CONSTRAINT chk_promotion_discount_type
CHECK (
    (percent_discount IS NULL OR percent_discount > 0)
    AND (fixed_amount IS NULL OR fixed_amount > 0)
    AND NOT (percent_discount IS NOT NULL AND fixed_amount IS NOT NULL)
);
