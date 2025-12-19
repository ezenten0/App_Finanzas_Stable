package com.example.app_finanzas.home.analytics

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.app_finanzas.data.budget.BudgetGoal
import com.example.app_finanzas.home.model.Transaction
import com.example.app_finanzas.home.model.TransactionType
import kotlin.math.abs

object InsightGenerator {
    fun buildInsights(
        transactions: List<Transaction>,
        budgets: List<BudgetGoal>
    ): List<FinancialInsight> {
        if (transactions.isEmpty()) return emptyList()

        val monthTransactions = TransactionAnalytics.currentMonthTransactions(transactions)

        val totalIncome = monthTransactions
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amountCents } / 100.0
        val totalExpense = monthTransactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amountCents } / 100.0
        val netBalance = totalIncome - totalExpense

        val insights = mutableListOf<FinancialInsight>()

        // 1) Ahorro / sobre-gasto
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
                message = "Estás gastando ${formatAmount(abs(netBalance))} más de lo que ingresas este mes. Ajusta tus presupuestos para evitar pérdidas.",
                category = InsightCategory.WARNING
            )
        }

        // 2) Categoría de gasto principal
        val expensesByCategory = monthTransactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amountCents } / 100.0 }

        if (expensesByCategory.isNotEmpty()) {
            val (topCategory, amount) = expensesByCategory.maxByOrNull { it.value }!!
            insights += FinancialInsight(
                id = "topCategory",
                title = "Mayor gasto en $topCategory",
                message = "Has invertido ${formatAmount(amount)} en $topCategory este mes. Considera establecer un límite específico.",
                category = InsightCategory.EXPENSE
            )
        }

        // 3) Alertas de presupuesto
        if (budgets.isNotEmpty()) {
            val progress = TransactionAnalytics.calculateBudgetProgress(
                transactions = monthTransactions,
                monthlyBudget = budgets.associate { it.category to it.limit }
            )
            progress.filter { it.progress >= 0.75 }.forEach { budget ->
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
        }

        // 4) Sugerencia de inversión
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
    // Formatea como entero en CLP: ej. $12.345 CLP
    private fun formatAmount(value: Double): String {
        val intValue = value.toInt()
        val formatted = "%,d".format(intValue)      // 12345 -> "12,345"
        return "$$formatted CLP"                   // "$12,345 CLP"
    }
}
