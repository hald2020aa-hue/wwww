package org.koitharu.kotatsu.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import org.koitharu.kotatsu.data.model.*
import org.koitharu.kotatsu.data.repository.MangaRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    mangaId: String,
    chapterId: String,
    initialPage: Int,
    repository: MangaRepository,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val library by repository.library.collectAsState()
    val exploreManga by repository.exploreManga.collectAsState()
    val readerSettings by repository.readerSettings.collectAsState()

    val manga = remember(library, exploreManga, mangaId) {
        library.find { it.id == mangaId }
            ?: exploreManga.find { it.id == mangaId }
            ?: Manga(id = mangaId, title = "Manga", coverUrl = "")
    }

    var chapters by remember { mutableStateOf<List<Chapter>>(emptyList()) }
    var currentChapterId by remember { mutableStateOf(chapterId) }
    var pages by remember { mutableStateOf<List<MangaPage>>(emptyList()) }
    var currentPage by remember { mutableStateOf(initialPage.coerceAtLeast(1)) }
    var isLoadingPages by remember { mutableStateOf(true) }
    var showControls by remember { mutableStateOf(true) }
    var showSettingsSheet by remember { mutableStateOf(false) }

    val currentChapter = remember(chapters, currentChapterId) {
        chapters.find { it.id == currentChapterId } ?: Chapter(
            id = currentChapterId,
            mangaId = mangaId,
            number = 1f,
            title = "Chapter 1",
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

    val backgroundColor = when (readerSettings.background) {
        ReaderBackground.BLACK -> Color(0xFF000000)
        ReaderBackground.DARK_GRAY -> Color(0xFF14151C)
        ReaderBackground.WHITE -> Color(0xFFFFFFFF)
    }

    val isDarkBg = readerSettings.background != ReaderBackground.WHITE
    val hudColor = if (isDarkBg) Color.Black.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.95f)
    val textColor = if (isDarkBg) Color.White else Color.Black

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
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.testTag("reader_loading")
                    )
                }
            } else if (pages.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Unable to load chapter pages",
                        color = textColor
                    )
                }
            } else {
                when (readerSettings.readingMode) {
                    ReadingMode.WEBTOON -> {
                        val listState = rememberLazyListState()

                        // Sync current page with visible item
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
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(page.imageUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Page ${page.pageNumber}",
                                    contentScale = ContentScale.FillWidth,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("page_image_${page.pageNumber}")
                                )
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
                                        text = "End of ${currentChapter.title}",
                                        color = textColor,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )

                                    if (hasNextChapter) {
                                        Button(
                                            onClick = {
                                                val next = chapters[currentChapterIndex + 1]
                                                currentChapterId = next.id
                                            },
                                            modifier = Modifier.testTag("webtoon_next_chapter_btn")
                                        ) {
                                            Text("Next Chapter: ${chapters[currentChapterIndex + 1].title}")
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Icon(Icons.Default.ArrowForward, contentDescription = null)
                                        }
                                    } else {
                                        Text(
                                            text = "You're caught up with the latest chapter!",
                                            color = textColor.copy(alpha = 0.7f),
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
                                            contentDescription = "Page ${page.pageNumber}",
                                            contentScale = ContentScale.Fit,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            }

                            // Tap zones for page turning
                            Row(modifier = Modifier.fillMaxSize()) {
                                // Left tap zone
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
                                // Center tap zone (toggle controls)
                                Box(
                                    modifier = Modifier
                                        .weight(0.4f)
                                        .fillMaxHeight()
                                        .clickable { showControls = !showControls }
                                )
                                // Right tap zone
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

            // Top HUD Bar
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                Surface(
                    color = hudColor,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBackClick, modifier = Modifier.testTag("reader_back_btn")) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textColor)
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                        ) {
                            Text(
                                text = manga.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                                maxLines = 1
                            )
                            Text(
                                text = "${currentChapter.title} • $currentPage / ${pages.size}",
                                style = MaterialTheme.typography.bodySmall,
                                color = textColor.copy(alpha = 0.8f),
                                maxLines = 1
                            )
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
                                        snackbarHostState.showSnackbar("Bookmarked page $currentPage")
                                    }
                                }
                            },
                            modifier = Modifier.testTag("reader_bookmark_btn")
                        ) {
                            Icon(Icons.Outlined.BookmarkBorder, contentDescription = "Bookmark Page", tint = textColor)
                        }

                        // Reader Settings
                        IconButton(
                            onClick = { showSettingsSheet = true },
                            modifier = Modifier.testTag("reader_settings_btn")
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = "Reader Settings", tint = textColor)
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
                    color = hudColor,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(16.dp)
                    ) {
                        // Page slider scrubber
                        if (pages.isNotEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "$currentPage",
                                    color = textColor,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(36.dp)
                                )

                                Slider(
                                    value = currentPage.toFloat(),
                                    onValueChange = { currentPage = it.toInt() },
                                    valueRange = 1f..pages.size.toFloat(),
                                    steps = (pages.size - 2).coerceAtLeast(0),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("reader_page_slider")
                                )

                                Text(
                                    text = "${pages.size}",
                                    color = textColor.copy(alpha = 0.8f),
                                    modifier = Modifier.width(36.dp)
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
                                Icon(Icons.Default.SkipPrevious, contentDescription = null, tint = if (hasPrevChapter) textColor else textColor.copy(alpha = 0.3f))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Prev Ch.", color = if (hasPrevChapter) textColor else textColor.copy(alpha = 0.3f))
                            }

                            // Quick reading mode badge
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.clickable { showSettingsSheet = true }
                            ) {
                                Text(
                                    text = when (readerSettings.readingMode) {
                                        ReadingMode.WEBTOON -> "Webtoon"
                                        ReadingMode.PAGED_RTL -> "Manga (RTL)"
                                        ReadingMode.PAGED_LTR -> "Comic (LTR)"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
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
                                Text("Next Ch.", color = if (hasNextChapter) textColor else textColor.copy(alpha = 0.3f))
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.SkipNext, contentDescription = null, tint = if (hasNextChapter) textColor else textColor.copy(alpha = 0.3f))
                            }
                        }
                    }
                }
            }
        }
    }

    // Reader Settings Modal
    if (showSettingsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSettingsSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Reader Settings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                // Reading Mode Selector
                Text(text = "Reading Mode", fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = readerSettings.readingMode == ReadingMode.WEBTOON,
                        onClick = {
                            repository.saveReaderSettings(readerSettings.copy(readingMode = ReadingMode.WEBTOON))
                        },
                        label = { Text("Webtoon") },
                        modifier = Modifier.testTag("mode_webtoon")
                    )
                    FilterChip(
                        selected = readerSettings.readingMode == ReadingMode.PAGED_RTL,
                        onClick = {
                            repository.saveReaderSettings(readerSettings.copy(readingMode = ReadingMode.PAGED_RTL))
                        },
                        label = { Text("Manga (RTL)") },
                        modifier = Modifier.testTag("mode_paged_rtl")
                    )
                    FilterChip(
                        selected = readerSettings.readingMode == ReadingMode.PAGED_LTR,
                        onClick = {
                            repository.saveReaderSettings(readerSettings.copy(readingMode = ReadingMode.PAGED_LTR))
                        },
                        label = { Text("Paged (LTR)") },
                        modifier = Modifier.testTag("mode_paged_ltr")
                    )
                }

                // Background Color Selector
                Text(text = "Background Color", fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = readerSettings.background == ReaderBackground.BLACK,
                        onClick = {
                            repository.saveReaderSettings(readerSettings.copy(background = ReaderBackground.BLACK))
                        },
                        label = { Text("OLED Black") },
                        modifier = Modifier.testTag("bg_black")
                    )
                    FilterChip(
                        selected = readerSettings.background == ReaderBackground.DARK_GRAY,
                        onClick = {
                            repository.saveReaderSettings(readerSettings.copy(background = ReaderBackground.DARK_GRAY))
                        },
                        label = { Text("Dark Gray") },
                        modifier = Modifier.testTag("bg_dark_gray")
                    )
                    FilterChip(
                        selected = readerSettings.background == ReaderBackground.WHITE,
                        onClick = {
                            repository.saveReaderSettings(readerSettings.copy(background = ReaderBackground.WHITE))
                        },
                        label = { Text("White") },
                        modifier = Modifier.testTag("bg_white")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
