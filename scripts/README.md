# 📸 Scripts de Screenshots pour Play Store

Scripts automatisés pour capturer et préparer les screenshots pour Google Play Store.

## 🚀 Utilisation Rapide

### 1. Activer le mode Pro (optionnel)
```bash
./scripts/enable_pro_mode.sh
```
Active temporairement le mode Pro pour montrer toutes les fonctionnalités.

### 2. Capturer les screenshots
```bash
./scripts/capture_screenshots.sh
```
Guide interactif pour capturer 6 screenshots professionnels.

### 3. Redimensionner pour Play Store
```bash
./scripts/resize_screenshots.sh
```
Redimensionne automatiquement à 1080x2340 pixels (format Play Store).

### 4. Désactiver le mode Pro
```bash
./scripts/disable_pro_mode.sh
```
Remet l'app en mode FREE.

---

## 📋 Prérequis

### Pour capture_screenshots.sh
- Émulateur Android en cours d'exécution
- MoneyOne installé sur l'émulateur
- ADB installé et configuré

### Pour resize_screenshots.sh
- ImageMagick installé:
  ```bash
  brew install imagemagick
  ```

---

## 📁 Structure des fichiers

```
MoneyOne/
├── scripts/
│   ├── enable_pro_mode.sh      # Active mode Pro
│   ├── capture_screenshots.sh  # Capture screenshots
│   ├── resize_screenshots.sh   # Redimensionne images
│   ├── disable_pro_mode.sh     # Désactive mode Pro
│   └── README.md               # Ce fichier
└── screenshots/
    ├── 01_main_screen.png
    ├── 02_add_transaction.png
    ├── 03_budgets.png
    ├── 04_savings_goals.png
    ├── 05_settings.png
    ├── 06_smart_insights.png
    └── playstore/               # Screenshots redimensionnés
        ├── 01_main_screen.png   # 1080x2340
        ├── 02_add_transaction.png
        └── ...
```

---

## 🎯 Screenshots à capturer

1. **Écran principal** - Calendrier avec transactions
2. **Ajout transaction** - Formulaire rempli
3. **Budgets** - Liste avec barres de progression
4. **Objectifs d'épargne** - Objectifs avec progression
5. **Settings** - Paramètres de l'app
6. **Smart Insights** - Graphiques et analyses (Pro)

---

## 💡 Conseils

### Avant de capturer:
- Ajoutez des données de démo réalistes
- Utilisez le thème par défaut
- Langue: Français
- Mode Pro activé pour montrer toutes les fonctionnalités

### Après capture:
- Vérifiez que toutes les images sont nettes
- Ajoutez du texte avec Canva (optionnel)
- Uploadez sur Play Console

---

## 🔧 Dépannage

### "Aucun émulateur détecté"
```bash
# Vérifier les appareils connectés
adb devices

# Redémarrer ADB si nécessaire
adb kill-server
adb start-server
```

### "ImageMagick n'est pas installé"
```bash
# macOS
brew install imagemagick

# Linux
sudo apt-get install imagemagick
```

### "MoneyOne n'est pas installé"
```bash
# Installer l'APK
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## 📊 Format Play Store

- **Taille**: 1080x2340 pixels
- **Ratio**: 9:19.5
- **Format**: PNG
- **Qualité**: 95%
- **Nombre**: Minimum 2, recommandé 5-8

---

**Créé pour MoneyOne - Février 2026** 🚀
