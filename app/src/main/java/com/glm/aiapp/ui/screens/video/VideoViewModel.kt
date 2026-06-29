package com.glm.aiapp.ui.screens.video

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glm.aiapp.domain.model.GeneratedVideo
import com.glm.aiapp.domain.model.VideoQuality
import com.glm.aiapp.domain.model.VideoSize
import com.glm.aiapp.domain.model.VideoStatus
import com.glm.aiapp.domain.repository.VideoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VideoUiState(
    val prompt: String = "",
    val quality: VideoQuality = VideoQuality.SPEED,
    val size: VideoSize = VideoSize.HD_LANDSCAPE,
    val fps: Int = 30,
    val duration: Int = 5,
    val isCreating: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class VideoViewModel @Inject constructor(
    private val repo: VideoRepository
) : ViewModel() {

    private val _state = MutableStateFlow(VideoUiState())
    val state: StateFlow<VideoUiState> = _state

    val videos: StateFlow<List<GeneratedVideo>> = repo.observeVideos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setPrompt(p: String) { _state.value = _state.value.copy(prompt = p) }
    fun setQuality(q: VideoQuality) { _state.value = _state.value.copy(quality = q) }
    fun setSize(s: VideoSize) { _state.value = _state.value.copy(size = s) }
    fun setFps(f: Int) { _state.value = _state.value.copy(fps = f) }
    fun setDuration(d: Int) { _state.value = _state.value.copy(duration = d) }
    fun clearError() { _state.value = _state.value.copy(error = null) }

    fun create() {
        val prompt = _state.value.prompt.trim()
        if (prompt.isBlank()) {
            _state.value = _state.value.copy(error = "Describe the video first")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(isCreating = true, error = null)
            try {
                val video = repo.createTask(prompt, _state.value.quality, _state.value.size, _state.value.fps, _state.value.duration)
                _state.value = _state.value.copy(isCreating = false)
                // Start polling
                pollUntilDone(video.taskId)
            } catch (t: Throwable) {
                _state.value = _state.value.copy(isCreating = false, error = t.message ?: "Failed to create video task")
            }
        }
    }

    private fun pollUntilDone(taskId: String) {
        viewModelScope.launch {
            var attempts = 0
            while (attempts < 60) {
                delay(5_000)
                try {
                    val v = repo.pollTask(taskId)
                    if (v.status != VideoStatus.PROCESSING) break
                } catch (_: Throwable) { /* swallow poll errors */ }
                attempts++
            }
        }
    }

    fun delete(id: String) { viewModelScope.launch { repo.deleteVideo(id) } }
}
