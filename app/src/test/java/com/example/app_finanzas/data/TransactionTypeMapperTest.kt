package com.example.app_finanzas.data

import com.example.app_finanzas.data.remote.RemoteTransactionDto
import com.example.app_finanzas.data.remote.toEntity
import com.example.app_finanzas.data.remote.toRemoteDto
import com.example.app_finanzas.data.transaction.TransactionTypeMapper
import com.example.app_finanzas.home.model.Transaction
import com.example.app_finanzas.home.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Test

class TransactionTypeMapperTest {

    @Test
    fun `toStorage sends credit and debit to backend`() {
        assertEquals("CREDIT", TransactionTypeMapper.toStorage(TransactionType.INCOME))
        assertEquals("DEBIT", TransactionTypeMapper.toStorage(TransactionType.EXPENSE))
    }

    @Test
    fun `fromStorage understands backend values`() {
        assertEquals(TransactionType.INCOME, TransactionTypeMapper.fromStorage("CREDIT"))
        assertEquals(TransactionType.EXPENSE, TransactionTypeMapper.fromStorage("DEBIT"))
    }

    @Test
    fun `fromStorage keeps support for legacy aliases`() {
        assertEquals(TransactionType.INCOME, TransactionTypeMapper.fromStorage("INCOME"))
        assertEquals(TransactionType.EXPENSE, TransactionTypeMapper.fromStorage("EXPENSE"))
        assertEquals(TransactionType.EXPENSE, TransactionTypeMapper.fromStorage("unknown"))
    }

    @Test
    fun `round trip to remote dto keeps income as credit`() {
        val transaction = Transaction(
            id = 0,
            title = "Salary",
            description = "Monthly paycheck",
            amount = 1000.0,
            type = TransactionType.INCOME,
            category = "Income",
            date = "2024-10-10"
        )

        val remote = transaction.toRemoteDto()
        val entity = remote.toEntity()

        assertEquals("CREDIT", remote.type)
        assertEquals("CREDIT", entity.type)
        assertEquals(TransactionType.INCOME, TransactionTypeMapper.fromStorage(entity.type))
    }

    @Test
    fun `round trip to remote dto keeps expense as debit`() {
        val transaction = Transaction(
            id = 1,
            title = "Groceries",
            description = "Weekly shopping",
            amount = 150.0,
            type = TransactionType.EXPENSE,
            category = "Food",
            date = "2024-10-11"
        )

        val remote = transaction.toRemoteDto()
        val entity = remote.toEntity()

        assertEquals("DEBIT", remote.type)
        assertEquals("DEBIT", entity.type)
        assertEquals(TransactionType.EXPENSE, TransactionTypeMapper.fromStorage(entity.type))
    }
}
