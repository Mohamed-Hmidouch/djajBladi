-- ============================================================
-- V30 : Scenarios riches par role et logique metier
-- Couvre : approbation health records, alertes vaccination,
--          FCR/dashboard, achats client, mortalite, stock
-- ============================================================

-- ============================================================
-- SCENARIO A : ADMIN - Gestion des approbations health records
-- 5 cas : PENDING x2 (urgence + normal), APPROVED x2, REJECTED x1
-- ============================================================

-- A1 : PENDING - Traitement urgence maladie respiratoire grave (lot 4 actif)
--      Declencheur : is_vaccination=false + maladie grave signalee + cout eleve
INSERT INTO health_records (batch_id, veterinarian_id, diagnosis, treatment, examination_date, next_visit_date,
    mortality_count, notes, treatment_cost, requires_approval, approval_status,
    withdrawal_days, is_vaccination, stock_item_id, quantity_used, created_at, updated_at)
VALUES (18, 192,
    'Maladie de Newcastle - forme nerveuse - taux mortalite 2%',
    'Enrofloxacine 10% dose double + vitamines en eau de boisson 7 jours + isolement immediat',
    '2026-03-28', '2026-04-04',
    14, 'URGENCE : transmission rapide detectee - necessite approbation immediate admin',
    6200.00, true, 'PENDING_APPROVAL',
    10, false, 31, 25.00,
    NOW(), NOW());

-- A2 : PENDING - Traitement coccidiose modere (lot 5 actif)
--      Declencheur : maladie signalee par vet
INSERT INTO health_records (batch_id, veterinarian_id, diagnosis, treatment, examination_date, next_visit_date,
    mortality_count, notes, treatment_cost, requires_approval, approval_status,
    withdrawal_days, is_vaccination, stock_item_id, quantity_used, created_at, updated_at)
VALUES (19, 192,
    'Coccidiose intestinale - forme legere - diarrhee jaunatre',
    'Amoxicilline poudre + vitamines electrolytes pendant 5 jours',
    '2026-03-27', '2026-04-03',
    2, 'Detecte lors de la visite de routine J5 - traitement preventif recommande',
    360.00, true, 'PENDING_APPROVAL',
    5, false, 32, 3.00,
    NOW(), NOW());

-- A3 : APPROVED - Traitement bronchite lot 5 - approuve hier
INSERT INTO health_records (batch_id, veterinarian_id, diagnosis, treatment, examination_date, next_visit_date,
    mortality_count, notes, treatment_cost, requires_approval, approval_status,
    approved_by_id, approved_at, withdrawal_days, is_vaccination, stock_item_id, quantity_used, created_at, updated_at)
VALUES (19, 192,
    'Bronchite infectieuse - forme subclinique',
    'Vitamines + electrolytes eau de boisson 3 jours - renforcement immunitaire',
    '2026-03-25', '2026-04-01',
    0, 'Traitement de soutien post-vaccination - approuve rapidement',
    180.00, true, 'APPROVED',
    188, '2026-03-25 11:30:00', 3, false, 33, 2.50,
    NOW(), NOW());

-- A4 : APPROVED - Traitement parasitaire lot 4 - approuve avec deduction stock
INSERT INTO health_records (batch_id, veterinarian_id, diagnosis, treatment, examination_date, next_visit_date,
    mortality_count, notes, treatment_cost, requires_approval, approval_status,
    approved_by_id, approved_at, withdrawal_days, is_vaccination, stock_item_id, quantity_used, created_at, updated_at)
VALUES (18, 192,
    'Parasitisme interne leger - heterakis',
    'Enrofloxacine 10% dose prophylactique 3 jours',
    '2026-03-20', '2026-03-27',
    0, 'Traitement preventif recommande suite controle sanitaire trimestriel - approuve',
    510.00, true, 'APPROVED',
    188, '2026-03-20 09:15:00', 5, false, 31, 6.00,
    NOW(), NOW());

-- A5 : REJECTED - Traitement antibiotique sur lot 4 - trop cher sans justification clinique suffisante
INSERT INTO health_records (batch_id, veterinarian_id, diagnosis, treatment, examination_date, next_visit_date,
    mortality_count, notes, treatment_cost, requires_approval, approval_status,
    approved_by_id, approved_at, withdrawal_days, is_vaccination, stock_item_id, quantity_used, created_at, updated_at)
VALUES (18, 192,
    'Stress thermique leger - comportement anormal',
    'Protocole antibiotique large spectre + supplementation vitaminique intensive',
    '2026-03-15', '2026-03-22',
    0, 'REJETE : admin estime traitement disproportionne - stress thermique ne justifie pas antibiotiques - ameliorer ventilation',
    8500.00, true, 'REJECTED',
    188, '2026-03-15 16:00:00', 14, false, 31, 40.00,
    NOW(), NOW());

-- ============================================================
-- SCENARIO B : VETERINAIRE - Vaccinations (is_vaccination = true)
-- 5 cas : vaccine fait, vaccine en retard, vaccine planifie,
--         rappel fait, vaccination sans stock
-- ============================================================

-- B1 : Vaccination Newcastle lot 4 J1 - faite a l'arrivee
INSERT INTO health_records (batch_id, veterinarian_id, diagnosis, treatment, examination_date, next_visit_date,
    mortality_count, notes, treatment_cost, requires_approval, approval_status,
    approved_by_id, approved_at, withdrawal_days, is_vaccination, stock_item_id, quantity_used, created_at, updated_at)
VALUES (18, 192,
    'Vaccination preventive Newcastle + IB - J1 protocole Cobb 500',
    'Spray vaccin IB+ND a la couvoir - 2900 doses administrees',
    '2026-03-08', '2026-03-22',
    0, 'Vaccination d entree realisee - conditions optimales - 100% couverture',
    87.00, false, 'APPROVED',
    188, '2026-03-08 09:00:00', 0, true, 34, 2900.00,
    NOW(), NOW());

-- B2 : Vaccination Gumboro lot 4 J12 - faite a J14 (legere retard ok)
INSERT INTO health_records (batch_id, veterinarian_id, diagnosis, treatment, examination_date, next_visit_date,
    mortality_count, notes, treatment_cost, requires_approval, approval_status,
    approved_by_id, approved_at, withdrawal_days, is_vaccination, stock_item_id, quantity_used, created_at, updated_at)
VALUES (18, 192,
    'Vaccination Gumboro IBD - protocole Cobb 500 J12 (realise J14)',
    'Vaccin IBD Intermediate eau de boisson - 2880 doses - 2h sans eau avant',
    '2026-03-22', '2026-03-29',
    0, 'Vaccination realisee avec 2 jours retard - vet absent weekend - couverture complete',
    108.00, false, 'APPROVED',
    188, '2026-03-22 10:30:00', 0, true, 35, 2880.00,
    NOW(), NOW());

-- B3 : Vaccination Newcastle rappel lot 5 J7 - realisee
INSERT INTO health_records (batch_id, veterinarian_id, diagnosis, treatment, examination_date, next_visit_date,
    mortality_count, notes, treatment_cost, requires_approval, approval_status,
    approved_by_id, approved_at, withdrawal_days, is_vaccination, stock_item_id, quantity_used, created_at, updated_at)
VALUES (19, 192,
    'Vaccination Newcastle + IB J1 - protocole Arbor Acres',
    'Spray vaccin IB+ND - 4895 doses administrees a la couvoir',
    '2026-03-22', '2026-03-29',
    0, 'Vaccination entree lot 005 - Arbor Acres - conditions excellentes',
    88.11, false, 'APPROVED',
    188, '2026-03-22 08:00:00', 0, true, 34, 4895.00,
    NOW(), NOW());

-- B4 : Vaccination Bronchite Infectieuse lot 2 - historique
INSERT INTO health_records (batch_id, veterinarian_id, diagnosis, treatment, examination_date, next_visit_date,
    mortality_count, notes, treatment_cost, requires_approval, approval_status,
    approved_by_id, approved_at, withdrawal_days, is_vaccination, stock_item_id, quantity_used, created_at, updated_at)
VALUES (16, 192,
    'Vaccination IB - Bronchite Infectieuse rappel J21',
    'Vaccin Bronchite Infectieuse spray - 3720 doses',
    '2026-01-31', '2026-02-14',
    0, 'Rappel vaccinal realise a J21 selon protocole Cobb 500 - lot 002',
    66.96, false, 'APPROVED',
    188, '2026-01-31 09:00:00', 0, true, 36, 3720.00,
    NOW(), NOW());

-- B5 : Examen sans vaccination ni traitement - bilan sanitaire lot 3
INSERT INTO health_records (batch_id, veterinarian_id, diagnosis, treatment, examination_date, next_visit_date,
    mortality_count, notes, treatment_cost, requires_approval, approval_status,
    approved_by_id, approved_at, withdrawal_days, is_vaccination, created_at, updated_at)
VALUES (17, 192,
    'Bilan sanitaire pre-vente - aucune pathologie detectee',
    'Aucun traitement necessaire - lot sain certifie',
    '2026-03-24', '2026-04-07',
    0, 'Inspection complete pre-vente : poids moyen 2.5kg, FCR 1.72, etat sanitaire excellent - CERTIFIE VENDABLE',
    0.00, false, 'APPROVED',
    188, '2026-03-24 14:00:00', 0, false,
    NOW(), NOW());

-- ============================================================
-- SCENARIO C : OUVRIER - Enregistrements alimentation
-- 5 lots x scenarios variés : normal, alerte sous-consommation,
--   changement aliment, alerte surconsommation, rupture partielle
-- ============================================================

-- C1 : Alimentation normale lot 4 - semaine courante complete
INSERT INTO feeding_records (batch_id, feed_type, quantity, feeding_date, notes, recorded_by_id, stock_item_id, created_at, updated_at)
VALUES
    (18, 'Aliment Croissance (Grower)', 290.00, '2026-03-27', 'Alimentation journaliere normale - poulets actifs', 190, 29, NOW(), NOW()),
    (18, 'Aliment Croissance (Grower)', 305.00, '2026-03-28', 'Augmentation ration - bonne croissance visible', 190, 29, NOW(), NOW()),
    (18, 'Aliment Croissance (Grower)', 295.00, '2026-03-29', 'Ration normale - J21 bon comportement', 191, 29, NOW(), NOW());

-- C2 : Sous-consommation detectee lot 5 J4 (signal d alerte)
INSERT INTO feeding_records (batch_id, feed_type, quantity, feeding_date, notes, recorded_by_id, stock_item_id, created_at, updated_at)
VALUES
    (19, 'Aliment Demarrage (Starter)', 80.00, '2026-03-26',
     'ALERTE: consommation anormalement basse (normale=180kg) - poulets groupes dans un coin - signale au vet',
     190, 28, NOW(), NOW());

-- C3 : Passage aliment starter vers grower lot 4 J10
INSERT INTO feeding_records (batch_id, feed_type, quantity, feeding_date, notes, recorded_by_id, stock_item_id, created_at, updated_at)
VALUES
    (18, 'Aliment Demarrage (Starter)', 185.00, '2026-03-17', 'Derniere ration starter - passage grower demain', 190, 28, NOW(), NOW()),
    (18, 'Aliment Croissance (Grower)', 220.00, '2026-03-18', 'Premier jour grower - transition progressive', 191, 29, NOW(), NOW());

-- C4 : Alimentation lot 2 historique complet (lot completed)
INSERT INTO feeding_records (batch_id, feed_type, quantity, feeding_date, notes, recorded_by_id, stock_item_id, created_at, updated_at)
VALUES
    (16, 'Aliment Demarrage (Starter)', 190.00, '2026-01-15', 'J5 normal', 190, 28, NOW(), NOW()),
    (16, 'Aliment Croissance (Grower)', 280.00, '2026-02-01', 'J22 croissance forte', 191, 29, NOW(), NOW()),
    (16, 'Aliment Finition (Finisher)', 320.00, '2026-02-15', 'J36 finition - preparation vente', 190, 30, NOW(), NOW()),
    (16, 'Aliment Finition (Finisher)', 310.00, '2026-02-20', 'J41 - ration reduite pre-abattage', 191, 30, NOW(), NOW());

-- C5 : Alimentation lot 3 (READY_FOR_SALE) - derniers jours avant vente
INSERT INTO feeding_records (batch_id, feed_type, quantity, feeding_date, notes, recorded_by_id, stock_item_id, created_at, updated_at)
VALUES
    (17, 'Aliment Finition (Finisher)', 380.00, '2026-03-10', 'Finition J49 - poids cible atteint', 190, 30, NOW(), NOW()),
    (17, 'Aliment Finition (Finisher)', 350.00, '2026-03-18', 'J57 - reduction ration progressive pre-vente', 191, 30, NOW(), NOW()),
    (17, 'Aliment Finition (Finisher)', 290.00, '2026-03-25', 'J64 - derniere alimentation avant vente', 190, 30, NOW(), NOW());

-- ============================================================
-- SCENARIO D : OUVRIER - Mortalite quotidienne
-- 5 cas : normale, spike signale, due a chaleur, post-traitement, liee examen vet
-- ============================================================

-- D1 : Mortalite normale lot 5 debut de vie
INSERT INTO daily_mortality_records (batch_id, record_date, mortality_count, source, notes, recorded_by_id, created_at, updated_at)
VALUES
    (19, '2026-03-24', 1, 'WORKER_REPORT', 'Mortalite J2 - normale debut de vie', 190, NOW(), NOW()),
    (19, '2026-03-26', 2, 'WORKER_REPORT', 'Mortalite J4 - legere, dans les normes', 190, NOW(), NOW()),
    (19, '2026-03-28', 1, 'WORKER_REPORT', 'Mortalite J6 - tres faible - excellent lot', 191, NOW(), NOW());

-- D2 : Spike mortalite lot 4 - signale et suivi
INSERT INTO daily_mortality_records (batch_id, record_date, mortality_count, source, notes, recorded_by_id, created_at, updated_at)
VALUES
    (18, '2026-03-28', 11, 'WORKER_REPORT', 'SPIKE: 11 morts en 1 journee - alerte envoyee au vet - probable cause: courant d air nuit', 191, NOW(), NOW()),
    (18, '2026-03-29', 4, 'WORKER_REPORT', 'Retour partiel a la normale apres fermeture aeration - vet contacte', 190, NOW(), NOW());

-- D3 : Mortalite due chaleur lot 3 (historique semaine chaude)
INSERT INTO daily_mortality_records (batch_id, record_date, mortality_count, source, notes, recorded_by_id, created_at, updated_at)
VALUES
    (17, '2026-03-05', 8, 'WORKER_REPORT', 'Chaleur 38C - mortalite stress thermique - brumisateurs actives', 190, NOW(), NOW()),
    (17, '2026-03-06', 5, 'WORKER_REPORT', 'Suite chaleur J2 - amelioration apres refroidissement', 191, NOW(), NOW()),
    (17, '2026-03-07', 2, 'WORKER_REPORT', 'Retour a la normale - temperature stabilisee', 190, NOW(), NOW());

-- D4 : Mortalite post-traitement antibiotique lot 2 (normal apres maladie)
INSERT INTO daily_mortality_records (batch_id, record_date, mortality_count, source, notes, recorded_by_id, created_at, updated_at)
VALUES
    (16, '2026-02-04', 18, 'WORKER_REPORT', 'Mortalite elevee J25 - maladie en cours - traitement administre par vet', 190, NOW(), NOW()),
    (16, '2026-02-06', 9, 'WORKER_REPORT', 'Amelioration progressive J27 - traitement efficace', 191, NOW(), NOW()),
    (16, '2026-02-08', 3, 'WORKER_REPORT', 'J29 retour normal - traitement termine avec succes', 190, NOW(), NOW());

-- D5 : Mortalite enregistree par vet lors examen lot 1 (historique)
INSERT INTO daily_mortality_records (batch_id, record_date, mortality_count, source, notes, recorded_by_id, created_at, updated_at)
VALUES
    (15, '2026-01-20', 35, 'VETERINARIAN_EXAMINATION',
     'Mortalite constatee lors visite sanitaire - cause: Gumboro - traitement d urgence prescrit', 192, NOW(), NOW()),
    (15, '2026-01-28', 12, 'VETERINARIAN_EXAMINATION',
     'Visite de controle J44 - mortalite residuelle - evolution favorable', 192, NOW(), NOW());

-- ============================================================
-- SCENARIO E : CLIENT - Commandes (5 cas metier)
-- cas : commande normale payee, commande grosse quantite pending,
--       commande annulee par client, commande deuxieme lot,
--       commande en attente livraison
-- ============================================================

-- E1 : Commande normale lot 3 - client 1 - PAYEE hier
INSERT INTO sales (batch_id, client_id, quantity, unit_price, total_price, sale_date, payment_status,
    delivery_address, notes, recorded_by_id, created_at, updated_at)
VALUES (17, 193, 800, 28.50, 22800.00, '2026-03-28',
    'Paid', '34 Rue Allal Ben Abdallah, Casablanca',
    'Commande reguliere - client fidele - livraison assuree sous 24h',
    188, NOW(), NOW());

-- E2 : Grosse commande lot 3 - client 2 - EN ATTENTE PAIEMENT (virement bancaire)
INSERT INTO sales (batch_id, client_id, quantity, unit_price, total_price, sale_date, payment_status,
    delivery_address, notes, recorded_by_id, created_at, updated_at)
VALUES (17, 194, 1200, 28.00, 33600.00, '2026-03-29',
    'Pending', '8 Boulevard Hassan II, Fes',
    'Grosse commande - virement bancaire en cours - confirmation attendue demain',
    188, NOW(), NOW());

-- E3 : Commande annulee par client 3 (logistique) - lot 3
INSERT INTO sales (batch_id, client_id, quantity, unit_price, total_price, sale_date, payment_status,
    delivery_address, notes, recorded_by_id, created_at, updated_at)
VALUES (17, 195, 600, 28.50, 17100.00, '2026-03-26',
    'Cancelled', '15 Rue Ibn Khaldoun, Tanger',
    'Annule par client - probleme camion frigorifique - reprogramme semaine prochaine',
    188, NOW(), NOW());

-- E4 : Commande lot 2 (Completed) - client 1 - PAYEE
INSERT INTO sales (batch_id, client_id, quantity, unit_price, total_price, sale_date, payment_status,
    delivery_address, notes, recorded_by_id, created_at, updated_at)
VALUES (16, 193, 1000, 27.50, 27500.00, '2026-03-22',
    'Paid', '34 Rue Allal Ben Abdallah, Casablanca',
    'Premiere tranche lot 002 - client satisfait qualite produit',
    188, NOW(), NOW());

-- E5 : Commande lot 2 - client 3 - EN ATTENTE (livraison planifiee demain)
INSERT INTO sales (batch_id, client_id, quantity, unit_price, total_price, sale_date, payment_status,
    delivery_address, notes, recorded_by_id, created_at, updated_at)
VALUES (16, 195, 1720, 27.50, 47300.00, '2026-03-29',
    'Pending', '15 Rue Ibn Khaldoun, Tanger',
    'Deuxieme tentative apres annulation - livraison confirmee 30/03',
    188, NOW(), NOW());

-- ============================================================
-- SCENARIO F : STOCK - Mouvements et alertes stock bas
-- Mise a jour des quantites pour refleter les consommations reelles
-- ============================================================

-- Deduire les quantites consommees par les vaccinations approuvees
-- Vaccin Newcastle (id=34) : 2900 + 4895 = 7795 doses utilisees
UPDATE stock_items SET quantity = quantity - 7795 WHERE id = 34;

-- Vaccin Gumboro (id=35) : 2880 doses utilisees
UPDATE stock_items SET quantity = quantity - 2880 WHERE id = 35;

-- Vaccin Bronchite (id=36) : 3720 doses utilisees
UPDATE stock_items SET quantity = quantity - 3720 WHERE id = 36;

-- Enrofloxacine (id=31) : 25 + 6 + 40 = 71 litres utilises (approuves)
UPDATE stock_items SET quantity = quantity - 71 WHERE id = 31;

-- Amoxicilline (id=32) : 3 kg utilise (approuve)
UPDATE stock_items SET quantity = quantity - 3 WHERE id = 32;

-- Vitamines (id=33) : 2.5 kg utilise (approuve)
UPDATE stock_items SET quantity = quantity - 2.5 WHERE id = 33;

-- Aliment Starter : consommations enregistrees ci-dessus
UPDATE stock_items SET quantity = quantity - 1635 WHERE id = 28;

-- Aliment Grower : consommations
UPDATE stock_items SET quantity = quantity - 1895 WHERE id = 29;

-- Aliment Finisher : consommations
UPDATE stock_items SET quantity = quantity - 1650 WHERE id = 30;

-- ============================================================
-- SCENARIO G : ADMIN - Ajout stock (reapprovisionnement)
-- Simule une livraison fournisseur
-- ============================================================

-- Nouveau stock : antibiotique tetracycline
INSERT INTO stock_items (type, name, quantity, unit, unit_price, stock_type, created_by_id, created_at, updated_at)
VALUES ('MEDICINE', 'Tetracycline poudre 20%', 50.00, 'kg', 110.00, 'MEDICATION', 188, NOW(), NOW());

-- Nouveau stock : desinfectant batiments
INSERT INTO stock_items (type, name, quantity, unit, unit_price, stock_type, created_by_id, created_at, updated_at)
VALUES ('EQUIPMENT', 'Desinfectant Virkon S', 40.00, 'litre', 65.00, 'EQUIPMENT', 188, NOW(), NOW());

-- Reapprovisionnement vaccin Newcastle (stock critique apres utilisation)
UPDATE stock_items SET quantity = quantity + 10000 WHERE id = 34;

-- Reapprovisionnement vaccin Gumboro
UPDATE stock_items SET quantity = quantity + 5000 WHERE id = 35;

-- ============================================================
-- SCENARIO H : FCR / DASHBOARD - Donnees complementaires pour
-- que le dashboard admin affiche des KPIs pertinents
-- ============================================================

-- Alimentation supplementaire lot 3 pour FCR complet
INSERT INTO feeding_records (batch_id, feed_type, quantity, feeding_date, notes, recorded_by_id, stock_item_id, created_at, updated_at)
VALUES
    (17, 'Aliment Demarrage (Starter)', 185.00, '2026-01-22', 'J2 - bon appetit', 190, 28, NOW(), NOW()),
    (17, 'Aliment Demarrage (Starter)', 210.00, '2026-01-26', 'J6 - croissance normale', 191, 28, NOW(), NOW()),
    (17, 'Aliment Croissance (Grower)',  285.00, '2026-02-03', 'J14 - passage grower', 190, 29, NOW(), NOW()),
    (17, 'Aliment Croissance (Grower)',  320.00, '2026-02-10', 'J21 bonne croissance', 191, 29, NOW(), NOW()),
    (17, 'Aliment Croissance (Grower)',  340.00, '2026-02-20', 'J31 excellent', 190, 29, NOW(), NOW()),
    (17, 'Aliment Finition (Finisher)', 360.00, '2026-02-28', 'J39 finition debut', 191, 30, NOW(), NOW());

-- Mortalite lot 3 complete pour dashboard
INSERT INTO daily_mortality_records (batch_id, record_date, mortality_count, source, notes, recorded_by_id, created_at, updated_at)
VALUES
    (17, '2026-01-22', 2, 'WORKER_REPORT', 'J2 normale', 190, NOW(), NOW()),
    (17, '2026-01-30', 3, 'WORKER_REPORT', 'J10 normale', 191, NOW(), NOW()),
    (17, '2026-02-10', 4, 'WORKER_REPORT', 'J21 normale', 190, NOW(), NOW()),
    (17, '2026-02-25', 6, 'WORKER_REPORT', 'J36 legere hausse chaleur', 191, NOW(), NOW()),
    (17, '2026-03-15', 2, 'WORKER_REPORT', 'J54 retour normale', 190, NOW(), NOW());
