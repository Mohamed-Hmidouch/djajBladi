-- V15: Peuplement des prix unitaires pour les items de stock existants
-- Prix du marche marocain (DH) pour le calcul du cout de revient dynamique
-- Aliments (DH par unite definie)
UPDATE stock_items SET unit_price = 5.50   WHERE id = 1;  -- Starter Feed Premium       DH/kg
UPDATE stock_items SET unit_price = 180.00 WHERE id = 2;  -- Aliment Demarrage          DH/sac 50kg
UPDATE stock_items SET unit_price = 170.00 WHERE id = 3;  -- Aliment Croissance         DH/sac 50kg
UPDATE stock_items SET unit_price = 165.00 WHERE id = 4;  -- Aliment Finition           DH/sac 50kg
UPDATE stock_items SET unit_price = 8.50   WHERE id = 5;  -- Tourteau de soja           DH/kg
UPDATE stock_items SET unit_price = 4.20   WHERE id = 6;  -- Mais concasse              DH/kg
-- Vaccins (DH par dose)
UPDATE stock_items SET unit_price = 120.00 WHERE id = 7;  -- Vaccin Newcastle (HB1)     DH/dose 500ml
UPDATE stock_items SET unit_price = 150.00 WHERE id = 8;  -- Vaccin Gumboro             DH/dose 500ml
UPDATE stock_items SET unit_price = 140.00 WHERE id = 9;  -- Vaccin Bronchite H120      DH/dose 500ml
UPDATE stock_items SET unit_price = 95.00  WHERE id = 10; -- Vaccin Marek               DH/dose 250ml
UPDATE stock_items SET unit_price = 110.00 WHERE id = 11; -- Vaccin Salmonelle          DH/dose 250ml
-- Vitamines et supplements (DH par unite)
UPDATE stock_items SET unit_price = 85.00  WHERE id = 12; -- Complexe vitaminique A+D3+E DH/flacon 1L
UPDATE stock_items SET unit_price = 45.00  WHERE id = 13; -- Vitamine C (acide ascorbique) DH/kg
UPDATE stock_items SET unit_price = 55.00  WHERE id = 14; -- Electrolytes rehydratants  DH/sachet 1kg
UPDATE stock_items SET unit_price = 75.00  WHERE id = 15; -- Probiotiques digestifs     DH/flacon 500g
UPDATE stock_items SET unit_price = 90.00  WHERE id = 16; -- Selenium + Vitamine E injectable DH/flacon 100ml
