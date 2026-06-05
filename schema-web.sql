-- ============================================================
-- CaveManager Web — Extensions de schéma pour fonctionnalités web
-- À exécuter APRÈS schema.sql
-- ============================================================

USE cavemanager;

-- ============================================================
-- TABLE : clients (Customer accounts)
-- ============================================================
CREATE TABLE IF NOT EXISTS clients (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    email           VARCHAR(255)    NOT NULL UNIQUE,
    mot_de_passe    VARCHAR(255)    NOT NULL,   -- bcrypt hashed
    nom             VARCHAR(100)    NOT NULL,
    prenom          VARCHAR(100)    NOT NULL,
    telephone       VARCHAR(20),
    adresse         VARCHAR(255),
    code_postal     VARCHAR(10),
    ville           VARCHAR(100),
    date_inscription TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    derniere_connexion TIMESTAMP,
    actif           BOOLEAN         DEFAULT TRUE,
    created_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_email_format CHECK (email LIKE '%@%.%')
) COMMENT 'Comptes clients pour l''interface web';

CREATE INDEX idx_clients_email ON clients(email);
CREATE INDEX idx_clients_actif ON clients(actif);

-- ============================================================
-- TABLE : commandes (Orders)
-- ============================================================
CREATE TABLE IF NOT EXISTS commandes (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    client_id       INT             NOT NULL,
    numero_commande VARCHAR(50)     NOT NULL UNIQUE,  -- EX: CMD-2024-0001
    statut          ENUM('PANIER', 'VALIDEE', 'EN_PREPARATION', 'PRETE', 'RETIREE', 'ANNULEE') 
                    DEFAULT 'PANIER',
    montant_total   DECIMAL(10, 2)  NOT NULL DEFAULT 0,
    montant_paye    DECIMAL(10, 2)  DEFAULT 0,
    mode_paiement   ENUM('EN_BOUTIQUE', 'ONLINE', 'AUTRE') DEFAULT 'EN_BOUTIQUE',
    statut_paiement ENUM('NON_PAYE', 'PARTIELLEMENT_PAYE', 'PAYE') DEFAULT 'NON_PAYE',
    date_creation   TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    date_retrait_prevue DATETIME,
    date_retrait_effective DATETIME,
    notes           TEXT,
    created_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_commande_client
        FOREIGN KEY (client_id) REFERENCES clients(id)
        ON DELETE CASCADE
) COMMENT 'Commandes en ligne des clients (panier -> commande validée)';

CREATE INDEX idx_commandes_client ON commandes(client_id);
CREATE INDEX idx_commandes_statut ON commandes(statut);
CREATE INDEX idx_commandes_date ON commandes(date_creation);
CREATE INDEX idx_commandes_numero ON commandes(numero_commande);

-- ============================================================
-- TABLE : lignes_commande (Order items)
-- ============================================================
CREATE TABLE IF NOT EXISTS lignes_commande (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    commande_id     INT             NOT NULL,
    produit_id      INT             NOT NULL,
    quantite        INT             NOT NULL,
    prix_unitaire   DECIMAL(10, 2)  NOT NULL,
    sous_total      DECIMAL(10, 2)  NOT NULL,
    created_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_ligne_commande
        FOREIGN KEY (commande_id) REFERENCES commandes(id)
        ON DELETE CASCADE,
    
    CONSTRAINT fk_ligne_produit
        FOREIGN KEY (produit_id) REFERENCES produits(id),
    
    CONSTRAINT chk_quantite_ligne_positive
        CHECK (quantite > 0)
) COMMENT 'Lignes de détail des commandes (produits + quantités)';

CREATE INDEX idx_lignes_commande ON lignes_commande(commande_id);
CREATE INDEX idx_lignes_produit ON lignes_commande(produit_id);

-- ============================================================
-- TABLE : historique_commandes (Audit trail)
-- ============================================================
CREATE TABLE IF NOT EXISTS historique_commandes (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    commande_id     INT             NOT NULL,
    ancien_statut   VARCHAR(50),
    nouveau_statut  VARCHAR(50)     NOT NULL,
    date_changement TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    commentaire     TEXT,
    
    CONSTRAINT fk_historique_commande
        FOREIGN KEY (commande_id) REFERENCES commandes(id)
        ON DELETE CASCADE
) COMMENT 'Historique des changements de statut de commande';

CREATE INDEX idx_historique_commande ON historique_commandes(commande_id);
CREATE INDEX idx_historique_date ON historique_commandes(date_changement);

-- ============================================================
-- TABLE : alertes_stock (Low stock alerts for web display)
-- ============================================================
CREATE TABLE IF NOT EXISTS alertes_stock (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    produit_id      INT             NOT NULL,
    quantite_actuelle INT,
    seuil           INT             NOT NULL,
    lue             BOOLEAN         DEFAULT FALSE,
    date_alerte     TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    date_resolution DATETIME,
    
    CONSTRAINT fk_alerte_produit
        FOREIGN KEY (produit_id) REFERENCES produits(id)
        ON DELETE CASCADE
) COMMENT 'Alertes de stock pour le dashboard caviste';

CREATE INDEX idx_alertes_produit ON alertes_stock(produit_id);
CREATE INDEX idx_alertes_lue ON alertes_stock(lue);

-- ============================================================
-- TABLE : avis_produits (Product reviews)
-- ============================================================
CREATE TABLE IF NOT EXISTS avis_produits (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    produit_id      INT             NOT NULL,
    client_id       INT             NOT NULL,
    note            INT             NOT NULL,  -- 1-5 stars
    titre           VARCHAR(200),
    texte           TEXT,
    date_avis       TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    moderé           BOOLEAN        DEFAULT FALSE,
    
    CONSTRAINT fk_avis_produit
        FOREIGN KEY (produit_id) REFERENCES produits(id)
        ON DELETE CASCADE,
    
    CONSTRAINT fk_avis_client
        FOREIGN KEY (client_id) REFERENCES clients(id)
        ON DELETE CASCADE,
    
    CONSTRAINT chk_note_range
        CHECK (note >= 1 AND note <= 5),
    
    UNIQUE KEY uk_avis_produit_client (produit_id, client_id)
) COMMENT 'Avis et notes des clients sur les produits';

CREATE INDEX idx_avis_produit ON avis_produits(produit_id);
CREATE INDEX idx_avis_client ON avis_produits(client_id);
