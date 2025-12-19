package com.example.app_finanzas.home.analytics

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.app_finanzas.data.insights.BudgetSnapshot
import com.example.app_finanzas.data.insights.MonthlyInsightsAggregate

/**
 * Builds user friendly insights from a pre-aggregated monthly snapshot so we avoid
 * scanning the full list of transactions on device.
 */
object AggregatedInsightGenerator {
    fun buildInsights(
        aggregate: MonthlyInsightsAggregate,
        budgetSnapshots: List<BudgetSnapshot>
    ): List<FinancialInsight> {
        val insights = mutableListOf<FinancialInsight>()
        val netBalance = aggregate.totalIncome - aggregate.totalExpense

        if (aggregate.totalIncome == 0.0 && aggregate.totalExpense == 0.0) return emptyList()

        if (netBalance >= 0) {
            val projected = netBalance * 3
            insights += FinancialInsight(
                id = "savings",
                title = "Ritmo de ahorro positivo",
                message = "Podrías ahorrar aproximadamente ${formatAmount(projected)} en los próximos 3 meses si mantienes el ritmo actual.",
                category = InsightCategory.SAVINGS
            )
        } else {
            insights += FinancialInsight(
                id = "overspend",
                title = "Gasto por encima de los ingresos",
                message = "Estás gastando ${formatAmount(kotlin.math.abs(netBalance))} más de lo que ingresas este mes. Ajusta tus presupuestos para evitar pérdidas.",
                category = InsightCategory.WARNING
            )
        }

        if (aggregate.expensesByCategory.isNotEmpty()) {
            val (topCategory, amount) = aggregate.expensesByCategory.maxByOrNull { it.value }!!
            insights += FinancialInsight(
                id = "topCategory",
                title = "Mayor gasto en $topCategory",
                message = "Has invertido ${formatAmount(amount)} en $topCategory este mes. Considera establecer un límite específico.",
                category = InsightCategory.EXPENSE
            )
        }

        budgetSnapshots.filter { it.progress >= 0.75 }.forEach { budget ->
            insights += FinancialInsight(
                id = "budget-${budget.category}",
                title = "Alerta en ${budget.category}",
                message = when {
                    budget.progress >= 1.0 ->
                        "Has superado el 100% del límite (${formatAmount(budget.limit)}). Ajusta tus gastos cuanto antes."
                    else ->
                        "Ya consumiste el ${(budget.progress * 100).toInt()}% de tu meta mensual en ${budget.category}. Reduce el ritmo para evitar sobrepasarla."
                },
                category = InsightCategory.BUDGET
            )
        }

        if (netBalance > 0) {
            val investmentSuggestion = netBalance * 0.2 * 12
            insights += FinancialInsight(
                id = "investment",
                title = "Multiplica tus ahorros",
                message = "Si destinas el 20% de tu ahorro mensual a inversiones podrías sumar cerca de ${formatAmount(investmentSuggestion)} en un año.",
                category = InsightCategory.OPPORTUNITY
            )
        }

        return insights.distinctBy { it.id }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun formatAmount(value: Double): String {
        val formatted = "%,d".format(value.toInt())
        return "$$formatted CLP"
    }
}
