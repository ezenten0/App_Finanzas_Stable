@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.example.app_finanzas.transactions.form

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.ArrowDropUp
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app_finanzas.data.transaction.TransactionRepository
import com.example.app_finanzas.home.model.TransactionType
import com.example.app_finanzas.ui.icons.CategoryIconByLabel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TransactionFormRoute(
    transactionRepository: TransactionRepository,
    transactionId: Int?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: TransactionFormViewModel = viewModel(
        factory = TransactionFormViewModel.Factory(transactionRepository, transactionId)
    )
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.saveSucceeded) {
        if (state.saveSucceeded) {
            onDismiss()
            viewModel.consumeSuccessFlag()
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { message ->
            scope.launch { snackbarHostState.showSnackbar(message) }
        }
    }

    TransactionFormScreen(
        state = state,
        onTitleChange = viewModel::onTitleChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        onAmountChange = viewModel::onAmountChange,
        onCategoryChange = viewModel::onCategoryChange,
        onTypeChange = viewModel::onTypeChange,
        onDateSelected = viewModel::onDateSelected,
        onSave = viewModel::saveTransaction,
        onDismiss = onDismiss,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun TransactionFormScreen(
    state: TransactionFormUiState,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onTypeChange: (TransactionType) -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val datePickerVisible = rememberSaveable { mutableStateOf(false) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("d 'de' MMMM yyyy", Locale("es", "ES")) }

    if (datePickerVisible.value) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.date
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        )
        val confirmEnabled by remember(datePickerState) {
            derivedStateOf { datePickerState.selectedDateMillis != null }
        }
        DatePickerDialog(
            onDismissRequest = { datePickerVisible.value = false },
            confirmButton = {
                TextButton(
                    enabled = confirmEnabled,
                    onClick = {
                        val millis = datePickerState.selectedDateMillis ?: return@TextButton
                        val selectedDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        onDateSelected(selectedDate)
                        datePickerVisible.value = false
                    }
                ) {
                    Text(text = "Listo")
                }
            },
            dismissButton = {
                TextButton(onClick = { datePickerVisible.value = false }) {
                    Text(text = "Cancelar")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                title = { Text(text = "Selecciona la fecha") }
            )
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (state.isEditing) "Editar movimiento" else "Nuevo movimiento",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Rounded.Close, contentDescription = "Cerrar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedVisibility(visible = state.isSaving) {
                    Text(
                        text = "Guardando movimiento...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Button(
                    onClick = onSave,
                    enabled = !state.isSaving,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(text = if (state.isEditing) "Actualizar" else "Guardar")
                }
            }
        }
    ) { innerPadding ->
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            AnimatedContent(
                targetState = state.type,
                transitionSpec = {
                    fadeIn() with fadeOut()
                },
                label = "typeAnimation"
            ) { type ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (type == TransactionType.INCOME) Icons.Rounded.ArrowUpward else Icons.Rounded.ArrowDownward,
                        contentDescription = null,
                        tint = if (type == TransactionType.INCOME) Color(0xFF2E7D32) else Color(0xFFC62828),
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(
                        text = if (type == TransactionType.INCOME) "Ingreso" else "Gasto",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                SegmentedButton(
                    selected = state.type == TransactionType.INCOME,
                    onClick = { onTypeChange(TransactionType.INCOME) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    icon = { Icon(imageVector = Icons.Rounded.ArrowUpward, contentDescription = null) }
                ) {
                    Text(text = "Ingreso")
                }
                SegmentedButton(
                    selected = state.type == TransactionType.EXPENSE,
                    onClick = { onTypeChange(TransactionType.EXPENSE) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    icon = { Icon(imageVector = Icons.Rounded.ArrowDownward, contentDescription = null) }
                ) {
                    Text(text = "Gasto")
                }
            }

            OutlinedTextField(
                value = state.title,
                onValueChange = onTitleChange,
                label = { Text(text = "Título") },
                leadingIcon = { Icon(imageVector = Icons.Rounded.Payments, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = state.description,
                onValueChange = onDescriptionChange,
                label = { Text(text = "Descripción") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.amount,
                onValueChange = onAmountChange,
                label = { Text(text = "Monto") },
                leadingIcon = { Icon(imageVector = Icons.Rounded.CheckCircle, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            var categoryMenuExpanded by remember { mutableStateOf(false) }
            val dropdownCategories = state.availableCategories.ifEmpty { emptyList() }

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = state.category,
                    onValueChange = {
                        onCategoryChange(it)
                        categoryMenuExpanded = true
                    },
                    label = { Text(text = "Categoría") },
                    placeholder = { Text(text = "Ej. Hogar") },
                    leadingIcon = {
                        val resolvedLabel = if (state.category.isBlank()) "Otros" else state.category
                        CategoryIconByLabel(
                            label = resolvedLabel,
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { categoryMenuExpanded = !categoryMenuExpanded }) {
                            val icon = if (categoryMenuExpanded) Icons.Rounded.ArrowDropUp else Icons.Rounded.ArrowDropDown
                            Icon(imageVector = icon, contentDescription = null)
                        }
                    },
                    supportingText = {
                        Text(text = "Selecciona una categoría o escribe una nueva")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                DropdownMenu(
                    expanded = categoryMenuExpanded,
                    onDismissRequest = { categoryMenuExpanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    dropdownCategories.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(text = option) },
                            leadingIcon = {
                                CategoryIconByLabel(
                                    label = option,
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                onCategoryChange(option)
                                categoryMenuExpanded = false
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text(text = "Crear nueva categoría…", fontWeight = FontWeight.SemiBold) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
                        },
                        onClick = {
                            onCategoryChange("")
                            categoryMenuExpanded = false
                        }
                    )
                }
            }

            if (dropdownCategories.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    dropdownCategories.take(8).forEach { option ->
                        val isSelected = option.equals(state.category, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = { onCategoryChange(option) },
                            label = { Text(text = option) },
                            leadingIcon = {
                                CategoryIconByLabel(
                                    label = option,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            )
                        )
                    }
                }
            }

            Button(
                onClick = { datePickerVisible.value = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(imageVector = Icons.Rounded.Event, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = dateFormatter.format(state.date))
            }

        }
    }
}

