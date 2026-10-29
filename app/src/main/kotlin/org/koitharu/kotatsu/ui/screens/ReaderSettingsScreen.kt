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
import org.koitharu.kotatsu.data.model.ReaderSettings
import org.koitharu.kotatsu.data.model.ReadingMode
import org.koitharu.kotatsu.data.model.ZoomMode
import org.koitharu.kotatsu.data.repository.MangaRepository
import org.koitharu.kotatsu.ui.components.*
import org.koitharu.kotatsu.ui.theme.LocalNeonPalette

@Composable
fun ReaderSettingsScreen(
    repository: MangaRepository,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalNeonPalette.current
    val readerSettings by repository.readerSettings.collectAsState()

    CosmicBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .testTag("reader_settings_screen")
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header matching Image 1
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
                        text = "إعدادات القارئ",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    )
                    Text(
                        text = "خصص تجربة القراءة كما تحب",
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

            // Card 1: الوضع الافتراضي (Image 1)
            NeonCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("الوضع الافتراضي", fontWeight = FontWeight.Bold, color = palette.textPrimary)
                        Text("إختر طريقة عرض الصفحات لقراءتك اليومية", style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Icon(Icons.Default.MenuBook, contentDescription = null, tint = palette.primary)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Segmented buttons: أساسي, ويبتون, مستمر, صفحات مزدوجة
                NeonSegmentedSelector(
                    options = listOf("صفحات مزدوجة", "مستمر", "ويبتون", "أساسي"),
                    selectedIndex = when (readerSettings.readingMode) {
                        ReadingMode.PAGED_LTR -> 0
                        ReadingMode.CONTINUOUS -> 1
                        ReadingMode.WEBTOON -> 2
                        ReadingMode.PAGED_RTL -> 3
                    },
                    onSelect = { idx ->
                        val mode = when (idx) {
                            0 -> ReadingMode.PAGED_LTR
                            1 -> ReadingMode.CONTINUOUS
                            2 -> ReadingMode.WEBTOON
                            else -> ReadingMode.PAGED_RTL
                        }
                        repository.saveReaderSettings(readerSettings.copy(readingMode = mode))
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Card 2: وضع التكبير (Image 1)
            NeonCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("وضع التكبير", fontWeight = FontWeight.Bold, color = palette.textPrimary)
                        Text("إختر طريقة التكبير الافتراضية للصور", style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Icon(Icons.Default.ZoomIn, contentDescription = null, tint = palette.primary)
                }

                Spacer(modifier = Modifier.height(14.dp))

                NeonSegmentedSelector(
                    options = listOf("مناسب للمركز", "ذكي", "مناسب للعرض"),
                    selectedIndex = when (readerSettings.zoomMode) {
                        ZoomMode.FIT_CENTER -> 0
                        ZoomMode.SMART -> 1
                        ZoomMode.FIT_WIDTH -> 2
                    },
                    onSelect = { idx ->
                        val zm = when (idx) {
                            0 -> ZoomMode.FIT_CENTER
                            1 -> ZoomMode.SMART
                            else -> ZoomMode.FIT_WIDTH
                        }
                        repository.saveReaderSettings(readerSettings.copy(zoomMode = zm))
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Toggles inside zoom card
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = readerSettings.doubleTapZoom,
                        onCheckedChange = { repository.saveReaderSettings(readerSettings.copy(doubleTapZoom = it)) }
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        Text("التكبير بالضغط المزدوج", fontWeight = FontWeight.SemiBold, color = palette.textPrimary)
                        Text("السماح بالتكبير عند الضغط مرتين على الصفحة", style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = readerSettings.webtoonZoom,
                        onCheckedChange = { repository.saveReaderSettings(readerSettings.copy(webtoonZoom = it)) }
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        Text("تكبير وضع الويبتون", fontWeight = FontWeight.SemiBold, color = palette.textPrimary)
                        Text("تحسين التكبير للصور الطويلة في وضع الويبتون", style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Webtoon zoom level slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${(readerSettings.webtoonZoomLevel * 100).toInt()}%", color = palette.primary, fontWeight = FontWeight.Bold)
                    Text("مستوى تكبير الويبتون", color = palette.textPrimary, fontWeight = FontWeight.SemiBold)
                }

                Slider(
                    value = readerSettings.webtoonZoomLevel,
                    onValueChange = { repository.saveReaderSettings(readerSettings.copy(webtoonZoomLevel = it)) },
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(thumbColor = palette.primary, activeTrackColor = palette.primary)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = readerSettings.showGapsInWebtoon,
                        onCheckedChange = { repository.saveReaderSettings(readerSettings.copy(showGapsInWebtoon = it)) }
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        Text("إظهار الفجوات في وضع الويبتون", fontWeight = FontWeight.SemiBold, color = palette.textPrimary)
                        Text("إظهار المساحات الفارغة بين الصفحات في وضع الويبتون", style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Card 3: الترجمة الفورية للعربية (SSS X AI OCR Translator)
            NeonCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("الترجمة الفورية المدمجة للعربية", fontWeight = FontWeight.Bold, color = palette.primary)
                        Text("ترجمة تلقائية ذكية لفقاعات الحوار أثناء القراءة", style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Icon(Icons.Default.Translate, contentDescription = null, tint = palette.primary)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = readerSettings.isTranslationEnabled,
                        onCheckedChange = { repository.saveReaderSettings(readerSettings.copy(isTranslationEnabled = it)) }
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        Text("تفعيل الترجمة التلقائية", fontWeight = FontWeight.SemiBold, color = palette.textPrimary)
                        Text("استخراج وترجمة النصوص تلقائياً إلى العربية", style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("لغة الحوار الأصلية للترجمة:", fontWeight = FontWeight.SemiBold, color = palette.textPrimary, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)

                Spacer(modifier = Modifier.height(8.dp))

                NeonSegmentedSelector(
                    options = listOf("إنجليزية", "صينية", "كورية", "يابانية"),
                    selectedIndex = when (readerSettings.translationSourceLang) {
                        "en" -> 0
                        "zh" -> 1
                        "ko" -> 2
                        else -> 3
                    },
                    onSelect = { idx ->
                        val lang = when (idx) {
                            0 -> "en"
                            1 -> "zh"
                            2 -> "ko"
                            else -> "ja"
                        }
                        repository.saveReaderSettings(readerSettings.copy(translationSourceLang = lang))
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = readerSettings.showSpeechBubbleOverlay,
                        onCheckedChange = { repository.saveReaderSettings(readerSettings.copy(showSpeechBubbleOverlay = it)) }
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        Text("إظهار فقاعات الترجمة فوق الصفحة", fontWeight = FontWeight.SemiBold, color = palette.textPrimary)
                        Text("عرض النص العربي مباشرة داخل فقاعات الحوار", style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Card 4: أدوات التحكم أثناء القراءة (Image 1)
            NeonCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("أدوات التحكم أثناء القراءة", fontWeight = FontWeight.Bold, color = palette.textPrimary)
                        Text("خصص الأزرار والإيماءات لتجربة مريحة", style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Icon(Icons.Default.SportsEsports, contentDescription = null, tint = palette.primary)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = readerSettings.gestureNavigation,
                        onCheckedChange = { repository.saveReaderSettings(readerSettings.copy(gestureNavigation = it)) }
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        Text("التحكم بالإيماءات في التبديل", fontWeight = FontWeight.SemiBold, color = palette.textPrimary)
                        Text("استخدم إيماءات السحب للتنقل بين الفصول", style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = readerSettings.volumeKeyNavigation,
                        onCheckedChange = { repository.saveReaderSettings(readerSettings.copy(volumeKeyNavigation = it)) }
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        Text("أزرار الصوت للتنقل بين الصفحات", fontWeight = FontWeight.SemiBold, color = palette.textPrimary)
                        Text("استخدم أزرار رفع وخفض الصوت لتغيير الصفحة", style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // الاتجاه الافتراضي
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("الاتجاه الافتراضي", fontWeight = FontWeight.SemiBold, color = palette.textPrimary)
                        Text("اختر اتجاه السحب للتنقل بين الفصول", style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                NeonSegmentedSelector(
                    options = listOf("من اليسار لليمين", "من اليمين لليسار"),
                    selectedIndex = if (readerSettings.isRtl) 1 else 0,
                    onSelect = { idx ->
                        repository.saveReaderSettings(readerSettings.copy(isRtl = idx == 1))
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Card 5: مظهر الصفحة والشاشة (Image 1)
            NeonCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("مظهر الصفحة والشاشة", fontWeight = FontWeight.Bold, color = palette.textPrimary)
                        Text("تخصيص مظهر القراءة والعرض", style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Icon(Icons.Default.Tv, contentDescription = null, tint = palette.primary)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = readerSettings.full32BitColors,
                        onCheckedChange = { repository.saveReaderSettings(readerSettings.copy(full32BitColors = it)) }
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        Text("وضع الألوان 32-bit", fontWeight = FontWeight.SemiBold, color = palette.textPrimary)
                        Text("يعرض ملايين الألوان للأداء الأفضل", style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = readerSettings.fullScreenMode,
                        onCheckedChange = { repository.saveReaderSettings(readerSettings.copy(fullScreenMode = it)) }
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        Text("وضع ملء الشاشة", fontWeight = FontWeight.SemiBold, color = palette.textPrimary)
                        Text("إخفاء شريط الحالة وأشرطة التنقل", style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = readerSettings.keepScreenOn,
                        onCheckedChange = { repository.saveReaderSettings(readerSettings.copy(keepScreenOn = it)) }
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        Text("إبقاء الشاشة قيد التشغيل", fontWeight = FontWeight.SemiBold, color = palette.textPrimary)
                        Text("منع إيقاف الشاشة أثناء القراءة", style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = readerSettings.transparentInfoBar,
                        onCheckedChange = { repository.saveReaderSettings(readerSettings.copy(transparentInfoBar = it)) }
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        Text("شريط المعلومات الشفاف", fontWeight = FontWeight.SemiBold, color = palette.textPrimary)
                        Text("إظهار الوقت والصفحة في جزء علوي شفاف", style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = readerSettings.showChapterChangePopup,
                        onCheckedChange = { repository.saveReaderSettings(readerSettings.copy(showChapterChangePopup = it)) }
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        Text("إظهار نافذة تغيير الفصل", fontWeight = FontWeight.SemiBold, color = palette.textPrimary)
                        Text("عرض رسالة منبثقة بعنوان الفصل عند التبديل", style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
