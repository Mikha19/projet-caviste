-- ============================================================
-- CaveManager Pro — Données de démonstration
-- À exécuter APRÈS schema.sql
-- ============================================================

USE cavemanager;

-- ===== CATÉGORIES =====
INSERT INTO categories (nom, description) VALUES
    ('Vin rouge',       'Vins rouges de toutes régions'),
    ('Vin blanc',       'Vins blancs secs, moelleux et liquoreux'),
    ('Vin rosé',        'Vins rosés'),
    ('Champagne',       'Champagnes et vins effervescents'),
    ('Spiritueux',      'Cognac, armagnac, whisky, rhum...'),
    ('Bière artisanale','Bières produites par des brasseries indépendantes');

-- ===== PRODUITS =====
INSERT INTO produits (nom, appellation, millesime, producteur, region, categorie_id, prix_achat, prix_vente, quantite_stock, seuil_alerte, description) VALUES
-- Vins rouges (categorie_id=1)
('Château Margaux', 'Bordeaux AOC', 2018, 'Château Margaux', 'Bordeaux', 1, 280.00, 380.00, 12, 3, 'Grand cru classé, notes de cassis et de violette'),
('Gevrey-Chambertin', 'Gevrey-Chambertin AOC', 2019, 'Domaine Rossignol', 'Bourgogne', 1, 45.00, 68.00, 24, 6, 'Pinot noir élégant, tanins soyeux'),
('Côtes du Rhône', 'Côtes du Rhône AOC', 2021, 'Cave de Tain', 'Vallée du Rhône', 1, 8.50, 13.50, 3, 8, 'Assemblage grenache/syrah, fruité et accessible'),
('Pomerol', 'Pomerol AOC', 2017, 'Château La Fleur', 'Bordeaux', 1, 35.00, 52.00, 18, 6, 'Merlot dominant, rond et velouté'),
('Madiran', 'Madiran AOC', 2020, 'Domaine Berthoumieu', 'Sud-Ouest', 1, 12.00, 18.00, 30, 5, 'Tannat puissant, bon potentiel de garde'),
('Bandol Rouge', 'Bandol AOC', 2019, 'Domaine Tempier', 'Provence', 1, 28.00, 42.00, 2, 5, 'Mourvèdre, épicé et complexe'),
('Saint-Émilion Grand Cru', 'Saint-Émilion Grand Cru AOC', 2016, 'Château Beau-Séjour', 'Bordeaux', 1, 38.00, 58.00, 15, 4, 'Merlot/cabernet franc, structure et finesse'),
('Minervois', 'Minervois AOC', 2021, 'Domaine Borie de Maurel', 'Languedoc', 1, 7.50, 12.00, 42, 10, 'Syrah/grenache, chaleureux et fruité'),
-- Vins blancs (categorie_id=2)
('Chablis Premier Cru', 'Chablis 1er Cru AOC', 2020, 'Domaine Laroche', 'Bourgogne', 2, 22.00, 34.00, 6, 6, 'Chardonnay minéral, notes iodées'),
('Pouilly-Fumé', 'Pouilly-Fumé AOC', 2021, 'Domaine Dagueneau', 'Loire', 2, 18.00, 28.00, 4, 5, 'Sauvignon blanc de référence'),
('Condrieu', 'Condrieu AOC', 2020, 'Guigal', 'Vallée du Rhône', 2, 42.00, 62.00, 8, 3, 'Viognier floral, pêche et abricot'),
('Alsace Riesling', 'Alsace AOC', 2021, 'Domaine Trimbach', 'Alsace', 2, 14.00, 21.00, 20, 6, 'Riesling sec, agrumes et minéralité'),
('Sancerre Blanc', 'Sancerre AOC', 2022, 'Henri Bourgeois', 'Loire', 2, 16.50, 25.00, 12, 5, 'Sauvignon blanc vif, notes de pierre à fusil'),
('Meursault', 'Meursault AOC', 2019, 'Domaine Leflaive', 'Bourgogne', 2, 55.00, 82.00, 3, 3, 'Chardonnay beurré, notes de noisette'),
-- Vins rosés (categorie_id=3)
('Côtes de Provence Rosé', 'Côtes de Provence AOC', 2022, 'Château Miraval', 'Provence', 3, 14.00, 22.00, 18, 6, 'Rosé pâle, frais et élégant'),
('Tavel', 'Tavel AOC', 2022, 'Domaine de la Mordorée', 'Vallée du Rhône', 3, 11.00, 17.00, 3, 5, 'Grenache dominant, puissant et gastronomique'),
-- Champagnes (categorie_id=4)
('Champagne Brut NV', 'Champagne AOC', NULL, 'Moët & Chandon', 'Champagne', 4, 28.00, 42.00, 24, 6, 'Champagne non-millésimé, pomme verte et brioche'),
('Champagne Blanc de Blancs', 'Champagne AOC', 2015, 'Billecart-Salmon', 'Champagne', 4, 55.00, 82.00, 9, 3, 'Chardonnay pur, bulles fines et persistantes'),
('Crémant d''Alsace', 'Crémant d''Alsace AOC', NULL, 'Wolfberger', 'Alsace', 4, 9.50, 15.00, 2, 8, 'Alternative économique au champagne'),
('Champagne Rosé', 'Champagne AOC', NULL, 'Laurent-Perrier', 'Champagne', 4, 42.00, 65.00, 12, 4, 'Rosé de macération, fruits rouges éclatants'),
-- Spiritueux (categorie_id=5)
('Cognac VSOP', NULL, NULL, 'Rémy Martin', 'Cognac', 5, 32.00, 48.00, 8, 3, 'Cognac vieilli 4 ans minimum'),
('Armagnac XO', NULL, 2005, 'Château de Laubade', 'Armagnac', 5, 68.00, 98.00, 4, 2, 'Armagnac millésimé, remarquable complexité'),
('Calvados VSOP', NULL, NULL, 'Père Magloire', 'Normandie', 5, 22.00, 35.00, 6, 3, 'Eau de vie de pomme vieillie'),
('Single Malt 12 ans', NULL, NULL, 'Glenfiddich', 'Écosse', 5, 38.00, 58.00, 5, 3, 'Whisky single malt fruité et accessible'),
('Rhum Agricole Blanc', NULL, NULL, 'La Mauny', 'Martinique', 5, 18.00, 27.00, 0, 3, 'Rhum de canne fraîche, idéal pour cocktails'),
-- Bières artisanales (categorie_id=6)
('IPA Houblonnée', NULL, NULL, 'Brasserie de la Goutte d''Or', 'Île-de-France', 6, 2.80, 4.50, 48, 12, 'India Pale Ale, amère et aromatique'),
('Stout Irlandais', NULL, NULL, 'Brasserie Ninkasi', 'Rhône-Alpes', 6, 2.50, 4.00, 36, 12, 'Bière noire, notes de café et chocolat'),
('Blonde Artisanale', NULL, NULL, 'Brasserie Gallia', 'Île-de-France', 6, 2.20, 3.80, 2, 12, 'Blonde légère et désaltérante'),
('Triple Abbaye', NULL, NULL, 'Brasserie des Roches', 'Grand Est', 6, 3.50, 5.50, 24, 8, 'Bière forte dorée, 9% alc., épicée');

-- ===== MOUVEMENTS DE STOCK DE DÉMO =====
-- Quelques mouvements pour illustrer l'historique
INSERT INTO mouvements_stock (produit_id, type, quantite, commentaire) VALUES
(1,  'ENTREE', 12, 'Réception commande fournisseur BL-2024-001'),
(2,  'ENTREE', 24, 'Réception commande fournisseur BL-2024-001'),
(3,  'ENTREE', 12, 'Réception commande fournisseur BL-2024-001'),
(3,  'SORTIE',  6, 'Vente dégustation samedi'),
(3,  'SORTIE',  3, 'Vente directe'),
(9,  'ENTREE', 12, 'Réception BL-2024-002'),
(9,  'SORTIE',  6, 'Vente semaine 12'),
(17, 'ENTREE', 24, 'Stock fêtes de fin d''année'),
(17, 'SORTIE', 12, 'Ventes décembre'),
(19, 'ENTREE', 12, 'Commande spéciale'),
(19, 'SORTIE', 10, 'Ventes diverses'),
(25, 'ENTREE',  3, 'Commande test nouveau produit'),
(25, 'SORTIE',  3, 'Ventes semaine 8');
