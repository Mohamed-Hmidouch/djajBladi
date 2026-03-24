-- Migration: Extend daily_mortality_records table for mortality source attribution
-- Task: 1.2 Create migration to extend daily_mortality_records table
-- Requirements: 9.1, 9.2, 9.3

-- Add new columns to daily_mortality_records table
ALTER TABLE daily_mortality_records
ADD COLUMN source VARCHAR(30) NOT NULL DEFAULT 'WORKER_REPORT',
ADD COLUMN health_record_id BIGINT;

-- Add foreign key constraint to health_records table
ALTER TABLE daily_mortality_records
ADD CONSTRAINT fk_mortality_health_record 
    FOREIGN KEY (health_record_id) REFERENCES health_records(id);

-- Create indexes for performance optimization
CREATE INDEX idx_mortality_source ON daily_mortality_records(source);
CREATE INDEX idx_mortality_health_record ON daily_mortality_records(health_record_id);

-- Add comments for documentation
COMMENT ON COLUMN daily_mortality_records.source IS 'Source of mortality record: WORKER_REPORT or VETERINARIAN_EXAMINATION';
COMMENT ON COLUMN daily_mortality_records.health_record_id IS 'Reference to health record if mortality was recorded during veterinarian examination';
