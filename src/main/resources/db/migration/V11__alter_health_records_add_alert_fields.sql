ALTER TABLE health_records ADD COLUMN treatment_cost DECIMAL(12, 2);
ALTER TABLE health_records ADD COLUMN requires_approval BOOLEAN DEFAULT FALSE;
ALTER TABLE health_records ADD COLUMN approval_status VARCHAR(20);
ALTER TABLE health_records ADD COLUMN approved_by_id BIGINT;
ALTER TABLE health_records ADD COLUMN approved_at TIMESTAMP;

ALTER TABLE health_records
    ADD CONSTRAINT fk_health_approved_by FOREIGN KEY (approved_by_id) REFERENCES users(id);

CREATE INDEX idx_health_approval_status ON health_records(approval_status);
