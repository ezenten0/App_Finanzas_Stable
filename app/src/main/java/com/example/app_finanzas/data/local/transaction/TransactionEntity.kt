package com.example.app_finanzas.data.local.transaction

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.app_finanzas.data.sync.SyncStatus

/**
 * Room entity that persists each financial transaction the user records or imports.
 * The entity mirrors the UI model so that it can be displayed immediately without
 * complex mapping logic.
 */
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String,
    val amountCents: Long,
    val type: String,
    val category: String,
    val date: String,
    val monthKey: String,
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)
