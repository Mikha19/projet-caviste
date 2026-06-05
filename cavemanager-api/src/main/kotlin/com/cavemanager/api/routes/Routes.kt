package com.cavemanager.api.routes

import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.cavemanager.api.data.ClientDao
import com.cavemanager.api.models.*
import java.util.Date

fun Route.installAuthRoutes(
    jwtSecret: String,
    jwtIssuer: String,
    jwtAudience: String,
    jwtAlgorithm: Algorithm
) {
    route("/auth") {
        // Register a new client
        post("/register") {
            try {
                val request = call.receive<CreateClientRequest>()
                
                // Validation
                if (request.email.isBlank() || request.password.isBlank()) {
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse<Unit>(false, "Email and password are required")
                    )
                }
                
                val client = ClientDao.create(
                    email = request.email,
                    password = request.password,
                    nom = request.nom,
                    prenom = request.prenom,
                    telephone = request.telephone,
                    adresse = request.adresse,
                    codePostal = request.codePostal,
                    ville = request.ville
                )
                
                if (client == null) {
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse<Unit>(false, "Email already exists")
                    )
                }
                
                // Generate JWT
                val token = generateToken(client.id, jwtSecret, jwtIssuer, jwtAudience)
                
                call.respond(
                    HttpStatusCode.Created,
                    ApiResponse(true, "Registration successful", AuthResponse(token, client))
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Unit>(false, "Invalid request: ${e.message}")
                )
            }
        }
        
        // Login
        post("/login") {
            try {
                val request = call.receive<LoginRequest>()
                
                // Validate credentials
                if (!ClientDao.validatePassword(request.email, request.password)) {
                    return@post call.respond(
                        HttpStatusCode.Unauthorized,
                        ApiResponse<Unit>(false, "Invalid email or password")
                    )
                }
                
                val client = ClientDao.findByEmail(request.email)
                    ?: return@post call.respond(
                        HttpStatusCode.Unauthorized,
                        ApiResponse<Unit>(false, "User not found")
                    )
                
                // Update last connection
                ClientDao.updateLastConnection(client.id)
                
                // Generate JWT
                val token = generateToken(client.id, jwtSecret, jwtIssuer, jwtAudience)
                
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse(true, "Login successful", AuthResponse(token, client))
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Unit>(false, "Invalid request: ${e.message}")
                )
            }
        }
    }
}

fun Route.installProduitRoutes() {
    route("/produits") {
        // Get all products
        get {
            try {
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20
                val search = call.request.queryParameters["search"] ?: ""
                val categorieId = call.request.queryParameters["categorieId"]?.toIntOrNull()
                
                val allProduits = when {
                    search.isNotBlank() -> ProduitDao.rechercher(search)
                    categorieId != null -> ProduitDao.findByCategorie(categorieId)
                    else -> ProduitDao.findAll()
                }
                
                // Simple pagination
                val startIdx = (page - 1) * limit
                val endIdx = minOf(startIdx + limit, allProduits.size)
                val paginated = if (startIdx < allProduits.size) {
                    allProduits.subList(startIdx, endIdx)
                } else {
                    emptyList()
                }
                
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse(
                        true,
                        data = PaginatedResponse(
                            items = paginated,
                            total = allProduits.size,
                            page = page,
                            pageSize = limit,
                            totalPages = (allProduits.size + limit - 1) / limit
                        )
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse<Unit>(false, "Error fetching products: ${e.message}")
                )
            }
        }
        
        // Get product by ID
        get("/{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest)
                
                val produit = ProduitDao.findById(id)
                    ?: return@get call.respond(
                        HttpStatusCode.NotFound,
                        ApiResponse<Unit>(false, "Product not found")
                    )
                
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse(true, data = produit)
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse<Unit>(false, "Error fetching product: ${e.message}")
                )
            }
        }
        
        // Get low stock alerts
        get("/alerts/stock") {
            try {
                val alerts = AlerteStockDao.findUnread()
                
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse(true, data = alerts)
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse<Unit>(false, "Error fetching alerts: ${e.message}")
                )
            }
        }
    }
}

fun Route.installCommandeRoutes() {
    route("/commandes") {
        // Get client's orders
        get {
            try {
                val principal = call.authentication.principal<JWTPrincipal>()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized)
                val clientId = principal.payload.getClaim("clientId").asInt()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized)
                
                val commandes = CommandeDao.findByClientId(clientId)
                
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse(true, data = commandes)
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse<Unit>(false, "Error fetching orders: ${e.message}")
                )
            }
        }
        
        // Get current cart
        get("/panier") {
            try {
                val principal = call.authentication.principal<JWTPrincipal>()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized)
                val clientId = principal.payload.getClaim("clientId").asInt()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized)
                
                val panier = CommandeDao.getPanierActif(clientId)
                    ?: run {
                        // Create new cart if none exists
                        CommandeDao.createPanier(clientId)
                    }
                
                if (panier != null) {
                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse(true, data = panier)
                    )
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ApiResponse<Unit>(false, "No active cart")
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse<Unit>(false, "Error fetching cart: ${e.message}")
                )
            }
        }
        
        // Add to cart
        post("/panier/add") {
            try {
                val principal = call.authentication.principal<JWTPrincipal>()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized)
                val clientId = principal.payload.getClaim("clientId").asInt()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized)
                
                val request = call.receive<AddToCartRequest>()
                
                // Get product to verify it exists and get price
                val produit = ProduitDao.findById(request.produitId)
                    ?: return@post call.respond(
                        HttpStatusCode.NotFound,
                        ApiResponse<Unit>(false, "Product not found")
                    )
                
                // Get or create cart
                var panier = CommandeDao.getPanierActif(clientId)
                if (panier == null) {
                    panier = CommandeDao.createPanier(clientId)
                }
                
                if (panier != null) {
                    CommandeDao.addToCart(panier.id, request.produitId, request.quantite, produit.prixVente ?: 0.0)
                    call.respond(
                        HttpStatusCode.Created,
                        ApiResponse<Unit>(true, "Item added to cart")
                    )
                } else {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ApiResponse<Unit>(false, "Failed to add to cart")
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Unit>(false, "Error adding to cart: ${e.message}")
                )
            }
        }
        
        // Update cart line
        put("/panier/ligne/{ligneId}") {
            try {
                val principal = call.authentication.principal<JWTPrincipal>()
                    ?: return@put call.respond(HttpStatusCode.Unauthorized)
                
                val ligneId = call.parameters["ligneId"]?.toIntOrNull()
                    ?: return@put call.respond(HttpStatusCode.BadRequest)
                val request = call.receive<UpdateCartLineRequest>()
                
                val updated = CommandeDao.updateCartLine(ligneId, request.quantite)
                
                if (updated) {
                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse<Unit>(true, "Cart updated")
                    )
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ApiResponse<Unit>(false, "Line not found")
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Unit>(false, "Error updating cart: ${e.message}")
                )
            }
        }
        
        // Remove from cart
        delete("/panier/ligne/{ligneId}") {
            try {
                val principal = call.authentication.principal<JWTPrincipal>()
                    ?: return@delete call.respond(HttpStatusCode.Unauthorized)
                
                val ligneId = call.parameters["ligneId"]?.toIntOrNull()
                    ?: return@delete call.respond(HttpStatusCode.BadRequest)
                
                val removed = CommandeDao.removeFromCart(ligneId)
                
                if (removed) {
                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse<Unit>(true, "Item removed from cart")
                    )
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ApiResponse<Unit>(false, "Line not found")
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Unit>(false, "Error removing from cart: ${e.message}")
                )
            }
        }
        
        // Validate order (checkout)
        post("/panier/valider") {
            try {
                val principal = call.authentication.principal<JWTPrincipal>()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized)
                val clientId = principal.payload.getClaim("clientId").asInt()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized)
                
                val request = call.receive<ValidateOrderRequest>()
                
                val panier = CommandeDao.getPanierActif(clientId)
                    ?: return@post call.respond(
                        HttpStatusCode.NotFound,
                        ApiResponse<Unit>(false, "No active cart")
                    )
                
                if (panier.lignes.isEmpty()) {
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse<Unit>(false, "Cart is empty")
                    )
                }
                
                val validated = CommandeDao.validateOrder(
                    panier.id,
                    request.dateRetraitPrevue,
                    request.notes
                )
                
                if (validated) {
                    val updatedOrder = CommandeDao.findById(panier.id)
                    call.respond(
                        HttpStatusCode.Created,
                        ApiResponse(true, "Order validated", updatedOrder)
                    )
                } else {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ApiResponse<Unit>(false, "Failed to validate order")
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Unit>(false, "Error validating order: ${e.message}")
                )
            }
        }
        
        // Get order by ID
        get("/{id}") {
            try {
                val principal = call.authentication.principal<JWTPrincipal>()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized)
                
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest)
                
                val commande = CommandeDao.findById(id)
                    ?: return@get call.respond(
                        HttpStatusCode.NotFound,
                        ApiResponse<Unit>(false, "Order not found")
                    )
                
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse(true, data = commande)
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse<Unit>(false, "Error fetching order: ${e.message}")
                )
            }
        }
    }
}

fun Route.installClientRoutes() {
    route("/profile") {
        val principal = call.authentication.principal<JWTPrincipal>()
            ?: return@route
        val clientId = principal.payload.getClaim("clientId").asInt()
            ?: return@route
        
        // Get profile
        get {
            try {
                val client = ClientDao.findById(clientId)
                    ?: return@get call.respond(HttpStatusCode.NotFound)
                
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse(true, data = client)
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse<Unit>(false, "Error fetching profile: ${e.message}")
                )
            }
        }
        
        // Update profile
        put {
            try {
                val request = call.receive<UpdateProfileRequest>()
                
                val updated = ClientDao.update(
                    clientId,
                    nom = request.nom,
                    prenom = request.prenom,
                    telephone = request.telephone,
                    adresse = request.adresse,
                    codePostal = request.codePostal,
                    ville = request.ville
                )
                
                if (updated) {
                    val client = ClientDao.findById(clientId)
                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse(true, "Profile updated", client)
                    )
                } else {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse<Unit>(false, "Failed to update profile")
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Unit>(false, "Error updating profile: ${e.message}")
                )
            }
        }
    }
}

private fun generateToken(
    clientId: Int,
    secret: String,
    issuer: String,
    audience: String
): String {
    return JWT.create()
        .withIssuer(issuer)
        .withAudience(audience)
        .withClaim("clientId", clientId)
        .withExpiresAt(Date(System.currentTimeMillis() + 86400000)) // 24 hours
        .sign(Algorithm.HMAC256(secret))
}

private fun JWTPrincipal.getClientId(): Int? {
    return payload.getClaim("clientId").asInt()
}
