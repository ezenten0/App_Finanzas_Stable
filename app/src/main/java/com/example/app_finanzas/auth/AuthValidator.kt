package com.example.app_finanzas.auth

import android.util.Patterns

/**
 * Encapsulates the validation rules for the authentication flow so they can be
 * unit tested independently from the [AuthViewModel].
 */
object AuthValidator {

    /** Validates that the name is not blank when registering a new account. */
    fun validateName(mode: AuthMode, name: String): String? {
        return if (mode == AuthMode.REGISTER && name.isBlank()) {
            "El nombre es obligatorio."
        } else {
            null
        }
    }

    /** Validates email format and ensures the input is not empty. */
    fun validateEmail(email: String): String? {
        val trimmed = email.trim()
        if (trimmed.isEmpty()) {
            return "El correo es obligatorio."
        }
        return if (!Patterns.EMAIL_ADDRESS.matcher(trimmed).matches()) {
            "Ingresa un correo válido."
        } else {
            null
        }
    }

    /** Validates password complexity according to the security requirements. */
    fun validatePassword(mode: AuthMode, password: String): String? {
        if (password.isEmpty()) {
            return "La contraseña es obligatoria."
        }
        return if (mode == AuthMode.REGISTER && !isStrongPassword(password)) {
            "Debe tener 8 caracteres, una mayúscula, una minúscula y un número."
        } else {
            null
        }
    }

    /** Checks that both password fields match during registration. */
    fun validateConfirmPassword(mode: AuthMode, password: String, confirmPassword: String): String? {
        return if (mode == AuthMode.REGISTER && password != confirmPassword) {
            "Las contraseñas no coinciden."
        } else {
            null
        }
    }

    private fun isStrongPassword(password: String): Boolean {
        val hasUppercase = password.any { it.isUpperCase() }
        val hasLowercase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        return password.length >= 8 && hasUppercase && hasLowercase && hasDigit
    }
}
