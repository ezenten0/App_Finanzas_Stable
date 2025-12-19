package com.example.app_finanzas.auth

import android.content.Intent
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_finanzas.data.user.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthViewModel(
    private val authRepository: FirebaseAuthRepository
) : ViewModel() {

    private val _uiState = mutableStateOf(AuthUiState())
    val uiState: State<AuthUiState> = _uiState

    fun onNameChange(value: String) {
        _uiState.value = _uiState.value.copy(
            name = value,
            nameError = null,
            generalError = null
        )
    }

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(
            email = value,
            emailError = null,
            generalError = null
        )
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(
            password = value,
            passwordError = null,
            generalError = null
        )
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(
            confirmPassword = value,
            confirmPasswordError = null,
            generalError = null
        )
    }

    fun toggleMode() {
        val newMode = if (_uiState.value.mode == AuthMode.LOGIN) AuthMode.REGISTER else AuthMode.LOGIN
        _uiState.value = AuthUiState(mode = newMode)
    }

    fun onSubmit() {
        val state = _uiState.value
        val nameError = AuthValidator.validateName(state.mode, state.name)
        val emailError = AuthValidator.validateEmail(state.email)
        val passwordError = AuthValidator.validatePassword(state.mode, state.password)
        val confirmPasswordError = AuthValidator.validateConfirmPassword(
            state.mode,
            state.password,
            state.confirmPassword
        )

        if (
            nameError != null ||
            emailError != null ||
            passwordError != null ||
            confirmPasswordError != null
        ) {
            _uiState.value = state.copy(
                nameError = nameError,
                emailError = emailError,
                passwordError = passwordError,
                confirmPasswordError = confirmPasswordError
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, generalError = null)
            val result = withContext(Dispatchers.IO) {
                when (_uiState.value.mode) {
                    AuthMode.LOGIN -> authRepository.loginWithEmail(state.email, state.password)
                    AuthMode.REGISTER -> authRepository.registerWithEmail(state.name, state.email, state.password)
                }
            }

            result.fold(
                onSuccess = { handleAuthenticationSuccess(it) },
                onFailure = { error -> handleAuthenticationError(error, forGoogle = false) }
            )
        }
    }

    fun onGoogleSignInStarted() {
        _uiState.value = _uiState.value.copy(
            isGoogleSubmitting = true,
            generalError = null
        )
    }

    fun onGoogleSignInResult(data: Intent?) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                authRepository.signInWithGoogle(data)
            }

            result.fold(
                onSuccess = { handleAuthenticationSuccess(it) },
                onFailure = { error -> handleAuthenticationError(error, forGoogle = true) }
            )
        }
    }

    val googleSignInIntent: Intent
        get() = authRepository.googleSignInIntent

    fun onAuthHandled() {
        _uiState.value = _uiState.value.copy(
            isSubmitting = false,
            isGoogleSubmitting = false,
            authenticatedUser = null
        )
    }

    private fun handleAuthenticationSuccess(profile: UserProfile) {
        _uiState.value = _uiState.value.copy(
            isSubmitting = false,
            isGoogleSubmitting = false,
            generalError = null,
            authenticatedUser = profile
        )
    }

    private fun handleAuthenticationError(error: Throwable, forGoogle: Boolean) {
        val message = authRepository.parseAuthError(error)
        _uiState.value = _uiState.value.copy(
            isSubmitting = false,
            isGoogleSubmitting = if (forGoogle) false else _uiState.value.isGoogleSubmitting,
            generalError = message
        )
    }

    // Validation logic now lives in [AuthValidator] to keep this ViewModel focused on
    // UI state management and make the rules straightforward to test.
}
