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

    init {
        observeCategories()
        if (transactionId != null) {
            loadTransaction(transactionId)
        } else {
            _uiState.update { it.copy(date = LocalDate.now()) }
        }
    }

    private fun observeCategories() {
        viewModelScope.launch {
            repository.observeCategories().collect { storedCategories ->
                val merged = CategoryDefinitions.mergedLabels(storedCategories)
                _uiState.update { it.copy(availableCategories = merged) }
            }
        }
    }

    private fun loadTransaction(id: Int) {
        viewModelScope.launch {
            val transaction = repository.getTransactionById(id)
            if (transaction != null) {
                _uiState.update {
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
                _uiState.update { it.copy(errorMessage = "No se encontró el movimiento a editar") }
            }
        }
    }

    fun onTitleChange(value: String) {
        _uiState.update { it.copy(title = value) }
    }

    fun onDescriptionChange(value: String) {
        _uiState.update { it.copy(description = value) }
    }

    fun onAmountChange(value: String) {
        val normalized = value.replace(",", ".")
        _uiState.update { it.copy(amount = normalized) }
    }

    fun onCategoryChange(value: String) {
        _uiState.update { it.copy(category = value) }
    }

    fun onTypeChange(type: TransactionType) {
        _uiState.update { it.copy(type = type) }
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.update { it.copy(date = date) }
    }

    fun consumeSuccessFlag() {
        _uiState.update { it.copy(saveSucceeded = false) }
    }

    fun saveTransaction() {
        val state = _uiState.value
        val amountValue = state.amount.toDoubleOrNull()
        if (state.title.isBlank() || amountValue == null || amountValue <= 0) {
            _uiState.update {
                it.copy(errorMessage = "Completa el título y un monto válido.")
            }
            return
        }
        if (state.category.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = "Elige una categoría para el movimiento.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val transaction = Transaction(
                id = state.transactionId ?: 0,
                title = state.title.trim(),
                description = state.description.trim(),
                amount = amountValue,
                type = state.type,
                category = state.category.trim(),
                date = state.date.format(formatter)
            )
            val savedId = repository.upsertTransaction(transaction)
            _uiState.update {
                it.copy(
                    transactionId = savedId,
                    isSaving = false,
                    saveSucceeded = true,
                    isEditing = true
                )
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
    val errorMessage: String? = null
)
