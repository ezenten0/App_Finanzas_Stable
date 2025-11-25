package com.example.app_finanzas.network

data class RealtimeConnectionMetrics(
    val reconnectAttempts: Int = 0,
    val successfulConnections: Int = 0,
    val lastError: String? = null,
    val currentBackoffMs: Long = NetworkConfig.realtimeFallbackIntervalMs / 4
)
