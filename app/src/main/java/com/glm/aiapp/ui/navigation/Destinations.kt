package com.glm.aiapp.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Destination(val route: String, val title: String) {
    data object Chat : Destination("chat", "Chat")
    data object Vision : Destination("vision", "Vision")
    data object Image : Destination("image", "Image")
    data object Video : Destination("video", "Video")
    data object Speech : Destination("speech", "Speech")
    data object Search : Destination("search", "Search")
    data object Reader : Destination("reader", "Reader")
    data object FineTune : Destination("finetune", "Fine-tune")
    data object Settings : Destination("settings", "Settings")
}

data class BottomTab(
    val destination: Destination,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val label: String
)

val bottomTabs: List<BottomTab> = listOf(
    BottomTab(Destination.Chat, Icons.Filled.Chat, Icons.Outlined.Chat, "Chat"),
    BottomTab(Destination.Vision, Icons.Filled.Image, Icons.Outlined.Image, "Vision"),
    BottomTab(Destination.Image, Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome, "Create"),
    BottomTab(Destination.Search, Icons.Filled.Search, Icons.Outlined.Search, "Tools"),
    BottomTab(Destination.Settings, Icons.Filled.Settings, Icons.Outlined.Settings, "Settings")
)

/** Tools tab is a hub — it routes to Search / Reader / Video / Speech / Fine-tune. */
val toolsDestinations = listOf(
    Destination.Search,
    Destination.Reader,
    Destination.Video,
    Destination.Speech,
    Destination.FineTune
)
