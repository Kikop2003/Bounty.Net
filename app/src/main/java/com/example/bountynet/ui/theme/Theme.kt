package com.example.bountynet.ui.theme


import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable


private val DarkColorScheme = darkColorScheme(
    primary = NeonRose,
    secondary = NeonRose,
    tertiary = VibrantGolden,
    background = DarkBlue, // Preto profundo com tom azulado
    surface = DarkPurple, // Roxo escuro
    surfaceVariant = Purple,
    onPrimary = Black, // Preto para contraste
    onSecondary = White, // Branco para contraste
    onTertiary = Black, // Preto para contraste
    onBackground = White, // Branco para texto no fundo
    onSurface = White // Branco para texto na superfície
)

@Composable
fun bountyNetTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {


    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        shapes = shapes,
        content = content
    )
}