package com.smartbudget.data.dao

import androidx.room.*
import com.smartbudget.data.entity.Recurrence
import com.smartbudget.data.entity.Transaction
import com.smartbudget.data.entity.TransactionType
import kotlinx.coroutines.flow.Flow

data class TransactionWithCategory(
    val id: Long,
    val name: String,
    val amount: Double,
    val type: TransactionType,
    val categoryId: Long?,
    val accountId: Long,
    val date: Long,
    val note: String,
    val isValidated: Boolean,
    val recurrence: String,
    val categoryName: String?,
    val categoryIcon: String?,
    val categoryColor: Long?
)

@Dao
interface TransactionDao {
    @Query("""
        SELECT t.id, t.name, t.amount, t.type, t.categoryId, t.accountId, t.date, t.note, 
               t.isValidated, t.recurrence, c.name as categoryName, c.icon as categoryIcon, c.color as categoryColor
        FROM transactions t 
        LEFT JOIN categories c ON t.categoryId = c.id 
        WHERE t.accountId = :accountId AND t.date >= :startDate AND t.date < :endDate
        ORDER BY t.date DESC
    """)
    fun getTransactionsForPeriod(accountId: Long, startDate: Long, endDate: Long): Flow<List<TransactionWithCategory>>

    @Query("""
        SELECT t.id, t.name, t.amount, t.type, t.categoryId, t.accountId, t.date, t.note, 
               t.isValidated, t.recurrence, c.name as categoryName, c.icon as categoryIcon, c.color as categoryColor
        FROM transactions t 
        LEFT JOIN categories c ON t.categoryId = c.id 
        WHERE t.accountId = :accountId AND t.date >= :startDate AND t.date < :endDate
        ORDER BY t.date DESC
    """)
    fun getTransactionsForDay(accountId: Long, startDate: Long, endDate: Long): Flow<List<TransactionWithCategory>>

    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions 
        WHERE accountId = :accountId AND type = 'INCOME' AND date >= :startDate AND date < :endDate
    """)
    fun getTotalIncome(accountId: Long, startDate: Long, endDate: Long): Flow<Double>

    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions 
        WHERE accountId = :accountId AND type = 'EXPENSE' AND date >= :startDate AND date < :endDate
    """)
    fun getTotalExpenses(accountId: Long, startDate: Long, endDate: Long): Flow<Double>

    @Query("""
        SELECT COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amount ELSE -amount END), 0) 
        FROM transactions 
        WHERE accountId = :accountId AND date >= :startDate AND date < :endDate
    """)
    fun getDailyBalance(accountId: Long, startDate: Long, endDate: Long): Flow<Double>

    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions 
        WHERE accountId = :accountId AND type = 'EXPENSE' AND categoryId = :categoryId 
        AND date >= :startDate AND date < :endDate
    """)
    fun getExpensesByCategory(accountId: Long, categoryId: Long, startDate: Long, endDate: Long): Flow<Double>

    // All-accounts queries (consolidated view)
    @Query("""
        SELECT t.id, t.name, t.amount, t.type, t.categoryId, t.accountId, t.date, t.note, 
               t.isValidated, t.recurrence, c.name as categoryName, c.icon as categoryIcon, c.color as categoryColor
        FROM transactions t 
        LEFT JOIN categories c ON t.categoryId = c.id 
        WHERE t.date >= :startDate AND t.date < :endDate
        ORDER BY t.date DESC
    """)
    fun getAllTransactionsForPeriod(startDate: Long, endDate: Long): Flow<List<TransactionWithCategory>>

    @Query("""
        SELECT t.id, t.name, t.amount, t.type, t.categoryId, t.accountId, t.date, t.note, 
               t.isValidated, t.recurrence, c.name as categoryName, c.icon as categoryIcon, c.color as categoryColor
        FROM transactions t 
        LEFT JOIN categories c ON t.categoryId = c.id 
        WHERE t.date >= :startDate AND t.date < :endDate
        ORDER BY t.date DESC
    """)
    fun getAllTransactionsForDay(startDate: Long, endDate: Long): Flow<List<TransactionWithCategory>>

    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions 
        WHERE type = 'INCOME' AND date >= :startDate AND date < :endDate
    """)
    fun getAllTotalIncome(startDate: Long, endDate: Long): Flow<Double>

    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions 
        WHERE type = 'EXPENSE' AND date >= :startDate AND date < :endDate
    """)
    fun getAllTotalExpenses(startDate: Long, endDate: Long): Flow<Double>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: Transaction): Long

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): Transaction?

    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions 
        WHERE accountId = :accountId AND type = :type AND date >= :startDate AND date < :endDate
    """)
    suspend fun getTotalByType(accountId: Long, type: TransactionType, startDate: Long, endDate: Long): Double

    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions 
        WHERE type = :type AND date >= :startDate AND date < :endDate
    """)
    suspend fun getAllTotalByType(type: TransactionType, startDate: Long, endDate: Long): Double

    @Query("""
        SELECT COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amount ELSE -amount END), 0) 
        FROM transactions WHERE accountId = :accountId
    """)
    suspend fun getTotalBalance(accountId: Long): Double

    @Query("""
        SELECT COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amount ELSE -amount END), 0) 
        FROM transactions
    """)
    suspend fun getTotalBalanceAllAccounts(): Double

    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions 
        WHERE accountId = :accountId AND type = 'INCOME' AND date >= :startDate AND date < :endDate
    """)
    suspend fun getTotalIncomeDirect(accountId: Long, startDate: Long, endDate: Long): Double

    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions 
        WHERE accountId = :accountId AND type = 'EXPENSE' AND date >= :startDate AND date < :endDate
    """)
    suspend fun getTotalExpensesDirect(accountId: Long, startDate: Long, endDate: Long): Double

    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions 
        WHERE accountId = :accountId AND type = 'EXPENSE' AND categoryId = :categoryId 
        AND date >= :startDate AND date < :endDate
    """)
    suspend fun getExpensesByCategoryDirect(accountId: Long, categoryId: Long, startDate: Long, endDate: Long): Double

    @Query("""
        SELECT t.id, t.name, t.amount, t.type, t.categoryId, t.accountId, t.date, t.note, 
               t.isValidated, t.recurrence, c.name as categoryName, c.icon as categoryIcon, c.color as categoryColor
        FROM transactions t 
        LEFT JOIN categories c ON t.categoryId = c.id 
        WHERE t.name LIKE '%' || :query || '%' 
           OR t.note LIKE '%' || :query || '%' 
           OR c.name LIKE '%' || :query || '%'
           OR CAST(t.amount AS TEXT) LIKE '%' || :query || '%'
        ORDER BY t.date DESC
        LIMIT 100
    """)
    fun searchTransactions(query: String): Flow<List<TransactionWithCategory>>

    @Query("SELECT * FROM transactions WHERE recurrence != 'NONE'")
    suspend fun getRecurringTransactions(): List<Transaction>

    @Query("SELECT COUNT(*) FROM transactions WHERE recurrenceGroupId = :groupId AND date = :date")
    suspend fun countTransactionsForGroupAtDate(groupId: Long, date: Long): Int

    @Query("""
        SELECT MAX(date) FROM transactions 
        WHERE name = :name AND accountId = :accountId AND type = :type 
        AND categoryId = :categoryId AND amount = :amount
    """)
    suspend fun getLastOccurrenceDate(
        name: String, accountId: Long, type: TransactionType, 
        categoryId: Long?, amount: Double
    ): Long?

    @Query("SELECT MAX(date) FROM transactions WHERE recurrenceGroupId = :groupId")
    suspend fun getLastOccurrenceDateByGroupId(groupId: Long): Long?

    @Query("""
        SELECT * FROM transactions 
        WHERE recurrenceGroupId = :groupId AND date >= :fromDate
        ORDER BY date ASC
    """)
    suspend fun getFutureRecurringTransactions(groupId: Long, fromDate: Long): List<Transaction>

    @Query("""
        UPDATE transactions 
        SET name = :name, amount = :amount, type = :type, categoryId = :categoryId, 
            note = :note, recurrence = :recurrence
        WHERE recurrenceGroupId = :groupId AND date >= :fromDate
    """)
    suspend fun updateFutureRecurringTransactions(
        groupId: Long, fromDate: Long, name: String, amount: Double,
        type: TransactionType, categoryId: Long?, note: String, recurrence: Recurrence
    )
}
