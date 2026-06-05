package com.cavemanager.api.data

import com.cavemanager.api.models.CommandeDTO
import com.cavemanager.api.models.LigneCommandeDTO
import java.sql.ResultSet
import java.sql.Statement
import java.sql.Types
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * DAO pour gérer les commandes et leurs lignes
 */
object CommandeDao {
    
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
    
    /**
     * Crée une nouvelle commande (panier initial)
     */
    fun createPanier(clientId: Int): CommandeDTO? {
        val numeroCommande = "CMD-" + System.currentTimeMillis()
        
        val sql = """
            INSERT INTO commandes (client_id, numero_commande, statut, montant_total)
            VALUES (?, ?, 'PANIER', 0)
        """.trimIndent()
        
        return DatabasePool.getConnection().use { conn ->
            conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { stmt ->
                stmt.setInt(1, clientId)
                stmt.setString(2, numeroCommande)
                
                if (stmt.executeUpdate() > 0) {
                    stmt.generatedKeys.use { rs ->
                        if (rs.next()) {
                            val id = rs.getInt(1)
                            CommandeDTO(
                                id = id,
                                numerCommande = numeroCommande,
                                clientId = clientId,
                                statut = "PANIER",
                                montantTotal = 0.0,
                                dateCreation = LocalDateTime.now().format(formatter),
                                lignes = emptyList()
                            )
                        } else null
                    }
                } else null
            }
        }
    }
    
    /**
     * Récupère le panier actif d'un client
     */
    fun getPanierActif(clientId: Int): CommandeDTO? {
        val sql = """
            SELECT id, numero_commande, client_id, statut, montant_total, montant_paye,
                   mode_paiement, statut_paiement, date_creation, date_retrait_prevue,
                   date_retrait_effective, notes
            FROM commandes
            WHERE client_id = ? AND statut = 'PANIER'
            LIMIT 1
        """.trimIndent()
        
        return DatabasePool.getConnection().use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setInt(1, clientId)
                stmt.executeQuery().use { rs ->
                    if (rs.next()) {
                        val commande = mapResultSet(rs)
                        // Charger les lignes
                        val lignes = getLignesCommande(commande.id)
                        commande.copy(lignes = lignes)
                    } else null
                }
            }
        }
    }
    
    /**
     * Ajoute un produit au panier
     */
    fun addToCart(commandeId: Int, produitId: Int, quantite: Int, prixUnitaire: Double): LigneCommandeDTO? {
        // Vérifier si le produit est déjà dans le panier
        val existingLine = getLigneCommande(commandeId, produitId)
        
        val result = if (existingLine != null) {
            // Mettre à jour la quantité
            updateCartLine(existingLine.id!!, existingLine.quantite + quantite)
        } else {
            // Ajouter une nouvelle ligne
            val sql = """
                INSERT INTO lignes_commande (commande_id, produit_id, quantite, prix_unitaire, sous_total)
                VALUES (?, ?, ?, ?, ?)
            """.trimIndent()
            
            val sousTotal = quantite * prixUnitaire
            
            DatabasePool.getConnection().use { conn ->
                conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { stmt ->
                    stmt.setInt(1, commandeId)
                    stmt.setInt(2, produitId)
                    stmt.setInt(3, quantite)
                    stmt.setDouble(4, prixUnitaire)
                    stmt.setDouble(5, sousTotal)
                    
                    if (stmt.executeUpdate() > 0) {
                        stmt.generatedKeys.use { rs ->
                            if (rs.next()) rs.getInt(1) else null
                        }
                    } else null
                }
            }
        }
        
        // Recalculer le montant total de la commande
        if (result != null) {
            updateMontantTotal(commandeId)
            return getLigneCommandeById(if (existingLine != null) existingLine.id!! else result!!)
        }
        
        return null
    }
    
    /**
     * Met à jour une ligne du panier
     */
    fun updateCartLine(ligneId: Int, nouveleQuantite: Int): Boolean {
        return DatabasePool.getConnection().use { conn ->
            val sql = """
                UPDATE lignes_commande 
                SET quantite = ?, sous_total = prix_unitaire * ?
                WHERE id = ?
            """.trimIndent()
            
            conn.prepareStatement(sql).use { stmt ->
                stmt.setInt(1, nouveleQuantite)
                stmt.setInt(2, nouveleQuantite)
                stmt.setInt(3, ligneId)
                
                val updated = stmt.executeUpdate() > 0
                
                if (updated) {
                    // Récupérer la commande et recalculer le montant
                    val commandeId = getCommandeIdByLigne(ligneId)
                    if (commandeId != null) {
                        updateMontantTotal(commandeId)
                    }
                }
                
                updated
            }
        }
    }
    
    /**
     * Supprime une ligne du panier
     */
    fun removeFromCart(ligneId: Int): Boolean {
        val commandeId = getCommandeIdByLigne(ligneId)
        
        return DatabasePool.getConnection().use { conn ->
            val sql = "DELETE FROM lignes_commande WHERE id = ?"
            
            conn.prepareStatement(sql).use { stmt ->
                stmt.setInt(1, ligneId)
                val deleted = stmt.executeUpdate() > 0
                
                if (deleted && commandeId != null) {
                    updateMontantTotal(commandeId)
                }
                
                deleted
            }
        }
    }
    
    /**
     * Valide une commande (passe de PANIER à VALIDEE)
     */
    fun validateOrder(commandeId: Int, dateRetraitPrevue: String, notes: String? = null): Boolean {
        return DatabasePool.getConnection().use { conn ->
            val sql = """
                UPDATE commandes
                SET statut = 'VALIDEE', date_retrait_prevue = ?, notes = ?
                WHERE id = ? AND statut = 'PANIER'
            """.trimIndent()
            
            conn.prepareStatement(sql).use { stmt ->
                stmt.setString(1, dateRetraitPrevue)
                stmt.setString(2, notes)
                stmt.setInt(3, commandeId)
                
                val updated = stmt.executeUpdate() > 0
                
                if (updated) {
                    // Enregistrer dans l'historique
                    recordStatusChange(commandeId, "PANIER", "VALIDEE")
                }
                
                updated
            }
        }
    }
    
    /**
     * Met à jour le statut d'une commande
     */
    fun updateStatus(commandeId: Int, newStatus: String, notes: String? = null): Boolean {
        return DatabasePool.getConnection().use { conn ->
            val sql = """
                UPDATE commandes
                SET statut = ?, notes = ? WHERE id = ?
            """.trimIndent()
            
            conn.prepareStatement(sql).use { stmt ->
                stmt.setString(1, newStatus)
                stmt.setString(2, notes)
                stmt.setInt(3, commandeId)
                
                val updated = stmt.executeUpdate() > 0
                
                if (updated) {
                    recordStatusChange(commandeId, null, newStatus, notes)
                }
                
                updated
            }
        }
    }
    
    /**
     * Récupère toutes les commandes d'un client
     */
    fun findByClientId(clientId: Int, statut: String? = null): List<CommandeDTO> {
        val sql = if (statut != null) {
            """
                SELECT id, numero_commande, client_id, statut, montant_total, montant_paye,
                       mode_paiement, statut_paiement, date_creation, date_retrait_prevue,
                       date_retrait_effective, notes
                FROM commandes
                WHERE client_id = ? AND statut = ?
                ORDER BY date_creation DESC
            """.trimIndent()
        } else {
            """
                SELECT id, numero_commande, client_id, statut, montant_total, montant_paye,
                       mode_paiement, statut_paiement, date_creation, date_retrait_prevue,
                       date_retrait_effective, notes
                FROM commandes
                WHERE client_id = ?
                ORDER BY date_creation DESC
            """.trimIndent()
        }
        
        return DatabasePool.getConnection().use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setInt(1, clientId)
                if (statut != null) stmt.setString(2, statut)
                
                stmt.executeQuery().use { rs ->
                    val commandes = mutableListOf<CommandeDTO>()
                    while (rs.next()) {
                        val commande = mapResultSet(rs)
                        val lignes = getLignesCommande(commande.id)
                        commandes.add(commande.copy(lignes = lignes))
                    }
                    commandes
                }
            }
        }
    }
    
    /**
     * Récupère une commande par ID
     */
    fun findById(commandeId: Int): CommandeDTO? {
        val sql = """
            SELECT id, numero_commande, client_id, statut, montant_total, montant_paye,
                   mode_paiement, statut_paiement, date_creation, date_retrait_prevue,
                   date_retrait_effective, notes
            FROM commandes
            WHERE id = ?
        """.trimIndent()
        
        return DatabasePool.getConnection().use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setInt(1, commandeId)
                stmt.executeQuery().use { rs ->
                    if (rs.next()) {
                        val commande = mapResultSet(rs)
                        val lignes = getLignesCommande(commande.id)
                        commande.copy(lignes = lignes)
                    } else null
                }
            }
        }
    }
    
    /**
     * Récupère les lignes d'une commande
     */
    private fun getLignesCommande(commandeId: Int): List<LigneCommandeDTO> {
        val sql = """
            SELECT lc.id, lc.produit_id, p.nom, lc.quantite, lc.prix_unitaire, lc.sous_total
            FROM lignes_commande lc
            JOIN produits p ON lc.produit_id = p.id
            WHERE lc.commande_id = ?
            ORDER BY lc.id
        """.trimIndent()
        
        return DatabasePool.getConnection().use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setInt(1, commandeId)
                stmt.executeQuery().use { rs ->
                    val lignes = mutableListOf<LigneCommandeDTO>()
                    while (rs.next()) {
                        lignes.add(
                            LigneCommandeDTO(
                                id = rs.getInt("id"),
                                produitId = rs.getInt("produit_id"),
                                produitNom = rs.getString("nom"),
                                quantite = rs.getInt("quantite"),
                                prixUnitaire = rs.getDouble("prix_unitaire"),
                                sousTotal = rs.getDouble("sous_total")
                            )
                        )
                    }
                    lignes
                }
            }
        }
    }
    
    private fun getLigneCommande(commandeId: Int, produitId: Int): LigneCommandeDTO? {
        val sql = """
            SELECT id, produit_id, quantite, prix_unitaire, sous_total
            FROM lignes_commande
            WHERE commande_id = ? AND produit_id = ?
        """.trimIndent()
        
        return DatabasePool.getConnection().use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setInt(1, commandeId)
                stmt.setInt(2, produitId)
                stmt.executeQuery().use { rs ->
                    if (rs.next()) {
                        LigneCommandeDTO(
                            id = rs.getInt("id"),
                            produitId = rs.getInt("produit_id"),
                            quantite = rs.getInt("quantite"),
                            prixUnitaire = rs.getDouble("prix_unitaire"),
                            sousTotal = rs.getDouble("sous_total")
                        )
                    } else null
                }
            }
        }
    }
    
    private fun getLigneCommandeById(ligneId: Int): LigneCommandeDTO? {
        val sql = """
            SELECT id, commande_id, produit_id, quantite, prix_unitaire, sous_total
            FROM lignes_commande
            WHERE id = ?
        """.trimIndent()
        
        return DatabasePool.getConnection().use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setInt(1, ligneId)
                stmt.executeQuery().use { rs ->
                    if (rs.next()) {
                        LigneCommandeDTO(
                            id = rs.getInt("id"),
                            produitId = rs.getInt("produit_id"),
                            quantite = rs.getInt("quantite"),
                            prixUnitaire = rs.getDouble("prix_unitaire"),
                            sousTotal = rs.getDouble("sous_total")
                        )
                    } else null
                }
            }
        }
    }
    
    private fun getCommandeIdByLigne(ligneId: Int): Int? {
        val sql = "SELECT commande_id FROM lignes_commande WHERE id = ?"
        
        return DatabasePool.getConnection().use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setInt(1, ligneId)
                stmt.executeQuery().use { rs ->
                    if (rs.next()) rs.getInt("commande_id") else null
                }
            }
        }
    }
    
    private fun updateMontantTotal(commandeId: Int) {
        val sql = """
            UPDATE commandes
            SET montant_total = (SELECT COALESCE(SUM(sous_total), 0) FROM lignes_commande WHERE commande_id = ?)
            WHERE id = ?
        """.trimIndent()
        
        DatabasePool.getConnection().use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setInt(1, commandeId)
                stmt.setInt(2, commandeId)
                stmt.executeUpdate()
            }
        }
    }
    
    private fun recordStatusChange(commandeId: Int, oldStatus: String?, newStatus: String, notes: String? = null) {
        val sql = """
            INSERT INTO historique_commandes (commande_id, ancien_statut, nouveau_statut, commentaire)
            VALUES (?, ?, ?, ?)
        """.trimIndent()
        
        DatabasePool.getConnection().use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setInt(1, commandeId)
                stmt.setString(2, oldStatus)
                stmt.setString(3, newStatus)
                stmt.setString(4, notes)
                stmt.executeUpdate()
            }
        }
    }
    
    private fun mapResultSet(rs: ResultSet): CommandeDTO {
        return CommandeDTO(
            id = rs.getInt("id"),
            numerCommande = rs.getString("numero_commande"),
            clientId = rs.getInt("client_id"),
            statut = rs.getString("statut"),
            montantTotal = rs.getDouble("montant_total"),
            montantPaye = rs.getDouble("montant_paye"),
            modePaiement = rs.getString("mode_paiement"),
            statutPaiement = rs.getString("statut_paiement"),
            dateCreation = formatDateTime(rs.getString("date_creation")),
            dateRetraitPrevue = formatDateTime(rs.getString("date_retrait_prevue")),
            dateRetraitEffective = formatDateTime(rs.getString("date_retrait_effective")),
            notes = rs.getString("notes")
        )
    }
    
    private fun formatDateTime(dateStr: String?): String? {
        return dateStr
    }
}
