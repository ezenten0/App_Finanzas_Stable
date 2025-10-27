package com.example.app_finanzas.data.local.transaction

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.app_finanzas.data.local.AppDatabase
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests that verify the persistence contract for transactions.
 */
@RunWith(AndroidJUnit4::class)
class TransactionDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: TransactionDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.transactionDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndRetrieveTransactions() = runBlocking {
        val entities = listOf(
            TransactionEntity(id = 1, title = "Pago", description = "Salario", amount = 1000.0, type = "INCOME", category = "Salario", date = "2024-10-01"),
            TransactionEntity(id = 2, title = "Gasto", description = "Cine", amount = 50.0, type = "EXPENSE", category = "Entretenimiento", date = "2024-10-02")
        )

        dao.upsertTransactions(entities)

        val count = dao.countTransactions()
        val retrieved = dao.getTransactionById(1)

        assertEquals(2, count)
        assertEquals("Pago", retrieved?.title)
    }
}
