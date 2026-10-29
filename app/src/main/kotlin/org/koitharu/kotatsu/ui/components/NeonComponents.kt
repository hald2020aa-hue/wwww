package org.koitharu.kotatsu.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koitharu.kotatsu.data.model.NeonColorPalette
import org.koitharu.kotatsu.ui.theme.LocalNeonPalette

@Composable
fun CosmicBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val palette = LocalNeonPalette.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        palette.background,
                        Color(0xFF040A1E),
                        palette.surface,
                        Color(0xFF02040C)
                    )
                )
            )
            .drawBehind {
                // Subtle stardust ambient light aura
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            palette.glow.copy(alpha = 0.15f),
                            Color.Transparent
                        ),
                        radius = size.width * 0.7f
                    ),
                    center = androidx.compose.ui.geometry.Offset(size.width * 0.5f, size.height * 0.25f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            palette.secondary.copy(alpha = 0.12f),
                            Color.Transparent
                        ),
                        radius = size.width * 0.6f
                    ),
                    center = androidx.compose.ui.geometry.Offset(size.width * 0.8f, size.height * 0.75f)
                )
            }
    ) {
        content()
    }
}

@Composable
fun NeonCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(20.dp),
    borderColor: Color? = null,
    borderWidth: Dp = 1.2.dp,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val palette = LocalNeonPalette.current
    val effectiveBorder = borderColor ?: palette.border.copy(alpha = 0.45f)

    val cardModifier = modifier
        .fillMaxWidth()
        .clip(shape)
        .border(
            BorderStroke(
                borderWidth,
                Brush.linearGradient(
                    listOf(
                        effectiveBorder,
                        effectiveBorder.copy(alpha = 0.25f),
                        effectiveBorder.copy(alpha = 0.65f)
                    )
                )
            ),
            shape = shape
        )
        .background(
            Brush.verticalGradient(
                listOf(
                    palette.surfaceCard,
                    palette.surface.copy(alpha = 0.95f)
                )
            )
        )
        .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
        .padding(contentPadding)

    Column(modifier = cardModifier) {
        content()
    }
}

@Composable
fun NeonSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "البحث في المانجا...",
    onMenuClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val palette = LocalNeonPalette.current

    Surface(
        shape = RoundedCornerShape(28.dp),
        color = palette.surfaceCard,
        border = BorderStroke(
            1.2.dp,
            Brush.horizontalGradient(
                listOf(
                    palette.border.copy(alpha = 0.8f),
                    palette.secondary.copy(alpha = 0.4f),
                    palette.border.copy(alpha = 0.8f)
                )
            )
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .testTag("neon_search_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onMenuClick != null) {
                IconButton(
                    onClick = onMenuClick,
                    modifier = Modifier.size(40.dp).testTag("search_menu_btn")
                ) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = palette.textPrimary
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (query.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyLarge,
                        color = palette.textSecondary,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    textStyle = TextStyle(
                        color = palette.textPrimary,
                        fontSize = 16.sp,
                        textAlign = TextAlign.End
                    ),
                    cursorBrush = SolidColor(palette.primary),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Search action icon inside glowing circle
            Surface(
                shape = CircleShape,
                color = palette.primary.copy(alpha = 0.2f),
                border = BorderStroke(1.dp, palette.primary.copy(alpha = 0.6f)),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = palette.textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun NeonPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    val palette = LocalNeonPalette.current

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(26.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, palette.primary.copy(alpha = 0.8f)),
        modifier = modifier
            .height(50.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(26.dp),
                spotColor = palette.primary,
                ambientColor = palette.glow
            )
            .testTag("neon_pill_btn")
    ) {
        Box(
            modifier = Modifier
                .background(palette.primaryGradient)
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun NeonFilterPill(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    val palette = LocalNeonPalette.current

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) Color.Transparent else palette.surfaceCard,
        border = BorderStroke(
            1.dp,
            if (isSelected) palette.primary else palette.border.copy(alpha = 0.35f)
        ),
        modifier = modifier
            .height(42.dp)
            .testTag("filter_pill_$text")
    ) {
        Box(
            modifier = Modifier
                .then(
                    if (isSelected) Modifier.background(palette.primaryGradient)
                    else Modifier
                )
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (icon != null) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = if (isSelected) Color.White else palette.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color.White else palette.textSecondary
                )
            }
        }
    }
}

@Composable
fun NeonSettingItem(
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    iconColor: Color? = null,
    isChecked: Boolean? = null,
    onCheckedChange: ((Boolean) -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val palette = LocalNeonPalette.current
    val effectiveIconColor = iconColor ?: palette.primary

    NeonCard(
        modifier = modifier.testTag("setting_item_${title.take(10)}"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        onClick = if (isChecked != null && onCheckedChange != null) {
            { onCheckedChange(!isChecked) }
        } else onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Right Icon box (RTL layout)
            if (icon != null) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = effectiveIconColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, effectiveIconColor.copy(alpha = 0.5f)),
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = effectiveIconColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
            }

            // Title & Subtitle
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.textPrimary
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textSecondary,
                        lineHeight = 16.sp
                    )
                }
            }

            // Left Action / Switch / Chevron
            if (isChecked != null && onCheckedChange != null) {
                Switch(
                    checked = isChecked,
                    onCheckedChange = onCheckedChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = palette.primary,
                        uncheckedThumbColor = palette.textSecondary,
                        uncheckedTrackColor = palette.surface
                    ),
                    modifier = Modifier.testTag("switch_${title.take(10)}")
                )
            } else if (onClick != null) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack, // In RTL, back arrow points left (forward direction)
                    contentDescription = null,
                    tint = palette.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun NeonSegmentedSelector(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalNeonPalette.current

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = palette.surface,
        border = BorderStroke(1.dp, palette.border.copy(alpha = 0.35f)),
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            options.forEachIndexed { index, title ->
                val isSelected = selectedIndex == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .then(
                            if (isSelected) Modifier.background(palette.primaryGradient)
                            else Modifier.background(Color.Transparent)
                        )
                        .clickable { onSelect(index) }
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else palette.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun SSSXBottomNavigation(
    selectedTab: String, // "feed", "explore", "favorites", "history", "settings"
    onSelectTab: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalNeonPalette.current

    Surface(
        color = palette.surface.copy(alpha = 0.95f),
        border = BorderStroke(1.dp, palette.border.copy(alpha = 0.25f)),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("sssx_bottom_nav")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 8.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tabs = listOf(
                Triple("feed", "الموجز", Icons.Default.RssFeed),
                Triple("explore", "استكشف", Icons.Default.Explore),
                Triple("favorites", "المفضلة", Icons.Default.Favorite),
                Triple("history", "السجل", Icons.Default.AccessTime),
                Triple("settings", "إعدادات", Icons.Default.Settings)
            )

            tabs.forEach { (id, label, icon) ->
                val isSelected = selectedTab == id

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onSelectTab(id) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("nav_tab_$id")
                ) {
                    if (isSelected) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.Transparent,
                            border = BorderStroke(1.dp, palette.primary.copy(alpha = 0.8f)),
                            modifier = Modifier
                                .height(38.dp)
                                .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = palette.primary)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(palette.primaryGradient)
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        icon,
                                        contentDescription = label,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                        // Small glowing dot below active tab
                        Spacer(modifier = Modifier.height(3.dp))
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .background(palette.tertiary, CircleShape)
                        )
                    } else {
                        Icon(
                            icon,
                            contentDescription = label,
                            tint = palette.textSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.textSecondary
                        )
                    }
                }
            }
        }
    }
}
