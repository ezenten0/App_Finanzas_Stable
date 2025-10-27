package com.example.app_finanzas.home.analytics

import com.example.app_finanzas.data.budget.BudgetGoal
import com.example.app_finanzas.home.model.Transaction
import com.example.app_finanzas.home.model.TransactionType
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import kotlin.math.abs

/**
 * Generates actionable insights and projections so the user understands how to
 * grow their money based on the latest transactions and configured budgets.
 */
object InsightGenerator {

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())

    fun buildInsights(
        transactions: List<Transaction>,
        budgets: List<BudgetGoal>
    ): List<FinancialInsight> {
        if (transactions.isEmpty()) return emptyList()

        val currentMonth = LocalDate.now().withDayOfMonth(1)
        val groupedByMonth = transactions.groupBy { transactionMonth(it) }
        val monthTransactions = groupedByMonth[currentMonth] ?: transactions

        val totalIncome = monthTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val totalExpense = monthTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val netBalance = totalIncome - totalExpense

        val insights = mutableListOf<FinancialInsight>()

        if (netBalance >= 0) {
            val projected = netBalance * 3
            insights += FinancialInsight(
                id = "savings",
                title = "Ritmo de ahorro positivo",
                message = "Podrías ahorrar aproximadamente %.2f€ en los próximos 3 meses si mantienes el ritmo actual.".format(projected),
                category = InsightCategory.SAVINGS
            )
        } else {
            insights += FinancialInsight(
                id = "overspend",
                title = "Gasto por encima de los ingresos",
                message = "Estás gastando %.2f€ más de lo que ingresas este mes. Ajusta tus presupuestos para evitar pérdidas.".format(abs(netBalance)),
                category = InsightCategory.WARNING
            )
        }

        val expensesByCategory = monthTransactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        if (expensesByCategory.isNotEmpty()) {
            val (topCategory, amount) = expensesByCategory.maxByOrNull { it.value }!!
            insights += FinancialInsight(
                id = "topCategory",
                title = "Mayor gasto en $topCategory",
                message = "Has invertido ${formatAmount(amount)} en $topCategory este mes. Considera establecer un límite específico.",
                category = InsightCategory.EXPENSE
            )
        }

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
                        budget.progress >= 1.0 -> "Has superado el 100% del límite (${formatAmount(budget.limit)}). Ajusta tus gastos cuanto antes."
                        else -> "Ya consumiste el ${(budget.progress * 100).toInt()}% de tu meta mensual en ${budget.category}. Reduce el ritmo para evitar sobrepasarla."
                    },
                    category = InsightCategory.BUDGET
                )
            }
        }

        if (netBalance > 0) {
            val investmentSuggestion = netBalance * 0.2 * 12
            insights += FinancialInsight(
                id = "investment",
                title = "Multiplica tus ahorros",
                message = "Si destinas el 20% de tu ahorro mensual a inversiones podrías sumar cerca de %.2f€ en un año.".format(investmentSuggestion),
                category = InsightCategory.OPPORTUNITY
            )
        }

        return insights.distinctBy { it.id }
    }

    private fun transactionMonth(transaction: Transaction): LocalDate {
        return parseDate(transaction.date)?.withDayOfMonth(1) ?: LocalDate.now().withDayOfMonth(1)
    }

    private fun parseDate(date: String): LocalDate? {
        return try {
            LocalDate.parse(date, formatter)
        } catch (error: DateTimeParseException) {
            null
        }
    }

    private fun formatAmount(value: Double): String {
        return "%.2f€".format(value)
    }
}

/**
 * UI friendly representation of an automatically generated financial insight.
 */
data class FinancialInsight(
    val id: String,
    val title: String,
    val message: String,
    val category: InsightCategory
)

enum class InsightCategory {
    SAVINGS,
    EXPENSE,
    BUDGET,
    OPPORTUNITY,
    WARNING
}
