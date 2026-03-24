-- Migration: Create vaccination_protocols table for Sanitary & Security Shield feature
-- Task: 1.5 Create migration for vaccination_protocols table
-- Requirements: 5.1, 10.1, 10.3

-- Create vaccination_protocols table
CREATE TABLE vaccination_protocols (
    id BIGSERIAL PRIMARY KEY,
    strain VARCHAR(100) NOT NULL,
    vaccine_name VARCHAR(200) NOT NULL,
    day_of_life INTEGER NOT NULL,
    notes TEXT,
    created_by_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_vac_protocol_created_by FOREIGN KEY (created_by_id) REFERENCES users(id),
    CONSTRAINT uq_vac_protocol_strain_vaccine_day UNIQUE (strain, vaccine_name, day_of_life),
    CONSTRAINT chk_vac_protocol_day_positive CHECK (day_of_life > 0)
);

-- Create indexes for performance optimization
CREATE INDEX idx_vac_protocol_strain ON vaccination_protocols(strain);
CREATE INDEX idx_vac_protocol_day ON vaccination_protocols(day_of_life);

-- Add comments for documentation
COMMENT ON TABLE vaccination_protocols IS 'Stores strain-specific vaccination schedules for poultry batches';
COMMENT ON COLUMN vaccination_protocols.strain IS 'Chicken strain identifier (e.g., Ross 308, Cobb 500)';
COMMENT ON COLUMN vaccination_protocols.vaccine_name IS 'Name of the vaccine to be administered';
COMMENT ON COLUMN vaccination_protocols.day_of_life IS 'Day number when vaccination should be administered (must be positive)';
COMMENT ON COLUMN vaccination_protocols.notes IS 'Optional notes about vaccination protocol or administration instructions';
COMMENT ON COLUMN vaccination_protocols.created_by_id IS 'User who created this vaccination protocol';
COMMENT ON COLUMN vaccination_protocols.created_at IS 'Timestamp when the protocol was created';
COMMENT ON COLUMN vaccination_protocols.updated_at IS 'Timestamp when the protocol was last updated';
