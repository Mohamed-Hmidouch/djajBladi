-- V12__seed_dev_data.sql
-- Donnees realistes de developpement pour la ferme avicole Djaj Bladi
-- Principe: faits bruts en DB, services appliquent la logique metier
-- Mot de passe pour tous les users: Admin1234!
-- Hash BCrypt 12 rounds: $2a$12$1K5pMFooBXsJFnQMdhlXE.LCj9s0pPPqNZ3o1Ymu7XMd5e6VKDMfW

-- -----------------------------------------------------------------------
-- 1. USERS
-- -----------------------------------------------------------------------
INSERT INTO users (full_name, email, password_hash, phone_number, role, is_active, city, created_at, updated_at, last_login_at) VALUES
('Mohammed Hmidouch',   'admin@djajbladi.ma',     '$2a$12$1K5pMFooBXsJFnQMdhlXE.LCj9s0pPPqNZ3o1Ymu7XMd5e6VKDMfW', '0661234567', 'Admin',       true, 'Casablanca', NOW() - INTERVAL '180 days', NOW(), NOW() - INTERVAL '1 hour'),
('Karim Bensalem',      'karim@djajbladi.ma',     '$2a$12$1K5pMFooBXsJFnQMdhlXE.LCj9s0pPPqNZ3o1Ymu7XMd5e6VKDMfW', '0662345678', 'Ouvrier',     true, 'Meknes',     NOW() - INTERVAL '150 days', NOW(), NOW() - INTERVAL '3 hours'),
('Fatima Zahra Idrissi','fatima@djajbladi.ma',    '$2a$12$1K5pMFooBXsJFnQMdhlXE.LCj9s0pPPqNZ3o1Ymu7XMd5e6VKDMfW', '0663456789', 'Ouvrier',     true, 'Fes',        NOW() - INTERVAL '120 days', NOW(), NOW() - INTERVAL '1 day'),
('Dr. Youssef Alami',   'vet1@djajbladi.ma',      '$2a$12$1K5pMFooBXsJFnQMdhlXE.LCj9s0pPPqNZ3o1Ymu7XMd5e6VKDMfW', '0664567890', 'Veterinaire', true, 'Rabat',      NOW() - INTERVAL '160 days', NOW(), NOW() - INTERVAL '2 days'),
('Dr. Soukaina Tahiri', 'vet2@djajbladi.ma',      '$2a$12$1K5pMFooBXsJFnQMdhlXE.LCj9s0pPPqNZ3o1Ymu7XMd5e6VKDMfW', '0665678901', 'Veterinaire', true, 'Meknes',     NOW() - INTERVAL '90 days',  NOW(), NOW() - INTERVAL '5 days'),
('Hassan Benali',       'client1@djajbladi.ma',   '$2a$12$1K5pMFooBXsJFnQMdhlXE.LCj9s0pPPqNZ3o1Ymu7XMd5e6VKDMfW', '0666789012', 'Client',      true, 'Marrakech',  NOW() - INTERVAL '60 days',  NOW(), NOW() - INTERVAL '7 days'),
('Aicha Moussaoui',     'client2@djajbladi.ma',   '$2a$12$1K5pMFooBXsJFnQMdhlXE.LCj9s0pPPqNZ3o1Ymu7XMd5e6VKDMfW', '0667890123', 'Client',      true, 'Agadir',     NOW() - INTERVAL '45 days',  NOW(), NOW() - INTERVAL '10 days');

-- -----------------------------------------------------------------------
-- 2. BUILDINGS
-- -----------------------------------------------------------------------
INSERT INTO buildings (name, max_capacity, created_by_id, created_at, updated_at)
SELECT 'Batiment A', 12000, (SELECT id FROM users WHERE email='admin@djajbladi.ma'), NOW() - INTERVAL '180 days', NOW()
UNION ALL
SELECT 'Batiment B', 10000, (SELECT id FROM users WHERE email='admin@djajbladi.ma'), NOW() - INTERVAL '180 days', NOW()
UNION ALL
SELECT 'Batiment C',  8000, (SELECT id FROM users WHERE email='admin@djajbladi.ma'), NOW() - INTERVAL '90 days',  NOW()
UNION ALL
SELECT 'Batiment D',  5000, (SELECT id FROM users WHERE email='admin@djajbladi.ma'), NOW() - INTERVAL '30 days',  NOW();

-- -----------------------------------------------------------------------
-- 3. BATCHES
-- created_by_id uses subqueries to be ID-independent
-- -----------------------------------------------------------------------
INSERT INTO batches (batch_number, chicken_count, arrival_date, status, notes, created_by_id, building_id, strain, purchase_price, created_at, updated_at)
SELECT 'LOT-2024-001', 10000, NOW()::DATE - INTERVAL '90 days',  'Completed', 'Lot termine avec succes. Taux de mortalite dans les normes.',           (SELECT id FROM users WHERE email='admin@djajbladi.ma'), (SELECT id FROM buildings WHERE name='Batiment A'), 'Ross 308',  3.20, NOW() - INTERVAL '90 days',  NOW()
UNION ALL
SELECT 'LOT-2024-002',  8500, NOW()::DATE - INTERVAL '75 days',  'Completed', 'Lot termine. Quelques cas respiratoires traites en semaine 3.',         (SELECT id FROM users WHERE email='admin@djajbladi.ma'), (SELECT id FROM buildings WHERE name='Batiment B'), 'Cobb 500',  3.10, NOW() - INTERVAL '75 days',  NOW()
UNION ALL
SELECT 'LOT-2024-003',  9500, NOW()::DATE - INTERVAL '38 days',  'Active',    'Lot en cours. Phase de croissance. Alimentation de croissance active.', (SELECT id FROM users WHERE email='karim@djajbladi.ma'), (SELECT id FROM buildings WHERE name='Batiment A'), 'Ross 308',  3.25, NOW() - INTERVAL '38 days',  NOW()
UNION ALL
SELECT 'LOT-2024-004',  7800, NOW()::DATE - INTERVAL '28 days',  'Active',    'Lot en cours. Bonne progression. RAS.',                                 (SELECT id FROM users WHERE email='karim@djajbladi.ma'), (SELECT id FROM buildings WHERE name='Batiment B'), 'Cobb 500',  3.15, NOW() - INTERVAL '28 days',  NOW()
UNION ALL
SELECT 'LOT-2025-001',  6000, NOW()::DATE - INTERVAL '15 days',  'Active',    'Nouveau lot arrive. Phase demarrage.',                                  (SELECT id FROM users WHERE email='karim@djajbladi.ma'), (SELECT id FROM buildings WHERE name='Batiment C'), 'Hubbard',   3.30, NOW() - INTERVAL '15 days',  NOW()
UNION ALL
SELECT 'LOT-2025-002',  4500, NOW()::DATE - INTERVAL '7 days',   'Active',    'Lot recemment arrive. Adaptation en cours.',                            (SELECT id FROM users WHERE email='karim@djajbladi.ma'), (SELECT id FROM buildings WHERE name='Batiment D'), 'Ross 308',  3.20, NOW() - INTERVAL  '7 days',  NOW();

-- -----------------------------------------------------------------------
-- 4. FEEDING RECORDS
-- Courbe realiste par phase:
--   J1-J7   : 38-55 kg/jour (demarrage)
--   J8-J21  : 60-110 kg/jour (croissance 1)
--   J22-J35 : 115-160 kg/jour (croissance 2)
--   J36-J42 : 165-180 kg/jour (finition)
-- Batch 3: 38 jours d'historique  |  Batch 4: 28 jours  |  Batch 5: 15 jours  |  Batch 6: 7 jours
-- Batches 1 et 2 (Completed): 42 jours complets
-- -----------------------------------------------------------------------

-- Feeding records use DO block to resolve IDs once
DO $$
DECLARE
  v_admin     BIGINT := (SELECT id FROM users WHERE email = 'admin@djajbladi.ma');
  v_ouvrier1  BIGINT := (SELECT id FROM users WHERE email = 'karim@djajbladi.ma');
  v_ouvrier2  BIGINT := (SELECT id FROM users WHERE email = 'fatima@djajbladi.ma');
  v_vet1      BIGINT := (SELECT id FROM users WHERE email = 'vet1@djajbladi.ma');
  v_vet2      BIGINT := (SELECT id FROM users WHERE email = 'vet2@djajbladi.ma');
  v_client1   BIGINT := (SELECT id FROM users WHERE email = 'client1@djajbladi.ma');
  v_client2   BIGINT := (SELECT id FROM users WHERE email = 'client2@djajbladi.ma');
  v_bat_a     BIGINT := (SELECT id FROM buildings WHERE name = 'Batiment A');
  v_bat_b     BIGINT := (SELECT id FROM buildings WHERE name = 'Batiment B');
  v_bat_c     BIGINT := (SELECT id FROM buildings WHERE name = 'Batiment C');
  v_bat_d     BIGINT := (SELECT id FROM buildings WHERE name = 'Batiment D');
  v_b1        BIGINT := (SELECT id FROM batches WHERE batch_number = 'LOT-2024-001');
  v_b2        BIGINT := (SELECT id FROM batches WHERE batch_number = 'LOT-2024-002');
  v_b3        BIGINT := (SELECT id FROM batches WHERE batch_number = 'LOT-2024-003');
  v_b4        BIGINT := (SELECT id FROM batches WHERE batch_number = 'LOT-2024-004');
  v_b5        BIGINT := (SELECT id FROM batches WHERE batch_number = 'LOT-2025-001');
  v_b6        BIGINT := (SELECT id FROM batches WHERE batch_number = 'LOT-2025-002');
BEGIN

-- Batch 1 (LOT-2024-001, Completed, 10000 poules) - 42 jours complets
INSERT INTO feeding_records (batch_id, feed_type, quantity, feeding_date, notes, recorded_by_id, created_at, updated_at) VALUES
(v_b1, 'Demarrage',   38.50, NOW()::DATE - INTERVAL '132 days', NULL, v_ouvrier1, NOW() - INTERVAL '132 days', NOW() - INTERVAL '132 days'),
(1, 'Demarrage',   40.00, NOW()::DATE - INTERVAL '131 days', NULL, 2, NOW() - INTERVAL '131 days', NOW() - INTERVAL '131 days'),
(1, 'Demarrage',   42.50, NOW()::DATE - INTERVAL '130 days', NULL, 2, NOW() - INTERVAL '130 days', NOW() - INTERVAL '130 days'),
(1, 'Demarrage',   43.00, NOW()::DATE - INTERVAL '129 days', NULL, 2, NOW() - INTERVAL '129 days', NOW() - INTERVAL '129 days'),
(1, 'Demarrage',   44.50, NOW()::DATE - INTERVAL '128 days', NULL, 2, NOW() - INTERVAL '128 days', NOW() - INTERVAL '128 days'),
(1, 'Demarrage',   46.00, NOW()::DATE - INTERVAL '127 days', NULL, 2, NOW() - INTERVAL '127 days', NOW() - INTERVAL '127 days'),
(1, 'Demarrage',   48.00, NOW()::DATE - INTERVAL '126 days', NULL, 2, NOW() - INTERVAL '126 days', NOW() - INTERVAL '126 days'),
(1, 'Croissance',  60.00, NOW()::DATE - INTERVAL '125 days', NULL, 2, NOW() - INTERVAL '125 days', NOW() - INTERVAL '125 days'),
(1, 'Croissance',  65.00, NOW()::DATE - INTERVAL '124 days', NULL, 2, NOW() - INTERVAL '124 days', NOW() - INTERVAL '124 days'),
(1, 'Croissance',  70.00, NOW()::DATE - INTERVAL '123 days', NULL, 2, NOW() - INTERVAL '123 days', NOW() - INTERVAL '123 days'),
(1, 'Croissance',  75.00, NOW()::DATE - INTERVAL '122 days', NULL, 2, NOW() - INTERVAL '122 days', NOW() - INTERVAL '122 days'),
(1, 'Croissance',  80.00, NOW()::DATE - INTERVAL '121 days', NULL, 2, NOW() - INTERVAL '121 days', NOW() - INTERVAL '121 days'),
(1, 'Croissance',  85.00, NOW()::DATE - INTERVAL '120 days', NULL, 2, NOW() - INTERVAL '120 days', NOW() - INTERVAL '120 days'),
(1, 'Croissance',  90.00, NOW()::DATE - INTERVAL '119 days', NULL, 2, NOW() - INTERVAL '119 days', NOW() - INTERVAL '119 days'),
(1, 'Croissance',  92.00, NOW()::DATE - INTERVAL '118 days', NULL, 2, NOW() - INTERVAL '118 days', NOW() - INTERVAL '118 days'),
(1, 'Croissance',  95.00, NOW()::DATE - INTERVAL '117 days', NULL, 2, NOW() - INTERVAL '117 days', NOW() - INTERVAL '117 days'),
(1, 'Croissance',  97.00, NOW()::DATE - INTERVAL '116 days', NULL, 2, NOW() - INTERVAL '116 days', NOW() - INTERVAL '116 days'),
(1, 'Croissance', 100.00, NOW()::DATE - INTERVAL '115 days', NULL, 2, NOW() - INTERVAL '115 days', NOW() - INTERVAL '115 days'),
(1, 'Croissance', 102.00, NOW()::DATE - INTERVAL '114 days', NULL, 2, NOW() - INTERVAL '114 days', NOW() - INTERVAL '114 days'),
(1, 'Croissance', 105.00, NOW()::DATE - INTERVAL '113 days', NULL, 2, NOW() - INTERVAL '113 days', NOW() - INTERVAL '113 days'),
(1, 'Croissance', 108.00, NOW()::DATE - INTERVAL '112 days', NULL, 2, NOW() - INTERVAL '112 days', NOW() - INTERVAL '112 days'),
(1, 'Finition',   115.00, NOW()::DATE - INTERVAL '111 days', NULL, 2, NOW() - INTERVAL '111 days', NOW() - INTERVAL '111 days'),
(1, 'Finition',   118.00, NOW()::DATE - INTERVAL '110 days', NULL, 2, NOW() - INTERVAL '110 days', NOW() - INTERVAL '110 days'),
(1, 'Finition',   122.00, NOW()::DATE - INTERVAL '109 days', NULL, 2, NOW() - INTERVAL '109 days', NOW() - INTERVAL '109 days'),
(1, 'Finition',   125.00, NOW()::DATE - INTERVAL '108 days', NULL, 2, NOW() - INTERVAL '108 days', NOW() - INTERVAL '108 days'),
(1, 'Finition',   128.00, NOW()::DATE - INTERVAL '107 days', NULL, 2, NOW() - INTERVAL '107 days', NOW() - INTERVAL '107 days'),
(1, 'Finition',   130.00, NOW()::DATE - INTERVAL '106 days', NULL, 2, NOW() - INTERVAL '106 days', NOW() - INTERVAL '106 days'),
(1, 'Finition',   132.00, NOW()::DATE - INTERVAL '105 days', NULL, 2, NOW() - INTERVAL '105 days', NOW() - INTERVAL '105 days'),
(1, 'Finition',   135.00, NOW()::DATE - INTERVAL '104 days', NULL, 2, NOW() - INTERVAL '104 days', NOW() - INTERVAL '104 days'),
(1, 'Finition',   138.00, NOW()::DATE - INTERVAL '103 days', NULL, 2, NOW() - INTERVAL '103 days', NOW() - INTERVAL '103 days'),
(1, 'Finition',   140.00, NOW()::DATE - INTERVAL '102 days', NULL, 2, NOW() - INTERVAL '102 days', NOW() - INTERVAL '102 days'),
(1, 'Finition',   142.00, NOW()::DATE - INTERVAL '101 days', NULL, 2, NOW() - INTERVAL '101 days', NOW() - INTERVAL '101 days'),
(1, 'Finition',   145.00, NOW()::DATE - INTERVAL '100 days', NULL, 2, NOW() - INTERVAL '100 days', NOW() - INTERVAL '100 days'),
(1, 'Finition',   148.00, NOW()::DATE - INTERVAL  '99 days', NULL, 2, NOW() - INTERVAL  '99 days', NOW() - INTERVAL  '99 days'),
(1, 'Finition',   150.00, NOW()::DATE - INTERVAL  '98 days', NULL, 2, NOW() - INTERVAL  '98 days', NOW() - INTERVAL  '98 days'),
(1, 'Finition',   152.00, NOW()::DATE - INTERVAL  '97 days', NULL, 2, NOW() - INTERVAL  '97 days', NOW() - INTERVAL  '97 days'),
(1, 'Finition',   155.00, NOW()::DATE - INTERVAL  '96 days', NULL, 2, NOW() - INTERVAL  '96 days', NOW() - INTERVAL  '96 days'),
(1, 'Finition',   158.00, NOW()::DATE - INTERVAL  '95 days', NULL, 2, NOW() - INTERVAL  '95 days', NOW() - INTERVAL  '95 days'),
(1, 'Finition',   160.00, NOW()::DATE - INTERVAL  '94 days', NULL, 2, NOW() - INTERVAL  '94 days', NOW() - INTERVAL  '94 days'),
(1, 'Finition',   162.00, NOW()::DATE - INTERVAL  '93 days', NULL, 2, NOW() - INTERVAL  '93 days', NOW() - INTERVAL  '93 days'),
(1, 'Finition',   165.00, NOW()::DATE - INTERVAL  '92 days', NULL, 2, NOW() - INTERVAL  '92 days', NOW() - INTERVAL  '92 days'),
(1, 'Finition',   168.00, NOW()::DATE - INTERVAL  '91 days', NULL, 2, NOW() - INTERVAL  '91 days', NOW() - INTERVAL  '91 days');

-- Batch 2 (LOT-2024-002, Completed, 8500 poules) - 42 jours complets
INSERT INTO feeding_records (batch_id, feed_type, quantity, feeding_date, notes, recorded_by_id, created_at, updated_at) VALUES
(2, 'Demarrage',   32.00, NOW()::DATE - INTERVAL '117 days', NULL, 3, NOW() - INTERVAL '117 days', NOW() - INTERVAL '117 days'),
(2, 'Demarrage',   33.50, NOW()::DATE - INTERVAL '116 days', NULL, 3, NOW() - INTERVAL '116 days', NOW() - INTERVAL '116 days'),
(2, 'Demarrage',   35.00, NOW()::DATE - INTERVAL '115 days', NULL, 3, NOW() - INTERVAL '115 days', NOW() - INTERVAL '115 days'),
(2, 'Demarrage',   36.00, NOW()::DATE - INTERVAL '114 days', NULL, 3, NOW() - INTERVAL '114 days', NOW() - INTERVAL '114 days'),
(2, 'Demarrage',   37.50, NOW()::DATE - INTERVAL '113 days', NULL, 3, NOW() - INTERVAL '113 days', NOW() - INTERVAL '113 days'),
(2, 'Demarrage',   39.00, NOW()::DATE - INTERVAL '112 days', NULL, 3, NOW() - INTERVAL '112 days', NOW() - INTERVAL '112 days'),
(2, 'Demarrage',   41.00, NOW()::DATE - INTERVAL '111 days', NULL, 3, NOW() - INTERVAL '111 days', NOW() - INTERVAL '111 days'),
(2, 'Croissance',  52.00, NOW()::DATE - INTERVAL '110 days', NULL, 3, NOW() - INTERVAL '110 days', NOW() - INTERVAL '110 days'),
(2, 'Croissance',  56.00, NOW()::DATE - INTERVAL '109 days', NULL, 3, NOW() - INTERVAL '109 days', NOW() - INTERVAL '109 days'),
(2, 'Croissance',  60.00, NOW()::DATE - INTERVAL '108 days', NULL, 3, NOW() - INTERVAL '108 days', NOW() - INTERVAL '108 days'),
(2, 'Croissance',  64.00, NOW()::DATE - INTERVAL '107 days', NULL, 3, NOW() - INTERVAL '107 days', NOW() - INTERVAL '107 days'),
(2, 'Croissance',  68.00, NOW()::DATE - INTERVAL '106 days', NULL, 3, NOW() - INTERVAL '106 days', NOW() - INTERVAL '106 days'),
(2, 'Croissance',  72.00, NOW()::DATE - INTERVAL '105 days', NULL, 3, NOW() - INTERVAL '105 days', NOW() - INTERVAL '105 days'),
(2, 'Croissance',  76.00, NOW()::DATE - INTERVAL '104 days', NULL, 3, NOW() - INTERVAL '104 days', NOW() - INTERVAL '104 days'),
(2, 'Croissance',  80.00, NOW()::DATE - INTERVAL '103 days', NULL, 3, NOW() - INTERVAL '103 days', NOW() - INTERVAL '103 days'),
(2, 'Croissance',  83.00, NOW()::DATE - INTERVAL '102 days', NULL, 3, NOW() - INTERVAL '102 days', NOW() - INTERVAL '102 days'),
(2, 'Croissance',  86.00, NOW()::DATE - INTERVAL '101 days', NULL, 3, NOW() - INTERVAL '101 days', NOW() - INTERVAL '101 days'),
(2, 'Croissance',  89.00, NOW()::DATE - INTERVAL '100 days', NULL, 3, NOW() - INTERVAL '100 days', NOW() - INTERVAL '100 days'),
(2, 'Croissance',  91.00, NOW()::DATE - INTERVAL  '99 days', NULL, 3, NOW() - INTERVAL  '99 days', NOW() - INTERVAL  '99 days'),
(2, 'Croissance',  93.00, NOW()::DATE - INTERVAL  '98 days', NULL, 3, NOW() - INTERVAL  '98 days', NOW() - INTERVAL  '98 days'),
(2, 'Croissance',  95.00, NOW()::DATE - INTERVAL  '97 days', NULL, 3, NOW() - INTERVAL  '97 days', NOW() - INTERVAL  '97 days'),
(2, 'Finition',   100.00, NOW()::DATE - INTERVAL  '96 days', NULL, 3, NOW() - INTERVAL  '96 days', NOW() - INTERVAL  '96 days'),
(2, 'Finition',   103.00, NOW()::DATE - INTERVAL  '95 days', NULL, 3, NOW() - INTERVAL  '95 days', NOW() - INTERVAL  '95 days'),
(2, 'Finition',   106.00, NOW()::DATE - INTERVAL  '94 days', NULL, 3, NOW() - INTERVAL  '94 days', NOW() - INTERVAL  '94 days'),
(2, 'Finition',   109.00, NOW()::DATE - INTERVAL  '93 days', NULL, 3, NOW() - INTERVAL  '93 days', NOW() - INTERVAL  '93 days'),
(2, 'Finition',   112.00, NOW()::DATE - INTERVAL  '92 days', NULL, 3, NOW() - INTERVAL  '92 days', NOW() - INTERVAL  '92 days'),
(2, 'Finition',   115.00, NOW()::DATE - INTERVAL  '91 days', NULL, 3, NOW() - INTERVAL  '91 days', NOW() - INTERVAL  '91 days'),
(2, 'Finition',   118.00, NOW()::DATE - INTERVAL  '90 days', NULL, 3, NOW() - INTERVAL  '90 days', NOW() - INTERVAL  '90 days'),
(2, 'Finition',   120.00, NOW()::DATE - INTERVAL  '89 days', NULL, 3, NOW() - INTERVAL  '89 days', NOW() - INTERVAL  '89 days'),
(2, 'Finition',   122.00, NOW()::DATE - INTERVAL  '88 days', NULL, 3, NOW() - INTERVAL  '88 days', NOW() - INTERVAL  '88 days'),
(2, 'Finition',   124.00, NOW()::DATE - INTERVAL  '87 days', NULL, 3, NOW() - INTERVAL  '87 days', NOW() - INTERVAL  '87 days'),
(2, 'Finition',   126.00, NOW()::DATE - INTERVAL  '86 days', NULL, 3, NOW() - INTERVAL  '86 days', NOW() - INTERVAL  '86 days'),
(2, 'Finition',   128.00, NOW()::DATE - INTERVAL  '85 days', NULL, 3, NOW() - INTERVAL  '85 days', NOW() - INTERVAL  '85 days'),
(2, 'Finition',   130.00, NOW()::DATE - INTERVAL  '84 days', NULL, 3, NOW() - INTERVAL  '84 days', NOW() - INTERVAL  '84 days'),
(2, 'Finition',   132.00, NOW()::DATE - INTERVAL  '83 days', NULL, 3, NOW() - INTERVAL  '83 days', NOW() - INTERVAL  '83 days'),
(2, 'Finition',   134.00, NOW()::DATE - INTERVAL  '82 days', NULL, 3, NOW() - INTERVAL  '82 days', NOW() - INTERVAL  '82 days'),
(2, 'Finition',   136.00, NOW()::DATE - INTERVAL  '81 days', NULL, 3, NOW() - INTERVAL  '81 days', NOW() - INTERVAL  '81 days'),
(2, 'Finition',   138.00, NOW()::DATE - INTERVAL  '80 days', NULL, 3, NOW() - INTERVAL  '80 days', NOW() - INTERVAL  '80 days'),
(2, 'Finition',   140.00, NOW()::DATE - INTERVAL  '79 days', NULL, 3, NOW() - INTERVAL  '79 days', NOW() - INTERVAL  '79 days'),
(2, 'Finition',   142.00, NOW()::DATE - INTERVAL  '78 days', NULL, 3, NOW() - INTERVAL  '78 days', NOW() - INTERVAL  '78 days'),
(2, 'Finition',   144.00, NOW()::DATE - INTERVAL  '77 days', NULL, 3, NOW() - INTERVAL  '77 days', NOW() - INTERVAL  '77 days'),
(2, 'Finition',   145.00, NOW()::DATE - INTERVAL  '76 days', NULL, 3, NOW() - INTERVAL  '76 days', NOW() - INTERVAL  '76 days');

-- Batch 3 (LOT-2024-003, Active, 9500 poules) - 38 jours (J1-J38)
INSERT INTO feeding_records (batch_id, feed_type, quantity, feeding_date, notes, recorded_by_id, created_at, updated_at) VALUES
(3, 'Demarrage',   36.00, NOW()::DATE - INTERVAL '38 days', NULL, 2, NOW() - INTERVAL '38 days', NOW() - INTERVAL '38 days'),
(3, 'Demarrage',   37.50, NOW()::DATE - INTERVAL '37 days', NULL, 2, NOW() - INTERVAL '37 days', NOW() - INTERVAL '37 days'),
(3, 'Demarrage',   39.00, NOW()::DATE - INTERVAL '36 days', NULL, 2, NOW() - INTERVAL '36 days', NOW() - INTERVAL '36 days'),
(3, 'Demarrage',   41.00, NOW()::DATE - INTERVAL '35 days', NULL, 2, NOW() - INTERVAL '35 days', NOW() - INTERVAL '35 days'),
(3, 'Demarrage',   43.00, NOW()::DATE - INTERVAL '34 days', NULL, 2, NOW() - INTERVAL '34 days', NOW() - INTERVAL '34 days'),
(3, 'Demarrage',   45.00, NOW()::DATE - INTERVAL '33 days', NULL, 2, NOW() - INTERVAL '33 days', NOW() - INTERVAL '33 days'),
(3, 'Demarrage',   47.00, NOW()::DATE - INTERVAL '32 days', NULL, 2, NOW() - INTERVAL '32 days', NOW() - INTERVAL '32 days'),
(3, 'Croissance',  58.00, NOW()::DATE - INTERVAL '31 days', NULL, 2, NOW() - INTERVAL '31 days', NOW() - INTERVAL '31 days'),
(3, 'Croissance',  62.00, NOW()::DATE - INTERVAL '30 days', NULL, 2, NOW() - INTERVAL '30 days', NOW() - INTERVAL '30 days'),
(3, 'Croissance',  66.00, NOW()::DATE - INTERVAL '29 days', NULL, 2, NOW() - INTERVAL '29 days', NOW() - INTERVAL '29 days'),
(3, 'Croissance',  70.00, NOW()::DATE - INTERVAL '28 days', NULL, 2, NOW() - INTERVAL '28 days', NOW() - INTERVAL '28 days'),
(3, 'Croissance',  74.00, NOW()::DATE - INTERVAL '27 days', NULL, 2, NOW() - INTERVAL '27 days', NOW() - INTERVAL '27 days'),
(3, 'Croissance',  78.00, NOW()::DATE - INTERVAL '26 days', NULL, 2, NOW() - INTERVAL '26 days', NOW() - INTERVAL '26 days'),
(3, 'Croissance',  82.00, NOW()::DATE - INTERVAL '25 days', NULL, 2, NOW() - INTERVAL '25 days', NOW() - INTERVAL '25 days'),
(3, 'Croissance',  85.00, NOW()::DATE - INTERVAL '24 days', NULL, 2, NOW() - INTERVAL '24 days', NOW() - INTERVAL '24 days'),
(3, 'Croissance',  88.00, NOW()::DATE - INTERVAL '23 days', NULL, 2, NOW() - INTERVAL '23 days', NOW() - INTERVAL '23 days'),
(3, 'Croissance',  91.00, NOW()::DATE - INTERVAL '22 days', NULL, 2, NOW() - INTERVAL '22 days', NOW() - INTERVAL '22 days'),
(3, 'Croissance',  94.00, NOW()::DATE - INTERVAL '21 days', NULL, 2, NOW() - INTERVAL '21 days', NOW() - INTERVAL '21 days'),
(3, 'Croissance',  97.00, NOW()::DATE - INTERVAL '20 days', NULL, 2, NOW() - INTERVAL '20 days', NOW() - INTERVAL '20 days'),
(3, 'Croissance', 100.00, NOW()::DATE - INTERVAL '19 days', NULL, 2, NOW() - INTERVAL '19 days', NOW() - INTERVAL '19 days'),
(3, 'Croissance', 103.00, NOW()::DATE - INTERVAL '18 days', NULL, 2, NOW() - INTERVAL '18 days', NOW() - INTERVAL '18 days'),
(3, 'Finition',   112.00, NOW()::DATE - INTERVAL '17 days', NULL, 2, NOW() - INTERVAL '17 days', NOW() - INTERVAL '17 days'),
(3, 'Finition',   115.00, NOW()::DATE - INTERVAL '16 days', NULL, 2, NOW() - INTERVAL '16 days', NOW() - INTERVAL '16 days'),
(3, 'Finition',   118.00, NOW()::DATE - INTERVAL '15 days', NULL, 2, NOW() - INTERVAL '15 days', NOW() - INTERVAL '15 days'),
(3, 'Finition',   121.00, NOW()::DATE - INTERVAL '14 days', NULL, 2, NOW() - INTERVAL '14 days', NOW() - INTERVAL '14 days'),
(3, 'Finition',   124.00, NOW()::DATE - INTERVAL '13 days', NULL, 2, NOW() - INTERVAL '13 days', NOW() - INTERVAL '13 days'),
(3, 'Finition',   127.00, NOW()::DATE - INTERVAL '12 days', NULL, 2, NOW() - INTERVAL '12 days', NOW() - INTERVAL '12 days'),
(3, 'Finition',   130.00, NOW()::DATE - INTERVAL '11 days', NULL, 2, NOW() - INTERVAL '11 days', NOW() - INTERVAL '11 days'),
(3, 'Finition',   133.00, NOW()::DATE - INTERVAL '10 days', NULL, 2, NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days'),
(3, 'Finition',   136.00, NOW()::DATE - INTERVAL  '9 days', NULL, 2, NOW() - INTERVAL  '9 days', NOW() - INTERVAL  '9 days'),
(3, 'Finition',   139.00, NOW()::DATE - INTERVAL  '8 days', NULL, 2, NOW() - INTERVAL  '8 days', NOW() - INTERVAL  '8 days'),
(3, 'Finition',   142.00, NOW()::DATE - INTERVAL  '7 days', NULL, 2, NOW() - INTERVAL  '7 days', NOW() - INTERVAL  '7 days'),
(3, 'Finition',   145.00, NOW()::DATE - INTERVAL  '6 days', NULL, 2, NOW() - INTERVAL  '6 days', NOW() - INTERVAL  '6 days'),
(3, 'Finition',   148.00, NOW()::DATE - INTERVAL  '5 days', NULL, 2, NOW() - INTERVAL  '5 days', NOW() - INTERVAL  '5 days'),
(3, 'Finition',   150.00, NOW()::DATE - INTERVAL  '4 days', NULL, 2, NOW() - INTERVAL  '4 days', NOW() - INTERVAL  '4 days'),
(3, 'Finition',   152.00, NOW()::DATE - INTERVAL  '3 days', NULL, 2, NOW() - INTERVAL  '3 days', NOW() - INTERVAL  '3 days'),
(3, 'Finition',   154.00, NOW()::DATE - INTERVAL  '2 days', NULL, 2, NOW() - INTERVAL  '2 days', NOW() - INTERVAL  '2 days'),
(3, 'Finition',   156.00, NOW()::DATE - INTERVAL  '1 day',  NULL, 2, NOW() - INTERVAL  '1 day',  NOW() - INTERVAL  '1 day');

-- Batch 4 (LOT-2024-004, Active, 7800 poules) - 28 jours (J1-J28)
INSERT INTO feeding_records (batch_id, feed_type, quantity, feeding_date, notes, recorded_by_id, created_at, updated_at) VALUES
(4, 'Demarrage',  29.50, NOW()::DATE - INTERVAL '28 days', NULL, 3, NOW() - INTERVAL '28 days', NOW() - INTERVAL '28 days'),
(4, 'Demarrage',  31.00, NOW()::DATE - INTERVAL '27 days', NULL, 3, NOW() - INTERVAL '27 days', NOW() - INTERVAL '27 days'),
(4, 'Demarrage',  32.50, NOW()::DATE - INTERVAL '26 days', NULL, 3, NOW() - INTERVAL '26 days', NOW() - INTERVAL '26 days'),
(4, 'Demarrage',  34.00, NOW()::DATE - INTERVAL '25 days', NULL, 3, NOW() - INTERVAL '25 days', NOW() - INTERVAL '25 days'),
(4, 'Demarrage',  35.50, NOW()::DATE - INTERVAL '24 days', NULL, 3, NOW() - INTERVAL '24 days', NOW() - INTERVAL '24 days'),
(4, 'Demarrage',  37.00, NOW()::DATE - INTERVAL '23 days', NULL, 3, NOW() - INTERVAL '23 days', NOW() - INTERVAL '23 days'),
(4, 'Demarrage',  39.00, NOW()::DATE - INTERVAL '22 days', NULL, 3, NOW() - INTERVAL '22 days', NOW() - INTERVAL '22 days'),
(4, 'Croissance', 48.00, NOW()::DATE - INTERVAL '21 days', NULL, 3, NOW() - INTERVAL '21 days', NOW() - INTERVAL '21 days'),
(4, 'Croissance', 52.00, NOW()::DATE - INTERVAL '20 days', NULL, 3, NOW() - INTERVAL '20 days', NOW() - INTERVAL '20 days'),
(4, 'Croissance', 56.00, NOW()::DATE - INTERVAL '19 days', NULL, 3, NOW() - INTERVAL '19 days', NOW() - INTERVAL '19 days'),
(4, 'Croissance', 60.00, NOW()::DATE - INTERVAL '18 days', NULL, 3, NOW() - INTERVAL '18 days', NOW() - INTERVAL '18 days'),
(4, 'Croissance', 64.00, NOW()::DATE - INTERVAL '17 days', NULL, 3, NOW() - INTERVAL '17 days', NOW() - INTERVAL '17 days'),
(4, 'Croissance', 68.00, NOW()::DATE - INTERVAL '16 days', NULL, 3, NOW() - INTERVAL '16 days', NOW() - INTERVAL '16 days'),
(4, 'Croissance', 72.00, NOW()::DATE - INTERVAL '15 days', NULL, 3, NOW() - INTERVAL '15 days', NOW() - INTERVAL '15 days'),
(4, 'Croissance', 75.00, NOW()::DATE - INTERVAL '14 days', NULL, 3, NOW() - INTERVAL '14 days', NOW() - INTERVAL '14 days'),
(4, 'Croissance', 78.00, NOW()::DATE - INTERVAL '13 days', NULL, 3, NOW() - INTERVAL '13 days', NOW() - INTERVAL '13 days'),
(4, 'Croissance', 81.00, NOW()::DATE - INTERVAL '12 days', NULL, 3, NOW() - INTERVAL '12 days', NOW() - INTERVAL '12 days'),
(4, 'Croissance', 84.00, NOW()::DATE - INTERVAL '11 days', NULL, 3, NOW() - INTERVAL '11 days', NOW() - INTERVAL '11 days'),
(4, 'Croissance', 87.00, NOW()::DATE - INTERVAL '10 days', NULL, 3, NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days'),
(4, 'Croissance', 90.00, NOW()::DATE - INTERVAL  '9 days', NULL, 3, NOW() - INTERVAL  '9 days', NOW() - INTERVAL  '9 days'),
(4, 'Croissance', 93.00, NOW()::DATE - INTERVAL  '8 days', NULL, 3, NOW() - INTERVAL  '8 days', NOW() - INTERVAL  '8 days'),
(4, 'Finition',   98.00, NOW()::DATE - INTERVAL  '7 days', NULL, 3, NOW() - INTERVAL  '7 days', NOW() - INTERVAL  '7 days'),
(4, 'Finition',  102.00, NOW()::DATE - INTERVAL  '6 days', NULL, 3, NOW() - INTERVAL  '6 days', NOW() - INTERVAL  '6 days'),
(4, 'Finition',  106.00, NOW()::DATE - INTERVAL  '5 days', NULL, 3, NOW() - INTERVAL  '5 days', NOW() - INTERVAL  '5 days'),
(4, 'Finition',  110.00, NOW()::DATE - INTERVAL  '4 days', NULL, 3, NOW() - INTERVAL  '4 days', NOW() - INTERVAL  '4 days'),
(4, 'Finition',  114.00, NOW()::DATE - INTERVAL  '3 days', NULL, 3, NOW() - INTERVAL  '3 days', NOW() - INTERVAL  '3 days'),
(4, 'Finition',  118.00, NOW()::DATE - INTERVAL  '2 days', NULL, 3, NOW() - INTERVAL  '2 days', NOW() - INTERVAL  '2 days'),
(4, 'Finition',  122.00, NOW()::DATE - INTERVAL  '1 day',  NULL, 3, NOW() - INTERVAL  '1 day',  NOW() - INTERVAL  '1 day');

-- Batch 5 (LOT-2025-001, Active, 6000 poules) - 15 jours (J1-J15)
INSERT INTO feeding_records (batch_id, feed_type, quantity, feeding_date, notes, recorded_by_id, created_at, updated_at) VALUES
(5, 'Demarrage', 22.50, NOW()::DATE - INTERVAL '15 days', NULL, 2, NOW() - INTERVAL '15 days', NOW() - INTERVAL '15 days'),
(5, 'Demarrage', 23.50, NOW()::DATE - INTERVAL '14 days', NULL, 2, NOW() - INTERVAL '14 days', NOW() - INTERVAL '14 days'),
(5, 'Demarrage', 24.50, NOW()::DATE - INTERVAL '13 days', NULL, 2, NOW() - INTERVAL '13 days', NOW() - INTERVAL '13 days'),
(5, 'Demarrage', 25.50, NOW()::DATE - INTERVAL '12 days', NULL, 2, NOW() - INTERVAL '12 days', NOW() - INTERVAL '12 days'),
(5, 'Demarrage', 26.50, NOW()::DATE - INTERVAL '11 days', NULL, 2, NOW() - INTERVAL '11 days', NOW() - INTERVAL '11 days'),
(5, 'Demarrage', 27.50, NOW()::DATE - INTERVAL '10 days', NULL, 2, NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days'),
(5, 'Demarrage', 29.00, NOW()::DATE - INTERVAL  '9 days', NULL, 2, NOW() - INTERVAL  '9 days', NOW() - INTERVAL  '9 days'),
(5, 'Croissance',36.00, NOW()::DATE - INTERVAL  '8 days', NULL, 2, NOW() - INTERVAL  '8 days', NOW() - INTERVAL  '8 days'),
(5, 'Croissance',39.00, NOW()::DATE - INTERVAL  '7 days', NULL, 2, NOW() - INTERVAL  '7 days', NOW() - INTERVAL  '7 days'),
(5, 'Croissance',42.00, NOW()::DATE - INTERVAL  '6 days', NULL, 2, NOW() - INTERVAL  '6 days', NOW() - INTERVAL  '6 days'),
(5, 'Croissance',45.00, NOW()::DATE - INTERVAL  '5 days', NULL, 2, NOW() - INTERVAL  '5 days', NOW() - INTERVAL  '5 days'),
(5, 'Croissance',48.00, NOW()::DATE - INTERVAL  '4 days', NULL, 2, NOW() - INTERVAL  '4 days', NOW() - INTERVAL  '4 days'),
(5, 'Croissance',51.00, NOW()::DATE - INTERVAL  '3 days', NULL, 2, NOW() - INTERVAL  '3 days', NOW() - INTERVAL  '3 days'),
(5, 'Croissance',54.00, NOW()::DATE - INTERVAL  '2 days', NULL, 2, NOW() - INTERVAL  '2 days', NOW() - INTERVAL  '2 days'),
(5, 'Croissance',57.00, NOW()::DATE - INTERVAL  '1 day',  NULL, 2, NOW() - INTERVAL  '1 day',  NOW() - INTERVAL  '1 day');

-- Batch 6 (LOT-2025-002, Active, 4500 poules) - 7 jours (J1-J7)
INSERT INTO feeding_records (batch_id, feed_type, quantity, feeding_date, notes, recorded_by_id, created_at, updated_at) VALUES
(6, 'Demarrage', 17.00, NOW()::DATE - INTERVAL '7 days', NULL, 3, NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days'),
(6, 'Demarrage', 17.80, NOW()::DATE - INTERVAL '6 days', NULL, 3, NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days'),
(6, 'Demarrage', 18.60, NOW()::DATE - INTERVAL '5 days', NULL, 3, NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days'),
(6, 'Demarrage', 19.40, NOW()::DATE - INTERVAL '4 days', NULL, 3, NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),
(6, 'Demarrage', 20.20, NOW()::DATE - INTERVAL '3 days', NULL, 3, NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days'),
(6, 'Demarrage', 21.00, NOW()::DATE - INTERVAL '2 days', NULL, 3, NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
(6, 'Demarrage', 21.80, NOW()::DATE - INTERVAL '1 day',  NULL, 3, NOW() - INTERVAL '1 day',  NOW() - INTERVAL '1 day');

-- -----------------------------------------------------------------------
-- 5. DAILY MORTALITY RECORDS
-- Pic de mortalite J3-J7 (installation), puis J20-J25 (stress thermique)
-- Total < 5% sur la duree du lot
-- -----------------------------------------------------------------------

-- Batch 1 (LOT-2024-001, 10000 poules, 42 jours)
INSERT INTO daily_mortality_records (batch_id, record_date, mortality_count, notes, recorded_by_id, created_at, updated_at) VALUES
(1,  NOW()::DATE - INTERVAL '132 days',  8, NULL, 2, NOW() - INTERVAL '132 days', NOW() - INTERVAL '132 days'),
(1,  NOW()::DATE - INTERVAL '131 days', 12, NULL, 2, NOW() - INTERVAL '131 days', NOW() - INTERVAL '131 days'),
(1,  NOW()::DATE - INTERVAL '130 days', 18, 'Pic normal installation', 2, NOW() - INTERVAL '130 days', NOW() - INTERVAL '130 days'),
(1,  NOW()::DATE - INTERVAL '129 days', 22, 'Pic normal installation', 2, NOW() - INTERVAL '129 days', NOW() - INTERVAL '129 days'),
(1,  NOW()::DATE - INTERVAL '128 days', 20, NULL, 2, NOW() - INTERVAL '128 days', NOW() - INTERVAL '128 days'),
(1,  NOW()::DATE - INTERVAL '127 days', 15, NULL, 2, NOW() - INTERVAL '127 days', NOW() - INTERVAL '127 days'),
(1,  NOW()::DATE - INTERVAL '126 days', 10, NULL, 2, NOW() - INTERVAL '126 days', NOW() - INTERVAL '126 days'),
(1,  NOW()::DATE - INTERVAL '125 days',  6, NULL, 2, NOW() - INTERVAL '125 days', NOW() - INTERVAL '125 days'),
(1,  NOW()::DATE - INTERVAL '124 days',  4, NULL, 2, NOW() - INTERVAL '124 days', NOW() - INTERVAL '124 days'),
(1,  NOW()::DATE - INTERVAL '123 days',  3, NULL, 2, NOW() - INTERVAL '123 days', NOW() - INTERVAL '123 days'),
(1,  NOW()::DATE - INTERVAL '122 days',  3, NULL, 2, NOW() - INTERVAL '122 days', NOW() - INTERVAL '122 days'),
(1,  NOW()::DATE - INTERVAL '121 days',  2, NULL, 2, NOW() - INTERVAL '121 days', NOW() - INTERVAL '121 days'),
(1,  NOW()::DATE - INTERVAL '120 days',  2, NULL, 2, NOW() - INTERVAL '120 days', NOW() - INTERVAL '120 days'),
(1,  NOW()::DATE - INTERVAL '119 days',  3, NULL, 2, NOW() - INTERVAL '119 days', NOW() - INTERVAL '119 days'),
(1,  NOW()::DATE - INTERVAL '118 days',  2, NULL, 2, NOW() - INTERVAL '118 days', NOW() - INTERVAL '118 days'),
(1,  NOW()::DATE - INTERVAL '117 days',  2, NULL, 2, NOW() - INTERVAL '117 days', NOW() - INTERVAL '117 days'),
(1,  NOW()::DATE - INTERVAL '116 days',  3, NULL, 2, NOW() - INTERVAL '116 days', NOW() - INTERVAL '116 days'),
(1,  NOW()::DATE - INTERVAL '115 days',  2, NULL, 2, NOW() - INTERVAL '115 days', NOW() - INTERVAL '115 days'),
(1,  NOW()::DATE - INTERVAL '114 days',  3, NULL, 2, NOW() - INTERVAL '114 days', NOW() - INTERVAL '114 days'),
(1,  NOW()::DATE - INTERVAL '113 days',  4, NULL, 2, NOW() - INTERVAL '113 days', NOW() - INTERVAL '113 days'),
(1,  NOW()::DATE - INTERVAL '112 days', 12, 'Stress thermique', 2, NOW() - INTERVAL '112 days', NOW() - INTERVAL '112 days'),
(1,  NOW()::DATE - INTERVAL '111 days', 16, 'Stress thermique pic', 2, NOW() - INTERVAL '111 days', NOW() - INTERVAL '111 days'),
(1,  NOW()::DATE - INTERVAL '110 days', 14, NULL, 2, NOW() - INTERVAL '110 days', NOW() - INTERVAL '110 days'),
(1,  NOW()::DATE - INTERVAL '109 days', 10, NULL, 2, NOW() - INTERVAL '109 days', NOW() - INTERVAL '109 days'),
(1,  NOW()::DATE - INTERVAL '108 days',  7, NULL, 2, NOW() - INTERVAL '108 days', NOW() - INTERVAL '108 days'),
(1,  NOW()::DATE - INTERVAL '107 days',  4, NULL, 2, NOW() - INTERVAL '107 days', NOW() - INTERVAL '107 days'),
(1,  NOW()::DATE - INTERVAL '106 days',  3, NULL, 2, NOW() - INTERVAL '106 days', NOW() - INTERVAL '106 days'),
(1,  NOW()::DATE - INTERVAL '105 days',  2, NULL, 2, NOW() - INTERVAL '105 days', NOW() - INTERVAL '105 days'),
(1,  NOW()::DATE - INTERVAL '104 days',  2, NULL, 2, NOW() - INTERVAL '104 days', NOW() - INTERVAL '104 days'),
(1,  NOW()::DATE - INTERVAL '103 days',  2, NULL, 2, NOW() - INTERVAL '103 days', NOW() - INTERVAL '103 days'),
(1,  NOW()::DATE - INTERVAL '102 days',  1, NULL, 2, NOW() - INTERVAL '102 days', NOW() - INTERVAL '102 days'),
(1,  NOW()::DATE - INTERVAL '101 days',  2, NULL, 2, NOW() - INTERVAL '101 days', NOW() - INTERVAL '101 days'),
(1,  NOW()::DATE - INTERVAL '100 days',  1, NULL, 2, NOW() - INTERVAL '100 days', NOW() - INTERVAL '100 days'),
(1,  NOW()::DATE - INTERVAL  '99 days',  2, NULL, 2, NOW() - INTERVAL  '99 days', NOW() - INTERVAL  '99 days'),
(1,  NOW()::DATE - INTERVAL  '98 days',  1, NULL, 2, NOW() - INTERVAL  '98 days', NOW() - INTERVAL  '98 days'),
(1,  NOW()::DATE - INTERVAL  '97 days',  1, NULL, 2, NOW() - INTERVAL  '97 days', NOW() - INTERVAL  '97 days'),
(1,  NOW()::DATE - INTERVAL  '96 days',  2, NULL, 2, NOW() - INTERVAL  '96 days', NOW() - INTERVAL  '96 days'),
(1,  NOW()::DATE - INTERVAL  '95 days',  1, NULL, 2, NOW() - INTERVAL  '95 days', NOW() - INTERVAL  '95 days'),
(1,  NOW()::DATE - INTERVAL  '94 days',  1, NULL, 2, NOW() - INTERVAL  '94 days', NOW() - INTERVAL  '94 days'),
(1,  NOW()::DATE - INTERVAL  '93 days',  2, NULL, 2, NOW() - INTERVAL  '93 days', NOW() - INTERVAL  '93 days'),
(1,  NOW()::DATE - INTERVAL  '92 days',  1, NULL, 2, NOW() - INTERVAL  '92 days', NOW() - INTERVAL  '92 days'),
(1,  NOW()::DATE - INTERVAL  '91 days',  1, NULL, 2, NOW() - INTERVAL  '91 days', NOW() - INTERVAL  '91 days');

-- Batch 2 (LOT-2024-002, 8500 poules, 42 jours)
INSERT INTO daily_mortality_records (batch_id, record_date, mortality_count, notes, recorded_by_id, created_at, updated_at) VALUES
(2, NOW()::DATE - INTERVAL '117 days',  7, NULL, 3, NOW() - INTERVAL '117 days', NOW() - INTERVAL '117 days'),
(2, NOW()::DATE - INTERVAL '116 days', 10, NULL, 3, NOW() - INTERVAL '116 days', NOW() - INTERVAL '116 days'),
(2, NOW()::DATE - INTERVAL '115 days', 15, 'Pic installation', 3, NOW() - INTERVAL '115 days', NOW() - INTERVAL '115 days'),
(2, NOW()::DATE - INTERVAL '114 days', 18, 'Pic installation', 3, NOW() - INTERVAL '114 days', NOW() - INTERVAL '114 days'),
(2, NOW()::DATE - INTERVAL '113 days', 16, NULL, 3, NOW() - INTERVAL '113 days', NOW() - INTERVAL '113 days'),
(2, NOW()::DATE - INTERVAL '112 days', 12, NULL, 3, NOW() - INTERVAL '112 days', NOW() - INTERVAL '112 days'),
(2, NOW()::DATE - INTERVAL '111 days',  8, NULL, 3, NOW() - INTERVAL '111 days', NOW() - INTERVAL '111 days'),
(2, NOW()::DATE - INTERVAL '110 days',  5, NULL, 3, NOW() - INTERVAL '110 days', NOW() - INTERVAL '110 days'),
(2, NOW()::DATE - INTERVAL '109 days',  4, NULL, 3, NOW() - INTERVAL '109 days', NOW() - INTERVAL '109 days'),
(2, NOW()::DATE - INTERVAL '108 days',  3, NULL, 3, NOW() - INTERVAL '108 days', NOW() - INTERVAL '108 days'),
(2, NOW()::DATE - INTERVAL '107 days',  3, NULL, 3, NOW() - INTERVAL '107 days', NOW() - INTERVAL '107 days'),
(2, NOW()::DATE - INTERVAL '106 days',  2, NULL, 3, NOW() - INTERVAL '106 days', NOW() - INTERVAL '106 days'),
(2, NOW()::DATE - INTERVAL '105 days',  3, NULL, 3, NOW() - INTERVAL '105 days', NOW() - INTERVAL '105 days'),
(2, NOW()::DATE - INTERVAL '104 days',  2, NULL, 3, NOW() - INTERVAL '104 days', NOW() - INTERVAL '104 days'),
(2, NOW()::DATE - INTERVAL '103 days',  3, NULL, 3, NOW() - INTERVAL '103 days', NOW() - INTERVAL '103 days'),
(2, NOW()::DATE - INTERVAL '102 days',  2, NULL, 3, NOW() - INTERVAL '102 days', NOW() - INTERVAL '102 days'),
(2, NOW()::DATE - INTERVAL '101 days',  2, NULL, 3, NOW() - INTERVAL '101 days', NOW() - INTERVAL '101 days'),
(2, NOW()::DATE - INTERVAL '100 days', 10, 'Debut probleme respiratoire', 3, NOW() - INTERVAL '100 days', NOW() - INTERVAL '100 days'),
(2, NOW()::DATE - INTERVAL  '99 days', 14, 'Maladie respiratoire', 3, NOW() - INTERVAL  '99 days', NOW() - INTERVAL  '99 days'),
(2, NOW()::DATE - INTERVAL  '98 days', 11, NULL, 3, NOW() - INTERVAL  '98 days', NOW() - INTERVAL  '98 days'),
(2, NOW()::DATE - INTERVAL  '97 days',  8, 'Traitement en cours', 3, NOW() - INTERVAL  '97 days', NOW() - INTERVAL  '97 days'),
(2, NOW()::DATE - INTERVAL  '96 days',  5, NULL, 3, NOW() - INTERVAL  '96 days', NOW() - INTERVAL  '96 days'),
(2, NOW()::DATE - INTERVAL  '95 days',  4, NULL, 3, NOW() - INTERVAL  '95 days', NOW() - INTERVAL  '95 days'),
(2, NOW()::DATE - INTERVAL  '94 days',  3, NULL, 3, NOW() - INTERVAL  '94 days', NOW() - INTERVAL  '94 days'),
(2, NOW()::DATE - INTERVAL  '93 days',  2, NULL, 3, NOW() - INTERVAL  '93 days', NOW() - INTERVAL  '93 days'),
(2, NOW()::DATE - INTERVAL  '92 days',  2, NULL, 3, NOW() - INTERVAL  '92 days', NOW() - INTERVAL  '92 days'),
(2, NOW()::DATE - INTERVAL  '91 days',  2, NULL, 3, NOW() - INTERVAL  '91 days', NOW() - INTERVAL  '91 days'),
(2, NOW()::DATE - INTERVAL  '90 days',  2, NULL, 3, NOW() - INTERVAL  '90 days', NOW() - INTERVAL  '90 days'),
(2, NOW()::DATE - INTERVAL  '89 days',  1, NULL, 3, NOW() - INTERVAL  '89 days', NOW() - INTERVAL  '89 days'),
(2, NOW()::DATE - INTERVAL  '88 days',  2, NULL, 3, NOW() - INTERVAL  '88 days', NOW() - INTERVAL  '88 days'),
(2, NOW()::DATE - INTERVAL  '87 days',  1, NULL, 3, NOW() - INTERVAL  '87 days', NOW() - INTERVAL  '87 days'),
(2, NOW()::DATE - INTERVAL  '86 days',  1, NULL, 3, NOW() - INTERVAL  '86 days', NOW() - INTERVAL  '86 days'),
(2, NOW()::DATE - INTERVAL  '85 days',  2, NULL, 3, NOW() - INTERVAL  '85 days', NOW() - INTERVAL  '85 days'),
(2, NOW()::DATE - INTERVAL  '84 days',  1, NULL, 3, NOW() - INTERVAL  '84 days', NOW() - INTERVAL  '84 days'),
(2, NOW()::DATE - INTERVAL  '83 days',  1, NULL, 3, NOW() - INTERVAL  '83 days', NOW() - INTERVAL  '83 days'),
(2, NOW()::DATE - INTERVAL  '82 days',  2, NULL, 3, NOW() - INTERVAL  '82 days', NOW() - INTERVAL  '82 days'),
(2, NOW()::DATE - INTERVAL  '81 days',  1, NULL, 3, NOW() - INTERVAL  '81 days', NOW() - INTERVAL  '81 days'),
(2, NOW()::DATE - INTERVAL  '80 days',  1, NULL, 3, NOW() - INTERVAL  '80 days', NOW() - INTERVAL  '80 days'),
(2, NOW()::DATE - INTERVAL  '79 days',  1, NULL, 3, NOW() - INTERVAL  '79 days', NOW() - INTERVAL  '79 days'),
(2, NOW()::DATE - INTERVAL  '78 days',  1, NULL, 3, NOW() - INTERVAL  '78 days', NOW() - INTERVAL  '78 days'),
(2, NOW()::DATE - INTERVAL  '77 days',  1, NULL, 3, NOW() - INTERVAL  '77 days', NOW() - INTERVAL  '77 days'),
(2, NOW()::DATE - INTERVAL  '76 days',  1, NULL, 3, NOW() - INTERVAL  '76 days', NOW() - INTERVAL  '76 days');

-- Batch 3 (LOT-2024-003, Active, 9500 poules, 38 jours)
INSERT INTO daily_mortality_records (batch_id, record_date, mortality_count, notes, recorded_by_id, created_at, updated_at) VALUES
(3, NOW()::DATE - INTERVAL '38 days',  7, NULL, 2, NOW() - INTERVAL '38 days', NOW() - INTERVAL '38 days'),
(3, NOW()::DATE - INTERVAL '37 days', 11, NULL, 2, NOW() - INTERVAL '37 days', NOW() - INTERVAL '37 days'),
(3, NOW()::DATE - INTERVAL '36 days', 17, 'Pic installation', 2, NOW() - INTERVAL '36 days', NOW() - INTERVAL '36 days'),
(3, NOW()::DATE - INTERVAL '35 days', 19, 'Pic installation', 2, NOW() - INTERVAL '35 days', NOW() - INTERVAL '35 days'),
(3, NOW()::DATE - INTERVAL '34 days', 15, NULL, 2, NOW() - INTERVAL '34 days', NOW() - INTERVAL '34 days'),
(3, NOW()::DATE - INTERVAL '33 days', 10, NULL, 2, NOW() - INTERVAL '33 days', NOW() - INTERVAL '33 days'),
(3, NOW()::DATE - INTERVAL '32 days',  7, NULL, 2, NOW() - INTERVAL '32 days', NOW() - INTERVAL '32 days'),
(3, NOW()::DATE - INTERVAL '31 days',  5, NULL, 2, NOW() - INTERVAL '31 days', NOW() - INTERVAL '31 days'),
(3, NOW()::DATE - INTERVAL '30 days',  3, NULL, 2, NOW() - INTERVAL '30 days', NOW() - INTERVAL '30 days'),
(3, NOW()::DATE - INTERVAL '29 days',  3, NULL, 2, NOW() - INTERVAL '29 days', NOW() - INTERVAL '29 days'),
(3, NOW()::DATE - INTERVAL '28 days',  2, NULL, 2, NOW() - INTERVAL '28 days', NOW() - INTERVAL '28 days'),
(3, NOW()::DATE - INTERVAL '27 days',  3, NULL, 2, NOW() - INTERVAL '27 days', NOW() - INTERVAL '27 days'),
(3, NOW()::DATE - INTERVAL '26 days',  2, NULL, 2, NOW() - INTERVAL '26 days', NOW() - INTERVAL '26 days'),
(3, NOW()::DATE - INTERVAL '25 days',  2, NULL, 2, NOW() - INTERVAL '25 days', NOW() - INTERVAL '25 days'),
(3, NOW()::DATE - INTERVAL '24 days',  3, NULL, 2, NOW() - INTERVAL '24 days', NOW() - INTERVAL '24 days'),
(3, NOW()::DATE - INTERVAL '23 days',  2, NULL, 2, NOW() - INTERVAL '23 days', NOW() - INTERVAL '23 days'),
(3, NOW()::DATE - INTERVAL '22 days',  9, 'Debut anomalie mortalite', 2, NOW() - INTERVAL '22 days', NOW() - INTERVAL '22 days'),
(3, NOW()::DATE - INTERVAL '21 days', 13, 'Stress thermique / anomalie', 2, NOW() - INTERVAL '21 days', NOW() - INTERVAL '21 days'),
(3, NOW()::DATE - INTERVAL '20 days', 11, NULL, 2, NOW() - INTERVAL '20 days', NOW() - INTERVAL '20 days'),
(3, NOW()::DATE - INTERVAL '19 days',  8, NULL, 2, NOW() - INTERVAL '19 days', NOW() - INTERVAL '19 days'),
(3, NOW()::DATE - INTERVAL '18 days',  5, NULL, 2, NOW() - INTERVAL '18 days', NOW() - INTERVAL '18 days'),
(3, NOW()::DATE - INTERVAL '17 days',  3, NULL, 2, NOW() - INTERVAL '17 days', NOW() - INTERVAL '17 days'),
(3, NOW()::DATE - INTERVAL '16 days',  2, NULL, 2, NOW() - INTERVAL '16 days', NOW() - INTERVAL '16 days'),
(3, NOW()::DATE - INTERVAL '15 days',  2, NULL, 2, NOW() - INTERVAL '15 days', NOW() - INTERVAL '15 days'),
(3, NOW()::DATE - INTERVAL '14 days',  2, NULL, 2, NOW() - INTERVAL '14 days', NOW() - INTERVAL '14 days'),
(3, NOW()::DATE - INTERVAL '13 days',  1, NULL, 2, NOW() - INTERVAL '13 days', NOW() - INTERVAL '13 days'),
(3, NOW()::DATE - INTERVAL '12 days',  2, NULL, 2, NOW() - INTERVAL '12 days', NOW() - INTERVAL '12 days'),
(3, NOW()::DATE - INTERVAL '11 days',  1, NULL, 2, NOW() - INTERVAL '11 days', NOW() - INTERVAL '11 days'),
(3, NOW()::DATE - INTERVAL '10 days',  2, NULL, 2, NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days'),
(3, NOW()::DATE - INTERVAL  '9 days',  1, NULL, 2, NOW() - INTERVAL  '9 days', NOW() - INTERVAL  '9 days'),
(3, NOW()::DATE - INTERVAL  '8 days',  2, NULL, 2, NOW() - INTERVAL  '8 days', NOW() - INTERVAL  '8 days'),
(3, NOW()::DATE - INTERVAL  '7 days',  1, NULL, 2, NOW() - INTERVAL  '7 days', NOW() - INTERVAL  '7 days'),
(3, NOW()::DATE - INTERVAL  '6 days',  2, NULL, 2, NOW() - INTERVAL  '6 days', NOW() - INTERVAL  '6 days'),
(3, NOW()::DATE - INTERVAL  '5 days',  1, NULL, 2, NOW() - INTERVAL  '5 days', NOW() - INTERVAL  '5 days'),
(3, NOW()::DATE - INTERVAL  '4 days',  1, NULL, 2, NOW() - INTERVAL  '4 days', NOW() - INTERVAL  '4 days'),
(3, NOW()::DATE - INTERVAL  '3 days',  2, NULL, 2, NOW() - INTERVAL  '3 days', NOW() - INTERVAL  '3 days'),
(3, NOW()::DATE - INTERVAL  '2 days',  1, NULL, 2, NOW() - INTERVAL  '2 days', NOW() - INTERVAL  '2 days'),
(3, NOW()::DATE - INTERVAL  '1 day',   1, NULL, 2, NOW() - INTERVAL  '1 day',  NOW() - INTERVAL  '1 day');

-- Batch 4 (LOT-2024-004, Active, 7800 poules, 28 jours)
INSERT INTO daily_mortality_records (batch_id, record_date, mortality_count, notes, recorded_by_id, created_at, updated_at) VALUES
(4, NOW()::DATE - INTERVAL '28 days',  6, NULL, 3, NOW() - INTERVAL '28 days', NOW() - INTERVAL '28 days'),
(4, NOW()::DATE - INTERVAL '27 days',  9, NULL, 3, NOW() - INTERVAL '27 days', NOW() - INTERVAL '27 days'),
(4, NOW()::DATE - INTERVAL '26 days', 13, 'Pic installation', 3, NOW() - INTERVAL '26 days', NOW() - INTERVAL '26 days'),
(4, NOW()::DATE - INTERVAL '25 days', 15, 'Pic installation', 3, NOW() - INTERVAL '25 days', NOW() - INTERVAL '25 days'),
(4, NOW()::DATE - INTERVAL '24 days', 12, NULL, 3, NOW() - INTERVAL '24 days', NOW() - INTERVAL '24 days'),
(4, NOW()::DATE - INTERVAL '23 days',  8, NULL, 3, NOW() - INTERVAL '23 days', NOW() - INTERVAL '23 days'),
(4, NOW()::DATE - INTERVAL '22 days',  5, NULL, 3, NOW() - INTERVAL '22 days', NOW() - INTERVAL '22 days'),
(4, NOW()::DATE - INTERVAL '21 days',  4, NULL, 3, NOW() - INTERVAL '21 days', NOW() - INTERVAL '21 days'),
(4, NOW()::DATE - INTERVAL '20 days',  3, NULL, 3, NOW() - INTERVAL '20 days', NOW() - INTERVAL '20 days'),
(4, NOW()::DATE - INTERVAL '19 days',  3, NULL, 3, NOW() - INTERVAL '19 days', NOW() - INTERVAL '19 days'),
(4, NOW()::DATE - INTERVAL '18 days',  2, NULL, 3, NOW() - INTERVAL '18 days', NOW() - INTERVAL '18 days'),
(4, NOW()::DATE - INTERVAL '17 days',  2, NULL, 3, NOW() - INTERVAL '17 days', NOW() - INTERVAL '17 days'),
(4, NOW()::DATE - INTERVAL '16 days',  3, NULL, 3, NOW() - INTERVAL '16 days', NOW() - INTERVAL '16 days'),
(4, NOW()::DATE - INTERVAL '15 days',  2, NULL, 3, NOW() - INTERVAL '15 days', NOW() - INTERVAL '15 days'),
(4, NOW()::DATE - INTERVAL '14 days',  2, NULL, 3, NOW() - INTERVAL '14 days', NOW() - INTERVAL '14 days'),
(4, NOW()::DATE - INTERVAL '13 days',  3, NULL, 3, NOW() - INTERVAL '13 days', NOW() - INTERVAL '13 days'),
(4, NOW()::DATE - INTERVAL '12 days',  2, NULL, 3, NOW() - INTERVAL '12 days', NOW() - INTERVAL '12 days'),
(4, NOW()::DATE - INTERVAL '11 days',  2, NULL, 3, NOW() - INTERVAL '11 days', NOW() - INTERVAL '11 days'),
(4, NOW()::DATE - INTERVAL '10 days',  2, NULL, 3, NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days'),
(4, NOW()::DATE - INTERVAL  '9 days',  1, NULL, 3, NOW() - INTERVAL  '9 days', NOW() - INTERVAL  '9 days'),
(4, NOW()::DATE - INTERVAL  '8 days',  2, NULL, 3, NOW() - INTERVAL  '8 days', NOW() - INTERVAL  '8 days'),
(4, NOW()::DATE - INTERVAL  '7 days',  1, NULL, 3, NOW() - INTERVAL  '7 days', NOW() - INTERVAL  '7 days'),
(4, NOW()::DATE - INTERVAL  '6 days',  1, NULL, 3, NOW() - INTERVAL  '6 days', NOW() - INTERVAL  '6 days'),
(4, NOW()::DATE - INTERVAL  '5 days',  2, NULL, 3, NOW() - INTERVAL  '5 days', NOW() - INTERVAL  '5 days'),
(4, NOW()::DATE - INTERVAL  '4 days',  1, NULL, 3, NOW() - INTERVAL  '4 days', NOW() - INTERVAL  '4 days'),
(4, NOW()::DATE - INTERVAL  '3 days',  1, NULL, 3, NOW() - INTERVAL  '3 days', NOW() - INTERVAL  '3 days'),
(4, NOW()::DATE - INTERVAL  '2 days',  2, NULL, 3, NOW() - INTERVAL  '2 days', NOW() - INTERVAL  '2 days'),
(4, NOW()::DATE - INTERVAL  '1 day',   1, NULL, 3, NOW() - INTERVAL  '1 day',  NOW() - INTERVAL  '1 day');

-- Batch 5 (LOT-2025-001, Active, 6000 poules, 15 jours)
INSERT INTO daily_mortality_records (batch_id, record_date, mortality_count, notes, recorded_by_id, created_at, updated_at) VALUES
(5, NOW()::DATE - INTERVAL '15 days',  5, NULL, 2, NOW() - INTERVAL '15 days', NOW() - INTERVAL '15 days'),
(5, NOW()::DATE - INTERVAL '14 days',  8, NULL, 2, NOW() - INTERVAL '14 days', NOW() - INTERVAL '14 days'),
(5, NOW()::DATE - INTERVAL '13 days', 12, 'Pic installation', 2, NOW() - INTERVAL '13 days', NOW() - INTERVAL '13 days'),
(5, NOW()::DATE - INTERVAL '12 days', 14, 'Pic installation', 2, NOW() - INTERVAL '12 days', NOW() - INTERVAL '12 days'),
(5, NOW()::DATE - INTERVAL '11 days', 10, NULL, 2, NOW() - INTERVAL '11 days', NOW() - INTERVAL '11 days'),
(5, NOW()::DATE - INTERVAL '10 days',  7, NULL, 2, NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days'),
(5, NOW()::DATE - INTERVAL  '9 days',  5, NULL, 2, NOW() - INTERVAL  '9 days', NOW() - INTERVAL  '9 days'),
(5, NOW()::DATE - INTERVAL  '8 days',  3, NULL, 2, NOW() - INTERVAL  '8 days', NOW() - INTERVAL  '8 days'),
(5, NOW()::DATE - INTERVAL  '7 days',  3, NULL, 2, NOW() - INTERVAL  '7 days', NOW() - INTERVAL  '7 days'),
(5, NOW()::DATE - INTERVAL  '6 days',  2, NULL, 2, NOW() - INTERVAL  '6 days', NOW() - INTERVAL  '6 days'),
(5, NOW()::DATE - INTERVAL  '5 days',  2, NULL, 2, NOW() - INTERVAL  '5 days', NOW() - INTERVAL  '5 days'),
(5, NOW()::DATE - INTERVAL  '4 days',  2, NULL, 2, NOW() - INTERVAL  '4 days', NOW() - INTERVAL  '4 days'),
(5, NOW()::DATE - INTERVAL  '3 days',  1, NULL, 2, NOW() - INTERVAL  '3 days', NOW() - INTERVAL  '3 days'),
(5, NOW()::DATE - INTERVAL  '2 days',  2, NULL, 2, NOW() - INTERVAL  '2 days', NOW() - INTERVAL  '2 days'),
(5, NOW()::DATE - INTERVAL  '1 day',   1, NULL, 2, NOW() - INTERVAL  '1 day',  NOW() - INTERVAL  '1 day');

-- Batch 6 (LOT-2025-002, Active, 4500 poules, 7 jours)
INSERT INTO daily_mortality_records (batch_id, record_date, mortality_count, notes, recorded_by_id, created_at, updated_at) VALUES
(6, NOW()::DATE - INTERVAL '7 days',  4, NULL, 3, NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days'),
(6, NOW()::DATE - INTERVAL '6 days',  6, NULL, 3, NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days'),
(6, NOW()::DATE - INTERVAL '5 days',  9, 'Pic installation', 3, NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days'),
(6, NOW()::DATE - INTERVAL '4 days', 10, 'Pic installation', 3, NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),
(6, NOW()::DATE - INTERVAL '3 days',  7, NULL, 3, NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days'),
(6, NOW()::DATE - INTERVAL '2 days',  5, NULL, 3, NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
(6, NOW()::DATE - INTERVAL '1 day',   3, NULL, 3, NOW() - INTERVAL '1 day',  NOW() - INTERVAL '1 day');

-- -----------------------------------------------------------------------
-- 6. HEALTH RECORDS
-- 3 approuves (Approved), 3 en attente (Pending), 2 rejetes (Rejected)
-- requires_approval = true pour ceux > 500 MAD
-- -----------------------------------------------------------------------
INSERT INTO health_records (batch_id, veterinarian_id, diagnosis, treatment, examination_date, next_visit_date, mortality_count, notes, treatment_cost, requires_approval, approval_status, approved_by_id, approved_at, created_at, updated_at) VALUES
-- Batch 1 - Approuve
(1, 4, 'Bilan sante semaine 3 - RAS', 'Vaccination Newcastle administree', NOW()::DATE - INTERVAL '120 days', NOW()::DATE - INTERVAL '106 days', 3, 'Lot en bonne sante generale', 350.00, false, 'Approved', 1, NOW() - INTERVAL '120 days', NOW() - INTERVAL '121 days', NOW() - INTERVAL '120 days'),
-- Batch 1 - Approuve (cout eleve)
(1, 4, 'Stress thermique semaine 5 - traitement', 'Electrolytes + vitamines C et E. Ventilation renforcee.', NOW()::DATE - INTERVAL '112 days', NOW()::DATE - INTERVAL '98 days', 28, 'Episode de stress thermique. Mesures correctives appliquees.', 1200.00, true, 'Approved', 1, NOW() - INTERVAL '111 days', NOW() - INTERVAL '113 days', NOW() - INTERVAL '111 days'),
-- Batch 2 - Approuve
(2, 5, 'Maladie de Newcastle confirmee', 'Vaccination d''urgence lot entier. Vitamine E + selenium.', NOW()::DATE - INTERVAL '100 days', NOW()::DATE - INTERVAL '86 days', 36, 'Foyer detecte. Traitement d''urgence applique avec succes.', 2800.00, true, 'Approved', 1, NOW() - INTERVAL '99 days', NOW() - INTERVAL '101 days', NOW() - INTERVAL '99 days'),
-- Batch 3 - En attente (anomalie mortalite recente)
(3, 4, 'Anomalie mortalite J22 - investigation', 'Analyses en cours. Antibiotiques preventifs commences.', NOW()::DATE - INTERVAL '22 days', NOW()::DATE + INTERVAL '1 day', 22, 'Pic de mortalite inhabituel. Necessite validation admin.', 1850.00, true, 'Pending', NULL, NULL, NOW() - INTERVAL '22 days', NOW() - INTERVAL '22 days'),
-- Batch 3 - En attente
(3, 5, 'Controle routine semaine 5', 'Complement mineral et vitamine A. Pesee hebdomadaire.', NOW()::DATE - INTERVAL '10 days', NOW()::DATE + INTERVAL '4 days', 4, 'Lot en bonne progression. Poids conforme courbe standard.', 420.00, false, 'Pending', NULL, NULL, NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days'),
-- Batch 4 - En attente
(4, 4, 'Bronchite infectieuse suspectee', 'Tetracycline en eau de boisson 5 jours. Isolement preventif.', NOW()::DATE - INTERVAL  '8 days', NOW()::DATE + INTERVAL '3 days', 6, 'Symptomes respiratoires legers. Traitement preventif administre.', 980.00, true, 'Pending', NULL, NULL, NOW() - INTERVAL  '8 days', NOW() - INTERVAL  '8 days'),
-- Batch 5 - Rejete
(5, 5, 'Traitement excessif propose par sous-traitant', 'Antibiotiques non conformes protocole ferme', NOW()::DATE - INTERVAL '12 days', NULL, 14, 'Traitement refuse car non conforme au protocole interne.', 5500.00, true, 'Rejected', 1, NOW() - INTERVAL '11 days', NOW() - INTERVAL '13 days', NOW() - INTERVAL '11 days'),
-- Batch 6 - Rejete
(6, 4, 'Diagnostic errone - erreur de protocole', 'Traitement non adapte a la souche presente', NOW()::DATE - INTERVAL  '5 days', NULL, 10, 'Diagnostic rejete apres second avis. Nouveau protocole en cours.', 750.00, true, 'Rejected', 1, NOW() - INTERVAL  '4 days', NOW() - INTERVAL  '6 days', NOW() - INTERVAL  '4 days');

-- -----------------------------------------------------------------------
-- 7. STOCK ITEMS
-- -----------------------------------------------------------------------
INSERT INTO stock_items (type, name, quantity, unit, created_by_id, created_at, updated_at) VALUES
-- Aliments
('Feed',    'Aliment Demarrage (0-10 jours)',           850.0000, 'sac 50kg', 1, NOW() - INTERVAL '30 days', NOW()),
('Feed',    'Aliment Croissance (11-28 jours)',         1200.0000, 'sac 50kg', 1, NOW() - INTERVAL '30 days', NOW()),
('Feed',    'Aliment Finition (29-42 jours)',           950.0000, 'sac 50kg', 1, NOW() - INTERVAL '25 days', NOW()),
('Feed',    'Tourteau de soja',                         320.0000, 'kg',       1, NOW() - INTERVAL '20 days', NOW()),
('Feed',    'Mais concasse',                            500.0000, 'kg',       1, NOW() - INTERVAL '15 days', NOW()),
-- Vaccins
('Vaccine', 'Vaccin Newcastle (HB1)',                    45.0000, 'dose 500ml', 1, NOW() - INTERVAL '60 days', NOW()),
('Vaccine', 'Vaccin Gumboro',                            30.0000, 'dose 500ml', 1, NOW() - INTERVAL '60 days', NOW()),
('Vaccine', 'Vaccin Bronchite Infectieuse H120',         25.0000, 'dose 500ml', 1, NOW() - INTERVAL '45 days', NOW()),
('Vaccine', 'Vaccin Marek',                              60.0000, 'dose 250ml', 1, NOW() - INTERVAL '90 days', NOW()),
('Vaccine', 'Vaccin Salmonelle',                         20.0000, 'dose 250ml', 1, NOW() - INTERVAL '30 days', NOW()),
-- Vitamines et complements
('Vitamin', 'Complexe vitaminique A+D3+E',              180.0000, 'flacon 1L',  1, NOW() - INTERVAL '45 days', NOW()),
('Vitamin', 'Vitamine C (acide ascorbique)',             95.0000,  'kg',        1, NOW() - INTERVAL '30 days', NOW()),
('Vitamin', 'Electrolytes rehydratants',                 65.0000,  'sachet 1kg', 1, NOW() - INTERVAL '20 days', NOW()),
('Vitamin', 'Probiotiques digestifs',                    40.0000,  'flacon 500g', 1, NOW() - INTERVAL '15 days', NOW()),
('Vitamin', 'Selenium + Vitamine E injectable',          12.0000,  'flacon 100ml', 1, NOW() - INTERVAL '10 days', NOW());

-- -----------------------------------------------------------------------
-- 8. SALES (ventes des lots completes)
-- -----------------------------------------------------------------------
INSERT INTO sales (batch_id, client_id, quantity, unit_price, total_price, sale_date, payment_status, delivery_address, notes, recorded_by_id, created_at, updated_at) VALUES
-- Ventes LOT-2024-001
(1, 6, 4500, 38.50, 173250.00, NOW()::DATE - INTERVAL '92 days', 'Paid',    'Av. Hassan II, Casablanca', 'Premiere tranche livree. Poids moyen 2.2kg.', 1, NOW() - INTERVAL '92 days', NOW() - INTERVAL '92 days'),
(1, 7, 3200, 37.80, 120960.00, NOW()::DATE - INTERVAL '91 days', 'Paid',    'Rte de Marrakech, Agadir',  'Deuxieme tranche. Client satisfait.', 1, NOW() - INTERVAL '91 days', NOW() - INTERVAL '91 days'),
(1, 6, 2000, 38.00,  76000.00, NOW()::DATE - INTERVAL '90 days', 'Paid',    'Av. Hassan II, Casablanca', 'Solde final du lot.', 1, NOW() - INTERVAL '90 days', NOW() - INTERVAL '90 days'),
-- Ventes LOT-2024-002
(2, 7, 3800, 36.50, 138700.00, NOW()::DATE - INTERVAL '77 days', 'Paid',    'Rte de Marrakech, Agadir',  'Lot complet. Poids moyen 2.1kg.', 1, NOW() - INTERVAL '77 days', NOW() - INTERVAL '77 days'),
(2, 6, 2500, 36.00,  90000.00, NOW()::DATE - INTERVAL '76 days', 'Paid',    'Av. Hassan II, Casablanca', NULL, 1, NOW() - INTERVAL '76 days', NOW() - INTERVAL '76 days'),
-- Vente partielle LOT-2024-003 (encore actif - vente partielle anticipee)
(3, 6, 1000, 40.00,  40000.00, NOW()::DATE - INTERVAL  '3 days', 'Pending', 'Av. Hassan II, Casablanca', 'Vente anticipee 1000 poulets. Paiement en attente.', 1, NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days');
