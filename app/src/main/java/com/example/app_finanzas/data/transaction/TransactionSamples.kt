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
                amount = 1450.0,
                type = TransactionTypeMapper.toStorage(TransactionType.INCOME),
                category = "Salario",
                date = "2024-10-05",
                syncStatus = SyncStatus.SYNCED
            ),
            TransactionEntity(
                id = 2,
                title = "Supermercado",
                description = "Compra semanal",
                amount = 210.5,
                type = TransactionTypeMapper.toStorage(TransactionType.EXPENSE),
                category = "Alimentos",
                date = "2024-10-06",
                syncStatus = SyncStatus.SYNCED
            ),
            TransactionEntity(
                id = 3,
                title = "Freelance diseño",
                description = "Proyecto UX/UI",
                amount = 380.0,
                type = TransactionTypeMapper.toStorage(TransactionType.INCOME),
                category = "Freelance",
                date = "2024-10-07",
                syncStatus = SyncStatus.SYNCED
            ),
            TransactionEntity(
                id = 4,
                title = "Suscripción streaming",
                description = "Plan familiar",
                amount = 12.99,
                type = TransactionTypeMapper.toStorage(TransactionType.EXPENSE),
                category = "Entretenimiento",
                date = "2024-10-08",
                syncStatus = SyncStatus.SYNCED
            ),
            TransactionEntity(
                id = 5,
                title = "Cena con amigos",
                description = "Restaurante centro",
                amount = 48.25,
                type = TransactionTypeMapper.toStorage(TransactionType.EXPENSE),
                category = "Social",
                date = "2024-10-08",
                syncStatus = SyncStatus.SYNCED
            ),
            TransactionEntity(
                id = 6,
                title = "Intereses cuenta",
                description = "Rendimiento mensual",
                amount = 25.75,
                type = TransactionTypeMapper.toStorage(TransactionType.INCOME),
                category = "Inversiones",
                date = "2024-10-09",
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
    private const val CREDIT = "CREDIT"
    private const val DEBIT = "DEBIT"

    /**
     * Converts a domain type into the canonical value expected by the backend
     * (`CREDIT`/`DEBIT`). Legacy aliases from the database are normalized to
     * avoid leaking UI wording to the API.
     */
    fun toStorage(type: TransactionType): String = when (type) {
        TransactionType.INCOME -> CREDIT
        TransactionType.EXPENSE -> DEBIT
    }

    /**
     * Translates storage or network values into domain enums. Both the
     * canonical API values (`CREDIT`/`DEBIT`) and the legacy domain names
     * (`INCOME`/`EXPENSE`) are supported to keep backwards compatibility with
     * existing data while honoring the contract expected by ledger-service.
     */
    fun fromStorage(value: String): TransactionType {
        return when (value.uppercase()) {
            CREDIT, TransactionType.INCOME.name -> TransactionType.INCOME
            DEBIT, TransactionType.EXPENSE.name -> TransactionType.EXPENSE
            else -> TransactionType.EXPENSE
        }
    }
}
