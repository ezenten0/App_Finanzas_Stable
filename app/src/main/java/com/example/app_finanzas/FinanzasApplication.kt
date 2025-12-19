package com.example.app_finanzas

import android.app.Application
import com.google.firebase.FirebaseApp

class FinanzasApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
