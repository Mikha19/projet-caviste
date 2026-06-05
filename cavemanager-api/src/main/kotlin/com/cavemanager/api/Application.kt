package com.cavemanager.api

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.http.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.cavemanager.api.routes.installAuthRoutes
import com.cavemanager.api.routes.installProduitRoutes
import com.cavemanager.api.routes.installCommandeRoutes
import com.cavemanager.api.data.DatabasePool
import kotlinx.serialization.json.Json
import org.slf4j.event.Level
import java.util.Date

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    // Initialize database pool
    DatabasePool.initialize()
    
    // Configure JSON serialization
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
    
    // Configure CORS
    install(CORS) {
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Patch)
        
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowHeader("X-API-Key")
        
        allowCredentials = true
        maxAge = Duration.ofDays(1)
        
        // Allow from frontend URLs
        allowHost("localhost:3000")
        allowHost("localhost:3001")
        allowHost("127.0.0.1:3000")
        allowHost("127.0.0.1:3001")
        allowHost("*") // For development only - restrict in production
    }
    
    // Configure logging
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }
    
    // Configure JWT Authentication
    val jwtSecret = System.getenv("JWT_SECRET") ?: "your-secret-key-change-in-production"
    val jwtIssuer = "cavemanager"
    val jwtAudience = "cavemanager-clients"
    val jwtAlgorithm = Algorithm.HMAC256(jwtSecret)
    
    install(Authentication) {
        jwt("auth-jwt") {
            realm = "cavemanager"
            schemeWithName("Bearer")
            verifier(
                JWT
                    .require(jwtAlgorithm)
                    .withIssuer(jwtIssuer)
                    .withAudience(jwtAudience)
                    .build()
            )
            validate { credential ->
                if (credential.payload.audience.contains(jwtAudience)) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }
    
    // Setup routes
    routing {
        // Health check
        get("/health") {
            call.respond(mapOf("status" to "ok"))
        }
        
        // API v1
        route("/api/v1") {
            // Authentication routes (public)
            installAuthRoutes(jwtSecret, jwtIssuer, jwtAudience, jwtAlgorithm)
            
            // Protected routes
            authenticate("auth-jwt") {
                // Products routes (read-only for customers, admin for caviste)
                installProduitRoutes()
                
                // Orders routes
                installCommandeRoutes()
                
                // Profile routes
                installClientRoutes()
            }
        }
    }
}

// Helper function to extract client ID from JWT
fun JWTPrincipal.getClientId(): Int? {
    return payload.getClaim("clientId").asInt()
}
