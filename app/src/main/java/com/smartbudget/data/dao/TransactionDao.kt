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
        WHERE t.userId = :userId AND t.accountId = :accountId AND t.date >= :startDate AND t.date < :endDate AND t.isDeleted = 0
        ORDER BY t.date DESC
    """)
    fun getTransactionsForPeriod(userId: String, accountId: Long, startDate: Long, endDate: Long): Flow<List<TransactionWithCategory>>

    @Query("""
        SELECT t.id, t.name, t.amount, t.type, t.categoryId, t.accountId, t.date, t.note, 
               t.isValidated, t.recurrence, t.recurringId, c.name as categoryName, c.icon as categoryIcon, c.color as categoryColor
        FROM transactions t 
        LEFT JOIN categories c ON t.categoryId = c.id 
        WHERE t.userId = :userId AND t.accountId = :accountId AND t.date >= :startDate AND t.date < :endDate AND t.isDeleted = 0
        ORDER BY t.date DESC
    """)
    fun getTransactionsForDay(userId: String, accountId: Long, startDate: Long, endDate: Long): Flow<List<TransactionWithCategory>>

    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions 
        WHERE userId = :userId AND accountId = :accountId AND type = 'INCOME' AND date >= :startDate AND date < :endDate AND isDeleted = 0
    """)
    fun getTotalIncome(userId: String, accountId: Long, startDate: Long, endDate: Long): Flow<Double>

    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions 
        WHERE userId = :userId AND accountId = :accountId AND type = 'EXPENSE' AND date >= :startDate AND date < :endDate AND isDeleted = 0
    """)
    fun getTotalExpenses(userId: String, accountId: Long, startDate: Long, endDate: Long): Flow<Double>

    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions 
        WHERE userId = :userId AND accountId = :accountId AND type = 'INCOME' AND (:startDate IS NULL OR date >= :startDate) AND date < :endDate AND isDeleted = 0
    """)
    fun getTotalIncome(userId: String, accountId: Long, startDate: Long?, endDate: Long): Flow<Double>

    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions 
        WHERE userId = :userId AND accountId = :accountId AND type = 'EXPENSE' AND (:startDate IS NULL OR date >= :startDate) AND date < :endDate AND isDeleted = 0
    """)
    fun getTotalExpenses(userId: String, accountId: Long, startDate: Long?, endDate: Long): Flow<Double>

    @Query("""
        SELECT COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amount ELSE -amount END), 0) 
        FROM transactions 
        WHERE userId = :userId AND accountId = :accountId AND date >= :startDate AND date < :endDate AND isDeleted = 0
    """)
    fun getDailyBalance(userId: String, accountId: Long, startDate: Long, endDate: Long): Flow<Double>

    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions 
        WHERE userId = :userId AND accountId = :accountId AND type = 'EXPENSE' AND categoryId = :categoryId 
        AND date >= :startDate AND date < :endDate AND isDeleted = 0
    """)
    fun getExpensesByCategory(userId: String, accountId: Long, categoryId: Long, startDate: Long, endDate: Long): Flow<Double>

    // All-accounts queries (consolidated view)
    @Query("""
        SELECT t.id, t.name, t.amount, t.type, t.categoryId, t.accountId, t.date, t.note, 
               t.isValidated, t.recurrence, t.recurringId, c.name as categoryName, c.icon as categoryIcon, c.color as categoryColor
        FROM transactions t 
        LEFT JOIN categories c ON t.categoryId = c.id 
        WHERE t.userId = :userId AND t.date >= :startDate AND t.date < :endDate AND t.isDeleted = 0
        ORDER BY t.date DESC
    """)
    fun getAllTransactionsForPeriod(userId: String, startDate: Long, endDate: Long): Flow<List<TransactionWithCategory>>

    @Query("""
        SELECT t.id, t.name, t.amount, t.type, t.categoryId, t.accountId, t.date, t.note, 
               t.isValidated, t.recurrence, t.recurringId, c.name as categoryName, c.icon as categoryIcon, c.color as categoryColor
        FROM transactions t 
        LEFT JOIN categories c ON t.categoryId = c.id 
        WHERE t.userId = :userId AND t.date >= :startDate AND t.date < :endDate AND t.isDeleted = 0
        ORDER BY t.date DESC
    """)
    fun getAllTransactionsForDay(userId: String, startDate: Long, endDate: Long): Flow<List<TransactionWithCategory>>

    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions 
        WHERE userId = :userId AND type = 'INCOME' AND date >= :startDate AND date < :endDate AND isDeleted = 0
    """)
    fun getAllTotalIncome(userId: String, startDate: Long, endDate: Long): Flow<Double>

    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions 
        WHERE userId = :userId AND type = 'EXPENSE' AND date >= :startDate AND date < :endDate AND isDeleted = 0
    """)
    fun getAllTotalExpenses(userId: String, startDate: Long, endDate: Long): Flow<Double>

    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions 
        WHERE userId = :userId AND type = 'INCOME' AND (:startDate IS NULL OR date >= :startDate) AND date < :endDate AND isDeleted = 0
    """)
    fun getAllTotalIncome(userId: String, startDate: Long?, endDate: Long): Flow<Double>

    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions 
        WHERE userId = :userId AND type = 'EXPENSE' AND (:startDate IS NULL OR date >= :startDate) AND date < :endDate AND isDeleted = 0
    """)
    fun getAllTotalExpenses(userId: String, startDate: Long?, endDate: Long): Flow<Double>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: Transaction): Long

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("SELECT * FROM transactions WHERE id = :id AND userId = :userId")
    suspend fun getTransactionById(id: Long, userId: String): Transaction?

    @Query("""
        SELECT COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amount ELSE -amount END), 0) 
        FROM transactions WHERE userId = :userId AND accountId = :accountId AND isDeleted = 0
    """)
    suspend fun getTotalBalance(userId: String, accountId: Long): Double

    @Query("""
        SELECT COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amount ELSE -amount END), 0) 
        FROM transactions WHERE userId = :userId AND isDeleted = 0
    """)
    suspend fun getTotalBalanceAllAccounts(userId: String): Double

    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions 
        WHERE userId = :userId AND accountId = :accountId AND type = 'INCOME' AND date >= :startDate AND date < :endDate AND isDeleted = 0
    """)
    suspend fun getTotalIncomeDirect(userId: String, accountId: Long, startDate: Long, endDate: Long): Double

    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions 
        WHERE userId = :userId AND accountId = :accountId AND type = 'EXPENSE' AND date >= :startDate AND date < :endDate AND isDeleted = 0
    """)
    suspend fun getTotalExpensesDirect(userId: String, accountId: Long, startDate: Long, endDate: Long): Double

    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions 
        WHERE userId = :userId AND accountId = :accountId AND type = 'EXPENSE' AND categoryId = :categoryId 
        AND date >= :startDate AND date < :endDate AND isDeleted = 0
    """)
    suspend fun getExpensesByCategoryDirect(userId: String, accountId: Long, categoryId: Long, startDate: Long, endDate: Long): Double

    @Query("""
        SELECT t.id, t.name, t.amount, t.type, t.categoryId, t.accountId, t.date, t.note, 
               t.isValidated, t.recurrence, t.recurringId, c.name as categoryName, c.icon as categoryIcon, c.color as categoryColor
        FROM transactions t 
        LEFT JOIN categories c ON t.categoryId = c.id 
        WHERE t.userId = :userId AND t.isDeleted = 0 AND (
            t.name LIKE '%' || :query || '%' 
           OR t.note LIKE '%' || :query || '%' 
           OR c.name LIKE '%' || :query || '%'
           OR CAST(t.amount AS TEXT) LIKE '%' || :query || '%'
        )
        ORDER BY t.date DESC
        LIMIT 100
    """)
    fun searchTransactions(userId: String, query: String): Flow<List<TransactionWithCategory>>

    @Insert
    suspend fun insertAll(transactions: List<Transaction>)

    @Query("SELECT * FROM transactions WHERE userId = :userId AND recurringId = :recurringId AND date = :date AND isDeleted = 0")
    suspend fun getOccurrenceByRecurringAndDate(userId: String, recurringId: Long, date: Long): Transaction?

    @Query("SELECT MAX(date) FROM transactions WHERE userId = :userId AND recurringId = :recurringId AND isDeleted = 0")
    suspend fun getLastOccurrenceDateForRecurring(userId: String, recurringId: Long): Long?

    @Query("SELECT * FROM transactions WHERE userId = :userId AND recurringId = :recurringId AND date >= :fromDate AND isDeleted = 0")
    suspend fun getFutureOccurrences(userId: String, recurringId: Long, fromDate: Long): List<Transaction>

    @Query("""
        UPDATE transactions SET amount = :amount, name = :name, categoryId = :categoryId, note = :note, type = :type
        WHERE userId = :userId AND recurringId = :recurringId AND date >= :fromDate AND isDeleted = 0 AND isModified = 0
    """)
    suspend fun updateFutureUnmodifiedOccurrences(
        userId: String, recurringId: Long, fromDate: Long, name: String, amount: Double,
        type: TransactionType, categoryId: Long?, note: String
    )

    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions 
        WHERE userId = :userId AND accountId = :accountId AND type = :type AND date >= :startDate AND date < :endDate AND isDeleted = 0
    """)
    suspend fun getTotalByType(userId: String, accountId: Long, type: TransactionType, startDate: Long, endDate: Long): Double

    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions 
        WHERE userId = :userId AND type = :type AND date >= :startDate AND date < :endDate AND isDeleted = 0
    """)
    suspend fun getAllTotalByType(userId: String, type: TransactionType, startDate: Long, endDate: Long): Double

    @Query("UPDATE transactions SET isDeleted = 1 WHERE userId = :userId AND id = :id")
    suspend fun softDelete(userId: String, id: Long)

    @Query("UPDATE transactions SET isDeleted = 1 WHERE userId = :userId AND recurringId = :recurringId AND date >= :fromDate")
    suspend fun softDeleteFutureOccurrences(userId: String, recurringId: Long, fromDate: Long)

    @Query("UPDATE transactions SET isDeleted = 1 WHERE userId = :userId AND recurringId = :recurringId")
    suspend fun softDeleteAllOccurrences(userId: String, recurringId: Long)
    
    @Query("SELECT * FROM transactions WHERE userId = :userId AND date >= :startDate AND date < :endDate AND isDeleted = 0")
    suspend fun getTransactionsByDateRangeDirect(userId: String, startDate: Long, endDate: Long): List<Transaction>
    
    @Query("DELETE FROM transactions")
    suspend fun deleteAll()
}
