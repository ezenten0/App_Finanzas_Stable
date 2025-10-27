package com.example.app_finanzas

import android.os.Bundle
import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.app_finanzas.data.local.AppDatabase
import com.example.app_finanzas.data.budget.BudgetRepository
import com.example.app_finanzas.data.transaction.TransactionRepository
import com.example.app_finanzas.data.user.UserProfile
import com.example.app_finanzas.navigation.FinanceApp
import com.example.app_finanzas.ui.theme.App_FinanzasTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Build the Room database and repository that feed every screen with persisted data.
        val database = AppDatabase.getInstance(applicationContext)
        val transactionRepository = TransactionRepository(database.transactionDao())
        val budgetRepository = BudgetRepository(database.budgetDao())
        lifecycleScope.launch { budgetRepository.ensureSeedData() }
        setContent {
            App_FinanzasTheme {
                val userName = intent.getStringExtra(EXTRA_USER_NAME).orEmpty()
                val userEmail = intent.getStringExtra(EXTRA_USER_EMAIL).orEmpty()
                Surface(modifier = Modifier.fillMaxSize()) {
                    FinanceApp(
                        transactionRepository = transactionRepository,
                        budgetRepository = budgetRepository,
                        userName = userName,
                        userEmail = userEmail
                    )
                }
            }
        }
    }

    companion object {
        private const val EXTRA_USER_NAME = "extra_user_name"
        private const val EXTRA_USER_EMAIL = "extra_user_email"

        fun createIntent(context: Context, profile: UserProfile): Intent {
            return Intent(context, MainActivity::class.java).apply {
                putExtra(EXTRA_USER_NAME, profile.name)
                putExtra(EXTRA_USER_EMAIL, profile.email)
            }
        }
    }
}
