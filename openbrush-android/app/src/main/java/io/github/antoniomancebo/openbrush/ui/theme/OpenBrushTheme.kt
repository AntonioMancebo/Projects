package io.github.antoniomancebo.openbrush.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF006C62),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF9EF2E4),
    onPrimaryContainer = Color(0xFF00201C),
    secondary = Color(0xFF4A635E),
    secondaryContainer = Color(0xFFCCE8E1),
    tertiary = Color(0xFF456179),
    tertiaryContainer = Color(0xFFCBE6FF),
    background = Color(0xFFF6FBF9),
    surface = Color(0xFFF6FBF9),
    surfaceContainer = Color(0xFFEFF5F2),
    surfaceContainerHigh = Color(0xFFE8EFEC),
    surfaceContainerHighest = Color(0xFFE1E9E6),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF82D5C8),
    onPrimary = Color(0xFF003731),
    primaryContainer = Color(0xFF005048),
    onPrimaryContainer = Color(0xFF9EF2E4),
    secondary = Color(0xFFB1CCC5),
    secondaryContainer = Color(0xFF334B47),
    tertiary = Color(0xFFADCBE5),
    tertiaryContainer = Color(0xFF2D4960),
    background = Color(0xFF101412),
    surface = Color(0xFF101412),
    surfaceContainer = Color(0xFF171C1A),
    surfaceContainerHigh = Color(0xFF202624),
    surfaceContainerHighest = Color(0xFF2B312F),
)

@Composable
fun OpenBrushTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content,
    )
}
