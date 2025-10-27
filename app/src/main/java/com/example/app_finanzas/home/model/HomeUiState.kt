package com.example.app_finanzas.home.model

data class HomeUiState(
    val userName: String = "",
    val userEmail: String = "",
    val totalBalance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val transactions: List<Transaction> = emptyList()
)
