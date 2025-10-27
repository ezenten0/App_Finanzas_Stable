package com.example.app_finanzas.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.Alignment
import com.example.app_finanzas.data.transaction.TransactionRepository
import com.example.app_finanzas.home.analytics.BalanceSummary
import com.example.app_finanzas.home.analytics.StatisticsRange
import com.example.app_finanzas.home.analytics.TimeSeriesPoint
import com.example.app_finanzas.home.analytics.TransactionAnalytics
import com.example.app_finanzas.home.model.Transaction
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.max

private val statsNumberFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale("es", "ES"))

/**
 * Route that aggregates the stored transactions into high level statistics.
 */
@Composable
fun StatisticsRoute(
    transactionRepository: TransactionRepository,
    modifier: Modifier = Modifier
) {
    val transactions by transactionRepository.observeTransactions().collectAsState(initial = emptyList())
    StatisticsScreen(transactions = transactions, modifier = modifier)
}

/**
 * Visualizes total balance metrics and the distribution of expenses per category.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    transactions: List<Transaction>,
    modifier: Modifier = Modifier
) {
    val summary = remember(transactions) { TransactionAnalytics.calculateBalanceSummary(transactions) }
    val expensesByCategory = remember(transactions) { TransactionAnalytics.calculateExpenseByCategory(transactions) }
    var selectedRange by rememberSaveable { mutableStateOf(StatisticsRange.LAST_7_DAYS) }
    val timeSeries = remember(transactions, selectedRange) {
        TransactionAnalytics.calculateTimeSeries(transactions, selectedRange)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Estadísticas", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                BalanceOverviewCard(summary = summary)
            }
            item {
                RangeSelector(
                    selectedRange = selectedRange,
                    onRangeSelected = { selectedRange = it }
                )
            }
            item {
                IncomeExpenseChart(points = timeSeries)
            }
            if (expensesByCategory.isNotEmpty()) {
                item {
                    Text(
                        text = "Gastos por categoría",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                items(expensesByCategory.entries.toList(), key = { it.key }) { (category, amount) ->
                    CategoryStatRow(category = category, amount = amount)
                }
            } else {
                item {
                    Text(
                        text = "Aún no se registran gastos.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun BalanceOverviewCard(summary: BalanceSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "Balance actual", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = statsNumberFormat.format(summary.totalBalance), style = MaterialTheme.typography.headlineSmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = "Ingresos", style = MaterialTheme.typography.labelLarge)
                    Text(text = statsNumberFormat.format(summary.totalIncome), style = MaterialTheme.typography.bodyLarge)
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = "Gastos", style = MaterialTheme.typography.labelLarge)
                    Text(text = statsNumberFormat.format(summary.totalExpense), style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
private fun RangeSelector(
    selectedRange: StatisticsRange,
    onRangeSelected: (StatisticsRange) -> Unit,
    modifier: Modifier = Modifier
) {
    val ranges = StatisticsRange.values()
    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        ranges.forEachIndexed { index, range ->
            SegmentedButton(
                selected = selectedRange == range,
                onClick = { onRangeSelected(range) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = ranges.size)
            ) {
                Text(text = range.displayName)
            }
        }
    }
}

@Composable
private fun IncomeExpenseChart(
    points: List<TimeSeriesPoint>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Evolución de ingresos y gastos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            if (points.isEmpty()) {
                Text(
                    text = "Registra movimientos para ver la evolución de tus finanzas en el tiempo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                return@Column
            }

            val incomeColor = MaterialTheme.colorScheme.primary
            val expenseColor = MaterialTheme.colorScheme.error
            val gridColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            val axisColor = MaterialTheme.colorScheme.outline
            val formatter = remember { DateTimeFormatter.ofPattern("d MMM", Locale("es", "ES")) }
            val maxValue = points.maxOf { max(it.income, it.expense) }
            val safeMax = if (maxValue > 0) maxValue else 1.0

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
                val leftPadding = 48f
                val rightPadding = 16f
                val topPadding = 16f
                val bottomPadding = 36f
                val chartWidth = size.width - leftPadding - rightPadding
                val chartHeight = size.height - topPadding - bottomPadding
                val bottomY = size.height - bottomPadding
                val startX = leftPadding
                val endX = size.width - rightPadding
                val xStep = if (points.size <= 1) 0f else chartWidth / (points.size - 1)

                // Draw horizontal grid lines
                val gridLines = 4
                val gridSpacing = chartHeight / gridLines
                for (i in 0..gridLines) {
                    val y = bottomY - gridSpacing * i
                    drawLine(
                        color = gridColor,
                        start = Offset(startX, y),
                        end = Offset(endX, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Draw axes
                drawLine(
                    color = axisColor,
                    start = Offset(startX, bottomY),
                    end = Offset(endX, bottomY),
                    strokeWidth = 2.dp.toPx()
                )
                drawLine(
                    color = axisColor,
                    start = Offset(startX, bottomY),
                    end = Offset(startX, bottomY - chartHeight),
                    strokeWidth = 2.dp.toPx()
                )

                val incomePath = Path()
                val expensePath = Path()
                val incomePoints = mutableListOf<Offset>()
                val expensePoints = mutableListOf<Offset>()

                points.forEachIndexed { index, point ->
                    val x = startX + xStep * index
                    val incomeRatio = (point.income / safeMax).toFloat()
                    val expenseRatio = (point.expense / safeMax).toFloat()
                    val incomeY = bottomY - (incomeRatio * chartHeight)
                    val expenseY = bottomY - (expenseRatio * chartHeight)

                    if (index == 0) {
                        incomePath.moveTo(x, incomeY)
                        expensePath.moveTo(x, expenseY)
                    } else {
                        incomePath.lineTo(x, incomeY)
                        expensePath.lineTo(x, expenseY)
                    }

                    incomePoints.add(Offset(x, incomeY))
                    expensePoints.add(Offset(x, expenseY))
                }

                drawPath(
                    path = incomePath,
                    color = incomeColor,
                    style = Stroke(width = 3.dp.toPx())
                )
                drawPath(
                    path = expensePath,
                    color = expenseColor,
                    style = Stroke(width = 3.dp.toPx())
                )

                val pointRadius = 4.dp.toPx()
                incomePoints.forEach { point ->
                    drawCircle(color = incomeColor, radius = pointRadius, center = point)
                }
                expensePoints.forEach { point ->
                    drawCircle(color = expenseColor, radius = pointRadius, center = point)
                }
            }

            val axisDates = when {
                points.size >= 3 -> listOf(points.first().date, points[points.size / 2].date, points.last().date)
                points.size == 2 -> listOf(points.first().date, points.last().date)
                else -> listOf(points.first().date)
            }

            when (axisDates.size) {
                1 -> Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = formatter.format(axisDates.first()),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                2 -> Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    axisDates.forEach { date ->
                        Text(
                            text = formatter.format(date),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    axisDates.forEach { date ->
                        Text(
                            text = formatter.format(date),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(color = incomeColor, label = "Ingresos")
                LegendItem(color = expenseColor, label = "Gastos")
                Spacer(modifier = Modifier.weight(1f, fill = true))
                Text(
                    text = "Máximo mostrado: ${statsNumberFormat.format(safeMax)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color = color, shape = CircleShape)
        )
        Text(text = label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun CategoryStatRow(category: String, amount: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = category, style = MaterialTheme.typography.bodyLarge)
            Text(text = statsNumberFormat.format(amount), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}
