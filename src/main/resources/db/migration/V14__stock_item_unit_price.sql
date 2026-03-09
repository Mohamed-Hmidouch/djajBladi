-- V14: Ajout du prix unitaire sur les items de stock
-- Permet le calcul du cout de revient alimentaire :
--   cout_aliment = SUM(feeding_record.quantity * stock_item.unit_price)
-- Le champ est nullable pour maintenir la compatibilite avec les items existants.

ALTER TABLE stock_items
    ADD COLUMN unit_price DECIMAL(12, 2);

COMMENT ON COLUMN stock_items.unit_price IS
    'Prix unitaire en DH (ex. DH/kg pour aliment). Requis pour le calcul du cout de revient.';
