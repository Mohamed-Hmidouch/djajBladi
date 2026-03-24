-- Migration: Add current_count column to batches table
-- Purpose: Track living chickens separately from initial chicken_count for mortality synchronization
-- Requirements: 3.1, 3.2

-- Add current_count column (nullable initially to allow data migration)
ALTER TABLE batches ADD COLUMN current_count INTEGER;

-- Initialize current_count with chicken_count for existing batches
UPDATE batches SET current_count = chicken_count WHERE current_count IS NULL;

-- Make current_count NOT NULL after initialization
ALTER TABLE batches ALTER COLUMN current_count SET NOT NULL;

-- Create index on current_count for query performance
CREATE INDEX idx_batch_current_count ON batches(current_count);

COMMENT ON COLUMN batches.current_count IS 'Nombre actuel de poules vivantes dans le lot';
