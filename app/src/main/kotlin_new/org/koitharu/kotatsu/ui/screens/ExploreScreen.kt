package org.koitharu.kotatsu.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koitharu.kotatsu.data.model.Manga
import org.koitharu.kotatsu.data.repository.MangaRepository
import org.koitharu.kotatsu.ui.components.MangaGridCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    repository: MangaRepository,
    onMangaClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val exploreManga by repository.exploreManga.collectAsState()
    val isLoading by repository.isLoadingExplore.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Manga>?>(null) }
    var isSearching by remember { mutableStateOf(false) }
    var selectedGenre by remember { mutableStateOf("All") }

    val genres = listOf("All", "Action", "Fantasy", "Comedy", "Supernatural", "Adventure", "Romance", "Horror")

    fun triggerSearch(query: String) {
        if (query.isBlank()) {
            searchResults = null
            return
        }
        isSearching = true
        coroutineScope.launch {
            val results = repository.searchManga(query)
            searchResults = results
            isSearching = false
        }
    }

    val displayList = remember(exploreManga, searchResults, selectedGenre) {
        val base = searchResults ?: exploreManga
        if (selectedGenre == "All") {
            base
        } else {
            base.filter { manga ->
                manga.genres.any { it.equals(selectedGenre, ignoreCase = true) }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("explore_screen")
    ) {
        // Search bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    triggerSearch(it)
                },
                placeholder = { Text("Search MangaDex & online catalog...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            searchResults = null
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("explore_search_input"),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                singleLine = true
            )

            IconButton(
                onClick = { repository.loadExplore() },
                modifier = Modifier.testTag("refresh_explore_btn")
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh catalog")
            }
        }

        // Genre filter chips row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            genres.forEach { genre ->
                val isSelected = selectedGenre == genre
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedGenre = genre },
                    label = { Text(genre) },
                    modifier = Modifier.testTag("genre_chip_$genre")
                )
            }
        }

        // Content
        if (isLoading || isSearching) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.testTag("explore_loading_indicator")
                )
            }
        } else if (displayList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "No manga found",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(onClick = {
                        searchQuery = ""
                        searchResults = null
                        selectedGenre = "All"
                        repository.loadExplore()
                    }) {
                        Text("Reset Search")
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 140.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("explore_manga_grid")
            ) {
                items(displayList, key = { it.id }) { manga ->
                    MangaGridCard(
                        manga = manga,
                        onClick = { onMangaClick(manga.id) },
                        onFavoriteToggle = { repository.toggleFavorite(manga) }
                    )
                }
            }
        }
    }
}
