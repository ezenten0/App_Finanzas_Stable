package com.example.app_finanzas.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.app_finanzas.data.local.SyncStatusConverter
import com.example.app_finanzas.data.local.budget.BudgetDao
import com.example.app_finanzas.data.local.budget.BudgetEntity
import com.example.app_finanzas.data.local.transaction.TransactionDao
import com.example.app_finanzas.data.local.transaction.TransactionEntity
import com.example.app_finanzas.data.local.user.UserDao
import com.example.app_finanzas.data.local.user.UserEntity

/**
 * Central Room database that houses user credentials and financial transactions.
 */
@Database(
    entities = [UserEntity::class, TransactionEntity::class, BudgetEntity::class],
    version = 6,
    exportSchema = false
)
@TypeConverters(SyncStatusConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context.applicationContext).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "finanzas_app.db"
            )
                .addMigrations(MIGRATION_5_6)
                .build()
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS transactions_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        amountCents INTEGER NOT NULL,
                        type TEXT NOT NULL,
                        category TEXT NOT NULL,
                        date TEXT NOT NULL,
                        monthKey TEXT NOT NULL,
                        syncStatus TEXT NOT NULL
                    )
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    INSERT INTO transactions_new (id, title, description, amountCents, type, category, date, monthKey, syncStatus)
                    SELECT
                        id,
                        title,
                        description,
                        CAST(ROUND(ABS(amount) * 100) AS INTEGER),
                        LOWER(CASE UPPER(type)
                            WHEN 'CREDIT' THEN 'income'
                            WHEN 'INCOME' THEN 'income'
                            WHEN 'DEBIT' THEN 'expense'
                            WHEN 'EXPENSE' THEN 'expense'
                            ELSE 'expense'
                        END),
                        category,
                        date,
                        CASE WHEN LENGTH(date) >= 7 THEN SUBSTR(date, 1, 7) ELSE '1970-01' END,
                        syncStatus
                    FROM transactions
                    """.trimIndent()
                )

                database.execSQL("DROP TABLE transactions")
                database.execSQL("ALTER TABLE transactions_new RENAME TO transactions")
            }
        }
    }
}
