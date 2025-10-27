@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.app_finanzas.insights

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Report
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.TrendingDown
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.app_finanzas.data.budget.BudgetRepository
import com.example.app_finanzas.data.transaction.TransactionRepository
import com.example.app_finanzas.home.analytics.FinancialInsight
import com.example.app_finanzas.home.analytics.InsightCategory
import com.example.app_finanzas.home.analytics.InsightGenerator

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun InsightsRoute(
    transactionRepository: TransactionRepository,
    budgetRepository: BudgetRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transactions by transactionRepository.observeTransactions().collectAsState(initial = emptyList())
    val budgets by budgetRepository.observeBudgets().collectAsState(initial = emptyList())
    val insights = remember(transactions, budgets) { InsightGenerator.buildInsights(transactions, budgets) }

    InsightsScreen(
        insights = insights,
        onBack = onBack,
        modifier = modifier
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun InsightsScreen(
    insights: List<FinancialInsight>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Centro de insights",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Recomendaciones para crecer tu dinero",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (insights.isEmpty()) {
                item { EmptyInsightsState() }
            } else {
                items(insights, key = { it.id }) { insight ->
                    InsightCard(insight = insight)
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun InsightCard(insight: FinancialInsight) {
    val highlightColor by animateColorAsState(
        targetValue = when (insight.category) {
            InsightCategory.SAVINGS, InsightCategory.OPPORTUNITY -> MaterialTheme.colorScheme.primary
            InsightCategory.EXPENSE -> MaterialTheme.colorScheme.tertiary
            InsightCategory.BUDGET -> MaterialTheme.colorScheme.secondary
            InsightCategory.WARNING -> MaterialTheme.colorScheme.error
        },
        label = "insightColor"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = insight.icon(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(36.dp)
                        .padding(end = 12.dp),
                    tint = highlightColor
                )
                Column {
                    Text(
                        text = insight.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    AnimatedContent(
                        targetState = insight.category,
                        transitionSpec = { fadeIn() with fadeOut() },
                        label = "categoryAnimation"
                    ) { category ->
                        Text(
                            text = category.displayName(),
                            style = MaterialTheme.typography.labelMedium,
                            color = highlightColor
                        )
                    }
                }
            }

            AnimatedContent(
                targetState = insight.message,
                transitionSpec = { fadeIn() with fadeOut() },
                label = "messageAnimation"
            ) { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptyInsightsState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Insights,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Aún no hay recomendaciones",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Registra más movimientos para recibir proyecciones personalizadas.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun FinancialInsight.icon() = when (category) {
    InsightCategory.SAVINGS -> Icons.Rounded.Savings
    InsightCategory.EXPENSE -> Icons.Rounded.TrendingDown
    InsightCategory.BUDGET -> Icons.Rounded.WarningAmber
    InsightCategory.OPPORTUNITY -> Icons.Rounded.Lightbulb
    InsightCategory.WARNING -> Icons.Rounded.Report
}

private fun InsightCategory.displayName(): String = when (this) {
    InsightCategory.SAVINGS -> "Ahorro"
    InsightCategory.EXPENSE -> "Control de gastos"
    InsightCategory.BUDGET -> "Presupuesto"
    InsightCategory.OPPORTUNITY -> "Oportunidad"
    InsightCategory.WARNING -> "Alerta"
}
