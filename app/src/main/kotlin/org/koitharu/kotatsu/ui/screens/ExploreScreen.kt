package org.koitharu.kotatsu.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.koitharu.kotatsu.data.model.Manga
import org.koitharu.kotatsu.data.model.MangaSource
import org.koitharu.kotatsu.data.repository.MangaRepository
import org.koitharu.kotatsu.ui.components.*
import org.koitharu.kotatsu.ui.theme.LocalNeonPalette

@Composable
fun ExploreScreen(
    repository: MangaRepository,
    onMangaClick: (String) -> Unit,
    onOpenDownloads: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalNeonPalette.current
    val coroutineScope = rememberCoroutineScope()
    val sources by repository.sources.collectAsState()
    val exploreManga by repository.exploreManga.collectAsState()
    val isLoading by repository.isLoadingExplore.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Manga>?>(null) }
    var selectedSource by remember { mutableStateOf<String?>(null) }
    var isGridView by remember { mutableStateOf(true) }

    fun triggerSearch(q: String) {
        if (q.isBlank()) {
            searchResults = null
            return
        }
        coroutineScope.launch {
            val res = repository.searchManga(q)
            searchResults = res
        }
    }

    val displayList = remember(exploreManga, searchResults, selectedSource) {
        val base = searchResults ?: exploreManga
        if (selectedSource == null) base else {
            base.filter { it.source.equals(selectedSource, ignoreCase = true) }
        }
    }

    CosmicBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
                .testTag("explore_screen")
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar (Image 8)
            NeonSearchBar(
                query = searchQuery,
                onQueryChange = {
                    searchQuery = it
                    triggerSearch(it)
                },
                placeholder = "البحث في المانجا..."
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Quick action pills (Image 8: التخزين المحلي, المحفوظات, عشوائي, التحميلات)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NeonFilterPill(
                    text = "التخزين المحلي",
                    isSelected = false,
                    icon = Icons.Default.Folder,
                    onClick = { /* local storage browse */ },
                    modifier = Modifier.weight(1f)
                )
                NeonFilterPill(
                    text = "المحفوظات",
                    isSelected = false,
                    icon = Icons.Default.Bookmark,
                    onClick = { /* bookmarks */ },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NeonFilterPill(
                    text = "عشوائي",
                    isSelected = false,
                    icon = Icons.Default.Casino,
                    onClick = {
                        val all = exploreManga
                        if (all.isNotEmpty()) {
                            onMangaClick(all.random().id)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                NeonFilterPill(
                    text = "التحميلات",
                    isSelected = false,
                    icon = Icons.Default.Download,
                    onClick = onOpenDownloads,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section Header: مصادر المانجا + Toggle (Image 8)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // View Mode Toggle (القائمة / الشبكة)
                Surface(
                    onClick = { isGridView = !isGridView },
                    shape = RoundedCornerShape(12.dp),
                    color = palette.surfaceCard,
                    border = BorderStroke(1.dp, palette.border.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (isGridView) "القائمة" else "الشبكة",
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.textPrimary
                        )
                        Icon(
                            if (isGridView) Icons.Default.List else Icons.Default.GridView,
                            contentDescription = null,
                            tint = palette.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Title
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "مصادر المانجا",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    )
                    Box(
                        modifier = Modifier
                            .size(width = 4.dp, height = 18.dp)
                            .background(palette.primary, RoundedCornerShape(2.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sources Grid (Image 8)
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(sources, key = { it.id }) { source ->
                    val isSelected = selectedSource == source.name
                    SourceTile(
                        source = source,
                        isSelected = isSelected,
                        onClick = {
                            selectedSource = if (isSelected) null else source.name
                        }
                    )
                }

                // If user selected a source or searched, render the manga items below
                if (displayList.isNotEmpty()) {
                    item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(4) }) {
                        Column(modifier = Modifier.padding(top = 16.dp)) {
                            Text(
                                text = if (selectedSource != null) "أبرز أعمال $selectedSource" else "الأعمال المقترحة",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = palette.primary,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.End
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }

                    items(displayList, key = { "manga_${it.id}" }, span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) { manga ->
                        NeonMangaCard(
                            manga = manga,
                            onClick = { onMangaClick(manga.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SourceTile(
    source: MangaSource,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalNeonPalette.current

    val tileColor = when (source.id) {
        "mangapro" -> Color(0xFF1E88E5)
        "manjanoon" -> Color(0xFF29B6F6)
        "areascans" -> Color(0xFF4CAF50)
        "arareascans" -> Color(0xFF26A69A)
        "thunderscans" -> Color(0xFF90CAF9)
        "hijalacom" -> Color(0xFFAB47BC)
        "mangaatrium" -> Color(0xFF00E5FF)
        "flares" -> Color(0xFFFF5252)
        "mangaswat" -> Color(0xFF7E57C2)
        "umimanga" -> Color(0xFF00B0FF)
        "mangaflame" -> Color(0xFFFF4081)
        "stellarsaber" -> Color(0xFF26C6DA)
        "3asq" -> Color(0xFFFF5722)
        "lekmanga" -> Color(0xFFEC407A)
        "linkmanga" -> Color(0xFFFFCA28)
        else -> palette.primary
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) palette.primary.copy(alpha = 0.25f) else palette.surfaceCard,
        border = BorderStroke(
            1.2.dp,
            if (isSelected) palette.primary else palette.border.copy(alpha = 0.35f)
        ),
        modifier = modifier
            .aspectRatio(0.9f)
            .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = tileColor)
            .testTag("source_tile_${source.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon Square with Initial or Glyph
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = tileColor.copy(alpha = 0.2f),
                border = BorderStroke(1.dp, tileColor.copy(alpha = 0.6f)),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    val initial = source.name.firstOrNull()?.uppercase() ?: "M"
                    Text(
                        text = if (source.id == "3asq") "3" else initial,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = tileColor,
                        fontSize = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = source.name,
                style = MaterialTheme.typography.labelSmall,
                color = palette.textPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}
