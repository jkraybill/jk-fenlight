package com.fenlight.companion.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF81D4FA),
    onPrimary = Color(0xFF003549),
    primaryContainer = Color(0xFF004D65),
    onPrimaryContainer = Color(0xFFBFE9FF),
    secondary = Color(0xFFB3C8D4),
    onSecondary = Color(0xFF1D333D),
    secondaryContainer = Color(0xFF334A54),
    onSecondaryContainer = Color(0xFFCFE4F0),
    tertiary = Color(0xFFC4C3EA),
    onTertiary = Color(0xFF2D2D4D),
    background = Color(0xFF0F1923),
    onBackground = Color(0xFFDEE3E9),
    surface = Color(0xFF0F1923),
    onSurface = Color(0xFFDEE3E9),
    surfaceVariant = Color(0xFF1C2B35),
    onSurfaceVariant = Color(0xFFB5CAD4),
    outline = Color(0xFF50677A),
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF006688),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFBFE9FF),
    onPrimaryContainer = Color(0xFF001F2B),
    secondary = Color(0xFF4D6470),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD0E9F7),
    onSecondaryContainer = Color(0xFF081E28),
    tertiary = Color(0xFF5C5C7E),
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFF5F9FC),
    onBackground = Color(0xFF171C1F),
    surface = Color(0xFFF5F9FC),
    onSurface = Color(0xFF171C1F),
    surfaceVariant = Color(0xFFDCE4EA),
    onSurfaceVariant = Color(0xFF40494F),
    outline = Color(0xFF70797E),
)

@Composable
fun FenLightTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography(),
        content = content,
    )
}
