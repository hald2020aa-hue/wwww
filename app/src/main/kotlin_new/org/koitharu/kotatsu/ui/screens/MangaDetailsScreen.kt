package org.koitharu.kotatsu.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import org.koitharu.kotatsu.data.model.Chapter
import org.koitharu.kotatsu.data.model.Manga
import org.koitharu.kotatsu.data.model.MangaStatus
import org.koitharu.kotatsu.data.repository.MangaRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MangaDetailsScreen(
    mangaId: String,
    repository: MangaRepository,
    onBackClick: () -> Unit,
    onReadChapterClick: (String, String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val library by repository.library.collectAsState()
    val exploreManga by repository.exploreManga.collectAsState()
    val categories by repository.categories.collectAsState()
    val readChapters by repository.readChapters.collectAsState()
    val downloadedChapters by repository.downloadedChapters.collectAsState()

    // Find manga from library or explore
    val manga = remember(library, exploreManga, mangaId) {
        library.find { it.id == mangaId }
            ?: exploreManga.find { it.id == mangaId }
            ?: Manga(
                id = mangaId,
                title = "Manga Details",
                coverUrl = "https://picsum.photos/seed/$mangaId/400/600"
            )
    }

    val isInLibrary = library.any { it.id == mangaId }
    val currentCategory = categories.find { it.id == manga.categoryId }?.name ?: "Reading"

    var chapters by remember { mutableStateOf<List<Chapter>>(emptyList()) }
    var isLoadingChapters by remember { mutableStateOf(true) }
    var isSortAscending by remember { mutableStateOf(true) }
    var chapterSearchQuery by remember { mutableStateOf("") }
    var isDescriptionExpanded by remember { mutableStateOf(false) }
    var showCategorySheet by remember { mutableStateOf(false) }

    LaunchedEffect(mangaId) {
        isLoadingChapters = true
        chapters = repository.getChapters(mangaId)
        isLoadingChapters = false
    }

    val sortedChapters = remember(chapters, isSortAscending, chapterSearchQuery) {
        val filtered = if (chapterSearchQuery.isBlank()) chapters else {
            chapters.filter {
                it.title.contains(chapterSearchQuery, ignoreCase = true) ||
                        it.number.toString().contains(chapterSearchQuery)
            }
        }
        if (isSortAscending) filtered.sortedBy { it.number } else filtered.sortedByDescending { it.number }
    }

    // Determine first unread chapter or resume point
    val resumeChapter = remember(chapters, manga, readChapters) {
        if (manga.lastReadChapterId != null) {
            chapters.find { it.id == manga.lastReadChapterId }
        } else {
            chapters.firstOrNull { !readChapters.contains(it.id) } ?: chapters.firstOrNull()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(manga.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.testTag("details_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { repository.toggleFavorite(manga) },
                        modifier = Modifier.testTag("details_favorite_toggle")
                    ) {
                        Icon(
                            imageVector = if (manga.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (manga.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            // Prominent Read / Resume Button
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            resumeChapter?.let { ch ->
                                val page = if (ch.id == manga.lastReadChapterId) manga.lastReadPage else 1
                                onReadChapterClick(manga.id, ch.id, page)
                            }
                        },
                        enabled = resumeChapter != null,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("resume_reading_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.MenuBook, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (manga.lastReadChapterId != null) "Resume Reading" else "Start Reading",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    OutlinedButton(
                        onClick = { showCategorySheet = true },
                        modifier = Modifier
                            .height(50.dp)
                            .testTag("library_category_btn")
                    ) {
                        Icon(
                            imageVector = if (isInLibrary) Icons.Filled.BookmarkAdded else Icons.Outlined.BookmarkAdd,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isInLibrary) currentCategory else "Add")
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("manga_details_list")
        ) {
            // Header: Cover + Info
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(manga.coverUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = manga.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(width = 120.dp, height = 175.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = manga.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Author: ${manga.author}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = manga.source,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (manga.status == MangaStatus.COMPLETED) Color(0xFF2E7D32) else MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = if (manga.status == MangaStatus.COMPLETED) "Completed" else "Ongoing",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Rating
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFB300),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${manga.rating} / 5.0",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Genre Chips
            if (manga.genres.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        manga.genres.forEach { genre ->
                            SuggestionChip(
                                onClick = {},
                                label = { Text(genre, fontSize = 12.sp) }
                            )
                        }
                    }
                }
            }

            // Synopsis
            if (manga.description.isNotBlank()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .clickable { isDescriptionExpanded = !isDescriptionExpanded }
                    ) {
                        Text(
                            text = "Synopsis",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = manga.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = if (isDescriptionExpanded) Int.MAX_VALUE else 3,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (isDescriptionExpanded) "Show Less" else "Read More",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Chapters Header & Filter
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Chapters (${chapters.size})",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { isSortAscending = !isSortAscending },
                            modifier = Modifier.testTag("sort_chapters_btn")
                        ) {
                            Icon(
                                imageVector = if (isSortAscending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                contentDescription = "Sort order"
                            )
                        }
                    }
                }
            }

            // Chapters List
            if (isLoadingChapters) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (sortedChapters.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No chapters found",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(sortedChapters, key = { it.id }) { chapter ->
                    val isRead = readChapters.contains(chapter.id)
                    val isDownloaded = downloadedChapters.contains(chapter.id)

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onReadChapterClick(manga.id, chapter.id, 1) }
                            .testTag("chapter_item_${chapter.id}"),
                        color = if (isRead) MaterialTheme.colorScheme.surface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = chapter.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isRead) FontWeight.Normal else FontWeight.SemiBold,
                                    color = if (isRead) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${chapter.uploadDate} • ${chapter.pageCount} pages",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Mark read toggle
                            IconButton(
                                onClick = { repository.markChapterRead(chapter.id, !isRead) },
                                modifier = Modifier.testTag("read_toggle_${chapter.id}")
                            ) {
                                Icon(
                                    imageVector = if (isRead) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircleOutline,
                                    contentDescription = "Toggle read",
                                    tint = if (isRead) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Download toggle
                            IconButton(
                                onClick = { repository.toggleDownload(chapter.id) },
                                modifier = Modifier.testTag("download_toggle_${chapter.id}")
                            ) {
                                Icon(
                                    imageVector = if (isDownloaded) Icons.Filled.DownloadDone else Icons.Outlined.Download,
                                    contentDescription = "Download chapter",
                                    tint = if (isDownloaded) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        thickness = 0.5.dp
                    )
                }
            }
        }
    }

    // Category Selector Bottom Sheet
    if (showCategorySheet) {
        ModalBottomSheet(
            onDismissRequest = { showCategorySheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Library Category",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                categories.filter { it.id != "all" && it.id != "favorites" }.forEach { category ->
                    val isCurrent = manga.categoryId == category.id && isInLibrary
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                repository.addToLibrary(manga, category.id)
                                showCategorySheet = false
                            },
                        color = if (isCurrent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                            )
                            if (isCurrent) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                if (isInLibrary) {
                    TextButton(
                        onClick = {
                            repository.removeFromLibrary(manga.id)
                            showCategorySheet = false
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Remove from Library")
                    }
                }
            }
        }
    }
}
