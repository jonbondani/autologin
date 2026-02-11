package com.autologin.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Blue700,
    onPrimary = Grey50,
    primaryContainer = Blue600,
    secondary = Green500,
    error = Red500,
    background = Grey50,
    surface = Grey100,
    onBackground = Grey900,
    onSurface = Grey900,
)

private val DarkColorScheme = darkColorScheme(
    primary = Blue600,
    onPrimary = Grey50,
    primaryContainer = Blue800,
    secondary = Green500,
    error = Red500,
)

@Composable
fun AutoLoginTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
