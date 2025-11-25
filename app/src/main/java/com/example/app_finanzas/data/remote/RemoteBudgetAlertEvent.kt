package com.example.app_finanzas.data.remote

import com.squareup.moshi.Json

data class RemoteBudgetAlertEvent(
    @Json(name = "user_id") val userId: String,
    @Json(name = "category") val category: String,
    @Json(name = "limit") val limit: Double,
    @Json(name = "spent") val spent: Double,
    @Json(name = "progress") val progress: Double,
    @Json(name = "threshold") val threshold: Double
)
