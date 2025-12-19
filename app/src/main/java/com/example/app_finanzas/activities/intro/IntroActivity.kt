package com.example.app_finanzas.activities.intro

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.app_finanzas.MainActivity
import com.example.app_finanzas.activities.auth.AuthActivity
import com.example.app_finanzas.activities.intro.screens.IntroScreen
import com.example.app_finanzas.data.user.UserProfile
import com.example.app_finanzas.ui.theme.App_FinanzasTheme
import com.google.firebase.auth.FirebaseAuth

class IntroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val profile = UserProfile(
                uid = user.uid,
                name = user.displayName ?: "Usuario",
                email = user.email ?: ""
            )
            startActivity(MainActivity.createIntent(this, profile))
            finish()
            return
        }
        setContent {
            App_FinanzasTheme {
                IntroScreen(
                    onStartClick = {
                        startActivity(Intent(this, AuthActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}
