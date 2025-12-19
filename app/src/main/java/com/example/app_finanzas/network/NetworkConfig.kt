package com.example.app_finanzas.network

import com.example.app_finanzas.BuildConfig

/**
 * Centralizes network-related constants used across Retrofit clients.
 */
object NetworkConfig {

    /**
     * Primary backend (ledger-service) for CRUD synchronization.
     */
    val FINANCE_SERVICE_BASE_URL: String
        get() = BuildConfig.LEDGER_BASE_URL.ensureTrailingSlash()

    /**
     * risk-service: expone /api/v1/insights en el puerto 8081.
     */
    val RISK_SERVICE_BASE_URL: String
        get() = BuildConfig.RISK_BASE_URL.ensureTrailingSlash()

    /**
     * notification-service: por si luego consumes algo directo de ahí.
     */
    val NOTIFICATION_SERVICE_BASE_URL: String
        get() = BuildConfig.NOTIF_BASE_URL.ensureTrailingSlash()
}

private fun String.ensureTrailingSlash(): String {
    return if (endsWith("/")) this else "$this/"
}
