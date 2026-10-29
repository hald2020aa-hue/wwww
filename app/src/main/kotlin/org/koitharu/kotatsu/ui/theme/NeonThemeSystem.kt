package org.koitharu.kotatsu.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import org.koitharu.kotatsu.data.model.NeonColorPalette
import org.koitharu.kotatsu.data.model.NeonThemeId
import org.koitharu.kotatsu.data.model.NeonThemes

val LocalNeonPalette = staticCompositionLocalOf {
    NeonThemes.getTheme(NeonThemeId.ELECTRIC_BLUE_PURPLE)
}

@Composable
fun SSSXTheme(
    neonPalette: NeonColorPalette = NeonThemes.getTheme(NeonThemeId.ELECTRIC_BLUE_PURPLE),
    content: @Composable () -> Unit
) {
    val materialScheme = darkColorScheme(
        primary = neonPalette.primary,
        onPrimary = Color.White,
        primaryContainer = neonPalette.primary.copy(alpha = 0.25f),
        onPrimaryContainer = neonPalette.primary,
        secondary = neonPalette.secondary,
        onSecondary = Color.White,
        secondaryContainer = neonPalette.secondary.copy(alpha = 0.25f),
        onSecondaryContainer = neonPalette.secondary,
        tertiary = neonPalette.tertiary,
        background = neonPalette.background,
        onBackground = neonPalette.textPrimary,
        surface = neonPalette.surface,
        onSurface = neonPalette.textPrimary,
        surfaceVariant = neonPalette.surfaceCard,
        onSurfaceVariant = neonPalette.textSecondary,
        outline = neonPalette.border.copy(alpha = 0.45f)
    )

    CompositionLocalProvider(
        LocalNeonPalette provides neonPalette
    ) {
        MaterialTheme(
            colorScheme = materialScheme,
            typography = SSSXTypography,
            content = content
        )
    }
}
