package com.glm.aiapp.data.repository

import com.glm.aiapp.data.api.GlmApi
import com.glm.aiapp.domain.model.PageReadResult
import com.glm.aiapp.domain.repository.PageReaderRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PageReaderRepositoryImpl @Inject constructor(
    private val api: GlmApi
) : PageReaderRepository {

    override suspend fun read(url: String): PageReadResult {
        val response = api.pageReader(mapOf("url" to url))
        return PageReadResult(
            title = response.data.title,
            url = response.data.url,
            html = response.data.html,
            publishedTime = response.data.publishedTime,
            tokens = response.data.usage?.tokens
        )
    }
}
