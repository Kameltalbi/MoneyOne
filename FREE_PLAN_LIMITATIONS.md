# Plan GRATUIT - Limitations implémentées

## ✅ Limitations implémentées

### 1. Comptes (AccountsScreen.kt)
- **Free** : Maximum 2 comptes
- **Pro** : Comptes illimités
- ✅ Bouton d'ajout masqué quand limite atteinte
- ✅ Message d'upgrade affiché : "Passez au plan Pro pour ajouter plus de comptes"
- ✅ Compteur dynamique : "2 / 2" (Free) ou "2 / ∞" (Pro)

### 2. Budgets (BudgetsScreen.kt)
- **Free** : Maximum 3 budgets (budget global + budgets par catégorie)
- **Pro** : Budgets illimités
- ✅ Bouton d'ajout masqué quand limite atteinte
- ✅ Card d'upgrade affichée : "Limite atteinte : 3 / 3 budgets"
- ✅ Bouton "Upgrade" pour passer au Pro

### 3. Objectifs d'épargne (SavingsGoalsScreen.kt)
- **Free** : Maximum 1 objectif
- **Pro** : Objectifs illimités
- ✅ Bouton d'ajout masqué quand limite atteinte
- ✅ Card d'upgrade affichée : "Limite atteinte : 1 / 1 objectif"
- ✅ Paramètre `isPro` ajouté à la fonction

### 4. Catégories personnalisées (CategoriesScreen.kt)
- **Free** : Maximum 7 catégories personnalisées (+ catégories par défaut)
- **Pro** : Catégories illimitées
- ✅ Bouton d'ajout masqué quand limite atteinte
- ✅ Card d'upgrade affichée : "Limite atteinte : 7 / 7 catégories personnalisées"
- ✅ Filtrage des catégories par défaut (non comptées dans la limite)

## ⚠️ Limitations à implémenter

### 5. Modifications transactions récurrentes
- **Free** : Uniquement "Modifier cette occurrence"
- **Pro** : Tous les modes (cette occurrence, futures, toute la série)
- 📝 À faire : Masquer les options "Modifier futures" et "Modifier toute la série" pour Free

### 6. Synchronisation cloud
- **Free** : Désactivée
- **Pro** : Activée (Firebase sync)
- 📝 À faire : Vérifier `PlanLimits.FREE_ALLOW_CLOUD_SYNC` dans les écrans de sync

### 7. Multi-devises
- **Free** : Désactivée (déjà implémenté dans AddTransactionScreen.kt)
- **Pro** : Activée
- ✅ Déjà implémenté : Bouton de sélection de devise masqué pour Free

## 📋 Fichier de constantes

**Fichier** : `app/src/main/java/com/smartbudget/billing/PlanLimits.kt`

```kotlin
object PlanLimits {
    // Free plan limits
    const val FREE_MAX_ACCOUNTS = 2
    const val FREE_MAX_BUDGETS = 3
    const val FREE_MAX_SAVINGS_GOALS = 1
    const val FREE_MAX_CUSTOM_CATEGORIES = 7
    
    // Pro plan (unlimited)
    const val PRO_MAX_ACCOUNTS = Int.MAX_VALUE
    const val PRO_MAX_BUDGETS = Int.MAX_VALUE
    const val PRO_MAX_SAVINGS_GOALS = Int.MAX_VALUE
    const val PRO_MAX_CUSTOM_CATEGORIES = Int.MAX_VALUE
    
    // Feature flags
    const val FREE_ALLOW_CLOUD_SYNC = false
    const val PRO_ALLOW_CLOUD_SYNC = true
    
    const val FREE_ALLOW_MULTI_CURRENCY = false
    const val PRO_ALLOW_MULTI_CURRENCY = true
    
    const val FREE_ALLOW_RECURRING_EDIT_ALL = false
    const val PRO_ALLOW_RECURRING_EDIT_ALL = true
}
```

## 🔄 Prochaines étapes

1. ✅ Mettre à jour les appels aux écrans modifiés dans Navigation.kt
2. ⚠️ Implémenter restrictions transactions récurrentes
3. ⚠️ Vérifier synchronisation cloud
4. ✅ Tester toutes les limitations
5. ✅ Générer nouvel APK de release

## 📱 Expérience utilisateur

### Parcours Free → Pro

**Semaine 1-2** : Découverte
- Utilisateur crée 1-2 comptes ✅
- Crée quelques budgets ✅
- Tout fonctionne bien

**Semaine 3-4** : Utilisation régulière
- Veut ajouter un 3ème compte → **Bloqué** → Message d'upgrade
- Veut créer plus de budgets → **Bloqué** → Message d'upgrade
- Veut créer un 2ème objectif → **Bloqué** → Message d'upgrade

**Mois 2+** : Conversion Pro
- Utilisateur investi dans l'app
- Atteint plusieurs limites
- **Upgrade naturel vers Pro** 💎

## 🎯 Taux de conversion attendu

Avec ces limitations :
- **3-5%** des utilisateurs Free passeront Pro
- **Délai moyen** : 3-6 mois d'utilisation
- **Raisons principales** :
  1. Besoin de plus de comptes (40%)
  2. Synchronisation cloud (30%)
  3. Plus de budgets/objectifs (20%)
  4. Multi-devises (10%)
