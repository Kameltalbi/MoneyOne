package com.smartbudget

import android.app.Application
import com.smartbudget.data.SmartBudgetDatabase
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
import com.smartbudget.ui.util.CurrencyFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
    val billingManager by lazy { BillingManager(this) }
    val budgetAlertManager by lazy { BudgetAlertManager(this, budgetRepository, transactionRepository) }

    override fun onCreate() {
        super.onCreate()
        CurrencyFormatter.init(this)
        billingManager.initialize()
        budgetAlertManager.createNotificationChannel()
        seedDefaultData()
    }

    private fun seedDefaultData() {
        CoroutineScope(Dispatchers.IO).launch {
            // Default account is now created during onboarding

            // Seed default categories
            val userManager = com.smartbudget.data.UserManager(this@SmartBudgetApp)
            val userId = userManager.getCurrentUserId()
            if (categoryRepository.count(userId) == 0) {
                val defaults = listOf(
                    Category(name = "Salaire", icon = "payments", color = 0xFF4CAF50, type = TransactionType.INCOME, isDefault = true),
                    Category(name = "Freelance", icon = "work", color = 0xFF66BB6A, type = TransactionType.INCOME, isDefault = true),
                    Category(name = "Alimentation", icon = "restaurant", color = 0xFFFF9800, type = TransactionType.EXPENSE, isDefault = true),
                    Category(name = "Transport", icon = "directions_car", color = 0xFF2196F3, type = TransactionType.EXPENSE, isDefault = true),
                    Category(name = "Logement", icon = "home", color = 0xFF9C27B0, type = TransactionType.EXPENSE, isDefault = true),
                    Category(name = "Shopping", icon = "shopping_bag", color = 0xFFE91E63, type = TransactionType.EXPENSE, isDefault = true),
                    Category(name = "Santé", icon = "local_hospital", color = 0xFFF44336, type = TransactionType.EXPENSE, isDefault = true),
                    Category(name = "Loisirs", icon = "sports_esports", color = 0xFF00BCD4, type = TransactionType.EXPENSE, isDefault = true),
                )
                categoryRepository.insertAll(defaults)
            }
        }
    }
}
