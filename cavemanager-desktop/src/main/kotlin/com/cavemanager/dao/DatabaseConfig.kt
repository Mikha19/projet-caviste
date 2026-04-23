package com.cavemanager.dao

import java.sql.Connection
import java.sql.DriverManager
import java.util.Properties

/**
 * Gestion de la connexion à la base de données MySQL.
 *
 * Cette classe utilise le pattern Singleton (object Kotlin) pour partager
 * une seule instance de configuration dans toute l'application.
 *
 * USAGE :
 *   DatabaseConfig.getConnection().use { conn ->
 *       // Utiliser conn ici
 *       // La connexion est fermée automatiquement à la fin du bloc
 *   }
 *
 * NOTE PÉDAGOGIQUE :
 *   En production, on utiliserait un pool de connexions (HikariCP).
 *   Pour ce projet pédagogique, chaque appel ouvre et ferme une connexion.
 *   Le pool de connexions peut être un TP avancé.
 */
object DatabaseConfig {

    private lateinit var url: String
    private lateinit var user: String
    private lateinit var password: String
    private var initialized = false

    /**
     * Initialise la configuration depuis le fichier database.properties.
     * Doit être appelé une seule fois au démarrage de l'application.
     */
    fun initialize() {
        if (initialized) return

        val props = Properties()
        val stream = DatabaseConfig::class.java
            .classLoader
            .getResourceAsStream("database.properties")
            ?: throw IllegalStateException(
                "Fichier database.properties introuvable dans resources/. " +
                "Vérifiez votre configuration de build."
            )

        props.load(stream)

        val host     = props.getProperty("db.host", "localhost")
        val port     = props.getProperty("db.port", "3306")
        val name     = props.getProperty("db.name")     ?: error("db.name manquant dans database.properties")
        user         = props.getProperty("db.user")     ?: error("db.user manquant dans database.properties")
        password     = props.getProperty("db.password") ?: error("db.password manquant dans database.properties")

        // useSSL=false est acceptable en développement local
        // serverTimezone évite les problèmes de fuseau horaire avec MySQL 8
        url = "jdbc:mysql://$host:$port/$name?useSSL=false&serverTimezone=Europe/Paris&allowPublicKeyRetrieval=true"

        initialized = true
        println("✅ DatabaseConfig initialisé : $host:$port/$name")
    }

    /**
     * Ouvre et retourne une nouvelle connexion à la base de données.
     *
     * Toujours utiliser dans un bloc .use { } ou try-finally pour garantir
     * la fermeture de la connexion.
     *
     * @throws IllegalStateException si DatabaseConfig.initialize() n'a pas été appelé
     * @throws java.sql.SQLException si la connexion échoue
     */
    fun getConnection(): Connection {
        check(initialized) {
            "DatabaseConfig.initialize() doit être appelé avant getConnection()"
        }
        return DriverManager.getConnection(url, user, password)
    }

    /**
     * Teste la connexion à la base de données.
     * Utile au démarrage pour afficher un message d'erreur clair.
     *
     * @return true si la connexion réussit, false sinon
     */
    fun testerConnexion(): Boolean {
        return try {
            getConnection().use { conn ->
                conn.isValid(5) // timeout de 5 secondes
            }
        } catch (e: Exception) {
            System.err.println("❌ Connexion BDD échouée : ${e.message}")
            false
        }
    }
}
