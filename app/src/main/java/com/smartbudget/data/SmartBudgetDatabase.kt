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
import com.smartbudget.data.dao.SavingsGoalDao
import com.smartbudget.data.dao.TransactionDao
import com.smartbudget.data.entity.Account
import com.smartbudget.data.entity.Budget
import com.smartbudget.data.entity.Category
import com.smartbudget.data.entity.SavingsGoal
import com.smartbudget.data.entity.Transaction

@Database(
    entities = [Account::class, Transaction::class, Category::class, Budget::class, SavingsGoal::class],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SmartBudgetDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao
    abstract fun savingsGoalDao(): SavingsGoalDao

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

        fun getDatabase(context: Context): SmartBudgetDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SmartBudgetDatabase::class.java,
                    "smartbudget_database"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
