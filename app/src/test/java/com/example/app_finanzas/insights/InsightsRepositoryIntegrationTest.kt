package com.example.app_finanzas.insights

import com.example.app_finanzas.data.budget.BudgetGoal
import com.example.app_finanzas.data.insights.InsightsRepository
import com.example.app_finanzas.home.analytics.FinancialInsight
import com.example.app_finanzas.home.analytics.InsightCategory
import com.example.app_finanzas.network.RiskServiceApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create

class InsightsRepositoryIntegrationTest {

    private val mockWebServer = MockWebServer()
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    @Before
    fun setUp() {
        mockWebServer.start()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `preserves local insights when backend returns no alerts`() = runTest {
        val emptyResponse = MockResponse()
            .setResponseCode(200)
            .setBody("{\"insights\":[]}")
            .setHeader("Content-Type", "application/json")
        mockWebServer.enqueue(emptyResponse)

        val api = buildRiskServiceApi()
        val repository = InsightsRepository(api)
        val localInsights = listOf(
            FinancialInsight(
                id = "local-budget",
                title = "Meta offline",
                message = "Insight generado localmente",
                category = InsightCategory.BUDGET
            )
        )

        val result = repository.fetchInsights(budgets = emptyList<BudgetGoal>(), localFallback = localInsights)

        val recorded = mockWebServer.takeRequest()
        assertEquals("/api/v1/insights", recorded.path)
        assertEquals(1, result.insights.count { it.id == "local-budget" })
        assertTrue(result.insights.size == 1)
        assertTrue(result.errorMessage == null)
    }

    private fun buildRiskServiceApi(): RiskServiceApi {
        val client = OkHttpClient.Builder().build()
        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        return retrofit.create()
    }
}
