package com.example.app_finanzas.transactions.loading

import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test

class TransactionLoadingTest {

    @Test
    fun simulateTransactionLoadingAdvancesVirtualTime() = runTest {
        val expectedDelay = 1_500L

        val actualDelay = simulateTransactionLoading(expectedDelay)

        assertEquals(expectedDelay, actualDelay)
        assertEquals(expectedDelay, currentTime)
    }
}
