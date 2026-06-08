-- ============================================================
-- CaveManager Pro — Script de création de la base de données
-- MySQL 8.0+
-- ============================================================
-- Exécution : mysql -u root -p < schema.sql
-- ============================================================

-- Création de la base
CREATE DATABASE IF NOT EXISTS cavemanager
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE cavemanager;

-- Création d'un utilisateur dédié (optionnel mais recommandé)
-- Adaptez le mot de passe selon votre environnement
CREATE USER IF NOT EXISTS 'cavemanager_user'@'localhost' IDENTIFIED BY 'passCaviste@user19';
GRANT ALL PRIVILEGES ON cavemanager.* TO 'cavemanager_user'@'localhost';
FLUSH PRIVILEGES;
SHOW GRANTS FOR 'cavemanager_user'@'localhost';
DROP USER 'cavemanager_user'@'localhost';

-- ============================================================
-- TABLE : categories
-- ============================================================
DROP TABLE IF EXISTS mouvements_stock;
DROP TABLE IF EXISTS produits;
DROP TABLE IF EXISTS categories;

CREATE TABLE categories (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    nom         VARCHAR(100) NOT NULL,
    description TEXT,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) COMMENT 'Catégories de produits (Vin rouge, Blanc, Champagne, Spiritueux...)';

-- ============================================================
-- TABLE : produits
-- ============================================================
CREATE TABLE produits (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    nom             VARCHAR(200)    NOT NULL,
    appellation     VARCHAR(150),
    millesime       YEAR,
    producteur      VARCHAR(150),
    region          VARCHAR(100),
    categorie_id    INT,
    prix_achat      DECIMAL(10, 2),
    prix_vente      DECIMAL(10, 2),
    quantite_stock  INT             NOT NULL DEFAULT 0,
    seuil_alerte    INT             NOT NULL DEFAULT 5,
    description     TEXT,
    created_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_produit_categorie
        FOREIGN KEY (categorie_id) REFERENCES categories(id)
        ON DELETE SET NULL,

    CONSTRAINT chk_quantite_positive
        CHECK (quantite_stock >= 0),

    CONSTRAINT chk_seuil_positive
        CHECK (seuil_alerte >= 0)
) COMMENT 'Produits en stock (vins, champagnes, spiritueux...)';

-- Index pour les recherches par nom et appellation
CREATE INDEX idx_produits_nom ON produits(nom);
CREATE INDEX idx_produits_appellation ON produits(appellation);
CREATE INDEX idx_produits_alerte ON produits(quantite_stock, seuil_alerte);

-- ============================================================
-- TABLE : mouvements_stock
-- ============================================================
CREATE TABLE mouvements_stock (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    produit_id      INT             NOT NULL,
    type            ENUM('ENTREE', 'SORTIE') NOT NULL,
    quantite        INT             NOT NULL,
    date_mouvement  TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    commentaire     VARCHAR(255),

    CONSTRAINT fk_mouvement_produit
        FOREIGN KEY (produit_id) REFERENCES produits(id),
        -- Pas de ON DELETE CASCADE : on veut conserver l'historique

    CONSTRAINT chk_quantite_mouvement
        CHECK (quantite > 0)
) COMMENT 'Historique de tous les mouvements de stock';

-- Index pour les requêtes par produit et par date
CREATE INDEX idx_mouvements_produit ON mouvements_stock(produit_id);
CREATE INDEX idx_mouvements_date ON mouvements_stock(date_mouvement);
