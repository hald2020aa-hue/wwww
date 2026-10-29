package org.koitharu.kotatsu.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag

sealed class Screen {
    object Library : Screen()
    object Explore : Screen()
    object History : Screen()
    object Saved : Screen()
    object Settings : Screen()
    data class Details(val mangaId: String) : Screen()
    data class Reader(val mangaId: String, val chapterId: String, val page: Int = 1) : Screen()
}

enum class NavTab(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    LIBRARY("Library", Icons.Filled.CollectionsBookmark, Icons.Outlined.CollectionsBookmark, "tab_library"),
    EXPLORE("Explore", Icons.Filled.Explore, Icons.Outlined.Explore, "tab_explore"),
    HISTORY("History", Icons.Filled.History, Icons.Outlined.History, "tab_history"),
    SAVED("Saved", Icons.Filled.Bookmark, Icons.Outlined.BookmarkBorder, "tab_saved"),
    SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings, "tab_settings")
}

@Composable
fun KotatsuNavigationBar(
    currentTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.testTag("bottom_nav_bar"),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = NavigationBarDefaults.Elevation
    ) {
        NavTab.values().forEach { tab ->
            val isSelected = currentTab == tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                        contentDescription = tab.label
                    )
                },
                label = { Text(tab.label) },
                alwaysShowLabel = true,
                modifier = Modifier.testTag(tab.testTag)
            )
        }
    }
}
