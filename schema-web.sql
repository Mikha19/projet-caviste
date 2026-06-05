-- ============================================================
-- CaveManager Web — Extensions de schéma pour web/mobile
-- MySQL 8.0+
-- ============================================================
-- Ajoute les tables pour clients, commandes et authentification

USE cavemanager;

-- ============================================================
-- TABLE : clients
-- ============================================================
CREATE TABLE IF NOT EXISTS clients (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    nom             VARCHAR(100) NOT NULL,
    prenom          VARCHAR(100) NOT NULL,
    telephone       VARCHAR(20),
    adresse         VARCHAR(255),
    code_postal     VARCHAR(10),
    ville           VARCHAR(100),
    pays            VARCHAR(50),
    role            ENUM('CLIENT', 'CAVISTE', 'ADMIN') DEFAULT 'CLIENT',
    statut          ENUM('ACTIF', 'INACTIF', 'SUSPENDU') DEFAULT 'ACTIF',
    date_inscription TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    derniere_connexion TIMESTAMP NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT 'Comptes clients et utilisateurs du système';

CREATE INDEX idx_clients_email ON clients(email);
CREATE INDEX idx_clients_role ON clients(role);
CREATE INDEX idx_clients_statut ON clients(statut);

-- ============================================================
-- TABLE : sessions
-- ============================================================
CREATE TABLE IF NOT EXISTS sessions (
    id              VARCHAR(255) PRIMARY KEY,
    client_id       INT NOT NULL,
    token           VARCHAR(500) NOT NULL UNIQUE,
    date_creation   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_expiration TIMESTAMP NOT NULL,
    adresse_ip      VARCHAR(50),
    user_agent      VARCHAR(500),
    actif           BOOLEAN DEFAULT TRUE,
    
    CONSTRAINT fk_session_client
        FOREIGN KEY (client_id) REFERENCES clients(id)
        ON DELETE CASCADE
) COMMENT 'Gestion des sessions d\'authentification';

CREATE INDEX idx_sessions_token ON sessions(token);
CREATE INDEX idx_sessions_client ON sessions(client_id);

-- ============================================================
-- TABLE : commandes
-- ============================================================
CREATE TABLE IF NOT EXISTS commandes (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    client_id       INT NOT NULL,
    numero_cmd      VARCHAR(50) NOT NULL UNIQUE,
    statut          ENUM('PANIER', 'CONFIRMEE', 'PRETE', 'RETIREE', 'ANNULEE') DEFAULT 'PANIER',
    montant_total   DECIMAL(10, 2) NOT NULL DEFAULT 0,
    date_creation   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_confirmation TIMESTAMP NULL,
    date_retrait    TIMESTAMP NULL,
    mode_paiement   ENUM('ESPECES', 'CARTE', 'CHEQUE', 'EN_ATTENTE') DEFAULT 'EN_ATTENTE',
    paiement_effectue BOOLEAN DEFAULT FALSE,
    notes           TEXT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_commande_client
        FOREIGN KEY (client_id) REFERENCES clients(id)
        ON DELETE RESTRICT
) COMMENT 'Commandes client en ligne';

CREATE INDEX idx_commandes_client ON commandes(client_id);
CREATE INDEX idx_commandes_statut ON commandes(statut);
CREATE INDEX idx_commandes_numero ON commandes(numero_cmd);
CREATE INDEX idx_commandes_date ON commandes(date_creation);

-- ============================================================
-- TABLE : lignes_commande
-- ============================================================
CREATE TABLE IF NOT EXISTS lignes_commande (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    commande_id     INT NOT NULL,
    produit_id      INT NOT NULL,
    quantite        INT NOT NULL DEFAULT 1,
    prix_unitaire   DECIMAL(10, 2) NOT NULL,
    prix_total      DECIMAL(10, 2) NOT NULL,
    date_ajout      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_ligne_commande
        FOREIGN KEY (commande_id) REFERENCES commandes(id)
        ON DELETE CASCADE,
        
    CONSTRAINT fk_ligne_produit
        FOREIGN KEY (produit_id) REFERENCES produits(id)
        ON DELETE RESTRICT,
        
    CONSTRAINT chk_quantite_ligne
        CHECK (quantite > 0)
) COMMENT 'Lignes de commande (articles du panier/commande)';

CREATE INDEX idx_lignes_commande ON lignes_commande(commande_id);
CREATE INDEX idx_lignes_produit ON lignes_commande(produit_id);

-- ============================================================
-- TABLE : notifications
-- ============================================================
CREATE TABLE IF NOT EXISTS notifications (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    client_id       INT,
    type            ENUM('COMMANDE', 'ALERTE_STOCK', 'PAIEMENT', 'SYSTEME') NOT NULL,
    titre           VARCHAR(255) NOT NULL,
    message         TEXT NOT NULL,
    lu              BOOLEAN DEFAULT FALSE,
    date_creation   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_lecture    TIMESTAMP NULL,
    
    CONSTRAINT fk_notif_client
        FOREIGN KEY (client_id) REFERENCES clients(id)
        ON DELETE SET NULL
) COMMENT 'Notifications système et commerciales';

CREATE INDEX idx_notif_client ON notifications(client_id);
CREATE INDEX idx_notif_lu ON notifications(lu);
CREATE INDEX idx_notif_date ON notifications(date_creation);

-- ============================================================
-- TABLE : historique_stock_alerte
-- ============================================================
CREATE TABLE IF NOT EXISTS historique_stock_alerte (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    produit_id      INT NOT NULL,
    quantite_actuelle INT NOT NULL,
    seuil_alerte    INT NOT NULL,
    date_alerte     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    traitee         BOOLEAN DEFAULT FALSE,
    
    CONSTRAINT fk_alerte_produit
        FOREIGN KEY (produit_id) REFERENCES produits(id)
        ON DELETE CASCADE
) COMMENT 'Historique des alertes stock pour traçabilité';

CREATE INDEX idx_alerte_produit ON historique_stock_alerte(produit_id);
CREATE INDEX idx_alerte_traitee ON historique_stock_alerte(traitee);

-- ============================================================
-- Triggers pour audit et automatisation
-- ============================================================

-- Trigger : Créer une alerte stock quand seuil dépassé
DELIMITER //
CREATE TRIGGER IF NOT EXISTS trg_alerte_stock_entree
AFTER INSERT ON mouvements_stock
FOR EACH ROW
BEGIN
    DECLARE qte_actuelle INT;
    DECLARE seuil INT;
    
    SELECT quantite_stock, seuil_alerte INTO qte_actuelle, seuil
    FROM produits WHERE id = NEW.produit_id;
    
    IF qte_actuelle <= seuil THEN
        INSERT INTO historique_stock_alerte (produit_id, quantite_actuelle, seuil_alerte)
        VALUES (NEW.produit_id, qte_actuelle, seuil);
    END IF;
END//
DELIMITER ;

-- Trigger : Mettre à jour le montant total de la commande
DELIMITER //
CREATE TRIGGER IF NOT EXISTS trg_update_montant_commande
AFTER INSERT ON lignes_commande
FOR EACH ROW
BEGIN
    UPDATE commandes
    SET montant_total = (
        SELECT SUM(prix_total) FROM lignes_commande WHERE commande_id = NEW.commande_id
    )
    WHERE id = NEW.commande_id;
END//
DELIMITER ;

DELIMITER //
CREATE TRIGGER IF NOT EXISTS trg_update_montant_commande_delete
AFTER DELETE ON lignes_commande
FOR EACH ROW
BEGIN
    UPDATE commandes
    SET montant_total = COALESCE((
        SELECT SUM(prix_total) FROM lignes_commande WHERE commande_id = OLD.commande_id
    ), 0)
    WHERE id = OLD.commande_id;
END//
DELIMITER ;

-- Trigger : Générer numéro de commande
DELIMITER //
CREATE TRIGGER IF NOT EXISTS trg_numero_commande
BEFORE INSERT ON commandes
FOR EACH ROW
BEGIN
    IF NEW.numero_cmd IS NULL OR NEW.numero_cmd = '' THEN
        SET NEW.numero_cmd = CONCAT('CMD-', DATE_FORMAT(NOW(), '%Y%m%d'), '-', LPAD(LAST_INSERT_ID(), 5, '0'));
    END IF;
END//
DELIMITER ;
