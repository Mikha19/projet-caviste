package com.cavemanager.controller

import com.cavemanager.dao.ProduitDao
import com.cavemanager.model.Produit
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.stage.Stage
import java.math.BigDecimal

/**
 * Contrôleur du formulaire d'ajout / modification d'un produit.
 *
 * Ce formulaire est modal : il bloque la fenêtre principale pendant son ouverture.
 * Il reçoit un produit existant (modification) ou null (création).
 *
 * NOTE PÉDAGOGIQUE :
 *   La validation est faite côté Kotlin avant d'appeler le DAO.
 *   Le DAO ne valide pas les données métier, il ne fait que persister.
 */
class ProduitFormController {

    // =========================================================================
    // Éléments FXML
    // =========================================================================

    @FXML private lateinit var fieldNom: TextField
    @FXML private lateinit var fieldAppellation: TextField
    @FXML private lateinit var fieldMillesime: TextField
    @FXML private lateinit var fieldProducteur: TextField
    @FXML private lateinit var fieldRegion: TextField
    @FXML private lateinit var fieldPrixAchat: TextField
    @FXML private lateinit var fieldPrixVente: TextField
    @FXML private lateinit var fieldQuantite: TextField
    @FXML private lateinit var fieldSeuil: TextField
    @FXML private lateinit var areaDescription: TextArea
    @FXML private lateinit var btnSauvegarder: Button
    @FXML private lateinit var btnAnnuler: Button
    @FXML private lateinit var labelTitre: Label
    @FXML private lateinit var labelErreur: Label

    // =========================================================================
    // État interne
    // =========================================================================

    private var produitExistant: Produit? = null
    private lateinit var stage: Stage

    // =========================================================================
    // Initialisation (appelée par MainController)
    // =========================================================================

    /**
     * @param produit null = mode création, non-null = mode modification
     * @param stage   La fenêtre modale (pour la fermer après sauvegarde)
     */
    fun initialiser(produit: Produit?, stage: Stage) {
        this.produitExistant = produit
        this.stage = stage

        if (produit != null) {
            // Mode modification : pré-remplir les champs
            labelTitre.text = "Modifier le produit"
            fieldNom.text = produit.nom
            fieldAppellation.text = produit.appellation ?: ""
            fieldMillesime.text = produit.millesime?.toString() ?: ""
            fieldProducteur.text = produit.producteur ?: ""
            fieldRegion.text = produit.region ?: ""
            fieldPrixAchat.text = produit.prixAchat?.toPlainString() ?: ""
            fieldPrixVente.text = produit.prixVente?.toPlainString() ?: ""
            fieldQuantite.text = produit.quantiteStock.toString()
            fieldSeuil.text = produit.seuilAlerte.toString()
            areaDescription.text = produit.description ?: ""

            // En modification, la quantité de stock n'est pas modifiable ici
            // (uniquement via les mouvements de stock)
            fieldQuantite.isDisable = true
            fieldQuantite.tooltip = Tooltip("Modifiez le stock via les boutons Entrée / Sortie")
        } else {
            labelTitre.text = "Ajouter un produit"
            fieldQuantite.text = "0"
            fieldSeuil.text = "5"
        }

        labelErreur.isVisible = false
    }

    // =========================================================================
    // Actions
    // =========================================================================

    @FXML
    fun onSauvegarder() {
        labelErreur.isVisible = false

        // 1. Validation
        val erreurs = valider()
        if (erreurs.isNotEmpty()) {
            labelErreur.text = erreurs.joinToString("\n")
            labelErreur.isVisible = true
            return
        }

        // 2. Construction de l'objet Produit depuis les champs
        val produit = construireProduit()

        // 3. Appel au DAO
        try {
            if (produitExistant == null) {
                ProduitDao.save(produit)
            } else {
                ProduitDao.update(produit)
            }
            stage.close()

        } catch (e: Exception) {
            labelErreur.text = "Erreur : ${e.message}"
            labelErreur.isVisible = true
        }
    }

    @FXML
    fun onAnnuler() {
        stage.close()
    }

    // =========================================================================
    // Validation
    // =========================================================================

    private fun valider(): List<String> {
        val erreurs = mutableListOf<String>()

        if (fieldNom.text.isBlank()) {
            erreurs.add("• Le nom est obligatoire")
        }

        val millesimeText = fieldMillesime.text.trim()
        if (millesimeText.isNotEmpty()) {
            val annee = millesimeText.toIntOrNull()
            if (annee == null || annee < 1900 || annee > 2100) {
                erreurs.add("• Le millésime doit être une année valide (ex: 2019)")
            }
        }

        listOf(fieldPrixAchat, fieldPrixVente).forEach { field ->
            val text = field.text.trim()
            if (text.isNotEmpty()) {
                val valeur = text.replace(",", ".").toBigDecimalOrNull()
                if (valeur == null || valeur < BigDecimal.ZERO) {
                    erreurs.add("• ${field.promptText} doit être un nombre positif")
                }
            }
        }

        val seuilText = fieldSeuil.text.trim()
        if (seuilText.isEmpty() || seuilText.toIntOrNull() == null || seuilText.toInt() < 0) {
            erreurs.add("• Le seuil d'alerte doit être un entier ≥ 0")
        }

        return erreurs
    }

    // =========================================================================
    // Construction de l'objet depuis les champs
    // =========================================================================

    private fun construireProduit(): Produit {
        fun String.toDecimalOrNull() = this.trim().replace(",", ".").toBigDecimalOrNull()

        return Produit(
            id            = produitExistant?.id ?: 0,
            nom           = fieldNom.text.trim(),
            appellation   = fieldAppellation.text.blankToNull(),
            millesime     = fieldMillesime.text.trim().toIntOrNull(),
            producteur    = fieldProducteur.text.blankToNull(),
            region        = fieldRegion.text.blankToNull(),
            prixAchat     = fieldPrixAchat.text.toDecimalOrNull(),
            prixVente     = fieldPrixVente.text.toDecimalOrNull(),
            quantiteStock = fieldQuantite.text.trim().toIntOrNull() ?: 0,
            seuilAlerte   = fieldSeuil.text.trim().toIntOrNull() ?: 5,
            description   = areaDescription.text.blankToNull()
        )
    }

    private fun String.blankToNull(): String? = this.trim().ifBlank { null }
}
