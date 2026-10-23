package gt.cocina.cocinagt.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Verde chapín y acento ámbar de la marca CocinaGT. */
val CocinaGreen = Color(0xFF008559)
val CocinaGreenDark = Color(0xFF00603F)
val CocinaAccent = Color(0xFFF59E1C)

private val LightColors = lightColorScheme(
    primary = CocinaGreen,
    onPrimary = Color.White,
    secondary = CocinaAccent,
    onSecondary = Color.White,
    primaryContainer = Color(0xFFB8F0D6),
    onPrimaryContainer = Color(0xFF002013)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF5FD9A6),
    onPrimary = Color(0xFF003824),
    secondary = CocinaAccent,
    primaryContainer = CocinaGreenDark,
    onPrimaryContainer = Color(0xFFB8F0D6)
)

@Composable
fun CocinaGTTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = MaterialTheme.typography,
        content = content
    )
}
