package com.example.app_finanzas.insights

import com.example.app_finanzas.MainDispatcherRule
import com.example.app_finanzas.data.budget.BudgetGoal
import com.example.app_finanzas.data.budget.BudgetRepository
import com.example.app_finanzas.data.insights.InsightsRepository
import com.example.app_finanzas.data.insights.InsightsResult
import com.example.app_finanzas.data.transaction.TransactionRepository
import com.example.app_finanzas.home.analytics.FinancialInsight
import com.example.app_finanzas.home.analytics.InsightCategory
import com.example.app_finanzas.home.model.Transaction
import com.example.app_finanzas.home.model.TransactionType
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InsightsViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val transactionFlow = MutableStateFlow(
        listOf(
            Transaction(
                id = 1,
                title = "Sueldo",
                description = "Ingreso principal",
                amount = 1000.0,
                type = TransactionType.INCOME,
                category = "Salario",
                date = "2024-11-01"
            )
        )
    )
    private val budgetFlow = MutableStateFlow(
        listOf(BudgetGoal(id = 1, category = "Salario", limit = 800.0, iconKey = "salary"))
    )

    private val transactionRepository = mockk<TransactionRepository> {
        every { observeTransactions() } returns transactionFlow
    }
    private val budgetRepository = mockk<BudgetRepository> {
        every { observeBudgets() } returns budgetFlow
    }
    private val insightsRepository = mockk<InsightsRepository>()

    @Test
    fun `refresh merges remote with locally generated insights`() = runTest {
        val remoteInsight = FinancialInsight(
            id = "remote-risk",
            title = "Riesgo remoto",
            message = "Mensaje desde servicio",
            category = InsightCategory.WARNING
        )
        coEvery { insightsRepository.fetchInsights(any(), any(), any()) } answers {
            val local = firstArg<List<FinancialInsight>>()
            InsightsResult(
                insights = (local + remoteInsight).distinctBy { it.id },
                fromCache = false,
                errorMessage = null
            )
        }

        val viewModel = InsightsViewModel(transactionRepository, budgetRepository, insightsRepository)

        advanceUntilIdle()
        viewModel.refreshInsights(forceRefresh = true)
        advanceUntilIdle()

        val ids = viewModel.uiState.value.insights.map { it.id }
        assertTrue(ids.contains("remote-risk"))
        assertTrue(ids.contains("savings"))
        assertTrue(ids.contains("investment"))
        assertEquals(ids.size, ids.distinct().size)
        assertFalse(viewModel.uiState.value.isOffline)
    }

    @Test
    fun `uses cached fallback when remote fails`() = runTest {
        coEvery { insightsRepository.fetchInsights(any(), any(), any()) } answers {
            val local = firstArg<List<FinancialInsight>>()
            InsightsResult(
                insights = local,
                fromCache = true,
                errorMessage = "cache"
            )
        }

        val viewModel = InsightsViewModel(transactionRepository, budgetRepository, insightsRepository)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isOffline)
        assertEquals(2, state.insights.size)
        assertTrue(state.insights.all { it.id == "savings" || it.id == "investment" })
    }
}
