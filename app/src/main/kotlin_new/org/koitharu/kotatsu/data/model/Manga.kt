package org.koitharu.kotatsu.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class MangaStatus {
    ONGOING,
    COMPLETED,
    SUSPENDED,
    UNKNOWN
}

@Serializable
data class Category(
    val id: String,
    val name: String,
    val order: Int = 0
)

@Serializable
data class Manga(
    val id: String,
    val title: String,
    val altTitle: String = "",
    val author: String = "Unknown Author",
    val artist: String = "",
    val description: String = "",
    val coverUrl: String,
    val status: MangaStatus = MangaStatus.ONGOING,
    val genres: List<String> = emptyList(),
    val source: String = "MangaDex",
    val rating: Float = 4.8f,
    val totalChapters: Int = 0,
    val categoryId: String = "reading",
    val isFavorite: Boolean = false,
    val lastReadChapterId: String? = null,
    val lastReadPage: Int = 1
)

@Serializable
data class Chapter(
    val id: String,
    val mangaId: String,
    val number: Float,
    val title: String,
    val scanlator: String = "",
    val uploadDate: String = "Recent",
    val pageCount: Int = 10,
    val isRead: Boolean = false,
    val isDownloaded: Boolean = false,
    val lastReadPage: Int = 1
)

@Serializable
data class MangaPage(
    val pageNumber: Int,
    val imageUrl: String
)

@Serializable
data class HistoryItem(
    val mangaId: String,
    val mangaTitle: String,
    val coverUrl: String,
    val chapterId: String,
    val chapterNumber: Float,
    val chapterTitle: String,
    val currentPage: Int,
    val totalPages: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class Bookmark(
    val id: String,
    val mangaId: String,
    val mangaTitle: String,
    val coverUrl: String,
    val chapterId: String,
    val chapterTitle: String,
    val pageNumber: Int,
    val imageUrl: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class MangaSource(
    val id: String,
    val name: String,
    val baseUrl: String,
    val language: String = "EN",
    val isEnabled: Boolean = true
)
