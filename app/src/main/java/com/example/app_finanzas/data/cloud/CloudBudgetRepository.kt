package com.example.app_finanzas.data.cloud

import com.example.app_finanzas.data.budget.BudgetGoal

/**
 * Persists and retrieves budgets from the authenticated user's Firestore
 * namespace.
 */
interface CloudBudgetRepository {
    suspend fun downloadBudgets(): List<BudgetGoal>

    suspend fun upsertBudget(goal: BudgetGoal): BudgetGoal

    suspend fun deleteBudget(id: Int)
}
