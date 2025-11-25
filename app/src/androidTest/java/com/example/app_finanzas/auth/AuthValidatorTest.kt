package com.example.app_finanzas.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests that ensure the authentication validation rules match the
 * requirements enforced in the UI.
 */
class AuthValidatorTest {

    @Test
    fun name_is_required_when_registering() {
        val error = AuthValidator.validateName(AuthMode.REGISTER, "")
        assertEquals("El nombre es obligatorio.", error)
    }

    @Test
    fun email_validation_catches_malformed_addresses() {
        val error = AuthValidator.validateEmail("correo-invalido")
        assertEquals("Ingresa un correo válido.", error)
    }

    @Test
    fun email_validation_passes_valid_addresses() {
        val error = AuthValidator.validateEmail("usuario@example.com")
        assertNull(error)
    }

    @Test
    fun password_must_be_strong_when_registering() {
        val error = AuthValidator.validatePassword(AuthMode.REGISTER, "weakpass")
        assertEquals("Debe tener 8 caracteres, una mayúscula, una minúscula y un número.", error)
    }

    @Test
    fun confirm_password_ensures_both_values_match() {
        val error =
            AuthValidator.validateConfirmPassword(AuthMode.REGISTER, "Seguro123", "Seguro124")
        assertEquals("Las contraseñas no coinciden.", error)
    }

    @Test
    fun login_mode_does_not_require_confirm_password() {
        val error =
            AuthValidator.validateConfirmPassword(AuthMode.LOGIN, "Clave123", "Diferente")
        assertNull(error)
    }
}
