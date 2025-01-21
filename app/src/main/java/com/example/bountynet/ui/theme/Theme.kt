package com.example.bountynet.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

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

private val LightColorScheme = lightColorScheme(
    primary = NeonCyan , // Ciano neon
    secondary = NeonRose, // Rosa neon
    tertiary = VibrantGolden, // Dourado vibrante
    background = DarkPurple, // Branco - alterei pra roxo
    surface = LightGray, // Cinza claro
    onPrimary = Black, // Preto para contraste
    onSecondary = White, // Branco para contraste
    onTertiary = Black, // Preto para contraste
    onBackground = Black, // Preto para texto no fundo
    onSurface = Black // Preto para texto na superfície

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun bountyNetTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
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
        shapes = shapes,
        content = content
    )
}