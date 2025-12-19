package com.example.app_finanzas.network

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor that injects Authorization headers to every HTTP request so
 * backends can authenticate the mobile app securely.
 */
class SecureAuthInterceptor(
    private val tokenProvider: AuthTokenProvider
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { tokenProvider.provideToken() }
        val requestBuilder = chain.request().newBuilder()
        if (token != null) {
            requestBuilder.header("Authorization", "Bearer $token")
        }
        return chain.proceed(requestBuilder.build())
    }
}
