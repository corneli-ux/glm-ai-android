package com.glm.aiapp.ui.screens.video

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.glm.aiapp.domain.model.GeneratedVideo
import com.glm.aiapp.domain.model.VideoQuality
import com.glm.aiapp.domain.model.VideoSize
import com.glm.aiapp.domain.model.VideoStatus
import com.glm.aiapp.ui.components.LoadingState

@Composable
fun VideoScreen(vm: VideoViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val videos by vm.videos.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = state.prompt,
            onValueChange = vm::setPrompt,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Describe the video…") },
            minLines = 2,
            maxLines = 5
        )

        // Quality
        Text("Quality", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            VideoQuality.entries.forEach { q ->
                FilterChip(
                    selected = state.quality == q,
                    onClick = { vm.setQuality(q) },
                    label = { Text(q.label) }
                )
            }
        }

        // Size
        Text("Size", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            VideoSize.entries.forEach { s ->
                FilterChip(
                    selected = state.size == s,
                    onClick = { vm.setSize(s) },
                    label = { Text(s.label) }
                )
            }
        }

        // FPS / duration
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("FPS:", style = MaterialTheme.typography.labelLarge)
            FilterChip(selected = state.fps == 30, onClick = { vm.setFps(30) }, label = { Text("30") })
            FilterChip(selected = state.fps == 60, onClick = { vm.setFps(60) }, label = { Text("60") })
            Spacer(Modifier.width(8.dp))
            Text("Duration:", style = MaterialTheme.typography.labelLarge)
            FilterChip(selected = state.duration == 5, onClick = { vm.setDuration(5) }, label = { Text("5s") })
            FilterChip(selected = state.duration == 10, onClick = { vm.setDuration(10) }, label = { Text("10s") })
        }

        Button(
            onClick = vm::create,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isCreating && state.prompt.isNotBlank()
        ) {
            Icon(Icons.Filled.VideoCall, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            Text(if (state.isCreating) "Submitting…" else "Create video")
        }

        state.error?.let { msg ->
            Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Text(msg, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.padding(12.dp))
            }
        }

        if (state.isCreating) LoadingState(message = "Submitting video job…")

        HorizontalDivider()
        Text("Your videos", style = MaterialTheme.typography.titleMedium)
        if (videos.isEmpty()) {
            Text("No videos yet. Submit a prompt above to begin.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            LazyVerticalGrid(
                columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(1),
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(videos, key = { it.id }) { v -> VideoCard(v, onDelete = { vm.delete(v.id) }) }
            }
        }
    }
}

@Composable
private fun VideoCard(video: GeneratedVideo, onDelete: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.PlayCircle,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = when (video.status) {
                    VideoStatus.PROCESSING -> MaterialTheme.colorScheme.secondary
                    VideoStatus.SUCCESS -> MaterialTheme.colorScheme.primary
                    VideoStatus.FAIL -> MaterialTheme.colorScheme.error
                }
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(video.prompt, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
                Spacer(Modifier.height(4.dp))
                when (video.status) {
                    VideoStatus.PROCESSING -> Text("Processing…", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                    VideoStatus.SUCCESS -> Text("Ready · ${video.quality} · ${video.size}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    VideoStatus.FAIL -> Text("Failed", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete")
            }
        }
    }
}
