package org.koitharu.kotatsu.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koitharu.kotatsu.data.repository.MangaRepository
import org.koitharu.kotatsu.ui.components.CosmicBackground
import org.koitharu.kotatsu.ui.components.NeonCard
import org.koitharu.kotatsu.ui.components.NeonSettingItem
import org.koitharu.kotatsu.ui.theme.LocalNeonPalette

@Composable
fun BackupSettingsScreen(
    repository: MangaRepository,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val palette = LocalNeonPalette.current

    CosmicBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .testTag("backup_settings_screen")
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header matching Image 7
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

                Text(
                    text = "النسخ الاحتياطي و الاستعادة",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary
                )

                Surface(
                    shape = CircleShape,
                    color = palette.primary.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, palette.primary.copy(alpha = 0.6f)),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, tint = palette.primary, modifier = Modifier.size(24.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Items from Image 7
            NeonSettingItem(
                title = "إنشاء نسخة احتياطية",
                subtitle = "يمكنك إنشاء نسخة احتياطية من السجل الخاص بك والمفضلة واستعادتها لاحقاً.",
                icon = Icons.Default.CloudUpload,
                iconColor = palette.primary,
                onClick = {
                    Toast.makeText(context, "تم حفظ النسخة الاحتياطية بنجاح في ملفات SSS X", Toast.LENGTH_SHORT).show()
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            NeonSettingItem(
                title = "استعادة من نسخة احتياطية",
                subtitle = "استعادة النسخة الاحتياطية التي تم إنشاؤها مسبقاً إلى هذا الجهاز.",
                icon = Icons.Default.CloudDownload,
                iconColor = Color(0xFF00E5FF),
                onClick = {
                    Toast.makeText(context, "تم استيراد واستعادة البيانات بالكامل", Toast.LENGTH_SHORT).show()
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            NeonSettingItem(
                title = "النسخ الاحتياطي الدوري",
                subtitle = "تفعيل النسخ الاحتياطي التلقائي لحماية بياناتك بشكل مستمر.",
                icon = Icons.Default.Update,
                iconColor = palette.secondary,
                onClick = {
                    Toast.makeText(context, "تم تفعيل الجدولة الدورية للنسخ الاحتياطي", Toast.LENGTH_SHORT).show()
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Info Card from Image 7
            NeonCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("معلومة", fontWeight = FontWeight.Bold, color = palette.textPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.Info, contentDescription = null, tint = palette.primary)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "تشمل النسخة الاحتياطية السجل، المفضلة، الإعدادات وجميع بياناتك الهامة في التطبيق.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.textSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                )
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
