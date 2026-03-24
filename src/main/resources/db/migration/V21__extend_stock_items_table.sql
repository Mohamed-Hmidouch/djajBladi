-- V21: Extend stock_items table for Sanitary & Security Shield
-- Adds stock_type categorization (VACCINE, MEDICATION, FEED, EQUIPMENT)
-- Increases unit_price precision to 4 decimal places for accurate treatment cost calculations
-- Requirements: 4.1, 8.2, 8.3

-- Add stock_type column with default value
ALTER TABLE stock_items
    ADD COLUMN stock_type VARCHAR(20) NOT NULL DEFAULT 'MEDICATION';

-- Modify unit_price precision from DECIMAL(12,2) to DECIMAL(12,4)
ALTER TABLE stock_items
    ALTER COLUMN unit_price TYPE DECIMAL(12, 4);

-- Create index on stock_type for efficient filtering
CREATE INDEX idx_stock_type_category ON stock_items(stock_type);

-- Add comments for documentation
COMMENT ON COLUMN stock_items.stock_type IS
    'Stock item category: VACCINE, MEDICATION, FEED, EQUIPMENT. Used for pharmacy integration and treatment tracking.';

COMMENT ON COLUMN stock_items.unit_price IS
    'Unit price with 4 decimal precision (DH). Required for accurate treatment cost calculations.';
