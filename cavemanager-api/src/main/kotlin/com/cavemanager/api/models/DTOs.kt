package com.cavemanager.api.models

import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.time.LocalDateTime

// ============================================================
// Produits (Products)
// ============================================================

@Serializable
data class ProduitDTO(
    val id: Int = 0,
    val nom: String,
    val appellation: String? = null,
    val millesime: Int? = null,
    val producteur: String? = null,
    val region: String? = null,
    val categorieId: Int? = null,
    val categorieName: String? = null,
    val prixAchat: Double? = null,
    val prixVente: Double? = null,
    val quantiteStock: Int = 0,
    val seuilAlerte: Int = 5,
    val description: String? = null,
    val estEnAlerte: Boolean = false
)

@Serializable
data class CategorieDTO(
    val id: Int,
    val nom: String,
    val description: String? = null
)

// ============================================================
// Clients (Customers)
// ============================================================

@Serializable
data class ClientDTO(
    val id: Int = 0,
    val email: String,
    val nom: String,
    val prenom: String,
    val telephone: String? = null,
    val adresse: String? = null,
    val codePostal: String? = null,
    val ville: String? = null
)

@Serializable
data class CreateClientRequest(
    val email: String,
    val password: String,
    val nom: String,
    val prenom: String,
    val telephone: String? = null,
    val adresse: String? = null,
    val codePostal: String? = null,
    val ville: String? = null
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val token: String,
    val client: ClientDTO
)

@Serializable
data class UpdateProfileRequest(
    val nom: String? = null,
    val prenom: String? = null,
    val telephone: String? = null,
    val adresse: String? = null,
    val codePostal: String? = null,
    val ville: String? = null
)

// ============================================================
// Commandes (Orders)
// ============================================================

@Serializable
data class CommandeDTO(
    val id: Int,
    val numerCommande: String,
    val clientId: Int,
    val statut: String,  // PANIER, VALIDEE, EN_PREPARATION, PRETE, RETIREE, ANNULEE
    val montantTotal: Double,
    val montantPaye: Double = 0.0,
    val modePaiement: String = "EN_BOUTIQUE",
    val statutPaiement: String = "NON_PAYE",
    val dateCreation: String,
    val dateRetraitPrevue: String? = null,
    val dateRetraitEffective: String? = null,
    val notes: String? = null,
    val lignes: List<LigneCommandeDTO> = emptyList()
)

@Serializable
data class LigneCommandeDTO(
    val id: Int? = null,
    val produitId: Int,
    val produitNom: String? = null,
    val quantite: Int,
    val prixUnitaire: Double,
    val sousTotal: Double
)

@Serializable
data class AddToCartRequest(
    val produitId: Int,
    val quantite: Int
)

@Serializable
data class UpdateCartLineRequest(
    val ligneId: Int,
    val quantite: Int
)

@Serializable
data class ValidateOrderRequest(
    val dateRetraitPrevue: String,  // ISO format: "2024-06-15T14:30:00"
    val notes: String? = null
)

@Serializable
data class UpdateOrderStatusRequest(
    val statut: String,
    val notes: String? = null
)

// ============================================================
// Alertes Stock
// ============================================================

@Serializable
data class AlerteStockDTO(
    val id: Int,
    val produitId: Int,
    val produitNom: String? = null,
    val quantiteActuelle: Int,
    val seuil: Int,
    val lue: Boolean,
    val dateAlerte: String
)

// ============================================================
// Avis Produits (Product Reviews)
// ============================================================

@Serializable
data class AvisDTO(
    val id: Int,
    val produitId: Int,
    val clientId: Int,
    val clientNom: String? = null,
    val note: Int,  // 1-5
    val titre: String? = null,
    val texte: String? = null,
    val dateAvis: String
)

@Serializable
data class CreateAvisRequest(
    val produitId: Int,
    val note: Int,
    val titre: String? = null,
    val texte: String? = null
)

// ============================================================
// Mouvements Stock (Stock Movements)
// ============================================================

@Serializable
data class MouvementStockDTO(
    val id: Int,
    val produitId: Int,
    val produitNom: String? = null,
    val type: String,  // ENTREE, SORTIE
    val quantite: Int,
    val dateMouvement: String,
    val commentaire: String? = null
)

// ============================================================
// API Responses
// ============================================================

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null,
    val errors: List<String>? = null
)

@Serializable
data class PaginatedResponse<T>(
    val items: List<T>,
    val total: Int,
    val page: Int,
    val pageSize: Int,
    val totalPages: Int
)
