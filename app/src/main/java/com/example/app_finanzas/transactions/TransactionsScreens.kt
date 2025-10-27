@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.app_finanzas.transactions

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.app_finanzas.data.transaction.TransactionRepository
import com.example.app_finanzas.home.model.Transaction
import com.example.app_finanzas.home.model.TransactionType
import com.example.app_finanzas.home.model.TransactionType.EXPENSE
import com.example.app_finanzas.home.model.TransactionType.INCOME
import com.example.app_finanzas.ui.icons.CategoryIconByLabel
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlinx.coroutines.launch

private val numberFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale("es", "ES"))

/**
 * High level route that surfaces all transactions persisted in the database.
 */
@Composable
fun TransactionsRoute(
    transactionRepository: TransactionRepository,
    onTransactionSelected: (Int) -> Unit,
    onAddTransaction: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transactions by transactionRepository.observeTransactions().collectAsState(initial = emptyList())
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    TransactionsScreen(
        transactions = transactions,
        onTransactionSelected = onTransactionSelected,
        onAddTransaction = onAddTransaction,
        onDeleteTransaction = { transaction ->
            scope.launch {
                transactionRepository.deleteTransaction(transaction.id)
                snackbarHostState.showSnackbar("Movimiento eliminado: ${transaction.title}")
            }
        },
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

/**
 * Displays the full transaction history with access to the detail screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    transactions: List<Transaction>,
    onTransactionSelected: (Int) -> Unit,
    onAddTransaction: () -> Unit,
    onDeleteTransaction: (Transaction) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    var transactionPendingDeletion by remember { mutableStateOf<Transaction?>(null) }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(text = "Historial de movimientos", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onAddTransaction) {
                        Icon(imageVector = Icons.Rounded.Add, contentDescription = "Añadir movimiento")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (transactions.isEmpty()) {
                item { EmptyTransactionsState(onAddTransaction = onAddTransaction) }
            } else {
                items(transactions, key = { it.id }) { transaction ->
                    TransactionListItem(
                        transaction = transaction,
                        onClick = { onTransactionSelected(transaction.id) },
                        onDelete = { transactionPendingDeletion = transaction }
                    )
                }
            }
        }
    }

    transactionPendingDeletion?.let { transaction ->
        AlertDialog(
            onDismissRequest = { transactionPendingDeletion = null },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteTransaction(transaction)
                    transactionPendingDeletion = null
                }) {
                    Text(text = "Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionPendingDeletion = null }) {
                    Text(text = "Cancelar")
                }
            },
            title = { Text(text = "Eliminar movimiento") },
            text = {
                Text(
                    text = "¿Deseas eliminar \"${transaction.title}\"? Esta acción no se puede deshacer.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        )
    }
}

/**
 * Detail route that loads an individual transaction by id.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TransactionDetailRoute(
    transactionRepository: TransactionRepository,
    transactionId: Int?,
    onBack: () -> Unit,
    onEdit: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val transactionState by produceState<Transaction?>(initialValue = null, transactionId) {
        value = transactionId?.let { transactionRepository.getTransactionById(it) }
    }

    TransactionDetailScreen(
        transaction = transactionState,
        onBack = onBack,
        onEdit = onEdit,
        modifier = modifier
    )
}

/**
 * Renders the transaction details highlighting the amount, category and date.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TransactionDetailScreen(
    transaction: Transaction?,
    onBack: () -> Unit,
    onEdit: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Detalle del movimiento", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    transaction?.let {
                        IconButton(onClick = { onEdit(it.id) }) {
                            Icon(imageVector = Icons.Rounded.Edit, contentDescription = "Editar movimiento")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        if (transaction == null) {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "No se encontró la transacción", style = MaterialTheme.typography.titleMedium)
                Button(onClick = onBack, modifier = Modifier.padding(top = 16.dp)) {
                    Text(text = "Volver")
                }
            }
        } else {
            TransactionDetailContent(
                transaction = transaction,
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            )
        }
    }
}

@Composable
private fun EmptyTransactionsState(onAddTransaction: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Aún no hay movimientos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Registra tu primer ingreso o gasto para comenzar a ver estadísticas.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Button(onClick = onAddTransaction) {
                Text(text = "Registrar movimiento")
            }
        }
    }
}

/**
 * Breaks down the transaction details inside a card for better readability.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun TransactionDetailContent(
    transaction: Transaction,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = transaction.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = transaction.description,
                style = MaterialTheme.typography.bodyLarge
            )
            AmountRow(label = "Monto", value = formatAmount(transaction))
            AmountRow(
                label = "Categoría",
                value = transaction.category,
                leadingIcon = {
                    CategoryIconByLabel(
                        label = transaction.category,
                        contentDescription = transaction.category,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )
            AmountRow(label = "Fecha", value = formatDate(transaction.date))
        }
    }
}

@Composable
private fun AmountRow(
    label: String,
    value: String,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            leadingIcon?.invoke()
            Text(text = value, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/**
 * Item used by the transaction list; it summarizes the entry and is clickable.
 */
@Composable
private fun TransactionListItem(
    transaction: Transaction,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = transaction.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(text = transaction.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Rounded.Delete, contentDescription = "Eliminar movimiento")
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CategoryIconByLabel(
                        label = transaction.category,
                        contentDescription = transaction.category,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(text = transaction.category, style = MaterialTheme.typography.labelLarge)
                }
                Text(text = formatAmount(transaction), style = MaterialTheme.typography.titleMedium, color = amountColor(transaction.type))
            }
        }
    }
}

private fun formatAmount(transaction: Transaction): String {
    val sign = if (transaction.type == INCOME) 1 else -1
    return numberFormat.format(transaction.amount * sign)
}

@Composable
private fun amountColor(type: TransactionType) = if (type == INCOME) {
    MaterialTheme.colorScheme.primary
} else {
    MaterialTheme.colorScheme.error
}

@RequiresApi(Build.VERSION_CODES.O)
private fun formatDate(raw: String): String {
    return runCatching {
        val parsed = LocalDate.parse(raw)
        parsed.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG))
    }.getOrDefault(raw)
}
