package com.example.app_finanzas.auth

import com.example.app_finanzas.data.user.UserProfile

data class AuthUiState(
    val mode: AuthMode = AuthMode.LOGIN,
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val generalError: String? = null,
    val isSubmitting: Boolean = false,
    val authenticatedUser: UserProfile? = null
)
