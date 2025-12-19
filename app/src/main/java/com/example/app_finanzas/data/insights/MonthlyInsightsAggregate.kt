package com.example.app_finanzas.data.insights

/**
 * Represents the aggregated monthly totals stored in Firestore so insights can be
 * generated without iterating over every transaction on the client.
 */
data class MonthlyInsightsAggregate(
    val monthKey: String = "",
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val expensesByCategory: Map<String, Double> = emptyMap(),
    val incomesByCategory: Map<String, Double> = emptyMap()
)
