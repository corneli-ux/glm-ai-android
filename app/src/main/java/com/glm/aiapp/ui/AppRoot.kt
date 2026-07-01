package com.glm.aiapp.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.glm.aiapp.domain.model.Message
import com.glm.aiapp.domain.model.Role
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.glm.aiapp.domain.model.ThemeMode
import com.glm.aiapp.ui.navigation.Destination
import com.glm.aiapp.ui.navigation.bottomTabs
import com.glm.aiapp.ui.screens.build.BuildScreen
import com.glm.aiapp.ui.screens.chat.ChatScreen
import com.glm.aiapp.ui.screens.login.LoginScreen
import com.glm.aiapp.ui.screens.search.SearchScreen
import com.glm.aiapp.ui.screens.settings.SettingsScreen
import com.glm.aiapp.ui.screens.vision.VisionScreen
import com.glm.aiapp.ui.theme.GLMTheme
import com.glm.aiapp.ui.theme.SettingsViewModel
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.coroutines.launch

@Composable
fun AppRoot() {
    val settingsVm: SettingsViewModel = hiltViewModel()

    GLMTheme(useDark = true) {
        val sessionToken = settingsVm.settings.collectAsStateWithLifecycle(initialValue = null).value?.sessionToken.orEmpty()
        if (sessionToken.isBlank()) {
            com.glm.aiapp.ui.screens.login.LoginScreen()
            return@GLMTheme
        }

        val navController = rememberNavController()
        val currentBackStack by navController.currentBackStackEntryAsState()
        val currentRoute = currentBackStack?.destination?.route
        val tabs = bottomTabs
        val tab = tabs.firstOrNull { it.destination.route == currentRoute }

        Scaffold(
            containerColor = Color.Black,
            contentColor = Color.White,
            bottomBar = {
                if (tab != null) {
                    NavigationBar(
                        containerColor = Color(0xFF0A0A0A),
                        contentColor = Color.White
                    ) {
                        tabs.forEach { t ->
                            val selected = t.destination.route == currentRoute
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    if (t.destination.route != currentRoute) {
                                        navController.navigate(t.destination.route) {
                                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) t.selectedIcon else t.unselectedIcon,
                                        contentDescription = t.label,
                                        modifier = Modifier.size(22.dp),
                                        tint = if (selected) Color.White else Color(0xFF555555)
                                    )
                                },
                                label = { Text(t.label, fontSize = 10.sp, color = if (selected) Color.White else Color(0xFF555555)) },
                                colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
                            )
                        }
                    }
                }
            }
        ) { padding ->
            Box(Modifier.padding(padding).background(Color.Black)) {
                NavHost(
                    navController = navController,
                    startDestination = Destination.Chat.route
                ) {
                    composable(Destination.Chat.route) { ChatScreen() }
                    composable(Destination.Build.route) { BuildScreen() }
                    composable(Destination.Vision.route) { VisionScreen() }
                    composable(Destination.Search.route) { SearchScreen() }
                    composable(Destination.Settings.route) { SettingsScreen() }
                }
            }
        }
    }
}
