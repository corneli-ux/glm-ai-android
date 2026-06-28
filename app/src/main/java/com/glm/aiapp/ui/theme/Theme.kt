package com.glm.aiapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

// Brand palette
private val Emerald = Color(0xFF0F766E)
private val EmeraldDark = Color(0xFF115E59)
private val Amber = Color(0xFFF59E0B)
private val Sand = Color(0xFFFEF3C7)

// Light scheme
private val LightColors = lightColorScheme(
    primary = Emerald,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCCFBF1),
    onPrimaryContainer = Color(0xFF002B27),
    secondary = Amber,
    onSecondary = Color.Black,
    secondaryContainer = Sand,
    onSecondaryContainer = Color(0xFF3A2A00),
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFF94A3B8),
    error = Color(0xFFDC2626),
    onError = Color.White
)

// Dark scheme
private val DarkColors = darkColorScheme(
    primary = Color(0xFF5EEAD4),
    onPrimary = Color(0xFF003731),
    primaryContainer = EmeraldDark,
    onPrimaryContainer = Color(0xFF6FF7E2),
    secondary = Amber,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF7A5800),
    onSecondaryContainer = Sand,
    background = Color(0xFF0B1220),
    onBackground = Color(0xFFE2E8F0),
    surface = Color(0xFF111827),
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = Color(0xFF1F2937),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF64748B),
    error = Color(0xFFFCA5A5),
    onError = Color(0xFF1F0808)
)

@Composable
fun GLMTheme(
    useDark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val scheme = if (useDark) DarkColors else LightColors
    val view = androidx.compose.ui.platform.LocalView.current
    if (!view.isInEditMode) {
        androidx.compose.runtime.SideEffect {
            val window = (view.context as? android.app.Activity)?.window ?: return@SideEffect
            window.statusBarColor = scheme.background.toArgb()
            window.navigationBarColor = scheme.background.toArgb()
            // Use WindowInsetsControllerCompat for light/dark bar icons (works on API 26+)
            val controller = androidx.core.view.WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !useDark
            controller.isAppearanceLightNavigationBars = !useDark
        }
    }
    MaterialTheme(
        colorScheme = scheme,
        typography = GLMTypography,
        content = content
    )
}
