package org.koitharu.kotatsu.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import org.koitharu.kotatsu.data.repository.MangaRepository
import org.koitharu.kotatsu.ui.components.CosmicBackground
import org.koitharu.kotatsu.ui.components.NeonSettingItem
import org.koitharu.kotatsu.ui.theme.LocalNeonPalette

@Composable
fun SettingsScreen(
    repository: MangaRepository,
    onNavigateTo: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalNeonPalette.current

    CosmicBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .testTag("root_settings_screen")
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header matching Image 10 exactly
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back button on the left (RTL)
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = palette.textPrimary
                    )
                }

                // Title + Subtitle
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "الإعدادات",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    )
                    Text(
                        text = "خصص تجربتك كما تحب",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textSecondary
                    )
                }

                // Gear Icon in glowing frame
                Surface(
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = palette.primary.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, palette.primary.copy(alpha = 0.6f)),
                    modifier = Modifier.size(46.dp)
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

            Spacer(modifier = Modifier.height(24.dp))

            // Setting items matching Image 10
            NeonSettingItem(
                title = "مظهر",
                subtitle = "مظهر التطبيق، وضع القائمة، اللغة",
                icon = Icons.Default.Palette,
                iconColor = palette.primary,
                onClick = { onNavigateTo("appearance") }
            )

            Spacer(modifier = Modifier.height(12.dp))

            NeonSettingItem(
                title = "ألوان النيون (12 ثيم)",
                subtitle = "اختر من بين 12 نمط نيون متوهج أو خصص ألوانك",
                icon = Icons.Default.AutoAwesome,
                iconColor = palette.secondary,
                onClick = { onNavigateTo("neon_themes") }
            )

            Spacer(modifier = Modifier.height(12.dp))

            NeonSettingItem(
                title = "مصادر المانجا",
                subtitle = "تمكين 444 من 882 مصدر",
                icon = Icons.Default.MenuBook,
                iconColor = Color(0xFF00E5FF),
                onClick = { onNavigateTo("explore") }
            )

            Spacer(modifier = Modifier.height(12.dp))

            NeonSettingItem(
                title = "إعدادات القارئ",
                subtitle = "وضع القراءة، وضع القياس، والترجمة الفورية للعربية",
                icon = Icons.Default.AutoStories,
                iconColor = palette.primary,
                onClick = { onNavigateTo("reader_settings") }
            )

            Spacer(modifier = Modifier.height(12.dp))

            NeonSettingItem(
                title = "Storage and network",
                subtitle = "استهلاك التخزين، الوكيل، اعادة تحميل المحتوى",
                icon = Icons.Default.Storage,
                iconColor = Color(0xFF2979FF),
                onClick = { onNavigateTo("storage") }
            )

            Spacer(modifier = Modifier.height(12.dp))

            NeonSettingItem(
                title = "التحميلات",
                subtitle = "مجلد التحميلات، التحميل عبر شبكة الواي فاي فقط",
                icon = Icons.Default.Download,
                iconColor = palette.secondary,
                onClick = { onNavigateTo("downloads") }
            )

            Spacer(modifier = Modifier.height(12.dp))

            NeonSettingItem(
                title = "التحقق من الفصول الجديدة",
                subtitle = "البحث عن تحديثات، إعدادات الإشعارات",
                icon = Icons.Default.Notifications,
                iconColor = Color(0xFFFFAB00),
                onClick = { onNavigateTo("feed") }
            )

            Spacer(modifier = Modifier.height(12.dp))

            NeonSettingItem(
                title = "خدمات",
                subtitle = "الاقتراحات، التزامن، تتبع",
                icon = Icons.Default.Extension,
                iconColor = Color(0xFF00E676),
                onClick = { /* services */ }
            )

            Spacer(modifier = Modifier.height(12.dp))

            NeonSettingItem(
                title = "النسخ الاحتياطي و الاستعادة",
                subtitle = "النسخ الاحتياطي الدوري",
                icon = Icons.Default.CloudUpload,
                iconColor = Color(0xFF00B0FF),
                onClick = { onNavigateTo("backup") }
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Footer branding for SSS X
            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 60.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "SSS X Manga Reader",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = palette.primary
                )
                Text(
                    text = "الإصدار 10.0 الاحترافي • بنية نيون متقدمة",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textSecondary
                )
            }
        }
    }
}
