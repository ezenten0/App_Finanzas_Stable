package com.example.app_finanzas.transactions.form

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.app_finanzas.categories.CategoryDefinitions
import com.example.app_finanzas.data.transaction.TransactionRepository
import com.example.app_finanzas.home.model.Transaction
import com.example.app_finanzas.home.model.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * ViewModel that drives the transaction editor by orchestrating loading,
 * validation and persistence of the captured information.
 */
@RequiresApi(Build.VERSION_CODES.O)
class TransactionFormViewModel(
    private val repository: TransactionRepository,
    private val transactionId: Int?
) : ViewModel() {

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    private val _uiState = MutableStateFlow(
        TransactionFormUiState(
            availableCategories = CategoryDefinitions.defaults.map { it.label }
        )
    )
    val uiState: StateFlow<TransactionFormUiState> = _uiState

    private fun reduceState(transform: (TransactionFormUiState) -> TransactionFormUiState) {
        _uiState.update { transform(it).recalculateCanSave() }
    }

    private fun TransactionFormUiState.recalculateCanSave(): TransactionFormUiState {
        val amountValue = amount.toDoubleOrNull()
        val canSave = title.isNotBlank() && amountValue != null && amountValue > 0.0 && category.isNotBlank()
        return copy(canSave = canSave)
    }

    init {
        observeCategories()
        if (transactionId != null) {
            loadTransaction(transactionId)
        } else {
            reduceState { it.copy(date = LocalDate.now()) }
        }
    }

    private fun observeCategories() {
        viewModelScope.launch {
            repository.observeCategories().collect { storedCategories ->
                val merged = CategoryDefinitions.mergedLabels(storedCategories)
                _uiState.update { it.copy(availableCategories = merged).recalculateCanSave() }
            }
        }
    }

    private fun loadTransaction(id: Int) {
        viewModelScope.launch {
            val transaction = repository.getTransactionById(id)
            if (transaction != null) {
                reduceState {
                    it.copy(
                        transactionId = transaction.id,
                        title = transaction.title,
                        description = transaction.description,
                        amount = transaction.amount.toString(),
                        type = transaction.type,
                        category = transaction.category,
                        date = LocalDate.parse(transaction.date, formatter),
                        isEditing = true
                    )
                }
            } else {
                reduceState { it.copy(errorMessage = "No se encontró el movimiento a editar") }
            }
        }
    }

    fun onTitleChange(value: String) {
        reduceState { it.copy(title = value, titleError = null, errorMessage = null) }
    }

    fun onDescriptionChange(value: String) {
        reduceState { it.copy(description = value) }
    }

    fun onAmountChange(value: String) {
        val normalized = value.replace(",", ".")
        reduceState { it.copy(amount = normalized, amountError = null, errorMessage = null) }
    }

    fun onCategoryChange(value: String) {
        reduceState { it.copy(category = value, categoryError = null, errorMessage = null) }
    }

    fun onTypeChange(type: TransactionType) {
        reduceState { it.copy(type = type) }
    }

    fun onDateSelected(date: LocalDate) {
        reduceState { it.copy(date = date) }
    }

    fun consumeSuccessFlag() {
        reduceState { it.copy(saveSucceeded = false) }
    }

    private fun validateFields(): Double? {
        val state = _uiState.value
        val trimmedTitle = state.title.trim()
        val trimmedCategory = state.category.trim()
        val amountValue = state.amount.toDoubleOrNull()

        val titleError = if (trimmedTitle.length < 3) {
            "Ingresa un título de al menos 3 caracteres."
        } else {
            null
        }

        val amountError = when {
            amountValue == null -> "Ingresa un monto numérico válido."
            amountValue <= 0.0 -> "El monto debe ser mayor a cero."
            else -> null
        }

        val categoryError = if (trimmedCategory.isBlank()) {
            "Selecciona o escribe una categoría."
        } else {
            null
        }

        val updated = state.copy(
            titleError = titleError,
            amountError = amountError,
            categoryError = categoryError
        ).recalculateCanSave()
        _uiState.value = updated

        return if (titleError == null && amountError == null && categoryError == null) {
            amountValue
        } else {
            null
        }
    }

    fun saveTransaction() {
        val amountValue = validateFields() ?: return

        viewModelScope.launch {
            reduceState { it.copy(isSaving = true, errorMessage = null) }
            val state = _uiState.value
            val transaction = Transaction(
                id = state.transactionId ?: 0,
                title = state.title.trim(),
                description = state.description.trim(),
                amount = amountValue,
                type = state.type,
                category = state.category.trim(),
                date = state.date.format(formatter)
            )
            runCatching { repository.upsertTransaction(transaction) }
                .onSuccess { savedId ->
                    reduceState {
                        it.copy(
                            transactionId = savedId,
                            isSaving = false,
                            saveSucceeded = true,
                            isEditing = true
                        )
                    }
                }
                .onFailure { throwable ->
                    reduceState {
                        it.copy(
                            isSaving = false,
                            errorMessage = "Ocurrió un error al guardar: ${throwable.localizedMessage ?: "Inténtalo nuevamente"}"
                        )
                    }
                }
        }
    }

    class Factory(
        private val repository: TransactionRepository,
        private val transactionId: Int?
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(TransactionFormViewModel::class.java))
            return TransactionFormViewModel(repository, transactionId) as T
        }
    }
}

/**
 * Immutable UI state captured by the transaction form composable.
 */
@RequiresApi(Build.VERSION_CODES.O)
data class TransactionFormUiState(
    val transactionId: Int? = null,
    val title: String = "",
    val description: String = "",
    val amount: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val category: String = "",
    val date: LocalDate = LocalDate.now(),
    val availableCategories: List<String> = emptyList(),
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val saveSucceeded: Boolean = false,
    val errorMessage: String? = null,
    val titleError: String? = null,
    val amountError: String? = null,
    val categoryError: String? = null,
    val canSave: Boolean = false
)
