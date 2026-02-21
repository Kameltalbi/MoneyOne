#!/bin/bash

# Script pour activer temporairement le mode Pro pour les screenshots
# Usage: ./enable_pro_mode.sh

set -e

echo "💎 Activation du mode Pro pour screenshots"
echo "=========================================="
echo ""

PACKAGE_NAME="com.smartbudget"
PREFS_FILE="/data/data/$PACKAGE_NAME/shared_prefs/moneyone_pro.xml"

# Vérifier si l'émulateur est connecté
if ! adb devices | grep -q "emulator"; then
    echo "❌ Erreur: Aucun émulateur détecté"
    exit 1
fi

echo "✅ Émulateur détecté"
echo ""

# Vérifier si l'app est installée
if ! adb shell pm list packages | grep -q "$PACKAGE_NAME"; then
    echo "❌ MoneyOne n'est pas installé sur l'émulateur"
    echo "   Installez l'app d'abord"
    exit 1
fi

echo "✅ MoneyOne détecté"
echo ""

echo "🔧 Configuration du mode Pro..."

# Arrêter l'app
adb shell am force-stop "$PACKAGE_NAME"

# Activer le mode Pro via SharedPreferences
adb shell "run-as $PACKAGE_NAME sh -c 'cat > shared_prefs/moneyone_pro.xml << EOF
<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\" ?>
<map>
    <boolean name=\"is_pro\" value=\"true\" />
</map>
EOF'"

echo "✅ Mode Pro activé!"
echo ""

# Redémarrer l'app
echo "🚀 Redémarrage de MoneyOne..."
adb shell monkey -p "$PACKAGE_NAME" -c android.intent.category.LAUNCHER 1 > /dev/null 2>&1

sleep 2

echo ""
echo "✅ MoneyOne redémarré en mode Pro!"
echo ""
echo "💎 Vous pouvez maintenant:"
echo "   1. Accéder à Smart Insights"
echo "   2. Créer des comptes/budgets/objectifs illimités"
echo "   3. Prendre des screenshots professionnels"
echo ""
echo "⚠️  N'oubliez pas de désactiver le mode Pro après:"
echo "   ./scripts/disable_pro_mode.sh"
echo ""
