package com.example.app_finanzas.home.analytics

import com.example.app_finanzas.home.model.Transaction
import com.example.app_finanzas.home.model.TransactionType
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Provides reusable analytics helpers that aggregate persisted transactions into
 * summary metrics consumed by the home, statistics, and budgets screens.
 */
object TransactionAnalytics {

    /**
     * Calculates overall balance, incomes, and expenses so they can be displayed in
     * the dashboard header.
     */
    fun calculateBalanceSummary(transactions: List<Transaction>): BalanceSummary {
        val income = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val expense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        return BalanceSummary(
            totalIncome = income,
            totalExpense = expense,
            totalBalance = income - expense
        )
    }

    /**
     * Groups expenses by category so the statistics screen can surface the biggest
     * spending buckets.
     */
    fun calculateExpenseByCategory(transactions: List<Transaction>): Map<String, Double> {
        return transactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
    }

    /**
     * Produces a simple progress percentage comparing category spend to an optional
     * monthly budget reference.
     */
    fun calculateBudgetProgress(
        transactions: List<Transaction>,
        monthlyBudget: Map<String, Double>
    ): List<BudgetProgress> {
        val expenses = calculateExpenseByCategory(transactions)
        return monthlyBudget.map { (category, limit) ->
            val used = expenses[category] ?: 0.0
            val progress = if (limit == 0.0) 0.0 else (used / limit).coerceAtMost(1.0)
            BudgetProgress(category = category, spent = used, limit = limit, progress = progress)
        }
    }

    fun calculateTimeSeries(
        transactions: List<Transaction>,
        range: StatisticsRange,
        referenceDate: LocalDate = LocalDate.now()
    ): List<TimeSeriesPoint> {
        val startDate = range.startDate(referenceDate)
        val safeStart = if (startDate.isAfter(referenceDate)) referenceDate else startDate
        val groupedByDate = transactions.mapNotNull { transaction ->
            val date = runCatching { LocalDate.parse(transaction.date) }.getOrNull()
            date?.let {
                it to transaction
            }
        }
            .filter { (date, _) -> !date.isBefore(safeStart) && !date.isAfter(referenceDate) }
            .groupBy({ it.first }) { it.second }

        val totalDays = ChronoUnit.DAYS.between(safeStart, referenceDate).toInt()
        if (totalDays < 0) return emptyList()

        return (0..totalDays).map { offset ->
            val date = safeStart.plusDays(offset.toLong())
            val transactionsForDate = groupedByDate[date].orEmpty()
            val income = transactionsForDate
                .filter { it.type == TransactionType.INCOME }
                .sumOf { it.amount }
            val expense = transactionsForDate
                .filter { it.type == TransactionType.EXPENSE }
                .sumOf { it.amount }
            TimeSeriesPoint(date = date, income = income, expense = expense)
        }
    }
}

/**
 * Aggregates the main balance metrics displayed on the home screen.
 */
data class BalanceSummary(
    val totalIncome: Double,
    val totalExpense: Double,
    val totalBalance: Double
)

/**
 * Represents how much of a budget has been consumed for a specific category.
 */
data class BudgetProgress(
    val category: String,
    val spent: Double,
    val limit: Double,
    val progress: Double
)

enum class StatisticsRange(val displayName: String) {
    LAST_7_DAYS("7 días"),
    LAST_MONTH("Último mes"),
    LAST_SIX_MONTHS("6 meses"),
    LAST_YEAR("1 año");

    internal fun startDate(referenceDate: LocalDate): LocalDate {
        return when (this) {
            LAST_7_DAYS -> referenceDate.minusDays(6)
            LAST_MONTH -> referenceDate.minusDays(29)
            LAST_SIX_MONTHS -> referenceDate.minusMonths(6)
            LAST_YEAR -> referenceDate.minusYears(1)
        }
    }
}

data class TimeSeriesPoint(
    val date: LocalDate,
    val income: Double,
    val expense: Double
)
