package com.glm.aiapp.ui.screens.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glm.aiapp.domain.model.PageReadResult
import com.glm.aiapp.domain.repository.PageReaderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReaderUiState(
    val url: String = "",
    val result: PageReadResult? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val repo: PageReaderRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ReaderUiState())
    val state: StateFlow<ReaderUiState> = _state

    fun setUrl(u: String) { _state.value = _state.value.copy(url = u) }
    fun clearError() { _state.value = _state.value.copy(error = null) }

    fun read() {
        val url = _state.value.url.trim()
        if (url.isBlank()) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val result = repo.read(url)
                _state.value = _state.value.copy(result = result, isLoading = false)
            } catch (t: Throwable) {
                _state.value = _state.value.copy(isLoading = false, error = t.message ?: "Failed to read page")
            }
        }
    }
}
