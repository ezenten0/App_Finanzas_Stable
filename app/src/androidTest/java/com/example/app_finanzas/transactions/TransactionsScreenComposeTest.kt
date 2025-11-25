package com.example.app_finanzas.transactions

import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.app_finanzas.ui.theme.App_FinanzasTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TransactionsScreenComposeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun emptyStateShowsCtaButton() {
        composeTestRule.setContent {
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

        composeTestRule.onNodeWithText("Aún no hay movimientos").assertIsDisplayed()
        composeTestRule.onNodeWithText("Registrar movimiento").assertIsDisplayed().assertHasClickAction()
    }
}
