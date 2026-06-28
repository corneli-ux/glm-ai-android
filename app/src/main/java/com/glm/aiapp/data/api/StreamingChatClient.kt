package com.glm.aiapp.data.api

import com.glm.aiapp.data.dto.StreamChunk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.buffer
import okio.use
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads an SSE stream from the GLM chat/completions endpoint and emits
 * parsed [StreamChunk]s. Closes the connection when the consumer cancels
 * the flow, or when the server sends `data: [DONE]`.
 */
@Singleton
class StreamingChatClient @Inject constructor(
    private val client: OkHttpClient,
    private val json: Json
) {
    fun stream(url: String, apiKey: String, payload: String): Flow<StreamChunk> = channelFlow {
        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $apiKey")
            .header("Accept", "text/event-stream")
            .header("Cache-Control", "no-cache")
            .post(payload.toRequestBody("application/json".toMediaType()))
            .build()

        val call = client.newCall(request)
        try {
            call.execute().use { response: Response ->
                if (!response.isSuccessful) {
                    close(ServerSentEventException("HTTP ${response.code}: ${response.message}"))
                    return@use
                }
                val body = response.body ?: run {
                    close(ServerSentEventException("Empty response body"))
                    return@use
                }
                val source = body.source().buffer()
                while (!isClosedForSend) {
                    val line = source.readUtf8Line() ?: break
                    if (line.isBlank()) continue
                    if (!line.startsWith("data:")) continue
                    val data = line.removePrefix("data:").trim()
                    if (data == "[DONE]") break
                    val chunk = runCatching { json.decodeFromString<StreamChunk>(data) }.getOrNull()
                    if (chunk != null) send(chunk)
                }
            }
        } catch (t: Throwable) {
            close(t)
        }
    }
}

class ServerSentEventException(message: String) : RuntimeException(message)
