package com.example.app_finanzas.data.budget

import com.example.app_finanzas.categories.CategoryDefinitions
import com.example.app_finanzas.data.local.budget.BudgetDao
import com.example.app_finanzas.data.local.budget.BudgetEntity
import com.example.app_finanzas.data.remote.RemoteBudgetAlertEvent
import com.example.app_finanzas.data.remote.toEntity
import com.example.app_finanzas.data.remote.toRemoteDto
import com.example.app_finanzas.data.sync.SyncStatus
import com.example.app_finanzas.home.analytics.BudgetProgress
import com.example.app_finanzas.network.FinanceServiceApi
import com.example.app_finanzas.network.NetworkState
import com.example.app_finanzas.network.RiskServiceApi
import com.example.app_finanzas.network.withNetworkRetry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Repository coordinating every budget persistence operation and exposing UI
 * friendly models for the presentation layer.
 */
class BudgetRepository(
    private val budgetDao: BudgetDao,
    private val financeServiceApi: FinanceServiceApi,
    private val riskServiceApi: RiskServiceApi
) {
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _networkState = MutableStateFlow<NetworkState>(NetworkState.Idle)
    val networkState: StateFlow<NetworkState> = _networkState
    private val triggeredAlerts = MutableStateFlow<Map<String, BudgetAlertLevel>>(emptyMap())

    fun observeBudgets(): Flow<List<BudgetGoal>> {
        return budgetDao.observeBudgets().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun refreshFromRemote() {
        _networkState.emit(NetworkState.Loading("Sincronizando presupuestos"))
        runCatching {
            val remote = withNetworkRetry { financeServiceApi.getBudgets() }
            budgetDao.upsertBudgets(remote.map { it.toEntity() })
            pushPendingBudgets()
        }.onSuccess {
            _networkState.emit(NetworkState.Success("Presupuestos sincronizados"))
        }.onFailure { error ->
            _networkState.emit(NetworkState.Error(error.localizedMessage ?: "Error de red"))
        }
    }

    suspend fun getBudgetById(id: Int): BudgetGoal? {
        return withContext(Dispatchers.IO) {
            budgetDao.getBudgetById(id)?.toDomain()
        }
    }

    suspend fun upsertBudget(goal: BudgetGoal): Int {
        return withContext(Dispatchers.IO) {
            val entity = goal.toEntity(syncStatus = SyncStatus.PENDING_UPLOAD)
            val id = budgetDao.upsertBudget(entity).toInt()
            val resolvedId = if (entity.id != 0) entity.id else id
            repositoryScope.launch { pushRemoteUpsert(entity.copy(id = resolvedId)) }
            resolvedId
        }
    }

    suspend fun deleteBudget(id: Int) {
        withContext(Dispatchers.IO) {
            val existing = budgetDao.getBudgetById(id)
            if (existing == null) {
                budgetDao.deleteBudget(id)
            } else {
                budgetDao.upsertBudget(existing.copy(syncStatus = SyncStatus.PENDING_DELETE))
                repositoryScope.launch { pushRemoteDelete(id) }
            }
        }
    }

    suspend fun ensureSeedData(defaults: List<BudgetGoal> = BudgetDefaults.defaultGoals()) {
        withContext(Dispatchers.IO) {
            val current = budgetDao.observeBudgets().first()
            if (current.isEmpty()) {
                defaults.forEach { budgetDao.upsertBudget(it.toEntity(syncStatus = SyncStatus.SYNCED)) }
            }
        }
    }

    private suspend fun pushPendingBudgets() {
        val pending = budgetDao.getPendingBudgets()
        pending.forEach { budget ->
            when (budget.syncStatus) {
                SyncStatus.PENDING_UPLOAD -> pushRemoteUpsert(budget)
                SyncStatus.PENDING_DELETE -> pushRemoteDelete(budget.id)
                else -> {}
            }
        }
    }

    private suspend fun pushRemoteUpsert(entity: BudgetEntity) {
        try {
            val response = withNetworkRetry { financeServiceApi.upsertBudget(entity.toRemoteDto()) }
            budgetDao.upsertBudget(response.toEntity())
            _networkState.emit(NetworkState.Success("Presupuesto sincronizado"))
        } catch (error: Throwable) {
            _networkState.emit(NetworkState.Error(error.localizedMessage ?: "Error de red"))
            budgetDao.updateSyncStatus(entity.id, SyncStatus.PENDING_UPLOAD)
        }
    }

    private suspend fun pushRemoteDelete(id: Int) {
        try {
            withNetworkRetry { financeServiceApi.deleteBudget(id) }
            budgetDao.deleteBudget(id)
            _networkState.emit(NetworkState.Success("Presupuesto eliminado"))
        } catch (error: Throwable) {
            _networkState.emit(NetworkState.Error(error.localizedMessage ?: "No se pudo eliminar"))
            budgetDao.updateSyncStatus(id, SyncStatus.PENDING_DELETE)
        }
    }

    fun trackBudgetAlerts(progress: List<BudgetProgress>, userId: String = "mobile-user") {
        val activeCategories = progress.map { it.category }.toSet()
        triggeredAlerts.update { alerts -> alerts.filterKeys { it in activeCategories } }

        progress.forEach { item ->
            val currentLevel = determineAlertLevel(item.progress)
            val previousLevel = triggeredAlerts.value[item.category] ?: BudgetAlertLevel.NONE

            if (currentLevel.priority > previousLevel.priority) {
                triggeredAlerts.update { alerts ->
                    alerts + (item.category to currentLevel)
                }
                repositoryScope.launch {
                    sendBudgetAlert(userId = userId, progress = item, level = currentLevel)
                }
            } else if (currentLevel == BudgetAlertLevel.NONE && previousLevel != BudgetAlertLevel.NONE) {
                triggeredAlerts.update { alerts ->
                    alerts - item.category
                }
            }
        }
    }

    private fun determineAlertLevel(progress: Double): BudgetAlertLevel {
        return when {
            progress >= 1.0 -> BudgetAlertLevel.CRITICAL
            progress >= 0.75 -> BudgetAlertLevel.WARNING
            else -> BudgetAlertLevel.NONE
        }
    }

    private suspend fun sendBudgetAlert(
        userId: String,
        progress: BudgetProgress,
        level: BudgetAlertLevel
    ) {
        val threshold = if (level == BudgetAlertLevel.CRITICAL) 1.0 else 0.75
        val payload = RemoteBudgetAlertEvent(
            userId = userId,
            category = progress.category,
            limit = progress.limit,
            spent = progress.spent,
            progress = progress.progress,
            threshold = threshold
        )

        runCatching { riskServiceApi.sendBudgetAlert(payload) }
            .onFailure { error ->
                _networkState.emit(NetworkState.Error(error.localizedMessage ?: "No se pudo notificar alerta"))
            }
    }

    private fun BudgetEntity.toDomain(): BudgetGoal {
        return BudgetGoal(
            id = id,
            category = category,
            limit = limit,
            iconKey = iconKey
        )
    }

    private fun BudgetGoal.toEntity(syncStatus: SyncStatus): BudgetEntity {
        return BudgetEntity(
            id = id,
            category = category.trim(),
            limit = limit,
            iconKey = iconKey,
            syncStatus = syncStatus
        )
    }
}

/**
 * Lightweight domain model consumed by the Budgets screen.
 */
data class BudgetGoal(
    val id: Int = 0,
    val category: String,
    val limit: Double,
    val iconKey: String
)

/**
 * Provides a curated list of starting budgets so the Budgets screen feels rich
 * on a fresh install while still allowing full customization afterwards.
 */
object BudgetDefaults {
    fun defaultGoals(): List<BudgetGoal> {
        return listOf(
            BudgetGoal(category = "Alimentos", limit = 300.0, iconKey = CategoryDefinitions.FOOD),
            BudgetGoal(category = "Entretenimiento", limit = 120.0, iconKey = CategoryDefinitions.ENTERTAINMENT),
            BudgetGoal(category = "Social", limit = 150.0, iconKey = CategoryDefinitions.SOCIAL),
            BudgetGoal(category = "Inversiones", limit = 200.0, iconKey = CategoryDefinitions.INVESTMENTS)
        )
    }
}

private enum class BudgetAlertLevel(val priority: Int) {
    NONE(0),
    WARNING(1),
    CRITICAL(2)
}
