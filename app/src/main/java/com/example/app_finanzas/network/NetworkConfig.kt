package com.example.app_finanzas.network

/**
 * Centralizes network-related constants used across Retrofit clients.
 */
object NetworkConfig {

    /**
     * Primary backend (ledger-service) for CRUD synchronization.
     * Puerto 8080
     */
    const val FINANCE_SERVICE_BASE_URL = "http://10.0.2.2:8080/"

    /**
     * risk-service: expone /api/v1/insights en el puerto 8081.
     */
    const val RISK_SERVICE_BASE_URL = "http://10.0.2.2:8081/"

    /**
     * notification-service: por si luego consumes algo directo de ahí.
     * Puerto 8082.
     */
    const val NOTIFICATION_SERVICE_BASE_URL = "http://10.0.2.2:8082/"

    /**
     * SSE de cambios de transacciones, publicado por el ledger-service.
     * Termina apuntando a:
     * http://10.0.2.2:8080/api/transactions/stream
     */
    const val REALTIME_TRANSACTIONS_URL =
        FINANCE_SERVICE_BASE_URL + "api/transactions/stream"

    const val realtimeFallbackIntervalMs = 15_000L
    const val realtimeConnectionTimeoutMs = 5_000L
}
