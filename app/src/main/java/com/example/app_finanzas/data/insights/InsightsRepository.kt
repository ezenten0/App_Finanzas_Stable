package com.example.app_finanzas.data.insights

import com.example.app_finanzas.data.budget.BudgetGoal
import com.example.app_finanzas.data.insights.cloud.InsightsCloudDataSource
import com.example.app_finanzas.data.insights.MonthlyInsightsAggregate
import com.example.app_finanzas.data.insights.remote.RemoteBudgetSnapshotDto
import com.example.app_finanzas.data.insights.remote.RemoteInsightsRequest
import com.example.app_finanzas.data.insights.remote.toDomain
import com.example.app_finanzas.home.analytics.AggregatedInsightGenerator
import com.example.app_finanzas.home.analytics.FinancialInsight
import com.example.app_finanzas.network.RiskServiceApi
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDate
import java.util.concurrent.TimeUnit

class InsightsRepository(
    private val riskServiceApi: RiskServiceApi,
    private val cloudDataSource: InsightsCloudDataSource? = null,
    private val userIdProvider: () -> String? = { FirebaseAuth.getInstance().currentUser?.uid }
) {
    private var cachedInsights: List<FinancialInsight> = emptyList()
    private var lastFetchTimestamp: Long = 0L
    private val cacheTtlMillis = TimeUnit.MINUTES.toMillis(5)

    suspend fun refreshFromRemote(budgets: List<BudgetGoal> = emptyList()) {
        val dataSource = cloudDataSource ?: return
        runCatching {
            val monthKey = dataSource.currentMonthKey()
            val aggregate = dataSource.getMonthlyAggregate(monthKey)
            if (aggregate != null) {
                val budgetSnapshots = calculateBudgetSnapshots(aggregate, budgets)
                cachedInsights = AggregatedInsightGenerator.buildInsights(aggregate, budgetSnapshots)
                lastFetchTimestamp = System.currentTimeMillis()
            }
        }
    }

    suspend fun fetchInsights(
        budgets: List<BudgetGoal>,
        forceRefresh: Boolean = false,
        localFallback: List<FinancialInsight> = emptyList()
    ): InsightsResult {
        val now = System.currentTimeMillis()
        val monthKey = cloudDataSource?.currentMonthKey() ?: defaultMonthKey()
        val aggregate = runCatching { cloudDataSource?.getMonthlyAggregate(monthKey) }.getOrNull()
        val budgetSnapshots = calculateBudgetSnapshots(aggregate, budgets)

        if (aggregate != null) {
            val aggregatedInsights = AggregatedInsightGenerator.buildInsights(aggregate, budgetSnapshots)
            cachedInsights = aggregatedInsights
            lastFetchTimestamp = now
            return InsightsResult(aggregatedInsights, fromCache = false, errorMessage = null)
        }

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
            val userId = userIdProvider() ?: error("Usuario no autenticado")
            val response = riskServiceApi.getInsights(
                RemoteInsightsRequest(
                    userId = userId,
                    budgets = budgetSnapshots.map { it.toRemoteDto() }
                )
            )
            val mapped = response.insights.map { it.toDomain() }
            val merged = mergeWithLocal(mapped, localFallback)
            cachedInsights = merged
            lastFetchTimestamp = now
            InsightsResult(insights = merged, fromCache = false, errorMessage = null)
        } catch (error: Exception) {
            val fallbackData = when {
                cachedInsights.isNotEmpty() -> cachedInsights
                aggregate != null -> AggregatedInsightGenerator.buildInsights(aggregate, budgetSnapshots)
                else -> localFallback
            }
            val merged = mergeWithLocal(fallbackData, localFallback)
            cachedInsights = merged
            InsightsResult(
                insights = merged,
                fromCache = true,
                errorMessage = error.localizedMessage ?: "No fue posible sincronizar los insights",
            )
        }
    }

    private fun mergeWithLocal(
        remote: List<FinancialInsight>,
        localFallback: List<FinancialInsight>
    ): List<FinancialInsight> {
        return (remote + localFallback).distinctBy { it.id }
    }

    private fun calculateBudgetSnapshots(
        aggregate: MonthlyInsightsAggregate?,
        budgets: List<BudgetGoal>
    ): List<BudgetSnapshot> {
        if (budgets.isEmpty()) return emptyList()
        val expensesByCategory = aggregate?.expensesByCategory ?: emptyMap()
        return budgets.map { goal ->
            val spent = expensesByCategory[goal.category] ?: 0.0
            val progress = if (goal.limit == 0.0) 0.0 else spent / goal.limit
            BudgetSnapshot(
                category = goal.category,
                limit = goal.limit,
                spent = spent,
                progress = progress
            )
        }
    }

    private fun defaultMonthKey(): String {
        return LocalDate.now().let { date -> "${date.year}-${"%02d".format(date.monthValue)}" }
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
    val progress: Double,
)

private fun BudgetSnapshot.toRemoteDto(): RemoteBudgetSnapshotDto {
    return RemoteBudgetSnapshotDto(
        category = category,
        limit = limit,
        spent = spent,
        progress = progress
    )
}
