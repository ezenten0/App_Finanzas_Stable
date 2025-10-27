package com.example.app_finanzas.data.transaction

import com.example.app_finanzas.data.local.transaction.TransactionEntity
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
                date = "2024-10-05"
            ),
            TransactionEntity(
                id = 2,
                title = "Supermercado",
                description = "Compra semanal",
                amount = 210.5,
                type = TransactionTypeMapper.toStorage(TransactionType.EXPENSE),
                category = "Alimentos",
                date = "2024-10-06"
            ),
            TransactionEntity(
                id = 3,
                title = "Freelance diseño",
                description = "Proyecto UX/UI",
                amount = 380.0,
                type = TransactionTypeMapper.toStorage(TransactionType.INCOME),
                category = "Freelance",
                date = "2024-10-07"
            ),
            TransactionEntity(
                id = 4,
                title = "Suscripción streaming",
                description = "Plan familiar",
                amount = 12.99,
                type = TransactionTypeMapper.toStorage(TransactionType.EXPENSE),
                category = "Entretenimiento",
                date = "2024-10-08"
            ),
            TransactionEntity(
                id = 5,
                title = "Cena con amigos",
                description = "Restaurante centro",
                amount = 48.25,
                type = TransactionTypeMapper.toStorage(TransactionType.EXPENSE),
                category = "Social",
                date = "2024-10-08"
            ),
            TransactionEntity(
                id = 6,
                title = "Intereses cuenta",
                description = "Rendimiento mensual",
                amount = 25.75,
                type = TransactionTypeMapper.toStorage(TransactionType.INCOME),
                category = "Inversiones",
                date = "2024-10-09"
            )
        )
    }
}

/**
 * Maps transaction types between storage (String) and domain (enum) so we keep
 * a user friendly representation in Compose.
 */
object TransactionTypeMapper {
    fun toStorage(type: TransactionType): String = type.name

    fun fromStorage(value: String): TransactionType = runCatching {
        TransactionType.valueOf(value)
    }.getOrDefault(TransactionType.EXPENSE)
}
