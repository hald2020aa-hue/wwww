package org.koitharu.kotatsu.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RssFeed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.koitharu.kotatsu.data.repository.MangaRepository
import org.koitharu.kotatsu.ui.components.CosmicBackground
import org.koitharu.kotatsu.ui.components.NeonCard
import org.koitharu.kotatsu.ui.components.NeonSearchBar
import org.koitharu.kotatsu.ui.theme.LocalNeonPalette

@Composable
fun FeedScreen(
    repository: MangaRepository,
    onMangaClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalNeonPalette.current
    val exploreManga by repository.exploreManga.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    val filtered = remember(exploreManga, searchQuery) {
        if (searchQuery.isBlank()) exploreManga else {
            exploreManga.filter { it.title.contains(searchQuery, ignoreCase = true) }
        }
    }

    CosmicBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
                .testTag("feed_screen")
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            NeonSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "البحث في الموجز والتحديثات..."
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "آخر الفصول المضافة",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.RssFeed, contentDescription = null, tint = palette.primary)
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filtered, key = { "feed_${it.id}" }) { manga ->
                    NeonCard(onClick = { onMangaClick(manga.id) }) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = manga.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = palette.textPrimary
                                )
                                Text(
                                    text = "تمت إضافة الفصل الجديد • ${manga.source}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = palette.primary
                                )
                                Text(
                                    text = "المؤلف: ${manga.author}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = palette.textSecondary
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Surface(
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                                color = palette.primary.copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, palette.primary)
                            ) {
                                Text(
                                    text = "جديد",
                                    color = palette.primary,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
