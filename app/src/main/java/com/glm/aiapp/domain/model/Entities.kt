package com.glm.aiapp.domain.model

/**
 * Domain entities — pure Kotlin, no Android or framework dependencies.
 * These are the objects the app reasons about. They live in `domain` and are
 * converted to/from DTOs at the data layer boundary.
 */

data class Conversation(
    val id: String,
    val title: String,
    val systemPrompt: String = "",
    val model: String = "glm-4.6",
    val thinkingEnabled: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long,
    val messages: List<Message> = emptyList()
)

data class Message(
    val id: String,
    val conversationId: String,
    val role: Role,
    val content: String,
    val thinking: String? = null,
    val attachments: List<Attachment> = emptyList(),
    val tokens: Int? = null,
    val createdAt: Long
)

enum class Role { SYSTEM, USER, ASSISTANT }

data class Attachment(
    val id: String,
    val type: AttachmentType,
    val url: String,
    val name: String? = null,
    val mimeType: String? = null
)

enum class AttachmentType { IMAGE, FILE, VIDEO }

data class GeneratedImage(
    val id: String,
    val prompt: String,
    val size: String,
    val base64: String,
    val createdAt: Long
)

data class GeneratedVideo(
    val id: String,
    val prompt: String,
    val taskId: String,
    val status: VideoStatus,
    val videoUrl: String? = null,
    val quality: String,
    val size: String,
    val fps: Int,
    val duration: Int,
    val createdAt: Long,
    val completedAt: Long? = null
)

enum class VideoStatus { PROCESSING, SUCCESS, FAIL }

data class VoiceClip(
    val id: String,
    val text: String,
    val voice: String,
    val speed: Float,
    val format: String,
    val audioBase64: String? = null,
    val audioUrl: String? = null,
    val createdAt: Long
)

data class Transcription(
    val id: String,
    val text: String,
    val fileName: String,
    val durationMs: Long? = null,
    val createdAt: Long
)

data class SearchResult(
    val title: String,
    val url: String,
    val snippet: String,
    val source: String? = null,
    val publishedDate: String? = null
)

data class PageReadResult(
    val title: String,
    val url: String,
    val html: String,
    val publishedTime: String? = null,
    val tokens: Int? = null
)

data class FineTuneDataset(
    val id: String,
    val name: String,
    val description: String,
    val examples: Int,
    val sizeBytes: Long,
    val format: String,
    val createdAt: Long
)

data class FineTuneJob(
    val id: String,
    val name: String,
    val status: FineTuneStatus,
    val baseModel: String,
    val datasetName: String,
    val examples: Int,
    val epochs: Int,
    val learningRate: Float,
    val progress: Float = 0f,
    val loss: Float? = null,
    val createdAt: Long
)

enum class FineTuneStatus { DRAFT, QUEUED, TRAINING, COMPLETED, FAILED }
