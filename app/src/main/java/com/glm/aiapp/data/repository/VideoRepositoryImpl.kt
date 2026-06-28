package com.glm.aiapp.data.repository

import com.glm.aiapp.data.api.GlmApi
import com.glm.aiapp.data.db.AppDatabase
import com.glm.aiapp.data.db.GeneratedVideoEntity
import com.glm.aiapp.data.dto.VideoRequest
import com.glm.aiapp.domain.model.*
import com.glm.aiapp.domain.repository.VideoRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoRepositoryImpl @Inject constructor(
    private val api: GlmApi,
    private val db: AppDatabase
) : VideoRepository {

    override suspend fun createTask(
        prompt: String,
        quality: VideoQuality,
        size: VideoSize,
        fps: Int,
        duration: Int
    ): GeneratedVideo {
        val response = api.createVideoTask(
            VideoRequest(
                prompt = prompt,
                quality = quality.id,
                size = size.id,
                fps = fps,
                duration = duration
            )
        )
        val entity = GeneratedVideoEntity(
            id = UUID.randomUUID().toString(),
            prompt = prompt,
            taskId = response.id,
            status = response.taskStatus,
            videoUrl = null,
            quality = quality.id,
            size = size.id,
            fps = fps,
            duration = duration,
            createdAt = System.currentTimeMillis(),
            completedAt = null
        )
        db.generatedVideoDao().upsert(entity)
        return entity.toDomain()
    }

    override suspend fun pollTask(taskId: String): GeneratedVideo {
        val response = api.queryAsyncResult(taskId)
        val newStatus = VideoStatus.entries.firstOrNull { it.name == response.taskStatus }
            ?: VideoStatus.PROCESSING
        val url = response.videoResult.firstOrNull()?.url
            ?: response.videoUrl ?: response.url ?: response.video
        // Find existing row, update, persist
        val existing = db.generatedVideoDao().observeAll().first()
            .firstOrNull { it.taskId == taskId } ?: error("Task $taskId not in local DB")
        val updated = existing.copy(
            status = newStatus.name,
            videoUrl = url ?: existing.videoUrl,
            completedAt = if (newStatus == VideoStatus.SUCCESS) System.currentTimeMillis() else null
        )
        db.generatedVideoDao().upsert(updated)
        return updated.toDomain()
    }

    override fun observeVideos() =
        db.generatedVideoDao().observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun deleteVideo(id: String) = db.generatedVideoDao().delete(id)

    private fun GeneratedVideoEntity.toDomain() = GeneratedVideo(
        id = id,
        prompt = prompt,
        taskId = taskId,
        status = VideoStatus.entries.firstOrNull { it.name == status } ?: VideoStatus.PROCESSING,
        videoUrl = videoUrl,
        quality = quality,
        size = size,
        fps = fps,
        duration = duration,
        createdAt = createdAt,
        completedAt = completedAt
    )
}
