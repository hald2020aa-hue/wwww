package org.koitharu.kotatsu.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.shadow
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
import org.koitharu.kotatsu.ui.components.CosmicBackground
import org.koitharu.kotatsu.ui.components.NeonCard
import org.koitharu.kotatsu.ui.theme.LocalNeonPalette

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
    val palette = LocalNeonPalette.current
    val library by repository.library.collectAsState()
    val exploreManga by repository.exploreManga.collectAsState()
    val categories by repository.categories.collectAsState()
    val readChapters by repository.readChapters.collectAsState()
    val downloadedChapters by repository.downloadedChapters.collectAsState()

    val manga = remember(library, exploreManga, mangaId) {
        library.find { it.id == mangaId }
            ?: exploreManga.find { it.id == mangaId }
            ?: Manga(
                id = mangaId,
                title = "تفاصيل المانجا",
                coverUrl = "https://picsum.photos/seed/$mangaId/400/600"
            )
    }

    val isInLibrary = library.any { it.id == mangaId }
    val currentCategory = categories.find { it.id == manga.categoryId }?.name ?: "المفضلة"

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

    val resumeChapter = remember(chapters, manga, readChapters) {
        if (manga.lastReadChapterId != null) {
            chapters.find { it.id == manga.lastReadChapterId }
        } else {
            chapters.firstOrNull { !readChapters.contains(it.id) } ?: chapters.firstOrNull()
        }
    }

    CosmicBackground(modifier = modifier) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Surface(
                    color = Color(0xCC040818),
                    border = BorderStroke(1.dp, palette.border.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier.testTag("details_back_btn")
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "رجوع",
                                tint = palette.textPrimary
                            )
                        }

                        Text(
                            text = manga.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                        )

                        IconButton(
                            onClick = { repository.toggleFavorite(manga) },
                            modifier = Modifier.testTag("details_favorite_toggle")
                        ) {
                            Icon(
                                imageVector = if (manga.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "المفضلة",
                                tint = if (manga.isFavorite) Color(0xFFFF1744) else palette.textPrimary
                            )
                        }
                    }
                }
            },
            bottomBar = {
                Surface(
                    color = Color(0xEB040818),
                    border = BorderStroke(1.dp, palette.border.copy(alpha = 0.35f)),
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Big Neon Gradient Read Button
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.Transparent,
                            border = BorderStroke(1.2.dp, palette.primary.copy(alpha = 0.8f)),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = palette.primary)
                                .clickable(enabled = resumeChapter != null) {
                                    resumeChapter?.let { ch ->
                                        val page = if (ch.id == manga.lastReadChapterId) manga.lastReadPage else 1
                                        onReadChapterClick(manga.id, ch.id, page)
                                    }
                                }
                                .testTag("resume_reading_button")
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(palette.primaryGradient),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.MenuBook, contentDescription = null, tint = Color.White)
                                    Text(
                                        text = if (manga.lastReadChapterId != null) "متابعة القراءة" else "ابدأ القراءة الآن",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        // Category Pill
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = palette.surfaceCard,
                            border = BorderStroke(1.dp, palette.secondary.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .height(50.dp)
                                .clickable { showCategorySheet = true }
                                .testTag("library_category_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (isInLibrary) Icons.Filled.BookmarkAdded else Icons.Outlined.BookmarkAdd,
                                    contentDescription = null,
                                    tint = palette.secondary
                                )
                                Text(
                                    text = if (isInLibrary) currentCategory else "إضافة",
                                    color = palette.textPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .testTag("manga_details_list")
            ) {
                // Header Banner & Cover
                item {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        // Blurred top aura
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(palette.primary.copy(alpha = 0.25f), Color.Transparent)
                                    )
                                )
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.5.dp, palette.primary.copy(alpha = 0.8f)),
                                shadowElevation = 16.dp,
                                modifier = Modifier
                                    .size(width = 125.dp, height = 180.dp)
                                    .shadow(16.dp, RoundedCornerShape(16.dp), spotColor = palette.primary)
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(manga.coverUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = manga.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = manga.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = palette.textPrimary
                                )

                                Text(
                                    text = "المؤلف: ${manga.author}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = palette.textSecondary
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = palette.primary.copy(alpha = 0.2f),
                                        border = BorderStroke(0.8.dp, palette.primary.copy(alpha = 0.5f))
                                    ) {
                                        Text(
                                            text = manga.source,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = palette.tertiary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (manga.status == MangaStatus.COMPLETED) Color(0x3300E676) else Color(0x332979FF),
                                        border = BorderStroke(0.8.dp, if (manga.status == MangaStatus.COMPLETED) Color(0xFF00E676) else Color(0xFF2979FF))
                                    ) {
                                        Text(
                                            text = if (manga.status == MangaStatus.COMPLETED) "مكتمل" else "مستمر",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

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
                                        fontWeight = FontWeight.SemiBold,
                                        color = palette.textPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                // Genre Pills
                if (manga.genres.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            manga.genres.forEach { genre ->
                                Surface(
                                    shape = CircleShape,
                                    color = palette.surfaceCard,
                                    border = BorderStroke(1.dp, palette.border.copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = genre,
                                        fontSize = 11.sp,
                                        color = palette.tertiary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                // Synopsis
                if (manga.description.isNotBlank()) {
                    item {
                        NeonCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .clickable { isDescriptionExpanded = !isDescriptionExpanded }
                        ) {
                            Text(
                                text = "نبذة عن القصة",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = palette.textPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = manga.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = palette.textSecondary,
                                maxLines = if (isDescriptionExpanded) Int.MAX_VALUE else 3,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 20.sp
                            )
                            Text(
                                text = if (isDescriptionExpanded) "عرض أقل" else "قراءة المزيد...",
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.tertiary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                    }
                }

                // Chapters Section Header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "الفصول المتاحة (${chapters.size})",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary
                        )

                        IconButton(
                            onClick = { isSortAscending = !isSortAscending },
                            modifier = Modifier.testTag("sort_chapters_btn")
                        ) {
                            Icon(
                                imageVector = if (isSortAscending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                contentDescription = "ترتيب",
                                tint = palette.tertiary
                            )
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
                            CircularProgressIndicator(color = palette.primary)
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
                                text = "لا توجد فصول متاحة حالياً",
                                color = palette.textSecondary
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
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onReadChapterClick(manga.id, chapter.id, 1) }
                                .testTag("chapter_item_${chapter.id}"),
                            color = if (isRead) Color(0x33081024) else palette.surfaceCard,
                            border = BorderStroke(
                                1.dp,
                                if (isRead) palette.border.copy(alpha = 0.2f) else palette.border.copy(alpha = 0.45f)
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = chapter.title,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = if (isRead) FontWeight.Normal else FontWeight.Bold,
                                        color = if (isRead) palette.textSecondary else palette.textPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${chapter.uploadDate} • ${chapter.pageCount} صفحة",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = palette.textSecondary.copy(alpha = 0.7f)
                                    )
                                }

                                // Mark read toggle
                                IconButton(
                                    onClick = { repository.markChapterRead(chapter.id, !isRead) },
                                    modifier = Modifier.testTag("read_toggle_${chapter.id}")
                                ) {
                                    Icon(
                                        imageVector = if (isRead) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircleOutline,
                                        contentDescription = "تمت القراءة",
                                        tint = if (isRead) palette.tertiary else palette.textSecondary
                                    )
                                }

                                // Download toggle
                                IconButton(
                                    onClick = { repository.toggleDownload(chapter.id) },
                                    modifier = Modifier.testTag("download_toggle_${chapter.id}")
                                ) {
                                    Icon(
                                        imageVector = if (isDownloaded) Icons.Filled.DownloadDone else Icons.Outlined.Download,
                                        contentDescription = "تنزيل",
                                        tint = if (isDownloaded) palette.secondary else palette.textSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Category Selector Bottom Sheet
        if (showCategorySheet) {
            ModalBottomSheet(
                onDismissRequest = { showCategorySheet = false },
                sheetState = rememberModalBottomSheetState(),
                containerColor = palette.surfaceCard
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "اختر التصنيف في المفضلة",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    )

                    categories.forEach { cat ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    repository.setMangaCategory(manga.id, cat.id)
                                    showCategorySheet = false
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = if (manga.categoryId == cat.id) palette.primary.copy(alpha = 0.25f) else Color.Transparent,
                            border = BorderStroke(
                                1.dp,
                                if (manga.categoryId == cat.id) palette.primary else palette.border.copy(alpha = 0.3f)
                            )
                        ) {
                            Text(
                                text = cat.name,
                                color = palette.textPrimary,
                                modifier = Modifier.padding(16.dp),
                                fontWeight = if (manga.categoryId == cat.id) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}
