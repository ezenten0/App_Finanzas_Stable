package com.example.app_finanzas.insights

import com.example.app_finanzas.data.budget.BudgetGoal
import com.example.app_finanzas.data.insights.InsightsRepository
import com.example.app_finanzas.data.insights.remote.RemoteInsightDto
import com.example.app_finanzas.data.insights.remote.RemoteInsightsRequest
import com.example.app_finanzas.data.insights.remote.RemoteInsightsResponse
import com.example.app_finanzas.data.remote.RemoteBudgetAlertEvent
import com.example.app_finanzas.home.analytics.FinancialInsight
import com.example.app_finanzas.home.analytics.InsightCategory
import com.example.app_finanzas.network.RiskServiceApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

private class FakeRiskServiceApi(var response: RemoteInsightsResponse) : RiskServiceApi {
    var capturedRequest: RemoteInsightsRequest? = null

    override suspend fun getInsights(request: RemoteInsightsRequest): RemoteInsightsResponse {
        capturedRequest = request
        return response
    }

    override suspend fun sendBudgetAlert(alert: RemoteBudgetAlertEvent) {
        TODO("Not yet implemented")
    }
}

class InsightsRepositoryTest {

    @Test
    fun `merge local insights when backend is empty`() = runTest {
        val api = FakeRiskServiceApi(RemoteInsightsResponse(emptyList()))
        val repository = InsightsRepository(api)
        val localInsights = listOf(
            FinancialInsight("local-budget", "Meta local", "Generado offline", InsightCategory.BUDGET)
        )
        val budgets = listOf(BudgetGoal(id = 1, category = "Food", limit = 100.0, iconKey = "food"))

        val result = repository.fetchInsights(budgets = budgets, localFallback = localInsights)

        assertTrue(result.insights.any { it.id == "local-budget" })
        assertEquals(1, api.capturedRequest?.budgets?.size)
    }

    @Test
    fun `merge remote and local without duplicates`() = runTest {
        val apiInsight = RemoteInsightDto(id = "local-budget", title = "Remoto", category = "budget")
        val api = FakeRiskServiceApi(RemoteInsightsResponse(listOf(apiInsight)))
        val repository = InsightsRepository(api)
        val localInsights = listOf(
            FinancialInsight("local-budget", "Local", "Desde el dispositivo", InsightCategory.BUDGET),
            FinancialInsight("savings", "Ahorro", "Local", InsightCategory.SAVINGS)
        )

        val result = repository.fetchInsights(budgets = emptyList(), localFallback = localInsights)

        assertEquals(2, result.insights.size)
        assertEquals(1, result.insights.count { it.id == "local-budget" })
    }
}
