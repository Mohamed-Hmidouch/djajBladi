-- V13: Ajout colonne stock_item_id dans feeding_records
-- Tracabilite obligatoire : chaque enregistrement d'alimentation
-- est lie a l'item de stock qui a ete consomme.
-- La contrainte NOT NULL est appliquee au niveau service (pas en base)
-- pour maintenir la compatibilite avec les donnees existantes (seed V12).

ALTER TABLE feeding_records
    ADD COLUMN stock_item_id BIGINT REFERENCES stock_items(id) ON DELETE SET NULL;

CREATE INDEX idx_feeding_stock_item ON feeding_records(stock_item_id);

COMMENT ON COLUMN feeding_records.stock_item_id IS
    'Tracabilite : item de stock (kg) consomme lors de cet enregistrement alimentation';
