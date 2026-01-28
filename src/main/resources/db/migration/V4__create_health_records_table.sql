-- Spring Boot Best Practice: Flyway migration pour table health_records (santé/vétérinaire)

CREATE TABLE health_records (
    id BIGSERIAL PRIMARY KEY,
    batch_id BIGINT NOT NULL,
    veterinarian_id BIGINT NOT NULL,
    diagnosis VARCHAR(255) NOT NULL,
    treatment TEXT,
    examination_date DATE NOT NULL,
    next_visit_date DATE,
    mortality_count INTEGER DEFAULT 0,
    notes TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_health_batch FOREIGN KEY (batch_id) REFERENCES batches(id) ON DELETE CASCADE,
    CONSTRAINT fk_health_veterinarian FOREIGN KEY (veterinarian_id) REFERENCES users(id)
);

CREATE INDEX idx_health_batch ON health_records(batch_id);
CREATE INDEX idx_health_veterinarian ON health_records(veterinarian_id);
CREATE INDEX idx_health_examination_date ON health_records(examination_date);
CREATE INDEX idx_health_next_visit ON health_records(next_visit_date);

COMMENT ON TABLE health_records IS 'Enregistrements de santé et visites vétérinaires';
COMMENT ON COLUMN health_records.mortality_count IS 'Nombre de poules mortes';
COMMENT ON COLUMN health_records.diagnosis IS 'Diagnostic du vétérinaire';
