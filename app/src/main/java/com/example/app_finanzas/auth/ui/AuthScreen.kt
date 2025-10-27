package com.example.app_finanzas.auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.app_finanzas.auth.AuthMode
import com.example.app_finanzas.auth.AuthUiState
import com.example.app_finanzas.ui.theme.App_FinanzasTheme

@Composable
fun AuthScreen(
    state: AuthUiState,
    onNameChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onToggleMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = state.mode.title,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = if (state.mode == AuthMode.LOGIN) {
                            "Accede a tu cuenta para seguir gestionando tus finanzas."
                        } else {
                            "Crea una cuenta para empezar a organizar tu dinero."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (state.mode == AuthMode.REGISTER) {
                    AuthTextField(
                        value = state.name,
                        onValueChange = onNameChanged,
                        label = "Nombre",
                        isError = state.nameError != null,
                        supportingText = state.nameError,
                        imeAction = ImeAction.Next
                    )
                }

                AuthTextField(
                    value = state.email,
                    onValueChange = onEmailChanged,
                    label = "Correo electrónico",
                    isError = state.emailError != null,
                    supportingText = state.emailError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    )
                )

                PasswordField(
                    value = state.password,
                    onValueChange = onPasswordChanged,
                    label = "Contraseña",
                    isError = state.passwordError != null,
                    supportingText = state.passwordError,
                    imeAction = if (state.mode == AuthMode.LOGIN) ImeAction.Done else ImeAction.Next
                )

                if (state.mode == AuthMode.REGISTER) {
                    PasswordField(
                        value = state.confirmPassword,
                        onValueChange = onConfirmPasswordChanged,
                        label = "Confirmar contraseña",
                        isError = state.confirmPasswordError != null,
                        supportingText = state.confirmPasswordError,
                        imeAction = ImeAction.Done
                    )
                }

                if (state.generalError != null) {
                    ErrorMessage(text = state.generalError)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onSubmit,
                    enabled = !state.isSubmitting,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    if (state.isSubmitting) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(text = state.mode.actionText)
                    }
                }

                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TextButton(onClick = onToggleMode, enabled = !state.isSubmitting) {
                        val toggleText = if (state.mode == AuthMode.LOGIN) {
                            "¿No tienes cuenta? Regístrate"
                        } else {
                            "¿Ya tienes cuenta? Inicia sesión"
                        }
                        Text(text = toggleText)
                    }
                }
            }
        }
    }
}

@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isError: Boolean,
    supportingText: String?,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    imeAction: ImeAction = ImeAction.Next
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(text = label) },
        isError = isError,
        supportingText = supportingText?.let { message ->
            { Text(text = message, color = MaterialTheme.colorScheme.error) }
        },
        keyboardOptions = keyboardOptions.copy(imeAction = imeAction),
        singleLine = true,
        colors = TextFieldDefaults.colors()
    )
}

@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isError: Boolean,
    supportingText: String?,
    imeAction: ImeAction
) {
    val (visible, setVisible) = rememberSaveable { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(text = label) },
        isError = isError,
        supportingText = supportingText?.let { message ->
            { Text(text = message, color = MaterialTheme.colorScheme.error) }
        },
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = { setVisible(!visible) }) {
                Icon(
                    imageVector = if (visible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                    contentDescription = if (visible) "Ocultar contraseña" else "Mostrar contraseña"
                )
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = imeAction
        ),
        colors = TextFieldDefaults.colors()
    )
}

@Composable
private fun ErrorMessage(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        shape = MaterialTheme.shapes.medium
    ) {
        Text(
            text = text,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            color = MaterialTheme.colorScheme.onErrorContainer,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Preview
@Composable
private fun AuthScreenLoginPreview() {
    App_FinanzasTheme {
        AuthScreen(
            state = AuthUiState(),
            onNameChanged = {},
            onEmailChanged = {},
            onPasswordChanged = {},
            onConfirmPasswordChanged = {},
            onSubmit = {},
            onToggleMode = {}
        )
    }
}

@Preview
@Composable
private fun AuthScreenRegisterPreview() {
    App_FinanzasTheme {
        AuthScreen(
            state = AuthUiState(mode = AuthMode.REGISTER),
            onNameChanged = {},
            onEmailChanged = {},
            onPasswordChanged = {},
            onConfirmPasswordChanged = {},
            onSubmit = {},
            onToggleMode = {}
        )
    }
}
