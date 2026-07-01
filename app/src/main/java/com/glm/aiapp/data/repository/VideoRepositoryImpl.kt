package com.glm.aiapp.data.repository

import com.glm.aiapp.data.api.PlatformClient
import com.glm.aiapp.data.db.AppDatabase
import com.glm.aiapp.data.db.GeneratedVideoEntity
import com.glm.aiapp.domain.model.*
import com.glm.aiapp.domain.repository.SettingsRepository
import com.glm.aiapp.domain.repository.VideoRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Video generation — routed through the platform's `/api/glm/video` proxy
 * (see PlatformClient), not called directly against Zhipu. This keeps the
 * GLM_API_KEY server-side and matches how vision/image/search already work.
 *
 * Video is async: `createTask` returns a task id right away, `pollTask` is
 * called repeatedly (e.g. from a UI-driven timer) until status is SUCCESS.
 */
@Singleton
class VideoRepositoryImpl @Inject constructor(
    private val platform: PlatformClient,
    private val db: AppDatabase,
    private val settingsRepo: SettingsRepository
) : VideoRepository {

    override suspend fun createTask(
        prompt: String,
        quality: VideoQuality,
        size: VideoSize,
        fps: Int,
        duration: Int
    ): GeneratedVideo {
        val s = settingsRepo.settings.first()
        if (s.sessionToken.isBlank()) error("Not signed in. Open Settings → Account → Sign in.")
        val result = platform.createVideoTask(
            s.platformUrl, s.sessionToken, prompt, quality.id, size.id, fps, duration
        )
        val entity = GeneratedVideoEntity(
            id = UUID.randomUUID().toString(),
            prompt = prompt,
            taskId = result.taskId,
            status = result.status,
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
        val s = settingsRepo.settings.first()
        if (s.sessionToken.isBlank()) error("Not signed in. Open Settings → Account → Sign in.")
        val result = platform.pollVideoTask(s.platformUrl, s.sessionToken, taskId)
        val newStatus = VideoStatus.entries.firstOrNull { it.name == result.status } ?: VideoStatus.PROCESSING
        val existing = db.generatedVideoDao().observeAll().first()
            .firstOrNull { it.taskId == taskId } ?: error("Task $taskId not in local DB")
        val updated = existing.copy(
            status = newStatus.name,
            videoUrl = result.videoUrl ?: existing.videoUrl,
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
