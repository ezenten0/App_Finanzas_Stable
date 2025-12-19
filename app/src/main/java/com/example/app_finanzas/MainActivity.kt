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
import com.example.app_finanzas.R
import com.example.app_finanzas.activities.auth.AuthActivity
import com.example.app_finanzas.data.local.AppDatabase
import com.example.app_finanzas.data.budget.BudgetRepository
import com.example.app_finanzas.data.transaction.TransactionRepository
import com.example.app_finanzas.data.insights.InsightsRepository
import com.example.app_finanzas.data.user.UserProfile
import com.example.app_finanzas.navigation.FinanceApp
import com.example.app_finanzas.ui.theme.App_FinanzasTheme
import com.example.app_finanzas.network.NetworkModule
import com.example.app_finanzas.data.cloud.FirestoreBudgetRepository
import com.example.app_finanzas.data.cloud.FirestoreTransactionRepository
import com.example.app_finanzas.data.cloud.UserProfileFirestore
import com.example.app_finanzas.data.insights.cloud.FirestoreInsightsDataSource
import com.example.app_finanzas.network.FirebaseAuthTokenProvider
import com.example.app_finanzas.auth.FirebaseAuthRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val userName = intent.getStringExtra(EXTRA_USER_NAME).orEmpty()
        val userEmail = intent.getStringExtra(EXTRA_USER_EMAIL).orEmpty()
        val userId = intent.getStringExtra(EXTRA_USER_ID).orEmpty()
        // Build the Room database and repository that feed every screen with persisted data.
        val database = AppDatabase.getInstance(applicationContext)
        val authTokenProvider = FirebaseAuthTokenProvider()
        val financeApi = NetworkModule.provideFinanceServiceApi(authTokenProvider)
        val firestore = FirebaseFirestore.getInstance()
        val transactionRepository = TransactionRepository(
            transactionDao = database.transactionDao(),
            financeServiceApi = financeApi,
            cloudRepository = FirestoreTransactionRepository(firestore, userId)
        )
        val budgetRepository = BudgetRepository(
            budgetDao = database.budgetDao(),
            financeServiceApi = financeApi,
            riskServiceApi = NetworkModule.provideRiskServiceApi(),
            cloudRepository = FirestoreBudgetRepository(firestore, userId),
            userIdProvider = { userId }
        )
        val insightsRepository = InsightsRepository(
            riskServiceApi = NetworkModule.provideRiskServiceApi(),
            cloudDataSource = FirestoreInsightsDataSource(firestore)
        )
        val authRepository = FirebaseAuthRepository(
            firebaseAuth = FirebaseAuth.getInstance(),
            googleSignInClient = GoogleSignIn.getClient(
                this,
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()
            ),
            userProfileFirestore = UserProfileFirestore(firestore)
        )
        lifecycleScope.launch {
            transactionRepository.refreshFromRemote()
            budgetRepository.refreshFromRemote()
            insightsRepository.refreshFromRemote()
        }
        setContent {
            App_FinanzasTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    FinanceApp(
                        transactionRepository = transactionRepository,
                        budgetRepository = budgetRepository,
                        insightsRepository = insightsRepository,
                        userId = userId,
                        userName = userName,
                        userEmail = userEmail,
                        onSignOut = {
                            launchSignOut(
                                authRepository = authRepository,
                                transactionRepository = transactionRepository,
                                budgetRepository = budgetRepository
                            )
                        }
                    )
                }
            }
        }
    }

    private fun launchSignOut(
        authRepository: FirebaseAuthRepository,
        transactionRepository: TransactionRepository,
        budgetRepository: BudgetRepository
    ) {
        lifecycleScope.launch {
            authRepository.signOut().onFailure { error ->
                error.printStackTrace()
            }
            withContext(Dispatchers.IO) {
                transactionRepository.clearLocalData()
                budgetRepository.clearLocalData()
            }
            startActivity(Intent(this@MainActivity, AuthActivity::class.java))
            finish()
        }
    }

    companion object {
        private const val EXTRA_USER_ID = "extra_user_id"
        private const val EXTRA_USER_NAME = "extra_user_name"
        private const val EXTRA_USER_EMAIL = "extra_user_email"

        fun createIntent(context: Context, profile: UserProfile): Intent {
            return Intent(context, MainActivity::class.java).apply {
                putExtra(EXTRA_USER_ID, profile.uid)
                putExtra(EXTRA_USER_NAME, profile.name)
                putExtra(EXTRA_USER_EMAIL, profile.email)
            }
        }
    }
}
