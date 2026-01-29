CREATE TABLE stock_items (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(20) NOT NULL,
    name VARCHAR(200),
    quantity DECIMAL(12, 4) NOT NULL,
    unit VARCHAR(50) NOT NULL,
    created_by_id BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_stock_created_by FOREIGN KEY (created_by_id) REFERENCES users(id)
);

CREATE INDEX idx_stock_type ON stock_items(type);

COMMENT ON TABLE stock_items IS 'Inventaire: aliments, vaccins, vitamines';
COMMENT ON COLUMN stock_items.type IS 'Feed | Vaccine | Vitamin';
COMMENT ON COLUMN stock_items.unit IS 'sac, dose, flacon, etc.';
