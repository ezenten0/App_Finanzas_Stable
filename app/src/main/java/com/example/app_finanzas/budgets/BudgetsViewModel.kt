package com.example.app_finanzas.budgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.app_finanzas.data.budget.BudgetGoal
import com.example.app_finanzas.data.budget.BudgetRepository
import com.example.app_finanzas.data.transaction.TransactionRepository
import com.example.app_finanzas.home.analytics.BudgetProgress
import com.example.app_finanzas.home.analytics.TransactionAnalytics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * ViewModel that synchronizes budget goals with the latest transactions to
 * calculate spending progress in real time.
 */
class BudgetsViewModel(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetsUiState())
    val uiState: StateFlow<BudgetsUiState> = _uiState

    init {
        observeBudgets()
        seedDefaults()
    }

    private fun observeBudgets() {
        viewModelScope.launch {
            combine(
                budgetRepository.observeBudgets(),
                transactionRepository.observeTransactions()
            ) { budgets, transactions ->
                val budgetMap = budgets.associate { it.category to it.limit }
                val progress = TransactionAnalytics.calculateBudgetProgress(transactions, budgetMap)
                BudgetsUiState(
                    goals = budgets,
                    progress = progress,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    private fun seedDefaults() {
        viewModelScope.launch {
            budgetRepository.ensureSeedData()
        }
    }

    fun saveBudget(goal: BudgetGoal) {
        viewModelScope.launch {
            budgetRepository.upsertBudget(goal)
        }
    }

    fun deleteBudget(id: Int) {
        viewModelScope.launch {
            budgetRepository.deleteBudget(id)
        }
    }

    class Factory(
        private val budgetRepository: BudgetRepository,
        private val transactionRepository: TransactionRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(BudgetsViewModel::class.java))
            return BudgetsViewModel(budgetRepository, transactionRepository) as T
        }
    }
}

/**
 * Immutable UI state consumed by the budgets screen.
 */
data class BudgetsUiState(
    val goals: List<BudgetGoal> = emptyList(),
    val progress: List<BudgetProgress> = emptyList(),
    val isLoading: Boolean = true
)
