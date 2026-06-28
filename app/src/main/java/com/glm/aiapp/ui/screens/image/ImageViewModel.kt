package com.glm.aiapp.ui.screens.image

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glm.aiapp.domain.model.GeneratedImage
import com.glm.aiapp.domain.model.ImageSize
import com.glm.aiapp.domain.repository.ImageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ImageUiState(
    val prompt: String = "",
    val size: ImageSize = ImageSize.SQUARE,
    val isGenerating: Boolean = false,
    val error: String? = null,
    val lastGenerated: GeneratedImage? = null
)

@HiltViewModel
class ImageViewModel @Inject constructor(
    private val repo: ImageRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ImageUiState())
    val state: StateFlow<ImageUiState> = _state

    val gallery: StateFlow<List<GeneratedImage>> = repo.observeGallery()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setPrompt(p: String) { _state.value = _state.value.copy(prompt = p) }
    fun setSize(s: ImageSize) { _state.value = _state.value.copy(size = s) }
    fun clearError() { _state.value = _state.value.copy(error = null) }

    fun generate() {
        val prompt = _state.value.prompt.trim()
        if (prompt.isBlank()) {
            _state.value = _state.value.copy(error = "Describe what you want to generate")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(isGenerating = true, error = null)
            try {
                val img = repo.generate(prompt, _state.value.size)
                _state.value = _state.value.copy(isGenerating = false, lastGenerated = img)
            } catch (t: Throwable) {
                _state.value = _state.value.copy(isGenerating = false, error = t.message ?: "Image generation failed")
            }
        }
    }

    fun delete(id: String) {
        viewModelScope.launch { repo.deleteImage(id) }
    }
}
