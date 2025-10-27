package com.example.app_finanzas.data.transaction

import com.example.app_finanzas.data.local.transaction.TransactionDao
import com.example.app_finanzas.data.local.transaction.TransactionEntity
import com.example.app_finanzas.home.model.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Repository that orchestrates transaction persistence and exposes the domain
 * models used by the UI layer. The repository encapsulates all mapping logic
 * between Room entities and Compose-friendly models so the presentation layer
 * remains lightweight.
 */
class TransactionRepository(
    private val transactionDao: TransactionDao
) {

    /**
     * Observes every transaction stored locally and transforms the result into
     * UI models for immediate consumption by the different screens.
     */
    fun observeTransactions(): Flow<List<Transaction>> {
        return transactionDao.observeTransactions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun observeCategories(): Flow<List<String>> {
        return transactionDao.observeCategories()
    }

    /**
     * Ensures the database contains a baseline data set; this keeps the demo
     * experience rich without requiring the user to add manual entries first.
     */
    suspend fun ensureSeedData() {
        withContext(Dispatchers.IO) {
            if (transactionDao.countTransactions() == 0) {
                transactionDao.upsertTransactions(TransactionSamples.defaultTransactions())
            }
        }
    }

    /**
     * Retrieves a single transaction for the detail screen, returning null if
     * the identifier no longer exists in the database.
     */
    suspend fun getTransactionById(transactionId: Int): Transaction? {
        return withContext(Dispatchers.IO) {
            transactionDao.getTransactionById(transactionId)?.toDomain()
        }
    }

    /**
     * Inserts or updates a single transaction and returns the resulting id so the
     * UI can react to the new entry immediately.
     */
    suspend fun upsertTransaction(transaction: Transaction): Int {
        return withContext(Dispatchers.IO) {
            val entity = transaction.toEntity()
            val generatedId = transactionDao.upsertTransaction(entity).toInt()
            if (entity.id != 0) entity.id else generatedId
        }
    }

    /**
     * Inserts a transaction list, typically used by tests or future sync flows.
     */
    suspend fun upsertTransactions(transactions: List<Transaction>) {
        withContext(Dispatchers.IO) {
            val entities = transactions.map { it.toEntity() }
            transactionDao.upsertTransactions(entities)
        }
    }

    /**
     * Removes a transaction when the user decides to discard it.
     */
    suspend fun deleteTransaction(transactionId: Int) {
        withContext(Dispatchers.IO) {
            transactionDao.deleteTransaction(transactionId)
        }
    }

    private fun TransactionEntity.toDomain(): Transaction {
        return Transaction(
            id = id,
            title = title,
            description = description,
            amount = amount,
            type = TransactionTypeMapper.fromStorage(type),
            category = category,
            date = date
        )
    }

    private fun Transaction.toEntity(): TransactionEntity {
        return TransactionEntity(
            id = id,
            title = title,
            description = description,
            amount = amount,
            type = TransactionTypeMapper.toStorage(type),
            category = category,
            date = date
        )
    }
}
