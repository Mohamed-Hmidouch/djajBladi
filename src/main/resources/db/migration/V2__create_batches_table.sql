-- Spring Boot Best Practice: Flyway migration pour table batches (lots de poules)

CREATE TABLE batches (
    id BIGSERIAL PRIMARY KEY,
    batch_number VARCHAR(50) UNIQUE NOT NULL,
    chicken_count INTEGER NOT NULL,
    arrival_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    notes TEXT,
    created_by_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_batch_created_by FOREIGN KEY (created_by_id) REFERENCES users(id)
);

CREATE INDEX idx_batch_number ON batches(batch_number);
CREATE INDEX idx_batch_status ON batches(status);
CREATE INDEX idx_batch_arrival_date ON batches(arrival_date);
CREATE INDEX idx_batch_created_by ON batches(created_by_id);

COMMENT ON TABLE batches IS 'Lots de poules (batches)';
COMMENT ON COLUMN batches.status IS 'Active | Completed | Archived';
COMMENT ON COLUMN batches.chicken_count IS 'Nombre de poules dans le lot';
