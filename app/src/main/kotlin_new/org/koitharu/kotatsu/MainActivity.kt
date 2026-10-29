package org.koitharu.kotatsu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import org.koitharu.kotatsu.data.repository.MangaRepository
import org.koitharu.kotatsu.ui.components.KotatsuNavigationBar
import org.koitharu.kotatsu.ui.components.NavTab
import org.koitharu.kotatsu.ui.components.Screen
import org.koitharu.kotatsu.ui.screens.*
import org.koitharu.kotatsu.ui.theme.KotatsuTheme

class MainActivity : ComponentActivity() {
    private lateinit var repository: MangaRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        repository = MangaRepository(applicationContext)

        setContent {
            KotatsuTheme {
                var currentTab by remember { mutableStateOf(NavTab.LIBRARY) }
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Library) }

                // Stack for sub-screen navigation
                val screenBackStack = remember { mutableStateListOf<Screen>() }

                fun navigateTo(screen: Screen) {
                    screenBackStack.add(currentScreen)
                    currentScreen = screen
                }

                fun navigateBack() {
                    if (screenBackStack.isNotEmpty()) {
                        currentScreen = screenBackStack.removeLast()
                    } else if (currentTab != NavTab.LIBRARY) {
                        currentTab = NavTab.LIBRARY
                        currentScreen = Screen.Library
                    } else {
                        finish()
                    }
                }

                BackHandler(enabled = screenBackStack.isNotEmpty() || currentTab != NavTab.LIBRARY) {
                    navigateBack()
                }

                val isReading = currentScreen is Screen.Reader
                val isDetails = currentScreen is Screen.Details

                Scaffold(
                    bottomBar = {
                        if (!isReading && !isDetails) {
                            KotatsuNavigationBar(
                                currentTab = currentTab,
                                onTabSelected = { tab ->
                                    currentTab = tab
                                    screenBackStack.clear()
                                    currentScreen = when (tab) {
                                        NavTab.LIBRARY -> Screen.Library
                                        NavTab.EXPLORE -> Screen.Explore
                                        NavTab.HISTORY -> Screen.History
                                        NavTab.SAVED -> Screen.Saved
                                        NavTab.SETTINGS -> Screen.Settings
                                    }
                                }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    when (val screen = currentScreen) {
                        is Screen.Library -> {
                            LibraryScreen(
                                repository = repository,
                                onMangaClick = { mangaId -> navigateTo(Screen.Details(mangaId)) },
                                onExploreClick = {
                                    currentTab = NavTab.EXPLORE
                                    currentScreen = Screen.Explore
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        is Screen.Explore -> {
                            ExploreScreen(
                                repository = repository,
                                onMangaClick = { mangaId -> navigateTo(Screen.Details(mangaId)) },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        is Screen.History -> {
                            HistoryScreen(
                                repository = repository,
                                onMangaClick = { mangaId -> navigateTo(Screen.Details(mangaId)) },
                                onResumeClick = { mangaId, chId, page ->
                                    navigateTo(Screen.Reader(mangaId, chId, page))
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        is Screen.Saved -> {
                            SavedScreen(
                                repository = repository,
                                onMangaClick = { mangaId -> navigateTo(Screen.Details(mangaId)) },
                                onReadPageClick = { mangaId, chId, page ->
                                    navigateTo(Screen.Reader(mangaId, chId, page))
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        is Screen.Settings -> {
                            SettingsScreen(
                                repository = repository,
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        is Screen.Details -> {
                            MangaDetailsScreen(
                                mangaId = screen.mangaId,
                                repository = repository,
                                onBackClick = { navigateBack() },
                                onReadChapterClick = { mangaId, chapterId, page ->
                                    navigateTo(Screen.Reader(mangaId, chapterId, page))
                                }
                            )
                        }
                        is Screen.Reader -> {
                            ReaderScreen(
                                mangaId = screen.mangaId,
                                chapterId = screen.chapterId,
                                initialPage = screen.page,
                                repository = repository,
                                onBackClick = { navigateBack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
