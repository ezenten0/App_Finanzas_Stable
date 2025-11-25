package com.example.app_finanzas.data.remote

import com.example.app_finanzas.data.local.transaction.TransactionEntity
import com.example.app_finanzas.data.sync.SyncStatus
import com.example.app_finanzas.home.model.Transaction
import com.example.app_finanzas.data.transaction.TransactionTypeMapper

/**
 * DTO that represents transactions in the remote API. Mapping helpers are
 * provided to convert between Room entities and domain models.
 */
data class RemoteTransactionDto(
    val id: Int?,
    val title: String,
    val description: String,
    val amount: Double,
    val type: String,
    val category: String,
    val date: String
)

fun RemoteTransactionDto.toEntity(): TransactionEntity {
    val canonicalType = TransactionTypeMapper.toStorage(TransactionTypeMapper.fromStorage(type))
    return TransactionEntity(
        id = id ?: 0,
        title = title,
        description = description,
        amount = amount,
        type = canonicalType,
        category = category,
        date = date,
        syncStatus = SyncStatus.SYNCED
    )
}

fun TransactionEntity.toRemoteDto(): RemoteTransactionDto {
    val canonicalType = TransactionTypeMapper.toStorage(TransactionTypeMapper.fromStorage(type))
    return RemoteTransactionDto(
        id = if (id == 0) null else id,
        title = title,
        description = description,
        amount = amount,
        type = canonicalType,
        category = category,
        date = date
    )
}

fun Transaction.toRemoteDto(): RemoteTransactionDto {
    return RemoteTransactionDto(
        id = if (id == 0) null else id,
        title = title,
        description = description,
        amount = amount,
        type = TransactionTypeMapper.toStorage(type),
        category = category,
        date = date
    )
}
