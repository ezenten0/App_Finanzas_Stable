package com.example.app_finanzas.data.transaction

import com.example.app_finanzas.data.cloud.CloudTransactionRepository
import com.example.app_finanzas.data.cloud.CloudTransactionDelta
import com.example.app_finanzas.data.local.transaction.TransactionDao
import com.example.app_finanzas.data.local.transaction.TransactionEntity
import com.example.app_finanzas.data.remote.RemoteTransactionDto
import com.example.app_finanzas.data.remote.toEntity
import com.example.app_finanzas.data.remote.toRemoteDto
import com.example.app_finanzas.data.sync.SyncStatus
import com.example.app_finanzas.home.model.Transaction
import com.example.app_finanzas.network.FinanceServiceApi
import com.example.app_finanzas.network.NetworkState
import com.example.app_finanzas.network.withNetworkRetry
import com.example.app_finanzas.data.transaction.calculateMonthKey
import kotlin.math.abs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Repository that orchestrates transaction persistence and exposes the domain
 * models used by the UI layer. The repository encapsulates all mapping logic
 * between Room entities and Compose-friendly models so the presentation layer
 * remains lightweight.
 */
class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val financeServiceApi: FinanceServiceApi,
    private val cloudRepository: CloudTransactionRepository? = null
) {
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _networkState = MutableStateFlow<NetworkState>(NetworkState.Idle)
    val networkState: StateFlow<NetworkState> = _networkState

    init {
        if (cloudRepository != null) {
            observeRemoteSnapshots()
            repositoryScope.launch { refreshFromRemote() }
        } else {
            observeRealtimeUpdates()
        }
    }

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
                if (cloudRepository != null) {
                    seedCloudTransactions()
                } else {
                    transactionDao.upsertTransactions(TransactionSamples.defaultTransactions())
                }
            }
        }
    }

    suspend fun clearLocalData() {
        withContext(Dispatchers.IO) {
            transactionDao.deleteAll()
        }
    }

    /**
     * Synchronizes the local cache with the remote API, merging remote-first data
     * with any pending offline operations.
     */
    suspend fun refreshFromRemote() {
        _networkState.emit(NetworkState.Loading("Sincronizando transacciones"))
        runCatching {
            val entities = if (cloudRepository != null) {
                val remote = cloudRepository.downloadTransactions()
                if (remote.isEmpty()) {
                    seedCloudTransactions()
                } else {
                    remote.map { it.toEntity(syncStatus = SyncStatus.SYNCED) }
                }
            } else {
                withNetworkRetry { financeServiceApi.getTransactions() }.map { it.toEntity() }
            }
            if (entities.isNotEmpty()) {
                transactionDao.upsertTransactions(entities)
            }
            if (cloudRepository == null) {
                pushPendingChanges()
            }
        }.onSuccess {
            _networkState.emit(NetworkState.Success("Transacciones sincronizadas"))
        }.onFailure { error ->
            _networkState.emit(NetworkState.Error(error.localizedMessage ?: "Fallo de red"))
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
    suspend fun upsertTransaction(transaction: Transaction): Int = withContext(Dispatchers.IO) {
        if (cloudRepository != null) {
            val persisted = cloudRepository.upsertTransaction(transaction)
            val entity = persisted.toEntity(syncStatus = SyncStatus.SYNCED)
            transactionDao.upsertTransaction(entity)
            return@withContext entity.id
        }

        val dto = transaction.toRemoteDto()
        val remote: RemoteTransactionDto = if (transaction.id == 0) {
            financeServiceApi.upsertTransaction(dto)
        } else {
            financeServiceApi.updateTransaction(transaction.id, dto)
        }
        val entity: TransactionEntity = remote.toEntity().copy(syncStatus = SyncStatus.SYNCED)
        transactionDao.upsertTransaction(entity)
        entity.id
    }

    /**
     * Inserts a transaction list, typically used by tests or future sync flows.
     */
    suspend fun upsertTransactions(transactions: List<Transaction>) {
        withContext(Dispatchers.IO) {
            val entities = transactions.map { it.toEntity(syncStatus = SyncStatus.PENDING_UPLOAD) }
            transactionDao.upsertTransactions(entities)
            repositoryScope.launch { pushPendingChanges() }
        }
    }

    /**
     * Removes a transaction when the user decides to discard it.
     */
    suspend fun deleteTransaction(transactionId: Int) {
        withContext(Dispatchers.IO) {
            if (cloudRepository != null) {
                cloudRepository.deleteTransaction(transactionId)
                transactionDao.deleteTransaction(transactionId)
                return@withContext
            }
            val existing = transactionDao.getTransactionById(transactionId)
            if (existing == null) {
                transactionDao.deleteTransaction(transactionId)
                return@withContext
            }
            if (existing.id == 0) {
                transactionDao.deleteTransaction(transactionId)
            } else {
                val pendingDeletion = existing.copy(syncStatus = SyncStatus.PENDING_DELETE)
                transactionDao.upsertTransaction(pendingDeletion)
                repositoryScope.launch { pushRemoteDelete(pendingDeletion.id) }
            }
        }
    }

    private suspend fun pushPendingChanges() {
        val pending = transactionDao.getPendingTransactions()
        pending.forEach { entity ->
            when (entity.syncStatus) {
                SyncStatus.PENDING_UPLOAD -> pushRemoteUpsert(entity)
                SyncStatus.PENDING_DELETE -> pushRemoteDelete(entity.id)
                else -> {}
            }
        }
    }

    private suspend fun pushRemoteUpsert(entity: TransactionEntity) {
        try {
            val response = withNetworkRetry {
                if (entity.id == 0) {
                    financeServiceApi.upsertTransaction(entity.toRemoteDto())
                } else {
                    financeServiceApi.updateTransaction(entity.id, entity.toRemoteDto())
                }
            }
            transactionDao.upsertTransaction(response.toEntity())
            _networkState.emit(NetworkState.Success("Transacción sincronizada"))
        } catch (error: Throwable) {
            _networkState.emit(NetworkState.Error(error.localizedMessage ?: "Sin conexión"))
            transactionDao.updateSyncStatus(entity.id, SyncStatus.PENDING_UPLOAD)
        }
    }

    private suspend fun pushRemoteDelete(transactionId: Int) {
        try {
            withNetworkRetry { financeServiceApi.deleteTransaction(transactionId) }
            transactionDao.deleteTransaction(transactionId)
            _networkState.emit(NetworkState.Success("Transacción eliminada"))
        } catch (error: Throwable) {
            _networkState.emit(NetworkState.Error(error.localizedMessage ?: "No se pudo borrar"))
            transactionDao.updateSyncStatus(transactionId, SyncStatus.PENDING_DELETE)
        }
    }

    private fun observeRemoteSnapshots() {
        val cloudFlow = cloudRepository?.observeTransactions() ?: return
        repositoryScope.launch {
            cloudFlow.collect { delta ->
                applyCloudDelta(delta)
            }
        }
    }

    private suspend fun applyCloudDelta(delta: CloudTransactionDelta) {
        if (delta.upserts.isNotEmpty()) {
            val entities = delta.upserts.map { it.toEntity(syncStatus = SyncStatus.SYNCED) }
            transactionDao.upsertTransactions(entities)
        }
        if (delta.deletedIds.isNotEmpty()) {
            delta.deletedIds.forEach { transactionDao.deleteTransaction(it) }
        }
    }

    private fun TransactionEntity.toDomain(): Transaction {
        return Transaction(
            id = id,
            title = title,
            description = description,
            amountCents = abs(amountCents),
            type = TransactionTypeMapper.fromStorage(type),
            category = category,
            date = date,
            monthKey = monthKey.ifBlank { calculateMonthKey(date) }
        )
    }

    private fun Transaction.toEntity(syncStatus: SyncStatus): TransactionEntity {
        return TransactionEntity(
            id = id,
            title = title,
            description = description,
            amountCents = abs(amountCents),
            type = TransactionTypeMapper.toStorage(type),
            category = category,
            date = date,
            monthKey = monthKey.ifBlank { calculateMonthKey(date) },
            syncStatus = syncStatus
        )
    }

    private suspend fun seedCloudTransactions(): List<TransactionEntity> {
        val seeds = TransactionSamples.defaultTransactions()
        val syncedSeeds = cloudRepository?.let { repository ->
            seeds.map { entity ->
                val persisted = repository.upsertTransaction(entity.toDomain())
                persisted.toEntity(syncStatus = SyncStatus.SYNCED)
            }
        }.orEmpty()

        if (syncedSeeds.isNotEmpty()) {
            transactionDao.upsertTransactions(syncedSeeds)
        }

        return syncedSeeds
    }

    /**
     * Starts the legacy SSE-based updates used by the REST backend. This is
     * intentionally skipped whenever a [CloudTransactionRepository]
     * implementation is present (e.g. Firestore) to avoid duplicate realtime
     * listeners.
     */
    private fun observeRealtimeUpdates() {
        // No-op placeholder for future SSE stream wiring when not using
        // Firestore.
    }
}
