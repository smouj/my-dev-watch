package io.github.smouj.mydevwatch.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Brand palette: graphite surfaces, off-white text, a single teal accent.
 * No neon, no gradients — the numbers on screen have to stay legible at a glance.
 */
object Palette {
    val Graphite = Color(0xFF0C1014)
    val SurfaceDark = Color(0xFF151B21)
    val SurfaceDarkHigh = Color(0xFF1D252C)
    val OffWhite = Color(0xFFE9EDF2)
    val Muted = Color(0xFF9AA5B1)
    val Teal = Color(0xFF2FB6C4)
    val TealDeep = Color(0xFF0F6F79)
    val Amber = Color(0xFFD79A3B)
    val Red = Color(0xFFD2564B)
    val Green = Color(0xFF4FB477)

    val LightBackground = Color(0xFFF4F6F8)
    val LightSurface = Color(0xFFFFFFFF)
    val Ink = Color(0xFF12181E)
}

private val DarkScheme = darkColorScheme(
    primary = Palette.Teal,
    onPrimary = Color(0xFF002B30),
    secondary = Palette.Muted,
    background = Palette.Graphite,
    onBackground = Palette.OffWhite,
    surface = Palette.SurfaceDark,
    onSurface = Palette.OffWhite,
    surfaceVariant = Palette.SurfaceDarkHigh,
    onSurfaceVariant = Palette.Muted,
    error = Palette.Red,
    outline = Color(0xFF2B343D)
)

private val LightScheme = lightColorScheme(
    primary = Palette.TealDeep,
    onPrimary = Color.White,
    secondary = Color(0xFF4A5560),
    background = Palette.LightBackground,
    onBackground = Palette.Ink,
    surface = Palette.LightSurface,
    onSurface = Palette.Ink,
    surfaceVariant = Color(0xFFE6EBEF),
    onSurfaceVariant = Color(0xFF4A5560),
    error = Palette.Red,
    outline = Color(0xFFC7D0D6)
)

private val AppTypography = Typography(
    displaySmall = TextStyle(fontSize = 34.sp, lineHeight = 38.sp, fontWeight = FontWeight.SemiBold),
    headlineMedium = TextStyle(fontSize = 26.sp, lineHeight = 30.sp, fontWeight = FontWeight.SemiBold),
    titleLarge = TextStyle(fontSize = 20.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 16.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),
    bodyLarge = TextStyle(fontSize = 15.sp, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontSize = 13.sp, lineHeight = 19.sp),
    labelLarge = TextStyle(fontSize = 13.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium),
    labelSmall = TextStyle(fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.6.sp)
)

@Composable
fun MyDevWatchTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkScheme else LightScheme,
        typography = AppTypography,
        content = content
    )
}
