package com.glm.aiapp.ui.screens.vision

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glm.aiapp.domain.model.Attachment
import com.glm.aiapp.domain.model.AttachmentType
import com.glm.aiapp.domain.repository.SettingsRepository
import com.glm.aiapp.domain.repository.VisionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VisionState(
    val prompt: String = "Describe this image in detail.",
    val selectedUri: Uri? = null,
    val mimeType: String? = null,
    val analysis: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class VisionViewModel @Inject constructor(
    private val visionRepo: VisionRepository,
    private val settingsRepo: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(VisionState())
    val state: StateFlow<VisionState> = _state

    val thinkingEnabled = settingsRepo.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun setPrompt(p: String) { _state.value = _state.value.copy(prompt = p) }
    fun setAttachment(uri: Uri, mimeType: String?) {
        _state.value = _state.value.copy(selectedUri = uri, mimeType = mimeType, analysis = null, error = null)
    }
    fun clearError() { _state.value = _state.value.copy(error = null) }

    fun analyze() {
        val current = _state.value
        val uri = current.selectedUri ?: run {
            _state.value = current.copy(error = "Pick an image first")
            return
        }
        viewModelScope.launch {
            _state.value = current.copy(isLoading = true, error = null, analysis = null)
            try {
                // For real production: upload the file to a storage bucket and pass the URL.
                // Here we pass the content uri as the image_url — the GLM API requires a public URL,
                // so production deployments should swap this for a signed upload URL.
                val attachment = Attachment(
                    id = uri.toString(),
                    type = AttachmentType.IMAGE,
                    url = uri.toString(),
                    mimeType = current.mimeType
                )
                val result = visionRepo.analyze(
                    prompt = current.prompt.ifBlank { "Describe this image." },
                    attachments = listOf(attachment),
                    thinking = thinkingEnabled.value?.chatParams?.thinking?.id == "enabled"
                )
                _state.value = _state.value.copy(analysis = result, isLoading = false)
            } catch (t: Throwable) {
                _state.value = _state.value.copy(error = t.message ?: "Vision analysis failed", isLoading = false)
            }
        }
    }
}
