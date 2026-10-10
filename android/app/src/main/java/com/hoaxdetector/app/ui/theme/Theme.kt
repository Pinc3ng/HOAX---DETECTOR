package com.hoaxdetector.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val EditorialColorScheme = lightColorScheme(
    primary = StitchPrimary,
    onPrimary = StitchOnPrimary,
    primaryContainer = StitchPrimaryContainer,
    onPrimaryContainer = StitchOnPrimaryContainer,
    secondary = StitchSecondary,
    onSecondary = StitchOnSecondary,
    secondaryContainer = StitchSecondaryContainer,
    onSecondaryContainer = StitchOnSecondaryContainer,
    tertiary = StitchTertiary,
    background = StitchSurface,
    onBackground = StitchOnSurface,
    surface = StitchSurfaceContainerLowest,
    onSurface = StitchOnSurface,
    surfaceVariant = StitchSurfaceContainer,
    onSurfaceVariant = StitchOnSurfaceVariant,
    error = StatusRed,
    onError = White,
    outline = StitchOutline,
    outlineVariant = StitchOutlineVariant
)

@Composable
fun HoaxDetectorTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = EditorialColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = StitchSurface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}

