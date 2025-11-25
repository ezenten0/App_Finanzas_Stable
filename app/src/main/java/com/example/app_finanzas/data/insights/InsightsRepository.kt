package com.example.app_finanzas.data.insights

import com.example.app_finanzas.data.insights.remote.RemoteBudgetSnapshotDto
import com.example.app_finanzas.data.insights.remote.RemoteInsightsRequest
import com.example.app_finanzas.data.insights.remote.toDomain
import com.example.app_finanzas.home.analytics.FinancialInsight
import com.example.app_finanzas.network.RiskServiceApi
import java.util.concurrent.TimeUnit

class InsightsRepository(
    private val riskServiceApi: RiskServiceApi
) {
    private var cachedInsights: List<FinancialInsight> = emptyList()
    private var lastFetchTimestamp: Long = 0L
    private val cacheTtlMillis = TimeUnit.MINUTES.toMillis(5)

    suspend fun fetchInsights(
        localFallback: List<FinancialInsight>,
        budgetSnapshots: List<BudgetSnapshot>,
        forceRefresh: Boolean = false
    ): InsightsResult {
        val now = System.currentTimeMillis()
        val canUseCache = !forceRefresh &&
            cachedInsights.isNotEmpty() &&
            now - lastFetchTimestamp < cacheTtlMillis
        if (canUseCache) {
            val mergedCache = mergeWithLocal(cachedInsights, localFallback)
            cachedInsights = mergedCache
            return InsightsResult(
                insights = mergedCache,
                fromCache = true,
                errorMessage = null
            )
        }

        return try {
            val response = riskServiceApi.getInsights(
                RemoteInsightsRequest(
                    userId = "mobile-user",
                    budgets = budgetSnapshots.map { it.toRemoteDto() }
                )
            )
            val mapped = response.insights.map { it.toDomain() }
            val merged = mergeWithLocal(mapped, localFallback)
            cachedInsights = merged
            lastFetchTimestamp = now
            InsightsResult(insights = merged, fromCache = false, errorMessage = null)
        } catch (error: Exception) {
            val fallbackData = if (cachedInsights.isNotEmpty()) cachedInsights else localFallback
            val merged = mergeWithLocal(fallbackData, localFallback)
            cachedInsights = merged
            InsightsResult(
                insights = merged,
                fromCache = true,
                errorMessage = error.localizedMessage ?: "No fue posible sincronizar los insights"
            )
        }
    }

    private fun mergeWithLocal(
        remote: List<FinancialInsight>,
        localFallback: List<FinancialInsight>
    ): List<FinancialInsight> {
        return (remote + localFallback).distinctBy { it.id }
    }
}

data class InsightsResult(
    val insights: List<FinancialInsight>,
    val fromCache: Boolean,
    val errorMessage: String?,
)

data class BudgetSnapshot(
    val category: String,
    val limit: Double,
    val spent: Double,
    val progress: Double
)

private fun BudgetSnapshot.toRemoteDto(): RemoteBudgetSnapshotDto {
    return RemoteBudgetSnapshotDto(
        category = category,
        limit = limit,
        spent = spent,
        progress = progress
    )
}
