package com.smartbudget.firebase

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.smartbudget.data.entity.*
import com.smartbudget.data.repository.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FirebaseSyncManager(
    private val context: Context,
    private val authManager: FirebaseAuthManager,
    private val accountRepo: AccountRepository,
    private val categoryRepo: CategoryRepository,
    private val transactionRepo: TransactionRepository,
    private val budgetRepo: BudgetRepository,
    private val savingsGoalRepo: SavingsGoalRepository,
    private val recurringRepo: RecurringRepository
) {
    private val firestore = FirebaseFirestore.getInstance()
    private val prefs = context.getSharedPreferences("firebase_sync", Context.MODE_PRIVATE)
    private val scope = CoroutineScope(Dispatchers.IO)
    
    companion object {
        private const val TAG = "FirebaseSyncManager"
        private const val COLLECTION_ACCOUNTS = "accounts"
        private const val COLLECTION_CATEGORIES = "categories"
        private const val COLLECTION_TRANSACTIONS = "transactions"
        private const val COLLECTION_BUDGETS = "budgets"
        private const val COLLECTION_SAVINGS = "savings_goals"
        private const val COLLECTION_RECURRING = "recurring_transactions"
        private const val KEY_LAST_SYNC = "last_sync_timestamp"
        private const val KEY_AUTO_SYNC = "auto_sync_enabled"
    }
    
    fun isAutoSyncEnabled(): Boolean = prefs.getBoolean(KEY_AUTO_SYNC, true)
    
    fun setAutoSyncEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_SYNC, enabled).apply()
    }
    
    private fun getUserCollection(collection: String): String {
        val userId = authManager.getUserId() ?: return ""
        return "users/$userId/$collection"
    }
    
    // Sync all data to cloud
    suspend fun syncToCloud(userId: String): Result<Unit> {
        if (!authManager.isSignedIn()) {
            return Result.failure(Exception("User not signed in"))
        }
        
        return try {
            Log.d(TAG, "Starting sync to cloud for user: $userId")
            
            // Sync accounts
            val accounts = accountRepo.getAllAccounts(userId).firstOrNull() ?: emptyList()
            for (account in accounts) {
                firestore.collection(getUserCollection(COLLECTION_ACCOUNTS))
                    .document(account.id.toString())
                    .set(mapOf(
                        "id" to account.id,
                        "name" to account.name,
                        "currency" to account.currency,
                        "isDefault" to account.isDefault,
                        "userId" to account.userId
                    ), SetOptions.merge())
                    .await()
            }
            
            // Sync categories
            val categories = categoryRepo.getAllCategories(userId).firstOrNull() ?: emptyList()
            for (category in categories) {
                firestore.collection(getUserCollection(COLLECTION_CATEGORIES))
                    .document(category.id.toString())
                    .set(mapOf(
                        "id" to category.id,
                        "name" to category.name,
                        "icon" to category.icon,
                        "color" to category.color,
                        "type" to category.type.name,
                        "isDefault" to category.isDefault,
                        "userId" to category.userId
                    ), SetOptions.merge())
                    .await()
            }
            
            // Sync transactions - get all for current year
            val startOfYear = java.time.LocalDate.now().withDayOfYear(1)
                .atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endOfYear = java.time.LocalDate.now().plusYears(1).withDayOfYear(1)
                .atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            val transactions = transactionRepo.getTransactionsByDateRangeDirect(userId, startOfYear, endOfYear)
            for (transaction in transactions) {
                firestore.collection(getUserCollection(COLLECTION_TRANSACTIONS))
                    .document(transaction.id.toString())
                    .set(mapOf(
                        "id" to transaction.id,
                        "name" to transaction.name,
                        "amount" to transaction.amount,
                        "type" to transaction.type.name,
                        "categoryId" to transaction.categoryId,
                        "accountId" to transaction.accountId,
                        "date" to transaction.date,
                        "note" to transaction.note,
                        "userId" to transaction.userId,
                        "recurringId" to transaction.recurringId,
                        "isModified" to transaction.isModified
                    ), SetOptions.merge())
                    .await()
            }
            
            // Sync budgets - current year/month
            val currentYearMonth = java.time.YearMonth.now().toString()
            val budgets = budgetRepo.getAllBudgetsForMonthDirect(userId, currentYearMonth)
            for (budget in budgets) {
                firestore.collection(getUserCollection(COLLECTION_BUDGETS))
                    .document(budget.id.toString())
                    .set(mapOf(
                        "id" to budget.id,
                        "categoryId" to budget.categoryId,
                        "amount" to budget.amount,
                        "yearMonth" to budget.yearMonth,
                        "isGlobal" to budget.isGlobal,
                        "userId" to budget.userId
                    ), SetOptions.merge())
                    .await()
            }
            
            // Sync savings goals
            val savingsGoals = savingsGoalRepo.getAllGoals(userId).firstOrNull() ?: emptyList()
            for (goal in savingsGoals) {
                firestore.collection(getUserCollection(COLLECTION_SAVINGS))
                    .document(goal.id.toString())
                    .set(mapOf(
                        "id" to goal.id,
                        "name" to goal.name,
                        "targetAmount" to goal.targetAmount,
                        "currentAmount" to goal.currentAmount,
                        "icon" to goal.icon,
                        "color" to goal.color,
                        "createdDate" to goal.createdDate,
                        "targetDate" to goal.targetDate,
                        "userId" to goal.userId
                    ), SetOptions.merge())
                    .await()
            }
            
            // Sync recurring transactions
            val recurring = recurringRepo.getActiveRecurring(userId)
            for (rec in recurring) {
                firestore.collection(getUserCollection(COLLECTION_RECURRING))
                    .document(rec.id.toString())
                    .set(mapOf(
                        "id" to rec.id,
                        "name" to rec.name,
                        "amount" to rec.amount,
                        "type" to rec.type.name,
                        "categoryId" to rec.categoryId,
                        "accountId" to rec.accountId,
                        "note" to rec.note,
                        "startDate" to rec.startDate,
                        "endDate" to rec.endDate,
                        "frequency" to rec.frequency.name,
                        "interval" to rec.interval,
                        "userId" to rec.userId
                    ), SetOptions.merge())
                    .await()
            }
            
            prefs.edit().putLong(KEY_LAST_SYNC, System.currentTimeMillis()).apply()
            Log.d(TAG, "Sync to cloud completed successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Sync to cloud failed", e)
            Result.failure(e)
        }
    }
    
    // Restore data from cloud
    suspend fun restoreFromCloud(userId: String): Result<Unit> {
        if (!authManager.isSignedIn()) {
            return Result.failure(Exception("User not signed in"))
        }
        
        return try {
            Log.d(TAG, "Starting restore from cloud for user: $userId")
            
            // Restore accounts
            val accountsSnapshot = firestore.collection(getUserCollection(COLLECTION_ACCOUNTS)).get().await()
            for (doc in accountsSnapshot.documents) {
                val account = Account(
                    id = doc.getLong("id") ?: 0,
                    name = doc.getString("name") ?: "",
                    currency = doc.getString("currency") ?: "EUR",
                    isDefault = doc.getBoolean("isDefault") ?: false,
                    userId = doc.getString("userId") ?: userId
                )
                accountRepo.insert(account)
            }
            
            // Restore categories
            val categoriesSnapshot = firestore.collection(getUserCollection(COLLECTION_CATEGORIES)).get().await()
            for (doc in categoriesSnapshot.documents) {
                val category = Category(
                    id = doc.getLong("id") ?: 0,
                    name = doc.getString("name") ?: "",
                    icon = doc.getString("icon") ?: "",
                    color = doc.getLong("color") ?: 0,
                    type = TransactionType.valueOf(doc.getString("type") ?: "EXPENSE"),
                    isDefault = doc.getBoolean("isDefault") ?: false,
                    userId = doc.getString("userId") ?: userId
                )
                categoryRepo.insert(category)
            }
            
            // Restore transactions
            val transactionsSnapshot = firestore.collection(getUserCollection(COLLECTION_TRANSACTIONS)).get().await()
            for (doc in transactionsSnapshot.documents) {
                val transaction = Transaction(
                    id = doc.getLong("id") ?: 0,
                    name = doc.getString("name") ?: "",
                    amount = doc.getDouble("amount") ?: 0.0,
                    type = TransactionType.valueOf(doc.getString("type") ?: "EXPENSE"),
                    categoryId = doc.getLong("categoryId"),
                    accountId = doc.getLong("accountId") ?: 0,
                    date = doc.getLong("date") ?: 0,
                    note = doc.getString("note") ?: "",
                    userId = doc.getString("userId") ?: userId,
                    recurringId = doc.getLong("recurringId"),
                    isModified = doc.getBoolean("isModified") ?: false
                )
                transactionRepo.insert(transaction)
            }
            
            // Restore budgets
            val budgetsSnapshot = firestore.collection(getUserCollection(COLLECTION_BUDGETS)).get().await()
            for (doc in budgetsSnapshot.documents) {
                val budget = Budget(
                    id = doc.getLong("id") ?: 0,
                    categoryId = doc.getLong("categoryId"),
                    amount = doc.getDouble("amount") ?: 0.0,
                    yearMonth = doc.getString("yearMonth") ?: "",
                    isGlobal = doc.getBoolean("isGlobal") ?: true,
                    userId = doc.getString("userId") ?: userId
                )
                budgetRepo.insert(budget)
            }
            
            // Restore savings goals
            val savingsSnapshot = firestore.collection(getUserCollection(COLLECTION_SAVINGS)).get().await()
            for (doc in savingsSnapshot.documents) {
                val goal = SavingsGoal(
                    id = doc.getLong("id") ?: 0,
                    name = doc.getString("name") ?: "",
                    targetAmount = doc.getDouble("targetAmount") ?: 0.0,
                    currentAmount = doc.getDouble("currentAmount") ?: 0.0,
                    icon = doc.getString("icon") ?: "savings",
                    color = doc.getLong("color") ?: 0xFF4CAF50,
                    createdDate = doc.getLong("createdDate") ?: System.currentTimeMillis(),
                    targetDate = doc.getLong("targetDate"),
                    userId = doc.getString("userId") ?: userId
                )
                savingsGoalRepo.insert(goal)
            }
            
            // Restore recurring transactions
            val recurringSnapshot = firestore.collection(getUserCollection(COLLECTION_RECURRING)).get().await()
            for (doc in recurringSnapshot.documents) {
                val recurring = RecurringTransaction(
                    id = doc.getLong("id") ?: 0,
                    name = doc.getString("name") ?: "",
                    amount = doc.getDouble("amount") ?: 0.0,
                    type = TransactionType.valueOf(doc.getString("type") ?: "EXPENSE"),
                    categoryId = doc.getLong("categoryId"),
                    accountId = doc.getLong("accountId") ?: 0,
                    note = doc.getString("note") ?: "",
                    startDate = doc.getLong("startDate") ?: 0,
                    endDate = doc.getLong("endDate"),
                    frequency = Frequency.valueOf(doc.getString("frequency") ?: "MONTHLY"),
                    interval = (doc.getLong("interval") ?: 1).toInt(),
                    userId = doc.getString("userId") ?: userId
                )
                recurringRepo.insert(recurring)
            }
            
            prefs.edit().putLong(KEY_LAST_SYNC, System.currentTimeMillis()).apply()
            Log.d(TAG, "Restore from cloud completed successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Restore from cloud failed", e)
            Result.failure(e)
        }
    }
    
    fun getLastSyncTime(): Long = prefs.getLong(KEY_LAST_SYNC, 0)
    
    // Auto-sync in background
    fun enableAutoSync(userId: String) {
        if (!isAutoSyncEnabled()) return
        
        scope.launch {
            syncToCloud(userId)
        }
    }
}
