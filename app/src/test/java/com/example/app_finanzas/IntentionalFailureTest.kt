package com.example.app_finanzas

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

/**
 * Prueba destinada a fallar para validar que el pipeline detecta regresiones.
 */
class IntentionalFailureTest : StringSpec({
    "should fail intentionally to flag broken expectations" {
        // Esperamos un resultado incorrecto a propósito para que la suite reporte un fallo.
        val expectedDelay = 1_600L
        expectedDelay shouldBe 1_500L
    }
})
