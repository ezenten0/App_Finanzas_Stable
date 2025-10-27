package com.example.app_finanzas.data.local.budget

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Persists a configurable budget goal per category so every user can tailor
 * their own saving objectives instead of relying on fixed defaults.
 */
@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val category: String,
    val limit: Double,
    val iconKey: String
)
