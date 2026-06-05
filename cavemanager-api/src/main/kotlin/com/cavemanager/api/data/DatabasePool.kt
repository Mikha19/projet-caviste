package com.cavemanager.api.data

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection
import java.util.Properties

/**
 * Gestion du pool de connexions à la base de données MySQL.
 * Utilise HikariCP pour la performance et la stabilité.
 */
object DatabasePool {
    
    private var dataSource: HikariDataSource? = null
    private var initialized = false

    fun initialize() {
        if (initialized) return
        
        val props = Properties()
        val stream = DatabasePool::class.java.classLoader
            .getResourceAsStream("database.properties")
            ?: throw IllegalStateException("database.properties not found in resources")
        
        props.load(stream)
        
        val host = props.getProperty("db.host", "localhost")
        val port = props.getProperty("db.port", "3306")
        val database = props.getProperty("db.name", "cavemanager")
        val user = props.getProperty("db.user", "cavemanager_user")
        val password = props.getProperty("db.password", "cavemanagerTest123!")
        
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = "jdbc:mysql://$host:$port/$database?useSSL=false&serverTimezone=UTC"
            username = user
            this.password = password
            maximumPoolSize = 10
            minimumIdle = 2
            connectionTimeout = 30000
            idleTimeout = 600000
            maxLifetime = 1800000
            autoCommit = true
        }
        
        dataSource = HikariDataSource(hikariConfig)
        initialized = true
    }
    
    fun getConnection(): Connection {
        if (!initialized) initialize()
        return dataSource?.connection ?: throw IllegalStateException("Database pool not initialized")
    }
    
    fun close() {
        dataSource?.close()
    }
}
