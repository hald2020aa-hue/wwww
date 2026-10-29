package org.koitharu.kotatsu.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class ReadingMode {
    WEBTOON,      // Continuous vertical scroll (ويبتون)
    PAGED_RTL,    // Traditional manga (من اليمين لليسار)
    PAGED_LTR,    // Western comic (من اليسار لليمين)
    CONTINUOUS    // مستمر
}

@Serializable
enum class ReaderBackground {
    BLACK,        // OLED pure black (#000000)
    DARK_GRAY,    // Dark gray (#14151C)
    WHITE         // Pure white (#FFFFFF)
}

@Serializable
enum class ZoomMode {
    FIT_WIDTH,   // مناسب للعرض
    SMART,       // ذكي
    FIT_CENTER   // مناسب للمركز
}

@Serializable
data class ReaderSettings(
    val readingMode: ReadingMode = ReadingMode.PAGED_RTL,
    val background: ReaderBackground = ReaderBackground.BLACK,
    val zoomMode: ZoomMode = ZoomMode.FIT_WIDTH,
    val doubleTapZoom: Boolean = true,
    val webtoonZoom: Boolean = true,
    val webtoonZoomLevel: Float = 0f,
    val showGapsInWebtoon: Boolean = false,
    val gestureNavigation: Boolean = true,
    val volumeKeyNavigation: Boolean = false,
    val swipeChapterTransition: Boolean = false,
    val isRtl: Boolean = true,
    val full32BitColors: Boolean = false,
    val fullScreenMode: Boolean = true,
    val keepScreenOn: Boolean = true,
    val transparentInfoBar: Boolean = false,
    val showChapterChangePopup: Boolean = true,

    // Real-Time Arabic Translation Features
    val isTranslationEnabled: Boolean = false,
    val translationSourceLang: String = "ja", // ja (يابانية), ko (كورية), zh (صينية), en (إنجليزية)
    val showSpeechBubbleOverlay: Boolean = true,

    // SSS X Neon Appearance & Customization
    val neonThemeId: NeonThemeId = NeonThemeId.ELECTRIC_BLUE_PURPLE,
    val customPrimaryColorHex: String = "#1B6BFF",
    val customSecondaryColorHex: String = "#8A2BE2",
    val reducedMotionAndGlow: Boolean = false,
    val isIncognitoMode: Boolean = false,
    val gridColumns: Int = 2,
    val listMode: String = "grid" // grid, detailed, compact
)
