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
        assertEquals("income", TransactionTypeMapper.toStorage(TransactionType.INCOME))
        assertEquals("expense", TransactionTypeMapper.toStorage(TransactionType.EXPENSE))
    }

    @Test
    fun `fromStorage understands backend values`() {
        assertEquals(TransactionType.INCOME, TransactionTypeMapper.fromStorage("income"))
        assertEquals(TransactionType.EXPENSE, TransactionTypeMapper.fromStorage("expense"))
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
            amountCents = 100_000,
            type = TransactionType.INCOME,
            category = "Income",
            date = "2024-10-10",
            monthKey = "2024-10"
        )

        val remote = transaction.toRemoteDto()
        val entity = remote.toEntity()

        assertEquals("income", remote.type)
        assertEquals("income", entity.type)
        assertEquals(TransactionType.INCOME, TransactionTypeMapper.fromStorage(entity.type))
    }

    @Test
    fun `round trip to remote dto keeps expense as debit`() {
        val transaction = Transaction(
            id = 1,
            title = "Groceries",
            description = "Weekly shopping",
            amountCents = 15_000,
            type = TransactionType.EXPENSE,
            category = "Food",
            date = "2024-10-11",
            monthKey = "2024-10"
        )

        val remote = transaction.toRemoteDto()
        val entity = remote.toEntity()

        assertEquals("expense", remote.type)
        assertEquals("expense", entity.type)
        assertEquals(TransactionType.EXPENSE, TransactionTypeMapper.fromStorage(entity.type))
    }
}
