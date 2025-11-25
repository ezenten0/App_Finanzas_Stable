package com.example.app_finanzas.data.remote

import com.example.app_finanzas.data.budget.BudgetGoal
import com.example.app_finanzas.data.local.budget.BudgetEntity
import com.example.app_finanzas.data.sync.SyncStatus

/**
 * DTO representation of budgets exchanged with the backend.
 */
data class RemoteBudgetDto(
    val id: Int?,
    val category: String,
    val limit: Double,
    val iconKey: String
)

fun RemoteBudgetDto.toEntity(): BudgetEntity {
    return BudgetEntity(
        id = id ?: 0,
        category = category,
        limit = limit,
        iconKey = iconKey,
        syncStatus = SyncStatus.SYNCED
    )
}

fun BudgetGoal.toRemoteDto(): RemoteBudgetDto {
    return RemoteBudgetDto(
        id = if (id == 0) null else id,
        category = category,
        limit = limit,
        iconKey = iconKey
    )
}

fun BudgetEntity.toRemoteDto(): RemoteBudgetDto {
    return RemoteBudgetDto(
        id = if (id == 0) null else id,
        category = category,
        limit = limit,
        iconKey = iconKey
    )
}
