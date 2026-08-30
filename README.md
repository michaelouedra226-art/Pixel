# PixelForge — Pro Graphic Engine & Design Studio

PixelForge est une application Android moderne de création graphique, d'édition photo et de design vectoriel développée en **Kotlin** et **Jetpack Compose** avec **Material Design 3**.

---

## 🚀 Compilation & Exécution sur GitHub & en Local

### Prérequis
- **JDK 17** ou supérieur
- **Android SDK** (API 36, minSdk 24)
- **Android Studio Ladybug / Koala** ou terminal CLI

### Commandes de Compilation (CLI)

1. **Cloner le dépôt :**
   ```bash
   git clone https://github.com/your-username/pixelforge.git
   cd pixelforge
   ```

2. **Préparer l'environnement :**
   ```bash
   cp .env.example .env
   ```

3. **Générer le keystore de debug (si nécessaire) :**
   ```bash
   if [ -f debug.keystore.base64 ]; then
     base64 -d debug.keystore.base64 > debug.keystore
   fi
   ```

4. **Lancer la compilation de l'APK Debug :**
   ```bash
   chmod +x gradlew
   ./gradlew assembleDebug
   ```
   L'APK généré se trouvera dans : `app/build/outputs/apk/debug/app-debug.apk`

5. **Exécuter les tests unitaires :**
   ```bash
   ./gradlew testDebugUnitTest
   ```

---

## 🤖 Intégration Continue (GitHub Actions)

Le workflow CI configuré dans `.github/workflows/android.yml` exécute automatiquement :
1. La configuration de Java JDK 17 et Gradle
2. La préparation de l'environnement et du keystore
3. L'exécution des tests
4. La compilation de l'APK (`./gradlew assembleDebug`)
5. L'archivage de l'APK prêt au téléchargement dans les *Artifacts* de la page GitHub Actions.

---

## 🛠️ Architecture du Projet

- **UI & Design System** : Jetpack Compose, Material Design 3, Thème Obsidian & Champagne Gold.
- **Rendu Graphique 2D** : Moteur Canvas multi-calques (Textes enrichis 3D/dégradés, Formes vectorielles, Tracé de Bézier interactif, Dessin à main levée, Stickers et Import d'images).
- **Base de Données Locale** : Room Database pour la persistance des projets et calques.
- **Moteur d'Export** : Export haute fidélité vers PNG (avec transparence Alpha), JPG, WebP et SVG vectoriel.
