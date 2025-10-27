package com.example.app_finanzas.data.local.transaction

import androidx.room.Entity
import androidx.room.PrimaryKey

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
    val amount: Double,
    val type: String,
    val category: String,
    val date: String
)
