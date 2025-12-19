package com.example.app_finanzas.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.app_finanzas.data.budget.BudgetRepository
import com.example.app_finanzas.data.insights.InsightsRepository
import com.example.app_finanzas.home.analytics.FinancialInsight
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class InsightsViewModel(
    private val budgetRepository: BudgetRepository,
    private val insightsRepository: InsightsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InsightsUiState(isLoading = true))
    val uiState: StateFlow<InsightsUiState> = _uiState.asStateFlow()

    private var latestBudgets = emptyList<com.example.app_finanzas.data.budget.BudgetGoal>()

    init {
        refreshInsights()
        observeBudgets()
    }

    private fun observeBudgets() {
        viewModelScope.launch {
            budgetRepository.observeBudgets().collectLatest { budgets ->
                latestBudgets = budgets
                if (_uiState.value.insights.isEmpty()) {
                    refreshInsights()
                }
            }
        }
    }

    fun refreshInsights(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = insightsRepository.fetchInsights(
                budgets = latestBudgets,
                forceRefresh = forceRefresh,
                localFallback = _uiState.value.insights
            )
            _uiState.value = InsightsUiState(
                isLoading = false,
                insights = result.insights,
                errorMessage = result.errorMessage,
                isOffline = result.fromCache
            )
        }
    }
}

class InsightsViewModelFactory(
    private val budgetRepository: BudgetRepository,
    private val insightsRepository: InsightsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InsightsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InsightsViewModel(budgetRepository, insightsRepository) as T
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
