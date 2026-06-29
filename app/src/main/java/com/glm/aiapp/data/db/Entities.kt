package com.glm.aiapp.data.db

import androidx.room.*

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val systemPrompt: String = "",
    val model: String = "glm-4.6",
    val thinkingEnabled: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(
    tableName = "messages",
    foreignKeys = [ForeignKey(
        entity = ConversationEntity::class,
        parentColumns = ["id"],
        childColumns = ["conversationId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("conversationId")]
)
data class MessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val role: String,
    val content: String,
    val thinking: String? = null,
    val tokens: Int? = null,
    val createdAt: Long
)

@Entity(tableName = "attachments")
data class AttachmentEntity(
    @PrimaryKey val id: String,
    val messageId: String,
    val type: String,
    val url: String,
    val name: String? = null,
    val mimeType: String? = null
)

@Entity(tableName = "generated_images")
data class GeneratedImageEntity(
    @PrimaryKey val id: String,
    val prompt: String,
    val size: String,
    val base64: String,
    val createdAt: Long
)

@Entity(tableName = "generated_videos")
data class GeneratedVideoEntity(
    @PrimaryKey val id: String,
    val prompt: String,
    val taskId: String,
    val status: String,
    val videoUrl: String?,
    val quality: String,
    val size: String,
    val fps: Int,
    val duration: Int,
    val createdAt: Long,
    val completedAt: Long?
)

@Entity(tableName = "voice_clips")
data class VoiceClipEntity(
    @PrimaryKey val id: String,
    val text: String,
    val voice: String,
    val speed: Float,
    val format: String,
    val audioBase64: String?,
    val audioUrl: String?,
    val createdAt: Long
)

@Entity(tableName = "transcriptions")
data class TranscriptionEntity(
    @PrimaryKey val id: String,
    val text: String,
    val fileName: String,
    val durationMs: Long?,
    val createdAt: Long
)

@Entity(tableName = "finetune_datasets")
data class FineTuneDatasetEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val examples: Int,
    val sizeBytes: Long,
    val format: String,
    val createdAt: Long
)

@Entity(tableName = "finetune_jobs")
data class FineTuneJobEntity(
    @PrimaryKey val id: String,
    val name: String,
    val status: String,
    val baseModel: String,
    val datasetName: String,
    val examples: Int,
    val epochs: Int,
    val learningRate: Float,
    val progress: Float,
    val loss: Float?,
    val createdAt: Long
)
