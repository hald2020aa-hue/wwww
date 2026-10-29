package org.koitharu.kotatsu.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = KotatsuPrimary,
    onPrimary = KotatsuDarkBackground,
    primaryContainer = KotatsuPrimaryVariant,
    secondary = KotatsuSecondary,
    onSecondary = KotatsuDarkBackground,
    secondaryContainer = KotatsuSecondaryContainer,
    tertiary = KotatsuTertiary,
    background = KotatsuDarkBackground,
    onBackground = KotatsuOnBackground,
    surface = KotatsuDarkSurface,
    onSurface = KotatsuOnSurface,
    surfaceVariant = KotatsuDarkSurfaceVariant,
    onSurfaceVariant = KotatsuOnSurfaceVariant,
    surfaceContainer = KotatsuDarkSurfaceContainer,
    error = KotatsuError
)

private val LightColorScheme = lightColorScheme(
    primary = KotatsuLightPrimary,
    onPrimary = KotatsuLightSurface,
    secondary = KotatsuLightSecondary,
    background = KotatsuLightBackground,
    onBackground = KotatsuLightOnBackground,
    surface = KotatsuLightSurface,
    onSurface = KotatsuLightOnSurface,
    surfaceVariant = KotatsuLightSurfaceVariant,
    onSurfaceVariant = KotatsuLightOnSurfaceVariant
)

@Composable
fun KotatsuTheme(
    darkTheme: Boolean = true, // Default to Kotatsu's signature dark manga theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SSSXTypography,
        content = content
    )
}
