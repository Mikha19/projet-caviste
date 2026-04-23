package com.cavemanager

import com.cavemanager.dao.DatabaseConfig
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.control.Alert
import javafx.stage.Stage

/**
 * Point d'entrée de l'application CaveManager Desktop.
 *
 * JavaFX requiert une classe qui étend Application.
 * La méthode start() est le vrai "main" de l'application graphique.
 *
 * NOTE : En Kotlin, la fonction main() globale appelle Application.launch()
 * qui instancie la classe App et appelle start() sur le thread JavaFX.
 */
class App : Application() {

    override fun start(primaryStage: Stage) {
        // 1. Initialisation de la connexion base de données
        try {
            DatabaseConfig.initialize()
            if (!DatabaseConfig.testerConnexion()) {
                afficherErreurBDD()
                return
            }
        } catch (e: Exception) {
            afficherErreurBDD(e.message)
            return
        }

        // 2. Chargement de l'interface principale
        try {
            val loader = FXMLLoader(App::class.java.getResource("/fxml/main.fxml"))
            val scene = Scene(loader.load(), 1200.0, 750.0)

            // Application du CSS global
            scene.stylesheets.add(App::class.java.getResource("/css/styles.css")!!.toExternalForm())

            primaryStage.apply {
                title = "🍷 CaveManager Pro — Gestion de Stock"
                this.scene = scene
                minWidth = 900.0
                minHeight = 600.0
                show()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Alert(Alert.AlertType.ERROR).apply {
                title = "Erreur de démarrage"
                headerText = "Impossible de charger l'interface"
                contentText = e.message
                showAndWait()
            }
        }
    }

    private fun afficherErreurBDD(detail: String? = null) {
        Alert(Alert.AlertType.ERROR).apply {
            title = "Erreur de connexion"
            headerText = "Impossible de se connecter à la base de données"
            contentText = buildString {
                appendLine("Vérifiez que :")
                appendLine("• MySQL Server est démarré")
                appendLine("• Les paramètres dans database.properties sont corrects")
                appendLine("• La base de données 'cavemanager' existe")
                detail?.let { appendLine("\nDétail : $it") }
            }
            showAndWait()
        }
    }
}

/**
 * Point d'entrée Kotlin.
 * On appelle Application.launch() qui gère le cycle de vie JavaFX.
 */
fun main() {
    Application.launch(App::class.java)
}
