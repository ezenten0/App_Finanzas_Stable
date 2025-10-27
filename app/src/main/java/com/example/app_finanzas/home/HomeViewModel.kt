package com.example.app_finanzas.home

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_finanzas.data.transaction.TransactionRepository
import com.example.app_finanzas.home.analytics.TransactionAnalytics
import com.example.app_finanzas.home.model.HomeUiState
import com.example.app_finanzas.home.model.Transaction
import kotlinx.coroutines.launch

/**
 * ViewModel in charge of preparing the home dashboard state by reacting to the
 * transaction stream and user profile information.
 */
class HomeViewModel(
    private val transactionRepository: TransactionRepository
) : ViewModel() {
    private val _uiState = mutableStateOf(HomeUiState())
    val uiState: State<HomeUiState> = _uiState

    init {
        observeTransactions()
        seedDefaultTransactions()
    }

    /**
     * Updates the user identity displayed on the toolbar with the authenticated
     * information passed from the login flow.
     */
    fun updateUserProfile(name: String, email: String) {
        val displayName = name.trim().ifBlank { "Usuario" }
        _uiState.value = _uiState.value.copy(
            userName = displayName,
            userEmail = email.trim()
        )
    }

    /**
     * Reacts to every transaction stored in the database and keeps the UI state
     * synchronized via a single source of truth.
     */
    private fun observeTransactions() {
        viewModelScope.launch {
            transactionRepository.observeTransactions().collect { transactions ->
                updateState(transactions)
            }
        }
    }

    /**
     * Seeds sample data the first time the user opens the application so the
     * analytics widgets have meaningful values.
     */
    private fun seedDefaultTransactions() {
        viewModelScope.launch {
            transactionRepository.ensureSeedData()
        }
    }

    private fun updateState(transactions: List<Transaction>) {
        val summary = TransactionAnalytics.calculateBalanceSummary(transactions)
        _uiState.value = _uiState.value.copy(
            totalBalance = summary.totalBalance,
            totalIncome = summary.totalIncome,
            totalExpense = summary.totalExpense,
            transactions = transactions
        )
    }
}
