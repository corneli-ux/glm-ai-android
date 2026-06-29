package com.glm.aiapp.ui.screens.speech

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.glm.aiapp.domain.model.AudioFormat
import com.glm.aiapp.domain.model.Voice
import com.glm.aiapp.ui.components.LoadingState
import java.io.File

@Composable
fun SpeechScreen(vm: SpeechViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val clips by vm.clips.collectAsStateWithLifecycle()
    val transcriptions by vm.transcriptions.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val pickAudio = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val bytes = context.contentResolver.openInputStream(it)?.readBytes() ?: return@let
            val base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
            vm.setAsrAudio(base64, it.lastPathSegment ?: "audio.wav")
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // TTS section
        Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Text to speech", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = state.ttsText,
                    onValueChange = vm::setTtsText,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Enter text") },
                    minLines = 3
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Voice.entries.forEach { v ->
                        FilterChip(selected = state.voice == v, onClick = { vm.setVoice(v) }, label = { Text(v.label) })
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Speed:", style = MaterialTheme.typography.labelLarge)
                    Slider(
                        value = state.speed,
                        onValueChange = vm::setSpeed,
                        valueRange = 0.5f..2.0f,
                        steps = 14,
                        modifier = Modifier.weight(1f)
                    )
                    Text("${state.speed}x", style = MaterialTheme.typography.labelSmall)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AudioFormat.entries.forEach { f ->
                        FilterChip(selected = state.format == f, onClick = { vm.setFormat(f) }, label = { Text(f.label) })
                    }
                }
                Button(
                    onClick = vm::synthesize,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isSynthesizing && state.ttsText.isNotBlank()
                ) {
                    Icon(Icons.Filled.VolumeUp, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(if (state.isSynthesizing) "Synthesizing…" else "Generate speech")
                }
            }
        }

        if (state.isSynthesizing) LoadingState(message = "Synthesizing audio…")

        // Clips
        if (clips.isNotEmpty()) {
            Text("Recent clips", style = MaterialTheme.typography.titleMedium)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.heightIn(max = 200.dp)) {
                items(clips, key = { it.id }) { clip ->
                    Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surface) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = {
                                clip.audioBase64?.let { vm.togglePlay(clip.id, it) }
                            }) {
                                Icon(
                                    if (state.playingClipId == clip.id) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    contentDescription = "Play"
                                )
                            }
                            Column(Modifier.weight(1f)) {
                                Text(clip.text, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                                Text("${clip.voice} · ${clip.speed}x · ${clip.format}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        HorizontalDivider()

        // ASR section
        Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Speech to text", style = MaterialTheme.typography.titleMedium)
                Text(
                    if (state.asrFileName != null) "Selected: ${state.asrFileName}" else "Pick an audio file to transcribe",
                    style = MaterialTheme.typography.bodySmall
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { pickAudio.launch("audio/*") }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.GraphicEq, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Pick audio")
                    }
                    Button(
                        onClick = vm::transcribe,
                        modifier = Modifier.weight(1f),
                        enabled = state.asrBase64 != null && !state.isTranscribing
                    ) {
                        Icon(Icons.Filled.Mic, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(if (state.isTranscribing) "Transcribing…" else "Transcribe")
                    }
                }
            }
        }

        if (transcriptions.isNotEmpty()) {
            Text("Transcriptions", style = MaterialTheme.typography.titleMedium)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(transcriptions, key = { it.id }) { tr ->
                    Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surface) {
                        Column(Modifier.padding(12.dp)) {
                            Text(tr.fileName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(4.dp))
                            Text(tr.text, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }

        state.error?.let { msg ->
            Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Text(msg, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.padding(12.dp))
            }
        }
    }
}
