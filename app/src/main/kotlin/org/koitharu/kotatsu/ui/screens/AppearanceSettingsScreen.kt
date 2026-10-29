package org.koitharu.kotatsu.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koitharu.kotatsu.data.model.NeonThemeId
import org.koitharu.kotatsu.data.repository.MangaRepository
import org.koitharu.kotatsu.ui.components.*
import org.koitharu.kotatsu.ui.theme.LocalNeonPalette

@Composable
fun AppearanceSettingsScreen(
    repository: MangaRepository,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalNeonPalette.current
    val readerSettings by repository.readerSettings.collectAsState()

    var themeModeIndex by remember { mutableIntStateOf(1) } // 0: فاتح, 1: داكن, 2: حسب النظام
    var listModeIndex by remember { mutableIntStateOf(0) } // 0: شبكة, 1: قائمة
    var gridScale by remember { mutableFloatStateOf(1.0f) }

    var showQuickFilters by remember { mutableStateOf(true) }
    var showProgressIndicators by remember { mutableStateOf(true) }
    var showBookmarksInList by remember { mutableStateOf(true) }
    var showContinueReading by remember { mutableStateOf(true) }
    var showLatestUpdates by remember { mutableStateOf(true) }
    var lockUi by remember { mutableStateOf(false) }
    var confirmExit by remember { mutableStateOf(true) }

    CosmicBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .testTag("appearance_settings_screen")
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header matching Image 5
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = palette.textPrimary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "الإعدادات",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    )
                    Text(
                        text = "خصص مظهر التطبيق حسب ذوقك",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textSecondary
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = palette.primary.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, palette.primary.copy(alpha = 0.6f)),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = null,
                            tint = palette.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Card 1: مخطط الألوان (Image 5)
            NeonCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("مخطط الألوان", fontWeight = FontWeight.Bold, color = palette.textPrimary)
                        Text("اختر المظهر العام للتطبيق", style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Icon(Icons.Default.ColorLens, contentDescription = null, tint = palette.primary)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Presets cards: بنفسجي, وردي, أمبري, أخضر, داكن أزرق, ديناميكية
                val colorCards = listOf(
                    Triple("بنفسجي", Color(0xFF9D00FF), NeonThemeId.NEON_PURPLE),
                    Triple("وردي", Color(0xFFFF007F), NeonThemeId.CYBERPUNK_PINK),
                    Triple("أمبري", Color(0xFFFFAB00), NeonThemeId.GOLDEN_AMBER),
                    Triple("أخضر", Color(0xFF00E676), NeonThemeId.EMERALD_GREEN),
                    Triple("داكن أزرق", Color(0xFF0088FF), NeonThemeId.ELECTRIC_BLUE),
                    Triple("ديناميكية", Color(0xFF1B6BFF), NeonThemeId.ELECTRIC_BLUE_PURPLE)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    colorCards.forEach { (title, color, themeId) ->
                        val isSelected = readerSettings.neonThemeId == themeId
                        Surface(
                            onClick = { repository.setNeonTheme(themeId) },
                            shape = RoundedCornerShape(12.dp),
                            color = palette.surface,
                            border = BorderStroke(
                                if (isSelected) 2.dp else 1.dp,
                                if (isSelected) color else palette.border.copy(alpha = 0.25f)
                            ),
                            modifier = Modifier.weight(1f).height(65.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(modifier = Modifier.size(16.dp).background(color, CircleShape))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = title,
                                    fontSize = 9.sp,
                                    color = if (isSelected) color else palette.textSecondary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Card 2: مظهر (فاتح / داكن / حسب النظام)
            NeonCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("مظهر", fontWeight = FontWeight.Bold, color = palette.textPrimary)
                        Text("اختيار مظهر التطبيق العام", style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Icon(Icons.Default.DarkMode, contentDescription = null, tint = palette.primary)
                }

                Spacer(modifier = Modifier.height(14.dp))

                NeonSegmentedSelector(
                    options = listOf("فاتح", "داكن", "حسب النظام"),
                    selectedIndex = themeModeIndex,
                    onSelect = { themeModeIndex = it }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Card 3: اللغة (العربية)
            NeonCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = palette.textSecondary)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text("اللغة", fontWeight = FontWeight.Bold, color = palette.textPrimary)
                            Text("اختر لغة واجهة التطبيق (العربية)", style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                        }
                        Icon(Icons.Default.Language, contentDescription = null, tint = palette.primary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Card 4: وضع القائمة وحجم الشبكة
            NeonCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("وضع القائمة", fontWeight = FontWeight.Bold, color = palette.textPrimary)
                        Text("طريقة عرض العناصر في القوائم", style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Icon(Icons.Default.List, contentDescription = null, tint = palette.primary)
                }

                Spacer(modifier = Modifier.height(12.dp))

                NeonSegmentedSelector(
                    options = listOf("قائمة", "شبكة"),
                    selectedIndex = listModeIndex,
                    onSelect = { listModeIndex = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${(gridScale * 100).toInt()}%", color = palette.primary, fontWeight = FontWeight.Bold)
                    Text("حجم الشبكة", color = palette.textPrimary, fontWeight = FontWeight.SemiBold)
                }

                Slider(
                    value = gridScale,
                    onValueChange = { gridScale = it },
                    valueRange = 0.5f..1.5f,
                    colors = SliderDefaults.colors(thumbColor = palette.primary, activeTrackColor = palette.primary)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Card 5: خيارات القراءة
            NeonCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("خيارات القراءة", fontWeight = FontWeight.Bold, color = palette.textPrimary)
                        Text("تحكم في تجربة القراءة الخاصة بك", style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Icon(Icons.Default.MenuBook, contentDescription = null, tint = palette.primary)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(checked = showQuickFilters, onCheckedChange = { showQuickFilters = it })
                    Column(horizontalAlignment = Alignment.End) {
                        Text("إظهار عوامل التصفية السريعة", fontWeight = FontWeight.SemiBold, color = palette.textPrimary)
                        Text("إظهار عوامل التصفية السريعة فوق لوحة المحتوى", style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(checked = showProgressIndicators, onCheckedChange = { showProgressIndicators = it })
                    Column(horizontalAlignment = Alignment.End) {
                        Text("إظهار مؤشرات التقدم في القراءة", fontWeight = FontWeight.SemiBold, color = palette.textPrimary)
                        Text("عرض نسبة القراءة على العناصر", style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(checked = showBookmarksInList, onCheckedChange = { showBookmarksInList = it })
                    Column(horizontalAlignment = Alignment.End) {
                        Text("إظهار الإشارات في القوائم", fontWeight = FontWeight.SemiBold, color = palette.textPrimary)
                        Text("العلامات المحفوظة في الفصول والقوائم", style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Card 6: الشاشة الرئيسية
            NeonCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("الشاشة الرئيسية", fontWeight = FontWeight.Bold, color = palette.textPrimary)
                        Text("التحكم بما يظهر في الشاشة الرئيسية", style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Icon(Icons.Default.Home, contentDescription = null, tint = palette.primary)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(checked = showContinueReading, onCheckedChange = { showContinueReading = it })
                    Column(horizontalAlignment = Alignment.End) {
                        Text("إظهار قسم المتابعة المستمرة", fontWeight = FontWeight.SemiBold, color = palette.textPrimary)
                        Text("عرض زر المتابعة العائم في الشاشة الرئيسية", style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(checked = showLatestUpdates, onCheckedChange = { showLatestUpdates = it })
                    Column(horizontalAlignment = Alignment.End) {
                        Text("إظهار أحدث التحديثات", fontWeight = FontWeight.SemiBold, color = palette.textPrimary)
                        Text("عرض أحدث الإضافات والمصادر الجديدة", style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Card 7: الخصوصية
            NeonCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("الخصوصية", fontWeight = FontWeight.Bold, color = palette.textPrimary)
                        Text("إدارة بياناتك وخصوصية استخدامك", style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Icon(Icons.Default.Security, contentDescription = null, tint = palette.primary)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(checked = lockUi, onCheckedChange = { lockUi = it })
                    Column(horizontalAlignment = Alignment.End) {
                        Text("تثبيت واجهة المستخدم", fontWeight = FontWeight.SemiBold, color = palette.textPrimary)
                        Text("منع تغييرات التصميم حسب تفضيلات النظام", style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(checked = confirmExit, onCheckedChange = { confirmExit = it })
                    Column(horizontalAlignment = Alignment.End) {
                        Text("تأكيد الخروج", fontWeight = FontWeight.SemiBold, color = palette.textPrimary)
                        Text("إظهار تأكيد قبل إغلاق التطبيق", style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
