-- V25: Add selling_price_per_unit to batches for client purchase system
-- This column stores the price per chicken set by admin when batch is READY_FOR_SALE

ALTER TABLE batches ADD COLUMN IF NOT EXISTS selling_price_per_unit NUMERIC(10,2);

-- Add a minimum_order_quantity column (default 1, admin can set per batch)
ALTER TABLE batches ADD COLUMN IF NOT EXISTS minimum_order_quantity INTEGER DEFAULT 1;

COMMENT ON COLUMN batches.selling_price_per_unit IS 'Price per chicken in DH set by admin when marking batch READY_FOR_SALE';
COMMENT ON COLUMN batches.minimum_order_quantity IS 'Minimum number of chickens a client must order from this batch';

-- Mark completed batches with enough age as READY_FOR_SALE for demo data
-- (Only batches older than 35 days with status = Completed are eligible)
UPDATE batches
SET status = 'READY_FOR_SALE',
    selling_price_per_unit = 35.00,
    minimum_order_quantity = 100
WHERE status = 'Completed'
  AND arrival_date < CURRENT_DATE - INTERVAL '35 days';
