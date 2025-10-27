package com.example.app_finanzas.home.model

/**
 * Enumerates the two types of financial transactions supported by the app and used
 * across the analytics and UI layers.
 */
enum class TransactionType {
    INCOME,
    EXPENSE
}

/**
 * UI friendly representation of a persisted transaction.
 */
data class Transaction(
    val id: Int,
    val title: String,
    val description: String,
    val amount: Double,
    val type: TransactionType,
    val category: String,
    val date: String
)
