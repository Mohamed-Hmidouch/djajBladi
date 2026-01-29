ALTER TABLE batches ADD COLUMN strain VARCHAR(100);
ALTER TABLE batches ADD COLUMN purchase_price DECIMAL(10, 2);
ALTER TABLE batches ADD COLUMN building_id BIGINT REFERENCES buildings(id);
CREATE INDEX idx_batch_building ON batches(building_id);

COMMENT ON COLUMN batches.strain IS 'Souche des poussins';
COMMENT ON COLUMN batches.purchase_price IS 'Prix d''achat du lot';
