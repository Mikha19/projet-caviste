package com.cavemanager.api.data

import com.cavemanager.api.models.CategorieDTO
import java.sql.ResultSet

/**
 * DAO pour accéder aux catégories de produits
 */
object CategorieDao {
    
    fun findAll(): List<CategorieDTO> {
        val sql = """
            SELECT id, nom, description
            FROM categories
            ORDER BY nom ASC
        """.trimIndent()
        
        return DatabasePool.getConnection().use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.executeQuery().use { rs ->
                    val categories = mutableListOf<CategorieDTO>()
                    while (rs.next()) {
                        categories.add(mapResultSet(rs))
                    }
                    categories
                }
            }
        }
    }
    
    fun findById(id: Int): CategorieDTO? {
        val sql = """
            SELECT id, nom, description
            FROM categories
            WHERE id = ?
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
    
    private fun mapResultSet(rs: ResultSet): CategorieDTO {
        return CategorieDTO(
            id = rs.getInt("id"),
            nom = rs.getString("nom"),
            description = rs.getString("description")
        )
    }
}
