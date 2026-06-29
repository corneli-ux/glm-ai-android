package com.glm.aiapp.ui

import androidx.compose.animation.*
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.glm.aiapp.ui.components.AppBottomBar
import com.glm.aiapp.ui.components.AppTopBar
import com.glm.aiapp.ui.navigation.Destination
import com.glm.aiapp.ui.navigation.bottomTabs
import com.glm.aiapp.ui.screens.build.BuildScreen
import com.glm.aiapp.ui.screens.chat.ChatScreen
import com.glm.aiapp.ui.screens.finetune.FineTuneScreen
import com.glm.aiapp.ui.screens.image.ImageScreen
import com.glm.aiapp.ui.screens.login.LoginScreen
import com.glm.aiapp.ui.screens.reader.ReaderScreen
import com.glm.aiapp.ui.screens.search.SearchScreen
import com.glm.aiapp.ui.screens.settings.SettingsScreen
import com.glm.aiapp.ui.screens.speech.SpeechScreen
import com.glm.aiapp.ui.screens.video.VideoScreen
import com.glm.aiapp.ui.screens.vision.VisionScreen
import com.glm.aiapp.ui.theme.GLMTheme
import com.glm.aiapp.ui.theme.SettingsViewModel

@Composable
fun AppRoot() {
    val settingsVm: SettingsViewModel = hiltViewModel()
    val settings by settingsVm.settings.collectAsStateWithLifecycle(initialValue = null)

    val useDark = when (settings?.themeMode) {
        null, com.glm.aiapp.domain.model.ThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
        com.glm.aiapp.domain.model.ThemeMode.LIGHT -> false
        com.glm.aiapp.domain.model.ThemeMode.DARK -> true
    }

    GLMTheme(useDark = useDark) {
        // Gate: show login screen until user is authenticated
        val sessionToken = settings?.sessionToken.orEmpty()
        if (sessionToken.isBlank()) {
            LoginScreen()
            return@GLMTheme
        }

        val navController = rememberNavController()
        val currentBackStack by navController.currentBackStackEntryAsState()
        val currentRoute = currentBackStack?.destination?.route
        val tab = bottomTabs.firstOrNull { it.destination.route == currentRoute }

        Scaffold(
            topBar = {
                if (tab != null) AppTopBar(title = tab.destination.title, showBack = false)
            },
            bottomBar = {
                if (tab != null) {
                    AppBottomBar(
                        currentRoute = currentRoute,
                        onSelect = { route ->
                            if (route != currentRoute) {
                                navController.navigate(route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        ) { innerPadding: PaddingValues ->
            NavHost(
                navController = navController,
                startDestination = Destination.Chat.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Destination.Chat.route) { ChatScreen() }
                composable(Destination.Build.route) { BuildScreen() }
                composable(Destination.Vision.route) { VisionScreen() }
                composable(Destination.Image.route) { ImageScreen() }
                composable(Destination.Video.route) { VideoScreen() }
                composable(Destination.Speech.route) { SpeechScreen() }
                composable(Destination.Search.route) { SearchScreen() }
                composable(Destination.Reader.route) { ReaderScreen() }
                composable(Destination.FineTune.route) { FineTuneScreen() }
                composable(Destination.Settings.route) { SettingsScreen() }
            }
        }
    }
}
