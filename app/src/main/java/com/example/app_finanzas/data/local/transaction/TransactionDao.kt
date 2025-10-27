package com.example.app_finanzas.data.local.transaction

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object that exposes all persistence operations for transactions.
 * The DAO relies on reactive [Flow] queries so that the UI stays in sync with
 * any changes in the database automatically.
 */
@Dao
interface TransactionDao {

    /**
     * Streams all transactions ordered by their insertion id, newest first, so the
     * dashboard and history screens can react to updates in real time.
     */
    @Query("SELECT * FROM transactions ORDER BY id DESC")
    fun observeTransactions(): Flow<List<TransactionEntity>>

    /**
     * Retrieves a single transaction by its identifier which powers the detail screen.
     */
    @Query("SELECT * FROM transactions WHERE id = :transactionId")
    suspend fun getTransactionById(transactionId: Int): TransactionEntity?

    /**
     * Counts the number of records; used to seed the database the first time the
     * application runs.
     */
    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun countTransactions(): Int

    /**
     * Inserts or updates a list of transactions, enabling bulk seeding or sync operations.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTransaction(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTransactions(transactions: List<TransactionEntity>)

    @Query("DELETE FROM transactions WHERE id = :transactionId")
    suspend fun deleteTransaction(transactionId: Int)

    @Query("SELECT DISTINCT category FROM transactions ORDER BY category ASC")
    fun observeCategories(): Flow<List<String>>
}
