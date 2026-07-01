package com.glm.aiapp.data.api

import com.glm.aiapp.domain.model.AttachmentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Client for the Pullarao AppForge platform's GLM proxy endpoints.
 * All requests carry the student's session JWT — never an API key.
 *
 * This is deliberately the ONLY place in the app that talks to GLM. The
 * platform (pullarao-appforge) holds the real GLM_API_KEY server-side and
 * forwards these calls on the student's behalf — the app never ships or
 * asks for a Zhipu API key. (See `GlmApi.kt` for the raw Zhipu contract
 * this proxies — useful to read, but it should never be called directly
 * from a student's device in production.)
 *
 * Endpoints called:
 *   POST /api/glm/vision       { prompt, imageUrl }                → { text }
 *   POST /api/glm/images       { prompt, size }                    → { base64, size }
 *   POST /api/glm/websearch    { query, num, recency_days }        → { results }
 *   POST /api/glm/pagereader   { url }                             → { result }
 *   POST /api/glm/video        { prompt, quality, size, fps, ... } → { taskId, status }
 *   GET  /api/glm/video?id=... —                                    → { taskId, status, videoUrl }
 *   POST /api/glm/speech/tts   { text, voice, speed, format }      → { audioBase64, format }
 *   POST /api/glm/speech/asr   multipart: file                     → { text }
 */
@Singleton
class PlatformClient @Inject constructor() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .build()

    suspend fun vision(platformUrl: String, token: String, prompt: String, imageUrl: String): String = withContext(Dispatchers.IO) {
        val payload = JSONObject().put("prompt", prompt).put("imageUrl", imageUrl).toString()
        val res = doRequest("${platformUrl.trimEnd('/')}/api/glm/vision", token, payload)
        res.optString("text", "")
    }

    suspend fun generateImage(platformUrl: String, token: String, prompt: String, size: String): String = withContext(Dispatchers.IO) {
        val payload = JSONObject().put("prompt", prompt).put("size", size).toString()
        val res = doRequest("${platformUrl.trimEnd('/')}/api/glm/images", token, payload)
        res.optString("base64", "")
    }

    suspend fun webSearch(platformUrl: String, token: String, query: String, num: Int = 10, recencyDays: Int = 30): List<SearchHit> = withContext(Dispatchers.IO) {
        val payload = JSONObject()
            .put("query", query)
            .put("num", num)
            .put("recency_days", recencyDays)
            .toString()
        val res = doRequest("${platformUrl.trimEnd('/')}/api/glm/websearch", token, payload)
        // The platform returns { results: [...] } where the shape mirrors GLM's web_search response
        val arr = res.optJSONArray("results") ?: JSONArray()
        // GLM returns results as { data: [ { title, link, snippet, ... } ] } or similar — handle both
        val effectiveArr = if (arr.length() == 0) {
            res.optJSONObject("results")?.optJSONArray("data") ?: JSONArray()
        } else arr
        (0 until effectiveArr.length()).map { i ->
            val o = effectiveArr.getJSONObject(i)
            SearchHit(
                title = o.optString("title"),
                url = o.optString("link", o.optString("url")),
                snippet = o.optString("snippet", o.optString("description")),
                source = o.optString("source", null),
                publishedDate = o.optString("publish_date", o.optString("publishedTime", null))
            )
        }
    }

    suspend fun pageReader(platformUrl: String, token: String, url: String): PageRead = withContext(Dispatchers.IO) {
        val payload = JSONObject().put("url", url).toString()
        val res = doRequest("${platformUrl.trimEnd('/')}/api/glm/pagereader", token, payload)
        val data = res.optJSONObject("result")?.optJSONObject("data") ?: JSONObject()
        PageRead(
            title = data.optString("title"),
            url = data.optString("url", url),
            html = data.optString("html"),
            publishedTime = data.optString("publishedTime", null),
            tokens = data.optJSONObject("usage")?.optInt("tokens")
        )
    }

    /** Kicks off an async video render — returns immediately with a task id. */
    suspend fun createVideoTask(
        platformUrl: String, token: String, prompt: String,
        quality: String, size: String, fps: Int, duration: Int, withAudio: Boolean = false
    ): VideoTaskResult = withContext(Dispatchers.IO) {
        val payload = JSONObject()
            .put("prompt", prompt)
            .put("quality", quality)
            .put("size", size)
            .put("fps", fps)
            .put("duration", duration)
            .put("withAudio", withAudio)
            .toString()
        val res = doRequest("${platformUrl.trimEnd('/')}/api/glm/video", token, payload)
        VideoTaskResult(taskId = res.optString("taskId"), status = res.optString("status", "PROCESSING"))
    }

    /** Polls a previously created video task for its current status / result URL. */
    suspend fun pollVideoTask(platformUrl: String, token: String, taskId: String): VideoTaskResult = withContext(Dispatchers.IO) {
        val req = Request.Builder()
            .url("${platformUrl.trimEnd('/')}/api/glm/video?id=$taskId")
            .header("Authorization", "Bearer $token")
            .get()
            .build()
        val res = executeJson(req)
        VideoTaskResult(
            taskId = res.optString("taskId", taskId),
            status = res.optString("status", "PROCESSING"),
            videoUrl = res.optString("videoUrl", null)
        )
    }

    /** Text-to-speech — returns base64-encoded audio bytes ready to decode and play. */
    suspend fun synthesizeSpeech(
        platformUrl: String, token: String, text: String, voice: String, speed: Float, format: String
    ): String = withContext(Dispatchers.IO) {
        val payload = JSONObject()
            .put("text", text)
            .put("voice", voice)
            .put("speed", speed)
            .put("format", format)
            .toString()
        val res = doRequest("${platformUrl.trimEnd('/')}/api/glm/speech/tts", token, payload)
        res.optString("audioBase64", "")
    }

    /** Speech-to-text — uploads raw audio bytes as multipart, returns the transcript. */
    suspend fun transcribeSpeech(
        platformUrl: String, token: String, audioBytes: ByteArray, fileName: String
    ): String = withContext(Dispatchers.IO) {
        val body = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart(
                "file", fileName,
                audioBytes.toRequestBody("application/octet-stream".toMediaType())
            )
            .build()
        val req = Request.Builder()
            .url("${platformUrl.trimEnd('/')}/api/glm/speech/asr")
            .header("Authorization", "Bearer $token")
            .post(body)
            .build()
        val res = executeJson(req)
        res.optString("text", "")
    }

    private fun executeJson(req: Request): JSONObject {
        client.newCall(req).execute().use { res ->
            val body = res.body?.string() ?: ""
            if (!res.isSuccessful) {
                val msg = runCatching { JSONObject(body).optString("error", "HTTP ${res.code}") }.getOrDefault("HTTP ${res.code}")
                throw RuntimeException(msg)
            }
            return JSONObject(body)
        }
    }

    private fun doRequest(url: String, token: String, payload: String): JSONObject {
        val req = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
            .post(payload.toRequestBody("application/json".toMediaType()))
            .build()
        return executeJson(req)
    }
}

data class SearchHit(
    val title: String,
    val url: String,
    val snippet: String,
    val source: String?,
    val publishedDate: String?
)

data class PageRead(
    val title: String,
    val url: String,
    val html: String,
    val publishedTime: String?,
    val tokens: Int?
)

data class VideoTaskResult(
    val taskId: String,
    val status: String,
    val videoUrl: String? = null
)
