package org.koitharu.kotatsu.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.koitharu.kotatsu.R
import org.koitharu.kotatsu.data.model.Manga
import org.koitharu.kotatsu.data.repository.MangaRepository
import org.koitharu.kotatsu.ui.components.*
import org.koitharu.kotatsu.ui.theme.LocalNeonPalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    repository: MangaRepository,
    onMangaClick: (String) -> Unit,
    onExploreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalNeonPalette.current
    val library by repository.library.collectAsState()
    val readerSettings by repository.readerSettings.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterPill by remember { mutableStateOf("all") } // "all", "favs", "read_later"
    var showFilterSheet by remember { mutableStateOf(false) }

    var listMode by remember { mutableStateOf("grid") } // "grid", "details", "compact"
    var gridScale by remember { mutableFloatStateOf(0.5f) }
    var sortOrder by remember { mutableStateOf("آخر ما تم قراءته") }

    val filteredManga = remember(library, searchQuery, selectedFilterPill) {
        library.filter { manga ->
            val matchPill = when (selectedFilterPill) {
                "favs" -> manga.isFavorite
                "read_later" -> manga.categoryId == "plan"
                else -> true
            }
            val matchSearch = searchQuery.isBlank() ||
                    manga.title.contains(searchQuery, ignoreCase = true) ||
                    manga.author.contains(searchQuery, ignoreCase = true)
            matchPill && matchSearch
        }
    }

    CosmicBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
                .testTag("favorites_screen")
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar (Image 2)
            NeonSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "البحث في المانجا...",
                onMenuClick = { showFilterSheet = true }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Top Filter Pills (Image 2: كل, المفضلة, اقرأ لاحقا)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                NeonFilterPill(
                    text = "كل",
                    isSelected = selectedFilterPill == "all",
                    icon = Icons.Default.GridView,
                    onClick = { selectedFilterPill = "all" },
                    modifier = Modifier.weight(1f)
                )

                NeonFilterPill(
                    text = "المفضلة",
                    isSelected = selectedFilterPill == "favs",
                    icon = Icons.Default.Favorite,
                    onClick = { selectedFilterPill = "favs" },
                    modifier = Modifier.weight(1f)
                )

                NeonFilterPill(
                    text = "اقرأ لاحقا",
                    isSelected = selectedFilterPill == "read_later",
                    icon = Icons.Default.AccessTime,
                    onClick = { selectedFilterPill = "read_later" },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Content Area
            if (filteredManga.isEmpty()) {
                // Empty state matching Image 2 exactly
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Glowing Mascot Image (cropped from Image 2)
                        Box(
                            modifier = Modifier
                                .size(240.dp)
                                .shadow(24.dp, RoundedCornerShape(120.dp), spotColor = palette.glow),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.mascot_reading_cat),
                                contentDescription = "SSS X Cat Mascot",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(32.dp))
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "يبدو أنه فارغ...",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "لا مفضلة بعد",
                            style = MaterialTheme.typography.bodyLarge,
                            color = palette.textSecondary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        NeonPillButton(
                            text = "اكتشف مانجا جديدة",
                            icon = Icons.Default.FavoriteBorder,
                            onClick = onExploreClick,
                            modifier = Modifier.width(230.dp)
                        )
                    }
                }
            } else {
                // Manga Grid with neon card borders
                val columns = if (gridScale > 0.6f) 3 else 2
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredManga, key = { it.id }) { manga ->
                        NeonMangaCard(
                            manga = manga,
                            onClick = { onMangaClick(manga.id) }
                        )
                    }
                }
            }
        }
    }

    // Filter Bottom Sheet (matching Image 3)
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            containerColor = palette.surface,
            scrimColor = Color.Black.copy(alpha = 0.65f),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            modifier = Modifier.testTag("filter_bottom_sheet")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "وضع القائمة",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.GridView, contentDescription = null, tint = palette.primary)
                }

                // 3 Options: شبكة, تفاصيل, المدمج
                NeonSegmentedSelector(
                    options = listOf("المدمج", "تفاصيل", "شبكة"),
                    selectedIndex = when (listMode) {
                        "compact" -> 0
                        "details" -> 1
                        else -> 2
                    },
                    onSelect = { idx ->
                        listMode = when (idx) {
                            0 -> "compact"
                            1 -> "details"
                            else -> "grid"
                        }
                    }
                )

                // Grid Size Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "حجم الشبكة",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.Widgets, contentDescription = null, tint = palette.primary)
                }

                Slider(
                    value = gridScale,
                    onValueChange = { gridScale = it },
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = palette.primary,
                        activeTrackColor = palette.primary,
                        inactiveTrackColor = palette.border.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Sort Order Dropdown Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ترتيب الفرز",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.SwapVert, contentDescription = null, tint = palette.primary)
                }

                NeonCard(
                    contentPadding = PaddingValues(14.dp),
                    onClick = {
                        sortOrder = if (sortOrder == "آخر ما تم قراءته") "تاريخ الإضافة" else "آخر ما تم قراءته"
                    }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = palette.textSecondary)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = sortOrder, color = palette.textPrimary, fontWeight = FontWeight.SemiBold)
                            Icon(Icons.Default.AccessTime, contentDescription = null, tint = palette.primary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun NeonMangaCard(
    manga: Manga,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalNeonPalette.current

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = palette.surfaceCard,
        border = BorderStroke(1.2.dp, palette.border.copy(alpha = 0.6f)),
        modifier = modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(16.dp), spotColor = palette.glow)
            .testTag("neon_manga_card_${manga.id}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.72f)
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

                // Neon badge for source
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = palette.primary.copy(alpha = 0.85f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Text(
                        text = manga.source,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Text(
                    text = manga.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = manga.author,
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
