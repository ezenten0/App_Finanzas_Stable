package com.example.app_finanzas.data

import com.example.app_finanzas.data.local.transaction.TransactionDao
import io.mockk.mockk
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class TransactionRepositoryMockTest {

    @Disabled("TODO: Implement MockK verification once the microservice/API layer is ready")
    @Test
    fun shouldMockRemoteAndDatabaseLayers() {
        // Mock representing the transaction DAO or a REST gateway so we can verify
        // how the repository interacts with storage services in microservices.
        mockk<TransactionDao>()
    }
}
