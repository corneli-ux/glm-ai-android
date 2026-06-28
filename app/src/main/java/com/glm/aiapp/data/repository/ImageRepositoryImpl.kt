package com.glm.aiapp.data.repository

import com.glm.aiapp.data.api.GlmApi
import com.glm.aiapp.data.db.AppDatabase
import com.glm.aiapp.data.db.GeneratedImageEntity
import com.glm.aiapp.data.dto.ImageRequest
import com.glm.aiapp.domain.model.GeneratedImage
import com.glm.aiapp.domain.model.ImageSize
import com.glm.aiapp.domain.repository.ImageRepository
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageRepositoryImpl @Inject constructor(
    private val api: GlmApi,
    private val db: AppDatabase
) : ImageRepository {

    override suspend fun generate(prompt: String, size: ImageSize): GeneratedImage {
        val response = api.generateImage(ImageRequest(prompt = prompt, size = size.id))
        val base64 = response.data.firstOrNull()?.base64 ?: error("Image generation returned no data")
        val entity = GeneratedImageEntity(
            id = UUID.randomUUID().toString(),
            prompt = prompt,
            size = size.id,
            base64 = base64,
            createdAt = System.currentTimeMillis()
        )
        db.generatedImageDao().upsert(entity)
        return entity.toDomain()
    }

    override fun observeGallery() =
        db.generatedImageDao().observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun deleteImage(id: String) = db.generatedImageDao().delete(id)

    private fun GeneratedImageEntity.toDomain() = GeneratedImage(
        id = id, prompt = prompt, size = size, base64 = base64, createdAt = createdAt
    )
}
