CREATE TABLE daily_mortality_records (
    id BIGSERIAL PRIMARY KEY,
    batch_id BIGINT NOT NULL,
    record_date DATE NOT NULL,
    mortality_count INTEGER NOT NULL CHECK (mortality_count >= 0),
    notes TEXT,
    recorded_by_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_mortality_batch FOREIGN KEY (batch_id) REFERENCES batches(id) ON DELETE CASCADE,
    CONSTRAINT fk_mortality_recorded_by FOREIGN KEY (recorded_by_id) REFERENCES users(id),
    CONSTRAINT uq_mortality_batch_date UNIQUE (batch_id, record_date)
);

CREATE INDEX idx_mortality_batch ON daily_mortality_records(batch_id);
CREATE INDEX idx_mortality_date ON daily_mortality_records(record_date);
CREATE INDEX idx_mortality_recorded_by ON daily_mortality_records(recorded_by_id);
