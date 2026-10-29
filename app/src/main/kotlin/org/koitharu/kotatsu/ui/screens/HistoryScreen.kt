package org.koitharu.kotatsu.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import org.koitharu.kotatsu.data.repository.MangaRepository
import org.koitharu.kotatsu.ui.components.*
import org.koitharu.kotatsu.ui.theme.LocalNeonPalette
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(
    repository: MangaRepository,
    onMangaClick: (String) -> Unit,
    onResumeClick: (String, String, Int) -> Unit,
    onExploreClick: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalNeonPalette.current
    val history by repository.history.collectAsState()
    val readerSettings by repository.readerSettings.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy • HH:mm", Locale.getDefault()) }

    val filteredHistory = remember(history, searchQuery) {
        if (searchQuery.isBlank()) history else {
            history.filter {
                it.mangaTitle.contains(searchQuery, ignoreCase = true) ||
                        it.chapterTitle.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    CosmicBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
                .testTag("history_screen")
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar with 3-dot popup (Image 4)
            Box(modifier = Modifier.fillMaxWidth()) {
                NeonSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "البحث في المانجا...",
                    onMenuClick = { showMenu = true }
                )

                // Dropdown menu matching Image 4
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier
                        .background(palette.surfaceCard)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    DropdownMenuItem(
                        text = { Text("محو سجل", color = palette.textPrimary, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth()) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFFF5252)) },
                        onClick = {
                            showMenu = false
                            showClearDialog = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("خيارات القائمة", color = palette.textPrimary, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth()) },
                        leadingIcon = { Icon(Icons.Default.List, contentDescription = null, tint = palette.primary) },
                        onClick = { showMenu = false }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                if (readerSettings.isIncognitoMode) "الوضع الخفي (مفعل)" else "الوضع الخفي",
                                color = if (readerSettings.isIncognitoMode) palette.primary else palette.textPrimary,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        leadingIcon = { Icon(Icons.Default.VisibilityOff, contentDescription = null, tint = palette.secondary) },
                        onClick = {
                            repository.saveReaderSettings(readerSettings.copy(isIncognitoMode = !readerSettings.isIncognitoMode))
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("الإعدادات", color = palette.textPrimary, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth()) },
                        leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null, tint = palette.primary) },
                        onClick = {
                            showMenu = false
                            onOpenSettings()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Incognito banner if active
            if (readerSettings.isIncognitoMode) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = palette.secondary.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, palette.secondary.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.VisibilityOff, contentDescription = null, tint = palette.secondary, modifier = Modifier.size(20.dp))
                        Text(
                            text = "الوضع الخفي مفعل: لن يتم حفظ سجل القراءة مؤقتاً.",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textPrimary
                        )
                    }
                }
            }

            if (filteredHistory.isEmpty()) {
                // Empty state matching Image 4
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(240.dp)
                                .shadow(24.dp, RoundedCornerShape(120.dp), spotColor = palette.glow),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.mascot_history_cat),
                                contentDescription = "History Mascot",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(32.dp))
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "كل ما تقرأه سيعرض هنا",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "تابع قراءة مانجاك المفضلة،\nوستجدها محفوظة في سجلك تلقائياً.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = palette.textSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        NeonPillButton(
                            text = "ابدأ الاستكشاف الآن",
                            icon = Icons.Default.Explore,
                            onClick = onExploreClick,
                            modifier = Modifier.width(230.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredHistory, key = { it.mangaId }) { item ->
                        val progress = (item.currentPage.toFloat() / item.totalPages.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
                        val percent = (progress * 100).toInt()

                        NeonCard(
                            onClick = { onMangaClick(item.mangaId) }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Resume button on the left (RTL)
                                IconButton(
                                    onClick = { onResumeClick(item.mangaId, item.chapterId, item.currentPage) },
                                    modifier = Modifier.testTag("resume_hist_${item.mangaId}")
                                ) {
                                    Icon(
                                        Icons.Default.PlayArrow,
                                        contentDescription = "Resume",
                                        tint = palette.primary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.mangaTitle,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = palette.textPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = item.chapterTitle,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = palette.primary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Neon progress bar
                                    LinearProgressIndicator(
                                        progress = { progress },
                                        color = palette.primary,
                                        trackColor = palette.surface,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "صفحة ${item.currentPage}/${item.totalPages} ($percent%)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = palette.textSecondary
                                        )
                                        Text(
                                            text = dateFormat.format(Date(item.timestamp)),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = palette.textSecondary.copy(alpha = 0.7f),
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                // Cover Image
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(item.coverUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = item.mangaTitle,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(width = 65.dp, height = 90.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("محو سجل القراءة", textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth()) },
            text = { Text("هل أنت متأكد من رغبتك في حذف كامل سجل القراءة في SSS X؟", textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth()) },
            confirmButton = {
                TextButton(
                    onClick = {
                        repository.clearHistory()
                        showClearDialog = false
                    }
                ) {
                    Text("محو السجل", color = Color(0xFFFF5252))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
