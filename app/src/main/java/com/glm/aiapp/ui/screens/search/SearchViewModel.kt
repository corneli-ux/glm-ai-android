package com.glm.aiapp.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glm.aiapp.domain.model.SearchResult
import com.glm.aiapp.domain.repository.SearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val results: List<SearchResult> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repo: SearchRepository
) : ViewModel() {
    private val _state = MutableStateFlow(SearchUiState())
    val state: StateFlow<SearchUiState> = _state

    fun setQuery(q: String) { _state.value = _state.value.copy(query = q) }
    fun clearError() { _state.value = _state.value.copy(error = null) }

    fun search() {
        val q = _state.value.query.trim()
        if (q.isBlank()) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val results = repo.search(q, num = 10, recencyDays = 30)
                _state.value = _state.value.copy(results = results, isLoading = false)
            } catch (t: Throwable) {
                _state.value = _state.value.copy(isLoading = false, error = t.message ?: "Search failed")
            }
        }
    }
}
