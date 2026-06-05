# 🍷 CaveManager Pro

Application de gestion de stock pour caviste, développée en **Kotlin + JavaFX** (desktop) et **Kotlin + Jetpack Compose** (Android), avec une base de données **MySQL Server** partagée.

> Projet pédagogique — Bac+2 / Bac+5 Développement Logiciel

---

## Sommaire

1. [Présentation](#1-présentation)
2. [Stack technique](#2-stack-technique)
3. [Prérequis](#3-prérequis)
4. [Installation de l'environnement](#4-installation-de-lenvironnement)
5. [Configuration de la base de données](#5-configuration-de-la-base-de-données)
6. [Lancer le projet en développement](#6-lancer-le-projet-en-développement)
7. [Structure du projet](#7-structure-du-projet)
8. [Fonctionnalités](#8-fonctionnalités)
9. [Déploiement](#9-déploiement)
10. [Travaux Pratiques](#10-travaux-pratiques)
11. [Problèmes fréquents](#11-problèmes-fréquents)

---

## 1. Présentation

CaveManager Pro permet à un caviste de :
- Gérer son catalogue de produits (vins, champagnes, spiritueux, bières)
- Suivre les entrées et sorties de stock en temps réel
- Être alerté visuellement quand un produit passe sous son seuil d'alerte
- Consulter l'historique des mouvements de stock

Le projet est volontairement **incomplet** : les étudiants doivent y ajouter des fonctionnalités guidées par les Travaux Pratiques (voir section 10).

---

## 2. Stack technique

| Couche          | Technologie                        | Version  |
|-----------------|------------------------------------|----------|
| Langage         | Kotlin                             | 2.0.21   |
| UI Desktop      | JavaFX + FXML                      | 21       |
| UI Mobile       | Jetpack Compose                    | Android  |
| Base de données | MySQL Server                       | 8.0+     |
| Accès BDD       | JDBC (mysql-connector-j)           | 8.3.0    |
| Build           | Gradle Kotlin DSL                  | 8.x      |
| IDE Desktop     | IntelliJ IDEA Community            | 2024.x   |
| IDE Android     | Android Studio                     | Ladybug+ |

---

## 3. Prérequis

Installez les outils suivants **dans cet ordre** avant de continuer.

### 3.1 OpenJDK 23

Vérifiez si Java est déjà installé :
```bash
java -version
```
Si la commande échoue ou si la version est inférieure à 17, téléchargez OpenJDK 23 :
- Windows/Mac/Linux : https://adoptium.net

Après installation, vérifiez que `JAVA_HOME` est bien défini :
```bash
# Windows (PowerShell)
echo $env:JAVA_HOME

# Mac / Linux
echo $JAVA_HOME
```
Si vide, définissez-le manuellement (voir FAQ section 11).

### 3.2 IntelliJ IDEA Community

Téléchargement gratuit : https://www.jetbrains.com/idea/download  
Choisissez **Community Edition** (pas besoin de licence).

### 3.3 MySQL Server 8.0+

Téléchargement : https://dev.mysql.com/downloads/mysql  
Pendant l'installation :
- Notez bien le **mot de passe root** que vous choisissez
- Laissez le port par défaut : **3306**
- Choisissez "Developer Default" comme type d'installation

### 3.4 MySQL Workbench

Téléchargement : https://dev.mysql.com/downloads/workbench  
Outil graphique pour administrer la base de données.  
Généralement inclus dans l'installation MySQL Server.

### 3.5 Git

Téléchargement : https://git-scm.com  
Après installation, configurez votre identité :
```bash
git config --global user.name "Votre Nom"
git config --global user.email "votre@email.com"
```

---

## 4. Installation de l'environnement

### 4.1 Récupérer le projet

```bash
# Cloner le dépôt
git clone <url-du-depot>
cd cavemanager-desktop

# Ou décompresser l'archive ZIP fournie
```

### 4.2 Ouvrir dans IntelliJ IDEA

1. Lancer IntelliJ IDEA
2. `File` → `Open` → sélectionner le dossier `cavemanager-desktop`
3. IntelliJ détecte automatiquement le projet Gradle
4. Attendre la fin de la synchronisation Gradle (barre de progression en bas)  
   ⚠️ La première fois peut prendre 2 à 5 minutes selon la connexion Internet

### 4.3 Vérifier la configuration Gradle

Si IntelliJ affiche une erreur Gradle, vérifiez :
- `File` → `Project Structure` → `SDK` → sélectionner votre JDK 23
- `File` → `Settings` → `Build` → `Gradle` → `Gradle JVM` → sélectionner le même JDK

---

## 5. Configuration de la base de données

### 5.1 Créer la base et l'utilisateur

Ouvrez **MySQL Workbench**, connectez-vous en root, puis :

```
File → Open SQL Script → sélectionner schema.sql → ⚡ Exécuter (Ctrl+Shift+Enter)
```

Ce script crée :
- La base de données `cavemanager`
- L'utilisateur `cavemanager_user` avec le mot de passe `cavemanagerTest123!`
- Les tables `categories`, `produits`, `mouvements_stock`

### 5.2 Injecter les données de démonstration

```
File → Open SQL Script → sélectionner seed.sql → ⚡ Exécuter
```

Vérification :
```sql
USE cavemanager;
SELECT COUNT(*) FROM produits;    -- doit retourner 30
SELECT COUNT(*) FROM categories;  -- doit retourner 6
```

### 5.3 Configurer la connexion dans l'application

Ouvrez le fichier :
```
src/main/resources/database.properties
```

Contenu par défaut (modifiez si nécessaire) :
```properties
db.host=localhost
db.port=3306
db.name=cavemanager
db.user=cavemanager_user
db.password=cavemanagerTest123!
```

> ⚠️ Ne committez jamais ce fichier avec de vrais mots de passe sur un dépôt public.

---

## 6. Lancer le projet en développement

### Depuis IntelliJ IDEA

`Run` → `Run 'MainKt'`  
Ou cliquer sur le bouton ▶️ vert en haut à droite.

### Depuis le terminal

```bash
# Windows
gradlew.bat run

# Mac / Linux
./gradlew run
```

L'application doit s'ouvrir et afficher la liste des 30 produits de démonstration.

---

## 7. Structure du projet

```
cavemanager-desktop/
│
├── build.gradle.kts                    ← Configuration Gradle (dépendances, plugins)
├── settings.gradle.kts                 ← Nom du projet
│
├── src/main/
│   ├── kotlin/com/cavemanager/
│   │   │
│   │   ├── Main.kt                     ← Point d'entrée, initialisation JavaFX
│   │   │
│   │   ├── model/                      ← Entités métier (data classes)
│   │   │   ├── Produit.kt
│   │   │   └── MouvementStock.kt
│   │   │
│   │   ├── dao/                        ← Accès base de données (SQL ici uniquement)
│   │   │   ├── DatabaseConfig.kt       ← Gestion de la connexion MySQL
│   │   │   ├── ProduitDao.kt           ← CRUD produits
│   │   │   └── MouvementStockDao.kt    ← CRUD mouvements + transactions
│   │   │
│   │   └── controller/                 ← Logique de l'interface utilisateur
│   │       ├── MainController.kt       ← Fenêtre principale
│   │       ├── ProduitFormController.kt
│   │       └── MouvementFormController.kt
│   │
│   └── resources/
│       ├── fxml/                       ← Interfaces utilisateur (XML)
│       │   ├── main.fxml
│       │   ├── produit-form.fxml
│       │   └── mouvement-form.fxml
│       ├── css/
│       │   └── styles.css              ← Thème visuel de l'application
│       └── database.properties         ← Configuration BDD (ne pas committer)
│
└── src/test/                           ← Tests unitaires (à compléter)
```

### Règles d'architecture à respecter

| Couche | Rôle | Interdit |
|---|---|---|
| `model/` | Définir les données | Aucun SQL, aucun import JavaFX |
| `dao/` | Lire/écrire en BDD | Aucune logique métier, aucun import JavaFX |
| `controller/` | Piloter l'UI | Aucun SQL direct |

---

## 8. Fonctionnalités

### Incluses dans la V1 (base fournie)

- ✅ Liste des produits avec recherche en temps réel
- ✅ Ajout d'un produit (formulaire complet avec validation)
- ✅ Modification d'un produit (double-clic ou bouton)
- ✅ Suppression avec confirmation (refusée si mouvements associés)
- ✅ Entrée de stock (transaction atomique BDD)
- ✅ Sortie de stock (vérification stock suffisant + transaction)
- ✅ Alerte visuelle pour les produits sous le seuil (fond rouge)
- ✅ Compteur d'alertes dans l'en-tête

### À implémenter par les étudiants (V2+)

Voir section 10 — Travaux Pratiques.

---

## 9. Déploiement

### 9.1 JAR exécutable (développement / démonstration)

Génère un JAR autonome avec toutes les dépendances incluses.  
**Prérequis :** Java doit être installé sur la machine cible.

```bash
./gradlew fatJar
```

Le fichier est généré dans :
```
build/libs/cavemanager-desktop-1.0.0-all.jar
```

Lancement :
```bash
java -jar build/libs/cavemanager-desktop-1.0.0-all.jar
```

---

### 9.2 Installeur natif Windows — EXE (production)

`jpackage` est inclus dans le JDK 17+. Il crée un `.exe` qui **embarque le JRE** : l'utilisateur n'a **rien à installer**.

**Étape 1 — Générer le fatJar**
```bash
./gradlew fatJar
```

**Étape 2 — Créer l'installeur EXE**
```bash
jpackage `
  --input build/libs `
  --main-jar cavemanager-desktop-1.0.0-all.jar `
  --main-class com.cavemanager.MainKt `
  --name CaveManager `
  --app-version 1.0.0 `
  --type exe `
  --dest build/installer `
  --win-menu `
  --win-shortcut `
  --win-dir-chooser
```

L'installeur est généré dans :
```
build/installer/CaveManager-1.0.0.exe
```

---

### 9.3 Installeur natif Windows — MSI (production, recommandé entreprise)

Le format MSI est préféré en entreprise car il supporte le déploiement via GPO.  
**Prérequis supplémentaire :** installer [WiX Toolset 3.x](https://wixtoolset.org/releases/)

```bash
jpackage `
  --input build/libs `
  --main-jar cavemanager-desktop-1.0.0-all.jar `
  --main-class com.cavemanager.MainKt `
  --name CaveManager `
  --app-version 1.0.0 `
  --type msi `
  --dest build/installer `
  --win-menu `
  --win-shortcut `
  --win-per-user-install
```

---

### 9.4 Automatiser le déploiement via Gradle

Ajoutez cette tâche dans `build.gradle.kts` pour ne pas retaper la commande :

```kotlin
tasks.register<Exec>("jpackage") {
    dependsOn("fatJar")
    commandLine(
        "jpackage",
        "--input", "build/libs",
        "--main-jar", "cavemanager-desktop-1.0.0-all.jar",
        "--main-class", "com.cavemanager.MainKt",
        "--name", "CaveManager",
        "--app-version", "1.0.0",
        "--type", "exe",
        "--dest", "build/installer",
        "--win-menu",
        "--win-shortcut"
    )
}
```

Puis :
```bash
./gradlew jpackage
```

---

### 9.5 Récapitulatif des options de déploiement

| Format | Commande | Java requis | Usage |
|--------|----------|-------------|-------|
| JAR | `./gradlew fatJar` | ✅ Oui | Développement, soutenance |
| EXE | `jpackage --type exe` | ❌ Non | Livraison client |
| MSI | `jpackage --type msi` | ❌ Non | Déploiement entreprise |

> ⚠️ **Avant toute soutenance**, testez le JAR sur une machine sans IntelliJ ouvert pour vous assurer qu'il fonctionne de façon autonome.

---

## 10. Travaux Pratiques

Le guide étudiant complet (PDF fourni séparément) détaille chaque TP avec le code Kotlin à compléter, les requêtes SQL à écrire et les critères de validation.

| TP  | Fonctionnalité                    | Difficulté   | Module            |
|-----|-----------------------------------|--------------|-------------------|
| TP1 | Gestion des catégories            | ⭐           | Desktop + BDD     |
| TP2 | Gestion des fournisseurs          | ⭐⭐         | Desktop + BDD     |
| TP3 | Historique des mouvements         | ⭐⭐         | Desktop + BDD     |
| TP4 | Tableau de bord (graphiques)      | ⭐⭐⭐       | Desktop           |
| TP5 | Mouvements de stock sur Android   | ⭐⭐         | Android           |
| TP6 | Notifications d'alerte Android    | ⭐⭐⭐       | Android           |
| TP7 | Export PDF et CSV                 | ⭐⭐⭐       | Desktop           |
| TP8 | Authentification utilisateurs     | ⭐⭐⭐⭐     | Desktop + Android |

---

## 11. Problèmes fréquents

### `JAVA_HOME` non défini (Windows)

```powershell
# Trouver le chemin de votre JDK
where java

# Définir JAVA_HOME (remplacer le chemin)
[System.Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Eclipse Adoptium\jdk-23", "User")
```
Redémarrer le terminal après.

### MySQL ne démarre pas

```bash
# Vérifier le statut
net start MySQL80

# Si le service s'appelle différemment, lister les services MySQL
sc query type= service | findstr -i mysql
```

### Accès refusé à la BDD

```sql
-- En root dans MySQL Workbench
DROP USER IF EXISTS 'cavemanager_user'@'localhost';
CREATE USER 'cavemanager_user'@'localhost' IDENTIFIED BY 'cavemanagerTest123!';
GRANT ALL PRIVILEGES ON cavemanager.* TO 'cavemanager_user'@'localhost';
FLUSH PRIVILEGES;
```

### Les émojis s'affichent mal dans la console Windows

Ajoutez ces arguments JVM dans `build.gradle.kts` :
```kotlin
applicationDefaultJvmArgs = listOf(
    "--add-opens", "javafx.graphics/com.sun.javafx.application=ALL-UNNAMED",
    "-Dfile.encoding=UTF-8",
    "-Dstdout.encoding=UTF-8"
)
```

### Warning "Unsupported JavaFX configuration"

Avertissement sans impact fonctionnel, lié au chargement de JavaFX via Gradle.  
Il disparaît lors du déploiement avec `jpackage`.

### `IllegalArgumentException: Unable to coerce ... to Node` sur la TableView

Le `placeholder` de la TableView doit être un nœud FXML, pas une chaîne. Dans `main.fxml` :
```xml
<TableView fx:id="tableViewProduits" VBox.vgrow="ALWAYS">
    <placeholder>
        <Label text="Aucun produit trouvé. Cliquez sur '+ Ajouter' pour commencer."
               style="-fx-text-fill: #9E9E9E; -fx-font-size: 14px;"/>
    </placeholder>
    ...
```

---

## Conventions de code

- **Langue** : variables et commentaires en français (projet pédagogique)
- **Nommage** : `camelCase` pour variables/fonctions, `PascalCase` pour classes
- **Indentation** : 4 espaces
- **SQL** : toujours utiliser des `PreparedStatement`, jamais de concaténation de chaînes
- **Commits Git** : messages en français, clairs et au présent — ex: `Ajoute la gestion des fournisseurs`

---

*CaveManager Pro — Projet pédagogique Kotlin — v1.0.0*
