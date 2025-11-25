package com.example.app_finanzas.network

/**
 * Represents the current network operation state so the UI can surface progress
 * and repository layers can coordinate retries.
 */
sealed class NetworkState {
    data object Idle : NetworkState()
    data class Loading(val message: String? = null) : NetworkState()
    data class Success(val message: String? = null) : NetworkState()
    data class Error(val error: String, val recoverable: Boolean = true) : NetworkState()
}
