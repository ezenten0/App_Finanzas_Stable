package com.example.app_finanzas.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app_finanzas.data.transaction.TransactionRepository
import com.example.app_finanzas.home.model.HomeUiState
import com.example.app_finanzas.home.model.Transaction
import com.example.app_finanzas.home.model.TransactionType
import com.example.app_finanzas.ui.icons.CategoryIconByLabel
import com.example.app_finanzas.ui.theme.App_FinanzasTheme
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val currencyFormatter: NumberFormat =
    NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("es").setRegion("ES").build())

/**
 * Entry point composable that wires the [HomeViewModel] to the UI and reacts to
 * user profile updates coming from the authentication flow.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeRoute(
    userName: String,
    userEmail: String,
    transactionRepository: TransactionRepository,
    onAddTransaction: () -> Unit,
    onTransactionSelected: (Int) -> Unit,
    onShowInsights: () -> Unit,
    onShowStatistics: () -> Unit,
    viewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(transactionRepository)
    )
) {
    val state by viewModel.uiState
    LaunchedEffect(userName, userEmail) {
        viewModel.updateUserProfile(userName, userEmail)
    }
    HomeScreen(
        state = state,
        onTransactionSelected = onTransactionSelected,
        onAddTransaction = onAddTransaction,
        onShowInsights = onShowInsights,
        onShowStatistics = onShowStatistics
    )
}

/**
 * Stateless representation of the dashboard that displays the balance summary and
 * the recent transaction history.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    state: HomeUiState,
    modifier: Modifier = Modifier,
    onTransactionSelected: (Int) -> Unit = {},
    onAddTransaction: () -> Unit = {},
    onShowInsights: () -> Unit = {},
    onShowStatistics: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            HomeTopBar(
                userName = state.userName,
                userEmail = state.userEmail,
                onShowInsights = onShowInsights
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddTransaction,
                icon = { Icon(imageVector = Icons.Rounded.Add, contentDescription = "Añadir movimiento") },
                text = { Text(text = "Registrar") },
                elevation = FloatingActionButtonDefaults.elevation(6.dp)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                BalanceSummaryCard(
                    totalBalance = state.totalBalance,
                    totalIncome = state.totalIncome,
                    totalExpense = state.totalExpense,
                    onClick = onShowStatistics
                )
            }
            item {
                TransactionsHeader()
            }
            items(
                items = state.transactions,
                key = { it.id }
            ) { transaction ->
                TransactionCard(
                    transaction = transaction,
                    onClick = { onTransactionSelected(transaction.id) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar(
    userName: String,
    userEmail: String,
    onShowInsights: () -> Unit
) {
    val greetingName = userName.ifBlank { "Usuario" }
    TopAppBar(
        title = {
            Column {
                Text(
                    text = "Hola, $greetingName",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Tu resumen financiero",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                if (userEmail.isNotBlank()) {
                    Text(
                        text = userEmail,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = onShowInsights) {
                Icon(
                    imageVector = Icons.Rounded.Notifications,
                    contentDescription = "Notificaciones"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Composable
@OptIn(ExperimentalAnimationApi::class)
private fun BalanceSummaryCard(
    totalBalance: Double,
    totalIncome: Double,
    totalExpense: Double,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    var isAnimating by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
            .clickable(enabled = !isAnimating) {
                if (isAnimating) return@clickable
                isAnimating = true
                scope.launch {
                    scale.animateTo(0.96f, animationSpec = tween(durationMillis = 140))
                    scale.animateTo(1f, animationSpec = tween(durationMillis = 200))
                    onClick()
                    isAnimating = false
                }
            },
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Balance disponible",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                )
                AnimatedContent(
                    targetState = totalBalance,
                    label = "balanceAnimation"
                ) { value ->
                    Text(
                        text = formatCurrency(value),
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                BalanceItem(
                    label = "Ingresos",
                    amount = totalIncome,
                    icon = Icons.Rounded.ArrowUpward,
                    iconTint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.weight(1f)
                )
                BalanceItem(
                    label = "Gastos",
                    amount = totalExpense,
                    icon = Icons.Rounded.ArrowDownward,
                    iconTint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun BalanceItem(
    label: String,
    amount: Double,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f)
        ),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.size(36.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                ),
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = formatCurrency(amount),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
private fun TransactionsHeader() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Movimientos recientes",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "Un vistazo a tus ingresos y gastos de los últimos días",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Card that displays a single transaction and triggers navigation when tapped.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun TransactionCard(
    transaction: Transaction,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = transaction.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = transaction.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = formatAmountWithSign(transaction),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = amountColor(transaction.type)
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryIconByLabel(
                        label = transaction.category,
                        contentDescription = transaction.category,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = transaction.category,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = formatDateLabel(transaction.date),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun amountColor(type: TransactionType): Color = when (type) {
    TransactionType.INCOME -> MaterialTheme.colorScheme.primary
    TransactionType.EXPENSE -> MaterialTheme.colorScheme.error
}

private fun formatAmountWithSign(transaction: Transaction): String {
    val sign = if (transaction.type == TransactionType.INCOME) "+" else "-"
    return "$sign${formatCurrency(transaction.amount)}"
}

private fun formatCurrency(amount: Double): String {
    return currencyFormatter.format(amount)
}

@RequiresApi(Build.VERSION_CODES.O)
private fun formatDateLabel(raw: String): String {
    return runCatching {
        val parsed = LocalDate.parse(raw)
        val locale = Locale.Builder().setLanguage("es").setRegion("ES").build()
        parsed.format(DateTimeFormatter.ofPattern("d MMM", locale))
    }.getOrDefault(raw)
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    App_FinanzasTheme {
        HomeScreen(
            state = HomeUiState(
                userName = "Laura",
                userEmail = "laura@example.com",
                totalBalance = 1634.26,
                totalIncome = 1855.75,
                totalExpense = 221.49,
                transactions = listOf(
                    Transaction(
                        id = 1,
                        title = "Pago de salario",
                        description = "Depósito mensual de tu trabajo",
                        amount = 1450.0,
                        type = TransactionType.INCOME,
                        category = "Salario",
                        date = "2024-10-05"
                    ),
                    Transaction(
                        id = 2,
                        title = "Supermercado",
                        description = "Compra semanal",
                        amount = 210.5,
                        type = TransactionType.EXPENSE,
                        category = "Alimentos",
                        date = "2024-10-06"
                    )
                )
            )
        )
    }
}
