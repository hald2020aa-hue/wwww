package org.koitharu.kotatsu.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class ReadingMode {
    WEBTOON,      // Continuous vertical scroll
    PAGED_RTL,    // Traditional manga (right-to-left)
    PAGED_LTR     // Western comic (left-to-right)
}

@Serializable
enum class ReaderBackground {
    BLACK,        // OLED pure black (#000000)
    DARK_GRAY,    // Dark gray (#181920)
    WHITE         // Pure white (#FFFFFF)
}

@Serializable
data class ReaderSettings(
    val readingMode: ReadingMode = ReadingMode.WEBTOON,
    val background: ReaderBackground = ReaderBackground.BLACK,
    val keepScreenOn: Boolean = true,
    val showPageNumber: Boolean = true,
    val invertTapZones: Boolean = false,
    val volumeKeyNavigation: Boolean = false
)
