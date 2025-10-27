package com.example.app_finanzas.data.budget

import com.example.app_finanzas.categories.CategoryDefinitions
import com.example.app_finanzas.data.local.budget.BudgetDao
import com.example.app_finanzas.data.local.budget.BudgetEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Repository coordinating every budget persistence operation and exposing UI
 * friendly models for the presentation layer.
 */
class BudgetRepository(
    private val budgetDao: BudgetDao
) {

    fun observeBudgets(): Flow<List<BudgetGoal>> {
        return budgetDao.observeBudgets().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun getBudgetById(id: Int): BudgetGoal? {
        return withContext(Dispatchers.IO) {
            budgetDao.getBudgetById(id)?.toDomain()
        }
    }

    suspend fun upsertBudget(goal: BudgetGoal): Int {
        return withContext(Dispatchers.IO) {
            budgetDao.upsertBudget(goal.toEntity()).toInt()
        }
    }

    suspend fun deleteBudget(id: Int) {
        withContext(Dispatchers.IO) {
            budgetDao.deleteBudget(id)
        }
    }

    suspend fun ensureSeedData(defaults: List<BudgetGoal> = BudgetDefaults.defaultGoals()) {
        withContext(Dispatchers.IO) {
            val current = budgetDao.observeBudgets().first()
            if (current.isEmpty()) {
                defaults.forEach { budgetDao.upsertBudget(it.toEntity()) }
            }
        }
    }

    private fun BudgetEntity.toDomain(): BudgetGoal {
        return BudgetGoal(
            id = id,
            category = category,
            limit = limit,
            iconKey = iconKey
        )
    }

    private fun BudgetGoal.toEntity(): BudgetEntity {
        return BudgetEntity(
            id = id,
            category = category.trim(),
            limit = limit,
            iconKey = iconKey
        )
    }
}

/**
 * Lightweight domain model consumed by the Budgets screen.
 */
data class BudgetGoal(
    val id: Int = 0,
    val category: String,
    val limit: Double,
    val iconKey: String
)

/**
 * Provides a curated list of starting budgets so the Budgets screen feels rich
 * on a fresh install while still allowing full customization afterwards.
 */
object BudgetDefaults {
    fun defaultGoals(): List<BudgetGoal> {
        return listOf(
            BudgetGoal(category = "Alimentos", limit = 300.0, iconKey = CategoryDefinitions.FOOD),
            BudgetGoal(category = "Entretenimiento", limit = 120.0, iconKey = CategoryDefinitions.ENTERTAINMENT),
            BudgetGoal(category = "Social", limit = 150.0, iconKey = CategoryDefinitions.SOCIAL),
            BudgetGoal(category = "Inversiones", limit = 200.0, iconKey = CategoryDefinitions.INVESTMENTS)
        )
    }
}
