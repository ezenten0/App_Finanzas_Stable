package com.example.app_finanzas.auth

enum class AuthMode {
    LOGIN,
    REGISTER;

    val title: String
        get() = when (this) {
            LOGIN -> "Inicia sesión"
            REGISTER -> "Crear cuenta"
        }

    val actionText: String
        get() = when (this) {
            LOGIN -> "Iniciar sesión"
            REGISTER -> "Registrarse"
        }
}
