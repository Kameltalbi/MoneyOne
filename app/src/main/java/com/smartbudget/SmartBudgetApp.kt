package com.smartbudget

import android.app.Application
import com.smartbudget.data.SmartBudgetDatabase
import com.smartbudget.data.RecurringGenerator
import com.smartbudget.data.entity.Account
import com.smartbudget.data.entity.Category
import com.smartbudget.data.entity.TransactionType
import com.smartbudget.data.repository.AccountRepository
import com.smartbudget.data.repository.BudgetRepository
import com.smartbudget.data.repository.CategoryRepository
import com.smartbudget.data.repository.SavingsGoalRepository
import com.smartbudget.data.repository.RecurringRepository
import com.smartbudget.data.repository.TransactionRepository
import com.smartbudget.billing.BillingManager
import com.smartbudget.notification.BudgetAlertManager
// import com.smartbudget.firebase.FirebaseAuthManager // Removed - Firebase disabled
// import com.smartbudget.firebase.FirebaseSyncManager // Removed - Firebase disabled
import com.smartbudget.security.SecurityManager
import com.smartbudget.currency.ExchangeRateService
import com.smartbudget.sms.SmsImportService
import com.smartbudget.ui.util.CurrencyFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
// import com.google.firebase.FirebaseApp // Removed - Firebase disabled

class SmartBudgetApp : Application() {
    var database = null as SmartBudgetDatabase?
        private set

    fun getDb(): SmartBudgetDatabase {
        if (database == null || !database!!.isOpen) {
            database = SmartBudgetDatabase.getDatabase(this)
        }
        return database!!
    }

    fun reopenDatabase() {
        database = SmartBudgetDatabase.getDatabase(this)
    }

    val accountRepository by lazy { AccountRepository(getDb().accountDao()) }
    val categoryRepository by lazy { CategoryRepository(getDb().categoryDao()) }
    val transactionRepository by lazy { TransactionRepository(getDb().transactionDao()) }
    val budgetRepository by lazy { BudgetRepository(getDb().budgetDao()) }
    val savingsGoalRepository by lazy { SavingsGoalRepository(getDb().savingsGoalDao()) }
    val recurringRepository by lazy { RecurringRepository(getDb().recurringDao()) }
    val recurringGenerator by lazy { RecurringGenerator(transactionRepository) }
    val billingManager by lazy { BillingManager(this) }
    val budgetAlertManager by lazy { BudgetAlertManager(this, budgetRepository, transactionRepository) }
    val securityManager by lazy { SecurityManager(this) }
    val exchangeRateService by lazy { ExchangeRateService(this) }
    val smsImportService by lazy { SmsImportService(this) }
    // Firebase disabled - removed firebaseAuthManager and firebaseSyncManager
    // val firebaseAuthManager by lazy { FirebaseAuthManager(this) }
    // val firebaseSyncManager by lazy { 
    //     FirebaseSyncManager(
    //         this,
    //         firebaseAuthManager,
    //         accountRepository,
    //         categoryRepository,
    //         transactionRepository,
    //         budgetRepository,
    //         savingsGoalRepository,
    //         recurringRepository
    //     )
    // }

    override fun onCreate() {
        super.onCreate()
        
        // Firebase disabled for production - causes crashes
        // Initialize Firebase manually BEFORE any Firebase usage
        // try {
        //     FirebaseApp.initializeApp(this)
        //     android.util.Log.d("SmartBudgetApp", "Firebase initialized successfully")
        // } catch (e: Exception) {
        //     android.util.Log.e("SmartBudgetApp", "Firebase initialization failed", e)
        // }
        
        CurrencyFormatter.init(this)
        billingManager.initialize()
        budgetAlertManager.createNotificationChannel()
        // initializeFirebase() // Disabled - causes crash
        seedDefaultData()
    }
    
    // Firebase disabled - initializeFirebase removed
    // private fun initializeFirebase() {
    //     CoroutineScope(Dispatchers.IO).launch {
    //         try {
    //             val firebaseApps = FirebaseApp.getApps(this@SmartBudgetApp)
    //             if (firebaseApps.isEmpty()) {
    //                 android.util.Log.w("SmartBudgetApp", "Firebase not available, skipping Firebase initialization")
    //                 return@launch
    //             }
    //             if (!firebaseAuthManager.isSignedIn()) {
    //                 val result = firebaseAuthManager.signInAnonymously()
    //                 if (result.isSuccess) {
    //                     android.util.Log.d("SmartBudgetApp", "Firebase anonymous sign-in successful")
    //                 } else {
    //                     android.util.Log.e("SmartBudgetApp", "Firebase anonymous sign-in failed", result.exceptionOrNull())
    //                 }
    //             }
    //         } catch (e: Exception) {
    //             android.util.Log.e("SmartBudgetApp", "Error in initializeFirebase", e)
    //         }
    //     }
    // }

    private fun seedDefaultData() {
        CoroutineScope(Dispatchers.IO).launch {
            // Default account is now created during onboarding

            // Seed default categories - always check and recreate if missing
            val userManager = com.smartbudget.data.UserManager(this@SmartBudgetApp)
            val userId = userManager.getCurrentUserId()
            
            // Check if default categories exist, if not recreate them
            val categoryCount = categoryRepository.count(userId)
            if (categoryCount == 0) {
                val defaults = listOf(
                    Category(name = "Salaire", icon = "payments", color = 0xFF4CAF50, type = TransactionType.INCOME, isDefault = true, userId = userId),
                    Category(name = "Freelance", icon = "work", color = 0xFF66BB6A, type = TransactionType.INCOME, isDefault = true, userId = userId),
                    Category(name = "Alimentation", icon = "restaurant", color = 0xFFFF9800, type = TransactionType.EXPENSE, isDefault = true, userId = userId),
                    Category(name = "Transport", icon = "directions_car", color = 0xFF2196F3, type = TransactionType.EXPENSE, isDefault = true, userId = userId),
                    Category(name = "Logement", icon = "home", color = 0xFF9C27B0, type = TransactionType.EXPENSE, isDefault = true, userId = userId),
                    Category(name = "Shopping", icon = "shopping_bag", color = 0xFFE91E63, type = TransactionType.EXPENSE, isDefault = true, userId = userId),
                    Category(name = "Santé", icon = "local_hospital", color = 0xFFF44336, type = TransactionType.EXPENSE, isDefault = true, userId = userId),
                    Category(name = "Loisirs", icon = "sports_esports", color = 0xFF00BCD4, type = TransactionType.EXPENSE, isDefault = true, userId = userId),
                )
                categoryRepository.insertAll(defaults)
                android.util.Log.d("SmartBudgetApp", "Created ${defaults.size} default categories for user $userId")
            }
        }
    }
}
