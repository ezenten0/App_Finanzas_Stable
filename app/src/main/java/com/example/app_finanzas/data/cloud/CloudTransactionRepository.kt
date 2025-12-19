package com.example.app_finanzas.data.cloud

import com.example.app_finanzas.home.model.Transaction
import kotlinx.coroutines.flow.Flow

/**
 * Persists and retrieves transactions from the authenticated user's Firestore
 * namespace.
 */
interface CloudTransactionRepository {
    fun observeTransactions(): Flow<CloudTransactionDelta>

    suspend fun downloadTransactions(): List<Transaction>

    suspend fun upsertTransaction(transaction: Transaction): Transaction

    suspend fun deleteTransaction(transactionId: Int)
}

data class CloudTransactionDelta(
    val upserts: List<Transaction>,
    val deletedIds: List<Int>
)
