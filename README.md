# SmartBudget 💰

Application Android native de gestion des dépenses personnelles, construite avec **Kotlin** et **Jetpack Compose**.

## Fonctionnalités

### Écran principal - Vue Mois
- **Calendrier mensuel** interactif avec navigation gauche/droite
- Affichage du solde quotidien sous chaque jour (vert = positif, rouge = négatif)
- Jour actuel entouré, jour sélectionné surligné
- **Résumé mensuel** : solde, revenus, dépenses, écart budget
- **Liste des transactions** du jour sélectionné avec icône, montant, checkbox validation
- Bouton flottant "+" pour ajouter une transaction

### Ajouter une transaction
- Type : Dépense / Revenu
- Montant avec clavier numérique
- Sélection de catégorie avec grille d'icônes
- Sélecteur de date Material 3
- Note optionnelle
- Récurrence : Unique / Hebdomadaire / Mensuel

### Paramètres
- **Gestion des catégories** : Ajouter / Modifier / Supprimer
  - Icône personnalisable (30+ icônes Material)
  - Couleur personnalisable (18 couleurs)
  - Type (revenu ou dépense)
- **Budget mensuel** : Définir un budget global par mois
  - Barre de progression avec alertes (>80%, >100%)

### Tableau de bord
- Graphique camembert (donut) des dépenses par catégorie
- Jauge d'utilisation du budget
- Comparaison revenus vs dépenses avec barres

## Architecture technique

| Composant | Technologie |
|-----------|-------------|
| Langage | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Base de données | Room (SQLite) |
| Architecture | MVVM |
| State management | StateFlow |
| Navigation | Navigation Compose |
| Thème | Light + Dark Mode |

## Structure du projet

```
app/src/main/java/com/smartbudget/
├── SmartBudgetApp.kt          # Application class + seed data
├── MainActivity.kt            # Entry point
├── data/
│   ├── entity/                # Room entities (Account, Transaction, Category, Budget)
│   ├── dao/                   # Data Access Objects
│   ├── repository/            # Repository layer
│   ├── Converters.kt          # Room type converters
│   └── SmartBudgetDatabase.kt # Room database
└── ui/
    ├── theme/                 # Material 3 theme (colors, typography)
    ├── components/            # Reusable composables (Calendar, Summary, TransactionItem)
    ├── screens/               # Screen composables (Main, AddTransaction, Settings, Dashboard)
    ├── viewmodel/             # ViewModels (Main, Transaction, Settings)
    ├── navigation/            # Navigation graph
    └── util/                  # Utilities (IconMapper, CurrencyFormatter, DateUtils)
```

## Base de données

### Tables
- **accounts** : Comptes (multi-comptes supporté)
- **transactions** : Transactions avec montant, type, date, récurrence
- **categories** : Catégories personnalisables avec icône et couleur
- **budgets** : Budgets mensuels (global ou par catégorie)

### Relations
- `transaction → category` (FK)
- `transaction → account` (FK)

## Catégories par défaut
- Salaire, Freelance (revenus)
- Alimentation, Transport, Logement, Shopping, Santé, Loisirs (dépenses)

## Build & Run

1. Ouvrir le projet dans **Android Studio Hedgehog** ou plus récent
2. Sync Gradle
3. Run sur un émulateur ou appareil (API 26+)

```bash
./gradlew assembleDebug
```

## Évolutions prévues
- [ ] Export PDF / CSV
- [ ] Multi comptes complet
- [ ] Backup cloud
- [ ] Widget Android
- [ ] Sécurité par PIN
- [ ] Comparaison mois précédent
- [ ] Budget par catégorie
