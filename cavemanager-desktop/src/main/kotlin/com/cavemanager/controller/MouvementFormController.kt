package com.cavemanager.controller

import com.cavemanager.dao.MouvementStockDao
import com.cavemanager.model.MouvementStock
import com.cavemanager.model.TypeMouvement
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.stage.Stage

/**
 * Contrôleur du formulaire de mouvement de stock (entrée ou sortie).
 */
class MouvementFormController {

    @FXML private lateinit var labelTitre: Label
    @FXML private lateinit var labelProduit: Label
    @FXML private lateinit var labelStockActuel: Label
    @FXML private lateinit var fieldQuantite: TextField
    @FXML private lateinit var fieldCommentaire: TextField
    @FXML private lateinit var btnConfirmer: Button
    @FXML private lateinit var labelErreur: Label

    private var produitId: Int = 0
    private var estEntree: Boolean = true
    private lateinit var stage: Stage

    fun initialiser(produit: com.cavemanager.model.Produit, estEntree: Boolean, stage: Stage) {
        this.produitId = produit.id
        this.estEntree = estEntree
        this.stage = stage

        labelTitre.text = if (estEntree) "📦 Entrée de stock" else "🛒 Sortie de stock"
        labelProduit.text = produit.labelCourt
        labelStockActuel.text = "Stock actuel : ${produit.quantiteStock} bouteille(s)"

        if (estEntree) {
            btnConfirmer.styleClass.add("btn-success")
        } else {
            btnConfirmer.styleClass.add("btn-danger")
            // Affichage de l'alerte si stock bas
            if (produit.estEnAlerte) {
                labelStockActuel.style = "-fx-text-fill: red; -fx-font-weight: bold;"
            }
        }
        labelErreur.isVisible = false
    }

    @FXML
    fun onConfirmer() {
        val quantite = fieldQuantite.text.trim().toIntOrNull()
        if (quantite == null || quantite <= 0) {
            labelErreur.text = "La quantité doit être un entier strictement positif"
            labelErreur.isVisible = true
            return
        }

        try {
            val mouvement = MouvementStock(
                produitId   = produitId,
                type        = if (estEntree) TypeMouvement.ENTREE else TypeMouvement.SORTIE,
                quantite    = quantite,
                commentaire = fieldCommentaire.text.trim().ifBlank { null }
            )

            if (estEntree) MouvementStockDao.entreeStock(mouvement)
            else MouvementStockDao.sortieStock(mouvement)

            stage.close()

        } catch (e: IllegalStateException) {
            // Stock insuffisant
            labelErreur.text = e.message
            labelErreur.isVisible = true
        } catch (e: Exception) {
            labelErreur.text = "Erreur : ${e.message}"
            labelErreur.isVisible = true
        }
    }

    @FXML
    fun onAnnuler() {
        stage.close()
    }
}
