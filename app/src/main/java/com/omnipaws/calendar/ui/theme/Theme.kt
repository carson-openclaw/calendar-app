package com.omnipaws.calendar.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val AuraLightColorScheme = lightColorScheme(
    primary = Accent,
    onPrimary = Paper,
    primaryContainer = AccentLight,
    onPrimaryContainer = Ink,

    secondary = PaperDim,
    onSecondary = Ink,
    secondaryContainer = OutlineVariant,
    onSecondaryContainer = InkLight,

    tertiary = Muted,
    onTertiary = Paper,
    tertiaryContainer = PaperDim,
    onTertiaryContainer = Ink,

    background = Paper,
    onBackground = Ink,

    surface = Paper,
    onSurface = Ink,
    surfaceVariant = PaperDim,
    onSurfaceVariant = Muted,

    outline = Outline,
    outlineVariant = OutlineVariant,

    error = Color(0xFFB3261E),
    onError = Paper,
)

@Composable
fun AuraTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    // Only touch the window when a real Activity backs the view (also true in
    // layoutlib/Paparazzi edit-mode where context is a BridgeContext, not an Activity).
    val context = view.context
    if (context is Activity) {
        SideEffect {
            val window = (context as Activity).window
            window.statusBarColor = Paper.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
            window.navigationBarColor = Paper.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = true
        }
    }

    MaterialTheme(
        colorScheme = AuraLightColorScheme,
        typography = AuraTypography,
        shapes = AuraShapes,
        content = content
    )
}
