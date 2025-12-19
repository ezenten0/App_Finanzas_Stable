package com.example.app_finanzas.auth

import android.content.Intent
import com.example.app_finanzas.data.cloud.UserProfileFirestore
import com.example.app_finanzas.data.user.UserProfile
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import java.util.concurrent.CancellationException
class FirebaseAuthRepository(
    private val firebaseAuth: FirebaseAuth,
    private val googleSignInClient: GoogleSignInClient,
    private val userProfileFirestore: UserProfileFirestore
) {

    val googleSignInIntent: Intent
        get() = googleSignInClient.signInIntent

    suspend fun registerWithEmail(name: String, email: String, password: String): Result<UserProfile> {
        return runCatching {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email.trim(), password).awaitResult()
            val user = authResult.user ?: throw IllegalStateException("No se pudo obtener el usuario creado.")

            val profileUpdates = userProfileChangeRequest { displayName = name.trim() }
            user.updateProfile(profileUpdates).awaitResult()

            user.toUserProfile(fallbackName = name.trim()).also { profile ->
                userProfileFirestore.upsertProfile(profile, provider = "password")
            }
        }
    }

    suspend fun loginWithEmail(email: String, password: String): Result<UserProfile> {
        return runCatching {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email.trim(), password).awaitResult()
            val user = authResult.user ?: throw IllegalStateException("No se pudo obtener el usuario autenticado.")
            user.toUserProfile().also { profile ->
                userProfileFirestore.upsertProfile(profile, provider = "password")
            }
        }
    }

    suspend fun signInWithGoogle(data: Intent?): Result<UserProfile> {
        return runCatching {
            val accountTask = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = accountTask.getResult(ApiException::class.java)
                ?: throw IllegalArgumentException("No se seleccionó ninguna cuenta de Google.")

            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).awaitResult()
            val user = authResult.user ?: throw IllegalStateException("No se pudo obtener el usuario de Google.")
            val fallbackName = account.displayName ?: account.givenName ?: account.email.orEmpty()
            user.toUserProfile(fallbackName = fallbackName).also { profile ->
                userProfileFirestore.upsertProfile(profile, provider = "google.com")
            }
        }
    }

    suspend fun signOut(): Result<Unit> {
        return runCatching {
            googleSignInClient.signOut().awaitResult()
            firebaseAuth.signOut()
        }
    }

    fun parseAuthError(error: Throwable): String {
        return when (error) {
            is FirebaseAuthException -> when (error.errorCode) {
                "ERROR_INVALID_EMAIL" -> "El formato del correo es inválido."
                "ERROR_EMAIL_ALREADY_IN_USE" -> "Ya existe una cuenta con este correo."
                "ERROR_WRONG_PASSWORD", "ERROR_INVALID_CREDENTIAL" -> "Correo o contraseña incorrectos."
                "ERROR_USER_DISABLED" -> "La cuenta ha sido deshabilitada."
                "ERROR_USER_NOT_FOUND" -> "No existe una cuenta con este correo."
                else -> error.message ?: "Se produjo un error de autenticación."
            }

            is ApiException -> "No se pudo completar el inicio de sesión con Google."
            else -> error.message ?: "Se produjo un error al autenticar."
        }
    }
    private suspend fun <T> Task<T>.awaitResult(): T = suspendCancellableCoroutine { cont ->

        addOnCompleteListener { task ->
            if (!cont.isActive) return@addOnCompleteListener  // si se canceló, no hacemos nada

            if (task.isSuccessful) {
                cont.resume(task.result)
            } else {
                cont.resumeWithException(
                    task.exception ?: IllegalStateException("Operación de autenticación fallida.")
                )
            }
        }

        addOnCanceledListener {
            if (cont.isActive) cont.cancel(CancellationException("La Task fue cancelada."))
        }

        // NO intentes cancelar la Task acá: Task no soporta cancel() genérico
        // cont.invokeOnCancellation { /* no-op */ }
    }
    private fun com.google.firebase.auth.FirebaseUser.toUserProfile(fallbackName: String? = null): UserProfile {
        val resolvedName = displayName?.takeIf { it.isNotBlank() } ?: fallbackName ?: "Usuario"
        return UserProfile(
            uid = uid,
            name = resolvedName,
            email = email.orEmpty()
        )
    }
}
