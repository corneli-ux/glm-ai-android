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

// Brand palette — premium emerald + amber
private val Emerald = Color(0xFF10B981)
private val EmeraldLight = Color(0xFF34D399)
private val EmeraldDark = Color(0xFF059669)
private val EmeraldContainer = Color(0xFF064E3B)
private val Amber = Color(0xFFF59E0B)
private val AmberContainer = Color(0xFF78350F)

// Neutrals
private val InkDark = Color(0xFF0F172A)
private val InkLight = Color(0xFFF8FAFC)
private val SlateDark = Color(0xFF1E293B)
private val SlateLight = Color(0xFFE2E8F0)

// Light scheme — clean white with emerald accents
private val LightColors = lightColorScheme(
    primary = EmeraldDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1FAE5),
    onPrimaryContainer = Color(0xFF064E3B),
    secondary = Amber,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = Color(0xFF78350F),
    tertiary = Color(0xFF0EA5E9),
    onTertiary = Color.White,
    background = InkLight,
    onBackground = InkDark,
    surface = Color.White,
    onSurface = InkDark,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0),
    error = Color(0xFFDC2626),
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF7F1D1D),
    scrim = Color(0xFF000000)
)

// Dark scheme — deep slate with emerald glow
private val DarkColors = darkColorScheme(
    primary = EmeraldLight,
    onPrimary = Color(0xFF00382A),
    primaryContainer = EmeraldContainer,
    onPrimaryContainer = Color(0xFF6EE7B7),
    secondary = Amber,
    onSecondary = Color(0xFF451A03),
    secondaryContainer = AmberContainer,
    onSecondaryContainer = Color(0xFFFDE68A),
    tertiary = Color(0xFF38BDF8),
    onTertiary = Color(0xFF0C2A3A),
    background = InkDark,
    onBackground = Color(0xFFE2E8F0),
    surface = SlateDark,
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF64748B),
    outlineVariant = Color(0xFF334155),
    error = Color(0xFFFCA5A5),
    onError = Color(0xFF450A0A),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFCA5A5),
    scrim = Color(0xFF000000)
)

@Composable
fun GLMTheme(
    useDark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val scheme = if (useDark) DarkColors else LightColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        androidx.compose.runtime.SideEffect {
            val window = (view.context as? android.app.Activity)?.window ?: return@SideEffect
            window.statusBarColor = scheme.background.toArgb()
            window.navigationBarColor = scheme.background.toArgb()
            val controller = WindowCompat.getInsetsController(window, view)
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
