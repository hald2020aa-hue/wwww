package org.koitharu.kotatsu.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import org.koitharu.kotatsu.data.repository.MangaRepository
import org.koitharu.kotatsu.ui.components.CosmicBackground
import org.koitharu.kotatsu.ui.components.NeonSettingItem
import org.koitharu.kotatsu.ui.theme.LocalNeonPalette

@Composable
fun DownloadsSettingsScreen(
    repository: MangaRepository,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalNeonPalette.current

    var downloadOverCellular by remember { mutableStateOf(true) }
    var disableBatteryOptimization by remember { mutableStateOf(false) }
    var askDestinationEachTime by remember { mutableStateOf(false) }

    CosmicBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .testTag("downloads_settings_screen")
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header matching Image 6
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
                        text = "التحميلات",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    )
                    Text(
                        text = "إدارة كل ما يخص تنزيل المانجا على جهازك",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textSecondary
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = palette.secondary.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, palette.secondary.copy(alpha = 0.6f)),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = palette.secondary, modifier = Modifier.size(24.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Items from Image 6
            NeonSettingItem(
                title = "مجلدات المانجا المحلية",
                subtitle = "إدارة المجلدات التي تحتوي على مانجا في جهازك",
                icon = Icons.Default.Folder,
                iconColor = palette.primary,
                onClick = { /* local folders */ }
            )

            Spacer(modifier = Modifier.height(12.dp))

            NeonSettingItem(
                title = "مجلد التحميلات",
                subtitle = "/Internal storage/Downloads\nتحديد مكان حفظ التنزيلات على جهازك",
                icon = Icons.Default.FolderSpecial,
                iconColor = Color(0xFF00E5FF),
                onClick = { /* destination */ }
            )

            Spacer(modifier = Modifier.height(12.dp))

            NeonSettingItem(
                title = "تنسيق التحميل المفضل",
                subtitle = "اختيار صيغة حفظ الفصول بشكل افتراضي (CBZ)",
                icon = Icons.Default.Description,
                iconColor = palette.secondary,
                onClick = { /* format */ }
            )

            Spacer(modifier = Modifier.height(12.dp))

            NeonSettingItem(
                title = "التنزيل عبر شبكة الجوال",
                subtitle = "السماح بالتنزيل عند استخدام بيانات الجوال",
                icon = Icons.Default.SignalCellularAlt,
                iconColor = Color(0xFF2979FF),
                isChecked = downloadOverCellular,
                onCheckedChange = { downloadOverCellular = it }
            )

            Spacer(modifier = Modifier.height(12.dp))

            NeonSettingItem(
                title = "تعطيل \"استهلاك أقل للبطارية\"",
                subtitle = "قد يساعد في بدء التنزيل إذا كان لديك مشاكل معه",
                icon = Icons.Default.BatteryAlert,
                iconColor = Color(0xFFFF9100),
                isChecked = disableBatteryOptimization,
                onCheckedChange = { disableBatteryOptimization = it }
            )

            Spacer(modifier = Modifier.height(12.dp))

            NeonSettingItem(
                title = "حفظ الصفحات",
                subtitle = "مجلد حفظ الصفحة الافتراضية\nلم يتم تحديد قيمة",
                icon = Icons.Default.Layers,
                iconColor = Color(0xFF00E676),
                onClick = { /* save pages */ }
            )

            Spacer(modifier = Modifier.height(12.dp))

            NeonSettingItem(
                title = "اطلب وجهة المجلد في كل مرة",
                subtitle = "السؤال عن مكان الحفظ في كل عملية تنزيل",
                icon = Icons.Default.HelpOutline,
                iconColor = palette.primary,
                isChecked = askDestinationEachTime,
                onCheckedChange = { askDestinationEachTime = it }
            )

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
