package org.koitharu.kotatsu.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import org.koitharu.kotatsu.data.model.*
import org.koitharu.kotatsu.data.repository.MangaRepository
import org.koitharu.kotatsu.data.translation.TranslatedBubble
import org.koitharu.kotatsu.ui.theme.LocalNeonPalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    mangaId: String,
    chapterId: String,
    initialPage: Int,
    repository: MangaRepository,
    onBackClick: () -> Unit,
    onOpenReaderSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val palette = LocalNeonPalette.current
    val library by repository.library.collectAsState()
    val exploreManga by repository.exploreManga.collectAsState()
    val readerSettings by repository.readerSettings.collectAsState()

    val manga = remember(library, exploreManga, mangaId) {
        library.find { it.id == mangaId }
            ?: exploreManga.find { it.id == mangaId }
            ?: Manga(id = mangaId, title = "SSS X Manga", coverUrl = "")
    }

    var chapters by remember { mutableStateOf<List<Chapter>>(emptyList()) }
    var currentChapterId by remember { mutableStateOf(chapterId) }
    var pages by remember { mutableStateOf<List<MangaPage>>(emptyList()) }
    var currentPage by remember { mutableStateOf(initialPage.coerceAtLeast(1)) }
    var isLoadingPages by remember { mutableStateOf(true) }
    var showControls by remember { mutableStateOf(true) }
    var showTranslationDialog by remember { mutableStateOf(false) }

    // Translations for current chapter page
    var pageBubbles by remember { mutableStateOf<List<TranslatedBubble>>(emptyList()) }
    var isTranslating by remember { mutableStateOf(false) }

    val currentChapter = remember(chapters, currentChapterId) {
        chapters.find { it.id == currentChapterId } ?: Chapter(
            id = currentChapterId,
            mangaId = mangaId,
            number = 1f,
            title = "الفصل 1",
            pageCount = 12
        )
    }

    val currentChapterIndex = remember(chapters, currentChapterId) {
        chapters.indexOfFirst { it.id == currentChapterId }
    }
    val hasPrevChapter = currentChapterIndex > 0
    val hasNextChapter = currentChapterIndex >= 0 && currentChapterIndex < chapters.size - 1

    // Load chapters list
    LaunchedEffect(mangaId) {
        val loaded = repository.getChapters(mangaId).sortedBy { it.number }
        chapters = loaded
    }

    // Load pages when chapter changes
    LaunchedEffect(currentChapterId) {
        isLoadingPages = true
        val loadedPages = repository.getPages(currentChapterId, currentChapter.pageCount)
        pages = loadedPages
        isLoadingPages = false
        currentPage = 1
        repository.updateReadingProgress(manga, currentChapter, 1, loadedPages.size)
    }

    // Trigger real translation when translation is enabled or page changes
    LaunchedEffect(readerSettings.isTranslationEnabled, currentPage, currentChapterId) {
        if (readerSettings.isTranslationEnabled && pages.isNotEmpty()) {
            isTranslating = true
            try {
                val bubbles = repository.getPageTranslations(
                    mangaId = manga.id,
                    chapterId = currentChapterId,
                    pageNumber = currentPage,
                    sourceLang = readerSettings.translationSourceLang
                )
                pageBubbles = bubbles
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isTranslating = false
            }
        } else {
            pageBubbles = emptyList()
        }
    }

    val backgroundColor = when (readerSettings.background) {
        ReaderBackground.BLACK -> Color(0xFF000000)
        ReaderBackground.DARK_GRAY -> Color(0xFF090D1A)
        ReaderBackground.WHITE -> Color(0xFFFFFFFF)
    }

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = backgroundColor,
        modifier = modifier
            .fillMaxSize()
            .testTag("reader_screen")
    ) { _ ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
        ) {
            if (isLoadingPages) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            color = palette.primary,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("reader_loading")
                        )
                        Text(
                            text = "جاري تحميل صفحات الفصل...",
                            color = palette.textSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else if (pages.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "تعذر تحميل فصول المانجا",
                        color = palette.textPrimary
                    )
                }
            } else {
                when (readerSettings.readingMode) {
                    ReadingMode.WEBTOON, ReadingMode.CONTINUOUS -> {
                        val listState = rememberLazyListState()

                        LaunchedEffect(listState.firstVisibleItemIndex) {
                            val newPage = (listState.firstVisibleItemIndex + 1).coerceIn(1, pages.size)
                            if (newPage != currentPage) {
                                currentPage = newPage
                                repository.updateReadingProgress(manga, currentChapter, newPage, pages.size)
                            }
                        }

                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onTap = { showControls = !showControls }
                                    )
                                }
                                .testTag("reader_webtoon_list")
                        ) {
                            itemsIndexed(pages, key = { index, page -> "${page.pageNumber}_$index" }) { index, page ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight()
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(page.imageUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "الصفحة ${page.pageNumber}",
                                        contentScale = ContentScale.FillWidth,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("page_image_${page.pageNumber}")
                                    )

                                    // Render speech bubble overlays on current active page
                                    if (readerSettings.isTranslationEnabled && page.pageNumber == currentPage) {
                                        SpeechBubblesOverlay(
                                            bubbles = pageBubbles,
                                            palette = palette
                                        )
                                    }
                                }
                            }

                            // End of chapter footer
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text(
                                        text = "نهاية ${currentChapter.title}",
                                        color = palette.textPrimary,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )

                                    if (hasNextChapter) {
                                        Button(
                                            onClick = {
                                                val next = chapters[currentChapterIndex + 1]
                                                currentChapterId = next.id
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = palette.primary),
                                            shape = RoundedCornerShape(16.dp),
                                            modifier = Modifier.testTag("webtoon_next_chapter_btn")
                                        ) {
                                            Text(
                                                "الفصل التالي: ${chapters[currentChapterIndex + 1].title}",
                                                color = Color.White
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Icon(
                                                Icons.AutoMirrored.Filled.ArrowForward,
                                                contentDescription = null,
                                                tint = Color.White
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = "لقد وصلت إلى أحدث الفصول المتاحة!",
                                            color = palette.textSecondary,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    ReadingMode.PAGED_RTL, ReadingMode.PAGED_LTR -> {
                        val isRtl = readerSettings.readingMode == ReadingMode.PAGED_RTL
                        val pagerState = rememberPagerState(initialPage = 0, pageCount = { pages.size })

                        LaunchedEffect(pagerState.currentPage) {
                            val activeIndex = if (isRtl) pages.size - 1 - pagerState.currentPage else pagerState.currentPage
                            currentPage = (activeIndex + 1).coerceIn(1, pages.size)
                            repository.updateReadingProgress(manga, currentChapter, currentPage, pages.size)
                        }

                        Box(modifier = Modifier.fillMaxSize()) {
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("reader_horizontal_pager")
                            ) { pageIdx ->
                                val realIdx = if (isRtl) pages.size - 1 - pageIdx else pageIdx
                                val page = pages.getOrNull(realIdx)
                                if (page != null) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data(page.imageUrl)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = "الصفحة ${page.pageNumber}",
                                            contentScale = ContentScale.Fit,
                                            modifier = Modifier.fillMaxSize()
                                        )

                                        if (readerSettings.isTranslationEnabled && page.pageNumber == currentPage) {
                                            SpeechBubblesOverlay(
                                                bubbles = pageBubbles,
                                                palette = palette
                                            )
                                        }
                                    }
                                }
                            }

                            // Tap zones for page turning
                            Row(modifier = Modifier.fillMaxSize()) {
                                Box(
                                    modifier = Modifier
                                        .weight(0.3f)
                                        .fillMaxHeight()
                                        .clickable {
                                            if (isRtl) {
                                                if (pagerState.currentPage < pages.size - 1) {
                                                    coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                                                }
                                            } else {
                                                if (pagerState.currentPage > 0) {
                                                    coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                                                }
                                            }
                                        }
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(0.4f)
                                        .fillMaxHeight()
                                        .clickable { showControls = !showControls }
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(0.3f)
                                        .fillMaxHeight()
                                        .clickable {
                                            if (isRtl) {
                                                if (pagerState.currentPage > 0) {
                                                    coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                                                }
                                            } else {
                                                if (pagerState.currentPage < pages.size - 1) {
                                                    coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                                                }
                                            }
                                        }
                                )
                            }
                        }
                    }
                }
            }

            // Top HUD Bar (Futuristic Neon Cyber Bar)
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                Surface(
                    color = Color(0xEB040818),
                    border = BorderStroke(1.dp, palette.border.copy(alpha = 0.35f)),
                    shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(16.dp, spotColor = palette.glow)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier.testTag("reader_back_btn")
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = palette.textPrimary
                            )
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = manga.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = palette.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${currentChapter.title} • ص $currentPage من ${pages.size}",
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.tertiary,
                                maxLines = 1
                            )
                        }

                        // Real-Time Translation Quick-Toggle Button
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (readerSettings.isTranslationEnabled) palette.primary.copy(alpha = 0.25f) else Color(0x22FFFFFF),
                            border = BorderStroke(
                                1.dp,
                                if (readerSettings.isTranslationEnabled) palette.primary else palette.border.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier
                                .clickable {
                                    val newState = !readerSettings.isTranslationEnabled
                                    repository.saveReaderSettings(readerSettings.copy(isTranslationEnabled = newState))
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(
                                            if (newState) "تم تفعيل الترجمة الفورية للعربية 🇸🇦" else "تم إيقاف الترجمة الفورية"
                                        )
                                    }
                                }
                                .padding(horizontal = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.Translate,
                                    contentDescription = "ترجمة",
                                    tint = if (readerSettings.isTranslationEnabled) palette.tertiary else palette.textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (readerSettings.isTranslationEnabled) "عربي (نشط)" else "ترجمة",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (readerSettings.isTranslationEnabled) palette.tertiary else palette.textSecondary
                                )
                            }
                        }

                        // Bookmark current page
                        IconButton(
                            onClick = {
                                val curPageObj = pages.getOrNull(currentPage - 1)
                                if (curPageObj != null) {
                                    repository.addBookmark(
                                        manga = manga,
                                        chapter = currentChapter,
                                        pageNumber = currentPage,
                                        imageUrl = curPageObj.imageUrl
                                    )
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("تم حفظ الصفحة $currentPage في الإشارات المرجعية")
                                    }
                                }
                            },
                            modifier = Modifier.testTag("reader_bookmark_btn")
                        ) {
                            Icon(
                                Icons.Outlined.BookmarkBorder,
                                contentDescription = "إشارة مرجعية",
                                tint = palette.textPrimary
                            )
                        }

                        // Reader Full Settings (Opens ReaderSettingsScreen)
                        IconButton(
                            onClick = onOpenReaderSettings,
                            modifier = Modifier.testTag("reader_settings_btn")
                        ) {
                            Icon(
                                Icons.Default.Tune,
                                contentDescription = "إعدادات القارئ",
                                tint = palette.primary
                            )
                        }
                    }
                }
            }

            // Bottom HUD Control Bar
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Surface(
                    color = Color(0xEB040818),
                    border = BorderStroke(1.dp, palette.border.copy(alpha = 0.35f)),
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(16.dp, spotColor = palette.glow)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(16.dp)
                    ) {
                        // Translation language indicator pill
                        if (readerSettings.isTranslationEnabled) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = palette.surfaceCard,
                                    border = BorderStroke(1.dp, palette.tertiary.copy(alpha = 0.6f)),
                                    modifier = Modifier.clickable { showTranslationDialog = true }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(palette.tertiary, CircleShape)
                                        )
                                        val langLabel = when (readerSettings.translationSourceLang) {
                                            "ja" -> "اليابانية 🇯🇵"
                                            "ko" -> "الكورية 🇰🇷"
                                            "zh" -> "الصينية 🇨🇳"
                                            "en" -> "الإنجليزية 🇺🇸"
                                            else -> "تلقائي"
                                        }
                                        Text(
                                            text = "مترجم من $langLabel إلى العربية",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = palette.textPrimary
                                        )
                                        if (isTranslating) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(10.dp),
                                                strokeWidth = 1.5.dp,
                                                color = palette.tertiary
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Page slider scrubber
                        if (pages.isNotEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "$currentPage",
                                    color = palette.tertiary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(36.dp),
                                    textAlign = TextAlign.Center
                                )

                                Slider(
                                    value = currentPage.toFloat(),
                                    onValueChange = { currentPage = it.toInt() },
                                    valueRange = 1f..pages.size.toFloat(),
                                    steps = (pages.size - 2).coerceAtLeast(0),
                                    colors = SliderDefaults.colors(
                                        thumbColor = palette.primary,
                                        activeTrackColor = palette.primary,
                                        inactiveTrackColor = palette.border.copy(alpha = 0.3f)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("reader_page_slider")
                                )

                                Text(
                                    text = "${pages.size}",
                                    color = palette.textSecondary,
                                    modifier = Modifier.width(36.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Chapter Controls Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    if (hasPrevChapter) {
                                        currentChapterId = chapters[currentChapterIndex - 1].id
                                    }
                                },
                                enabled = hasPrevChapter,
                                modifier = Modifier.testTag("prev_chapter_btn")
                            ) {
                                Icon(
                                    Icons.Default.SkipPrevious,
                                    contentDescription = null,
                                    tint = if (hasPrevChapter) palette.textPrimary else palette.textSecondary.copy(alpha = 0.3f)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "الفصل السابق",
                                    color = if (hasPrevChapter) palette.textPrimary else palette.textSecondary.copy(alpha = 0.3f)
                                )
                            }

                            // Quick reading mode badge
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = palette.primary.copy(alpha = 0.25f),
                                border = BorderStroke(1.dp, palette.primary.copy(alpha = 0.5f)),
                                modifier = Modifier.clickable { onOpenReaderSettings() }
                            ) {
                                Text(
                                    text = when (readerSettings.readingMode) {
                                        ReadingMode.WEBTOON -> "ويبتون"
                                        ReadingMode.CONTINUOUS -> "مستمر"
                                        ReadingMode.PAGED_RTL -> "مانجا (RTL)"
                                        ReadingMode.PAGED_LTR -> "كوميك (LTR)"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            TextButton(
                                onClick = {
                                    if (hasNextChapter) {
                                        currentChapterId = chapters[currentChapterIndex + 1].id
                                    }
                                },
                                enabled = hasNextChapter,
                                modifier = Modifier.testTag("next_chapter_btn")
                            ) {
                                Text(
                                    "الفصل التالي",
                                    color = if (hasNextChapter) palette.textPrimary else palette.textSecondary.copy(alpha = 0.3f)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.SkipNext,
                                    contentDescription = null,
                                    tint = if (hasNextChapter) palette.textPrimary else palette.textSecondary.copy(alpha = 0.3f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Language selector dialog
    if (showTranslationDialog) {
        AlertDialog(
            onDismissRequest = { showTranslationDialog = false },
            containerColor = palette.surfaceCard,
            title = {
                Text(
                    text = "لغة المانجا الأصلية",
                    color = palette.textPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "ja" to "اليابانية (Manga) 🇯🇵",
                        "ko" to "الكورية (Manhwa) 🇰🇷",
                        "zh" to "الصينية (Manhua) 🇨🇳",
                        "en" to "الإنجليزية 🇺🇸"
                    ).forEach { (code, name) ->
                        val isSelected = readerSettings.translationSourceLang == code
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) palette.primary.copy(alpha = 0.3f) else Color.Transparent,
                            border = BorderStroke(1.dp, if (isSelected) palette.primary else palette.border.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    repository.saveReaderSettings(readerSettings.copy(translationSourceLang = code))
                                    showTranslationDialog = false
                                }
                        ) {
                            Text(
                                text = name,
                                color = if (isSelected) palette.tertiary else palette.textPrimary,
                                modifier = Modifier.padding(12.dp),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTranslationDialog = false }) {
                    Text("إغلاق", color = palette.primary)
                }
            }
        )
    }
}

@Composable
fun BoxScope.SpeechBubblesOverlay(
    bubbles: List<TranslatedBubble>,
    palette: org.koitharu.kotatsu.data.model.NeonColorPalette
) {
    bubbles.forEach { bubble ->
        var showOriginal by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = (bubble.xRatio * 320).dp,
                    top = (bubble.yRatio * 520).dp
                )
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xF5050C24),
                border = BorderStroke(1.2.dp, palette.tertiary.copy(alpha = 0.85f)),
                modifier = Modifier
                    .widthIn(max = 240.dp)
                    .shadow(12.dp, spotColor = palette.tertiary)
                    .clickable { showOriginal = !showOriginal }
                    .testTag("translated_bubble_${bubble.id}")
            ) {
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                    Text(
                        text = if (showOriginal) bubble.originalText else bubble.translatedText,
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    if (showOriginal) {
                        Text(
                            text = "(الأصلية - انقر للترجمة)",
                            color = palette.tertiary,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }
    }
}
