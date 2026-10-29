package org.koitharu.kotatsu.data.model

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable

@Serializable
enum class NeonThemeId {
    ELECTRIC_BLUE_PURPLE, // Default
    ELECTRIC_BLUE,
    NEON_PURPLE,
    CYBERPUNK_PINK,
    EMERALD_GREEN,
    NEON_RED,
    GOLDEN_AMBER,
    ICE_CYAN,
    TOXIC_LIME,
    AURORA,
    GALAXY,
    CYBER_BLUE_PINK,
    MONOCHROME_NEON,
    CUSTOM
}

data class NeonColorPalette(
    val id: NeonThemeId,
    val nameAr: String,
    val nameEn: String,
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val glow: Color,
    val border: Color,
    val background: Color = Color(0xFF040714),
    val surface: Color = Color(0xFF070C20),
    val surfaceCard: Color = Color(0xFF0B122E).copy(alpha = 0.88f),
    val textPrimary: Color = Color(0xFFF1F5F9),
    val textSecondary: Color = Color(0xFF94A3B8)
) {
    val primaryGradient: Brush
        get() = Brush.horizontalGradient(listOf(primary, secondary))

    val borderGradient: Brush
        get() = Brush.linearGradient(
            colors = listOf(
                primary.copy(alpha = 0.85f),
                secondary.copy(alpha = 0.50f),
                tertiary.copy(alpha = 0.85f)
            )
        )

    val backgroundGradient: Brush
        get() = Brush.verticalGradient(
            colors = listOf(
                background,
                surface,
                Color(0xFF02040A)
            )
        )
}

object NeonThemes {
    val allThemes = listOf(
        NeonColorPalette(
            id = NeonThemeId.ELECTRIC_BLUE_PURPLE,
            nameAr = "أزرق كهربائي بنفسجي (الافتراضي)",
            nameEn = "Electric Blue-Purple",
            primary = Color(0xFF1B6BFF),
            secondary = Color(0xFF8A2BE2),
            tertiary = Color(0xFF00E5FF),
            glow = Color(0xFF1B6BFF),
            border = Color(0xFF2979FF)
        ),
        NeonColorPalette(
            id = NeonThemeId.ELECTRIC_BLUE,
            nameAr = "أزرق كهربائي",
            nameEn = "Electric Blue",
            primary = Color(0xFF0088FF),
            secondary = Color(0xFF00E5FF),
            tertiary = Color(0xFF2979FF),
            glow = Color(0xFF00B0FF),
            border = Color(0xFF00B0FF)
        ),
        NeonColorPalette(
            id = NeonThemeId.NEON_PURPLE,
            nameAr = "بنفسجي نيون",
            nameEn = "Neon Purple",
            primary = Color(0xFF9D00FF),
            secondary = Color(0xFFD600FF),
            tertiary = Color(0xFF7C4DFF),
            glow = Color(0xFFAA00FF),
            border = Color(0xFFB388FF)
        ),
        NeonColorPalette(
            id = NeonThemeId.CYBERPUNK_PINK,
            nameAr = "وردي سايبربانك",
            nameEn = "Cyberpunk Pink",
            primary = Color(0xFFFF007F),
            secondary = Color(0xFFFF0055),
            tertiary = Color(0xFFFF4081),
            glow = Color(0xFFFF007F),
            border = Color(0xFFFF4081)
        ),
        NeonColorPalette(
            id = NeonThemeId.EMERALD_GREEN,
            nameAr = "أخضر زمردي",
            nameEn = "Emerald Green",
            primary = Color(0xFF00E676),
            secondary = Color(0xFF00BFA5),
            tertiary = Color(0xFF69F0AE),
            glow = Color(0xFF00E676),
            border = Color(0xFF00E676)
        ),
        NeonColorPalette(
            id = NeonThemeId.NEON_RED,
            nameAr = "أحمر نيون",
            nameEn = "Neon Red",
            primary = Color(0xFFFF1744),
            secondary = Color(0xFFFF5252),
            tertiary = Color(0xFFD50000),
            glow = Color(0xFFFF1744),
            border = Color(0xFFFF5252)
        ),
        NeonColorPalette(
            id = NeonThemeId.GOLDEN_AMBER,
            nameAr = "ذهبي وكهرماني",
            nameEn = "Golden Amber",
            primary = Color(0xFFFFAB00),
            secondary = Color(0xFFFF6D00),
            tertiary = Color(0xFFFFD700),
            glow = Color(0xFFFFAB00),
            border = Color(0xFFFFD54F)
        ),
        NeonColorPalette(
            id = NeonThemeId.ICE_CYAN,
            nameAr = "سماوي جليدي",
            nameEn = "Ice Cyan",
            primary = Color(0xFF00F5FF),
            secondary = Color(0xFF00B4D8),
            tertiary = Color(0xFF90E0EF),
            glow = Color(0xFF00F5FF),
            border = Color(0xFF48CAE4)
        ),
        NeonColorPalette(
            id = NeonThemeId.TOXIC_LIME,
            nameAr = "أخضر ليموني نيون",
            nameEn = "Toxic Lime",
            primary = Color(0xFF76FF03),
            secondary = Color(0xFFAEEA00),
            tertiary = Color(0xFFC6FF00),
            glow = Color(0xFF76FF03),
            border = Color(0xFFB2FF59)
        ),
        NeonColorPalette(
            id = NeonThemeId.AURORA,
            nameAr = "شفق قطبي (Aurora)",
            nameEn = "Aurora",
            primary = Color(0xFF00F2FE),
            secondary = Color(0xFF9933FF),
            tertiary = Color(0xFFFF007F),
            glow = Color(0xFF4FACFE),
            border = Color(0xFF00F2FE)
        ),
        NeonColorPalette(
            id = NeonThemeId.GALAXY,
            nameAr = "مجرة نيون",
            nameEn = "Galaxy",
            primary = Color(0xFF3D5AFE),
            secondary = Color(0xFF7C4DFF),
            tertiary = Color(0xFFE040FB),
            glow = Color(0xFF651FFF),
            border = Color(0xFF7C4DFF)
        ),
        NeonColorPalette(
            id = NeonThemeId.CYBER_BLUE_PINK,
            nameAr = "سايبر أزرق ووردي",
            nameEn = "Cyber Blue-Pink",
            primary = Color(0xFF00E5FF),
            secondary = Color(0xFFFF007F),
            tertiary = Color(0xFF7C4DFF),
            glow = Color(0xFF00E5FF),
            border = Color(0xFF00E5FF)
        ),
        NeonColorPalette(
            id = NeonThemeId.MONOCHROME_NEON,
            nameAr = "أبيض فضي نيون",
            nameEn = "Monochrome Neon",
            primary = Color(0xFFE2E8F0),
            secondary = Color(0xFF818CF8),
            tertiary = Color(0xFFF8FAFC),
            glow = Color(0xFFC7D2FE),
            border = Color(0xFFE2E8F0)
        )
    )

    fun getTheme(id: NeonThemeId): NeonColorPalette {
        return allThemes.find { it.id == id } ?: allThemes.first()
    }
}
