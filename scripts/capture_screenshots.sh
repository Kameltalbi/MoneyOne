#!/bin/bash

# Script pour capturer des screenshots professionnels pour Play Store
# Usage: ./capture_screenshots.sh

set -e

echo "📸 Script de capture de screenshots pour MoneyOne"
echo "=================================================="
echo ""

# Vérifier si l'émulateur est connecté
if ! adb devices | grep -q "emulator"; then
    echo "❌ Erreur: Aucun émulateur détecté"
    echo "Veuillez démarrer l'émulateur et réessayer"
    exit 1
fi

echo "✅ Émulateur détecté"
echo ""

# Créer le dossier screenshots s'il n'existe pas
SCREENSHOTS_DIR="screenshots"
mkdir -p "$SCREENSHOTS_DIR"

echo "📁 Dossier screenshots créé: $SCREENSHOTS_DIR"
echo ""

# Fonction pour capturer un screenshot
capture_screenshot() {
    local name=$1
    local delay=$2
    
    echo "⏳ Attente de ${delay}s pour '$name'..."
    sleep "$delay"
    
    echo "📸 Capture de '$name'..."
    adb exec-out screencap -p > "$SCREENSHOTS_DIR/${name}.png"
    
    if [ -f "$SCREENSHOTS_DIR/${name}.png" ]; then
        echo "✅ Screenshot sauvegardé: ${name}.png"
    else
        echo "❌ Erreur lors de la capture de ${name}.png"
    fi
    echo ""
}

echo "🎬 Début de la capture..."
echo ""
echo "INSTRUCTIONS:"
echo "1. Assurez-vous que MoneyOne est ouvert sur l'émulateur"
echo "2. Naviguez vers chaque écran quand demandé"
echo "3. Appuyez sur ENTRÉE pour capturer chaque screenshot"
echo ""

# Screenshot 1: Écran principal (Calendrier)
echo "📱 Screenshot 1/6: Écran principal (Calendrier avec transactions)"
echo "   → Assurez-vous d'avoir des transactions visibles"
read -p "   Appuyez sur ENTRÉE quand prêt..."
capture_screenshot "01_main_screen" 1

# Screenshot 2: Ajout de transaction
echo "📱 Screenshot 2/6: Formulaire d'ajout de transaction"
echo "   → Ouvrez le formulaire d'ajout (bouton +)"
read -p "   Appuyez sur ENTRÉE quand prêt..."
capture_screenshot "02_add_transaction" 1

# Screenshot 3: Budgets
echo "📱 Screenshot 3/6: Écran Budgets"
echo "   → Naviguez vers l'onglet Budgets"
read -p "   Appuyez sur ENTRÉE quand prêt..."
capture_screenshot "03_budgets" 1

# Screenshot 4: Objectifs d'épargne
echo "📱 Screenshot 4/6: Objectifs d'épargne"
echo "   → Naviguez vers l'onglet Objectifs"
read -p "   Appuyez sur ENTRÉE quand prêt..."
capture_screenshot "04_savings_goals" 1

# Screenshot 5: Settings
echo "📱 Screenshot 5/6: Paramètres"
echo "   → Naviguez vers l'onglet Plus/Settings"
read -p "   Appuyez sur ENTRÉE quand prêt..."
capture_screenshot "05_settings" 1

# Screenshot 6: Smart Insights (optionnel)
echo "📱 Screenshot 6/6: Smart Insights (optionnel)"
echo "   → Si en mode Pro, naviguez vers Smart Insights"
read -p "   Appuyez sur ENTRÉE pour capturer (ou Ctrl+C pour passer)..."
capture_screenshot "06_smart_insights" 1

echo ""
echo "✅ Capture terminée!"
echo ""
echo "📊 Résumé:"
ls -lh "$SCREENSHOTS_DIR"/*.png 2>/dev/null | awk '{print "   -", $9, "(" $5 ")"}'
echo ""
echo "📁 Screenshots sauvegardés dans: $SCREENSHOTS_DIR/"
echo ""
echo "🎨 Prochaine étape: Redimensionner les images"
echo "   Exécutez: ./scripts/resize_screenshots.sh"
echo ""
