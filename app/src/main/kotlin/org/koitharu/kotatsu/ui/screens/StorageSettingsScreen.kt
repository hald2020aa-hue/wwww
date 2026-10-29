package org.koitharu.kotatsu.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koitharu.kotatsu.data.repository.MangaRepository
import org.koitharu.kotatsu.ui.components.CosmicBackground
import org.koitharu.kotatsu.ui.components.NeonCard
import org.koitharu.kotatsu.ui.theme.LocalNeonPalette

@Composable
fun StorageSettingsScreen(
    repository: MangaRepository,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val palette = LocalNeonPalette.current

    var ignoreSsl by remember { mutableStateOf(false) }
    var disableConnectionCheck by remember { mutableStateOf(false) }
    var blockAds by remember { mutableStateOf(true) }

    CosmicBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .testTag("storage_settings_screen")
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header matching Image 9
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
                        text = "التخزين والشبكة",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    )
                    Text(
                        text = "Storage and network",
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
                        Icon(Icons.Default.Storage, contentDescription = null, tint = palette.primary, modifier = Modifier.size(24.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Card 1: استهلاك التخزين (Image 9)
            NeonCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text("36,9 GB متوفر", fontWeight = FontWeight.Bold, color = palette.textPrimary)
                        Text("متاح من 37,0 GB", style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text("استهلاك التخزين", fontWeight = FontWeight.Bold, color = palette.textPrimary)
                            Text("Storage usage", style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                        }
                        Icon(Icons.Default.Storage, contentDescription = null, tint = Color(0xFF00E5FF))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Green Glowing Progress Bar
                LinearProgressIndicator(
                    progress = { 0.08f },
                    color = Color(0xFF00E676),
                    trackColor = palette.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Breakdown tags row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StorageBadge("المانجا المحفوظة", "0 B", Color(0xFF2979FF))
                    StorageBadge("بيانات الصفحات", "0 B", Color(0xFFFF5252))
                    StorageBadge("بيانات أخرى", "5,9 MB", Color(0xFF00E676))
                    StorageBadge("متوفر", "36,9 GB", palette.textSecondary)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Card 2: إدارة البيانات (Image 9)
            NeonCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("إدارة البيانات", fontWeight = FontWeight.Bold, color = palette.textPrimary)
                        Text("Data removal", style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = palette.secondary)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Sub item 1
                NeonSettingRow(
                    title = "إعادة تحميل المحتوى",
                    subtitle = "أبداً",
                    icon = Icons.Default.Refresh,
                    onClick = { Toast.makeText(context, "تمت جدولة إعادة تحميل المحتوى", Toast.LENGTH_SHORT).show() }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Sub item 2
                NeonSettingRow(
                    title = "إعادة تحميل الصفحات",
                    subtitle = "فقط على Wi-Fi",
                    icon = Icons.Default.Wifi,
                    onClick = { Toast.makeText(context, "تم ضبط الإعداد: فقط على Wi-Fi", Toast.LENGTH_SHORT).show() }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Card 3: الشبكة والاتصال (Image 9)
            NeonCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("الشبكة والاتصال", fontWeight = FontWeight.Bold, color = palette.textPrimary)
                        Text("Network and connection", style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Icon(Icons.Default.SettingsInputAntenna, contentDescription = null, tint = palette.primary)
                }

                Spacer(modifier = Modifier.height(14.dp))

                NeonSettingRow(
                    title = "الوكيل",
                    subtitle = "معطل",
                    icon = Icons.Default.Router,
                    onClick = { Toast.makeText(context, "إعدادات الوكيل", Toast.LENGTH_SHORT).show() }
                )

                Spacer(modifier = Modifier.height(10.dp))

                NeonSettingRow(
                    title = "DNS مع HTTPS",
                    subtitle = "معطل",
                    icon = Icons.Default.Security,
                    onClick = { Toast.makeText(context, "إعدادات DNS over HTTPS", Toast.LENGTH_SHORT).show() }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Card 4: إعدادات متقدمة (Image 9)
            NeonCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("إعدادات متقدمة", fontWeight = FontWeight.Bold, color = palette.textPrimary)
                        Text("Advanced settings", style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Icon(Icons.Default.Tune, contentDescription = null, tint = palette.primary)
                }

                Spacer(modifier = Modifier.height(14.dp))

                NeonSettingRow(
                    title = "وكيل تحسين الصور",
                    subtitle = "لا شيء",
                    icon = Icons.Default.Image,
                    onClick = { Toast.makeText(context, "تمكين وكيل تحسين الصور", Toast.LENGTH_SHORT).show() }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(checked = ignoreSsl, onCheckedChange = { ignoreSsl = it })
                    Column(horizontalAlignment = Alignment.End) {
                        Text("تجاهل أخطاء SSL", fontWeight = FontWeight.SemiBold, color = palette.textPrimary)
                        Text("يمكنك تعطيل التحقق من شهادات SSL في حالة مواجهة مشاكل", style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(checked = disableConnectionCheck, onCheckedChange = { disableConnectionCheck = it })
                    Column(horizontalAlignment = Alignment.End) {
                        Text("تعطيل التحقق من الاتصال", fontWeight = FontWeight.SemiBold, color = palette.textPrimary)
                        Text("تخطي التحقق من الاتصال لتفادي مشاكل الاتصال المتقطع", style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(checked = blockAds, onCheckedChange = { blockAds = it })
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Block ads in browser", fontWeight = FontWeight.SemiBold, color = palette.textPrimary)
                        Text("Block advertisement in the built-in browser (beta)", style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun StorageBadge(title: String, value: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        modifier = Modifier.padding(horizontal = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = color)
            Text(title, style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = Color.White)
        }
    }
}

@Composable
fun NeonSettingRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    val palette = LocalNeonPalette.current
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = palette.surface,
        border = BorderStroke(1.dp, palette.border.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = palette.textSecondary, modifier = Modifier.size(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(title, fontWeight = FontWeight.SemiBold, color = palette.textPrimary)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                }
                Icon(icon, contentDescription = null, tint = palette.primary, modifier = Modifier.size(20.dp))
            }
        }
    }
}
