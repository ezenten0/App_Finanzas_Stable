package com.example.app_finanzas.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = GreenPrimaryDark,
    onPrimary = Color(0xFF00382B),
    primaryContainer = GreenPrimaryContainerDark,
    onPrimaryContainer = GreenPrimaryLight,
    secondary = TealSecondaryDark,
    onSecondary = Color(0xFF00382C),
    secondaryContainer = TealSecondaryContainerDark,
    onSecondaryContainer = TealSecondaryLight,
    tertiary = BlueTertiaryDark,
    onTertiary = Color(0xFF0C2F56),
    tertiaryContainer = BlueTertiaryContainerDark,
    onTertiaryContainer = BlueTertiaryLight,
    background = DarkBackground,
    onBackground = Color(0xFFC6CEC9),
    surface = DarkSurface,
    onSurface = Color(0xFFC6CEC9),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = OutlineDark,
    error = Error,
    onError = OnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = ErrorContainer
)

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimaryLight,
    onPrimary = Color.White,
    primaryContainer = GreenPrimaryContainerLight,
    onPrimaryContainer = Color(0xFF002117),
    secondary = TealSecondaryLight,
    onSecondary = Color.White,
    secondaryContainer = TealSecondaryContainerLight,
    onSecondaryContainer = Color(0xFF07201A),
    tertiary = BlueTertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = BlueTertiaryContainerLight,
    onTertiaryContainer = Color(0xFF001C3C),
    background = LightBackground,
    onBackground = Color(0xFF1C211E),
    surface = LightSurface,
    onSurface = Color(0xFF1C211E),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = OutlineLight,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer
)

@Composable
fun App_FinanzasTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
