package com.example.app_finanzas.home.analytics

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
