-- V24: Fix stock_items enum values to match StockType Java enum
-- The 'type' column had old PascalCase values (Feed, Vaccine, Vitamin)
-- that no longer match the renamed enum constants (FEED, VACCINE, MEDICATION, EQUIPMENT).
-- Also synchronises the 'stock_type' column which was added in V21 but never populated.

-- Fix 'type' column: PascalCase -> UPPER_CASE
UPDATE stock_items SET type = 'FEED'       WHERE type = 'Feed';
UPDATE stock_items SET type = 'VACCINE'    WHERE type = 'Vaccine';
UPDATE stock_items SET type = 'MEDICATION' WHERE type = 'Vitamin';

-- Sync 'stock_type' column to match the corrected 'type' values
UPDATE stock_items SET stock_type = 'FEED'       WHERE type = 'FEED';
UPDATE stock_items SET stock_type = 'VACCINE'    WHERE type = 'VACCINE';
UPDATE stock_items SET stock_type = 'MEDICATION' WHERE type = 'MEDICATION';
UPDATE stock_items SET stock_type = 'EQUIPMENT'  WHERE type = 'EQUIPMENT';

COMMENT ON COLUMN stock_items.type IS
    'Primary stock type: FEED, VACCINE, MEDICATION, EQUIPMENT. Matches StockType Java enum (UPPER_CASE).';
