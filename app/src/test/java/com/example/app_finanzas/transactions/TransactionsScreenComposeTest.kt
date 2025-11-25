package com.example.app_finanzas.transactions

import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.app_finanzas.ui.theme.App_FinanzasTheme
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.runner.Description
import org.junit.runners.model.Statement

@Disabled
class TransactionsScreenComposeTest {

    private val composeTestRule = createComposeRule()

    private fun runComposeTest(block: ComposeContentTestRule.() -> Unit) {
        val statement = object : Statement() {
            override fun evaluate() {
                composeTestRule.block()
            }
        }
        composeTestRule
            .apply(statement, Description.createTestDescription(javaClass, "compose"))
            .evaluate()
    }

    @Test
    fun emptyStateShowsCtaButton() = runComposeTest {
        setContent {
            App_FinanzasTheme {
                TransactionsScreen(
                    transactions = emptyList(),
                    onTransactionSelected = {},
                    onAddTransaction = {},
                    onDeleteTransaction = {},
                    snackbarHostState = SnackbarHostState()
                )
            }
        }

        onNodeWithText("Aún no hay movimientos").assertIsDisplayed()
        onNodeWithText("Registrar movimiento").assertIsDisplayed().assertHasClickAction()
    }
}
