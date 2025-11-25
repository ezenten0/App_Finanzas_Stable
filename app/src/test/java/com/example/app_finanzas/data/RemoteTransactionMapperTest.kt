package com.example.app_finanzas.data

import com.example.app_finanzas.data.local.transaction.TransactionEntity
import com.example.app_finanzas.data.remote.RemoteTransactionDto
import com.example.app_finanzas.data.remote.toEntity
import com.example.app_finanzas.data.remote.toRemoteDto
import com.example.app_finanzas.data.sync.SyncStatus
import com.example.app_finanzas.data.transaction.TransactionTypeMapper
import com.example.app_finanzas.home.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RemoteTransactionMapperTest {

    @Test
    fun `toEntity keeps payload and marks row as synced`() {
        val dto = RemoteTransactionDto(
            id = null,
            title = "Dividendos",
            description = "Pago mensual",
            amount = 150.5,
            type = "income",
            category = "Inversiones",
            date = "2024-11-01"
        )

        val entity = dto.toEntity()

        assertEquals(0, entity.id)
        assertEquals("Dividendos", entity.title)
        assertEquals(150.5, entity.amount, 0.0)
        assertEquals(TransactionTypeMapper.toStorage(TransactionType.INCOME), entity.type)
        assertEquals(SyncStatus.SYNCED, entity.syncStatus)
    }

    @Test
    fun `toRemoteDto normalizes type and removes placeholder ids`() {
        val entity = TransactionEntity(
            id = 0,
            title = "Renta",
            description = "Pago mensual",
            amount = 400.0,
            type = "income",
            category = "Hogar",
            date = "2024-11-05",
            syncStatus = SyncStatus.PENDING_UPLOAD
        )

        val remote = entity.toRemoteDto()

        assertNull(remote.id)
        assertEquals("Renta", remote.title)
        assertEquals("CREDIT", remote.type)
    }
}
