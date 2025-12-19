package com.example.app_finanzas.auth

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app_finanzas.auth.ui.AuthScreen
import com.example.app_finanzas.data.user.UserProfile

@Composable
fun AuthRoute(
    onAuthenticated: (UserProfile) -> Unit,
    viewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(LocalContext.current))
) {
    val state by viewModel.uiState
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result -> viewModel.onGoogleSignInResult(result.data) }
    )

    LaunchedEffect(state.authenticatedUser) {
        state.authenticatedUser?.let { profile ->
            onAuthenticated(profile)
            viewModel.onAuthHandled()
        }
    }

    AuthScreen(
        state = state,
        onNameChanged = viewModel::onNameChange,
        onEmailChanged = viewModel::onEmailChange,
        onPasswordChanged = viewModel::onPasswordChange,
        onConfirmPasswordChanged = viewModel::onConfirmPasswordChange,
        onSubmit = viewModel::onSubmit,
        onGoogleSignIn = {
            if (!state.isSubmitting && !state.isGoogleSubmitting) {
                viewModel.onGoogleSignInStarted()
                googleSignInLauncher.launch(viewModel.googleSignInIntent)
            }
        },
        onToggleMode = viewModel::toggleMode
    )
}
