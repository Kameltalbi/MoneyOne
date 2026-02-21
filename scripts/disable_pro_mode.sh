#!/bin/bash

# Script pour désactiver le mode Pro après les screenshots
# Usage: ./disable_pro_mode.sh

set -e

echo "🔓 Désactivation du mode Pro"
echo "============================"
echo ""

PACKAGE_NAME="com.smartbudget"

# Vérifier si l'émulateur est connecté
if ! adb devices | grep -q "emulator"; then
    echo "❌ Erreur: Aucun émulateur détecté"
    exit 1
fi

echo "✅ Émulateur détecté"
echo ""

# Arrêter l'app
adb shell am force-stop "$PACKAGE_NAME"

# Désactiver le mode Pro
adb shell "run-as $PACKAGE_NAME sh -c 'cat > shared_prefs/moneyone_pro.xml << EOF
<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\" ?>
<map>
    <boolean name=\"is_pro\" value=\"false\" />
</map>
EOF'"

echo "✅ Mode Pro désactivé!"
echo ""

# Redémarrer l'app
echo "🚀 Redémarrage de MoneyOne..."
adb shell monkey -p "$PACKAGE_NAME" -c android.intent.category.LAUNCHER 1 > /dev/null 2>&1

sleep 2

echo ""
echo "✅ MoneyOne redémarré en mode FREE!"
echo ""
