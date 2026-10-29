package org.koitharu.kotatsu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import org.koitharu.kotatsu.data.repository.MangaRepository
import org.koitharu.kotatsu.ui.components.SSSXBottomNavigation
import org.koitharu.kotatsu.ui.screens.*
import org.koitharu.kotatsu.ui.theme.SSSXTheme

sealed interface SSSXScreen {
    object Feed : SSSXScreen
    object Explore : SSSXScreen
    object Favorites : SSSXScreen
    object History : SSSXScreen
    object Settings : SSSXScreen

    // Sub-screens
    object AppearanceSettings : SSSXScreen
    object NeonCustomization : SSSXScreen
    object ReaderSettingsSub : SSSXScreen
    object StorageSettings : SSSXScreen
    object DownloadsSettings : SSSXScreen
    object BackupSettings : SSSXScreen

    data class Details(val mangaId: String) : SSSXScreen
    data class Reader(val mangaId: String, val chapterId: String, val page: Int) : SSSXScreen
}

class MainActivity : ComponentActivity() {
    private lateinit var repository: MangaRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        repository = MangaRepository(applicationContext)

        setContent {
            val activeNeonPalette by repository.activeNeonTheme.collectAsState()

            SSSXTheme(neonPalette = activeNeonPalette) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    var currentTab by remember { mutableStateOf("favorites") }
                    var currentScreen by remember { mutableStateOf<SSSXScreen>(SSSXScreen.Favorites) }

                    // Stack for sub-screen navigation
                    val screenBackStack = remember { mutableStateListOf<SSSXScreen>() }

                    fun navigateTo(screen: SSSXScreen) {
                        screenBackStack.add(currentScreen)
                        currentScreen = screen
                    }

                    fun navigateBack() {
                        if (screenBackStack.isNotEmpty()) {
                            currentScreen = screenBackStack.removeLast()
                        } else if (currentTab != "favorites") {
                            currentTab = "favorites"
                            currentScreen = SSSXScreen.Favorites
                        } else {
                            finish()
                        }
                    }

                    BackHandler(enabled = screenBackStack.isNotEmpty() || currentTab != "favorites") {
                        navigateBack()
                    }

                    fun selectTab(tabId: String) {
                        currentTab = tabId
                        screenBackStack.clear()
                        currentScreen = when (tabId) {
                            "feed" -> SSSXScreen.Feed
                            "explore" -> SSSXScreen.Explore
                            "favorites" -> SSSXScreen.Favorites
                            "history" -> SSSXScreen.History
                            "settings" -> SSSXScreen.Settings
                            else -> SSSXScreen.Favorites
                        }
                    }

                    val isSubScreen = currentScreen !in listOf(
                        SSSXScreen.Feed,
                        SSSXScreen.Explore,
                        SSSXScreen.Favorites,
                        SSSXScreen.History,
                        SSSXScreen.Settings
                    )

                    Scaffold(
                        bottomBar = {
                            if (!isSubScreen) {
                                SSSXBottomNavigation(
                                    selectedTab = currentTab,
                                    onSelectTab = { selectTab(it) }
                                )
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    ) { innerPadding ->
                        val screenModifier = if (isSubScreen) Modifier else Modifier.padding(innerPadding)

                        when (val screen = currentScreen) {
                            is SSSXScreen.Feed -> {
                                FeedScreen(
                                    repository = repository,
                                    onMangaClick = { mangaId -> navigateTo(SSSXScreen.Details(mangaId)) },
                                    modifier = screenModifier
                                )
                            }
                            is SSSXScreen.Explore -> {
                                ExploreScreen(
                                    repository = repository,
                                    onMangaClick = { mangaId -> navigateTo(SSSXScreen.Details(mangaId)) },
                                    onOpenDownloads = { navigateTo(SSSXScreen.DownloadsSettings) },
                                    modifier = screenModifier
                                )
                            }
                            is SSSXScreen.Favorites -> {
                                FavoritesScreen(
                                    repository = repository,
                                    onMangaClick = { mangaId -> navigateTo(SSSXScreen.Details(mangaId)) },
                                    onExploreClick = { selectTab("explore") },
                                    modifier = screenModifier
                                )
                            }
                            is SSSXScreen.History -> {
                                HistoryScreen(
                                    repository = repository,
                                    onMangaClick = { mangaId -> navigateTo(SSSXScreen.Details(mangaId)) },
                                    onResumeClick = { mangaId, chId, page ->
                                        navigateTo(SSSXScreen.Reader(mangaId, chId, page))
                                    },
                                    onExploreClick = { selectTab("explore") },
                                    onOpenSettings = { selectTab("settings") },
                                    modifier = screenModifier
                                )
                            }
                            is SSSXScreen.Settings -> {
                                SettingsScreen(
                                    repository = repository,
                                    onNavigateTo = { route ->
                                        when (route) {
                                            "appearance" -> navigateTo(SSSXScreen.AppearanceSettings)
                                            "neon_themes" -> navigateTo(SSSXScreen.NeonCustomization)
                                            "reader_settings" -> navigateTo(SSSXScreen.ReaderSettingsSub)
                                            "storage" -> navigateTo(SSSXScreen.StorageSettings)
                                            "downloads" -> navigateTo(SSSXScreen.DownloadsSettings)
                                            "backup" -> navigateTo(SSSXScreen.BackupSettings)
                                            "feed" -> selectTab("feed")
                                            "explore" -> selectTab("explore")
                                        }
                                    },
                                    onBackClick = { selectTab("favorites") },
                                    modifier = screenModifier
                                )
                            }
                            is SSSXScreen.AppearanceSettings -> {
                                AppearanceSettingsScreen(
                                    repository = repository,
                                    onBackClick = { navigateBack() },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            is SSSXScreen.NeonCustomization -> {
                                NeonCustomizationScreen(
                                    repository = repository,
                                    onBackClick = { navigateBack() },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            is SSSXScreen.ReaderSettingsSub -> {
                                ReaderSettingsScreen(
                                    repository = repository,
                                    onBackClick = { navigateBack() },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            is SSSXScreen.StorageSettings -> {
                                StorageSettingsScreen(
                                    repository = repository,
                                    onBackClick = { navigateBack() },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            is SSSXScreen.DownloadsSettings -> {
                                DownloadsSettingsScreen(
                                    repository = repository,
                                    onBackClick = { navigateBack() },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            is SSSXScreen.BackupSettings -> {
                                BackupSettingsScreen(
                                    repository = repository,
                                    onBackClick = { navigateBack() },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            is SSSXScreen.Details -> {
                                MangaDetailsScreen(
                                    mangaId = screen.mangaId,
                                    repository = repository,
                                    onBackClick = { navigateBack() },
                                    onReadChapterClick = { mId, chId, page ->
                                        navigateTo(SSSXScreen.Reader(mId, chId, page))
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            is SSSXScreen.Reader -> {
                                ReaderScreen(
                                    mangaId = screen.mangaId,
                                    chapterId = screen.chapterId,
                                    initialPage = screen.page,
                                    repository = repository,
                                    onBackClick = { navigateBack() },
                                    onOpenReaderSettings = { navigateTo(SSSXScreen.ReaderSettingsSub) },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
