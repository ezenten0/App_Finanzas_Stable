package com.example.app_finanzas.network

import com.example.app_finanzas.data.remote.RemoteBudgetDto
import com.example.app_finanzas.data.remote.RemoteTransactionDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface FinanceServiceApi {

    @GET("api/transactions")
    suspend fun getTransactions(): List<RemoteTransactionDto>

    @POST("api/transactions")
    suspend fun upsertTransaction(
        @Body transaction: RemoteTransactionDto
    ): RemoteTransactionDto

    @PUT("api/transactions/{id}")
    suspend fun updateTransaction(
        @Path("id") transactionId: Int,
        @Body transaction: RemoteTransactionDto
    ): RemoteTransactionDto


    @DELETE("api/transactions/{id}")
    suspend fun deleteTransaction(
        @Path("id") transactionId: Int
    )

    @GET("api/budgets")
    suspend fun getBudgets(): List<RemoteBudgetDto>

    @POST("api/budgets")
    suspend fun upsertBudget(
        @Body budgetDto: RemoteBudgetDto
    ): RemoteBudgetDto

    @DELETE("api/budgets/{id}")
    suspend fun deleteBudget(
        @Path("id") budgetId: Int
    )
}
