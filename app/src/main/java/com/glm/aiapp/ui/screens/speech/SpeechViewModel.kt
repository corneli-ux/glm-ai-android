package com.glm.aiapp.ui.screens.speech

import android.media.MediaPlayer
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glm.aiapp.domain.model.AudioFormat
import com.glm.aiapp.domain.model.Voice
import com.glm.aiapp.domain.repository.SpeechRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import javax.inject.Inject

data class SpeechUiState(
    val ttsText: String = "",
    val voice: Voice = Voice.TONGTONG,
    val speed: Float = 1.0f,
    val format: AudioFormat = AudioFormat.WAV,
    val isSynthesizing: Boolean = false,
    val isPlaying: Boolean = false,
    val playingClipId: String? = null,
    val asrBase64: String? = null,
    val asrFileName: String? = null,
    val isTranscribing: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SpeechViewModel @Inject constructor(
    private val repo: SpeechRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SpeechUiState())
    val state: StateFlow<SpeechUiState> = _state

    val clips = repo.observeClips().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val transcriptions = repo.observeTranscriptions().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private var mediaPlayer: MediaPlayer? = null

    fun setTtsText(t: String) { _state.value = _state.value.copy(ttsText = t) }
    fun setVoice(v: Voice) { _state.value = _state.value.copy(voice = v) }
    fun setSpeed(s: Float) { _state.value = _state.value.copy(speed = s) }
    fun setFormat(f: AudioFormat) { _state.value = _state.value.copy(format = f) }
    fun setAsrAudio(base64: String, fileName: String) {
        _state.value = _state.value.copy(asrBase64 = base64, asrFileName = fileName)
    }
    fun clearError() { _state.value = _state.value.copy(error = null) }

    fun synthesize() {
        val text = _state.value.ttsText.trim()
        if (text.isBlank()) {
            _state.value = _state.value.copy(error = "Enter text to synthesize")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(isSynthesizing = true, error = null)
            try {
                repo.synthesize(text, _state.value.voice, _state.value.speed, _state.value.format)
                _state.value = _state.value.copy(isSynthesizing = false)
            } catch (t: Throwable) {
                _state.value = _state.value.copy(isSynthesizing = false, error = t.message ?: "TTS failed")
            }
        }
    }

    fun togglePlay(clipId: String, base64: String) {
        val mp = mediaPlayer
        if (_state.value.playingClipId == clipId && mp?.isPlaying == true) {
            mp.pause()
            _state.value = _state.value.copy(isPlaying = false, playingClipId = null)
            return
        }
        mp?.release()
        try {
            val bytes = Base64.decode(base64, Base64.NO_WRAP)
            val tmp = File.createTempFile("clip_${UUID.randomUUID()}", ".wav")
            tmp.writeBytes(bytes)
            mediaPlayer = MediaPlayer().apply {
                setDataSource(tmp.absolutePath)
                prepare()
                setOnCompletionListener {
                    _state.value = _state.value.copy(isPlaying = false, playingClipId = null)
                    it.release()
                    mediaPlayer = null
                    tmp.delete()
                }
                start()
            }
            _state.value = _state.value.copy(isPlaying = true, playingClipId = clipId)
        } catch (t: Throwable) {
            _state.value = _state.value.copy(error = t.message ?: "Playback failed")
        }
    }

    fun transcribe() {
        val s = _state.value
        val base64 = s.asrBase64 ?: run {
            _state.value = s.copy(error = "Record or pick audio first")
            return
        }
        viewModelScope.launch {
            _state.value = s.copy(isTranscribing = true, error = null)
            try {
                repo.transcribe(base64, s.asrFileName ?: "recording.wav")
                _state.value = _state.value.copy(isTranscribing = false, asrBase64 = null, asrFileName = null)
            } catch (t: Throwable) {
                _state.value = _state.value.copy(isTranscribing = false, error = t.message ?: "Transcription failed")
            }
        }
    }

    override fun onCleared() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
