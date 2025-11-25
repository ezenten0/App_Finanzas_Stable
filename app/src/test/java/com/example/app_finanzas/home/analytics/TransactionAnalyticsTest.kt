package com.example.app_finanzas.home.analytics

import com.example.app_finanzas.home.model.Transaction
import com.example.app_finanzas.home.model.TransactionType
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.shouldBe
import java.time.LocalDate

class TransactionAnalyticsTest : StringSpec({
    val baseTransactions = listOf(
        Transaction(id = 1, title = "Salario", description = "Pago mensual", amount = 1500.0, type = TransactionType.INCOME, category = "Trabajo", date = "2024-10-01"),
        Transaction(id = 2, title = "Alquiler", description = "Depto", amount = 700.0, type = TransactionType.EXPENSE, category = "Hogar", date = "2024-10-02"),
        Transaction(id = 3, title = "Supermercado", description = "Compras", amount = 200.0, type = TransactionType.EXPENSE, category = "Alimentos", date = "2024-10-02"),
        Transaction(id = 4, title = "Venta", description = "Garage", amount = 250.0, type = TransactionType.INCOME, category = "Otros", date = "2024-10-03")
    )

    "calculateBalanceSummary aggregates totals" {
        val summary = TransactionAnalytics.calculateBalanceSummary(baseTransactions)

        summary.totalIncome shouldBe 1750.0
        summary.totalExpense shouldBe 900.0
        summary.totalBalance shouldBe 850.0
    }

    "calculateExpenseByCategory groups expenses" {
        val expenses = TransactionAnalytics.calculateExpenseByCategory(baseTransactions)

        expenses.shouldContainExactly(
            mapOf(
                "Hogar" to 700.0,
                "Alimentos" to 200.0
            )
        )
    }

    "calculateBudgetProgress keeps percentages above 100" {
        val budgets = mapOf("Hogar" to 800.0, "Alimentos" to 150.0)

        val progress = TransactionAnalytics.calculateBudgetProgress(baseTransactions, budgets)

        progress.shouldContainExactly(
            listOf(
                BudgetProgress(category = "Hogar", spent = 700.0, limit = 800.0, progress = 0.875),
                BudgetProgress(category = "Alimentos", spent = 200.0, limit = 150.0, progress = 1.3333333333333333)
            )
        )
    }

    "calculateTimeSeries respects time range" {
        val now = LocalDate.of(2024, 10, 5)
        val points = TransactionAnalytics.calculateTimeSeries(baseTransactions, StatisticsRange.LAST_7_DAYS, now)

        points.size shouldBe 7
        points.first().date shouldBe now.minusDays(6)
        points.last().income shouldBe 0.0
        points.last().expense shouldBe 0.0
    }
})
