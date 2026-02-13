package com.smartbudget

import android.app.Application
import com.smartbudget.data.SmartBudgetDatabase
import com.smartbudget.data.entity.Account
import com.smartbudget.data.entity.Category
import com.smartbudget.data.entity.TransactionType
import com.smartbudget.data.repository.AccountRepository
import com.smartbudget.data.repository.BudgetRepository
import com.smartbudget.data.repository.CategoryRepository
import com.smartbudget.data.repository.TransactionRepository
import com.smartbudget.billing.BillingManager
import com.smartbudget.ui.util.CurrencyFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmartBudgetApp : Application() {
    val database by lazy { SmartBudgetDatabase.getDatabase(this) }
    val accountRepository by lazy { AccountRepository(database.accountDao()) }
    val categoryRepository by lazy { CategoryRepository(database.categoryDao()) }
    val transactionRepository by lazy { TransactionRepository(database.transactionDao()) }
    val budgetRepository by lazy { BudgetRepository(database.budgetDao()) }
    val billingManager by lazy { BillingManager(this) }

    override fun onCreate() {
        super.onCreate()
        CurrencyFormatter.init(this)
        billingManager.initialize()
        seedDefaultData()
    }

    private fun seedDefaultData() {
        CoroutineScope(Dispatchers.IO).launch {
            // Seed default account
            if (accountRepository.getDefaultAccount() == null) {
                accountRepository.insert(Account(name = "Compte principal", isDefault = true))
            }

            // Seed default categories
            if (categoryRepository.count() == 0) {
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
