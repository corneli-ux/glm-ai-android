package com.glm.aiapp.data.repository

import com.glm.aiapp.data.api.GlmApi
import com.glm.aiapp.data.dto.ChatCompletionRequest
import com.glm.aiapp.data.dto.ChatMessage
import com.glm.aiapp.data.dto.ThinkingConfig
import com.glm.aiapp.domain.model.Attachment
import com.glm.aiapp.domain.repository.SettingsRepository
import com.glm.aiapp.domain.repository.VisionRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VisionRepositoryImpl @Inject constructor(
    private val api: GlmApi,
    private val settingsRepo: SettingsRepository,
    private val json: Json
) : VisionRepository {

    override suspend fun analyze(prompt: String, attachments: List<Attachment>, thinking: Boolean): String {
        val settings = settingsRepo.settings.first()
        val content = buildJsonArray {
            add(buildJsonObject { put("type", "text"); put("text", prompt) })
            attachments.forEach { att ->
                val type = when (att.type) {
                    com.glm.aiapp.domain.model.AttachmentType.IMAGE -> "image_url"
                    com.glm.aiapp.domain.model.AttachmentType.VIDEO -> "video_url"
                    com.glm.aiapp.domain.model.AttachmentType.FILE -> "file_url"
                }
                add(buildJsonObject {
                    put(type, buildJsonObject { put("url", att.url) })
                })
            }
        }
        val request = ChatCompletionRequest(
            model = "glm-4.6",
            messages = listOf(ChatMessage("user", content)),
            stream = false,
            thinking = ThinkingConfig(if (thinking) "enabled" else "disabled")
        )
        val response = api.visionChat(request)
        return response.choices.firstOrNull()?.message?.content?.let { raw ->
            runCatching {
                val element = Json.decodeFromString<JsonElement>(raw.toString())
                element.jsonPrimitive.content
            }.getOrElse { raw.toString() }
        } ?: ""
    }
}
