package org.koitharu.kotatsu.data.repository

import android.content.Context
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koitharu.kotatsu.data.translation.MangaTranslator
import org.koitharu.kotatsu.data.model.Manga
import org.koitharu.kotatsu.data.model.Chapter
import org.koitharu.kotatsu.data.model.Category
import org.koitharu.kotatsu.data.model.MangaSource
import org.koitharu.kotatsu.data.model.HistoryItem
import org.koitharu.kotatsu.data.model.Bookmark
import org.koitharu.kotatsu.data.model.MangaPage
import org.koitharu.kotatsu.data.model.MangaStatus
import org.koitharu.kotatsu.data.model.ReaderSettings
import org.koitharu.kotatsu.data.model.NeonThemeId
import org.koitharu.kotatsu.data.model.NeonColorPalette
import org.koitharu.kotatsu.data.model.NeonThemes
import org.koitharu.kotatsu.data.model.*
import org.koitharu.kotatsu.data.translation.TranslatedBubble
import java.io.File

class MangaRepository(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }
    private val mangaDexApi = MangaDexApi()
    val translator = MangaTranslator()

    private val categoriesFile = File(context.filesDir, "categories.json")
    private val libraryFile = File(context.filesDir, "library.json")
    private val historyFile = File(context.filesDir, "history.json")
    private val bookmarksFile = File(context.filesDir, "bookmarks.json")
    private val settingsFile = File(context.filesDir, "sss_x_settings.json")

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

    private val _activeNeonTheme = MutableStateFlow(NeonThemes.getTheme(NeonThemeId.ELECTRIC_BLUE_PURPLE))
    val activeNeonTheme: StateFlow<NeonColorPalette> = _activeNeonTheme.asStateFlow()

    private val _sources = MutableStateFlow<List<MangaSource>>(emptyList())
    val sources: StateFlow<List<MangaSource>> = _sources.asStateFlow()

    private val _exploreManga = MutableStateFlow<List<Manga>>(emptyList())
    val exploreManga: StateFlow<List<Manga>> = _exploreManga.asStateFlow()

    private val _isLoadingExplore = MutableStateFlow(false)
    val isLoadingExplore: StateFlow<Boolean> = _isLoadingExplore.asStateFlow()

    init {
        loadData()
        loadSources()
        loadExplore()
    }

    private fun loadData() {
        val defaultCategories = listOf(
            Category("all", "الكل", 0),
            Category("reading", "قيد القراءة", 1),
            Category("plan", "اقرأ لاحقاً", 2),
            Category("completed", "المكتملة", 3),
            Category("favorites", "المفضلة", 4)
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

        if (libraryFile.exists()) {
            try {
                val content = libraryFile.readText()
                _library.value = json.decodeFromString(content)
            } catch (e: Exception) {
                _library.value = emptyList()
            }
        } else {
            _library.value = emptyList()
        }

        if (historyFile.exists()) {
            try {
                val content = historyFile.readText()
                _history.value = json.decodeFromString(content)
            } catch (e: Exception) {
                _history.value = emptyList()
            }
        } else {
            _history.value = emptyList()
        }

        if (bookmarksFile.exists()) {
            try {
                val content = bookmarksFile.readText()
                _bookmarks.value = json.decodeFromString(content)
            } catch (e: Exception) {
                _bookmarks.value = emptyList()
            }
        }

        if (settingsFile.exists()) {
            try {
                val content = settingsFile.readText()
                val loadedSettings: ReaderSettings = json.decodeFromString(content)
                _readerSettings.value = loadedSettings
                updateActiveNeonPalette(loadedSettings)
            } catch (e: Exception) {
                _readerSettings.value = ReaderSettings()
            }
        }
    }

    private fun updateActiveNeonPalette(settings: ReaderSettings) {
        if (settings.neonThemeId == NeonThemeId.CUSTOM) {
            val p = try { Color(android.graphics.Color.parseColor(settings.customPrimaryColorHex)) } catch (e: Exception) { Color(0xFF1B6BFF) }
            val s = try { Color(android.graphics.Color.parseColor(settings.customSecondaryColorHex)) } catch (e: Exception) { Color(0xFF8A2BE2) }
            _activeNeonTheme.value = NeonColorPalette(
                id = NeonThemeId.CUSTOM,
                nameAr = "مخصص",
                nameEn = "Custom",
                primary = p,
                secondary = s,
                tertiary = Color(0xFF00E5FF),
                glow = p,
                border = p
            )
        } else {
            _activeNeonTheme.value = NeonThemes.getTheme(settings.neonThemeId)
        }
    }

    private fun loadSources() {
        _sources.value = listOf(
            MangaSource("mangapro", "MangaPro", "https://mangapro.org", "AR"),
            MangaSource("manjanoon", "Manjanoon", "https://manjanoon.com", "AR"),
            MangaSource("areascans", "AreaScans", "https://areascans.net", "AR"),
            MangaSource("arareascans", "ArAreaScans", "https://ar.areascans.net", "AR"),
            MangaSource("thunderscans", "ThunderScans", "https://thunderscans.com", "AR"),
            MangaSource("hijalacom", "Hijalacom", "https://hijalacom.com", "AR"),
            MangaSource("mangaatrium", "MangaAtrium", "https://mangaatrium.com", "AR"),
            MangaSource("flares", "Fl-Ares", "https://fl-ares.com", "AR"),
            MangaSource("mangaswat", "MangaSwat", "https://swatmanga.com", "AR"),
            MangaSource("umimanga", "UmiManga", "https://umimanga.com", "AR"),
            MangaSource("mangaflame", "MangaFlame", "https://mangaflame.org", "AR"),
            MangaSource("stellarsaber", "StellarSaber", "https://stellarsaber.com", "AR"),
            MangaSource("3asq", "3Asq", "https://3asq.org", "AR"),
            MangaSource("lekmanga", "LekManga", "https://lekmanga.net", "AR"),
            MangaSource("linkmanga", "Link-Manga", "https://link-manga.com", "AR"),
            MangaSource("likemanga", "Like-Manga", "https://likemanga.io", "AR"),
            MangaSource("mangadex", "MangaDex", "https://mangadex.org", "EN/Global")
        )
    }

    private fun saveCategories() {
        scope.launch {
            try { categoriesFile.writeText(json.encodeToString(_categories.value)) } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun saveLibrary() {
        scope.launch {
            try { libraryFile.writeText(json.encodeToString(_library.value)) } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun saveHistory() {
        scope.launch {
            try { historyFile.writeText(json.encodeToString(_history.value)) } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun saveBookmarks() {
        scope.launch {
            try { bookmarksFile.writeText(json.encodeToString(_bookmarks.value)) } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun saveReaderSettings(settings: ReaderSettings) {
        _readerSettings.value = settings
        updateActiveNeonPalette(settings)
        scope.launch {
            try { settingsFile.writeText(json.encodeToString(settings)) } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun setNeonTheme(themeId: NeonThemeId) {
        val updated = _readerSettings.value.copy(neonThemeId = themeId)
        saveReaderSettings(updated)
    }

    fun setCustomColors(primaryHex: String, secondaryHex: String) {
        val updated = _readerSettings.value.copy(
            neonThemeId = NeonThemeId.CUSTOM,
            customPrimaryColorHex = primaryHex,
            customSecondaryColorHex = secondaryHex
        )
        saveReaderSettings(updated)
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
        if (_readerSettings.value.isIncognitoMode) return // Don't save history in incognito mode

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

        _library.value = _library.value.map {
            if (it.id == manga.id) it.copy(lastReadChapterId = chapter.id, lastReadPage = page) else it
        }
        saveLibrary()

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
        if (mangaId.contains("-") && mangaId.length > 20) {
            val remote = mangaDexApi.fetchChapters(mangaId)
            if (remote.isNotEmpty()) return remote
        }
        return generateChaptersForManga(mangaId)
    }

    suspend fun getPages(chapterId: String, pageCount: Int = 12): List<MangaPage> {
        if (chapterId.contains("-") && chapterId.length > 20) {
            val remote = mangaDexApi.fetchChapterPages(chapterId)
            if (remote.isNotEmpty()) return remote
        }
        return generatePagesForChapter(chapterId, pageCount)
    }

    suspend fun getPageTranslations(
        mangaId: String,
        chapterId: String,
        pageNumber: Int,
        sourceLang: String = "ja"
    ): List<TranslatedBubble> {
        return translator.detectAndTranslatePageBubbles(mangaId, chapterId, pageNumber, sourceLang)
    }

    private fun generateChaptersForManga(mangaId: String): List<Chapter> {
        val total = 30
        return (1..total).map { num ->
            val id = "${mangaId}_ch_$num"
            Chapter(
                id = id,
                mangaId = mangaId,
                number = num.toFloat(),
                title = "الفصل $num: ${getChapterTitleSubtitle(num)}",
                uploadDate = "منذ ${num * 2} أيام",
                pageCount = 14 + (num % 6),
                isRead = _readChapters.value.contains(id),
                isDownloaded = _downloadedChapters.value.contains(id)
            )
        }
    }

    private fun getChapterTitleSubtitle(num: Int): String {
        return when (num % 6) {
            1 -> "الصحوة الكبرى"
            2 -> "أفق الظلام"
            3 -> "اختبار القوة الأسطوري"
            4 -> "إطلاق العنان للقدرات"
            5 -> "نقطة التحول المصيرية"
            else -> "صراع الأرواح"
        }
    }

    private fun generatePagesForChapter(chapterId: String, count: Int): List<MangaPage> {
        val hash = chapterId.hashCode()
        return (1..count).map { pageNum ->
            val seed = kotlin.math.abs(hash + pageNum * 7919) % 1000
            MangaPage(
                pageNumber = pageNum,
                imageUrl = "https://picsum.photos/seed/sss_x_$seed/800/1200"
            )
        }
    }

    private fun getCuratedExploreManga(): List<Manga> {
        return listOf(
            Manga(
                id = "curated_solo_leveling",
                title = "Solo Leveling",
                author = "Chugong, DUBU",
                description = "في عالم تظهر فيه البوابات المليئة بالوحوش، يتحول الصياد الأضعف سونغ جين وو إلى ملك الظلال الأعظم.",
                coverUrl = "https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?w=600&auto=format&fit=crop&q=80",
                status = MangaStatus.COMPLETED,
                genres = listOf("أكشن", "فانتازيا", "خارق للطبيعة"),
                rating = 4.95f,
                source = "MangaPro"
            ),
            Manga(
                id = "curated_one_piece",
                title = "One Piece",
                author = "Eiichiro Oda",
                description = "رحلة مونكي دي لوفي وطاقمه بحثاً عن الكنز الأعظم ون بيس ليكون ملك القراصنة القادم.",
                coverUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=600&auto=format&fit=crop&q=80",
                status = MangaStatus.ONGOING,
                genres = listOf("مغامرات", "كوميديا", "أكشن"),
                rating = 4.98f,
                source = "3Asq"
            ),
            Manga(
                id = "curated_jujutsu_kaisen",
                title = "Jujutsu Kaisen",
                author = "Gege Akutami",
                description = "يوجي إيتادوري يبتلع إصبع لعنة أسطورية ويخوض معارك خارقة في مدرسة السحر والشعوذة.",
                coverUrl = "https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?w=600&auto=format&fit=crop&q=80",
                status = MangaStatus.COMPLETED,
                genres = listOf("خارق للطبيعة", "رعب", "أكشن"),
                rating = 4.88f,
                source = "MangaSwat"
            ),
            Manga(
                id = "curated_frieren",
                title = "Frieren: Beyond Journey's End",
                author = "Kanehito Yamada",
                description = "الساحرة الخالدة فريرين تبدأ رحلة جديدة لفهم مشاعر البشر بعد نهاية رحلة الأبطال.",
                coverUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=600&auto=format&fit=crop&q=80",
                status = MangaStatus.ONGOING,
                genres = listOf("فانتازيا", "دراما", "مغامرات"),
                rating = 4.97f,
                source = "AreaScans"
            ),
            Manga(
                id = "curated_tower_of_god",
                title = "Tower of God",
                author = "SIU",
                description = "اصعد البرج ليتحقق كل ما تتمناه... رحلة بام المحفوفة بالمخاطر داخل أروقة البرج.",
                coverUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600&auto=format&fit=crop&q=80",
                status = MangaStatus.ONGOING,
                genres = listOf("فانتازيا", "غموض", "أكشن"),
                rating = 4.85f,
                source = "Manjanoon"
            ),
            Manga(
                id = "curated_chainsaw_man",
                title = "Chainsaw Man",
                author = "Tatsuki Fujimoto",
                description = "دينجي الفتى المعدم يتحول إلى رجل المنشار في عالم مليء بالشياطين المتعطشة للدماء.",
                coverUrl = "https://images.unsplash.com/photo-1563089145-599997674d42?w=600&auto=format&fit=crop&q=80",
                status = MangaStatus.ONGOING,
                genres = listOf("أكشن", "رعب", "كوميديا"),
                rating = 4.90f,
                source = "ThunderScans"
            )
        )
    }
}
