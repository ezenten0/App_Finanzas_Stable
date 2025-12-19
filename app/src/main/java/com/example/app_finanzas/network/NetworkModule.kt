package com.example.app_finanzas.network

import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.create
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Builds the OkHttp and Retrofit clients used to communicate with the EFT
 * microservices.
 */
object NetworkModule {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private fun defaultHeadersInterceptor(): Interceptor {
        return Interceptor { chain ->
            val request = chain.request()
                .newBuilder()
                .header("Accept", "application/json")
                .header("User-Agent", "Finanzas3/1.0")
                .build()
            chain.proceed(request)
        }
    }

    private fun loggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    private fun clientErrorLoggingInterceptor(): Interceptor {
        return Interceptor { chain ->
            val request = chain.request()
            val response = chain.proceed(request)
            if (response.code >= 400) {
                val preview = response.peekBody(2_048).string()
                Log.w(
                    "FinanceHttp",
                    "HTTP ${response.code} for ${request.method} ${request.url}. Body preview: $preview"
                )
            }
            response
        }
    }

    private fun timingInterceptor(): Interceptor {
        return Interceptor { chain ->
            val request = chain.request()
            val start = System.nanoTime()
            try {
                val response = chain.proceed(request)
                val durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start)
                Log.i(
                    "FinanceHttp",
                    "${request.method} ${request.url} -> ${response.code} in ${durationMs}ms"
                )
                response
            } catch (error: Exception) {
                val durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start)
                Log.e(
                    "FinanceHttp",
                    "${request.method} ${request.url} failed after ${durationMs}ms",
                    error
                )
                throw error
            }
        }
    }

    private fun buildHttpClient(tokenProvider: AuthTokenProvider = FirebaseAuthTokenProvider()): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .addInterceptor(defaultHeadersInterceptor())
            .addInterceptor(SecureAuthInterceptor(tokenProvider))
            .addInterceptor(timingInterceptor())
            .addInterceptor(clientErrorLoggingInterceptor())
            .addInterceptor(loggingInterceptor())
            .build()
    }

    private fun buildRetrofit(baseUrl: String, tokenProvider: AuthTokenProvider = FirebaseAuthTokenProvider()): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(buildHttpClient(tokenProvider))
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    fun provideRiskServiceApi(): RiskServiceApi {
        val retrofit = buildRetrofit(NetworkConfig.RISK_SERVICE_BASE_URL)
        return retrofit.create()
    }

    fun provideFinanceServiceApi(
        tokenProvider: AuthTokenProvider = FirebaseAuthTokenProvider()
    ): FinanceServiceApi {
        val retrofit = buildRetrofit(NetworkConfig.FINANCE_SERVICE_BASE_URL, tokenProvider)
        return retrofit.create()
    }
}
