package com.cavemanager.api.data

import com.cavemanager.api.models.ProduitDTO
import java.sql.ResultSet

/**
 * DAO pour accéder aux produits depuis la base de données
 */
object ProduitDao {
    
    fun findAll(): List<ProduitDTO> {
        val sql = """
            SELECT p.id, p.nom, p.appellation, p.millesime, p.producteur, p.region,
                   p.categorie_id, c.nom as categorie_nom, p.prix_vente,
                   p.quantite_stock, p.seuil_alerte, p.description,
                   CASE WHEN p.quantite_stock <= p.seuil_alerte THEN true ELSE false END as est_en_alerte
            FROM produits p
            LEFT JOIN categories c ON p.categorie_id = c.id
            ORDER BY p.nom ASC
        """.trimIndent()
        
        return DatabasePool.getConnection().use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.executeQuery().use { rs ->
                    val produits = mutableListOf<ProduitDTO>()
                    while (rs.next()) {
                        produits.add(mapResultSet(rs))
                    }
                    produits
                }
            }
        }
    }
    
    fun findById(id: Int): ProduitDTO? {
        val sql = """
            SELECT p.id, p.nom, p.appellation, p.millesime, p.producteur, p.region,
                   p.categorie_id, c.nom as categorie_nom, p.prix_vente,
                   p.quantite_stock, p.seuil_alerte, p.description,
                   CASE WHEN p.quantite_stock <= p.seuil_alerte THEN true ELSE false END as est_en_alerte
            FROM produits p
            LEFT JOIN categories c ON p.categorie_id = c.id
            WHERE p.id = ?
        """.trimIndent()
        
        return DatabasePool.getConnection().use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setInt(1, id)
                stmt.executeQuery().use { rs ->
                    if (rs.next()) mapResultSet(rs) else null
                }
            }
        }
    }
    
    fun rechercher(terme: String): List<ProduitDTO> {
        if (terme.isBlank()) return findAll()
        
        val sql = """
            SELECT p.id, p.nom, p.appellation, p.millesime, p.producteur, p.region,
                   p.categorie_id, c.nom as categorie_nom, p.prix_vente,
                   p.quantite_stock, p.seuil_alerte, p.description,
                   CASE WHEN p.quantite_stock <= p.seuil_alerte THEN true ELSE false END as est_en_alerte
            FROM produits p
            LEFT JOIN categories c ON p.categorie_id = c.id
            WHERE p.nom LIKE ? OR p.appellation LIKE ? OR p.producteur LIKE ?
            ORDER BY p.nom ASC
        """.trimIndent()
        
        val pattern = "%$terme%"
        
        return DatabasePool.getConnection().use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setString(1, pattern)
                stmt.setString(2, pattern)
                stmt.setString(3, pattern)
                stmt.executeQuery().use { rs ->
                    val produits = mutableListOf<ProduitDTO>()
                    while (rs.next()) {
                        produits.add(mapResultSet(rs))
                    }
                    produits
                }
            }
        }
    }
    
    fun findByCategorie(categorieId: Int): List<ProduitDTO> {
        val sql = """
            SELECT p.id, p.nom, p.appellation, p.millesime, p.producteur, p.region,
                   p.categorie_id, c.nom as categorie_nom, p.prix_vente,
                   p.quantite_stock, p.seuil_alerte, p.description,
                   CASE WHEN p.quantite_stock <= p.seuil_alerte THEN true ELSE false END as est_en_alerte
            FROM produits p
            LEFT JOIN categories c ON p.categorie_id = c.id
            WHERE p.categorie_id = ?
            ORDER BY p.nom ASC
        """.trimIndent()
        
        return DatabasePool.getConnection().use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setInt(1, categorieId)
                stmt.executeQuery().use { rs ->
                    val produits = mutableListOf<ProduitDTO>()
                    while (rs.next()) {
                        produits.add(mapResultSet(rs))
                    }
                    produits
                }
            }
        }
    }
    
    fun findLowStock(): List<ProduitDTO> {
        val sql = """
            SELECT p.id, p.nom, p.appellation, p.millesime, p.producteur, p.region,
                   p.categorie_id, c.nom as categorie_nom, p.prix_vente,
                   p.quantite_stock, p.seuil_alerte, p.description,
                   CASE WHEN p.quantite_stock <= p.seuil_alerte THEN true ELSE false END as est_en_alerte
            FROM produits p
            LEFT JOIN categories c ON p.categorie_id = c.id
            WHERE p.quantite_stock <= p.seuil_alerte
            ORDER BY p.quantite_stock ASC
        """.trimIndent()
        
        return DatabasePool.getConnection().use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.executeQuery().use { rs ->
                    val produits = mutableListOf<ProduitDTO>()
                    while (rs.next()) {
                        produits.add(mapResultSet(rs))
                    }
                    produits
                }
            }
        }
    }
    
    private fun mapResultSet(rs: ResultSet): ProduitDTO {
        return ProduitDTO(
            id = rs.getInt("id"),
            nom = rs.getString("nom"),
            appellation = rs.getString("appellation"),
            millesime = rs.getInt("millesime").takeIf { it != 0 },
            producteur = rs.getString("producteur"),
            region = rs.getString("region"),
            categorieId = rs.getInt("categorie_id").takeIf { it != 0 },
            categorieName = rs.getString("categorie_nom"),
            prixVente = rs.getDouble("prix_vente"),
            quantiteStock = rs.getInt("quantite_stock"),
            seuilAlerte = rs.getInt("seuil_alerte"),
            description = rs.getString("description"),
            estEnAlerte = rs.getBoolean("est_en_alerte")
        )
    }
}
