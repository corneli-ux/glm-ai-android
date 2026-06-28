package com.glm.aiapp.ui.screens.finetune

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glm.aiapp.domain.model.FineTuneDataset
import com.glm.aiapp.domain.model.FineTuneJob
import com.glm.aiapp.domain.model.FineTuneStatus
import com.glm.aiapp.domain.repository.FineTuneRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FineTuneUiState(
    val newJobName: String = "",
    val newBaseModel: String = "glm-4.6",
    val newDatasetId: String = "",
    val newEpochs: Int = 3,
    val newLearningRate: Float = 5e-5f,
    val error: String? = null
)

@HiltViewModel
class FineTuneViewModel @Inject constructor(
    private val repo: FineTuneRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FineTuneUiState())
    val state: StateFlow<FineTuneUiState> = _state

    val datasets: StateFlow<List<FineTuneDataset>> = repo.observeDatasets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val jobs: StateFlow<List<FineTuneJob>> = repo.observeJobs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setName(n: String) { _state.value = _state.value.copy(newJobName = n) }
    fun setBaseModel(m: String) { _state.value = _state.value.copy(newBaseModel = m) }
    fun setDataset(id: String) { _state.value = _state.value.copy(newDatasetId = id) }
    fun setEpochs(e: Int) { _state.value = _state.value.copy(newEpochs = e) }
    fun setLearningRate(lr: Float) { _state.value = _state.value.copy(newLearningRate = lr) }
    fun clearError() { _state.value = _state.value.copy(error = null) }

    fun createJob() {
        val s = _state.value
        if (s.newJobName.isBlank() || s.newDatasetId.isBlank()) {
            _state.value = s.copy(error = "Name and dataset are required")
            return
        }
        viewModelScope.launch {
            try {
                repo.createJob(s.newJobName, s.newBaseModel, s.newDatasetId, s.newEpochs, s.newLearningRate)
                _state.value = s.copy(newJobName = "", newDatasetId = "")
            } catch (t: Throwable) {
                _state.value = s.copy(error = t.message ?: "Failed to queue job")
            }
        }
    }

    fun refresh(id: String) {
        viewModelScope.launch {
            runCatching { repo.refreshJobStatus(id) }
                .onFailure { _state.value = _state.value.copy(error = it.message) }
        }
    }
}
