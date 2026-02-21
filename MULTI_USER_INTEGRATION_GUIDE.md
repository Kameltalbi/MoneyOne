# Guide d'intégration Multi-Utilisateurs

## Problème résolu
Les données créées dans le compte 2 étaient enregistrées dans le compte 1 car l'application n'avait pas de système de gestion multi-utilisateurs. Toutes les données étaient stockées sans distinction d'utilisateur.

## Solution implémentée

### 1. Modifications de la base de données (v8 → v9)
- Ajout du champ `userId: String` à toutes les entités :
  - `Account`
  - `Transaction`
  - `Category`
  - `Budget`
  - `SavingsGoal`
  - `RecurringTransaction`
- Migration automatique : toutes les données existantes reçoivent `userId = "default_user"`
- Indices créés sur `userId` pour optimiser les performances

### 2. UserManager
Classe créée dans `/app/src/main/java/com/smartbudget/data/UserManager.kt`
- Stocke l'utilisateur actuel dans SharedPreferences
- Méthodes :
  - `getCurrentUserId()`: Récupère l'ID de l'utilisateur actuel
  - `setCurrentUserId(userId)`: Définit l'utilisateur actuel
  - `hasUser()`: Vérifie si un utilisateur est défini
  - `clearCurrentUser()`: Efface l'utilisateur actuel

### 3. Modifications des DAOs
Tous les DAOs ont été mis à jour pour filtrer par `userId` :
- `AccountDao`, `CategoryDao`, `BudgetDao`, `SavingsGoalDao`, `RecurringDao`, `TransactionDao`
- Toutes les requêtes `@Query` incluent maintenant `WHERE userId = :userId`

### 4. Modifications des Repositories
Tous les repositories passent maintenant `userId` aux DAOs :
- `AccountRepository`, `CategoryRepository`, `BudgetRepository`
- `SavingsGoalRepository`, `RecurringRepository`, `TransactionRepository`

## Intégration dans les ViewModels

### Étape 1: Injecter UserManager
Dans chaque ViewModel, ajoutez UserManager :

```kotlin
class MainViewModel(
    application: Application,
    private val userManager: UserManager  // AJOUTER CECI
) : AndroidViewModel(application) {
    
    private val userId: String
        get() = userManager.getCurrentUserId()
    
    // Reste du code...
}
```

### Étape 2: Passer userId aux repositories
Modifiez tous les appels aux repositories pour inclure `userId` :

**AVANT:**
```kotlin
val allAccounts = accountRepository.allAccounts
val transactions = transactionRepository.getTransactionsForPeriod(accountId, start, end)
```

**APRÈS:**
```kotlin
val allAccounts = accountRepository.getAllAccounts(userId)
val transactions = transactionRepository.getTransactionsForPeriod(userId, accountId, start, end)
```

### Étape 3: Définir userId lors de la création d'entités
Lors de la création de nouvelles entités, définissez le `userId` :

```kotlin
val newAccount = Account(
    name = accountName,
    currency = currency,
    userId = userId  // AJOUTER CECI
)

val newTransaction = Transaction(
    name = name,
    amount = amount,
    type = type,
    accountId = accountId,
    date = date,
    userId = userId  // AJOUTER CECI
)
```

## Intégration avec Google Sign-In

### Dans SettingsScreen.kt
Lorsque l'utilisateur se connecte avec Google, définissez le userId :

```kotlin
val signInLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult()
) { result ->
    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
    try {
        val account = task.getResult(ApiException::class.java)
        val email = account.email ?: return@rememberLauncherForActivityResult
        
        // AJOUTER CECI
        val userManager = UserManager(context)
        userManager.setCurrentUserId(email)
        
        // Recharger les données pour le nouvel utilisateur
        // viewModel.reloadData()
        
        isSignedIn.value = true
        accountEmail.value = email
    } catch (e: Exception) {
        backupMessage = context.getString(R.string.backup_sign_in_failed)
    }
}
```

### Changement d'utilisateur
Pour permettre le changement d'utilisateur :

```kotlin
fun switchUser(newUserEmail: String) {
    userManager.setCurrentUserId(newUserEmail)
    // Recharger toutes les données
    // Naviguer vers l'écran principal
}
```

## ViewModels à mettre à jour

Liste des ViewModels qui nécessitent des modifications :

1. **MainViewModel** - Gestion des comptes et transactions
2. **TransactionViewModel** - CRUD des transactions
3. **SettingsViewModel** - Budgets, catégories, objectifs d'épargne
4. **CategoryViewModel** (si existe) - Gestion des catégories
5. **BudgetViewModel** (si existe) - Gestion des budgets
6. **SavingsGoalViewModel** (si existe) - Gestion des objectifs

## Factory Pattern pour ViewModels

Créez une factory pour injecter UserManager :

```kotlin
class ViewModelFactory(
    private val application: Application,
    private val userManager: UserManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                MainViewModel(application, userManager) as T
            }
            modelClass.isAssignableFrom(TransactionViewModel::class.java) -> {
                TransactionViewModel(application, userManager) as T
            }
            // Ajouter d'autres ViewModels...
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
```

## Utilisation dans Composables

```kotlin
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val userManager = remember { UserManager(context) }
    val viewModel: MainViewModel = viewModel(
        factory = ViewModelFactory(
            context.applicationContext as Application,
            userManager
        )
    )
    
    // Reste du code...
}
```

## Points importants

1. **Données existantes** : Toutes les données existantes ont `userId = "default_user"`
2. **Compatibilité** : La migration est automatique au premier lancement
3. **Performance** : Les indices sur `userId` assurent des requêtes rapides
4. **Isolation** : Chaque utilisateur voit uniquement ses propres données
5. **Google Sign-In** : Utilisez l'email comme userId pour la cohérence

## Test de la solution

1. Créer un compte avec email1@example.com
2. Ajouter des transactions/comptes
3. Se déconnecter et se connecter avec email2@example.com
4. Vérifier que les données de email1 ne sont pas visibles
5. Créer des données pour email2
6. Se reconnecter avec email1 et vérifier que ses données sont toujours là

## Prochaines étapes

1. Mettre à jour tous les ViewModels pour utiliser UserManager
2. Ajouter une UI de sélection/changement d'utilisateur
3. Tester la migration de base de données
4. Tester le changement d'utilisateur
5. Vérifier l'isolation des données entre utilisateurs
