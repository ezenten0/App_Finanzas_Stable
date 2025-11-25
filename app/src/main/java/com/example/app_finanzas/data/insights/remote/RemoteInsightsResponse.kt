package com.example.app_finanzas.data.insights.remote

import com.example.app_finanzas.home.analytics.FinancialInsight
import com.example.app_finanzas.home.analytics.InsightCategory
import com.squareup.moshi.Json

data class RemoteInsightsResponse(
    @Json(name = "insights")
    val insights: List<RemoteInsightDto> = emptyList()
)

data class RemoteInsightsRequest(
    @Json(name = "user_id") val userId: String,
    @Json(name = "budgets") val budgets: List<RemoteBudgetSnapshotDto> = emptyList()
)

data class RemoteBudgetSnapshotDto(
    @Json(name = "category") val category: String,
    @Json(name = "limit") val limit: Double,
    @Json(name = "spent") val spent: Double,
    @Json(name = "progress") val progress: Double
)

data class RemoteInsightDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "title") val title: String = "",
    @Json(name = "message") val message: String? = null,
    @Json(name = "category") val category: String = "",
    @Json(name = "risk_level") val riskLevel: String? = null,
    @Json(name = "action") val action: String? = null,
    @Json(name = "budget_alert") val budgetAlert: RemoteBudgetAlertDto? = null
)

data class RemoteBudgetAlertDto(
    @Json(name = "category") val category: String? = null,
    @Json(name = "limit") val limit: Double? = null,
    @Json(name = "spent") val spent: Double? = null,
    @Json(name = "progress") val progress: Double? = null,
    @Json(name = "status") val status: String? = null
)

fun RemoteInsightDto.toDomain(): FinancialInsight {
    val safeId = id?.ifBlank { null } ?: "remote-${title.hashCode()}"
    val resolvedCategory = InsightCategory.values().firstOrNull {
        it.name.equals(category, ignoreCase = true)
    } ?: when (riskLevel?.lowercase()) {
        "warning", "alto" -> InsightCategory.WARNING
        "budget" -> InsightCategory.BUDGET
        "oportunidad", "opportunity" -> InsightCategory.OPPORTUNITY
        "ahorro", "savings" -> InsightCategory.SAVINGS
        else -> InsightCategory.EXPENSE
    }

    val body = buildString {
        message?.takeIf { it.isNotBlank() }?.let { append(it.trim()) }
        action?.takeIf { it.isNotBlank() }?.let { actionText ->
            if (isNotEmpty()) append(" ")
            append(actionText.trim())
        }
        budgetAlert?.let { alert ->
            val progressText = alert.progress?.times(100)?.toInt()
            val limitText = alert.limit?.let { "${"%,.2f".format(it)}" }
            if (progressText != null) {
                if (isNotEmpty()) append(" ")
                append("(Presupuesto ${alert.category ?: ""}: $progressText% consumido")
                limitText?.let { append(", meta $$it") }
                append(")")
            }
        }
    }.ifBlank { "Sin detalles disponibles." }

    return FinancialInsight(
        id = safeId,
        title = title.ifBlank { resolvedCategory.name.lowercase().replaceFirstChar { it.titlecase() } },
        message = body,
        category = resolvedCategory
    )
}
