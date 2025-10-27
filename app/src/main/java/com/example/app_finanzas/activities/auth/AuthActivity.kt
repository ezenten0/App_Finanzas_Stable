package com.example.app_finanzas.activities.auth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.app_finanzas.MainActivity
import com.example.app_finanzas.auth.AuthRoute
import com.example.app_finanzas.data.user.UserProfile
import com.example.app_finanzas.ui.theme.App_FinanzasTheme

class AuthActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            App_FinanzasTheme {
                AuthRoute(onAuthenticated = ::navigateToHome)
            }
        }
    }

    private fun navigateToHome(profile: UserProfile) {
        val intent = MainActivity.createIntent(this, profile)
        startActivity(intent)
        finish()
    }
}
