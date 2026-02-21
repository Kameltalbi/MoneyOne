#!/bin/bash

# Script pour redimensionner les screenshots au format Play Store
# Format requis: 1080x2340 pixels (ratio 9:19.5)
# Usage: ./resize_screenshots.sh

set -e

echo "🎨 Script de redimensionnement pour Play Store"
echo "=============================================="
echo ""

# Vérifier si ImageMagick est installé
if ! command -v convert &> /dev/null; then
    echo "❌ ImageMagick n'est pas installé"
    echo ""
    echo "Installation:"
    echo "  brew install imagemagick"
    echo ""
    exit 1
fi

echo "✅ ImageMagick détecté"
echo ""

SCREENSHOTS_DIR="screenshots"
RESIZED_DIR="screenshots/playstore"

# Créer le dossier de sortie
mkdir -p "$RESIZED_DIR"

echo "📁 Dossier de sortie: $RESIZED_DIR"
echo ""

# Vérifier s'il y a des screenshots
if ! ls "$SCREENSHOTS_DIR"/*.png 1> /dev/null 2>&1; then
    echo "❌ Aucun screenshot trouvé dans $SCREENSHOTS_DIR/"
    echo "   Exécutez d'abord: ./scripts/capture_screenshots.sh"
    exit 1
fi

echo "🔍 Screenshots trouvés:"
ls "$SCREENSHOTS_DIR"/*.png | grep -v playstore | while read file; do
    echo "   - $(basename "$file")"
done
echo ""

echo "🎨 Redimensionnement en cours..."
echo ""

# Compteur
count=0

# Redimensionner chaque screenshot
for file in "$SCREENSHOTS_DIR"/*.png; do
    # Ignorer le dossier playstore
    if [[ "$file" == *"playstore"* ]]; then
        continue
    fi
    
    filename=$(basename "$file")
    output="$RESIZED_DIR/$filename"
    
    echo "   📐 Traitement: $filename"
    
    # Redimensionner à 1080x2340 (format Play Store)
    # -resize: redimensionne en gardant le ratio
    # -gravity center: centre l'image
    # -extent: force la taille exacte avec bordures si nécessaire
    convert "$file" \
        -resize 1080x2340^ \
        -gravity center \
        -extent 1080x2340 \
        -quality 95 \
        "$output"
    
    if [ -f "$output" ]; then
        size=$(du -h "$output" | cut -f1)
        echo "      ✅ Sauvegardé: $filename ($size)"
        ((count++))
    else
        echo "      ❌ Erreur: $filename"
    fi
done

echo ""
echo "✅ Redimensionnement terminé!"
echo ""
echo "📊 Résumé:"
echo "   - Screenshots traités: $count"
echo "   - Format: 1080x2340 pixels"
echo "   - Qualité: 95%"
echo ""
echo "📁 Screenshots Play Store prêts dans:"
echo "   $RESIZED_DIR/"
echo ""
ls -lh "$RESIZED_DIR"/*.png 2>/dev/null | awk '{print "   -", $9, "(" $5 ")"}'
echo ""
echo "🚀 Prêt pour upload sur Play Console!"
echo ""
