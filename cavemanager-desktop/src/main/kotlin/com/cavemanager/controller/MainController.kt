package com.cavemanager.controller

import com.cavemanager.dao.ProduitDao
import com.cavemanager.model.Produit
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.collections.transformation.FilteredList
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.paint.Color
import javafx.stage.Modality
import javafx.stage.Stage
import java.net.URL
import java.util.ResourceBundle

/**
 * Contrôleur de la fenêtre principale.
 *
 * Responsabilités :
 *   - Afficher la liste des produits
 *   - Gérer la barre de recherche
 *   - Ouvrir les fenêtres d'ajout / modification / mouvements
 *   - Déclencher la suppression avec confirmation
 *
 * NOTE PÉDAGOGIQUE :
 *   Les annotations @FXML lient ce code aux éléments définis dans main.fxml.
 *   Le nom de la variable doit correspondre exactement au fx:id dans le FXML.
 *
 *   Initializable.initialize() est appelé automatiquement par FXMLLoader
 *   après que tous les @FXML ont été injectés.
 */
class MainController : Initializable {

    // =========================================================================
    // Éléments FXML (liés par fx:id dans main.fxml)
    // =========================================================================

    @FXML private lateinit var tableViewProduits: TableView<Produit>
    @FXML private lateinit var colNom: TableColumn<Produit, String>
    @FXML private lateinit var colAppellation: TableColumn<Produit, String>
    @FXML private lateinit var colMillesime: TableColumn<Produit, Int>
    @FXML private lateinit var colRegion: TableColumn<Produit, String>
    @FXML private lateinit var colStock: TableColumn<Produit, Int>
    @FXML private lateinit var colPrixVente: TableColumn<Produit, String>

    @FXML private lateinit var textFieldRecherche: TextField
    @FXML private lateinit var labelNbProduits: Label
    @FXML private lateinit var labelAlertes: Label

    @FXML private lateinit var btnAjouter: Button
    @FXML private lateinit var btnModifier: Button
    @FXML private lateinit var btnSupprimer: Button
    @FXML private lateinit var btnEntree: Button
    @FXML private lateinit var btnSortie: Button

    // =========================================================================
    // État interne du contrôleur
    // =========================================================================

    // ObservableList : JavaFX écoute les changements et rafraîchit la TableView automatiquement
    private val produits = FXCollections.observableArrayList<Produit>()
    private lateinit var produitsFiltres: FilteredList<Produit>

    // =========================================================================
    // Initialisation
    // =========================================================================

    override fun initialize(url: URL?, rb: ResourceBundle?) {
        configurerColonnes()
        configurerRecherche()
        configurerSelectionBoutons()
        chargerProduits()
    }

    private fun configurerColonnes() {
        // PropertyValueFactory utilise le nom de la propriété Kotlin (ou getter Java)
        colNom.cellValueFactory = PropertyValueFactory("nom")
        colAppellation.cellValueFactory = PropertyValueFactory("appellation")
        colMillesime.cellValueFactory = PropertyValueFactory("millesime")
        colRegion.cellValueFactory = PropertyValueFactory("region")
        colStock.cellValueFactory = PropertyValueFactory("quantiteStock")
        colPrixVente.cellValueFactory = PropertyValueFactory("prixVente")

        // Mise en évidence visuelle des produits en alerte (fond rouge clair)
        tableViewProduits.setRowFactory {
            object : TableRow<Produit>() {
                override fun updateItem(produit: Produit?, empty: Boolean) {
                    super.updateItem(produit, empty)
                    if (produit != null && produit.estEnAlerte) {
                        style = "-fx-background-color: #FFEBEE;"
                    } else {
                        style = ""
                    }
                }
            }
        }

        // Double-clic sur une ligne → ouvre le formulaire de modification
        tableViewProduits.setOnMouseClicked { event ->
            if (event.clickCount == 2 && tableViewProduits.selectionModel.selectedItem != null) {
                ouvrirFormulaireModification()
            }
        }
    }

    private fun configurerRecherche() {
        produitsFiltres = FilteredList(produits) { true }
        tableViewProduits.items = produitsFiltres

        // Le filtre se met à jour à chaque frappe dans le champ de recherche
        textFieldRecherche.textProperty().addListener { _, _, nouveauTerme ->
            produitsFiltres.setPredicate { produit ->
                if (nouveauTerme.isBlank()) return@setPredicate true
                val terme = nouveauTerme.lowercase()
                produit.nom.lowercase().contains(terme)
                    || (produit.appellation?.lowercase()?.contains(terme) == true)
                    || (produit.region?.lowercase()?.contains(terme) == true)
            }
            mettreAJourCompteurs()
        }
    }

    private fun configurerSelectionBoutons() {
        // Les boutons Modifier/Supprimer/Entrée/Sortie ne sont actifs que si une ligne est sélectionnée
        val aucuneSelection = tableViewProduits.selectionModel.selectedItemProperty().isNull
        btnModifier.disableProperty().bind(aucuneSelection)
        btnSupprimer.disableProperty().bind(aucuneSelection)
        btnEntree.disableProperty().bind(aucuneSelection)
        btnSortie.disableProperty().bind(aucuneSelection)
    }

    // =========================================================================
    // Chargement des données
    // =========================================================================

    /**
     * Charge tous les produits depuis la BDD et rafraîchit l'affichage.
     * Cette méthode est aussi appelée après chaque modification.
     */
    private fun chargerProduits() {
        try {
            val liste = ProduitDao.findAll()
            produits.setAll(liste)
            mettreAJourCompteurs()
        } catch (e: Exception) {
            afficherErreur("Erreur de chargement", "Impossible de charger les produits : ${e.message}")
        }
    }

    private fun mettreAJourCompteurs() {
        val total = produitsFiltres.size
        val enAlerte = produitsFiltres.count { it.estEnAlerte }

        labelNbProduits.text = "$total produit(s)"
        if (enAlerte > 0) {
            labelAlertes.text = "⚠️ $enAlerte en alerte"
            labelAlertes.textFill = Color.RED
        } else {
            labelAlertes.text = "✅ Stock OK"
            labelAlertes.textFill = Color.GREEN
        }
    }

    // =========================================================================
    // Actions boutons (@FXML = appelé depuis le FXML)
    // =========================================================================

    @FXML
    fun onAjouter() {
        ouvrirFormulaire(null)
    }

    @FXML
    fun onModifier() {
        ouvrirFormulaireModification()
    }

    @FXML
    fun onSupprimer() {
        val produit = tableViewProduits.selectionModel.selectedItem ?: return

        val confirmation = Alert(Alert.AlertType.CONFIRMATION).apply {
            title = "Confirmer la suppression"
            headerText = "Supprimer « ${produit.nom} » ?"
            contentText = "Cette action est irréversible.\n" +
                "Si ce produit a des mouvements de stock, la suppression sera refusée."
        }

        if (confirmation.showAndWait().orElse(null) == ButtonType.OK) {
            try {
                val supprime = ProduitDao.delete(produit.id)
                if (supprime) {
                    chargerProduits()
                    afficherInfo("Suppression réussie", "« ${produit.nom} » a été supprimé.")
                }
            } catch (e: java.sql.SQLIntegrityConstraintViolationException) {
                afficherErreur(
                    "Suppression impossible",
                    "Ce produit a des mouvements de stock associés.\n" +
                    "Archivez-le plutôt que de le supprimer."
                )
            } catch (e: Exception) {
                afficherErreur("Erreur", e.message ?: "Erreur inconnue")
            }
        }
    }

    @FXML
    fun onEntreeStock() {
        val produit = tableViewProduits.selectionModel.selectedItem ?: return
        ouvrirMouvement(produit, estEntree = true)
    }

    @FXML
    fun onSortieStock() {
        val produit = tableViewProduits.selectionModel.selectedItem ?: return
        ouvrirMouvement(produit, estEntree = false)
    }

    @FXML
    fun onEffacerRecherche() {
        textFieldRecherche.clear()
    }

    // =========================================================================
    // Ouverture des fenêtres modales
    // =========================================================================

    private fun ouvrirFormulaireModification() {
        val produit = tableViewProduits.selectionModel.selectedItem ?: return
        ouvrirFormulaire(produit)
    }

    /**
     * Ouvre le formulaire d'ajout (produit=null) ou de modification (produit != null).
     */
    private fun ouvrirFormulaire(produit: Produit?) {
        try {
            val loader = FXMLLoader(javaClass.getResource("/fxml/produit-form.fxml"))
            val stage = Stage().apply {
                scene = Scene(loader.load(), 600.0, 550.0)
                title = if (produit == null) "Ajouter un produit" else "Modifier : ${produit.nom}"
                initModality(Modality.APPLICATION_MODAL)
                isResizable = false
            }

            val controller = loader.getController<ProduitFormController>()
            controller.initialiser(produit, stage)

            stage.showAndWait()

            // Rafraîchir la liste après fermeture du formulaire
            chargerProduits()

        } catch (e: Exception) {
            e.printStackTrace()
            afficherErreur("Erreur", "Impossible d'ouvrir le formulaire : ${e.message}")
        }
    }

    private fun ouvrirMouvement(produit: Produit, estEntree: Boolean) {
        try {
            val loader = FXMLLoader(javaClass.getResource("/fxml/mouvement-form.fxml"))
            val stage = Stage().apply {
                scene = Scene(loader.load(), 480.0, 350.0)
                title = if (estEntree) "Entrée de stock — ${produit.nom}"
                        else "Sortie de stock — ${produit.nom}"
                initModality(Modality.APPLICATION_MODAL)
                isResizable = false
            }

            val controller = loader.getController<MouvementFormController>()
            controller.initialiser(produit, estEntree, stage)

            stage.showAndWait()
            chargerProduits()

        } catch (e: Exception) {
            afficherErreur("Erreur", "Impossible d'ouvrir le formulaire : ${e.message}")
        }
    }

    // =========================================================================
    // Utilitaires d'affichage
    // =========================================================================

    private fun afficherErreur(titre: String, message: String) {
        Alert(Alert.AlertType.ERROR).apply {
            this.title = titre
            headerText = null
            contentText = message
            showAndWait()
        }
    }

    private fun afficherInfo(titre: String, message: String) {
        Alert(Alert.AlertType.INFORMATION).apply {
            this.title = titre
            headerText = null
            contentText = message
            showAndWait()
        }
    }
}
