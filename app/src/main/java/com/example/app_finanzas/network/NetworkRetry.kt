package com.example.app_finanzas.network

import kotlinx.coroutines.delay
import kotlin.math.pow

/**
 * Basic retry helper with exponential backoff so remote calls tolerate
 * intermittent connectivity while remaining bounded.
 */
suspend fun <T> withNetworkRetry(
    retries: Int = 3,
    initialDelayMillis: Long = 500,
    block: suspend () -> T
): T {
    var lastError: Throwable? = null
    repeat(retries) { attempt ->
        try {
            return block()
        } catch (error: Throwable) {
            lastError = error
            val backoff = initialDelayMillis * (2.0.pow(attempt.toDouble())).toLong()
            delay(backoff)
        }
    }
    throw lastError ?: IllegalStateException("Unknown network error")
}
