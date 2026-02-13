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
import com.smartbudget.data.dao.TransactionDao
import com.smartbudget.data.entity.Account
import com.smartbudget.data.entity.Budget
import com.smartbudget.data.entity.Category
import com.smartbudget.data.entity.Transaction

@Database(
    entities = [Account::class, Transaction::class, Category::class, Budget::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SmartBudgetDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        @Volatile
        private var INSTANCE: SmartBudgetDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE transactions ADD COLUMN name TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getDatabase(context: Context): SmartBudgetDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SmartBudgetDatabase::class.java,
                    "smartbudget_database"
                ).addMigrations(MIGRATION_1_2)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
