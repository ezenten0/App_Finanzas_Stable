package com.example.app_finanzas.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
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
    version = 4,
    exportSchema = false
)
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
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
