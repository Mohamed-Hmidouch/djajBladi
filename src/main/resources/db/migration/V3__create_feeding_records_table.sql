-- Spring Boot Best Practice: Flyway migration pour table feeding_records (alimentation)

CREATE TABLE feeding_records (
    id BIGSERIAL PRIMARY KEY,
    batch_id BIGINT NOT NULL,
    feed_type VARCHAR(100) NOT NULL,
    quantity DECIMAL(10, 2) NOT NULL,
    feeding_date DATE NOT NULL,
    notes TEXT,
    recorded_by_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_feeding_batch FOREIGN KEY (batch_id) REFERENCES batches(id) ON DELETE CASCADE,
    CONSTRAINT fk_feeding_recorded_by FOREIGN KEY (recorded_by_id) REFERENCES users(id)
);

CREATE INDEX idx_feeding_batch ON feeding_records(batch_id);
CREATE INDEX idx_feeding_date ON feeding_records(feeding_date);
CREATE INDEX idx_feeding_recorded_by ON feeding_records(recorded_by_id);

COMMENT ON TABLE feeding_records IS 'Enregistrements d''alimentation des poules';
COMMENT ON COLUMN feeding_records.quantity IS 'Quantité en kg';
COMMENT ON COLUMN feeding_records.feed_type IS 'Type d''aliment donné';
