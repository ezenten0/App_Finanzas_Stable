package com.example.app_finanzas.insights

import com.example.app_finanzas.MainDispatcherRule
import com.example.app_finanzas.data.budget.BudgetGoal
import com.example.app_finanzas.data.budget.BudgetRepository
import com.example.app_finanzas.data.insights.InsightsRepository
import com.example.app_finanzas.data.insights.InsightsResult
import com.example.app_finanzas.home.analytics.FinancialInsight
import com.example.app_finanzas.home.analytics.InsightCategory
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

    private val budgetFlow = MutableStateFlow(
        listOf(BudgetGoal(id = 1, category = "Salario", limit = 800.0, iconKey = "salary"))
    )

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
        coEvery { insightsRepository.fetchInsights(any(), any(), any()) } returns InsightsResult(
            insights = listOf(remoteInsight),
            fromCache = false,
            errorMessage = null
        )

        val viewModel = InsightsViewModel(budgetRepository, insightsRepository)

        advanceUntilIdle()
        viewModel.refreshInsights(forceRefresh = true)
        advanceUntilIdle()

        val ids = viewModel.uiState.value.insights.map { it.id }
        assertTrue(ids.contains("remote-risk"))
        assertEquals(ids.size, ids.distinct().size)
        assertFalse(viewModel.uiState.value.isOffline)
    }

    @Test
    fun `uses cached fallback when remote fails`() = runTest {
        val cached = listOf(
            FinancialInsight(
                id = "cached",
                title = "Desde caché",
                message = "mensaje",
                category = InsightCategory.SAVINGS
            )
        )
        coEvery { insightsRepository.fetchInsights(any(), any(), any()) } returns InsightsResult(
            insights = cached,
            fromCache = true,
            errorMessage = "cache"
        )

        val viewModel = InsightsViewModel(budgetRepository, insightsRepository)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isOffline)
        assertEquals(1, state.insights.size)
        assertEquals("cached", state.insights.first().id)
    }
}
