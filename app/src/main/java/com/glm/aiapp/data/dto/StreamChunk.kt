package com.glm.aiapp.data.dto

import kotlinx.serialization.Serializable

/**
 * SSE event payload sent by the GLM streaming chat endpoint.
 * Each `data: {...}` line decodes to one of these.
 */
@Serializable
data class StreamChunk(
    val id: String? = null,
    val model: String? = null,
    val choices: List<Choice> = emptyList(),
    val usage: Usage? = null
)
