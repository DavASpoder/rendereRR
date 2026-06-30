package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val SophisticatedDarkColorScheme = darkColorScheme(
    primary = AccentBlue,
    onPrimary = AccentText,
    background = DarkBg,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    outline = DarkBorder,
    secondary = InfoBlue,
    tertiary = SuccessGreen
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark theme for Sophisticated Dark
    dynamicColor: Boolean = false, // Disable dynamic colors to ensure the specific color theme holds
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = SophisticatedDarkColorScheme,
        typography = Typography,
        content = content
    )
}
