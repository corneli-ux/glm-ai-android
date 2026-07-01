package com.glm.aiapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Black & white minimalist palette
private val Black = Color(0xFF000000)
private val DarkGray = Color(0xFF0A0A0A)
private val MediumGray = Color(0xFF1A1A1A)
private val LightGray = Color(0xFF2A2A2A)
private val BorderGray = Color(0xFF333333)
private val TextGray = Color(0xFF888888)
private val White = Color(0xFFFFFFFF)
private val OffWhite = Color(0xFFE0E0E0)

// Always dark — black theme
private val BlackColors = darkColorScheme(
    primary = White,
    onPrimary = Black,
    primaryContainer = LightGray,
    onPrimaryContainer = White,
    secondary = TextGray,
    onSecondary = White,
    secondaryContainer = MediumGray,
    onSecondaryContainer = OffWhite,
    tertiary = White,
    onTertiary = Black,
    background = Black,
    onBackground = White,
    surface = DarkGray,
    onSurface = White,
    surfaceVariant = MediumGray,
    onSurfaceVariant = TextGray,
    outline = BorderGray,
    outlineVariant = Color(0xFF222222),
    error = Color(0xFFFF4444),
    onError = White,
    errorContainer = Color(0xFF1A0000),
    onErrorContainer = Color(0xFFFF9999),
    scrim = Black
)

@Composable
fun GLMTheme(
    useDark: Boolean = true, // Always dark — black theme
    content: @Composable () -> Unit
) {
    val scheme = BlackColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        androidx.compose.runtime.SideEffect {
            val window = (view.context as? android.app.Activity)?.window ?: return@SideEffect
            window.statusBarColor = Black.toArgb()
            window.navigationBarColor = Black.toArgb()
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = false
            controller.isAppearanceLightNavigationBars = false
        }
    }
    MaterialTheme(
        colorScheme = scheme,
        typography = GLMTypography,
        content = content
    )
}
