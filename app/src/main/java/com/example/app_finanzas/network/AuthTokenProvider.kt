package com.example.app_finanzas.network

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GetTokenResult
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Simple abstraction that supplies the bearer token used across REST and
 * realtime channels. In a real project this could be backed by encrypted
 * preferences or the Android keystore.
 */
fun interface AuthTokenProvider {
    suspend fun provideToken(): String?
}

/**
 * Retrieves the Firebase ID token for the current user and caches it until it
 * expires. Subsequent callers reuse the cached value to avoid extra network
 * calls, while expired tokens trigger a refresh to keep requests authorized.
 */
class FirebaseAuthTokenProvider(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) : AuthTokenProvider {

    private val tokenMutex = Mutex()

    @Volatile
    private var cachedToken: String? = null

    @Volatile
    private var expirationTimestampMs: Long = 0L

    override suspend fun provideToken(): String? {
        val now = System.currentTimeMillis()
        val token = cachedToken
        if (token != null && now < expirationTimestampMs - TOKEN_REFRESH_GRACE_MS) {
            return token
        }

        return tokenMutex.withLock {
            val refreshedNow = System.currentTimeMillis()
            val cached = cachedToken
            if (cached != null && refreshedNow < expirationTimestampMs - TOKEN_REFRESH_GRACE_MS) {
                return@withLock cached
            }

            val user = firebaseAuth.currentUser ?: return@withLock null
            val result = runCatching { fetchIdToken(user) }.getOrNull() ?: return@withLock null
            val newToken = result.token
            if (newToken != null) {
                cachedToken = newToken
                expirationTimestampMs = result.expirationTimestamp * 1_000
            }
            newToken
        }
    }

    private suspend fun fetchIdToken(user: FirebaseUser): GetTokenResult =
        suspendCancellableCoroutine { continuation ->
            user.getIdToken(false)
                .addOnSuccessListener { continuation.resume(it) }
                .addOnFailureListener { continuation.resumeWithException(it) }
                .addOnCanceledListener { continuation.cancel() }
        }

    private companion object {
        const val TOKEN_REFRESH_GRACE_MS = 60_000
    }
}
