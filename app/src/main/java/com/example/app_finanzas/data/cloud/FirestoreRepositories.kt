package com.example.app_finanzas.data.cloud

import com.example.app_finanzas.data.budget.BudgetGoal
import com.example.app_finanzas.data.insights.cloud.FirestoreInsightsDataSource
import com.example.app_finanzas.data.insights.cloud.FirestoreMonthlyInsightsDocument
import com.example.app_finanzas.data.transaction.TransactionTypeMapper
import com.example.app_finanzas.data.transaction.calculateMonthKey
import com.example.app_finanzas.data.transaction.toCents
import com.example.app_finanzas.home.model.Transaction
import com.example.app_finanzas.home.model.TransactionType
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

private const val USERS_COLLECTION = "users"
private const val TRANSACTIONS_COLLECTION = "transactions"
private const val BUDGETS_COLLECTION = "budgets"

class FirestoreTransactionRepository(
    private val firestore: FirebaseFirestore,
    private val userId: String
) : CloudTransactionRepository {

    private val insightsDataSource = FirestoreInsightsDataSource(firestore)

    override fun observeTransactions(): Flow<CloudTransactionDelta> = callbackFlow {
        val listener: ListenerRegistration = firestore
            .userScope(userId)
            .collection(TRANSACTIONS_COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(CloudTransactionDelta(emptyList(), emptyList()))
                    return@addSnapshotListener
                }

                val changes = snapshot?.documentChanges ?: emptyList()
                val upserts = mutableListOf<Transaction>()
                val deletions = mutableListOf<Int>()

                for (change in changes) {
                    when (change.type) {
                        com.google.firebase.firestore.DocumentChange.Type.REMOVED -> {
                            change.document.getLong("id")?.toInt()?.let { deletions.add(it) }
                        }
                        else -> {
                            change.document.toObject(FirestoreTransactionDocument::class.java)
                                .toDomain()
                                .let { upserts.add(it) }
                        }
                    }
                }

                trySend(CloudTransactionDelta(upserts, deletions))
            }

        awaitClose { listener.remove() }
    }

    override suspend fun downloadTransactions(): List<Transaction> {
        val snapshot = firestore
            .userScope(userId)
            .collection(TRANSACTIONS_COLLECTION)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(FirestoreTransactionDocument::class.java)?.toDomain()
        }
    }

    override suspend fun upsertTransaction(transaction: Transaction): Transaction {
        val resolvedId = transaction.id.takeIf { it != 0 } ?: generateStableId()
        val document = FirestoreTransactionDocument.fromDomain(transaction.copy(id = resolvedId))

        val transactionRef = firestore
            .userScope(userId)
            .collection(TRANSACTIONS_COLLECTION)
            .document(resolvedId.toString())

        firestore.runTransaction { tx ->
            // ✅ 1) LECTURAS PRIMERO
            val previous = tx.get(transactionRef)
                .toObject(FirestoreTransactionDocument::class.java)

            // ✅ 3) ESCRITURA AL FINAL
            tx.set(transactionRef, document)

            Unit
        }.await()

        return document.toDomain()
    }

    override suspend fun deleteTransaction(transactionId: Int) {
        val transactionRef = firestore
            .userScope(userId)
            .collection(TRANSACTIONS_COLLECTION)
            .document(transactionId.toString())

        firestore.runTransaction { tx ->
            // ✅ 1) LECTURAS PRIMERO
            val existing = tx.get(transactionRef)
                .toObject(FirestoreTransactionDocument::class.java)

            // ✅ 3) ESCRITURA AL FINAL
            tx.delete(transactionRef)

            Unit
        }.await()
    }

    private fun updateAggregates(
        tx: com.google.firebase.firestore.Transaction,
        previous: FirestoreTransactionDocument?,
        current: FirestoreTransactionDocument?
    ) {
        val deltasByRef =
            linkedMapOf<DocumentReference, MutableList<Pair<FirestoreTransactionDocument, Int>>>()
        val monthKeyByRef = linkedMapOf<DocumentReference, String>()

        fun addDelta(doc: FirestoreTransactionDocument, sign: Int) {
            if (doc.monthKey.isBlank()) return
            val ref = insightsDataSource.aggregateRef(doc.monthKey) ?: return
            deltasByRef.getOrPut(ref) { mutableListOf() }.add(doc to sign)
            monthKeyByRef.putIfAbsent(ref, doc.monthKey)
        }

        if (previous != null) addDelta(previous, -1)
        if (current != null) addDelta(current, +1)

        if (deltasByRef.isEmpty()) return

        // ✅ LECTURAS: todas primero
        val baseAggByRef = linkedMapOf<DocumentReference, FirestoreMonthlyInsightsDocument>()
        for ((ref, _) in deltasByRef) {
            val monthKey = monthKeyByRef[ref].orEmpty()
            val base = tx.get(ref).toObject(FirestoreMonthlyInsightsDocument::class.java)
                ?: FirestoreMonthlyInsightsDocument(monthKey = monthKey)
            baseAggByRef[ref] = base
        }

        // ✅ ESCRITURAS: todas después
        for ((ref, deltas) in deltasByRef) {
            var agg = baseAggByRef.getValue(ref)
            for ((doc, sign) in deltas) {
                agg = agg.applyDelta(doc, sign = sign)
            }
            tx.set(ref, agg)
        }
    }
}

class FirestoreBudgetRepository(
    private val firestore: FirebaseFirestore,
    private val userId: String
) : CloudBudgetRepository {

    override suspend fun downloadBudgets(): List<BudgetGoal> {
        val snapshot = firestore
            .userScope(userId)
            .collection(BUDGETS_COLLECTION)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(FirestoreBudgetDocument::class.java)?.toDomain()
        }
    }

    override suspend fun upsertBudget(goal: BudgetGoal): BudgetGoal {
        val resolvedId = goal.id.takeIf { it != 0 } ?: generateStableId()
        val document = FirestoreBudgetDocument.fromDomain(goal.copy(id = resolvedId))

        firestore
            .userScope(userId)
            .collection(BUDGETS_COLLECTION)
            .document(resolvedId.toString())
            .set(document)
            .await()

        return document.toDomain()
    }

    override suspend fun deleteBudget(id: Int) {
        firestore
            .userScope(userId)
            .collection(BUDGETS_COLLECTION)
            .document(id.toString())
            .delete()
            .await()
    }
}

private fun FirebaseFirestore.userScope(userId: String) =
    collection(USERS_COLLECTION).document(userId)

private fun generateStableId(): Int = UUID.randomUUID().hashCode()

data class FirestoreTransactionDocument(
    val id: Int = 0,
    val title: String = "",
    val description: String = "",
    val amount: Double = 0.0,
    val type: String = TransactionType.EXPENSE.name,
    val category: String = "",
    val date: String = "",
    val monthKey: String = ""
) {
    fun toDomain(): Transaction {
        return Transaction(
            id = id,
            title = title,
            description = description,
            amountCents = amount.toCents(),
            type = TransactionTypeMapper.fromStorage(type),
            category = category,
            date = date,
            monthKey = monthKey.ifBlank { calculateMonthKey(date) }
        )
    }

    companion object {
        fun fromDomain(transaction: Transaction): FirestoreTransactionDocument {
            return FirestoreTransactionDocument(
                id = transaction.id,
                title = transaction.title,
                description = transaction.description,
                amount = transaction.amount,
                type = TransactionTypeMapper.toStorage(transaction.type),
                category = transaction.category,
                date = transaction.date,
                monthKey = transaction.monthKey.ifBlank { calculateMonthKey(transaction.date) }
            )
        }
    }
}

data class FirestoreBudgetDocument(
    val id: Int = 0,
    val category: String = "",
    val limit: Double = 0.0,
    val iconKey: String = ""
) {
    fun toDomain(): BudgetGoal {
        return BudgetGoal(
            id = id,
            category = category,
            limit = limit,
            iconKey = iconKey
        )
    }

    companion object {
        fun fromDomain(goal: BudgetGoal): FirestoreBudgetDocument {
            return FirestoreBudgetDocument(
                id = goal.id,
                category = goal.category,
                limit = goal.limit,
                iconKey = goal.iconKey
            )
        }
    }
}
