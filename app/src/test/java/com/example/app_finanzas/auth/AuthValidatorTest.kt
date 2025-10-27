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
    fun `name is required when registering`() {
        val error = AuthValidator.validateName(AuthMode.REGISTER, "")
        assertEquals("El nombre es obligatorio.", error)
    }

    @Test
    fun `email validation catches malformed addresses`() {
        val error = AuthValidator.validateEmail("correo-invalido")
        assertEquals("Ingresa un correo válido.", error)
    }

    @Test
    fun `email validation passes valid addresses`() {
        val error = AuthValidator.validateEmail("usuario@example.com")
        assertNull(error)
    }

    @Test
    fun `password must be strong when registering`() {
        val error = AuthValidator.validatePassword(AuthMode.REGISTER, "weakpass")
        assertEquals("Debe tener 8 caracteres, una mayúscula, una minúscula y un número.", error)
    }

    @Test
    fun `confirm password ensures both values match`() {
        val error = AuthValidator.validateConfirmPassword(AuthMode.REGISTER, "Seguro123", "Seguro124")
        assertEquals("Las contraseñas no coinciden.", error)
    }

    @Test
    fun `login mode does not require confirm password`() {
        val error = AuthValidator.validateConfirmPassword(AuthMode.LOGIN, "Clave123", "Diferente")
        assertNull(error)
    }
}
