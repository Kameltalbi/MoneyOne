package com.smartbudget.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.smartbudget.data.dao.AccountDao
import com.smartbudget.data.dao.BudgetDao
import com.smartbudget.data.dao.CategoryDao
import com.smartbudget.data.dao.RecurringDao
import com.smartbudget.data.dao.SavingsGoalDao
import com.smartbudget.data.dao.TransactionDao
import com.smartbudget.data.entity.Account
import com.smartbudget.data.entity.Budget
import com.smartbudget.data.entity.Category
import com.smartbudget.data.entity.RecurringTransaction
import com.smartbudget.data.entity.SavingsGoal
import com.smartbudget.data.entity.Transaction

@Database(
    entities = [Account::class, Transaction::class, Category::class, Budget::class, SavingsGoal::class, RecurringTransaction::class],
    version = 8,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SmartBudgetDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun recurringDao(): RecurringDao

    companion object {
        @Volatile
        private var INSTANCE: SmartBudgetDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE transactions ADD COLUMN name TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE transactions ADD COLUMN recurrenceEndDate INTEGER DEFAULT NULL")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS savings_goals (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        targetAmount REAL NOT NULL,
                        currentAmount REAL NOT NULL DEFAULT 0.0,
                        icon TEXT NOT NULL DEFAULT 'savings',
                        color INTEGER NOT NULL DEFAULT ${0xFF4CAF50},
                        createdDate INTEGER NOT NULL DEFAULT 0,
                        targetDate INTEGER DEFAULT NULL
                    )
                """)
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE accounts ADD COLUMN currency TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE transactions ADD COLUMN recurrenceGroupId INTEGER DEFAULT NULL")
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    UPDATE transactions SET recurrenceGroupId = (
                        SELECT MIN(t2.id) FROM transactions t2 
                        WHERE t2.name = transactions.name 
                        AND t2.accountId = transactions.accountId 
                        AND t2.type = transactions.type 
                        AND t2.categoryId IS transactions.categoryId 
                        AND t2.recurrence = transactions.recurrence
                        AND t2.recurrence != 'NONE'
                    )
                    WHERE recurrence != 'NONE' AND recurrenceGroupId IS NULL
                """)
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create recurring_transactions table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS recurring_transactions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        amount REAL NOT NULL,
                        type TEXT NOT NULL,
                        categoryId INTEGER DEFAULT NULL,
                        accountId INTEGER NOT NULL,
                        note TEXT NOT NULL DEFAULT '',
                        startDate INTEGER NOT NULL,
                        frequency TEXT NOT NULL,
                        `interval` INTEGER NOT NULL DEFAULT 1,
                        endDate INTEGER DEFAULT NULL,
                        isActive INTEGER NOT NULL DEFAULT 1,
                        FOREIGN KEY (categoryId) REFERENCES categories(id) ON DELETE SET NULL,
                        FOREIGN KEY (accountId) REFERENCES accounts(id) ON DELETE CASCADE
                    )
                """)
                database.execSQL("CREATE INDEX IF NOT EXISTS index_recurring_transactions_categoryId ON recurring_transactions(categoryId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_recurring_transactions_accountId ON recurring_transactions(accountId)")
                // Add new columns to transactions
                database.execSQL("ALTER TABLE transactions ADD COLUMN recurringId INTEGER DEFAULT NULL")
                database.execSQL("ALTER TABLE transactions ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE transactions ADD COLUMN isModified INTEGER NOT NULL DEFAULT 0")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_recurringId ON transactions(recurringId)")
            }
        }

        fun getDatabase(context: Context): SmartBudgetDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SmartBudgetDatabase::class.java,
                    "smartbudget_database"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
