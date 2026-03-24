-- V17__batch_assigned_to.sql
-- Add assigned_to_id column to batches table
ALTER TABLE batches ADD COLUMN assigned_to_id BIGINT;

-- Backfill: Assign existing batches to an ouvrier (karim@djajbladi.ma) to allow NOT NULL constraint
-- If karim doesn't exist (e.g., in tests), we fallback to any user, or we can just pick the min ID.
DO $$ 
DECLARE 
    first_ouvrier_id BIGINT;
BEGIN
    SELECT id INTO first_ouvrier_id FROM users WHERE email = 'karim@djajbladi.ma' LIMIT 1;
    
    IF first_ouvrier_id IS NULL THEN
        SELECT id INTO first_ouvrier_id FROM users WHERE role = 'Ouvrier' LIMIT 1;
    END IF;

    IF first_ouvrier_id IS NULL THEN
        SELECT MIN(id) INTO first_ouvrier_id FROM users;
    END IF;

    IF first_ouvrier_id IS NOT NULL THEN
        UPDATE batches SET assigned_to_id = first_ouvrier_id WHERE assigned_to_id IS NULL;
    END IF;
END $$;

-- Enforce NOT NULL and create foreign key + index
ALTER TABLE batches ALTER COLUMN assigned_to_id SET NOT NULL;
ALTER TABLE batches ADD CONSTRAINT fk_batch_assigned_to FOREIGN KEY (assigned_to_id) REFERENCES users(id);
CREATE INDEX idx_batch_assigned_to ON batches(assigned_to_id);
