package com.example.app_finanzas.home.analytics

import com.example.app_finanzas.home.model.Transaction
import com.example.app_finanzas.home.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Verifies the aggregation helpers that drive the home, statistics and budgets
 * screens.
 */
class TransactionAnalyticsTest {

    private val sampleTransactions = listOf(
        Transaction(1, "Ingreso", "Salario", 1000.0, TransactionType.INCOME, "Salario", "2024-10-01"),
        Transaction(2, "Cine", "Salida", 50.0, TransactionType.EXPENSE, "Entretenimiento", "2024-10-02"),
        Transaction(3, "Cena", "Restaurante", 40.0, TransactionType.EXPENSE, "Social", "2024-10-03")
    )

    @Test
    fun `balance summary aggregates income and expenses`() {
        val summary = TransactionAnalytics.calculateBalanceSummary(sampleTransactions)
        assertEquals(1000.0, summary.totalIncome, 0.001)
        assertEquals(90.0, summary.totalExpense, 0.001)
        assertEquals(910.0, summary.totalBalance, 0.001)
    }

    @Test
    fun `expenses group by category`() {
        val expenses = TransactionAnalytics.calculateExpenseByCategory(sampleTransactions)
        assertEquals(2, expenses.size)
        assertEquals(50.0, expenses["Entretenimiento"], 0.001)
        assertEquals(40.0, expenses["Social"], 0.001)
    }

    @Test
    fun `budget progress calculates percentages`() {
        val budget = mapOf("Entretenimiento" to 100.0, "Social" to 80.0)
        val progress = TransactionAnalytics.calculateBudgetProgress(sampleTransactions, budget)
        val entertainment = progress.first { it.category == "Entretenimiento" }
        val social = progress.first { it.category == "Social" }
        assertEquals(0.5, entertainment.progress, 0.001)
        assertEquals(0.5, social.progress, 0.001)
    }
}
