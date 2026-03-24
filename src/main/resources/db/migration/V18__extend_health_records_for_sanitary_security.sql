-- Migration: Extend health_records table for Sanitary & Security Shield feature
-- Task: 1.1 Create migration to extend health_records table
-- Requirements: 2.1, 2.2, 4.1

-- Add new columns to health_records table
ALTER TABLE health_records
ADD COLUMN withdrawal_days INTEGER,
ADD COLUMN is_vaccination BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN stock_item_id BIGINT,
ADD COLUMN quantity_used DECIMAL(12, 4);

-- Add foreign key constraint to stock_items table
ALTER TABLE health_records
ADD CONSTRAINT fk_health_record_stock_item 
    FOREIGN KEY (stock_item_id) REFERENCES stock_items(id);

-- Create indexes for performance optimization
CREATE INDEX idx_health_batch_withdrawal ON health_records(batch_id, withdrawal_days);
CREATE INDEX idx_health_withdrawal_days ON health_records(withdrawal_days);
CREATE INDEX idx_health_is_vaccination ON health_records(is_vaccination);
CREATE INDEX idx_health_stock_item ON health_records(stock_item_id);

-- Add comments for documentation
COMMENT ON COLUMN health_records.withdrawal_days IS 'Mandatory waiting period (in days) after antibiotic administration before poultry can be sold';
COMMENT ON COLUMN health_records.is_vaccination IS 'Indicates if this health record is for vaccination (no withdrawal period enforcement)';
COMMENT ON COLUMN health_records.stock_item_id IS 'Reference to the medication or vaccine used from pharmacy inventory';
COMMENT ON COLUMN health_records.quantity_used IS 'Quantity of medication/vaccine used (with 4 decimal precision)';
