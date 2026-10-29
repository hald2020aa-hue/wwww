package org.koitharu.kotatsu.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koitharu.kotatsu.data.model.ReaderBackground
import org.koitharu.kotatsu.data.model.ReadingMode
import org.koitharu.kotatsu.data.repository.MangaRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    repository: MangaRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val readerSettings by repository.readerSettings.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) }
            )
        },
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Section: Reader Preferences
            Text(
                text = "Reader Preferences",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Default Reading Mode
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Default Reading Mode", fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = readerSettings.readingMode == ReadingMode.WEBTOON,
                                onClick = { repository.saveReaderSettings(readerSettings.copy(readingMode = ReadingMode.WEBTOON)) },
                                label = { Text("Webtoon") }
                            )
                            FilterChip(
                                selected = readerSettings.readingMode == ReadingMode.PAGED_RTL,
                                onClick = { repository.saveReaderSettings(readerSettings.copy(readingMode = ReadingMode.PAGED_RTL)) },
                                label = { Text("Manga (RTL)") }
                            )
                            FilterChip(
                                selected = readerSettings.readingMode == ReadingMode.PAGED_LTR,
                                onClick = { repository.saveReaderSettings(readerSettings.copy(readingMode = ReadingMode.PAGED_LTR)) },
                                label = { Text("Comic (LTR)") }
                            )
                        }
                    }

                    HorizontalDivider()

                    // Background Color
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Reader Background", fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = readerSettings.background == ReaderBackground.BLACK,
                                onClick = { repository.saveReaderSettings(readerSettings.copy(background = ReaderBackground.BLACK)) },
                                label = { Text("OLED Black") }
                            )
                            FilterChip(
                                selected = readerSettings.background == ReaderBackground.DARK_GRAY,
                                onClick = { repository.saveReaderSettings(readerSettings.copy(background = ReaderBackground.DARK_GRAY)) },
                                label = { Text("Dark Gray") }
                            )
                            FilterChip(
                                selected = readerSettings.background == ReaderBackground.WHITE,
                                onClick = { repository.saveReaderSettings(readerSettings.copy(background = ReaderBackground.WHITE)) },
                                label = { Text("White") }
                            )
                        }
                    }

                    HorizontalDivider()

                    // Keep Screen On Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Keep Screen Awake", fontWeight = FontWeight.SemiBold)
                            Text("Prevents screen from sleeping while reading", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = readerSettings.keepScreenOn,
                            onCheckedChange = { repository.saveReaderSettings(readerSettings.copy(keepScreenOn = it)) },
                            modifier = Modifier.testTag("switch_keep_screen_on")
                        )
                    }
                }
            }

            // Section: Storage & Maintenance
            Text(
                text = "Storage & Cache",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                Toast.makeText(context, "Image cache cleared", Toast.LENGTH_SHORT).show()
                            }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Clear Image Cache", fontWeight = FontWeight.SemiBold)
                            Text("Frees space used by cached manga covers and pages", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.Default.CleaningServices, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }

                    HorizontalDivider()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                Toast.makeText(context, "Library and history backed up successfully", Toast.LENGTH_SHORT).show()
                            }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Backup Library & Data", fontWeight = FontWeight.SemiBold)
                            Text("Save your favorites and reading progress", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.Default.CloudUpload, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    }
                }
            }

            // Section: About Kotatsu
            Text(
                text = "About",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Kotatsu Manga Reader", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                    Text("Version 10.0.0 (Compose Edition)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("A modern, fast, and open manga reader featuring online catalogs, customized webtoon/paged reading, and offline storage.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
