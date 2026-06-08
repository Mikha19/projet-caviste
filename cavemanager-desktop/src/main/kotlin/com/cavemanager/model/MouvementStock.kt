package com.cavemanager.model

import java.time.LocalDateTime

/**
 * Type de mouvement de stock.
 * ENTREE : réception d'un fournisseur (augmente le stock)
 * SORTIE : vente ou casse (diminue le stock)
 */
enum class TypeMouvement {
    ENTREE, SORTIE;

    fun label(): String = when (this) {
        ENTREE -> "Entrée"
        SORTIE -> "Sortie"
    }
}

/**
 * Représente un mouvement de stock (entrée ou sortie).
 * Chaque mouvement est immuable et historisé en base de données.
 */
data class MouvementStock(
    val id: Int = 0,
    val produitId: Int,
    val type: TypeMouvement,
    val quantite: Int,
    val dateMouvement: LocalDateTime = LocalDateTime.now(),
    val commentaire: String? = null,
    // Optionnel : nom du produit pour l'affichage (JOIN SQL)
    val nomProduit: String? = null
) {
    init {
        require(quantite > 0) { "La quantité d'un mouvement doit être strictement positive (reçu: $quantite)" }
    }
}
