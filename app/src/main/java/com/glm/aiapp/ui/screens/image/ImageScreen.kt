package com.glm.aiapp.ui.screens.image

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.AssistChip
import com.glm.aiapp.domain.model.ImageSize
import com.glm.aiapp.ui.components.LoadingState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ImageScreen(vm: ImageViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val gallery by vm.gallery.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = state.prompt,
            onValueChange = vm::setPrompt,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Describe the image you want…") },
            minLines = 2,
            maxLines = 5
        )

        // Size picker — horizontal scrolling chips
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ImageSize.entries.forEach { size ->
                AssistChip(
                    onClick = { vm.setSize(size) },
                    label = { Text(size.label) },
                    leadingIcon = if (state.size == size) {
                        { Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp)) }
                    } else null,
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (state.size == size) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    )
                )
            }
        }

        Button(
            onClick = vm::generate,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isGenerating && state.prompt.isNotBlank()
        ) {
            Icon(Icons.Filled.AutoAwesome, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            Text(if (state.isGenerating) "Generating…" else "Generate")
        }

        state.error?.let { msg ->
            Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Text(msg, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.padding(12.dp))
            }
        }

        if (state.isGenerating) LoadingState(message = "Painting your image…")

        // Latest result
        state.lastGenerated?.let { img -> Base64Image(img.base64, img.prompt) }

        // Gallery
        HorizontalDivider()
        Text("Gallery", style = MaterialTheme.typography.titleMedium)
        if (gallery.isEmpty()) {
            Text("Nothing here yet — your generations will appear in this grid.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(gallery, key = { it.id }) { img ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        onClick = { /* could open detail view */ }
                    ) {
                        Box {
                            Base64Image(img.base64, img.prompt, Modifier.fillMaxWidth().height(160.dp))
                            IconButton(
                                onClick = { vm.delete(img.id) },
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Base64Image(base64: String, description: String, modifier: Modifier = Modifier) {
    val bitmap = remember(base64) {
        runCatching {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }.getOrNull()
    }
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = description,
            modifier = modifier.clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )
    } else {
        Surface(modifier = modifier, color = MaterialTheme.colorScheme.surfaceVariant) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Failed to decode", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
