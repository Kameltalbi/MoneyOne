# MoneyOne - Version GRATUITE vs PRO

## 🆓 VERSION GRATUITE (FREE)

### ✅ Fonctionnalités incluses

#### 👤 Utilisateur
- **1 utilisateur** (offline)
- Pas de login obligatoire
- Utilisation 100% locale

#### 🏦 Comptes bancaires
- **2 comptes bancaires maximum**
  - Exemple : Cash + Banque principale
  - Limitation : `maxAccounts = 2` (non-PRO)

#### 🏷️ Catégories
- **Catégories par défaut seulement**
- ❌ Pas de catégories personnalisées
- Les catégories par défaut sont pré-installées
- Limitation : Création/modification de catégories désactivée

#### 🎯 Objectifs d'épargne
- **1 objectif d'épargne maximum**
- Limitation : `maxSavingsGoals = 1` (non-PRO)

#### 💰 Budgets
- **1 budget actif maximum**
  - Peut être global OU par catégorie
  - Limitation : `maxBudgets = 1` (non-PRO)

#### ☁️ Sauvegarde
- **Google Drive backup manuel**
- Pas de synchronisation automatique
- L'utilisateur doit déclencher manuellement la sauvegarde

#### 📊 Transactions
- ✅ Transactions illimitées
- ✅ Transactions récurrentes
- ✅ Graphiques et statistiques
- ✅ Recherche de transactions
- ✅ Export de données

---

## ⭐ VERSION PRO (PREMIUM)

### ✅ Toutes les fonctionnalités FREE +

#### 🏦 Comptes bancaires
- **Comptes bancaires illimités**
- Pas de limite (techniquement limité à `Int.MAX_VALUE`)

#### 🏷️ Catégories
- **Catégories personnalisées illimitées**
- Création de catégories personnalisées
- Modification des catégories existantes
- Choix d'icônes et couleurs

#### 🎯 Objectifs d'épargne
- **Objectifs d'épargne illimités**
- Créez autant d'objectifs que vous voulez

#### 💰 Budgets
- **Budgets illimités**
- Budget global
- Budgets par catégorie (illimités)
- Budgets multiples par mois

#### 🎨 Personnalisation
- **Thèmes de couleurs personnalisés**
- Choix parmi plusieurs thèmes
- Interface personnalisable

#### ⚙️ Fonctionnalités avancées
- **Ajustement de solde**
- Outils de gestion avancés
- Fonctionnalités premium exclusives

#### ☁️ Sauvegarde
- **Google Drive backup automatique** (à implémenter)
- Synchronisation automatique
- Multi-device (à implémenter)

---

## 📋 Tableau comparatif

| Fonctionnalité | FREE | PRO |
|----------------|------|-----|
| **Utilisateurs** | 1 (offline) | 1 (offline) |
| **Comptes bancaires** | 2 max | Illimités |
| **Catégories personnalisées** | ❌ | ✅ |
| **Objectifs d'épargne** | 1 max | Illimités |
| **Budgets actifs** | 1 max | Illimités |
| **Transactions** | ✅ Illimitées | ✅ Illimitées |
| **Transactions récurrentes** | ✅ | ✅ |
| **Graphiques** | ✅ | ✅ |
| **Recherche** | ✅ | ✅ |
| **Export** | ✅ | ✅ |
| **Thèmes de couleurs** | ❌ | ✅ |
| **Ajustement de solde** | ❌ | ✅ |
| **Google Drive backup** | Manuel | Automatique* |
| **Multi-device** | ❌ | ✅* |

*À implémenter

---

## 🔧 Implémentation technique

### Vérifications dans le code

#### Comptes bancaires
```kotlin
// SettingsViewModel.kt
fun addAccount(name: String, currency: String, isPro: Boolean, ...) {
    val maxAccounts = if (isPro) Int.MAX_VALUE else 2
    if (count >= maxAccounts) {
        onError("free_max_accounts")
    }
}
```

#### Catégories personnalisées
```kotlin
// SettingsScreen.kt
onClick = if (isPro) onNavigateCategories else onNavigateProUpgrade
```

#### Objectifs d'épargne
```kotlin
// MainViewModel.kt
fun addSavingsGoal(name: String, targetAmount: Double, isPro: Boolean, ...) {
    if (!isPro && currentGoals.size >= 1) {
        onError("free_max_savings_goals")
    }
}
```

#### Budgets
```kotlin
// SettingsViewModel.kt
fun saveGlobalBudget(isPro: Boolean, ...) {
    if (!isPro && allBudgets.isNotEmpty()) {
        onError("free_max_budgets")
    }
}
```

### Messages d'erreur à ajouter

Dans `strings.xml` :
```xml
<string name="free_max_accounts">Version gratuite limitée à 2 comptes. Passez à Pro pour des comptes illimités ⭐</string>
<string name="free_max_savings_goals">Version gratuite limitée à 1 objectif d\'épargne. Passez à Pro pour des objectifs illimités ⭐</string>
<string name="free_max_budgets">Version gratuite limitée à 1 budget. Passez à Pro pour des budgets illimités ⭐</string>
<string name="pro_categories_only">Les catégories personnalisées sont réservées à la version Pro ⭐</string>
```

---

## 🚀 Prochaines étapes

1. ✅ Limiter les comptes à 2 pour FREE
2. ✅ Bloquer les catégories personnalisées pour FREE
3. ✅ Limiter les objectifs d'épargne à 1 pour FREE
4. ✅ Limiter les budgets à 1 pour FREE
5. ⏳ Mettre à jour les strings.xml avec les messages d'erreur
6. ⏳ Mettre à jour l'UI pour afficher les limitations
7. ⏳ Tester toutes les limitations
8. ⏳ Implémenter la sync automatique Google Drive pour PRO
9. ⏳ Implémenter le multi-device pour PRO

---

## ⚠️ Important

**Avant publication sur Play Store :**
```kotlin
// BillingManager.kt ligne 22
// TODO: Remettre à false avant publication
private val _isPro = MutableStateFlow(true)  // ← Changer à false
```

Actuellement, `isPro` est forcé à `true` pour les tests. Il faut le remettre à `false` et réactiver le système de billing avant la publication.
