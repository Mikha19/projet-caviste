package com.cavemanager.api.data

import com.cavemanager.api.models.AlerteStockDTO
import java.sql.ResultSet

/**
 * DAO pour accéder aux alertes de stock
 */
object AlerteStockDao {
    
    fun findUnread(): List<AlerteStockDTO> {
        val sql = """
            SELECT a.id, a.produit_id, p.nom as produit_nom, a.quantite_actuelle,
                   a.seuil, a.lue, a.date_alerte
            FROM alertes_stock a
            JOIN produits p ON a.produit_id = p.id
            WHERE a.lue = false
            ORDER BY a.date_alerte DESC
        """.trimIndent()
        
        return DatabasePool.getConnection().use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.executeQuery().use { rs ->
                    val alertes = mutableListOf<AlerteStockDTO>()
                    while (rs.next()) {
                        alertes.add(mapResultSet(rs))
                    }
                    alertes
                }
            }
        }
    }
    
    fun findAll(): List<AlerteStockDTO> {
        val sql = """
            SELECT a.id, a.produit_id, p.nom as produit_nom, a.quantite_actuelle,
                   a.seuil, a.lue, a.date_alerte
            FROM alertes_stock a
            JOIN produits p ON a.produit_id = p.id
            ORDER BY a.date_alerte DESC
        """.trimIndent()
        
        return DatabasePool.getConnection().use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.executeQuery().use { rs ->
                    val alertes = mutableListOf<AlerteStockDTO>()
                    while (rs.next()) {
                        alertes.add(mapResultSet(rs))
                    }
                    alertes
                }
            }
        }
    }
    
    fun markAsRead(alerteId: Int) {
        val sql = "UPDATE alertes_stock SET lue = true WHERE id = ?"
        
        DatabasePool.getConnection().use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setInt(1, alerteId)
                stmt.executeUpdate()
            }
        }
    }
    
    private fun mapResultSet(rs: ResultSet): AlerteStockDTO {
        return AlerteStockDTO(
            id = rs.getInt("id"),
            produitId = rs.getInt("produit_id"),
            produitNom = rs.getString("produit_nom"),
            quantiteActuelle = rs.getInt("quantite_actuelle"),
            seuil = rs.getInt("seuil"),
            lue = rs.getBoolean("lue"),
            dateAlerte = rs.getString("date_alerte")
        )
    }
}
