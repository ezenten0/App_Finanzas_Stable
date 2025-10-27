package com.example.app_finanzas.data.local.budget

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data access object exposing reactive streams and editing operations for the
 * configurable budgets stored locally.
 */
@Dao
interface BudgetDao {

    /**
     * Streams every budget goal ordered alphabetically to keep the UI stable
     * when items are updated.
     */
    @Query("SELECT * FROM budgets ORDER BY category ASC")
    fun observeBudgets(): Flow<List<BudgetEntity>>

    /**
     * Fetches a single budget goal by id so the editor screen can pre-populate
     * the fields when the user wants to make adjustments.
     */
    @Query("SELECT * FROM budgets WHERE id = :budgetId")
    suspend fun getBudgetById(budgetId: Int): BudgetEntity?

    /**
     * Inserts or updates a budget goal and returns the generated id so callers
     * can react accordingly.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBudget(budget: BudgetEntity): Long

    /**
     * Removes the selected budget from disk.
     */
    @Query("DELETE FROM budgets WHERE id = :budgetId")
    suspend fun deleteBudget(budgetId: Int)
}
