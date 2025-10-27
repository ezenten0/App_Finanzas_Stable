@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.example.app_finanzas.budgets

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app_finanzas.data.budget.BudgetGoal
import com.example.app_finanzas.data.budget.BudgetRepository
import com.example.app_finanzas.data.transaction.TransactionRepository
import com.example.app_finanzas.home.analytics.BudgetProgress
import com.example.app_finanzas.categories.CategoryDefinitions
import com.example.app_finanzas.ui.icons.CategoryIcon
import com.example.app_finanzas.ui.icons.CategoryIconRegistry
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

private val budgetNumberFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale("es", "ES"))

@Composable
fun BudgetsRoute(
    transactionRepository: TransactionRepository,
    budgetRepository: BudgetRepository,
    modifier: Modifier = Modifier,
    viewModel: BudgetsViewModel = viewModel(
        factory = BudgetsViewModel.Factory(budgetRepository, transactionRepository)
    )
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var editingGoal by remember { mutableStateOf<BudgetGoal?>(null) }

    BudgetsScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onAddBudget = {
            editingGoal = BudgetGoal(category = "", limit = 0.0, iconKey = CategoryDefinitions.OTHERS)
        },
        onEditBudget = { goal -> editingGoal = goal },
        onDeleteBudget = { goal ->
            viewModel.deleteBudget(goal.id)
            scope.launch { snackbarHostState.showSnackbar("Meta eliminada") }
        },
        modifier = modifier
    )

    editingGoal?.let { goal ->
        BudgetEditorDialog(
            goal = goal,
            onDismiss = { editingGoal = null },
            onConfirm = { updated ->
                viewModel.saveBudget(updated)
                scope.launch { snackbarHostState.showSnackbar("Meta guardada") }
                editingGoal = null
            }
        )
    }
}

@Composable
fun BudgetsScreen(
    state: BudgetsUiState,
    snackbarHostState: SnackbarHostState,
    onAddBudget: () -> Unit,
    onEditBudget: (BudgetGoal) -> Unit,
    onDeleteBudget: (BudgetGoal) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Presupuestos",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            AnimatedVisibility(visible = !state.isLoading) {
                ExtendedFloatingActionButton(
                    onClick = onAddBudget,
                    icon = { Icon(imageVector = Icons.Rounded.Add, contentDescription = null) },
                    text = { Text(text = "Nueva meta") },
                    shape = RoundedCornerShape(28.dp)
                )
            }
        }
    ) { innerPadding ->
        if (state.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
                Text(
                    text = "Cargando metas", 
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (state.progress.isEmpty()) {
                    item {
                        EmptyBudgetsState(onAddBudget = onAddBudget)
                    }
                } else {
                    items(state.progress, key = { it.category }) { progress ->
                        val goal = state.goals.firstOrNull { it.category == progress.category }
                        BudgetProgressCard(
                            progress = progress,
                            iconKey = goal?.iconKey,
                            onEdit = { goal?.let(onEditBudget) },
                            onDelete = { goal?.let(onDeleteBudget) },
                            modifier = Modifier
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyBudgetsState(onAddBudget: () -> Unit) {
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
            Icon(
                imageVector = Icons.Rounded.Savings,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Crea tu primera meta de ahorro",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Define límites personalizados para tus categorías y controla tus gastos con alertas visuales.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Button(onClick = onAddBudget, shape = RoundedCornerShape(20.dp)) {
                Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
                Spacer(modifier = Modifier.size(8.dp))
                Text(text = "Crear meta")
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun BudgetProgressCard(
    progress: BudgetProgress,
    iconKey: String?,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val alertColor = when {
        progress.progress >= 1.0 -> MaterialTheme.colorScheme.error
        progress.progress >= 0.75 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CategoryIcon(
                        key = iconKey,
                        contentDescription = progress.category,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(28.dp),
                        tint = alertColor
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = progress.category,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        AnimatedContent(
                            targetState = progress.spent,
                            transitionSpec = { fadeIn() with fadeOut() },
                            label = "spentAnimation"
                        ) { spent ->
                            Text(
                                text = "${budgetNumberFormat.format(spent)} de ${budgetNumberFormat.format(progress.limit)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onEdit) {
                        Icon(imageVector = Icons.Rounded.Edit, contentDescription = "Editar meta")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(imageVector = Icons.Rounded.Delete, contentDescription = "Eliminar meta")
                    }
                }
            }

            LinearProgressIndicator(
                progress = progress.progress.toFloat().coerceIn(0f, 1f),
                modifier = Modifier.fillMaxWidth(),
                color = alertColor
            )

            AnimatedVisibility(visible = progress.progress >= 0.75) {
                Text(
                    text = when {
                        progress.progress >= 1.0 -> "Has superado tu presupuesto. Revisa tus gastos."
                        else -> "Atención, has consumido más del 75% del presupuesto."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = alertColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun BudgetEditorDialog(
    goal: BudgetGoal,
    onDismiss: () -> Unit,
    onConfirm: (BudgetGoal) -> Unit
) {
    var category by rememberSaveable(goal.id, goal.category) { mutableStateOf(goal.category) }
    var limit by rememberSaveable(goal.id, goal.limit) { mutableStateOf(goal.limit.toString()) }
    var iconKey by rememberSaveable(goal.id, goal.iconKey) {
        mutableStateOf(goal.iconKey.ifBlank { CategoryDefinitions.OTHERS })
    }
    val isEditing = goal.id != 0

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val parsedLimit = limit.replace(",", ".").toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
                if (category.isNotBlank() && parsedLimit > 0.0) {
                    val normalizedIconKey = iconKey.ifBlank { CategoryDefinitions.OTHERS }
                    onConfirm(goal.copy(category = category.trim(), limit = parsedLimit, iconKey = normalizedIconKey))
                }
            }) {
                Text(text = if (isEditing) "Actualizar" else "Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(text = "Cancelar") }
        },
        title = {
            Text(
                text = if (isEditing) "Editar meta" else "Nueva meta",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {
                        category = it
                        CategoryDefinitions.keyForLabel(it)?.let { key -> iconKey = key }
                    },
                    label = { Text(text = "Categoría") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = limit,
                    onValueChange = { limit = it },
                    label = { Text(text = "Límite mensual") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Text(
                    text = "Icono",
                    style = MaterialTheme.typography.labelLarge
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryIconRegistry.defaultOptions.forEach { option ->
                        val selected = option.key == iconKey
                        FilterChip(
                            selected = selected,
                            onClick = { iconKey = option.key },
                            label = { Text(text = option.label) },
                            leadingIcon = {
                                Icon(imageVector = option.icon, contentDescription = option.label)
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            )
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CategoryIcon(
                        key = iconKey,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "Icono seleccionado",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "Recibirás alertas cuando superes el 75% del límite.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = AlertDialogDefaults.containerColor
    )
}
