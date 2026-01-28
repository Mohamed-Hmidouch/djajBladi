-- Spring Boot Best Practice: Flyway migration pour table sales (ventes)

CREATE TABLE sales (
    id BIGSERIAL PRIMARY KEY,
    batch_id BIGINT NOT NULL,
    client_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    sale_date DATE NOT NULL,
    payment_status VARCHAR(20) NOT NULL,
    delivery_address TEXT,
    notes TEXT,
    recorded_by_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_sale_batch FOREIGN KEY (batch_id) REFERENCES batches(id) ON DELETE CASCADE,
    CONSTRAINT fk_sale_client FOREIGN KEY (client_id) REFERENCES users(id),
    CONSTRAINT fk_sale_recorded_by FOREIGN KEY (recorded_by_id) REFERENCES users(id)
);

CREATE INDEX idx_sale_batch ON sales(batch_id);
CREATE INDEX idx_sale_client ON sales(client_id);
CREATE INDEX idx_sale_date ON sales(sale_date);
CREATE INDEX idx_sale_payment_status ON sales(payment_status);
CREATE INDEX idx_sale_recorded_by ON sales(recorded_by_id);

COMMENT ON TABLE sales IS 'Ventes de poules aux clients';
COMMENT ON COLUMN sales.payment_status IS 'Pending | Paid | Cancelled';
COMMENT ON COLUMN sales.quantity IS 'Nombre de poules vendues';
COMMENT ON COLUMN sales.total_price IS 'Prix total calculé: quantity * unit_price';
