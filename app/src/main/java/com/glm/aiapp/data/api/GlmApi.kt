package com.glm.aiapp.data.api

import com.glm.aiapp.data.dto.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.*

/**
 * Retrofit interface for the raw GLM API (OpenAI-compatible endpoints),
 * pointed directly at Zhipu's `open.bigmodel.cn`.
 *
 * NOTE: in production this app does NOT call Zhipu directly for any
 * capability — see `PlatformClient` for the routes actually used. This
 * interface documents the raw wire contract for reference (useful reading
 * if you're implementing the same routes on your own backend), and exists
 * as a hook for local development against a personal API key if you ever
 * want to bypass the platform. Base URL is configurable from Settings —
 * defaults to Zhipu's hosted endpoint.
 */
interface GlmApi {

    // ---- Chat completions ----

    @POST("chat/completions")
    suspend fun chatCompletion(@Body body: ChatCompletionRequest): ChatCompletionResponse

    @Streaming
    @POST("chat/completions")
    suspend fun chatCompletionStream(@Body body: ChatCompletionRequest): ResponseBody

    // ---- Vision (same endpoint, multimodal content array) ----

    @POST("chat/completions")
    suspend fun visionChat(@Body body: ChatCompletionRequest): ChatCompletionResponse

    // ---- Image generation ----

    @POST("images/generations")
    suspend fun generateImage(@Body body: ImageRequest): ImageResponse

    // ---- Video generation (async) ----

    @POST("videos/generations")
    suspend fun createVideoTask(@Body body: VideoRequest): VideoTaskResponse

    @GET("async-result/{id}")
    suspend fun queryAsyncResult(@Path("id") id: String): AsyncResultResponse

    // ---- Audio: TTS / ASR ----

    @Streaming
    @POST("audio/speech")
    suspend fun textToSpeech(@Body body: TtsRequest): ResponseBody

    @Multipart
    @POST("audio/transcriptions")
    suspend fun speechToText(
        @Part file: MultipartBody.Part,
        @Part("model") model: RequestBody
    ): AsrResponse

    // ---- Functions: web_search / page_reader ----

    @POST("web_search")
    suspend fun webSearch(@Body body: Map<String, @JvmSuppressWildcards Any>): WebSearchResponse

    @POST("page_reader")
    suspend fun pageReader(@Body body: Map<String, @JvmSuppressWildcards Any>): PageReadResponse
}
