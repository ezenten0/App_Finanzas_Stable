package com.example.app_finanzas.activities.intro

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.app_finanzas.activities.auth.AuthActivity
import com.example.app_finanzas.activities.intro.screens.IntroScreen
import com.example.app_finanzas.ui.theme.App_FinanzasTheme

class IntroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
