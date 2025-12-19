package com.example.app_finanzas.data.insights.cloud

import com.example.app_finanzas.data.insights.MonthlyInsightsAggregate
import com.example.app_finanzas.data.transaction.calculateMonthKey
import com.example.app_finanzas.data.cloud.FirestoreTransactionDocument
import com.example.app_finanzas.home.model.TransactionType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

private const val INSIGHTS_COLLECTION = "insights"
private const val MONTHLY_COLLECTION = "monthly"

class FirestoreInsightsDataSource(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : InsightsCloudDataSource {

    private val currentUserId: String?
        get() = auth.currentUser?.uid

    override suspend fun getMonthlyAggregate(monthKey: String): MonthlyInsightsAggregate? {
        val userId = currentUserId ?: return null
        val document = firestore
            .userScope(userId)
            .collection(INSIGHTS_COLLECTION)
            .document(MONTHLY_COLLECTION)
            .collection(MONTHLY_COLLECTION)
            .document(monthKey)
            .get()
            .await()

        return document.toObject(FirestoreMonthlyInsightsDocument::class.java)?.toDomain()
    }

    override suspend fun currentMonthKey(): String {
        return calculateMonthKey(java.time.LocalDate.now().toString())
    }

    internal fun aggregateRef(monthKey: String) = currentUserId?.let { userId ->
        firestore
            .userScope(userId)
            .collection(INSIGHTS_COLLECTION)
            .document(MONTHLY_COLLECTION)
            .collection(MONTHLY_COLLECTION)
            .document(monthKey)
    }
}

private fun FirebaseFirestore.userScope(userId: String) = collection("users").document(userId)

internal data class FirestoreMonthlyInsightsDocument(
    val monthKey: String = "",
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val expensesByCategory: Map<String, Double> = emptyMap(),
    val incomesByCategory: Map<String, Double> = emptyMap()
) {
    fun toDomain(): MonthlyInsightsAggregate {
        return MonthlyInsightsAggregate(
            monthKey = monthKey,
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            expensesByCategory = expensesByCategory,
            incomesByCategory = incomesByCategory
        )
    }

    fun applyDelta(transaction: FirestoreTransactionDocument, sign: Int): FirestoreMonthlyInsightsDocument {
        val isIncome = transaction.type.lowercase() == TransactionType.INCOME.name.lowercase()
        val amount = transaction.amount * sign
        val updatedExpenses = if (isIncome) {
            expensesByCategory
        } else {
            expensesByCategory.updateCategory(transaction.category, amount)
        }
        val updatedIncomes = if (isIncome) {
            incomesByCategory.updateCategory(transaction.category, amount)
        } else {
            incomesByCategory
        }

        val incomeTotal = if (isIncome) totalIncome + amount else totalIncome
        val expenseTotal = if (isIncome) totalExpense else totalExpense + amount

        return copy(
            totalIncome = incomeTotal.coerceAtLeast(0.0),
            totalExpense = expenseTotal.coerceAtLeast(0.0),
            expensesByCategory = updatedExpenses.filterValues { it > 0 },
            incomesByCategory = updatedIncomes.filterValues { it > 0 }
        )
    }

    private fun Map<String, Double>.updateCategory(category: String, delta: Double): Map<String, Double> {
        val newValue = (this[category] ?: 0.0) + delta
        return this + (category to newValue.coerceAtLeast(0.0))
    }
}

interface InsightsCloudDataSource {
    suspend fun getMonthlyAggregate(monthKey: String): MonthlyInsightsAggregate?
    suspend fun currentMonthKey(): String
}
