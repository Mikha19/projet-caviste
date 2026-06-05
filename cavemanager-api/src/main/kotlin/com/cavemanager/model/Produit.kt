package com.cavemanager.model

import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Représente un produit dans le stock du caviste.
 *
 * Il s'agit d'une data class Kotlin : equals(), hashCode() et toString()
 * sont générés automatiquement. La copie se fait avec .copy().
 *
 * Exemple :
 *   val produitModifie = produit.copy(prixVente = BigDecimal("25.00"))
 */
data class Produit(
    val id: Int = 0,                           // 0 = non encore enregistré en BDD
    val nom: String,
    val appellation: String? = null,
    val millesime: Int? = null,
    val producteur: String? = null,
    val region: String? = null,
    val categorieId: Int? = null,
    val prixAchat: BigDecimal? = null,
    val prixVente: BigDecimal? = null,
    val quantiteStock: Int = 0,
    val seuilAlerte: Int = 5,
    val description: String? = null,
    val createdAt: LocalDateTime? = null
) {
    /**
     * Retourne true si le stock est en dessous ou égal au seuil d'alerte.
     * Utilisé pour l'affichage visuel des alertes.
     */
    val estEnAlerte: Boolean
        get() = quantiteStock <= seuilAlerte

    /**
     * Retourne une description courte du produit pour les listes déroulantes.
     * Exemple : "Château Margaux 2019 (Bordeaux AOC)"
     */
    val labelCourt: String
        get() {
            val parts = mutableListOf(nom)
            millesime?.let { parts.add(it.toString()) }
            appellation?.let { parts.add("($it)") }
            return parts.joinToString(" ")
        }

    /**
     * Retourne true si ce produit est nouveau (pas encore en BDD).
     */
    val estNouveau: Boolean
        get() = id == 0
}
