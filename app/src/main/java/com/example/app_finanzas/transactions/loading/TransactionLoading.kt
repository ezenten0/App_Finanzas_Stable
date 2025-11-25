package com.example.app_finanzas.transactions.loading

import kotlinx.coroutines.delay

const val DEFAULT_LOADING_DELAY = 2_000L

/**
 * Small helper used by the transactions flow to simulate the time it takes to
 * fetch the latest movements from a remote microservice or storage backend.
 * It keeps the actual delay isolated so it can be tested deterministically.
 */
suspend fun simulateTransactionLoading(delayMillis: Long = DEFAULT_LOADING_DELAY): Long {
    delay(delayMillis)
    return delayMillis
}
