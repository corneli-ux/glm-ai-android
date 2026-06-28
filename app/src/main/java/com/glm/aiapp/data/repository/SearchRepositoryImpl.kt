package com.glm.aiapp.data.repository

import com.glm.aiapp.data.api.GlmApi
import com.glm.aiapp.domain.model.SearchResult
import com.glm.aiapp.domain.repository.SearchRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepositoryImpl @Inject constructor(
    private val api: GlmApi
) : SearchRepository {

    override suspend fun search(query: String, num: Int, recencyDays: Int): List<SearchResult> {
        val response = api.webSearch(
            mapOf("query" to query, "num" to num, "recency_days" to recencyDays)
        )
        return response.data.map { item ->
            SearchResult(
                title = item.title,
                url = item.link,
                snippet = item.snippet,
                source = item.source,
                publishedDate = item.publishDate
            )
        }
    }
}
