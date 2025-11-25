package com.example.app_finanzas.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.app_finanzas.data.budget.BudgetRepository
import com.example.app_finanzas.data.insights.InsightsRepository
import com.example.app_finanzas.data.insights.BudgetSnapshot
import com.example.app_finanzas.data.transaction.TransactionRepository
import com.example.app_finanzas.home.analytics.FinancialInsight
import com.example.app_finanzas.home.analytics.InsightGenerator
import com.example.app_finanzas.home.analytics.TransactionAnalytics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class InsightsViewModel(
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository,
    private val insightsRepository: InsightsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InsightsUiState(isLoading = true))
    val uiState: StateFlow<InsightsUiState> = _uiState.asStateFlow()

    private var latestLocalInsights: List<FinancialInsight> = emptyList()
    private var latestTransactions = emptyList<com.example.app_finanzas.home.model.Transaction>()
    private var latestBudgets = emptyList<com.example.app_finanzas.data.budget.BudgetGoal>()

    init {
        observeLocalData()
        refreshInsights()
    }

    private fun observeLocalData() {
        viewModelScope.launch {
            combine(
                transactionRepository.observeTransactions(),
                budgetRepository.observeBudgets()
            ) { transactions, budgets ->
                latestTransactions = transactions
                latestBudgets = budgets
                InsightGenerator.buildInsights(transactions, budgets)
            }.collectLatest { local ->
                latestLocalInsights = local
                if (uiState.value.insights.isEmpty() && local.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        insights = local,
                        isOffline = true,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun refreshInsights(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = insightsRepository.fetchInsights(
                localFallback = latestLocalInsights,
                budgetSnapshots = buildBudgetSnapshots(),
                forceRefresh = forceRefresh
            )
            _uiState.value = InsightsUiState(
                isLoading = false,
                insights = result.insights,
                errorMessage = result.errorMessage,
                isOffline = result.fromCache
            )
        }
    }

    private fun buildBudgetSnapshots(): List<BudgetSnapshot> {
        if (latestBudgets.isEmpty()) return emptyList()
        val monthTransactions = TransactionAnalytics.currentMonthTransactions(latestTransactions)
        val monthlyBudget = latestBudgets.associate { it.category to it.limit }
        return TransactionAnalytics.calculateBudgetProgress(monthTransactions, monthlyBudget)
            .map { progress ->
                BudgetSnapshot(
                    category = progress.category,
                    limit = progress.limit,
                    spent = progress.spent,
                    progress = progress.progress
                )
            }
    }
}

class InsightsViewModelFactory(
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository,
    private val insightsRepository: InsightsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InsightsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InsightsViewModel(transactionRepository, budgetRepository, insightsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

data class InsightsUiState(
    val isLoading: Boolean = false,
    val insights: List<FinancialInsight> = emptyList(),
    val errorMessage: String? = null,
    val isOffline: Boolean = false
)
