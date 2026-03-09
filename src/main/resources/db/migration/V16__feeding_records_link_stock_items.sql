-- V16: Liaison des feeding_records aux stock_items et ajout de records pour LOT-2025-002
-- Mapping feed_type -> stock_item_id :
--   Starter / Demarrage / Démarrage -> id 2 (Aliment Demarrage)
--   Croissance                      -> id 3 (Aliment Croissance)
--   Finition                        -> id 4 (Aliment Finition)

UPDATE feeding_records
SET stock_item_id = 2
WHERE feed_type IN ('Starter', 'Demarrage', 'Démarrage')
  AND stock_item_id IS NULL;

UPDATE feeding_records
SET stock_item_id = 3
WHERE feed_type = 'Croissance'
  AND stock_item_id IS NULL;

UPDATE feeding_records
SET stock_item_id = 4
WHERE feed_type = 'Finition'
  AND stock_item_id IS NULL;

-- Correction du seul enregistrement qui pointe vers id=1 (Starter Feed Premium) pour batch 1
-- Il utilise feed_type='Demarrage' donc on le relie correctement a id=2
-- (deja traite par le UPDATE ci-dessus)

-- Ajout des feeding records pour LOT-2025-002 (batch_id=7, arrival_date=2026-02-27, 4500 poussins)
-- Phase demarrage : J1-J10 (~16kg/j croissant)
INSERT INTO feeding_records (batch_id, feeding_date, quantity, feed_type, stock_item_id, notes, recorded_by_id, created_at, updated_at) VALUES
(7, '2026-02-27', 15.50, 'Demarrage', 2, 'J1 - Phase demarrage', 1, NOW(), NOW()),
(7, '2026-02-28', 16.20, 'Demarrage', 2, 'J2 - Phase demarrage', 1, NOW(), NOW()),
(7, '2026-03-01', 17.00, 'Demarrage', 2, 'J3 - Phase demarrage', 1, NOW(), NOW()),
(7, '2026-03-02', 17.80, 'Demarrage', 2, 'J4 - Phase demarrage', 1, NOW(), NOW()),
(7, '2026-03-03', 18.60, 'Demarrage', 2, 'J5 - Phase demarrage', 1, NOW(), NOW()),
(7, '2026-03-04', 19.40, 'Demarrage', 2, 'J6 - Phase demarrage', 1, NOW(), NOW()),
(7, '2026-03-05', 20.20, 'Demarrage', 2, 'J7 - Phase demarrage', 1, NOW(), NOW()),
(7, '2026-03-06', 21.00, 'Demarrage', 2, 'J8 - Phase demarrage', 1, NOW(), NOW()),
(7, '2026-03-07', 21.80, 'Demarrage', 2, 'J9 - Phase demarrage', 1, NOW(), NOW()),
(7, '2026-03-08', 22.60, 'Demarrage', 2, 'J10 - Phase demarrage', 1, NOW(), NOW()),
-- Phase croissance : J11 (2026-03-09)
(7, '2026-03-09', 24.00, 'Croissance', 3, 'J11 - Phase croissance', 1, NOW(), NOW());
