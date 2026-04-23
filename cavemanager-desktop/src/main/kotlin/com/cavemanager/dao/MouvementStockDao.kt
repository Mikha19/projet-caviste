package com.cavemanager.dao

import com.cavemanager.model.MouvementStock
import com.cavemanager.model.TypeMouvement
import java.sql.ResultSet
import java.sql.Statement
import java.sql.Types

/**
 * DAO pour les mouvements de stock.
 *
 * IMPORTANT : Les méthodes d'entrée/sortie de stock doivent être atomiques :
 * elles modifient à la fois la table mouvements_stock ET la table produits.
 * On utilise donc des transactions JDBC explicites.
 *
 * Une transaction garantit que si l'une des deux opérations échoue,
 * l'autre est annulée (rollback), évitant des incohérences en base.
 */
object MouvementStockDao {

    /**
     * Retourne les 50 derniers mouvements, tous produits confondus.
     */
    fun findRecents(limite: Int = 50): List<MouvementStock> {
        val sql = """
            SELECT m.id, m.produit_id, m.type, m.quantite, m.date_mouvement, m.commentaire,
                   p.nom AS nom_produit
            FROM mouvements_stock m
            INNER JOIN produits p ON m.produit_id = p.id
            ORDER BY m.date_mouvement DESC
            LIMIT ?
        """.trimIndent()

        return DatabaseConfig.getConnection().use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setInt(1, limite)
                stmt.executeQuery().use { rs ->
                    val mouvements = mutableListOf<MouvementStock>()
                    while (rs.next()) {
                        mouvements.add(mapResultSet(rs))
                    }
                    mouvements
                }
            }
        }
    }

    /**
     * Retourne tous les mouvements d'un produit donné.
     */
    fun findByProduit(produitId: Int): List<MouvementStock> {
        val sql = """
            SELECT m.id, m.produit_id, m.type, m.quantite, m.date_mouvement, m.commentaire,
                   p.nom AS nom_produit
            FROM mouvements_stock m
            INNER JOIN produits p ON m.produit_id = p.id
            WHERE m.produit_id = ?
            ORDER BY m.date_mouvement DESC
        """.trimIndent()

        return DatabaseConfig.getConnection().use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setInt(1, produitId)
                stmt.executeQuery().use { rs ->
                    val mouvements = mutableListOf<MouvementStock>()
                    while (rs.next()) {
                        mouvements.add(mapResultSet(rs))
                    }
                    mouvements
                }
            }
        }
    }

    /**
     * Enregistre une ENTRÉE de stock.
     *
     * Opération atomique (transaction) :
     *   1. INSERT dans mouvements_stock
     *   2. UPDATE produits SET quantite_stock = quantite_stock + quantite
     *
     * @param mouvement Le mouvement à enregistrer (type doit être ENTREE)
     * @return Le mouvement avec son ID généré
     * @throws IllegalArgumentException si le type n'est pas ENTREE
     */
    fun entreeStock(mouvement: MouvementStock): MouvementStock {
        require(mouvement.type == TypeMouvement.ENTREE) {
            "Utilisez sortieStock() pour les sorties"
        }
        require(mouvement.quantite > 0) {
            "La quantité doit être strictement positive"
        }

        return DatabaseConfig.getConnection().use { conn ->
            // Désactiver l'auto-commit pour gérer manuellement la transaction
            conn.autoCommit = false
            try {
                val mouvementInsere = insertMouvement(conn, mouvement)

                // Mise à jour du stock : addition
                val sqlUpdate = "UPDATE produits SET quantite_stock = quantite_stock + ? WHERE id = ?"
                conn.prepareStatement(sqlUpdate).use { stmt ->
                    stmt.setInt(1, mouvement.quantite)
                    stmt.setInt(2, mouvement.produitId)
                    val lignesModifiees = stmt.executeUpdate()
                    if (lignesModifiees == 0) {
                        throw IllegalArgumentException("Produit introuvable (id=${mouvement.produitId})")
                    }
                }

                conn.commit() // ✅ Les deux opérations ont réussi
                mouvementInsere

            } catch (e: Exception) {
                conn.rollback() // ❌ Annulation si l'une des opérations a échoué
                throw e
            } finally {
                conn.autoCommit = true // Toujours remettre l'auto-commit
            }
        }
    }

    /**
     * Enregistre une SORTIE de stock.
     *
     * Opération atomique (transaction) :
     *   1. Vérification que le stock est suffisant
     *   2. INSERT dans mouvements_stock
     *   3. UPDATE produits SET quantite_stock = quantite_stock - quantite
     *
     * @throws IllegalStateException si le stock est insuffisant
     */
    fun sortieStock(mouvement: MouvementStock): MouvementStock {
        require(mouvement.type == TypeMouvement.SORTIE) {
            "Utilisez entreeStock() pour les entrées"
        }
        require(mouvement.quantite > 0) {
            "La quantité doit être strictement positive"
        }

        return DatabaseConfig.getConnection().use { conn ->
            conn.autoCommit = false
            try {
                // Vérification du stock disponible AVANT de modifier quoi que ce soit
                val stockActuel = getStockActuel(conn, mouvement.produitId)
                if (stockActuel < mouvement.quantite) {
                    throw IllegalStateException(
                        "Stock insuffisant : $stockActuel disponible, ${mouvement.quantite} demandé"
                    )
                }

                val mouvementInsere = insertMouvement(conn, mouvement)

                val sqlUpdate = "UPDATE produits SET quantite_stock = quantite_stock - ? WHERE id = ?"
                conn.prepareStatement(sqlUpdate).use { stmt ->
                    stmt.setInt(1, mouvement.quantite)
                    stmt.setInt(2, mouvement.produitId)
                    stmt.executeUpdate()
                }

                conn.commit()
                mouvementInsere

            } catch (e: Exception) {
                conn.rollback()
                throw e
            } finally {
                conn.autoCommit = true
            }
        }
    }

    // =========================================================================
    // Méthodes privées utilitaires
    // =========================================================================

    private fun insertMouvement(conn: java.sql.Connection, mouvement: MouvementStock): MouvementStock {
        val sql = """
            INSERT INTO mouvements_stock (produit_id, type, quantite, commentaire)
            VALUES (?, ?, ?, ?)
        """.trimIndent()

        return conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { stmt ->
            stmt.setInt(1, mouvement.produitId)
            stmt.setString(2, mouvement.type.name)
            stmt.setInt(3, mouvement.quantite)
            if (mouvement.commentaire != null) stmt.setString(4, mouvement.commentaire)
            else stmt.setNull(4, Types.VARCHAR)

            stmt.executeUpdate()
            stmt.generatedKeys.use { keys ->
                if (keys.next()) mouvement.copy(id = keys.getInt(1))
                else throw IllegalStateException("Impossible de récupérer l'ID du mouvement inséré")
            }
        }
    }

    private fun getStockActuel(conn: java.sql.Connection, produitId: Int): Int {
        return conn.prepareStatement("SELECT quantite_stock FROM produits WHERE id = ?").use { stmt ->
            stmt.setInt(1, produitId)
            stmt.executeQuery().use { rs ->
                if (rs.next()) rs.getInt("quantite_stock")
                else throw IllegalArgumentException("Produit introuvable (id=$produitId)")
            }
        }
    }

    private fun mapResultSet(rs: ResultSet): MouvementStock {
        return MouvementStock(
            id             = rs.getInt("id"),
            produitId      = rs.getInt("produit_id"),
            type           = TypeMouvement.valueOf(rs.getString("type")),
            quantite       = rs.getInt("quantite"),
            dateMouvement  = rs.getTimestamp("date_mouvement").toLocalDateTime(),
            commentaire    = rs.getString("commentaire"),
            nomProduit     = rs.getString("nom_produit")
        )
    }
}
