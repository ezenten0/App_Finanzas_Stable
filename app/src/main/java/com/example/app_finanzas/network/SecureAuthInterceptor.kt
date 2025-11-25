package com.example.app_finanzas.network

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
        val request = chain.request()
            .newBuilder()
            .header("Authorization", "Bearer ${tokenProvider.provideToken()}")
            .build()
        return chain.proceed(request)
    }
}
