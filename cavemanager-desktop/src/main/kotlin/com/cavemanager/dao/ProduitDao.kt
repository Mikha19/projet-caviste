package com.cavemanager.dao

import com.cavemanager.model.Produit
import java.math.BigDecimal
import java.sql.ResultSet
import java.sql.Statement
import java.sql.Types

/**
 * DAO (Data Access Object) pour l'entité Produit.
 *
 * Responsabilités :
 *   - Lire et écrire des Produit dans la base de données
 *   - Convertir les ResultSet SQL en objets Kotlin (mapping)
 *   - Utiliser des PreparedStatement (jamais de concaténation de chaînes SQL !)
 *
 * Ce que ce DAO ne doit PAS faire :
 *   - Logique métier (ex: vérifier si un prix est cohérent)
 *   - Interaction avec l'interface utilisateur
 *   - Gérer des transactions multi-tables (c'est le rôle du Service)
 */
object ProduitDao {

    // =========================================================================
    // READ
    // =========================================================================

    /**
     * Retourne tous les produits triés par nom.
     *
     * NOTE PÉDAGOGIQUE :
     *   Le pattern "use { }" ferme automatiquement la connexion à la fin du bloc,
     *   même si une exception est levée. C'est l'équivalent Kotlin du try-with-resources Java.
     */
    fun findAll(): List<Produit> {
        val sql = """
            SELECT id, nom, appellation, millesime, producteur, region,
                   categorie_id, prix_achat, prix_vente,
                   quantite_stock, seuil_alerte, description, created_at
            FROM produits
            ORDER BY nom ASC
        """.trimIndent()

        return DatabaseConfig.getConnection().use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.executeQuery().use { rs ->
                    val produits = mutableListOf<Produit>()
                    while (rs.next()) {
                        produits.add(mapResultSet(rs))
                    }
                    produits
                }
            }
        }
    }

    /**
     * Recherche des produits dont le nom ou l'appellation contient le terme donné.
     * La recherche est insensible à la casse (LIKE en MySQL).
     *
     * @param terme Le texte à rechercher (peut être vide pour tout retourner)
     */
    fun rechercher(terme: String): List<Produit> {
        if (terme.isBlank()) return findAll()

        val sql = """
            SELECT id, nom, appellation, millesime, producteur, region,
                   categorie_id, prix_achat, prix_vente,
                   quantite_stock, seuil_alerte, description, created_at
            FROM produits
            WHERE nom LIKE ? OR appellation LIKE ?
            ORDER BY nom ASC
        """.trimIndent()

        val pattern = "%$terme%"

        return DatabaseConfig.getConnection().use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setString(1, pattern)
                stmt.setString(2, pattern)
                stmt.executeQuery().use { rs ->
                    val produits = mutableListOf<Produit>()
                    while (rs.next()) {
                        produits.add(mapResultSet(rs))
                    }
                    produits
                }
            }
        }
    }

    /**
     * Retourne un produit par son ID, ou null s'il n'existe pas.
     */
    fun findById(id: Int): Produit? {
        val sql = """
            SELECT id, nom, appellation, millesime, producteur, region,
                   categorie_id, prix_achat, prix_vente,
                   quantite_stock, seuil_alerte, description, created_at
            FROM produits
            WHERE id = ?
        """.trimIndent()

        return DatabaseConfig.getConnection().use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setInt(1, id)
                stmt.executeQuery().use { rs ->
                    if (rs.next()) mapResultSet(rs) else null
                }
            }
        }
    }

    /**
     * Retourne tous les produits en dessous de leur seuil d'alerte.
     */
    fun findEnAlerte(): List<Produit> {
        val sql = """
            SELECT id, nom, appellation, millesime, producteur, region,
                   categorie_id, prix_achat, prix_vente,
                   quantite_stock, seuil_alerte, description, created_at
            FROM produits
            WHERE quantite_stock <= seuil_alerte
            ORDER BY quantite_stock ASC
        """.trimIndent()

        return DatabaseConfig.getConnection().use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.executeQuery().use { rs ->
                    val produits = mutableListOf<Produit>()
                    while (rs.next()) {
                        produits.add(mapResultSet(rs))
                    }
                    produits
                }
            }
        }
    }

    // =========================================================================
    // CREATE
    // =========================================================================

    /**
     * Insère un nouveau produit en base et retourne le produit avec son ID généré.
     *
     * NOTE PÉDAGOGIQUE :
     *   RETURN_GENERATED_KEYS permet de récupérer l'ID auto-incrémenté
     *   généré par MySQL après l'INSERT.
     *
     * @param produit Le produit à insérer (son id sera ignoré)
     * @return Le produit avec son nouvel ID renseigné
     * @throws IllegalArgumentException si le produit a déjà un ID
     */
    fun save(produit: Produit): Produit {
        require(produit.estNouveau) {
            "Utilisez update() pour modifier un produit existant (id=${produit.id})"
        }

        val sql = """
            INSERT INTO produits
                (nom, appellation, millesime, producteur, region,
                 categorie_id, prix_achat, prix_vente,
                 quantite_stock, seuil_alerte, description)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()

        return DatabaseConfig.getConnection().use { conn ->
            conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { stmt ->
                // Paramètres obligatoires
                stmt.setString(1, produit.nom)

                // Paramètres optionnels : on utilise setNull si la valeur est null
                setStringOrNull(stmt, 2, produit.appellation)
                setIntOrNull(stmt, 3, produit.millesime)
                setStringOrNull(stmt, 4, produit.producteur)
                setStringOrNull(stmt, 5, produit.region)
                setIntOrNull(stmt, 6, produit.categorieId)
                setBigDecimalOrNull(stmt, 7, produit.prixAchat)
                setBigDecimalOrNull(stmt, 8, produit.prixVente)

                stmt.setInt(9, produit.quantiteStock)
                stmt.setInt(10, produit.seuilAlerte)
                setStringOrNull(stmt, 11, produit.description)

                stmt.executeUpdate()

                // Récupération de l'ID généré
                stmt.generatedKeys.use { keys ->
                    if (keys.next()) {
                        produit.copy(id = keys.getInt(1))
                    } else {
                        throw IllegalStateException("Impossible de récupérer l'ID généré après INSERT")
                    }
                }
            }
        }
    }

    // =========================================================================
    // UPDATE
    // =========================================================================

    /**
     * Met à jour un produit existant en base.
     *
     * @param produit Le produit avec les nouvelles valeurs (doit avoir un ID valide)
     * @return true si la mise à jour a réussi (1 ligne modifiée)
     */
    fun update(produit: Produit): Boolean {
        require(!produit.estNouveau) {
            "Utilisez save() pour insérer un nouveau produit"
        }

        val sql = """
            UPDATE produits
            SET nom = ?, appellation = ?, millesime = ?, producteur = ?, region = ?,
                categorie_id = ?, prix_achat = ?, prix_vente = ?,
                seuil_alerte = ?, description = ?
            WHERE id = ?
        """.trimIndent()

        // NOTE : quantite_stock n'est pas mis à jour ici.
        // Les modifications de stock se font uniquement via MouvementStockDao.

        return DatabaseConfig.getConnection().use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setString(1, produit.nom)
                setStringOrNull(stmt, 2, produit.appellation)
                setIntOrNull(stmt, 3, produit.millesime)
                setStringOrNull(stmt, 4, produit.producteur)
                setStringOrNull(stmt, 5, produit.region)
                setIntOrNull(stmt, 6, produit.categorieId)
                setBigDecimalOrNull(stmt, 7, produit.prixAchat)
                setBigDecimalOrNull(stmt, 8, produit.prixVente)
                stmt.setInt(9, produit.seuilAlerte)
                setStringOrNull(stmt, 10, produit.description)
                stmt.setInt(11, produit.id)

                stmt.executeUpdate() == 1
            }
        }
    }

    // =========================================================================
    // DELETE
    // =========================================================================

    /**
     * Supprime un produit par son ID.
     *
     * ATTENTION : La BDD va lever une SQLException si ce produit a des
     * mouvements de stock (contrainte de clé étrangère). Gérez cette exception
     * dans le contrôleur avec un message utilisateur explicite.
     *
     * @return true si la suppression a réussi (1 ligne supprimée)
     */
    fun delete(id: Int): Boolean {
        return DatabaseConfig.getConnection().use { conn ->
            conn.prepareStatement("DELETE FROM produits WHERE id = ?").use { stmt ->
                stmt.setInt(1, id)
                stmt.executeUpdate() == 1
            }
        }
    }

    // =========================================================================
    // MAPPING ResultSet → Produit
    // =========================================================================

    /**
     * Convertit une ligne de ResultSet en objet Produit.
     *
     * NOTE PÉDAGOGIQUE :
     *   rs.getInt() retourne 0 pour NULL en SQL.
     *   Pour distinguer 0 et NULL, on utilise rs.wasNull() après l'appel.
     *   C'est une subtilité importante du JDBC.
     */
    private fun mapResultSet(rs: ResultSet): Produit {
        val millesime = rs.getInt("millesime").let { if (rs.wasNull()) null else it }
        val categorieId = rs.getInt("categorie_id").let { if (rs.wasNull()) null else it }

        return Produit(
            id           = rs.getInt("id"),
            nom          = rs.getString("nom"),
            appellation  = rs.getString("appellation"),
            millesime    = millesime,
            producteur   = rs.getString("producteur"),
            region       = rs.getString("region"),
            categorieId  = categorieId,
            prixAchat    = rs.getBigDecimal("prix_achat"),
            prixVente    = rs.getBigDecimal("prix_vente"),
            quantiteStock = rs.getInt("quantite_stock"),
            seuilAlerte  = rs.getInt("seuil_alerte"),
            description  = rs.getString("description"),
            createdAt    = rs.getTimestamp("created_at")?.toLocalDateTime()
        )
    }

    // =========================================================================
    // HELPERS pour les paramètres nullable
    // =========================================================================

    private fun setStringOrNull(stmt: java.sql.PreparedStatement, index: Int, value: String?) {
        if (value != null) stmt.setString(index, value)
        else stmt.setNull(index, Types.VARCHAR)
    }

    private fun setIntOrNull(stmt: java.sql.PreparedStatement, index: Int, value: Int?) {
        if (value != null) stmt.setInt(index, value)
        else stmt.setNull(index, Types.INTEGER)
    }

    private fun setBigDecimalOrNull(stmt: java.sql.PreparedStatement, index: Int, value: BigDecimal?) {
        if (value != null) stmt.setBigDecimal(index, value)
        else stmt.setNull(index, Types.DECIMAL)
    }
}
