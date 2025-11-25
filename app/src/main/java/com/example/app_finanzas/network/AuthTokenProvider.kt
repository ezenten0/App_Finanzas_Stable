package com.example.app_finanzas.network

/**
 * Simple abstraction that supplies the bearer token used across REST and
 * realtime channels. In a real project this could be backed by encrypted
 * preferences or the Android keystore.
 */
fun interface AuthTokenProvider {
    fun provideToken(): String
}

/**
 * Default static token used for demo purposes; consumers can replace this with
 * their own provider if they manage dynamic credentials.
 */
object StaticAuthTokenProvider : AuthTokenProvider {
    private const val DEMO_TOKEN = "demo-finanzas-token"
    override fun provideToken(): String = DEMO_TOKEN
}
