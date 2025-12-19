package com.example.app_finanzas.data.transaction

import com.example.app_finanzas.data.local.transaction.TransactionEntity
import com.example.app_finanzas.data.sync.SyncStatus
import com.example.app_finanzas.home.model.TransactionType

/**
 * Utility responsible for providing an initial list of transactions so the home
 * dashboard showcases meaningful information even on a clean install.
 */
object TransactionSamples {

    /**
     * Generates the initial sample entries that are seeded the first time the
     * database is created.
     */
    fun defaultTransactions(): List<TransactionEntity> {
        return listOf(
            TransactionEntity(
                id = 1,
                title = "Pago de salario",
                description = "Depósito mensual de tu trabajo",
                amountCents = 145_000,
                type = TransactionTypeMapper.toStorage(TransactionType.INCOME),
                category = "Salario",
                date = "2024-10-05",
                monthKey = "2024-10",
                syncStatus = SyncStatus.SYNCED
            ),
            TransactionEntity(
                id = 2,
                title = "Supermercado",
                description = "Compra semanal",
                amountCents = 21_050,
                type = TransactionTypeMapper.toStorage(TransactionType.EXPENSE),
                category = "Alimentos",
                date = "2024-10-06",
                monthKey = "2024-10",
                syncStatus = SyncStatus.SYNCED
            ),
            TransactionEntity(
                id = 3,
                title = "Freelance diseño",
                description = "Proyecto UX/UI",
                amountCents = 38_000,
                type = TransactionTypeMapper.toStorage(TransactionType.INCOME),
                category = "Freelance",
                date = "2024-10-07",
                monthKey = "2024-10",
                syncStatus = SyncStatus.SYNCED
            ),
            TransactionEntity(
                id = 4,
                title = "Suscripción streaming",
                description = "Plan familiar",
                amountCents = 1_299,
                type = TransactionTypeMapper.toStorage(TransactionType.EXPENSE),
                category = "Entretenimiento",
                date = "2024-10-08",
                monthKey = "2024-10",
                syncStatus = SyncStatus.SYNCED
            ),
            TransactionEntity(
                id = 5,
                title = "Cena con amigos",
                description = "Restaurante centro",
                amountCents = 4_825,
                type = TransactionTypeMapper.toStorage(TransactionType.EXPENSE),
                category = "Social",
                date = "2024-10-08",
                monthKey = "2024-10",
                syncStatus = SyncStatus.SYNCED
            ),
            TransactionEntity(
                id = 6,
                title = "Intereses cuenta",
                description = "Rendimiento mensual",
                amountCents = 2_575,
                type = TransactionTypeMapper.toStorage(TransactionType.INCOME),
                category = "Inversiones",
                date = "2024-10-09",
                monthKey = "2024-10",
                syncStatus = SyncStatus.SYNCED
            )
        )
    }
}

/**
 * Maps transaction types between storage (String) and domain (enum) so we keep
 * a user friendly representation in Compose.
 */
object TransactionTypeMapper {
    private const val INCOME = "income"
    private const val EXPENSE = "expense"

    /**
     * Converts a domain type into the canonical value expected by the backend
     * (`CREDIT`/`DEBIT`). Legacy aliases from the database are normalized to
     * avoid leaking UI wording to the API.
     */
    fun toStorage(type: TransactionType): String = when (type) {
        TransactionType.INCOME -> INCOME
        TransactionType.EXPENSE -> EXPENSE
    }

    /**
     * Translates storage or network values into domain enums. Both the
     * canonical API values (`CREDIT`/`DEBIT`) and the legacy domain names
     * (`INCOME`/`EXPENSE`) are supported to keep backwards compatibility with
     * existing data while honoring the contract expected by ledger-service.
     */
    fun fromStorage(value: String): TransactionType {
        return when (value.lowercase()) {
            INCOME, TransactionType.INCOME.name.lowercase(), "credit" -> TransactionType.INCOME
            EXPENSE, TransactionType.EXPENSE.name.lowercase(), "debit" -> TransactionType.EXPENSE
            else -> TransactionType.EXPENSE
        }
    }
}
