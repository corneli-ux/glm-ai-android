package com.glm.aiapp.data.repository

import android.util.Base64
import com.glm.aiapp.data.api.GlmApi
import com.glm.aiapp.data.db.AppDatabase
import com.glm.aiapp.data.db.TranscriptionEntity
import com.glm.aiapp.data.db.VoiceClipEntity
import com.glm.aiapp.data.dto.TtsRequest
import com.glm.aiapp.domain.model.*
import com.glm.aiapp.domain.repository.SpeechRepository
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpeechRepositoryImpl @Inject constructor(
    private val api: GlmApi,
    private val db: AppDatabase
) : SpeechRepository {

    override suspend fun synthesize(
        text: String,
        voice: Voice,
        speed: Float,
        format: AudioFormat
    ): VoiceClip {
        val body = api.textToSpeech(
            TtsRequest(
                input = text,
                voice = voice.id,
                speed = speed,
                responseFormat = format.id,
                stream = false
            )
        )
        val bytes = body.bytes()
        val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
        val entity = VoiceClipEntity(
            id = UUID.randomUUID().toString(),
            text = text,
            voice = voice.id,
            speed = speed,
            format = format.id,
            audioBase64 = base64,
            audioUrl = null,
            createdAt = System.currentTimeMillis()
        )
        db.voiceClipDao().upsert(entity)
        return entity.toDomain()
    }

    override suspend fun transcribe(audioBase64: String, fileName: String): Transcription {
        // Decode base64 → bytes → multipart body
        val bytes = Base64.decode(audioBase64, Base64.NO_WRAP)
        val filePart = MultipartBody.Part.createFormData(
            "file", fileName, bytes.toRequestBody()
        )
        val modelPart = "whisper-1".toRequestBody()
        val response = api.speechToText(filePart, modelPart)
        val entity = TranscriptionEntity(
            id = UUID.randomUUID().toString(),
            text = response.text,
            fileName = fileName,
            durationMs = null,
            createdAt = System.currentTimeMillis()
        )
        db.transcriptionDao().upsert(entity)
        return entity.toDomain()
    }

    override fun observeClips() =
        db.voiceClipDao().observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeTranscriptions() =
        db.transcriptionDao().observeAll().map { list -> list.map { it.toDomain() } }

    private fun VoiceClipEntity.toDomain() = VoiceClip(
        id = id, text = text, voice = voice, speed = speed, format = format,
        audioBase64 = audioBase64, audioUrl = audioUrl, createdAt = createdAt
    )

    private fun TranscriptionEntity.toDomain() = Transcription(
        id = id, text = text, fileName = fileName, durationMs = durationMs, createdAt = createdAt
    )
}
