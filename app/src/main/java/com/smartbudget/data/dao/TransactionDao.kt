package com.smartbudget.data.dao

import androidx.room.*
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
    val recurringId: Long?,
    val categoryName: String?,
    val categoryIcon: String?,
    val categoryColor: Long?
)

@Dao
interface TransactionDao {
    @Query("""
        SELECT t.id, t.name, t.amount, t.type, t.categoryId, t.accountId, t.date, t.note, 
               t.isValidated, t.recurrence, t.recurringId, c.name as categoryName, c.icon as categoryIcon, c.color as categoryColor
        FROM transactions t 
        LEFT JOIN categories c ON t.categoryId = c.id 
        WHERE t.accountId = :accountId AND t.date >= :startDate AND t.date < :endDate AND t.isDeleted = 0
        ORDER BY t.date DESC
    """)
    fun getTransactionsForPeriod(accountId: Long, startDate: Long, endDate: Long): Flow<List<TransactionWithCategory>>

    @Query("""
        SELECT t.id, t.name, t.amount, t.type, t.categoryId, t.accountId, t.date, t.note, 
               t.isValidated, t.recurrence, t.recurringId, c.name as categoryName, c.icon as categoryIcon, c.color as categoryColor
        FROM transactions t 
        LEFT JOIN categories c ON t.categoryId = c.id 
        WHERE t.accountId = :accountId AND t.date >= :startDate AND t.date < :endDate AND t.isDeleted = 0
        ORDER BY t.date DESC
    """)
    fun getTransactionsForDay(accountId: Long, startDate: Long, endDate: Long): Flow<List<TransactionWithCategory>>

    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions 
        WHERE accountId = :accountId AND type = 'INCOME' AND date >= :startDate AND date < :endDate AND isDeleted = 0
    """)
    fun getTotalIncome(accountId: Long, startDate: Long, endDate: Long): Flow<Double>

    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions 
        WHERE accountId = :accountId AND type = 'EXPENSE' AND date >= :startDate AND date < :endDate AND isDeleted = 0
    """)
    fun getTotalExpenses(accountId: Long, startDate: Long, endDate: Long): Flow<Double>

    @Query("""
        SELECT COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amount ELSE -amount END), 0) 
        FROM transactions 
        WHERE accountId = :accountId AND date >= :startDate AND date < :endDate AND isDeleted = 0
    """)
    fun getDailyBalance(accountId: Long, startDate: Long, endDate: Long): Flow<Double>

    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions 
        WHERE accountId = :accountId AND type = 'EXPENSE' AND categoryId = :categoryId 
        AND date >= :startDate AND date < :endDate AND isDeleted = 0
    """)
    fun getExpensesByCategory(accountId: Long, categoryId: Long, startDate: Long, endDate: Long): Flow<Double>

    // All-accounts queries (consolidated view)
    @Query("""
        SELECT t.id, t.name, t.amount, t.type, t.categoryId, t.accountId, t.date, t.note, 
               t.isValidated, t.recurrence, t.recurringId, c.name as categoryName, c.icon as categoryIcon, c.color as categoryColor
        FROM transactions t 
        LEFT JOIN categories c ON t.categoryId = c.id 
        WHERE t.date >= :startDate AND t.date < :endDate AND t.isDeleted = 0
        ORDER BY t.date DESC
    """)
    fun getAllTransactionsForPeriod(startDate: Long, endDate: Long): Flow<List<TransactionWithCategory>>

    @Query("""
        SELECT t.id, t.name, t.amount, t.type, t.categoryId, t.accountId, t.date, t.note, 
               t.isValidated, t.recurrence, t.recurringId, c.name as categoryName, c.icon as categoryIcon, c.color as categoryColor
        FROM transactions t 
        LEFT JOIN categories c ON t.categoryId = c.id 
        WHERE t.date >= :startDate AND t.date < :endDate AND t.isDeleted = 0
        ORDER BY t.date DESC
    """)
    fun getAllTransactionsForDay(startDate: Long, endDate: Long): Flow<List<TransactionWithCategory>>

    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions 
        WHERE type = 'INCOME' AND date >= :startDate AND date < :endDate AND isDeleted = 0
    """)
    fun getAllTotalIncome(startDate: Long, endDate: Long): Flow<Double>

    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions 
        WHERE type = 'EXPENSE' AND date >= :startDate AND date < :endDate AND isDeleted = 0
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
        SELECT COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amount ELSE -amount END), 0) 
        FROM transactions WHERE accountId = :accountId AND isDeleted = 0
    """)
    suspend fun getTotalBalance(accountId: Long): Double

    @Query("""
        SELECT COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amount ELSE -amount END), 0) 
        FROM transactions WHERE isDeleted = 0
    """)
    suspend fun getTotalBalanceAllAccounts(): Double

    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions 
        WHERE accountId = :accountId AND type = 'INCOME' AND date >= :startDate AND date < :endDate AND isDeleted = 0
    """)
    suspend fun getTotalIncomeDirect(accountId: Long, startDate: Long, endDate: Long): Double

    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions 
        WHERE accountId = :accountId AND type = 'EXPENSE' AND date >= :startDate AND date < :endDate AND isDeleted = 0
    """)
    suspend fun getTotalExpensesDirect(accountId: Long, startDate: Long, endDate: Long): Double

    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions 
        WHERE accountId = :accountId AND type = 'EXPENSE' AND categoryId = :categoryId 
        AND date >= :startDate AND date < :endDate AND isDeleted = 0
    """)
    suspend fun getExpensesByCategoryDirect(accountId: Long, categoryId: Long, startDate: Long, endDate: Long): Double

    @Query("""
        SELECT t.id, t.name, t.amount, t.type, t.categoryId, t.accountId, t.date, t.note, 
               t.isValidated, t.recurrence, t.recurringId, c.name as categoryName, c.icon as categoryIcon, c.color as categoryColor
        FROM transactions t 
        LEFT JOIN categories c ON t.categoryId = c.id 
        WHERE t.isDeleted = 0 AND (
            t.name LIKE '%' || :query || '%' 
           OR t.note LIKE '%' || :query || '%' 
           OR c.name LIKE '%' || :query || '%'
           OR CAST(t.amount AS TEXT) LIKE '%' || :query || '%'
        )
        ORDER BY t.date DESC
        LIMIT 100
    """)
    fun searchTransactions(query: String): Flow<List<TransactionWithCategory>>

    @Insert
    suspend fun insertAll(transactions: List<Transaction>)

    @Query("SELECT * FROM transactions WHERE recurringId = :recurringId AND date = :date AND isDeleted = 0")
    suspend fun getOccurrenceByRecurringAndDate(recurringId: Long, date: Long): Transaction?

    @Query("SELECT MAX(date) FROM transactions WHERE recurringId = :recurringId AND isDeleted = 0")
    suspend fun getLastOccurrenceDateForRecurring(recurringId: Long): Long?

    @Query("SELECT * FROM transactions WHERE recurringId = :recurringId AND date >= :fromDate AND isDeleted = 0")
    suspend fun getFutureOccurrences(recurringId: Long, fromDate: Long): List<Transaction>

    @Query("""
        UPDATE transactions SET amount = :amount, name = :name, categoryId = :categoryId, note = :note, type = :type
        WHERE recurringId = :recurringId AND date >= :fromDate AND isDeleted = 0 AND isModified = 0
    """)
    suspend fun updateFutureUnmodifiedOccurrences(
        recurringId: Long, fromDate: Long, name: String, amount: Double,
        type: TransactionType, categoryId: Long?, note: String
    )

    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions 
        WHERE accountId = :accountId AND type = :type AND date >= :startDate AND date < :endDate AND isDeleted = 0
    """)
    suspend fun getTotalByType(accountId: Long, type: TransactionType, startDate: Long, endDate: Long): Double

    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions 
        WHERE type = :type AND date >= :startDate AND date < :endDate AND isDeleted = 0
    """)
    suspend fun getAllTotalByType(type: TransactionType, startDate: Long, endDate: Long): Double

    @Query("UPDATE transactions SET isDeleted = 1 WHERE id = :id")
    suspend fun softDelete(id: Long)

    @Query("UPDATE transactions SET isDeleted = 1 WHERE recurringId = :recurringId AND date >= :fromDate")
    suspend fun softDeleteFutureOccurrences(recurringId: Long, fromDate: Long)

    @Query("UPDATE transactions SET isDeleted = 1 WHERE recurringId = :recurringId")
    suspend fun softDeleteAllOccurrences(recurringId: Long)
}
