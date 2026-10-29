package org.koitharu.kotatsu.data.repository

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koitharu.kotatsu.data.model.*
import java.io.File

class MangaRepository(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }
    private val mangaDexApi = MangaDexApi()

    private val categoriesFile = File(context.filesDir, "categories.json")
    private val libraryFile = File(context.filesDir, "library.json")
    private val historyFile = File(context.filesDir, "history.json")
    private val bookmarksFile = File(context.filesDir, "bookmarks.json")
    private val settingsFile = File(context.filesDir, "reader_settings.json")

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _library = MutableStateFlow<List<Manga>>(emptyList())
    val library: StateFlow<List<Manga>> = _library.asStateFlow()

    private val _history = MutableStateFlow<List<HistoryItem>>(emptyList())
    val history: StateFlow<List<HistoryItem>> = _history.asStateFlow()

    private val _bookmarks = MutableStateFlow<List<Bookmark>>(emptyList())
    val bookmarks: StateFlow<List<Bookmark>> = _bookmarks.asStateFlow()

    private val _downloadedChapters = MutableStateFlow<Set<String>>(emptySet())
    val downloadedChapters: StateFlow<Set<String>> = _downloadedChapters.asStateFlow()

    private val _readChapters = MutableStateFlow<Set<String>>(emptySet())
    val readChapters: StateFlow<Set<String>> = _readChapters.asStateFlow()

    private val _readerSettings = MutableStateFlow(ReaderSettings())
    val readerSettings: StateFlow<ReaderSettings> = _readerSettings.asStateFlow()

    private val _exploreManga = MutableStateFlow<List<Manga>>(emptyList())
    val exploreManga: StateFlow<List<Manga>> = _exploreManga.asStateFlow()

    private val _isLoadingExplore = MutableStateFlow(false)
    val isLoadingExplore: StateFlow<Boolean> = _isLoadingExplore.asStateFlow()

    init {
        loadData()
        loadExplore()
    }

    private fun loadData() {
        // Load categories
        val defaultCategories = listOf(
            Category("all", "All", 0),
            Category("reading", "Reading", 1),
            Category("plan", "Plan to Read", 2),
            Category("completed", "Completed", 3),
            Category("favorites", "Favorites", 4)
        )
        if (categoriesFile.exists()) {
            try {
                val content = categoriesFile.readText()
                _categories.value = json.decodeFromString(content)
            } catch (e: Exception) {
                _categories.value = defaultCategories
            }
        } else {
            _categories.value = defaultCategories
            saveCategories()
        }

        // Load library
        if (libraryFile.exists()) {
            try {
                val content = libraryFile.readText()
                _library.value = json.decodeFromString(content)
            } catch (e: Exception) {
                _library.value = getCuratedSampleLibrary()
                saveLibrary()
            }
        } else {
            _library.value = getCuratedSampleLibrary()
            saveLibrary()
        }

        // Load history
        if (historyFile.exists()) {
            try {
                val content = historyFile.readText()
                _history.value = json.decodeFromString(content)
            } catch (e: Exception) {
                _history.value = getCuratedSampleHistory()
            }
        } else {
            _history.value = getCuratedSampleHistory()
            saveHistory()
        }

        // Load bookmarks
        if (bookmarksFile.exists()) {
            try {
                val content = bookmarksFile.readText()
                _bookmarks.value = json.decodeFromString(content)
            } catch (e: Exception) {
                _bookmarks.value = emptyList()
            }
        }

        // Load reader settings
        if (settingsFile.exists()) {
            try {
                val content = settingsFile.readText()
                _readerSettings.value = json.decodeFromString(content)
            } catch (e: Exception) {
                _readerSettings.value = ReaderSettings()
            }
        }
    }

    private fun saveCategories() {
        scope.launch {
            try {
                categoriesFile.writeText(json.encodeToString(_categories.value))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun saveLibrary() {
        scope.launch {
            try {
                libraryFile.writeText(json.encodeToString(_library.value))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun saveHistory() {
        scope.launch {
            try {
                historyFile.writeText(json.encodeToString(_history.value))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun saveBookmarks() {
        scope.launch {
            try {
                bookmarksFile.writeText(json.encodeToString(_bookmarks.value))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun saveReaderSettings(settings: ReaderSettings) {
        _readerSettings.value = settings
        scope.launch {
            try {
                settingsFile.writeText(json.encodeToString(settings))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun toggleFavorite(manga: Manga) {
        val current = _library.value
        val exists = current.any { it.id == manga.id }
        if (exists) {
            _library.value = current.filter { it.id != manga.id }
        } else {
            val updated = manga.copy(isFavorite = true, categoryId = "reading")
            _library.value = listOf(updated) + current
        }
        saveLibrary()
    }

    fun addToLibrary(manga: Manga, categoryId: String = "reading") {
        val current = _library.value
        val exists = current.find { it.id == manga.id }
        if (exists != null) {
            _library.value = current.map {
                if (it.id == manga.id) it.copy(categoryId = categoryId, isFavorite = true) else it
            }
        } else {
            val updated = manga.copy(categoryId = categoryId, isFavorite = true)
            _library.value = listOf(updated) + current
        }
        saveLibrary()
    }

    fun removeFromLibrary(mangaId: String) {
        _library.value = _library.value.filter { it.id != mangaId }
        saveLibrary()
    }

    fun setMangaCategory(mangaId: String, categoryId: String) {
        _library.value = _library.value.map {
            if (it.id == mangaId) it.copy(categoryId = categoryId) else it
        }
        saveLibrary()
    }

    fun addCategory(name: String) {
        val newId = name.lowercase().replace(" ", "_")
        if (_categories.value.any { it.id == newId }) return
        val newCategory = Category(newId, name, _categories.value.size)
        _categories.value = _categories.value + newCategory
        saveCategories()
    }

    fun removeCategory(categoryId: String) {
        if (categoryId in listOf("all", "reading", "plan", "completed", "favorites")) return
        _categories.value = _categories.value.filter { it.id != categoryId }
        // Remap manga in deleted category to "reading"
        _library.value = _library.value.map {
            if (it.categoryId == categoryId) it.copy(categoryId = "reading") else it
        }
        saveCategories()
        saveLibrary()
    }

    fun updateReadingProgress(
        manga: Manga,
        chapter: Chapter,
        page: Int,
        totalPages: Int
    ) {
        // Update history
        val currentHist = _history.value.filter { it.mangaId != manga.id }
        val newHist = HistoryItem(
            mangaId = manga.id,
            mangaTitle = manga.title,
            coverUrl = manga.coverUrl,
            chapterId = chapter.id,
            chapterNumber = chapter.number,
            chapterTitle = chapter.title,
            currentPage = page,
            totalPages = totalPages,
            timestamp = System.currentTimeMillis()
        )
        _history.value = listOf(newHist) + currentHist
        saveHistory()

        // Update manga in library if present
        _library.value = _library.value.map {
            if (it.id == manga.id) it.copy(lastReadChapterId = chapter.id, lastReadPage = page) else it
        }
        saveLibrary()

        // If completed all pages, mark chapter as read
        if (page >= totalPages) {
            markChapterRead(chapter.id, true)
        }
    }

    fun markChapterRead(chapterId: String, isRead: Boolean) {
        val current = _readChapters.value.toMutableSet()
        if (isRead) current.add(chapterId) else current.remove(chapterId)
        _readChapters.value = current
    }

    fun toggleDownload(chapterId: String) {
        val current = _downloadedChapters.value.toMutableSet()
        if (current.contains(chapterId)) {
            current.remove(chapterId)
        } else {
            current.add(chapterId)
        }
        _downloadedChapters.value = current
    }

    fun addBookmark(
        manga: Manga,
        chapter: Chapter,
        pageNumber: Int,
        imageUrl: String
    ) {
        val id = "${chapter.id}_$pageNumber"
        val existing = _bookmarks.value.filter { it.id != id }
        val bookmark = Bookmark(
            id = id,
            mangaId = manga.id,
            mangaTitle = manga.title,
            coverUrl = manga.coverUrl,
            chapterId = chapter.id,
            chapterTitle = chapter.title,
            pageNumber = pageNumber,
            imageUrl = imageUrl
        )
        _bookmarks.value = listOf(bookmark) + existing
        saveBookmarks()
    }

    fun removeBookmark(bookmarkId: String) {
        _bookmarks.value = _bookmarks.value.filter { it.id != bookmarkId }
        saveBookmarks()
    }

    fun clearHistory() {
        _history.value = emptyList()
        saveHistory()
    }

    fun removeHistoryItem(mangaId: String) {
        _history.value = _history.value.filter { it.mangaId != mangaId }
        saveHistory()
    }

    fun loadExplore(source: String = "MangaDex") {
        scope.launch {
            _isLoadingExplore.value = true
            try {
                val remoteList = mangaDexApi.fetchPopularManga(30)
                if (remoteList.isNotEmpty()) {
                    _exploreManga.value = remoteList
                } else {
                    _exploreManga.value = getCuratedExploreManga()
                }
            } catch (e: Exception) {
                _exploreManga.value = getCuratedExploreManga()
            } finally {
                _isLoadingExplore.value = false
            }
        }
    }

    suspend fun searchManga(query: String): List<Manga> {
        return if (query.isBlank()) {
            _exploreManga.value
        } else {
            try {
                val remote = mangaDexApi.searchManga(query, 20)
                if (remote.isNotEmpty()) remote else getCuratedExploreManga().filter {
                    it.title.contains(query, ignoreCase = true) ||
                            it.genres.any { g -> g.contains(query, ignoreCase = true) }
                }
            } catch (e: Exception) {
                getCuratedExploreManga().filter {
                    it.title.contains(query, ignoreCase = true) ||
                            it.genres.any { g -> g.contains(query, ignoreCase = true) }
                }
            }
        }
    }

    suspend fun getChapters(mangaId: String): List<Chapter> {
        // Try MangaDex if it's a UUID (MangaDex style ID)
        if (mangaId.contains("-") && mangaId.length > 20) {
            val remote = mangaDexApi.fetchChapters(mangaId)
            if (remote.isNotEmpty()) return remote
        }
        // Fallback to rich simulated chapters for curated manga
        return generateChaptersForManga(mangaId)
    }

    suspend fun getPages(chapterId: String, pageCount: Int = 12): List<MangaPage> {
        if (chapterId.contains("-") && chapterId.length > 20) {
            val remote = mangaDexApi.fetchChapterPages(chapterId)
            if (remote.isNotEmpty()) return remote
        }
        // Return rich illustrated manga comic pages
        return generatePagesForChapter(chapterId, pageCount)
    }

    private fun generateChaptersForManga(mangaId: String): List<Chapter> {
        val total = 25
        return (1..total).map { num ->
            val id = "${mangaId}_ch_$num"
            Chapter(
                id = id,
                mangaId = mangaId,
                number = num.toFloat(),
                title = "Chapter $num: ${getChapterTitleSubtitle(num)}",
                uploadDate = "${num * 3}d ago",
                pageCount = 12 + (num % 5),
                isRead = _readChapters.value.contains(id),
                isDownloaded = _downloadedChapters.value.contains(id)
            )
        }
    }

    private fun getChapterTitleSubtitle(num: Int): String {
        return when (num % 6) {
            1 -> "The Awakening"
            2 -> "Dark Horizons"
            3 -> "Trial by Fire"
            4 -> "Unleashed Power"
            5 -> "The Turning Point"
            else -> "Resonance of Fate"
        }
    }

    private fun generatePagesForChapter(chapterId: String, count: Int): List<MangaPage> {
        val hash = chapterId.hashCode()
        // Provide varied high quality comic art pages with seeds
        return (1..count).map { pageNum ->
            val seed = kotlin.math.abs(hash + pageNum * 7919) % 1000
            MangaPage(
                pageNumber = pageNum,
                imageUrl = "https://picsum.photos/seed/manga_$seed/800/1200"
            )
        }
    }

    private fun getCuratedSampleLibrary(): List<Manga> {
        return listOf(
            Manga(
                id = "curated_solo_leveling",
                title = "Solo Leveling",
                author = "Chugong, DUBU (REDICE)",
                description = "In a world where hunters, humans with supernatural abilities, must battle deadly monsters to protect the human race from certain annihilation, a notoriously weak hunter named Sung Jinwoo finds himself in a seemingly endless struggle for survival.",
                coverUrl = "https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?w=600&auto=format&fit=crop&q=80",
                status = MangaStatus.COMPLETED,
                genres = listOf("Action", "Fantasy", "Supernatural"),
                rating = 4.9f,
                categoryId = "reading",
                isFavorite = true
            ),
            Manga(
                id = "curated_frieren",
                title = "Frieren: Beyond Journey's End",
                author = "Kanehito Yamada, Tsukasa Abe",
                description = "The adventure is over but life goes on for an elf mage just beginning to learn what living is all about. Elf mage Frieren and her courageous fellow adventurers have defeated the Demon King.",
                coverUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=600&auto=format&fit=crop&q=80",
                status = MangaStatus.ONGOING,
                genres = listOf("Adventure", "Drama", "Fantasy"),
                rating = 4.95f,
                categoryId = "reading",
                isFavorite = true
            ),
            Manga(
                id = "curated_chainsaw_man",
                title = "Chainsaw Man",
                author = "Tatsuki Fujimoto",
                description = "Denji was a small-time devil hunter just trying to survive in a harsh world. After being killed on a job, he is revived by his pet devil Pochita and becomes Chainsaw Man.",
                coverUrl = "https://images.unsplash.com/photo-1563089145-599997674d42?w=600&auto=format&fit=crop&q=80",
                status = MangaStatus.ONGOING,
                genres = listOf("Action", "Horror", "Comedy"),
                rating = 4.85f,
                categoryId = "plan",
                isFavorite = true
            ),
            Manga(
                id = "curated_spy_family",
                title = "SPY x FAMILY",
                author = "Tatsuya Endo",
                description = "Master spy Twilight is unparalleled at what he does when it comes to going undercover on dangerous missions for the betterment of the world. But when he receives the ultimate assignment—to get married and have a kid!",
                coverUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=600&auto=format&fit=crop&q=80",
                status = MangaStatus.ONGOING,
                genres = listOf("Comedy", "Slice of Life", "Action"),
                rating = 4.88f,
                categoryId = "completed",
                isFavorite = true
            )
        )
    }

    private fun getCuratedSampleHistory(): List<HistoryItem> {
        return listOf(
            HistoryItem(
                mangaId = "curated_solo_leveling",
                mangaTitle = "Solo Leveling",
                coverUrl = "https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?w=600&auto=format&fit=crop&q=80",
                chapterId = "curated_solo_leveling_ch_14",
                chapterNumber = 14f,
                chapterTitle = "Chapter 14: Trial by Fire",
                currentPage = 8,
                totalPages = 16,
                timestamp = System.currentTimeMillis() - 3600000
            ),
            HistoryItem(
                mangaId = "curated_frieren",
                mangaTitle = "Frieren: Beyond Journey's End",
                coverUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=600&auto=format&fit=crop&q=80",
                chapterId = "curated_frieren_ch_22",
                chapterNumber = 22f,
                chapterTitle = "Chapter 22: The Turning Point",
                currentPage = 14,
                totalPages = 14,
                timestamp = System.currentTimeMillis() - 86400000
            )
        )
    }

    private fun getCuratedExploreManga(): List<Manga> {
        return listOf(
            Manga(
                id = "curated_one_piece",
                title = "One Piece",
                author = "Eiichiro Oda",
                description = "Gol D. Roger, a man referred to as the 'King of the Pirates,' is poised to be executed by the World Government. Before his death, he confirms the existence of a great treasure called One Piece.",
                coverUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=600&auto=format&fit=crop&q=80",
                status = MangaStatus.ONGOING,
                genres = listOf("Action", "Adventure", "Comedy"),
                rating = 4.96f
            ),
            Manga(
                id = "curated_jujutsu_kaisen",
                title = "Jujutsu Kaisen",
                author = "Gege Akutami",
                description = "Idly indulging in paranormal activities with the Occult Club, high schooler Yuuji Itadori spends his days at either the clubroom or the hospital visiting his bedridden grandfather.",
                coverUrl = "https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?w=600&auto=format&fit=crop&q=80",
                status = MangaStatus.COMPLETED,
                genres = listOf("Action", "Supernatural", "Dark Fantasy"),
                rating = 4.87f
            ),
            Manga(
                id = "curated_tower_of_god",
                title = "Tower of God",
                author = "SIU",
                description = "What do you desire? Money and wealth? Honor and pride? Authority and power? Revenge? Or something that transcends them all? Whatever you desire—it is here.",
                coverUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600&auto=format&fit=crop&q=80",
                status = MangaStatus.ONGOING,
                genres = listOf("Fantasy", "Mystery", "Action"),
                rating = 4.82f
            ),
            Manga(
                id = "curated_berserk",
                title = "Berserk",
                author = "Kentaro Miura",
                description = "Guts, a former mercenary now known as the 'Black Swordsman,' is out for revenge against his former comrade Griffith, who sacrificed his band to become a demonic god.",
                coverUrl = "https://images.unsplash.com/photo-1541701494587-cb58502866ab?w=600&auto=format&fit=crop&q=80",
                status = MangaStatus.ONGOING,
                genres = listOf("Dark Fantasy", "Horror", "Action"),
                rating = 4.97f
            ),
            Manga(
                id = "curated_demon_slayer",
                title = "Demon Slayer: Kimetsu no Yaiba",
                author = "Koyoharu Gotouge",
                description = "Tanjiro Kamado's peaceful life is shattered when his family is slaughtered by demons and his sister Nezuko is transformed into one.",
                coverUrl = "https://images.unsplash.com/photo-1563089145-599997674d42?w=600&auto=format&fit=crop&q=80",
                status = MangaStatus.COMPLETED,
                genres = listOf("Action", "Historical", "Supernatural"),
                rating = 4.84f
            ),
            Manga(
                id = "curated_vinland_saga",
                title = "Vinland Saga",
                author = "Makoto Yukimura",
                description = "Young Thorfinn grew up listening to the stories of old sailors that had traveled the ocean and reached the place of legend, Vinland.",
                coverUrl = "https://images.unsplash.com/photo-1514539079130-25950c84af65?w=600&auto=format&fit=crop&q=80",
                status = MangaStatus.ONGOING,
                genres = listOf("Action", "Adventure", "Historical"),
                rating = 4.91f
            )
        )
    }
}
