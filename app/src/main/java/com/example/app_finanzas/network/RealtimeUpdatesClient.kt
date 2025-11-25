package com.example.app_finanzas.network

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.util.concurrent.TimeUnit

/**
 * Listens to real-time updates from the backend using Server-Sent Events. If
 * the stream cannot be established, it falls back to emitting periodic refresh
 * signals so the repository can perform short polling.
 */
class RealtimeUpdatesClient(
    private val tokenProvider: AuthTokenProvider
) {
    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .connectTimeout(5, TimeUnit.SECONDS)
        .build()
    private val _metrics = MutableStateFlow(RealtimeConnectionMetrics())
    val metrics: StateFlow<RealtimeConnectionMetrics> = _metrics

    fun transactionEvents(): Flow<String> = callbackFlow {
        val request = Request.Builder()
            .url(NetworkConfig.REALTIME_TRANSACTIONS_URL)
            .addHeader("Authorization", "Bearer ${tokenProvider.provideToken()}")
            .build()

        val factory = EventSources.createFactory(client)
        val pollingJob = launch {
            while (isActive) {
                delay(NetworkConfig.realtimeFallbackIntervalMs)
                trySend("refresh")
            }
        }

        val initialBackoff = (NetworkConfig.realtimeFallbackIntervalMs / 4).coerceAtLeast(1_000L)
        var reconnectDelay = initialBackoff
        var attempts = 0

        while (isActive) {
            val failureSignal = CompletableDeferred<Unit>()
            attempts += 1
            _metrics.update { stats ->
                stats.copy(
                    reconnectAttempts = attempts,
                    currentBackoffMs = reconnectDelay,
                    lastError = null
                )
            }

            val eventSource = factory.newEventSource(request, object : EventSourceListener() {
                override fun onOpen(eventSource: EventSource, response: okhttp3.Response) {
                    reconnectDelay = initialBackoff
                    _metrics.update { stats ->
                        stats.copy(
                            successfulConnections = stats.successfulConnections + 1,
                            currentBackoffMs = reconnectDelay,
                            lastError = null
                        )
                    }
                }

                override fun onEvent(
                    eventSource: EventSource,
                    id: String?,
                    type: String?,
                    data: String
                ) {
                    trySend(data)
                }

                override fun onFailure(
                    eventSource: EventSource,
                    t: Throwable?,
                    response: okhttp3.Response?
                ) {
                    val reason = t?.localizedMessage ?: "HTTP ${response?.code ?: "desconocido"}"
                    _metrics.update { stats ->
                        stats.copy(
                            lastError = reason,
                            currentBackoffMs = reconnectDelay
                        )
                    }
                    if (!failureSignal.isCompleted) {
                        failureSignal.complete(Unit)
                    }
                }
            })

            try {
                failureSignal.await()
            } finally {
                eventSource.cancel()
            }

            if (!isActive) break

            delay(reconnectDelay)
            reconnectDelay = (reconnectDelay * 2).coerceAtMost(NetworkConfig.realtimeFallbackIntervalMs)
        }

        awaitClose {
            pollingJob.cancel()
        }
    }
}
