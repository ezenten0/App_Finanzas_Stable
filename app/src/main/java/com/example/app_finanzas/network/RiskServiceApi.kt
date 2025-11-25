package com.example.app_finanzas.network

import com.example.app_finanzas.data.insights.remote.RemoteInsightsRequest
import com.example.app_finanzas.data.insights.remote.RemoteInsightsResponse
import com.example.app_finanzas.data.remote.RemoteBudgetAlertEvent
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit definition for the risk-service stub that generates proactive
 * insights and alerts.
 */
interface RiskServiceApi {

    @POST("api/v1/insights")
    suspend fun getInsights(
        @Body request: RemoteInsightsRequest
    ): RemoteInsightsResponse

    @POST("api/v1/budget-alerts")
    suspend fun sendBudgetAlert(
        @Body alert: RemoteBudgetAlertEvent
    )
}
