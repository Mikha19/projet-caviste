package com.cavemanager.api.data

import com.cavemanager.api.models.ClientDTO
import at.favre.lib.bcrypt.BCrypt
import java.sql.ResultSet
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * DAO pour gérer les clients (comptes utilisateurs)
 */
object ClientDao {
    
    fun create(email: String, password: String, nom: String, prenom: String,
               telephone: String? = null, adresse: String? = null,
               codePostal: String? = null, ville: String? = null): ClientDTO? {
        
        // Vérifier que l'email n'existe pas déjà
        if (findByEmail(email) != null) {
            return null
        }
        
        val hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray())
        
        val sql = """
            INSERT INTO clients (email, mot_de_passe, nom, prenom, telephone, adresse, code_postal, ville)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()
        
        return DatabasePool.getConnection().use { conn ->
            conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS).use { stmt ->
                stmt.setString(1, email)
                stmt.setString(2, hashedPassword)
                stmt.setString(3, nom)
                stmt.setString(4, prenom)
                stmt.setString(5, telephone)
                stmt.setString(6, adresse)
                stmt.setString(7, codePostal)
                stmt.setString(8, ville)
                
                if (stmt.executeUpdate() > 0) {
                    stmt.generatedKeys.use { rs ->
                        if (rs.next()) {
                            val id = rs.getInt(1)
                            ClientDTO(
                                id = id,
                                email = email,
                                nom = nom,
                                prenom = prenom,
                                telephone = telephone,
                                adresse = adresse,
                                codePostal = codePostal,
                                ville = ville
                            )
                        } else null
                    }
                } else null
            }
        }
    }
    
    fun findByEmail(email: String): ClientDTO? {
        val sql = """
            SELECT id, email, nom, prenom, telephone, adresse, code_postal, ville
            FROM clients WHERE email = ? AND actif = true
        """.trimIndent()
        
        return DatabasePool.getConnection().use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setString(1, email)
                stmt.executeQuery().use { rs ->
                    if (rs.next()) mapResultSet(rs) else null
                }
            }
        }
    }
    
    fun findById(id: Int): ClientDTO? {
        val sql = """
            SELECT id, email, nom, prenom, telephone, adresse, code_postal, ville
            FROM clients WHERE id = ? AND actif = true
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
    
    fun validatePassword(email: String, password: String): Boolean {
        val sql = "SELECT mot_de_passe FROM clients WHERE email = ? AND actif = true"
        
        return DatabasePool.getConnection().use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setString(1, email)
                stmt.executeQuery().use { rs ->
                    if (rs.next()) {
                        val hashedPassword = rs.getString(1)
                        BCrypt.verifyer().verify(password.toCharArray(), hashedPassword.toByteArray()).verified
                    } else false
                }
            }
        }
    }
    
    fun update(id: Int, nom: String? = null, prenom: String? = null,
               telephone: String? = null, adresse: String? = null,
               codePostal: String? = null, ville: String? = null): Boolean {
        
        val updates = mutableListOf<String>()
        val params = mutableListOf<Any?>()
        
        nom?.let { updates.add("nom = ?"); params.add(it) }
        prenom?.let { updates.add("prenom = ?"); params.add(it) }
        telephone?.let { updates.add("telephone = ?"); params.add(it) }
        adresse?.let { updates.add("adresse = ?"); params.add(it) }
        codePostal?.let { updates.add("code_postal = ?"); params.add(it) }
        ville?.let { updates.add("ville = ?"); params.add(it) }
        
        if (updates.isEmpty()) return true
        
        val sql = "UPDATE clients SET ${updates.joinToString(", ")} WHERE id = ?"
        params.add(id)
        
        return DatabasePool.getConnection().use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                params.forEachIndexed { index, value ->
                    when (value) {
                        is String -> stmt.setString(index + 1, value)
                        is Int -> stmt.setInt(index + 1, value)
                        else -> stmt.setObject(index + 1, value)
                    }
                }
                stmt.executeUpdate() > 0
            }
        }
    }
    
    fun updateLastConnection(clientId: Int) {
        val sql = "UPDATE clients SET derniere_connexion = NOW() WHERE id = ?"
        
        DatabasePool.getConnection().use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setInt(1, clientId)
                stmt.executeUpdate()
            }
        }
    }
    
    private fun mapResultSet(rs: ResultSet): ClientDTO {
        return ClientDTO(
            id = rs.getInt("id"),
            email = rs.getString("email"),
            nom = rs.getString("nom"),
            prenom = rs.getString("prenom"),
            telephone = rs.getString("telephone"),
            adresse = rs.getString("adresse"),
            codePostal = rs.getString("code_postal"),
            ville = rs.getString("ville")
        )
    }
}
