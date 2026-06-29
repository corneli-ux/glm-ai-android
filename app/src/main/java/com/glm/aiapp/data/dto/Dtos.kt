package com.glm.aiapp.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * DTOs — wire formats for the GLM API (OpenAI-compatible).
 * These are intentionally decoupled from domain entities.
 */

@Serializable
data class ChatCompletionRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val temperature: Float? = null,
    @SerialName("max_tokens") val maxTokens: Int? = null,
    @SerialName("top_p") val topP: Float? = null,
    val stream: Boolean = false,
    val thinking: ThinkingConfig? = null
)

@Serializable
data class ThinkingConfig(val type: String)

@Serializable
data class ChatMessage(
    val role: String,
    val content: JsonElement // string for text chats, array for vision
)

@Serializable
data class ChatCompletionResponse(
    val id: String? = null,
    val model: String? = null,
    val choices: List<Choice> = emptyList(),
    val usage: Usage? = null
)

@Serializable
data class Choice(
    val index: Int = 0,
    val message: ChatMessage? = null,
    val delta: Delta? = null,
    @SerialName("finish_reason") val finishReason: String? = null
)

@Serializable
data class Delta(
    val role: String? = null,
    val content: String? = null,
    val thinking: String? = null
)

@Serializable
data class Usage(
    @SerialName("prompt_tokens") val promptTokens: Int = 0,
    @SerialName("completion_tokens") val completionTokens: Int = 0,
    @SerialName("total_tokens") val totalTokens: Int = 0
)

// Image generation
@Serializable
data class ImageRequest(val prompt: String, val size: String)

@Serializable
data class ImageResponse(val data: List<ImageData> = emptyList())

@Serializable
data class ImageData(val base64: String? = null, val url: String? = null)

// Video generation
@Serializable
data class VideoRequest(
    val prompt: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    val quality: String,
    @SerialName("with_audio") val withAudio: Boolean = false,
    val size: String,
    val fps: Int,
    val duration: Int
)

@Serializable
data class VideoTaskResponse(val id: String, @SerialName("task_status") val taskStatus: String)

@Serializable
data class AsyncResultResponse(
    @SerialName("task_status") val taskStatus: String,
    @SerialName("video_result") val videoResult: List<VideoResultItem> = emptyList(),
    @SerialName("video_url") val videoUrl: String? = null,
    val url: String? = null,
    val video: String? = null
)

@Serializable
data class VideoResultItem(val url: String)

// TTS
@Serializable
data class TtsRequest(
    val input: String,
    val voice: String = "tongtong",
    val speed: Float = 1.0f,
    @SerialName("response_format") val responseFormat: String = "wav",
    val stream: Boolean = false
)

// ASR
@Serializable
data class AsrRequest(@SerialName("file_base64") val fileBase64: String)

@Serializable
data class AsrResponse(val text: String = "")

// Function call (web_search, page_reader)
@Serializable
data class FunctionInvokeRequest(val arguments: String)

// Web search response shape (loose — GLM returns a list of items)
@Serializable
data class WebSearchItem(
    val title: String = "",
    val link: String = "",
    val snippet: String = "",
    val source: String? = null,
    @SerialName("publish_date") val publishDate: String? = null
)

@Serializable
data class WebSearchResponse(val data: List<WebSearchItem> = emptyList())

@Serializable
data class PageReadData(
    val html: String = "",
    val title: String = "",
    val url: String = "",
    @SerialName("publishedTime") val publishedTime: String? = null,
    val usage: PageReadUsage? = null
)

@Serializable
data class PageReadUsage(val tokens: Int = 0)

@Serializable
data class PageReadResponse(val data: PageReadData = PageReadData())

// Generic API error
@Serializable
data class ApiError(val error: ApiErrorBody? = null)

@Serializable
data class ApiErrorBody(val code: String? = null, val message: String? = null)
