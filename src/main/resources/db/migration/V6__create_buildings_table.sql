CREATE TABLE buildings (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    max_capacity INTEGER NOT NULL,
    created_by_id BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_building_created_by FOREIGN KEY (created_by_id) REFERENCES users(id)
);

CREATE INDEX idx_building_name ON buildings(name);

COMMENT ON TABLE buildings IS 'Bâtiments (ex. Bâtiment A, B) avec capacité max en poussins';
