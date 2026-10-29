package org.koitharu.kotatsu.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koitharu.kotatsu.data.model.NeonColorPalette
import org.koitharu.kotatsu.data.model.NeonThemeId
import org.koitharu.kotatsu.data.model.NeonThemes
import org.koitharu.kotatsu.data.repository.MangaRepository
import org.koitharu.kotatsu.ui.components.CosmicBackground
import org.koitharu.kotatsu.ui.components.NeonCard
import org.koitharu.kotatsu.ui.components.NeonSettingItem
import org.koitharu.kotatsu.ui.theme.LocalNeonPalette

@Composable
fun NeonCustomizationScreen(
    repository: MangaRepository,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activePalette = LocalNeonPalette.current
    val readerSettings by repository.readerSettings.collectAsState()

    var customPrimary by remember { mutableStateOf(readerSettings.customPrimaryColorHex) }
    var customSecondary by remember { mutableStateOf(readerSettings.customSecondaryColorHex) }

    CosmicBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .testTag("neon_customization_screen")
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = activePalette.textPrimary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "ألوان النيون في SSS X",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = activePalette.textPrimary
                    )
                    Text(
                        text = "تخصيص المظهر الكامل والتوهج",
                        style = MaterialTheme.typography.bodySmall,
                        color = activePalette.textSecondary
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = activePalette.primary.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, activePalette.primary.copy(alpha = 0.6f)),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.ColorLens,
                            contentDescription = null,
                            tint = activePalette.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Reduced glow option for low-end devices
            NeonSettingItem(
                title = "تقليل تأثيرات التوهج والحركة",
                subtitle = "مفيد للأجهزة الضعيفة وتوفير طاقة البطارية",
                isChecked = readerSettings.reducedMotionAndGlow,
                onCheckedChange = {
                    repository.saveReaderSettings(readerSettings.copy(reducedMotionAndGlow = it))
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "الثيمات الجاهزة (12 ثيم)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = activePalette.primary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 12 Neon Themes Cards
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                NeonThemes.allThemes.chunked(2).forEach { rowThemes ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowThemes.forEach { theme ->
                            val isSelected = readerSettings.neonThemeId == theme.id

                            Surface(
                                onClick = { repository.setNeonTheme(theme.id) },
                                shape = RoundedCornerShape(18.dp),
                                color = theme.surfaceCard,
                                border = BorderStroke(
                                    if (isSelected) 2.dp else 1.dp,
                                    if (isSelected) theme.primary else theme.border.copy(alpha = 0.35f)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(120.dp)
                                    .shadow(
                                        if (isSelected) 10.dp else 2.dp,
                                        RoundedCornerShape(18.dp),
                                        spotColor = theme.glow
                                    )
                                    .testTag("theme_card_${theme.id.name}")
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (isSelected) {
                                            Surface(
                                                shape = CircleShape,
                                                color = theme.primary,
                                                modifier = Modifier.size(22.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                                }
                                            }
                                        } else {
                                            Spacer(modifier = Modifier.size(22.dp))
                                        }

                                        // Color Swatches
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Box(modifier = Modifier.size(16.dp).background(theme.primary, CircleShape).border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape))
                                            Box(modifier = Modifier.size(16.dp).background(theme.secondary, CircleShape))
                                            Box(modifier = Modifier.size(16.dp).background(theme.tertiary, CircleShape))
                                        }
                                    }

                                    Column {
                                        Text(
                                            text = theme.nameAr,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = theme.nameEn,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = theme.textSecondary,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Custom Color Picker Section
            Text(
                text = "تخصيص يدوي للألوان",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = activePalette.primary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )

            Spacer(modifier = Modifier.height(12.dp))

            NeonCard {
                Text(
                    text = "اختر التدرج الخاص بك",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = activePalette.textPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))

                val customPresets = listOf(
                    "#FF0055" to "#7A00FF",
                    "#00FFA3" to "#00B8FF",
                    "#FFB800" to "#FF3D00",
                    "#00F0FF" to "#FF00E5",
                    "#7000FF" to "#00E5FF",
                    "#FF3366" to "#FFCC00"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    customPresets.forEach { (p, s) ->
                        val pColor = Color(android.graphics.Color.parseColor(p))
                        val sColor = Color(android.graphics.Color.parseColor(s))
                        Surface(
                            onClick = {
                                customPrimary = p
                                customSecondary = s
                                repository.setCustomColors(p, s)
                            },
                            shape = CircleShape,
                            modifier = Modifier.size(42.dp),
                            border = BorderStroke(2.dp, Color.White.copy(alpha = 0.5f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Brush.linearGradient(listOf(pColor, sColor)))
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}
