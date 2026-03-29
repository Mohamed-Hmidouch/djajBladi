-- ============================================================
-- V29 : Seed demo farm data - Ferme Beni Mellal
-- Covers all implemented features/scenarios
-- ============================================================

-- ============================================================
-- 1. USERS (Ouvrier, Veterinaire, Clients)
-- ============================================================
INSERT INTO users (full_name, email, password_hash, phone_number, role, is_active, city, created_at, updated_at)
VALUES
  ('Youssef Benali',    'ouvrier1@djajbladi.ma',    '$2a$12$HwKn4RlFMQigDXhJsIsr5OIWh2GkexW0uZIII2TRfNbxvpsf.XBKS', '0612345678', 'Ouvrier',     true, 'Beni Mellal', NOW(), NOW()),
  ('Rachid Ouali',      'ouvrier2@djajbladi.ma',    '$2a$12$HwKn4RlFMQigDXhJsIsr5OIWh2GkexW0uZIII2TRfNbxvpsf.XBKS', '0623456789', 'Ouvrier',     true, 'Beni Mellal', NOW(), NOW()),
  ('Dr. Sara Moussaoui','vet@djajbladi.ma',          '$2a$12$HwKn4RlFMQigDXhJsIsr5OIWh2GkexW0uZIII2TRfNbxvpsf.XBKS', '0634567890', 'Veterinaire', true, 'Beni Mellal', NOW(), NOW()),
  ('Hamid Tazi',        'client1@djajbladi.ma',      '$2a$12$HwKn4RlFMQigDXhJsIsr5OIWh2GkexW0uZIII2TRfNbxvpsf.XBKS', '0645678901', 'Client',      true, 'Casablanca',  NOW(), NOW()),
  ('Fatima Chraibi',    'client2@djajbladi.ma',      '$2a$12$HwKn4RlFMQigDXhJsIsr5OIWh2GkexW0uZIII2TRfNbxvpsf.XBKS', '0656789012', 'Client',      true, 'Rabat',       NOW(), NOW()),
  ('Khalid Mansouri',   'client3@djajbladi.ma',      '$2a$12$HwKn4RlFMQigDXhJsIsr5OIWh2GkexW0uZIII2TRfNbxvpsf.XBKS', '0667890123', 'Client',      true, 'Marrakech',   NOW(), NOW());

-- ============================================================
-- 2. BUILDINGS (4 batiments)
-- ============================================================
INSERT INTO buildings (name, max_capacity, created_by_id, created_at, updated_at)
VALUES
  ('Batiment A', 5000, 188, NOW(), NOW()),
  ('Batiment B', 4000, 188, NOW(), NOW()),
  ('Batiment C', 6000, 188, NOW(), NOW()),
  ('Batiment D', 3000, 188, NOW(), NOW());

-- ============================================================
-- 3. STOCK ITEMS (Aliments, Medicaments, Vaccins, Equipements)
-- ============================================================
INSERT INTO stock_items (type, name, quantity, unit, unit_price, stock_type, created_by_id, created_at, updated_at)
VALUES
  ('FEED',       'Aliment Demarrage (Starter)',    2500.00, 'kg',     3.50,  'FEED',       188, NOW(), NOW()),
  ('FEED',       'Aliment Croissance (Grower)',    3800.00, 'kg',     3.20,  'FEED',       188, NOW(), NOW()),
  ('FEED',       'Aliment Finition (Finisher)',    1200.00, 'kg',     3.00,  'FEED',       188, NOW(), NOW()),
  ('MEDICINE',   'Enrofloxacine 10%',               150.00, 'litre',  85.00, 'MEDICATION', 188, NOW(), NOW()),
  ('MEDICINE',   'Amoxicilline poudre',              80.00, 'kg',     120.00,'MEDICATION', 188, NOW(), NOW()),
  ('MEDICINE',   'Vitamines + Electrolytes',         60.00, 'kg',     95.00, 'MEDICATION', 188, NOW(), NOW()),
  ('VACCINE',    'Vaccin Newcastle (IB+ND)',          200.00, 'dose',  1.20,  'VACCINE',    188, NOW(), NOW()),
  ('VACCINE',    'Vaccin Gumboro (IBD)',              200.00, 'dose',  1.50,  'VACCINE',    188, NOW(), NOW()),
  ('VACCINE',    'Vaccin Bronchite Infectieuse',      150.00, 'dose',  1.80,  'VACCINE',    188, NOW(), NOW()),
  ('EQUIPMENT',  'Abreuvoirs automatiques',             30.00, 'unite', 45.00, 'EQUIPMENT',  188, NOW(), NOW()),
  ('EQUIPMENT',  'Mangeoires lineaires',                20.00, 'unite', 38.00, 'EQUIPMENT',  188, NOW(), NOW());

-- ============================================================
-- 4. VACCINATION PROTOCOLS (Programmes par race)
-- ============================================================
INSERT INTO vaccination_protocols (strain, vaccine_name, day_of_life, notes, created_by_id, created_at, updated_at)
VALUES
  -- Ross 308 protocol
  ('Ross 308', 'Vaccin Newcastle + Bronchite (IB+ND)',    1,  'Spray a la couvoir - 1er jour de vie',           188, NOW(), NOW()),
  ('Ross 308', 'Vaccin Gumboro IBD Intermediate',         14, 'Eau de boisson - Surveiller les signes cliniques', 188, NOW(), NOW()),
  ('Ross 308', 'Vaccin Newcastle + IB rappel',            21, 'Eau de boisson - 3eme semaine',                   188, NOW(), NOW()),
  ('Ross 308', 'Vaccin Gumboro IBD rappel',               28, 'Eau de boisson - 4eme semaine',                   188, NOW(), NOW()),

  -- Cobb 500 protocol
  ('Cobb 500', 'Vaccin Newcastle + Bronchite (IB+ND)',    1,  'Spray a la couvoir',                              188, NOW(), NOW()),
  ('Cobb 500', 'Vaccin Gumboro IBD',                      12, 'Eau de boisson',                                  188, NOW(), NOW()),
  ('Cobb 500', 'Vaccin Newcastle rappel',                 21, 'Eau de boisson',                                  188, NOW(), NOW()),

  -- Arbor Acres protocol
  ('Arbor Acres', 'Vaccin Newcastle + IB',                1,  'Spray - 1er jour',                                188, NOW(), NOW()),
  ('Arbor Acres', 'Vaccin Gumboro IBD',                   16, 'Eau de boisson',                                  188, NOW(), NOW()),
  ('Arbor Acres', 'Rappel Newcastle',                     24, 'Eau de boisson',                                  188, NOW(), NOW());

-- ============================================================
-- 5. BATCHES
-- Lot 1 : SOLD (termine il y a 2 mois)
-- Lot 2 : Completed (pret a vendre)
-- Lot 3 : READY_FOR_SALE
-- Lot 4 : Active (en cours, 3 semaines)
-- Lot 5 : Active (nouveau, 1 semaine)
-- ============================================================

-- Lot 1 : Termine et vendu - Batiment A - Ross 308
INSERT INTO batches (batch_number, chicken_count, current_count, arrival_date, status, strain, purchase_price, selling_price_per_unit, minimum_order_quantity, building_id, assigned_to_id, created_by_id, notes, created_at, updated_at)
SELECT
  'LOT-2026-001', 4800, 0, '2025-12-15', 'SOLD', 'Ross 308', 8.50, 28.00, 100,
  b.id, u_o.id, 188,
  'Premier lot de 2026 - bonne performance - FCR 1.75',
  '2025-12-15 08:00:00', '2026-02-10 10:00:00'
FROM buildings b, users u_o
WHERE b.name = 'Batiment A' AND u_o.email = 'ouvrier1@djajbladi.ma';

-- Lot 2 : Completed - Batiment B - Cobb 500
INSERT INTO batches (batch_number, chicken_count, current_count, arrival_date, status, strain, purchase_price, selling_price_per_unit, minimum_order_quantity, building_id, assigned_to_id, created_by_id, notes, created_at, updated_at)
SELECT
  'LOT-2026-002', 3800, 3720, '2026-01-10', 'Completed', 'Cobb 500', 8.20, 27.50, 200,
  b.id, u_o.id, 188,
  'Lot termine - en attente vente - poids moyen 2.3 kg',
  '2026-01-10 08:00:00', NOW()
FROM buildings b, users u_o
WHERE b.name = 'Batiment B' AND u_o.email = 'ouvrier1@djajbladi.ma';

-- Lot 3 : READY_FOR_SALE - Batiment C - Ross 308
INSERT INTO batches (batch_number, chicken_count, current_count, arrival_date, status, strain, purchase_price, selling_price_per_unit, minimum_order_quantity, building_id, assigned_to_id, created_by_id, notes, created_at, updated_at)
SELECT
  'LOT-2026-003', 5500, 5430, '2026-01-20', 'READY_FOR_SALE', 'Ross 308', 8.50, 28.50, 150,
  b.id, u_o.id, 188,
  'Lot pret pour vente - poids moyen 2.5 kg - qualite excellente',
  '2026-01-20 08:00:00', NOW()
FROM buildings b, users u_o
WHERE b.name = 'Batiment C' AND u_o.email = 'ouvrier2@djajbladi.ma';

-- Lot 4 : Active depuis 3 semaines - Batiment D - Cobb 500
INSERT INTO batches (batch_number, chicken_count, current_count, arrival_date, status, strain, purchase_price, selling_price_per_unit, minimum_order_quantity, building_id, assigned_to_id, created_by_id, notes, created_at, updated_at)
SELECT
  'LOT-2026-004', 2900, 2880, '2026-03-08', 'Active', 'Cobb 500', 8.00, 27.00, 100,
  b.id, u_o.id, 188,
  'Lot actif - semaine 3 - bonne croissance',
  '2026-03-08 08:00:00', NOW()
FROM buildings b, users u_o
WHERE b.name = 'Batiment D' AND u_o.email = 'ouvrier2@djajbladi.ma';

-- Lot 5 : Active depuis 1 semaine - Batiment A - Arbor Acres
INSERT INTO batches (batch_number, chicken_count, current_count, arrival_date, status, strain, purchase_price, selling_price_per_unit, minimum_order_quantity, building_id, assigned_to_id, created_by_id, notes, created_at, updated_at)
SELECT
  'LOT-2026-005', 4900, 4895, '2026-03-22', 'Active', 'Arbor Acres', 7.80, 27.00, 100,
  b.id, u_o.id, 188,
  'Nouveau lot - 1ere semaine - croissance normale',
  '2026-03-22 08:00:00', NOW()
FROM buildings b, users u_o
WHERE b.name = 'Batiment A' AND u_o.email = 'ouvrier1@djajbladi.ma';

-- ============================================================
-- 6. FEEDING RECORDS (enregistrements alimentation)
-- ============================================================

-- Lot 4 (actif 3 semaines) - alimentation quotidienne
INSERT INTO feeding_records (batch_id, feed_type, quantity, feeding_date, notes, recorded_by_id, stock_item_id, created_at, updated_at)
SELECT b.id, 'Aliment Demarrage (Starter)', 180.00, '2026-03-08', 'Alimentation J1', u.id, s.id, NOW(), NOW()
FROM batches b, users u, stock_items s
WHERE b.batch_number = 'LOT-2026-004' AND u.email = 'ouvrier2@djajbladi.ma' AND s.name = 'Aliment Demarrage (Starter)';

INSERT INTO feeding_records (batch_id, feed_type, quantity, feeding_date, notes, recorded_by_id, stock_item_id, created_at, updated_at)
SELECT b.id, 'Aliment Demarrage (Starter)', 195.00, '2026-03-10', 'Alimentation J3 - appetit normal', u.id, s.id, NOW(), NOW()
FROM batches b, users u, stock_items s
WHERE b.batch_number = 'LOT-2026-004' AND u.email = 'ouvrier2@djajbladi.ma' AND s.name = 'Aliment Demarrage (Starter)';

INSERT INTO feeding_records (batch_id, feed_type, quantity, feeding_date, notes, recorded_by_id, stock_item_id, created_at, updated_at)
SELECT b.id, 'Aliment Demarrage (Starter)', 210.00, '2026-03-13', 'Alimentation J6', u.id, s.id, NOW(), NOW()
FROM batches b, users u, stock_items s
WHERE b.batch_number = 'LOT-2026-004' AND u.email = 'ouvrier2@djajbladi.ma' AND s.name = 'Aliment Demarrage (Starter)';

INSERT INTO feeding_records (batch_id, feed_type, quantity, feeding_date, notes, recorded_by_id, stock_item_id, created_at, updated_at)
SELECT b.id, 'Aliment Croissance (Grower)', 250.00, '2026-03-17', 'Passage au Grower - J10', u.id, s.id, NOW(), NOW()
FROM batches b, users u, stock_items s
WHERE b.batch_number = 'LOT-2026-004' AND u.email = 'ouvrier2@djajbladi.ma' AND s.name = 'Aliment Croissance (Grower)';

INSERT INTO feeding_records (batch_id, feed_type, quantity, feeding_date, notes, recorded_by_id, stock_item_id, created_at, updated_at)
SELECT b.id, 'Aliment Croissance (Grower)', 280.00, '2026-03-22', 'Alimentation J15 - croissance normale', u.id, s.id, NOW(), NOW()
FROM batches b, users u, stock_items s
WHERE b.batch_number = 'LOT-2026-004' AND u.email = 'ouvrier1@djajbladi.ma' AND s.name = 'Aliment Croissance (Grower)';

INSERT INTO feeding_records (batch_id, feed_type, quantity, feeding_date, notes, recorded_by_id, stock_item_id, created_at, updated_at)
SELECT b.id, 'Aliment Croissance (Grower)', 295.00, '2026-03-26', 'Alimentation J19', u.id, s.id, NOW(), NOW()
FROM batches b, users u, stock_items s
WHERE b.batch_number = 'LOT-2026-004' AND u.email = 'ouvrier2@djajbladi.ma' AND s.name = 'Aliment Croissance (Grower)';

-- Lot 5 (actif 1 semaine)
INSERT INTO feeding_records (batch_id, feed_type, quantity, feeding_date, notes, recorded_by_id, stock_item_id, created_at, updated_at)
SELECT b.id, 'Aliment Demarrage (Starter)', 160.00, '2026-03-22', 'Alimentation J1 - bon appetit', u.id, s.id, NOW(), NOW()
FROM batches b, users u, stock_items s
WHERE b.batch_number = 'LOT-2026-005' AND u.email = 'ouvrier1@djajbladi.ma' AND s.name = 'Aliment Demarrage (Starter)';

INSERT INTO feeding_records (batch_id, feed_type, quantity, feeding_date, notes, recorded_by_id, stock_item_id, created_at, updated_at)
SELECT b.id, 'Aliment Demarrage (Starter)', 175.00, '2026-03-25', 'Alimentation J4', u.id, s.id, NOW(), NOW()
FROM batches b, users u, stock_items s
WHERE b.batch_number = 'LOT-2026-005' AND u.email = 'ouvrier1@djajbladi.ma' AND s.name = 'Aliment Demarrage (Starter)';

INSERT INTO feeding_records (batch_id, feed_type, quantity, feeding_date, notes, recorded_by_id, stock_item_id, created_at, updated_at)
SELECT b.id, 'Aliment Demarrage (Starter)', 185.00, '2026-03-28', 'Alimentation J7', u.id, s.id, NOW(), NOW()
FROM batches b, users u, stock_items s
WHERE b.batch_number = 'LOT-2026-005' AND u.email = 'ouvrier1@djajbladi.ma' AND s.name = 'Aliment Demarrage (Starter)';

-- ============================================================
-- 7. DAILY MORTALITY RECORDS
-- ============================================================

-- Lot 4 - mortalites normales
INSERT INTO daily_mortality_records (batch_id, record_date, mortality_count, source, notes, recorded_by_id, created_at, updated_at)
SELECT b.id, '2026-03-09', 3, 'WORKER_REPORT', 'Mortalite normale J2', u.id, NOW(), NOW()
FROM batches b, users u WHERE b.batch_number = 'LOT-2026-004' AND u.email = 'ouvrier2@djajbladi.ma';

INSERT INTO daily_mortality_records (batch_id, record_date, mortality_count, source, notes, recorded_by_id, created_at, updated_at)
SELECT b.id, '2026-03-12', 2, 'WORKER_REPORT', 'Mortalite faible - bon signe', u.id, NOW(), NOW()
FROM batches b, users u WHERE b.batch_number = 'LOT-2026-004' AND u.email = 'ouvrier2@djajbladi.ma';

INSERT INTO daily_mortality_records (batch_id, record_date, mortality_count, source, notes, recorded_by_id, created_at, updated_at)
SELECT b.id, '2026-03-19', 8, 'WORKER_REPORT', 'Legere augmentation - surveiller', u.id, NOW(), NOW()
FROM batches b, users u WHERE b.batch_number = 'LOT-2026-004' AND u.email = 'ouvrier2@djajbladi.ma';

INSERT INTO daily_mortality_records (batch_id, record_date, mortality_count, source, notes, recorded_by_id, created_at, updated_at)
SELECT b.id, '2026-03-25', 4, 'WORKER_REPORT', 'Retour a la normale apres traitement', u.id, NOW(), NOW()
FROM batches b, users u WHERE b.batch_number = 'LOT-2026-004' AND u.email = 'ouvrier1@djajbladi.ma';

-- Lot 5 - mortalites 1ere semaine
INSERT INTO daily_mortality_records (batch_id, record_date, mortality_count, source, notes, recorded_by_id, created_at, updated_at)
SELECT b.id, '2026-03-23', 2, 'WORKER_REPORT', 'Mortalite normale 1ere semaine', u.id, NOW(), NOW()
FROM batches b, users u WHERE b.batch_number = 'LOT-2026-005' AND u.email = 'ouvrier1@djajbladi.ma';

INSERT INTO daily_mortality_records (batch_id, record_date, mortality_count, source, notes, recorded_by_id, created_at, updated_at)
SELECT b.id, '2026-03-27', 1, 'WORKER_REPORT', 'Tres faible mortalite - excellent lot', u.id, NOW(), NOW()
FROM batches b, users u WHERE b.batch_number = 'LOT-2026-005' AND u.email = 'ouvrier1@djajbladi.ma';

-- Lot 2 - mortalites historiques
INSERT INTO daily_mortality_records (batch_id, record_date, mortality_count, source, notes, recorded_by_id, created_at, updated_at)
SELECT b.id, '2026-01-25', 12, 'WORKER_REPORT', 'Stress thermique - temperature basse', u.id, NOW(), NOW()
FROM batches b, users u WHERE b.batch_number = 'LOT-2026-002' AND u.email = 'ouvrier1@djajbladi.ma';

INSERT INTO daily_mortality_records (batch_id, record_date, mortality_count, source, notes, recorded_by_id, created_at, updated_at)
SELECT b.id, '2026-02-05', 6, 'WORKER_REPORT', 'Retour a la normale apres chauffage', u.id, NOW(), NOW()
FROM batches b, users u WHERE b.batch_number = 'LOT-2026-002' AND u.email = 'ouvrier1@djajbladi.ma';

INSERT INTO daily_mortality_records (batch_id, record_date, mortality_count, source, notes, recorded_by_id, created_at, updated_at)
SELECT b.id, '2026-02-20', 62, 'VETERINARIAN_EXAMINATION', 'Diagnostique maladie - traitement administre', u.id, NOW(), NOW()
FROM batches b, users u WHERE b.batch_number = 'LOT-2026-002' AND u.email = 'vet@djajbladi.ma';

-- ============================================================
-- 8. HEALTH RECORDS (examens veterinaires)
-- Scenario 1 : traitement simple approuve
-- Scenario 2 : vaccination (is_vaccination = true)
-- Scenario 3 : traitement en attente d'approbation
-- Scenario 4 : traitement rejete
-- ============================================================

-- Scenario 1 : Traitement bronchite - Lot 2 - APPROUVE
INSERT INTO health_records (batch_id, veterinarian_id, diagnosis, treatment, examination_date, next_visit_date, mortality_count, notes, treatment_cost, requires_approval, approval_status, approved_by_id, approved_at, withdrawal_days, is_vaccination, stock_item_id, quantity_used, created_at, updated_at)
SELECT
  b.id, vet.id,
  'Bronchite infectieuse - forme respiratoire',
  'Enrofloxacine 10% dans l eau de boisson pendant 5 jours - dose 1ml/litre',
  '2026-02-03', '2026-02-10', 5,
  'Cas detecte tot - pronostic favorable avec traitement',
  450.00, true, 'APPROVED', adm.id, '2026-02-03 14:00:00', 7, false,
  s.id, 10.00,
  NOW(), NOW()
FROM batches b, users vet, users adm, stock_items s
WHERE b.batch_number = 'LOT-2026-002'
  AND vet.email = 'vet@djajbladi.ma'
  AND adm.email = 'admin@djajbladi.ma'
  AND s.name = 'Enrofloxacine 10%';

-- Scenario 2 : Vaccination Gumboro - Lot 4 - APPROUVE (is_vaccination = true)
INSERT INTO health_records (batch_id, veterinarian_id, diagnosis, treatment, examination_date, next_visit_date, mortality_count, notes, treatment_cost, requires_approval, approval_status, approved_by_id, approved_at, withdrawal_days, is_vaccination, stock_item_id, quantity_used, created_at, updated_at)
SELECT
  b.id, vet.id,
  'Vaccination preventive Gumboro IBD - J14',
  'Administration vaccin IBD Intermediate en eau de boisson - 2900 doses',
  '2026-03-22', '2026-03-29', 0,
  'Vaccination realisee selon protocole Cobb 500 - J14',
  87.00, false, 'APPROVED', adm.id, '2026-03-22 10:00:00', 0, true,
  s.id, 2900.00,
  NOW(), NOW()
FROM batches b, users vet, users adm, stock_items s
WHERE b.batch_number = 'LOT-2026-004'
  AND vet.email = 'vet@djajbladi.ma'
  AND adm.email = 'admin@djajbladi.ma'
  AND s.name = 'Vaccin Gumboro (IBD)';

-- Scenario 3 : Traitement en attente approbation - Lot 4
INSERT INTO health_records (batch_id, veterinarian_id, diagnosis, treatment, examination_date, next_visit_date, mortality_count, notes, treatment_cost, requires_approval, approval_status, withdrawal_days, is_vaccination, stock_item_id, quantity_used, created_at, updated_at)
SELECT
  b.id, vet.id,
  'Suspicion Newcastle - mortalite elevee J11',
  'Amoxicilline poudre + vitamines en eau de boisson pendant 7 jours',
  '2026-03-19', '2026-03-26', 8,
  'Mortalite augmentee - traitement d urgence propose - necessite validation admin',
  320.00, true, 'PENDING_APPROVAL', 5, false,
  s.id, 3.00,
  NOW(), NOW()
FROM batches b, users vet, stock_items s
WHERE b.batch_number = 'LOT-2026-004'
  AND vet.email = 'vet@djajbladi.ma'
  AND s.name = 'Amoxicilline poudre';

-- Scenario 4 : Traitement rejete - Lot 3
INSERT INTO health_records (batch_id, veterinarian_id, diagnosis, treatment, examination_date, next_visit_date, mortality_count, notes, treatment_cost, requires_approval, approval_status, approved_by_id, approved_at, withdrawal_days, is_vaccination, stock_item_id, quantity_used, created_at, updated_at)
SELECT
  b.id, vet.id,
  'Diarrhee - suspicion coccidiose',
  'Toltrazuril 2.5% - traitement coccidiose',
  '2026-02-01', '2026-02-08', 2,
  'Dosage propose trop eleve - a revoir - admin a rejete le traitement',
  180.00, true, 'REJECTED', adm.id, '2026-02-01 16:30:00', 3, false,
  s.id, 2.00,
  NOW(), NOW()
FROM batches b, users vet, users adm, stock_items s
WHERE b.batch_number = 'LOT-2026-003'
  AND vet.email = 'vet@djajbladi.ma'
  AND adm.email = 'admin@djajbladi.ma'
  AND s.name = 'Vitamines + Electrolytes';

-- Vaccination Lot 5 - J1 (Newcastle)
INSERT INTO health_records (batch_id, veterinarian_id, diagnosis, treatment, examination_date, next_visit_date, mortality_count, notes, treatment_cost, requires_approval, approval_status, approved_by_id, approved_at, withdrawal_days, is_vaccination, stock_item_id, quantity_used, created_at, updated_at)
SELECT
  b.id, vet.id,
  'Vaccination preventive Newcastle + IB - J1',
  'Spray vaccin Newcastle + IB a la couvoir - 4900 doses',
  '2026-03-22', '2026-03-29', 0,
  'Vaccination d entree selon protocole Arbor Acres',
  88.20, false, 'APPROVED', adm.id, '2026-03-22 08:30:00', 0, true,
  s.id, 4900.00,
  NOW(), NOW()
FROM batches b, users vet, users adm, stock_items s
WHERE b.batch_number = 'LOT-2026-005'
  AND vet.email = 'vet@djajbladi.ma'
  AND adm.email = 'admin@djajbladi.ma'
  AND s.name = 'Vaccin Newcastle (IB+ND)';

-- ============================================================
-- 9. SALES (ventes)
-- Scenario 1 : vente payee (Lot 1 vendu)
-- Scenario 2 : vente payee (Lot 3 - partielle)
-- Scenario 3 : vente en attente paiement
-- Scenario 4 : vente annulee
-- ============================================================

-- Lot 1 SOLD - Vente complete - Client Casablanca - PAYEE
INSERT INTO sales (batch_id, client_id, quantity, unit_price, total_price, sale_date, payment_status, delivery_address, notes, recorded_by_id, created_at, updated_at)
SELECT b.id, c.id, 4800, 28.00, 134400.00, '2026-02-08', 'Paid', '12 Rue Hassan II, Casablanca', 'Vente complete du lot 001 - paiement recu', adm.id, NOW(), NOW()
FROM batches b, users c, users adm
WHERE b.batch_number = 'LOT-2026-001'
  AND c.email = 'client1@djajbladi.ma'
  AND adm.email = 'admin@djajbladi.ma';

-- Lot 3 READY_FOR_SALE - Vente partielle - Client Rabat - PAYEE
INSERT INTO sales (batch_id, client_id, quantity, unit_price, total_price, sale_date, payment_status, delivery_address, notes, recorded_by_id, created_at, updated_at)
SELECT b.id, c.id, 1500, 28.50, 42750.00, '2026-03-25', 'Paid', '45 Avenue Mohammed V, Rabat', 'Premiere livraison - client satisfait', adm.id, NOW(), NOW()
FROM batches b, users c, users adm
WHERE b.batch_number = 'LOT-2026-003'
  AND c.email = 'client2@djajbladi.ma'
  AND adm.email = 'admin@djajbladi.ma';

-- Lot 3 - Deuxieme vente - En attente paiement - Client Marrakech
INSERT INTO sales (batch_id, client_id, quantity, unit_price, total_price, sale_date, payment_status, delivery_address, notes, recorded_by_id, created_at, updated_at)
SELECT b.id, c.id, 2000, 28.50, 57000.00, '2026-03-27', 'Pending', '8 Derb Sidi Bouloukat, Marrakech', 'Livraison planifiee demain - paiement a la livraison', adm.id, NOW(), NOW()
FROM batches b, users c, users adm
WHERE b.batch_number = 'LOT-2026-003'
  AND c.email = 'client3@djajbladi.ma'
  AND adm.email = 'admin@djajbladi.ma';

-- Lot 2 Completed - Vente annulee
INSERT INTO sales (batch_id, client_id, quantity, unit_price, total_price, sale_date, payment_status, delivery_address, notes, recorded_by_id, created_at, updated_at)
SELECT b.id, c.id, 500, 27.50, 13750.00, '2026-03-20', 'Cancelled', '7 Rue Ibn Rochd, Fes', 'Commande annulee par le client - camion en panne', adm.id, NOW(), NOW()
FROM batches b, users c, users adm
WHERE b.batch_number = 'LOT-2026-002'
  AND c.email = 'client1@djajbladi.ma'
  AND adm.email = 'admin@djajbladi.ma';

-- Lot 2 - Vente en attente - Client Casablanca
INSERT INTO sales (batch_id, client_id, quantity, unit_price, total_price, sale_date, payment_status, delivery_address, notes, recorded_by_id, created_at, updated_at)
SELECT b.id, c.id, 3220, 27.50, 88550.00, '2026-03-28', 'Pending', '22 Boulevard Anfa, Casablanca', 'Negociation en cours pour livraison groupee', adm.id, NOW(), NOW()
FROM batches b, users c, users adm
WHERE b.batch_number = 'LOT-2026-002'
  AND c.email = 'client2@djajbladi.ma'
  AND adm.email = 'admin@djajbladi.ma';
